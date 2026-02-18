//LogWriter.kt FILE START

package com.example.ringdemo

import android.content.Context
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogWriter(context: Context) {
    private val dir: File = File(context.getExternalFilesDir(null), "logs").apply { mkdirs() }
    private val tsFile = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    private val file: File = File(dir, "ring_$tsFile.log")

    private val tsLine = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private val writer: BufferedWriter = BufferedWriter(
        OutputStreamWriter(FileOutputStream(file, true), Charsets.UTF_8),
        64 * 1024
    )

    fun path(): String = file.absolutePath

    @Synchronized
    fun log(line: String) {
        val ts = tsLine.format(Date())
        writer.write("[$ts] $line\n")
    }

    @Synchronized
    fun flush() {
        writer.flush()
    }

    @Synchronized
    fun close() {
        try { writer.flush() } catch (_: Exception) {}
        try { writer.close() } catch (_: Exception) {}
    }
}


//LogWriter.kt FILE END