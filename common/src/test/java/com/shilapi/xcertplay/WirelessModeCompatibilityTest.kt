package com.shilapi.xcertplay

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.shilapi.xcertplay.host.R
import com.shilapi.xcertplay.orchestration.WirelessHotspotMode
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27, 28])
class WirelessModeCompatibilityTest {
    @Test fun oldAndroidPreservesWifiDirectSelection() {
        val context = RuntimeEnvironment.getApplication()
        AirPlayPersistence.saveWirelessHotspotMode(context, WirelessHotspotMode.WIFI_P2P)
        assertEquals(WirelessHotspotMode.WIFI_P2P, AirPlayPersistence.loadWirelessHotspotMode(context))
        assertEquals("WIFI_P2P", context.getSharedPreferences("xcertplay_airplay", 0).getString("wireless_hotspot_mode", null))
    }

    @Test fun connectionControlsOfferWifiDirectAndExplainSystemSelectedFrequencyOnOldAndroid() {
        val activity = Robolectric.buildActivity(DiPlayActivity::class.java).get()
        val parent = LinearLayout(activity)
        DiPlayActivity::class.java.getDeclaredMethod("wirelessLinkControls", LinearLayout::class.java)
            .apply { isAccessible = true }.invoke(activity, parent)
        fun texts(view: View): List<String> = when (view) {
            is TextView -> listOf(view.text.toString())
            is ViewGroup -> (0 until view.childCount).flatMap { texts(view.getChildAt(it)) }
            else -> emptyList()
        }
        assertTrue(texts(parent).any { it.contains(activity.getString(R.string.existing_wifi_title)) })
        assertTrue(texts(parent).any { it.contains(activity.getString(R.string.wifi_direct)) })
        assertTrue(texts(parent).any { it == activity.getString(R.string.hotspot_mode_p2p_legacy_desc) })
        assertTrue(texts(parent).any { it.contains(activity.getString(R.string.built_in_car_hotspot)) })
    }

    @Test @Config(sdk = [29])
    fun android10StillOffersAndPreservesWifiDirect() {
        val context = RuntimeEnvironment.getApplication()
        assertTrue(WirelessHotspotMode.WIFI_P2P in AirPlayPersistence.availableWirelessHotspotModes())
        AirPlayPersistence.saveWirelessHotspotMode(context, WirelessHotspotMode.WIFI_P2P)
        assertEquals(WirelessHotspotMode.WIFI_P2P, AirPlayPersistence.loadWirelessHotspotMode(context))
    }
}
