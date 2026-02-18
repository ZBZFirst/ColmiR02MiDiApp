//RssiPlotView.kt FILE START

package com.example.ringdemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

class RssiPlotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private val paintGrid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f
        alpha = 70
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        alpha = 180
    }

    // reasonable BLE range clamps
    private val clampMin = -100
    private val clampMax = -30

    private var samples: List<Int> = emptyList()
    private var lastValue: Int? = null

    fun setSamples(rssiDbm: List<Int>) {
        samples = rssiDbm
        lastValue = rssiDbm.lastOrNull()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // mid grid line
        canvas.drawLine(0f, h * 0.5f, w, h * 0.5f, paintGrid)

        val data = samples
        lastValue?.let { canvas.drawText("RSSI: $it dBm", 16f, 32f, paintText) }
        if (data.size < 2) return

        val localMin = max(clampMin, data.minOrNull() ?: clampMin)
        val localMax = min(clampMax, data.maxOrNull() ?: clampMax)
        val span = max(1, localMax - localMin)

        fun x(i: Int): Float = (i.toFloat() / (data.size - 1).toFloat()) * w
        fun y(v: Int): Float {
            val t = (v - localMin).toFloat() / span.toFloat()
            return h - (t * h)
        }

        var px = x(0)
        var py = y(data[0])
        for (i in 1 until data.size) {
            val nx = x(i)
            val ny = y(data[i])
            canvas.drawLine(px, py, nx, ny, paintLine)
            px = nx
            py = ny
        }
    }
}

//RssiPlotView.kt FILE END