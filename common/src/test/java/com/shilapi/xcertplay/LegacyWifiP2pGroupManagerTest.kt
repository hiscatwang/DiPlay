package com.shilapi.xcertplay

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import com.shilapi.xcertplay.network.P2pResetRequiredException
import com.shilapi.xcertplay.network.WifiP2pGroupManager
import com.shilapi.xcertplay.network.WirelessHotspotInfo
import com.shilapi.xcertplay.network.WirelessHotspotBackend
import com.shilapi.xcertplay.airplay.*
import com.shilapi.xcertplay.orchestration.*
import com.shilapi.xcertplay.transport.Iap2IdentificationConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowWifiP2pManager
import java.net.InetAddress
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/** Runs against the actual API 27/28 framework, which lacks the API 29 P2P methods. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27, 28], manifest = Config.NONE,
    shadows = [LegacyWifiP2pGroupManagerTest.LegacyRadio::class, WifiP2pGroupManagerTest.P2pChannel::class])
class LegacyWifiP2pGroupManagerTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val radio get() = shadowOf(context.getSystemService(WifiP2pManager::class.java)) as LegacyRadio
    private val ownership get() = context.getSharedPreferences("carplay_wifi_p2p", Context.MODE_PRIVATE)

    @Before fun setup() {
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        context.getSystemService(WifiManager::class.java).apply {
            isWifiEnabled = true
            shadowOf(connectionInfo).setFrequency(5180)
            shadowOf(connectionInfo).setSupplicantState(SupplicantState.COMPLETED)
        }
    }

    @Test fun usesFrameworkCredentialsAndIpv4WithoutInventingAChannel() {
        WifiP2pGroupManager(context).use { manager ->
            val info = background { manager.start(5000) }
            assertEquals("DIRECT-system-legacy", info.ssid)
            assertEquals("legacy-generated-key", info.passphrase)
            assertEquals("192.168.49.1", info.hostAddress!!.hostAddress)
            assertEquals("p2p-test-missing", info.interfaceName)
            assertEquals(0, info.channel)
            assertNull(info.frequencyMHz)
            assertEquals("Auto", info.bandLabel)
            assertEquals(info.ssid, ownership.getString("owned_ssid", null))
            manager.onCarPlayConfirmed()
            assertNull(context.getSharedPreferences("carplay_wifi_p2p_success", 0).getString("confirmed", null))
        }
        assertEquals(1, radio.requests)
        assertEquals(1, radio.removals)
        assertNull(radio.group)
    }

    @Test fun controllerHonorsWifiDirectInsteadOfSilentlyStartingLocalOnlyHotspot() {
        val controller = CarPlayController(
            object : ContextWrapper(context) {
                override fun getApplicationContext(): Context = this
                override fun bindService(service: Intent, connection: ServiceConnection, flags: Int) = false
            },
            CarPlayRuntimeConfig(mfiTarget = MfiTarget.LOCAL, wirelessHotspotMode = WirelessHotspotMode.WIFI_P2P,
                identification = Iap2IdentificationConfig("Test", "Test", "Test", "Test", "1", "1", 0)),
            AirPlayConfig("Test", "02:00:00:00:00:01", "02:00:00:00:00:01", "1", AirPlayDisplayConfig(800, 480)),
            AirPlayIdentity.generate(), PairingStore(), object : AirPlaySessionListener {},
            CarPlayMediaEngine(object : MediaSink {}), {})
        try {
            val phase = controller.javaClass.getDeclaredField("phase").apply { isAccessible = true }
            phase.set(controller, phase.type.enumConstants.single { it.toString() == "WIRELESS" })
            val generation = controller.javaClass.getDeclaredField("wirelessGeneration").apply { isAccessible = true }
                .get(controller) as AtomicInteger
            val start = controller.javaClass.getDeclaredMethod("startWirelessHotspot", Int::class.javaPrimitiveType)
                .apply { isAccessible = true }
            val info = background { start.invoke(controller, generation.get()) as WirelessHotspotInfo }
            assertEquals(WirelessHotspotBackend.WIFI_P2P, info.backend)
            assertEquals(1, radio.requests)
        } finally { controller.close(); controller.awaitClosed(5000) }
    }

    @Test fun legacyBusyRetriesOnlyOnceWithoutWalkingModernFrequencyPlan() {
        radio.rejection = WifiP2pManager.BUSY
        WifiP2pGroupManager(context).use { manager ->
            assertTrue(failure { manager.start(3000) }.message!!.contains("busy"))
        }
        assertEquals(2, radio.requests)
        assertEquals(0, radio.removals)
    }

    @Test fun explicitlyUnsupportedRadioStopsImmediately() {
        radio.rejection = WifiP2pManager.P2P_UNSUPPORTED
        WifiP2pGroupManager(context).use { manager ->
            assertTrue(failure { manager.start(3000) }.message!!.contains("unsupported"))
        }
        assertEquals(1, radio.requests)
    }

    @Test fun unansweredCreationTimesOutWithoutIssuingDuplicateRequest() {
        radio.noReply = true
        WifiP2pGroupManager(context).use { manager ->
            assertTrue(failure { manager.start(150) }.message!!.contains("group creation"))
        }
        assertEquals(1, radio.requests)
    }

    @Test fun closingWhileWaitingCancelsStartupPromptly() {
        radio.noReply = true
        val manager = WifiP2pGroupManager(context)
        val executor = Executors.newSingleThreadExecutor()
        try {
            val future = executor.submit<Any> { manager.start(30000) }
            assertTrue(radio.requested.await(3, TimeUnit.SECONDS))
            manager.close()
            try { future.get(3, TimeUnit.SECONDS); fail("Expected cancellation") }
            catch (error: ExecutionException) { assertTrue(error.cause!!.message!!.contains("closed")) }
        } finally { manager.close(); executor.shutdownNow() }
        assertEquals(1, radio.requests)
    }

    @Test fun absentPasswordCannotBecomeAPartiallyUsableGroup() {
        radio.missingCredentials = true
        WifiP2pGroupManager(context).use { manager ->
            assertTrue(failure { manager.start(150) }.message!!.contains("usable Wi-Fi P2P group"))
        }
        assertEquals(1, radio.removals)
    }

    @Test fun reclaimsOnlyAnExactlyRememberedLegacyGroup() {
        radio.group = radio.makeGroup()
        ownership.edit().putString("owned_ssid", radio.group!!.networkName).commit()
        WifiP2pGroupManager(context).use { manager -> background { manager.start(5000) } }
        assertEquals(1, radio.requests)
        assertEquals(2, radio.removals)
    }

    @Test fun foreignGroupIsNeverReclaimed() {
        radio.group = radio.makeGroup()
        WifiP2pGroupManager(context).use { manager ->
            assertTrue(failure { manager.start(3000) } is P2pResetRequiredException)
        }
        assertEquals(0, radio.requests)
        assertEquals(0, radio.removals)
    }

    @Test fun closeDoesNotRemoveAnotherAppsReplacementGroup() {
        val manager = WifiP2pGroupManager(context)
        background { manager.start(5000) }
        radio.group = radio.makeGroup().also { shadowOf(it).setNetworkName("DIRECT-other-app") }
        background { manager.close() }
        assertEquals(0, radio.removals)
        assertNotNull(radio.group)
    }

    @Test fun missingPermissionStopsBeforeCreatingAnything() {
        shadowOf(context).denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        WifiP2pGroupManager(context).use { manager ->
            assertTrue(failure { manager.start(3000) }.message!!.contains("Location"))
        }
        assertEquals(0, radio.requests)
    }

    private fun failure(block: () -> Any): Throwable {
        try { background(block); fail("Expected failure") }
        catch (error: ExecutionException) { return error.cause!! }
        error("unreachable")
    }

    private fun <T> background(block: () -> T): T {
        val executor = Executors.newSingleThreadExecutor()
        return try { executor.submit<T> { block() }.get(10, TimeUnit.SECONDS) }
        finally { executor.shutdownNow() }
    }

    @Implements(WifiP2pManager::class)
    class LegacyRadio : ShadowWifiP2pManager() {
        var group: WifiP2pGroup? = null
        var requests = 0
        var removals = 0
        var rejection: Int? = null
        var missingCredentials = false
        var noReply = false
        val requested = CountDownLatch(1)

        // Deliberately only override the API 14 overload. Calls to newer methods
        // cannot succeed on these API 27/28 framework jars.
        @Implementation override fun createGroup(channel: WifiP2pManager.Channel, listener: WifiP2pManager.ActionListener) {
            requests++
            requested.countDown()
            if (noReply) return
            rejection?.let { listener.onFailure(it); return }
            group = makeGroup()
            listener.onSuccess()
        }

        @Implementation override fun requestGroupInfo(channel: WifiP2pManager.Channel, listener: WifiP2pManager.GroupInfoListener) {
            listener.onGroupInfoAvailable(group)
        }

        @Implementation override fun requestConnectionInfo(channel: WifiP2pManager.Channel, listener: WifiP2pManager.ConnectionInfoListener) {
            listener.onConnectionInfoAvailable(WifiP2pInfo().apply {
                groupFormed = true
                isGroupOwner = true
                groupOwnerAddress = InetAddress.getByName("192.168.49.1")
            })
        }

        @Implementation override fun removeGroup(channel: WifiP2pManager.Channel, listener: WifiP2pManager.ActionListener) {
            removals++
            group = null
            listener.onSuccess()
        }

        fun makeGroup() = WifiP2pGroup().apply {
            shadowOf(this).setIsGroupOwner(true)
            shadowOf(this).setNetworkName("DIRECT-system-legacy")
            shadowOf(this).setPassphrase(if (missingCredentials) null else "legacy-generated-key")
            shadowOf(this).setInterface("p2p-test-missing")
        }
    }
}
