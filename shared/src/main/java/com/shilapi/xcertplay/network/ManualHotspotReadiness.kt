package com.shilapi.xcertplay.network

import java.io.IOException
import java.util.concurrent.TimeUnit

/** Wait on the connection worker; never enable/disable the system-owned hotspot. */
internal fun <T : Any> awaitManualHotspot(
    timeoutMillis: Long,
    isClosed: () -> Boolean,
    isEnabled: () -> Boolean?,
    findInterface: () -> T?,
    onDiagnostic: (String) -> Unit,
    stableSamples: Int = 1,
    stableKey: (T) -> Any = { it },
    nanoTime: () -> Long = System::nanoTime,
    sleepNanos: (Long) -> Unit = TimeUnit.NANOSECONDS::sleep,
): T {
    require(stableSamples > 0)
    require(timeoutMillis > 0) { "timeoutMillis must be positive" }
    val started = nanoTime()
    val timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis)
    var lastReason: String? = null
    var lastKey: Any? = null
    var samples = 0
    while (true) {
        if (isClosed()) throw IOException("Manual hotspot startup cancelled")
        val enabled = isEnabled()
        // Some firmware retains an addressed wlan interface while its AP is disabled.
        // If the AP state API is hidden, preserve the interface-based fallback.
        val candidate = if (enabled == false) null else findInterface()
        if (isClosed()) throw IOException("Manual hotspot startup cancelled")
        val key = candidate?.let(stableKey)
        samples = if (key == null) 0 else if (key == lastKey) samples + 1 else 1
        lastKey = key
        if (candidate != null && samples >= stableSamples) {
            onDiagnostic("Manual hotspot ready after ${(nanoTime() - started) / 1_000_000}ms")
            return candidate
        }
        val reason = if (enabled == false) "system AP not enabled yet" else "local hotspot address not ready"
        if (reason != lastReason) {
            onDiagnostic("Manual hotspot waiting: $reason")
            lastReason = reason
        }
        val remaining = timeoutNanos - (nanoTime() - started)
        if (remaining <= 0) {
            throw WirelessStartupException(WirelessStartupFailure.HOTSPOT_NOT_READY, "Timed out after ${timeoutMillis}ms waiting for the manual hotspot: $reason")
        }
        try {
            sleepNanos(minOf(remaining, TimeUnit.MILLISECONDS.toNanos(500)))
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("Interrupted while waiting for the manual hotspot", interrupted)
        }
    }
}
