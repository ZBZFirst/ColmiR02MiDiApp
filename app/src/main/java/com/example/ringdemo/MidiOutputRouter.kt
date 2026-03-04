package com.example.ringdemo

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager

class MidiOutputRouter(
    context: Context,
    private val onLog: (String) -> Unit,
) {
    private val midiManager: MidiManager? =
        context.getSystemService(Context.MIDI_SERVICE) as? MidiManager

    private var device: MidiDevice? = null
    private var inputPort: MidiInputPort? = null
    private var lastNoPortLogMs: Long = 0L

    fun connectFirstAvailable() {
        val manager = midiManager ?: run {
            onLog("MIDI unavailable on this device")
            return
        }

        if (inputPort != null) return

        val infos = manager.devices
        if (infos.isEmpty()) {
            onLog("No MIDI devices found")
            return
        }

        val info = infos.first()
        manager.openDevice(info) { opened ->
            if (opened == null) {
                onLog("Failed to open MIDI device")
                return@openDevice
            }
            device = opened
            val port = opened.openInputPort(0)
            if (port == null) {
                onLog("MIDI input port unavailable")
                return@openDevice
            }
            inputPort = port
            onLog("MIDI connected: ${info.properties}")
        }
    }

    fun send(events: List<MidiMapper.MidiEvent>) {
        val port = inputPort
        if (port == null) {
            val now = System.currentTimeMillis()
            if (now - lastNoPortLogMs > 2000L) {
                lastNoPortLogMs = now
                onLog("MIDI send skipped: no connected endpoint")
            }
            return
        }

        val timestamp = System.nanoTime()
        for (e in events) {
            val msg = when (e) {
                is MidiMapper.MidiEvent.ControlChange -> byteArrayOf(
                    (0xB0 or (e.channel and 0x0F)).toByte(),
                    (e.cc and 0x7F).toByte(),
                    (e.value and 0x7F).toByte(),
                )

                is MidiMapper.MidiEvent.NoteOn -> byteArrayOf(
                    (0x90 or (e.channel and 0x0F)).toByte(),
                    (e.note and 0x7F).toByte(),
                    (e.velocity and 0x7F).toByte(),
                )

                is MidiMapper.MidiEvent.NoteOff -> byteArrayOf(
                    (0x80 or (e.channel and 0x0F)).toByte(),
                    (e.note and 0x7F).toByte(),
                    (e.velocity and 0x7F).toByte(),
                )
            }
            port.send(msg, 0, msg.size, timestamp)
        }
    }

    fun close() {
        try { inputPort?.close() } catch (_: Exception) {}
        inputPort = null
        try { device?.close() } catch (_: Exception) {}
        device = null
    }
}
