package com.example.ringdemo

import java.util.UUID

object Protocol {
    // === Device identity ===
    const val targetAddress: String = "30:35:47:33:DA:00"
    const val targetName: String = "R02_DA00"

    // === Notify UUIDs ===
    val notifyUuids: List<UUID> = listOf(
        UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"),
        UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7"),
        UUID.fromString("0000fea1-0000-1000-8000-00805f9b34fb"),
        UUID.fromString("0000fea2-0000-1000-8000-00805f9b34fb"),
    )

    // === Candidate write UUIDs ===
    val cmdWriteUuids: List<UUID> = listOf(
        UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"),
        UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7"),
        UUID.fromString("0000fea2-0000-1000-8000-00805f9b34fb"),
    )

    // === Commands ===
    const val START_RAW_HEX = "A10404"
    const val STOP_RAW_HEX = "A102"
    const val STOP_CAMERA_HEX = "0206"
    const val REBOOT_HEX = "08"

    // === Frame format ===
    private const val FRAME_SIZE = 16

    fun rebootFrame08(): ByteArray {
        // OTA tool shows: 08 00..00 08
        val frame = ByteArray(FRAME_SIZE) { 0 }
        frame[0] = 0x08
        frame[15] = 0x08
        return frame
    }

    fun framedCommandFor(hex: String): ByteArray {
        val clean = hex.replace(" ", "").replace("-", "").trim().uppercase()
        return if (clean == REBOOT_HEX) rebootFrame08() else framedCommand(clean)
    }


    fun framedCommand(hex: String): ByteArray {
        val clean = hex.replace(" ", "").replace("-", "").trim()
        val cmd = clean.chunked(2).map { it.toInt(16).toByte() }

        require(cmd.size <= FRAME_SIZE - 1) { "Command too long for $FRAME_SIZE-byte frame: $hex" }

        val frame = ByteArray(FRAME_SIZE) { 0 }
        for (i in cmd.indices) frame[i] = cmd[i]

        var sum = 0
        for (i in 0 until 15) sum = (sum + (frame[i].toInt() and 0xFF)) and 0xFF
        frame[15] = sum.toByte()

        return frame
    }
}
