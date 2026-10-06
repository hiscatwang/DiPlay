package com.shilapi.xcertplay.network

import java.io.IOException
import org.junit.Assert.*
import org.junit.Test

class ManualHotspotReadinessTest {
    private var now = 0L
    private var closed = false
    private var enabled: Boolean? = false
    private val messages = mutableListOf<String>()
    private fun waitFor(
        timeout: Long = 120_000,
        state: () -> Boolean? = { enabled },
        probe: () -> String? = { "ap0" },
        sleep: (Long) -> Unit = { now += it },
    ) = awaitManualHotspot(timeout, { closed }, state, probe, messages::add, nanoTime = { now }, sleepNanos = sleep)

    @Test fun movingInterfaceMustSettleBeforeItIsPublished() {
        val result = awaitManualHotspot(5_000, { false }, { true },
            { if (now < 1_000_000_000L) "old-address" else "new-address" }, messages::add,
            stableSamples = 3, nanoTime = { now }, sleepNanos = { now += it })
        assertEquals("new-address", result)
        assertEquals(2_000_000_000L, now)
    }

    @Test fun timeoutHasRecoverableClassification() {
        val error = assertThrows(WirelessStartupException::class.java) { waitFor(timeout = 10) }
        assertEquals(WirelessStartupFailure.HOTSPOT_NOT_READY, error.reason)
    }

    @Test fun coldBootCanTakeNinetySecondsAndLogsOnlyStateChanges() {
        var probes = 0
        assertEquals("ap0", waitFor(state = { now >= 90_000_000_000L }, probe = { probes++; "ap0" }))
        assertEquals(90_000_000_000L, now)
        assertEquals(1, probes)
        assertEquals(2, messages.size)
    }

    @Test fun waitsForAddressAfterApEnableInsteadOfUsingDisabledStaleInterface() {
        val probes = mutableListOf<Long>()
        assertEquals("ap0", waitFor(state = { now >= 1_000_000_000L }, probe = {
            probes += now
            if (now >= 2_000_000_000L) "ap0" else null
        }))
        assertEquals(listOf(1_000_000_000L, 1_500_000_000L, 2_000_000_000L), probes)
    }

    @Test fun alreadyEnabledAndHiddenStateDoNotAddStartupDelay() {
        assertEquals("ap0", waitFor(state = { true }))
        assertEquals("ap0", waitFor(state = { null }))
        assertEquals(0L, now)
    }

    @Test fun aHiddenStateStillWaitsForAnInterface() {
        assertEquals("ap0", waitFor(state = { null }, probe = { if (now == 0L) null else "ap0" }))
        assertEquals(500_000_000L, now)
    }

    @Test fun disabledHotspotTimesOutWithoutAcceptingItsRetainedAddress() {
        val error = assertThrows(IOException::class.java) {
            waitFor(timeout = 1_100, probe = { fail("Disabled AP must not use a stale address"); "ap0" })
        }
        assertTrue(error.message!!.contains("system AP not enabled yet"))
        assertEquals(1_100_000_000L, now)
    }

    @Test fun enabledApWithoutAddressTimesOutWithAnAccurateReason() {
        val error = assertThrows(IOException::class.java) {
            waitFor(timeout = 500, state = { true }, probe = { null })
        }
        assertTrue(error.message!!.contains("address not ready"))
    }

    @Test fun cancellationWhileWaitingDoesNotLaunchWhenHotspotLaterEnables() {
        val error = assertThrows(IOException::class.java) {
            waitFor(sleep = { now += it; closed = true; enabled = true })
        }
        assertTrue(error.message!!.contains("cancelled"))
        assertFalse(messages.any { it.startsWith("Manual hotspot ready") })
    }

    @Test fun cancellationDuringProbeDiscardsItsResult() {
        assertThrows(IOException::class.java) {
            waitFor(state = { true }, probe = { closed = true; "ap0" })
        }
    }

    @Test fun interruptedWorkerStopsAndPreservesInterruptFlag() {
        try {
            val error = assertThrows(IOException::class.java) { waitFor(sleep = { throw InterruptedException() }) }
            assertTrue(error.cause is InterruptedException)
            assertTrue(Thread.currentThread().isInterrupted)
        } finally { Thread.interrupted() }
    }
}
