package com.example.ringdemo

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.PI
import kotlin.math.sin

/**
 * 3-voice sine synth using AudioTrack (STREAM).
 * Update frequencies anytime via setFrequencies().
 */
class ToneEngine(
    private val sampleRateHz: Int = 48000,
    private val bufferFrames: Int = 480   // ~10ms @ 48k
) {
    private val running = AtomicBoolean(false)

    private data class Freqs(val fx: Float, val fy: Float, val fz: Float)
    private val freqsRef = AtomicReference(Freqs(440f, 440f, 440f))

    private var audioTrack: AudioTrack? = null
    private var worker: Thread? = null

    // oscillator phase in radians
    private var phX = 0.0
    private var phY = 0.0
    private var phZ = 0.0

    fun isRunning(): Boolean = running.get()

    fun setFrequencies(fxHz: Float, fyHz: Float, fzHz: Float) {
        // Keep it audible and safe-ish
        val fx = fxHz.coerceIn(20f, 2000f)
        val fy = fyHz.coerceIn(20f, 2000f)
        val fz = fzHz.coerceIn(20f, 2000f)
        freqsRef.set(Freqs(fx, fy, fz))
    }

    fun start() {
        if (running.getAndSet(true)) return

        val minBytes = AudioTrack.getMinBufferSize(
            sampleRateHz,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val desiredBytes = bufferFrames * 2 // mono PCM16 => 2 bytes/frame
        val bufferBytes = maxOf(minBytes, desiredBytes)

        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(sampleRateHz)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            bufferBytes,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        ).apply { play() }

        worker = Thread(::runAudioLoop, "ToneEngine").apply { start() }
    }

    fun stop() {
        running.set(false)
        try { worker?.join(250) } catch (_: Exception) {}
        worker = null

        try { audioTrack?.pause() } catch (_: Exception) {}
        try { audioTrack?.flush() } catch (_: Exception) {}
        try { audioTrack?.stop() } catch (_: Exception) {}
        try { audioTrack?.release() } catch (_: Exception) {}
        audioTrack = null
    }

    private fun runAudioLoop() {
        val track = audioTrack ?: return
        val pcm = ShortArray(bufferFrames)

        // Gain management:
        // Sum of 3 sines can clip. Keep per-voice gain low.
        val voiceGain = 0.18 // 3 voices => ~0.54 peak-ish

        val twoPi = 2.0 * PI

        while (running.get()) {
            val f = freqsRef.get()
            val stepX = twoPi * f.fx / sampleRateHz
            val stepY = twoPi * f.fy / sampleRateHz
            val stepZ = twoPi * f.fz / sampleRateHz

            for (i in 0 until bufferFrames) {
                val s =
                    voiceGain * sin(phX) +
                            voiceGain * sin(phY) +
                            voiceGain * sin(phZ)

                // Convert [-1,1] float-ish to PCM16
                val v = (s * 32767.0).toInt().coerceIn(-32768, 32767)
                pcm[i] = v.toShort()

                phX += stepX
                phY += stepY
                phZ += stepZ
                if (phX >= twoPi) phX -= twoPi
                if (phY >= twoPi) phY -= twoPi
                if (phZ >= twoPi) phZ -= twoPi
            }

            // Write blocking; keeps timing stable
            track.write(pcm, 0, pcm.size)
        }
    }
}
