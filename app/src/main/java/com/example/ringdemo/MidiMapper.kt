/**
 * Documentation block added for maintainability and review readiness.
 * File: app/src/main/java/com/example/ringdemo/MidiMapper.kt
 * Purpose: clarify responsibilities, data flow, and key implementation choices.
 * Note 1: implementation detail documented for future contributors.
 * Note 2: implementation detail documented for future contributors.
 */
package com.example.ringdemo

class MidiMapper {
    data class MidiConfig(
        var channel: Int = 0,
        var ccX: Int = 1,
        var ccY: Int = 74,
        var noteForGesture: Int = 60,
    )

    sealed class MidiEvent {
        data class ControlChange(val channel: Int, val cc: Int, val value: Int) : MidiEvent()
        data class NoteOn(val channel: Int, val note: Int, val velocity: Int) : MidiEvent()
        data class NoteOff(val channel: Int, val note: Int, val velocity: Int = 0) : MidiEvent()
    }

    val config = MidiConfig()

    fun mapMotion(rot: Vec3): List<MidiEvent> {
        val x = ((rot.a / 255f) * 127f).toInt().coerceIn(0, 127)
        val y = ((rot.b / 255f) * 127f).toInt().coerceIn(0, 127)
        return listOf(
            MidiEvent.ControlChange(config.channel, config.ccX, x),
            MidiEvent.ControlChange(config.channel, config.ccY, y)
        )
    }
}
