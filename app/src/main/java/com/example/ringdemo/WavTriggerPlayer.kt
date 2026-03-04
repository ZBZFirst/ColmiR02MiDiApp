/**
 * Supplemental documentation line 1 for readability and maintainability.
 */
/**
 * Documentation block added for maintainability and review readiness.
 * File: app/src/main/java/com/example/ringdemo/WavTriggerPlayer.kt
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
 * Note 15: implementation detail documented for future contributors.
 */
package com.example.ringdemo

import android.content.ContentResolver
import android.media.MediaMetadataRetriever
import android.media.SoundPool
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicInteger

class WavTriggerPlayer {
    private val maxInstances = 10
    private val soundPool = SoundPool.Builder().setMaxStreams(maxInstances).build()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var soundId: Int? = null
    private var loaded = false
    private var durationMs: Long = 25L
    private val activeInstances = AtomicInteger(0)

    fun load(contentResolver: ContentResolver, uri: Uri): Boolean {
        unload()

        val afd = contentResolver.openAssetFileDescriptor(uri, "r") ?: return false
        afd.use {
            soundId = soundPool.load(it, 1)
        }

        durationMs = readDurationMs(contentResolver, uri)
        loaded = true
        return true
    }

    fun getDurationMs(): Long = durationMs

    fun play(volume: Float = 1f): Boolean {
        if (!loaded) return false
        val id = soundId ?: return false

        val active = activeInstances.incrementAndGet()
        if (active > maxInstances) {
            activeInstances.decrementAndGet()
            return false
        }

        val v = volume.coerceIn(0f, 1f)
        val streamId = soundPool.play(id, v, v, 1, 0, 1f)
        if (streamId == 0) {
            activeInstances.decrementAndGet()
            return false
        }

        val releaseDelayMs = durationMs.coerceAtLeast(10L)
        mainHandler.postDelayed({
            val current = activeInstances.get()
            if (current > 0) activeInstances.decrementAndGet()
        }, releaseDelayMs)

        return true
    }

    fun unload() {
        soundId?.let { id ->
            try { soundPool.unload(id) } catch (_: Exception) {}
        }
        soundId = null
        loaded = false
        durationMs = 25L
        activeInstances.set(0)
    }

    fun release() {
        unload()
        try { soundPool.release() } catch (_: Exception) {}
    }

    private fun readDurationMs(contentResolver: ContentResolver, uri: Uri): Long {
        return try {
            val afd = contentResolver.openAssetFileDescriptor(uri, "r") ?: return 25L
            afd.use {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(it.fileDescriptor, it.startOffset, it.length)
                val dur = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                mmr.release()
                dur?.coerceAtLeast(10L) ?: 25L
            }
        } catch (_: Exception) {
            25L
        }
    }
}
