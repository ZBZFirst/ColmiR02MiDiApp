// MainActivity.kt FILE START
package com.example.ringdemo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.RangeSlider
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

    private lateinit var btnSound: MaterialButton
    private lateinit var sliderRssiGain: Slider
    private lateinit var tvRssiGain: TextView

    private lateinit var swAxisX: SwitchMaterial
    private lateinit var swAxisY: SwitchMaterial
    private lateinit var swAxisZ: SwitchMaterial
    private lateinit var sliderRangeX: RangeSlider
    private lateinit var sliderRangeY: RangeSlider
    private lateinit var sliderRangeZ: RangeSlider
    private lateinit var tvRangeX: TextView
    private lateinit var tvRangeY: TextView
    private lateinit var tvRangeZ: TextView

    private lateinit var swAxisX: SwitchMaterial
    private lateinit var swAxisY: SwitchMaterial
    private lateinit var swAxisZ: SwitchMaterial
    private lateinit var sliderRangeX: RangeSlider
    private lateinit var sliderRangeY: RangeSlider
    private lateinit var sliderRangeZ: RangeSlider
    private lateinit var tvRangeX: TextView
    private lateinit var tvRangeY: TextView
    private lateinit var tvRangeZ: TextView

    private lateinit var btnRssi: Button
    private lateinit var rssiPlot: RssiPlotView

    // -------------------------
    // RSSI viz
    // -------------------------
    private var rssiVizEnabled = false
    private var rssiPollJob: Job? = null
    private val rssiSeries: ArrayDeque<Int> = ArrayDeque()
    private val RSSI_MAX_SAMPLES = 200

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
    private val interpK = 2.0f
    private var rateEma = 0.0
    private val rateEmaAlpha = 0.25

    // -------------------------
    // Sound synthesis
    // -------------------------
    private val toneEngine = ToneEngine()
    private val toneMapper = ToneMapper()
    private val midiMapper = MidiMapper()
    private lateinit var midiOutput: MidiOutputRouter
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

    // -------------------------
    // RSSI -> EMA -> Zone -> Audio (NEW)
    // -------------------------
    private enum class ProxZone { ACTIVE, ROAMING }
    private var proxZone: ProxZone = ProxZone.ROAMING

    private var rssiEma: Float? = null
    private val rssiEmaAlpha = 0.20f

    private var lastRssiMs: Long = 0L

    // NEW: gain mapping uses |RSSI| (abs of negative dBm)
    private val gainMaxAbs = 70f   // |RSSI| >= 75  => gain = 1.0
    private val gainOffAbs = 50   // |RSSI| <= 40  => gain = 0.0
    private var rssiGainScale = 1.0f

    // if RSSI stops updating, treat as ROAMING
    private val roamStaleMs  = 1500L

    // -------------------------
    // Permissions
    // -------------------------
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

        midiOutput.connectFirstAvailable()
        ensureBluetoothAndPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        try { retryJob?.cancel() } catch (_: Exception) {}
        try { ble.disconnect() } catch (_: Exception) {}
        try { toneEngine.stop() } catch (_: Exception) {}
        try { logWriter.close() } catch (_: Exception) {}
        try { stopRssiPolling() } catch (_: Exception) {}
        try { midiOutput.close() } catch (_: Exception) {}
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

        btnRssi = findViewById(R.id.rssibutton)
        rssiPlot = findViewById(R.id.rssiPlot)

        swAutoInterp = findViewById(R.id.swAutoInterp)
        sliderInterp = findViewById(R.id.sliderInterp)
        tvInterp = findViewById(R.id.tvInterp)

        btnSound = findViewById(R.id.btnSound)
        sliderRssiGain = findViewById(R.id.sliderRssiGain)
        tvRssiGain = findViewById(R.id.tvRssiGain)

        swAxisX = findViewById(R.id.swAxisX)
        swAxisY = findViewById(R.id.swAxisY)
        swAxisZ = findViewById(R.id.swAxisZ)
        sliderRangeX = findViewById(R.id.sliderRangeX)
        sliderRangeY = findViewById(R.id.sliderRangeY)
        sliderRangeZ = findViewById(R.id.sliderRangeZ)
        tvRangeX = findViewById(R.id.tvRangeX)
        tvRangeY = findViewById(R.id.tvRangeY)
        tvRangeZ = findViewById(R.id.tvRangeZ)
    }

    private fun initLogger() {
        logWriter = LogWriter(this)
        tvLogPath.text = "Log: ${logWriter.path()}"
        logWriter.log("RingDemo start. Log file: ${logWriter.path()}")
        midiOutput = MidiOutputRouter(this) { msg ->
            logWriter.log(msg)
            tail(msg)
        }
    }

    private fun initControls() {
        btnRetry.setOnClickListener {
            tail("Manual retry pressed.")
            autoRetryEnabled = true
            startConnectFlow(userInitiated = true)
        }

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
                toneEngine.setGain(0f) // start muted until RSSI logic sets it
                toneEngine.setVoiceGains(1f, 1f, 1f)
                btnSound.text = "Sound: ON"
                tail("Sound ON")
            } else {
                toneEngine.stop()
                btnSound.text = "Sound: OFF"
                tail("Sound OFF")
            }
        }

        // RSSI->gain scaling control
        sliderRssiGain.value = rssiGainScale
        tvRssiGain.text = "RSSI gain scale: %.2fx".format(rssiGainScale)
        sliderRssiGain.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            rssiGainScale = value
            tvRssiGain.text = "RSSI gain scale: %.2fx".format(rssiGainScale)
            logWriter.log("rssi gain scale set: %.2f".format(rssiGainScale))
        }

        // RSSI graph/poll toggle
        btnRssi.setOnClickListener {
            rssiVizEnabled = !rssiVizEnabled
            if (rssiVizEnabled) {
                tail("RSSI Visualizer ON")
                startRssiPolling()
            } else {
                tail("RSSI Visualizer OFF")
                stopRssiPolling()
            }
        }

        // Tone mapper controls
        swAxisX.isChecked = true
        swAxisY.isChecked = true
        swAxisZ.isChecked = true

        sliderRangeX.values = listOf(120f, 880f)
        sliderRangeY.values = listOf(120f, 880f)
        sliderRangeZ.values = listOf(120f, 880f)
        updateRangeText('X', 120f, 880f)
        updateRangeText('Y', 120f, 880f)
        updateRangeText('Z', 120f, 880f)

        swAxisX.setOnCheckedChangeListener { _, checked -> toneMapper.setAxisEnabled('X', checked) }
        swAxisY.setOnCheckedChangeListener { _, checked -> toneMapper.setAxisEnabled('Y', checked) }
        swAxisZ.setOnCheckedChangeListener { _, checked -> toneMapper.setAxisEnabled('Z', checked) }

        sliderRangeX.addOnChangeListener { _, _, fromUser ->
            if (!fromUser) return@addOnChangeListener
            val values = sliderRangeX.values
            toneMapper.setAxisRange('X', values[0], values[1])
            updateRangeText('X', values[0], values[1])
        }
        sliderRangeY.addOnChangeListener { _, _, fromUser ->
            if (!fromUser) return@addOnChangeListener
            val values = sliderRangeY.values
            toneMapper.setAxisRange('Y', values[0], values[1])
            updateRangeText('Y', values[0], values[1])
        }
        sliderRangeZ.addOnChangeListener { _, _, fromUser ->
            if (!fromUser) return@addOnChangeListener
            val values = sliderRangeZ.values
            toneMapper.setAxisRange('Z', values[0], values[1])
            updateRangeText('Z', values[0], values[1])
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
            },
            onRssi = { rssiDbm ->
                runOnUiThread {
                    // Keep your graph
                    rssiSeries.addLast(rssiDbm)
                    while (rssiSeries.size > RSSI_MAX_SAMPLES) rssiSeries.removeFirst()
                    rssiPlot.setSamples(rssiSeries.toList())

                    // NEW: EMA + zone + audio fade
                    val ema = updateRssiEma(rssiDbm)
                    val zone = updateZoneFromEma(ema)
                    applyRssiAudio(ema, zone)

                    // Optional debug (uncomment if you want it noisy)
                    // tail("RSSI raw=$rssiDbm ema=%.1f zone=$zone".format(ema))
                }
            }
        )
    }

    // -------------------------
    // RSSI logic (NEW)
    // -------------------------
    private fun updateRssiEma(rssiDbm: Int): Float {
        val x = rssiDbm.toFloat()
        val prev = rssiEma
        val next = if (prev == null) x else (rssiEmaAlpha * x + (1f - rssiEmaAlpha) * prev)
        rssiEma = next
        lastRssiMs = System.currentTimeMillis()
        return next
    }

    private fun updateZoneFromEma(ema: Float): ProxZone {
        val now = System.currentTimeMillis()
        proxZone = if (now - lastRssiMs > roamStaleMs) ProxZone.ROAMING else ProxZone.ACTIVE
        return proxZone
    }


    private fun gainFromEma(ema: Float): Float {
        // ema is negative dBm; convert to magnitude like 75 for -75 dBm
        val rssiAbs = -ema

        return when {
            rssiAbs >= gainMaxAbs -> 1f
            rssiAbs <= gainOffAbs -> 0f
            else -> {
                // Map [gainOffAbs..gainMaxAbs] -> [0..1]
                (rssiAbs - gainOffAbs) / (gainMaxAbs - gainOffAbs)
            }
        }
    }


    private fun applyRssiAudio(ema: Float, zone: ProxZone) {
        if (!soundEnabled || !toneEngine.isRunning()) return

        if (zone == ProxZone.ROAMING) {
            toneEngine.setGain(0f)
            return
        }

        val g = gainFromEma(ema)
        val scaled = (g * rssiGainScale).coerceIn(0f, 1f)
        toneEngine.setGain(scaled)  // <= 40 abs => 0, >= 75 abs => 1, then slider scaled
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
            tvTail.text = tailLines.asReversed().joinToString("\n")
        }
    }

    private fun updateRangeText(axis: Char, minHz: Float, maxHz: Float) {
        val text = "%s range: %d - %d Hz".format(axis, minHz.toInt(), maxHz.toInt())
        when (axis.uppercaseChar()) {
            'X' -> tvRangeX.text = text
            'Y' -> tvRangeY.text = text
            'Z' -> tvRangeZ.text = text
        }
    }

    private fun startRssiPolling() {
        stopRssiPolling()
        rssiPollJob = lifecycleScope.launch {
            val periodMs = 250L
            while (isActive && rssiVizEnabled) {
                val ok = ble.readRemoteRssi()
                if (!ok) tail("readRemoteRssi() -> false (not connected?)")
                delay(periodMs)
            }
        }
    }

    private fun stopRssiPolling() {
        try { rssiPollJob?.cancel() } catch (_: Exception) {}
        rssiPollJob = null
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

            rateEma = if (rateEma == 0.0) hz else (rateEmaAlpha * hz + (1.0 - rateEmaAlpha) * rateEma)

            if (autoInterpEnabled) {
                val target = (interpK / rateEma).toFloat().coerceIn(interpMinSec, interpMaxSec)
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
            val periodMs = (1000.0 / 30.0).toLong()
            while (isActive) {
                delay(periodMs)

                val nowSec = System.nanoTime() / 1e9
                val out = smoother.sample(nowSec) ?: continue
                val (rot, g) = out

                val toneMapping = toneMapper.mapRotToTones(rot)
                val midiEvents = midiMapper.mapMotion(rot)
                midiOutput.send(midiEvents)

                // Volume is controlled by RSSI via setGain().
                if (soundEnabled && toneEngine.isRunning()) {
                    toneEngine.setFrequencies(toneMapping.fx, toneMapping.fy, toneMapping.fz)
                    toneEngine.setVoiceGains(toneMapping.gx, toneMapping.gy, toneMapping.gz)
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
// MainActivity.kt FILE END
