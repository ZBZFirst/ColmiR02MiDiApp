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

    val x = AxisConfig()
    val y = AxisConfig()
    val z = AxisConfig()

    fun setAxisEnabled(axis: Char, enabled: Boolean) {
        axisConfig(axis).enabled = enabled
    }

    fun setAxisRange(axis: Char, minHz: Float, maxHz: Float) {
        val a = axisConfig(axis)
        a.minHz = minHz.coerceIn(20f, 2000f)
        a.maxHz = maxHz.coerceIn(a.minHz + 1f, 2000f)
    }

    fun mapRotToTones(rot: Vec3): ToneMapping {
        val fx = mapAxis(rot.a, x)
        val fy = mapAxis(rot.b, y)
        val fz = mapAxis(rot.c, z)

        return ToneMapping(
            fx = fx,
            fy = fy,
            fz = fz,
            gx = if (x.enabled) 1f else 0f,
            gy = if (y.enabled) 1f else 0f,
            gz = if (z.enabled) 1f else 0f,
        )
    }

    private fun axisConfig(axis: Char): AxisConfig = when (axis.uppercaseChar()) {
        'X' -> x
        'Y' -> y
        'Z' -> z
        else -> error("Unknown axis: $axis")
    }

    private fun mapAxis(value: Float, config: AxisConfig): Float {
        val norm = (value / 255f).coerceIn(0f, 1f)
        val hz = config.minHz + (config.maxHz - config.minHz) * norm
        return hz.roundToInt().toFloat()
    }
}
