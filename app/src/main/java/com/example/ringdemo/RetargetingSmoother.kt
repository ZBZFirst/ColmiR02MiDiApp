package com.example.ringdemo

import kotlin.math.max

data class Vec3(val a: Float, val b: Float, val c: Float)

private fun lerp(a: Float, b: Float, u: Float) = a + (b - a) * u
private fun lerp3(a: Vec3, b: Vec3, u: Float) =
    Vec3(lerp(a.a, b.a, u), lerp(a.b, b.b, u), lerp(a.c, b.c, u))

private fun smoothstep(u: Float): Float {
    val x = u.coerceIn(0f, 1f)
    return x * x * (3f - 2f * x)
}

class RetargetingSmoother(
    var maxInterpSec: Float = 1.5f,
    private val useSmoothstep: Boolean = true
) {
    private var curRot: Vec3? = null
    private var curG: Vec3? = null

    private var startRot: Vec3? = null
    private var startG: Vec3? = null
    private var targetRot: Vec3? = null
    private var targetG: Vec3? = null

    private var t0Sec: Double = 0.0

    fun ingest(newRot: Vec3, newG: Vec3, nowSec: Double) {
        if (curRot == null) {
            curRot = newRot; curG = newG
            startRot = newRot; startG = newG
            targetRot = newRot; targetG = newG
            t0Sec = nowSec
            return
        }

        // Catch up current output to NOW
        val uRaw = ((nowSec - t0Sec) / max(1e-3, maxInterpSec.toDouble())).toFloat()
        val u = if (useSmoothstep) smoothstep(uRaw) else uRaw.coerceIn(0f, 1f)

        curRot = lerp3(startRot!!, targetRot!!, u)
        curG = lerp3(startG!!, targetG!!, u)

        // Retarget from current output
        startRot = curRot; startG = curG
        targetRot = newRot; targetG = newG
        t0Sec = nowSec
    }

    fun sample(nowSec: Double): Pair<Vec3, Vec3>? {
        val sRot = startRot ?: return null
        val sG = startG ?: return null
        val tRot = targetRot ?: return null
        val tG = targetG ?: return null

        val uRaw = ((nowSec - t0Sec) / max(1e-3, maxInterpSec.toDouble())).toFloat()
        val u = if (useSmoothstep) smoothstep(uRaw) else uRaw.coerceIn(0f, 1f)

        val rot = lerp3(sRot, tRot, u)
        val g = lerp3(sG, tG, u)

        curRot = rot; curG = g
        return rot to g
    }
}
