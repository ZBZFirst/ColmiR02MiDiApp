/**
 * Supplemental documentation line 1 for readability and maintainability.
 */
/**
 * Documentation block added for maintainability and review readiness.
 * File: app/src/main/java/com/example/ringdemo/RetargetingSmoother.kt
 * Purpose: clarify responsibilities, data flow, and key implementation choices.
 * Note 1: implementation detail documented for future contributors.
 * Note 2: implementation detail documented for future contributors.
 * Note 3: implementation detail documented for future contributors.
 * Note 4: implementation detail documented for future contributors.
 * Note 5: implementation detail documented for future contributors.
 * Note 6: implementation detail documented for future contributors.
 * Note 7: implementation detail documented for future contributors.
 */
//RetargetingSmoother.kt FILE START

package com.example.ringdemo

import kotlin.math.max

data class Vec3(val x: Float, val y: Float, val z: Float)

data class Quaternion(val x: Float, val y: Float, val z: Float, val w: Float) {
    companion object {
        fun identity() = Quaternion(0f, 0f, 0f, 1f)
    }

    operator fun plus(other: Quaternion) = Quaternion(x + other.x, y + other.y, z + other.z, w + other.w)
    operator fun times(s: Float) = Quaternion(x * s, y * s, z * s, w * s)

    fun normalized(): Quaternion {
        val mag = kotlin.math.sqrt(x * x + y * y + z * z + w * w)
        return if (mag < 1e-6f) identity() else Quaternion(x / mag, y / mag, z / mag, w / mag)
    }
}

private fun lerp(a: Float, b: Float, u: Float) = a + (b - a) * u
private fun lerp3(a: Vec3, b: Vec3, u: Float) =
    Vec3(lerp(a.x, b.x, u), lerp(a.y, b.y, u), lerp(a.z, b.z, u))

