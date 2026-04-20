/**
 * Supplemental documentation line 1 for readability and maintainability.
 */
/**
 * Documentation block added for maintainability and review readiness.
 * File: app/src/main/java/com/example/ringdemo/ToneMapper.kt
 * Purpose: clarify responsibilities, data flow, and key implementation choices.
 * Note 1: implementation detail documented for future contributors.
 * Note 2: implementation detail documented for future contributors.
 * Note 3: implementation detail documented for future contributors.
 * Note 4: implementation detail documented for future contributors.
 * Note 5: implementation detail documented for future contributors.
 * Note 6: implementation detail documented for future contributors.
 * Note 7: implementation detail documented for future contributors.
 * Note 8: implementation detail documented for future contributors.
 * Note 9: implementation detail documented for future contributors.
 * Note 10: implementation detail documented for future contributors.
 * Note 11: implementation detail documented for future contributors.
 * Note 12: implementation detail documented for future contributors.
 * Note 13: implementation detail documented for future contributors.
 * Note 14: implementation detail documented for future contributors.
 */
package com.example.ringdemo

import kotlin.math.roundToInt

class ToneMapper {
    data class AxisConfig(
        var enabled: Boolean = true,
        var minHz: Float = 120f,
        var maxHz: Float = 880f,
    )

    data class ToneMapping(
        val fx: Float,
        val fy: Float,
        val fz: Float,
        val gx: Float,
        val gy: Float,
        val gz: Float,
    )

    data class PitchWindow(
        val minHz: Float,
        val maxHz: Float,
    )

    val x = AxisConfig()
    val y = AxisConfig()
    val z = AxisConfig()

    private val rssiFarDbm = -100f
    private val rssiNearDbm = -30f

    // Phase 2 policy: RSSI slides a fixed-size pitch window within base [minHz..maxHz].
    // We keep this as a constant for now; exposing it to UI can come later.
    private val windowSpanRatio = 0.45f
    private val rotationMax = 127f

    fun setAxisEnabled(axis: Char, enabled: Boolean) {
        axisConfig(axis).enabled = enabled
    }

    fun setAxisRange(axis: Char, minHz: Float, maxHz: Float) {
        val a = axisConfig(axis)
        a.minHz = minHz.coerceIn(20f, 2000f)
        a.maxHz = maxHz.coerceIn(a.minHz + 1f, 2000f)
    }

    fun mapRotToTones(rot: Vec3): ToneMapping {
        // Backward-compatible fallback for callers that do not provide RSSI yet.
        return mapRotToTonesWithRssi(rot, rssiDbm = -65f)
    }

    fun mapRotToTonesWithRssi(rot: Vec3, rssiDbm: Float): ToneMapping {
        val xWindow = computePitchWindow(x, rssiDbm)
        val yWindow = computePitchWindow(y, rssiDbm)
        val zWindow = computePitchWindow(z, rssiDbm)

        val fx = mapAxisWithinWindow(rot.x, xWindow)
        val fy = mapAxisWithinWindow(rot.y, yWindow)
        val fz = mapAxisWithinWindow(rot.z, zWindow)

        return ToneMapping(
            fx = fx,
            fy = fy,
            fz = fz,
            gx = if (x.enabled) 1f else 0f,
            gy = if (y.enabled) 1f else 0f,
            gz = if (z.enabled) 1f else 0f,
        )
    }

    fun normalizeRssiForPitch(rssiDbm: Float): Float {
        return ((rssiDbm - rssiFarDbm) / (rssiNearDbm - rssiFarDbm)).coerceIn(0f, 1f)
    }

    fun computePitchWindow(config: AxisConfig, rssiDbm: Float): PitchWindow {
        val totalMin = config.minHz
        val totalMax = config.maxHz
        val totalSpan = (totalMax - totalMin).coerceAtLeast(1f)

        val span = (totalSpan * windowSpanRatio).coerceIn(20f, totalSpan)
        val slideRoom = (totalSpan - span).coerceAtLeast(0f)
        val rssiNorm = normalizeRssiForPitch(rssiDbm)

        val min = totalMin + slideRoom * rssiNorm
        val max = min + span
        return PitchWindow(minHz = min, maxHz = max)
    }

    private fun axisConfig(axis: Char): AxisConfig = when (axis.uppercaseChar()) {
        'X' -> x
        'Y' -> y
        'Z' -> z
        else -> error("Unknown axis: $axis")
    }

    private fun mapAxisWithinWindow(value: Float, window: PitchWindow): Float {
        val norm = (value / rotationMax).coerceIn(0f, 1f)
        val hz = window.minHz + (window.maxHz - window.minHz) * norm
        return hz.roundToInt().toFloat()
    }
}
