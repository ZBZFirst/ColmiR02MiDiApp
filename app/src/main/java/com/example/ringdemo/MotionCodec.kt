package com.example.ringdemo

import kotlin.math.atan2
import kotlin.math.sqrt

data class MotionSample(
    val rotX: Int,
    val rotY: Int,
    val rotZ: Int,
    val ax: Float,
    val ay: Float,
    val az: Float,
)

object MotionCodec {

    private fun int12(u12: Int): Int = if (u12 > 2047) u12 - 4096 else u12

    private fun raw12FromBytes(hi: Int, lo: Int): Int {
        val u12 = ((hi shl 4) or (lo and 0x0F)) and 0x0FFF
        return int12(u12)
    }

    private fun convertRawToG(raw: Int, rangeG: Float = 4f): Float {
        return (raw / 2048f) * rangeG
    }

    fun decodeType3Motion(bytes: ByteArray): MotionSample? {
        if (bytes.size < 8) return null
        if ((bytes[1].toInt() and 0xFF) != 3) return null

        val b2 = bytes[2].toInt() and 0xFF
        val b3 = bytes[3].toInt() and 0xFF
        val b4 = bytes[4].toInt() and 0xFF
        val b5 = bytes[5].toInt() and 0xFF
        val b6 = bytes[6].toInt() and 0xFF
        val b7 = bytes[7].toInt() and 0xFF

        val rawY = raw12FromBytes(b2, b3)
        val rawZ = raw12FromBytes(b4, b5)
        val rawX = raw12FromBytes(b6, b7)

        val ax = convertRawToG(rawX)
        val ay = convertRawToG(rawY)
        val az = convertRawToG(rawZ)

        val ratio = 127.0 / Math.PI
        val rotX = ((atan2(ax.toDouble(), sqrt((ay * ay + az * az).toDouble())) + Math.PI / 2) * ratio).toInt()
        val rotY = ((atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble())) + Math.PI / 2) * ratio).toInt()
        val rotZ = ((atan2(az.toDouble(), sqrt((ax * ax + ay * ay).toDouble())) + Math.PI / 2) * ratio).toInt()

        return MotionSample(rotX, rotY, rotZ, ax, ay, az)
    }
}