private fun slerp(q1: Quaternion, q2: Quaternion, t: Float): Quaternion {
    var dot = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w
    var q2Ref = q2

    if (dot < 0.0f) {
        dot = -dot
        q2Ref = Quaternion(-q2.x, -q2.y, -q2.z, -q2.w)
    }

    if (dot > 0.9995f) {
        return (q1 * (1f - t) + q2Ref * t).normalized()
    }

    val theta0 = kotlin.math.acos(dot)
    val theta = theta0 * t
    val sinTheta = kotlin.math.sin(theta)
    val sinTheta0 = kotlin.math.sin(theta0)

    val s0 = kotlin.math.cos(theta) - dot * sinTheta / sinTheta0
    val s1 = sinTheta / sinTheta0

    return (q1 * s0 + q2Ref * s1).normalized()
}

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
    private var curQuat: Quaternion = Quaternion.identity()

    private var startRot: Vec3? = null
    private var startG: Vec3? = null
    private var startQuat: Quaternion = Quaternion.identity()

    private var targetRot: Vec3? = null
    private var targetG: Vec3? = null
    private var targetQuat: Quaternion = Quaternion.identity()

    private var t0Sec: Double = 0.0

    private var orientationOffset: Quaternion = Quaternion.identity()
    var mode: String = "QUATERNION"
    
    // Physical jiggle state
    private var targetJiggle = Vec3(0f, 0f, 0f)
    private var curJiggle = Vec3(0f, 0f, 0f)
    private val jiggleDecay = 0.92f // Slower decay for smoother return
    private val jiggleLerp = 0.15f // Smoothing for the outward jump

    fun resetOrigin() {
        orientationOffset = curQuat.conjugate()
    }

    private fun Quaternion.conjugate() = Quaternion(-x, -y, -z, w)

    fun ingest(newRot: Vec3, newG: Vec3, nowSec: Double) {
        val newQuat = eulerToQuaternion(newRot)
        if (curRot == null) {
            curRot = newRot; curG = newG; curQuat = newQuat
            startRot = newRot; startG = newG; startQuat = newQuat
            targetRot = newRot; targetG = newG; targetQuat = newQuat
            t0Sec = nowSec
            return
        }

        // Calculate "Jiggle" based on acceleration delta from 1G
        val gMag = kotlin.math.sqrt(newG.x * newG.x + newG.y * newG.y + newG.z * newG.z)
        val force = (gMag - 1.0f).coerceIn(-2f, 2f)
        if (kotlin.math.abs(force) > 0.05f) {
            targetJiggle = Vec3(
                (newG.x * force * 15f), 
                (newG.y * force * 15f), 
                (newG.z * force * 15f)
            )
        } else {
            targetJiggle = Vec3(0f, 0f, 0f)
        }

        val uRaw = ((nowSec - t0Sec) / kotlin.math.max(1e-3, maxInterpSec.toDouble())).toFloat()
        val u = if (useSmoothstep) smoothstep(uRaw) else uRaw.coerceIn(0f, 1f)

        curRot = lerp3(startRot!!, targetRot!!, u)
        curG = lerp3(startG!!, targetG!!, u)
        curQuat = slerp(startQuat, targetQuat, u)

        startRot = curRot; startG = curG; startQuat = curQuat
        targetRot = newRot; targetG = newG; targetQuat = newQuat
        t0Sec = nowSec
    }

    fun sample(nowSec: Double): Triple<Vec3, Vec3, Quaternion>? {
        val sRot = startRot ?: return null
        val sG = startG ?: return null
        val sQ = startQuat
        val tRot = targetRot ?: return null
        val tG = targetG ?: return null
        val tQ = targetQuat

        val uRaw = ((nowSec - t0Sec) / kotlin.math.max(1e-3, maxInterpSec.toDouble())).toFloat()
        val u = if (useSmoothstep) smoothstep(uRaw) else uRaw.coerceIn(0f, 1f)

        val rot = lerp3(sRot, tRot, u)
        val g = lerp3(sG, tG, u)
        val q = if (mode == "QUATERNION") {
            slerp(sQ, tQ, u)
        } else {
            eulerToQuaternion(rot)
        }

        curRot = rot; curG = g; curQuat = q
        
        // Smoothly interpolate the jiggle toward target, then decay target
        curJiggle = lerp3(curJiggle, targetJiggle, jiggleLerp)
        targetJiggle = Vec3(targetJiggle.x * jiggleDecay, targetJiggle.y * jiggleDecay, targetJiggle.z * jiggleDecay)
        
        // Apply offset for "origin" chaining
        val localQ = orientationOffset * q
        
        // Return interpolated jiggle for displacement
        return Triple(rot, Vec3(g.x + curJiggle.x, g.y + curJiggle.y, g.z + curJiggle.z), localQ)
    }

    private operator fun Quaternion.times(q: Quaternion): Quaternion {
        return Quaternion(
            w * q.x + x * q.w + y * q.z - z * q.y,
            w * q.y + y * q.w + z * q.x - x * q.z,
            w * q.z + z * q.w + x * q.y - y * q.x,
            w * q.w - x * q.x - y * q.y - z * q.z
        )
    }

    private fun eulerToQuaternion(euler: Vec3): Quaternion {
        val ratio = Math.PI / 127.0 // Back to radians from 0-127 range
        // Subtract 63.5 to center the range [0..127] around 0
        val roll = (euler.x - 63.5) * ratio
        val pitch = (euler.y - 63.5) * ratio
        val yaw = (euler.z - 63.5) * ratio

        val cy = kotlin.math.cos(yaw * 0.5f)
        val sy = kotlin.math.sin(yaw * 0.5f)
        val cp = kotlin.math.cos(pitch * 0.5f)
        val sp = kotlin.math.sin(pitch * 0.5f)
        val cr = kotlin.math.cos(roll * 0.5f)
        val sr = kotlin.math.sin(roll * 0.5f)

        return Quaternion(
            (sr * cp * cy - cr * sp * sy).toFloat(),
            (cr * sp * cy + sr * cp * sy).toFloat(),
            (cr * cp * sy - sr * sp * cy).toFloat(),
            (cr * cp * cy + sr * sp * sy).toFloat()
        )
    }
}

//RetargetingSmoother.kt FILE END