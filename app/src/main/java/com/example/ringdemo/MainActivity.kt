package com.example.ringdemo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class MainActivity : ComponentActivity() {

    // -------------------------
    // UI
    // -------------------------
    private lateinit var rootScroll: ScrollView
    private lateinit var tvStatus: TextView
    private lateinit var tvLogPath: TextView
    private lateinit var tvRot: TextView
    private lateinit var tvG: TextView
    private lateinit var tvRate: TextView
    private lateinit var tvTail: TextView

    private lateinit var btnRetry: MaterialButton
    private lateinit var btnDisconnect: MaterialButton

    private lateinit var swAutoInterp: SwitchMaterial
    private lateinit var sliderInterp: Slider
    private lateinit var tvInterp: TextView

    // New: sound
    private lateinit var btnSound: MaterialButton

    // -------------------------
    // Logging
    // -------------------------
    private lateinit var logWriter: LogWriter
    private val tailLines: ArrayDeque<String> = ArrayDeque()
    private val TAIL_MAX_LINES = 40

    // -------------------------
    // Motion smoothing
    // -------------------------
    private val smoother = RetargetingSmoother(maxInterpSec = 1.5f, useSmoothstep = true)

    // Interp settings
    private var autoInterpEnabled = true
    private var interpManualSec = 1.50f

    // Adaptive tuning
    private val interpMinSec = 0.15f
    private val interpMaxSec = 1.50f
    private val interpK = 2.0f                 // targetSec ≈ k / pktRate
    private var rateEma = 0.0                  // smoothed pkt/s
    private val rateEmaAlpha = 0.25            // 0..1 (higher = faster response)

    // -------------------------
    // Sound synthesis
    // -------------------------
    private val toneEngine = ToneEngine()
    private var soundEnabled = false

    // -------------------------
    // BLE
    // -------------------------
    private lateinit var ble: BleRingClient

    // Rate estimation
    private var pktCount = 0
    private var rateT0Ms = 0L
    private var lastHz = 0.0

    // Retry policy
    private var autoRetryEnabled = true
    private var autoRetryDelayMs = 1500L
    private var lastState: String = "Idle"
    private var retryJob: Job? = null

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                setStatus("Permissions granted. Ready.")
                startConnectFlow(userInitiated = true)
            } else {
                setStatus("Permissions denied.")
                tail("Permissions denied.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initLogger()
        initControls()
        initBle()

        startDashboardLoop()
        startLogFlushLoop()

        ensureBluetoothAndPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { retryJob?.cancel() } catch (_: Exception) {}
        try { ble.disconnect() } catch (_: Exception) {}
        try { toneEngine.stop() } catch (_: Exception) {}
        try { logWriter.close() } catch (_: Exception) {}
    }

    // -------------------------
    // Setup
    // -------------------------
    private fun bindViews() {
        rootScroll = findViewById(R.id.rootScroll)
        tvStatus = findViewById(R.id.tvStatus)
        tvLogPath = findViewById(R.id.tvLogPath)

        tvRot = findViewById(R.id.tvRot)
        tvG = findViewById(R.id.tvG)
        tvRate = findViewById(R.id.tvRate)

        tvTail = findViewById(R.id.tvTail)

        btnRetry = findViewById(R.id.btnRetry)
        btnDisconnect = findViewById(R.id.btnDisconnect)

        swAutoInterp = findViewById(R.id.swAutoInterp)
        sliderInterp = findViewById(R.id.sliderInterp)
        tvInterp = findViewById(R.id.tvInterp)

        // New sound button ID (must exist in activity_main.xml)
        btnSound = findViewById(R.id.btnSound)
    }

    private fun initLogger() {
        logWriter = LogWriter(this)
        tvLogPath.text = "Log: ${logWriter.path()}"
        logWriter.log("RingDemo start. Log file: ${logWriter.path()}")
    }

    private fun initControls() {
        // Retry button = manual connect flow (re-enables auto-retry)
        btnRetry.setOnClickListener {
            tail("Manual retry pressed.")
            autoRetryEnabled = true
            startConnectFlow(userInitiated = true)
        }

        // Disconnect button = hard stop (disable auto-retry + cancel scheduled retry)
        btnDisconnect.setOnClickListener {
            tail("Disconnect pressed: sending STOP sequence then disconnect.")
            autoRetryEnabled = false
            retryJob?.cancel()
            ble.stopLightsAndDisconnect(sendReboot = true)

            lastState = "Disconnected"
            setStatus("State: Disconnecting (stop sequence)")
        }

        // Sound toggle
        btnSound.text = "Sound: OFF"
        btnSound.setOnClickListener {
            soundEnabled = !soundEnabled
            if (soundEnabled) {
                toneEngine.start()
                btnSound.text = "Sound: ON"
                tail("Sound ON")
            } else {
                toneEngine.stop()
                btnSound.text = "Sound: OFF"
                tail("Sound OFF")
            }
        }

        // Interp controls defaults
        autoInterpEnabled = true
        swAutoInterp.isChecked = true

        interpManualSec = 1.50f
        sliderInterp.value = interpManualSec
        tvInterp.text = "Smoothing: %.2f s (auto)".format(smoother.maxInterpSec)

        swAutoInterp.setOnCheckedChangeListener { _, isChecked ->
            autoInterpEnabled = isChecked
            if (!autoInterpEnabled) {
                smoother.maxInterpSec = interpManualSec
                tvInterp.text = "Smoothing: %.2f s".format(interpManualSec)
                logWriter.log("interp mode: MANUAL sec=${smoother.maxInterpSec}")
                tail("Auto smoothing OFF")
            } else {
                tvInterp.text = "Smoothing: %.2f s (auto)".format(smoother.maxInterpSec)
                logWriter.log("interp mode: AUTO")
                tail("Auto smoothing ON")
            }
        }

        // NOTE: In auto mode, slider acts as a CAP for smoothing time (max seconds).
        sliderInterp.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            interpManualSec = value
            if (!autoInterpEnabled) {
                smoother.maxInterpSec = interpManualSec
                tvInterp.text = "Smoothing: %.2f s".format(interpManualSec)
                logWriter.log("interp set manual: ${smoother.maxInterpSec}")
            } else {
                tvInterp.text = "Smoothing cap: %.2f s (auto)".format(interpManualSec)
            }
        }
    }

    private fun initBle() {
        ble = BleRingClient(
            context = this,
            onLog = { msg ->
                logWriter.log(msg)
                tail(msg)
            },
            onBytes = { _: UUID, value: ByteArray ->
                pktCount += 1
                updateRateAndMaybeAutoInterp()

                val head = value.take(8).joinToString(" ") { "%02X".format(it) }
                logWriter.log("RX len=${value.size} hex=$head")

                val m = MotionCodec.decodeType3Motion(value) ?: return@BleRingClient
                val now = System.nanoTime() / 1e9
                smoother.ingest(
                    newRot = Vec3(m.rotX.toFloat(), m.rotY.toFloat(), m.rotZ.toFloat()),
                    newG = Vec3(m.ax, m.ay, m.az),
                    nowSec = now
                )
            },
            onState = { state ->
                lastState = state
                setStatus("State: $state")

                if (state == "Disconnected") {
                    if (autoRetryEnabled) scheduleAutoRetry()
                }
            }
        )
    }

    // -------------------------
    // Connect / retry
    // -------------------------
    private fun startConnectFlow(userInitiated: Boolean) {
        retryJob?.cancel()
        if (userInitiated) setStatus("State: Scanning")
        ble.startConnectFlow()
    }

    private fun scheduleAutoRetry() {
        retryJob?.cancel()
        retryJob = lifecycleScope.launch {
            delay(autoRetryDelayMs)
            if (autoRetryEnabled && lastState == "Disconnected") {
                tail("Auto-retry firing...")
                startConnectFlow(userInitiated = false)
            }
        }
    }

    // -------------------------
    // UI + log helpers
    // -------------------------
    private fun setStatus(msg: String) {
        runOnUiThread { tvStatus.text = msg }
        logWriter.log("STATUS: $msg")
    }

    private fun tail(line: String) {
        runOnUiThread {
            tailLines.addLast(line)
            while (tailLines.size > TAIL_MAX_LINES) tailLines.removeFirst()

            // NEW: newest-first display
            tvTail.text = tailLines.asReversed().joinToString("\n")
        }
    }


    // -------------------------
    // Rate + adaptive interp
    // -------------------------
    private fun updateRateAndMaybeAutoInterp() {
        val nowMs = System.currentTimeMillis()
        if (rateT0Ms == 0L) rateT0Ms = nowMs
        val dt = nowMs - rateT0Ms

        if (dt >= 1000L) {
            val hz = pktCount * 1000.0 / dt
            lastHz = hz
            pktCount = 0
            rateT0Ms = nowMs

            // EMA for stability
            rateEma = if (rateEma == 0.0) hz else (rateEmaAlpha * hz + (1.0 - rateEmaAlpha) * rateEma)

            if (autoInterpEnabled) {
                val target = (interpK / rateEma).toFloat()
                    .coerceIn(interpMinSec, interpMaxSec)

                val cappedTarget = target.coerceAtMost(interpManualSec)
                val blended = (0.35f * cappedTarget) + (0.65f * smoother.maxInterpSec)
                smoother.maxInterpSec = blended

                runOnUiThread {
                    tvInterp.text = "Smoothing: %.2f s (auto)".format(smoother.maxInterpSec)
                }

                logWriter.log(
                    "autoInterp rate=%.2f ema=%.2f target=%.2f cap=%.2f applied=%.2f".format(
                        hz, rateEma, target, interpManualSec, smoother.maxInterpSec
                    )
                )
            }
        }
    }

    // -------------------------
    // Dashboard loop + sound feed
    // -------------------------
    private fun startDashboardLoop() {
        lifecycleScope.launch {
            val periodMs = (1000.0 / 30.0).toLong() // 30 Hz UI refresh
            while (isActive) {
                delay(periodMs)

                val nowSec = System.nanoTime() / 1e9
                val out = smoother.sample(nowSec) ?: continue
                val (rot, g) = out

                // Feed sound from SMOOTHED rot values
                if (soundEnabled && toneEngine.isRunning()) {
                    val fx = rot.a * 4.0f
                    val fy = rot.b * 4.0f
                    val fz = rot.c * 4.0f
                    toneEngine.setFrequencies(fx, fy, fz)
                }

                runOnUiThread {
                    tvRot.text = "rot: (%.1f, %.1f, %.1f)".format(rot.a, rot.b, rot.c)
                    tvG.text = "g:   (%+.3f, %+.3f, %+.3f)".format(g.a, g.b, g.c)
                    tvRate.text = "rate: %.1f pkt/s".format(lastHz)
                }
            }
        }
    }

    private fun startLogFlushLoop() {
        lifecycleScope.launch {
            while (isActive) {
                delay(2000)
                try { logWriter.flush() } catch (_: Exception) {}
            }
        }
    }

    // -------------------------
    // Permissions + Bluetooth
    // -------------------------
    private fun ensureBluetoothAndPermissions() {
        if (!isBluetoothEnabled()) {
            setStatus("Bluetooth OFF. Turn it on.")
            return
        }

        val missing = requiredPermissions().filterNot { hasPermission(it) }
        if (missing.isEmpty()) {
            setStatus("Permissions already granted. Ready.")
            startConnectFlow(userInitiated = true)
        } else {
            setStatus("Requesting permissions…")
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bm = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter? = bm.adapter
        return adapter?.isEnabled == true
    }

    private fun hasPermission(p: String): Boolean =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

    private fun requiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
