package com.example.ringdemo

import android.content.ContentResolver
import android.net.Uri
import android.media.SoundPool

class WavTriggerPlayer {
    private val soundPool = SoundPool.Builder().setMaxStreams(1).build()
    private var soundId: Int? = null
    private var loaded = false

    fun load(contentResolver: ContentResolver, uri: Uri): Boolean {
        unload()
        val afd = contentResolver.openAssetFileDescriptor(uri, "r") ?: return false
        afd.use {
            soundId = soundPool.load(it, 1)
        }
        loaded = true
        return true
    }

    fun play(volume: Float = 1f) {
        if (!loaded) return
        val id = soundId ?: return
        val v = volume.coerceIn(0f, 1f)
        soundPool.play(id, v, v, 1, 0, 1f)
    }

    fun unload() {
        soundId?.let { id ->
            try { soundPool.unload(id) } catch (_: Exception) {}
        }
        soundId = null
        loaded = false
    }

    fun release() {
        unload()
        try { soundPool.release() } catch (_: Exception) {}
    }
}
