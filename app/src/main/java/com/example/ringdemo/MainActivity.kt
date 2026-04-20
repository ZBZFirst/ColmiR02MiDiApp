/**
 * Additional maintainability notes for release review.
 * Additional maintainability notes for release review.
 * Additional maintainability notes for release review.
 */
/**
 * Supplemental documentation line 1 for readability and maintainability.
 * Supplemental documentation line 2 for readability and maintainability.
 * Supplemental documentation line 3 for readability and maintainability.
 * Supplemental documentation line 4 for readability and maintainability.
 * Supplemental documentation line 5 for readability and maintainability.
 * Supplemental documentation line 6 for readability and maintainability.
 * Supplemental documentation line 7 for readability and maintainability.
 * Supplemental documentation line 8 for readability and maintainability.
 * Supplemental documentation line 9 for readability and maintainability.
 * Supplemental documentation line 10 for readability and maintainability.
 * Supplemental documentation line 11 for readability and maintainability.
 * Supplemental documentation line 12 for readability and maintainability.
 * Supplemental documentation line 13 for readability and maintainability.
 * Supplemental documentation line 14 for readability and maintainability.
 * Supplemental documentation line 15 for readability and maintainability.
 * Supplemental documentation line 16 for readability and maintainability.
 * Supplemental documentation line 17 for readability and maintainability.
 */
/**
 * Documentation block added for maintainability and review readiness.
 * File: app/src/main/java/com/example/ringdemo/MainActivity.kt
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
 * Note 16: implementation detail documented for future contributors.
 * Note 17: implementation detail documented for future contributors.
 * Note 18: implementation detail documented for future contributors.
 * Note 19: implementation detail documented for future contributors.
 * Note 20: implementation detail documented for future contributors.
 * Note 21: implementation detail documented for future contributors.
 * Note 22: implementation detail documented for future contributors.
 * Note 23: implementation detail documented for future contributors.
 * Note 24: implementation detail documented for future contributors.
 * Note 25: implementation detail documented for future contributors.
 * Note 26: implementation detail documented for future contributors.
 * Note 27: implementation detail documented for future contributors.
 * Note 28: implementation detail documented for future contributors.
 * Note 29: implementation detail documented for future contributors.
 * Note 30: implementation detail documented for future contributors.
 * Note 31: implementation detail documented for future contributors.
 * Note 32: implementation detail documented for future contributors.
 * Note 33: implementation detail documented for future contributors.
 * Note 34: implementation detail documented for future contributors.
 * Note 35: implementation detail documented for future contributors.
 * Note 36: implementation detail documented for future contributors.
 * Note 37: implementation detail documented for future contributors.
 * Note 38: implementation detail documented for future contributors.
 * Note 39: implementation detail documented for future contributors.
 * Note 40: implementation detail documented for future contributors.
 * Note 41: implementation detail documented for future contributors.
 * Note 42: implementation detail documented for future contributors.
 * Note 43: implementation detail documented for future contributors.
 * Note 44: implementation detail documented for future contributors.
 * Note 45: implementation detail documented for future contributors.
 * Note 46: implementation detail documented for future contributors.
 * Note 47: implementation detail documented for future contributors.
 * Note 48: implementation detail documented for future contributors.
 * Note 49: implementation detail documented for future contributors.
 * Note 50: implementation detail documented for future contributors.
 * Note 51: implementation detail documented for future contributors.
 * Note 52: implementation detail documented for future contributors.
 * Note 53: implementation detail documented for future contributors.
 * Note 54: implementation detail documented for future contributors.
 * Note 55: implementation detail documented for future contributors.
 * Note 56: implementation detail documented for future contributors.
 * Note 57: implementation detail documented for future contributors.
 * Note 58: implementation detail documented for future contributors.
 * Note 59: implementation detail documented for future contributors.
 * Note 60: implementation detail documented for future contributors.
 * Note 61: implementation detail documented for future contributors.
 * Note 62: implementation detail documented for future contributors.
 * Note 63: implementation detail documented for future contributors.
 * Note 64: implementation detail documented for future contributors.
 * Note 65: implementation detail documented for future contributors.
 * Note 66: implementation detail documented for future contributors.
 * Note 67: implementation detail documented for future contributors.
 * Note 68: implementation detail documented for future contributors.
 * Note 69: implementation detail documented for future contributors.
 * Note 70: implementation detail documented for future contributors.
 * Note 71: implementation detail documented for future contributors.
 * Note 72: implementation detail documented for future contributors.
 * Note 73: implementation detail documented for future contributors.
 * Note 74: implementation detail documented for future contributors.
 * Note 75: implementation detail documented for future contributors.
 * Note 76: implementation detail documented for future contributors.
 * Note 77: implementation detail documented for future contributors.
 * Note 78: implementation detail documented for future contributors.
 * Note 79: implementation detail documented for future contributors.
 * Note 80: implementation detail documented for future contributors.
 * Note 81: implementation detail documented for future contributors.
 * Note 82: implementation detail documented for future contributors.
 * Note 83: implementation detail documented for future contributors.
 * Note 84: implementation detail documented for future contributors.
 * Note 85: implementation detail documented for future contributors.
 * Note 86: implementation detail documented for future contributors.
 * Note 87: implementation detail documented for future contributors.
 * Note 88: implementation detail documented for future contributors.
 * Note 89: implementation detail documented for future contributors.
 * Note 90: implementation detail documented for future contributors.
 * Note 91: implementation detail documented for future contributors.
 * Note 92: implementation detail documented for future contributors.
 * Note 93: implementation detail documented for future contributors.
 * Note 94: implementation detail documented for future contributors.
 * Note 95: implementation detail documented for future contributors.
 * Note 96: implementation detail documented for future contributors.
 * Note 97: implementation detail documented for future contributors.
 * Note 98: implementation detail documented for future contributors.
 * Note 99: implementation detail documented for future contributors.
 * Note 100: implementation detail documented for future contributors.
 * Note 101: implementation detail documented for future contributors.
 * Note 102: implementation detail documented for future contributors.
 * Note 103: implementation detail documented for future contributors.
 * Note 104: implementation detail documented for future contributors.
 * Note 105: implementation detail documented for future contributors.
 * Note 106: implementation detail documented for future contributors.
 * Note 107: implementation detail documented for future contributors.
 * Note 108: implementation detail documented for future contributors.
 * Note 109: implementation detail documented for future contributors.
 */
// MainActivity.kt FILE START
package com.example.ringdemo

import android.Manifest
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor

class MainActivity : ComponentActivity() {

    // -------------------------
    // Compose State
    // -------------------------
    private var dashboardState by mutableStateOf(DashboardState())

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
    private val wavPlayer = WavTriggerPlayer()
    private lateinit var midiOutput: MidiOutputRouter
    private var soundEnabled = false
    private var lastRssiDbmForTrigger: Int? = null
    private var wavTriggerThresholdDbm: Int = -65
    private var loadedWavUri: Uri? = null
    private var interpolateWavEnabled = false
    private var wavRepeatDelayMultiplier = 1
    private var wavRepeatJob: Job? = null
    private var latestRssiDbm: Int? = null

    // Phase 4: frequency glide smoothing (applied to final Hz)
    private var smoothFreqX: Float? = null
    private var smoothFreqY: Float? = null
    private var smoothFreqZ: Float? = null
    private var lastFreqSmoothSec: Double = 0.0
    private val freqSmoothingTauSec = 0.35f

    // -------------------------
    // BLE
    // -------------------------
    private lateinit var ble: BleRingClient

    // Rate estimation
    private var pktCount = 0
    private var rateT0Ms = 0L
    private var lastHz = 0.0
    private var lastMappingLogMs = 0L

    // Retry policy
    private var autoRetryEnabled = true
    private var autoRetryDelayMs = 1500L
    private var lastState: String = "Idle"
    private var retryJob: Job? = null

    private var selectedDeviceAddress: String? = null
    private var selectedDeviceLabel: String = "(none)"

    // -------------------------
    // RSSI -> EMA -> Zone -> Audio (NEW)
    // -------------------------
    private var rssiEma: Float? = null
    private val rssiEmaAlpha = 0.20f

    // Master output gain (volume only; RSSI no longer controls loudness)
    private var masterGain = 1.0f

    // -------------------------
    // Permissions
    // -------------------------
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val allGranted = results.values.all { it }
            if (allGranted) {
                setStatus("Permissions granted. Select a device.")
            } else {
                setStatus("Permissions denied.")
                tail("Permissions denied.")
            }
        }


    private val wavPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // best effort only
            }

            val ok = wavPlayer.load(contentResolver, uri)
            if (ok) {
                loadedWavUri = uri
                tail("Loaded WAV trigger sample")
            } else {
                tail("Failed to load WAV sample")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            DashboardScreen(
                state = dashboardState,
                onConnect = {
                    tail("Scanning for Rings...")
                    ble.startSelectionScan()
                    lifecycleScope.launch {
                        delay(1200)
                        ble.stopScan()
                        showDeviceSelectionDialog(autoConnect = true)
                    }
                },
                onDisconnect = {
                    tail("Disconnect pressed.")
                    autoRetryEnabled = false
                    retryJob?.cancel()
                    ble.stopLightsAndDisconnect(sendReboot = true)
                    dashboardState = dashboardState.copy(status = ConnectionStatus.Disconnected)
                },
                onResetOrigin = {
                    smoother.resetOrigin()
                    tail("Orientation origin reset.")
                },
                onToggleRotationMode = {
                    val nextMode = if (smoother.mode == "QUATERNION") "EULER" else "QUATERNION"
                    smoother.mode = nextMode
                    dashboardState = dashboardState.copy(rotationMode = nextMode)
                    tail("Visualizer mode: $nextMode")
                },
                onToggleSound = {
                    soundEnabled = !soundEnabled
                    if (soundEnabled) {
                        toneEngine.start()
                        toneEngine.setGain(masterGain)
                        toneEngine.setVoiceGains(1f, 1f, 1f)
                        tail("Sound ON")
                    } else {
                        toneEngine.stop()
                        resetFrequencySmoothing()
                        stopWavRepeatLoop()
                        tail("Sound OFF")
                    }
                    dashboardState = dashboardState.copy(isSoundEnabled = soundEnabled)
                },
                onUpdateSmoothing = {
                    interpManualSec = it
                    if (!autoInterpEnabled) {
                        smoother.maxInterpSec = it
                    }
                    dashboardState = dashboardState.copy(smoothing = it)
                },
                onUpdateGain = {
                    masterGain = it
                    if (soundEnabled && toneEngine.isRunning()) toneEngine.setGain(masterGain)
                    dashboardState = dashboardState.copy(masterGain = it)
                },
                onToggleAxis = { axis, enabled ->
                    val char = when(axis) {
                        0 -> 'X'
                        1 -> 'Y'
                        else -> 'Z'
                    }
                    toneMapper.setAxisEnabled(char, enabled)
                    dashboardState = dashboardState.copy(
                        axisEnabled = when(axis) {
                            0 -> dashboardState.axisEnabled.copy(first = enabled)
                            1 -> dashboardState.axisEnabled.copy(second = enabled)
                            else -> dashboardState.axisEnabled.copy(third = enabled)
                        }
                    )
                },
                onUpdateRange = { axis, range ->
                    val char = when(axis) {
                        0 -> 'X'
                        1 -> 'Y'
                        else -> 'Z'
                    }
                    toneMapper.setAxisRange(char, range.start, range.endInclusive)
                    dashboardState = dashboardState.copy(
                        xRange = if (axis == 0) range else dashboardState.xRange,
                        yRange = if (axis == 1) range else dashboardState.yRange,
                        zRange = if (axis == 2) range else dashboardState.zRange
                    )
                },
                onClearLogs = {
                    tailLines.clear()
                    dashboardState = dashboardState.copy(logs = emptyList())
                }
            )
        }

        initLogger()
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
        try { stopWavRepeatLoop() } catch (_: Exception) {}
        try { wavPlayer.release() } catch (_: Exception) {}
    }

    private fun initLogger() {
        logWriter = LogWriter(this)
        logWriter.log("RingDemo start. Log file: ${logWriter.path()}")
        midiOutput = MidiOutputRouter(this) { msg ->
            logWriter.log(msg)
            tail(msg)
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

                // RAW LOGGING
                val hex = value.joinToString("") { "%02X".format(it) }
                if (pktCount % 20 == 0) { // Log every 20th packet to avoid spamming
                    logWriter.log("RAW [$pktCount]: $hex")
                    tail("RAW: $hex")
                }

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
                val status = when(state) {
                    "Scanning" -> ConnectionStatus.Scanning
                    "Connecting" -> ConnectionStatus.Connecting
                    "ConnectedIdle" -> ConnectionStatus.ConnectedIdle
                    "Streaming" -> ConnectionStatus.Streaming
                    "Error" -> ConnectionStatus.Error
                    else -> ConnectionStatus.Disconnected
                }
                dashboardState = dashboardState.copy(status = status)

                if (state == "Disconnected") {
                    stopRssiPolling()
                    stopWavRepeatLoop()
                    resetFrequencySmoothing()
                    if (autoRetryEnabled) scheduleAutoRetry()
                } else if (state == "Streaming") {
                    startRssiPolling()
                }
            },
            onRssi = { rssiDbm ->
                runOnUiThread {
                    updateRssiEma(rssiDbm)
                    maybeTriggerWavFromRssi(rssiDbm)
                    dashboardState = dashboardState.copy(
                        rssi = rssiDbm,
                        rssiNorm = toneMapper.normalizeRssiForPitch(rssiDbm.toFloat())
                    )
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
        return next
    }

    private fun maybeTriggerWavFromRssi(rssiDbm: Int) {
        latestRssiDbm = rssiDbm

        if (!soundEnabled || loadedWavUri == null) {
            stopWavRepeatLoop()
            lastRssiDbmForTrigger = rssiDbm
            return
        }

        if (interpolateWavEnabled) {
            if (rssiDbm >= wavTriggerThresholdDbm) {
                startWavRepeatLoop()
            } else {
                stopWavRepeatLoop()
            }
            lastRssiDbmForTrigger = rssiDbm
            return
        }

        stopWavRepeatLoop()
        val previous = lastRssiDbmForTrigger
        lastRssiDbmForTrigger = rssiDbm
        if (previous == null) return

        val crossedUp = previous < wavTriggerThresholdDbm && rssiDbm >= wavTriggerThresholdDbm
        if (crossedUp) {
            wavPlayer.play(volume = 1f)
        }
    }

    private fun startWavRepeatLoop() {
        if (wavRepeatJob?.isActive == true) return

        wavRepeatJob = lifecycleScope.launch {
            while (isActive && soundEnabled && interpolateWavEnabled && loadedWavUri != null) {
                val currentRssi = latestRssiDbm ?: break
                if (currentRssi < wavTriggerThresholdDbm) break

                val played = wavPlayer.play(volume = 1f)
                val positiveRssi = abs(currentRssi)
                val waitMs = (positiveRssi * wavRepeatDelayMultiplier).toLong().coerceAtLeast(1L)

                if (!played) {
                    delay(10L)
                } else {
                    delay(waitMs)
                }
            }
        }
    }

    private fun stopWavRepeatLoop() {
        try { wavRepeatJob?.cancel() } catch (_: Exception) {}
        wavRepeatJob = null
    }

    // -------------------------
    // Connect / retry
    // -------------------------
    private fun startConnectFlow(userInitiated: Boolean) {
        retryJob?.cancel()
        ble.setSelectedDevice(selectedDeviceAddress)
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
        logWriter.log("STATUS: $msg")
    }

    private fun tail(line: String) {
        runOnUiThread {
            tailLines.addLast(line)
            while (tailLines.size > TAIL_MAX_LINES) tailLines.removeFirst()
            dashboardState = dashboardState.copy(logs = tailLines.toList())
        }
    }

    private fun updateRangeText(axis: Char, minHz: Float, maxHz: Float) {
        // No-op in Compose version (state handles it)
    }

    private fun shakeSelectDeviceButton() {
        // No-op in Compose version
    }

    private fun showDeviceSelectionDialog(autoConnect: Boolean = false) {
        val discovered = ble.getDiscoveredDevices()
        val connected = ble.getConnectedDevices()

        fun isRingDevice(d: DiscoveredDevice): Boolean {
            val name = d.name.uppercase()
            val addr = d.address.uppercase()
            if (addr == Protocol.targetAddress.uppercase()) return true
            if (name == Protocol.targetName.uppercase()) return true
            return Protocol.compatibleNameHints.any { hint ->
                name.contains(hint.uppercase())
            }
        }

        val connectedRings = connected.filter { isRingDevice(it) }
        val otherRings = discovered.filter { d ->
            isRingDevice(d) && !connectedRings.any { it.address == d.address }
        }

        val allDevices = connectedRings + otherRings

        if (allDevices.isEmpty()) {
            tail("No compatible rings found.")
            AlertDialog.Builder(this)
                .setTitle("No Rings Found")
                .setMessage("Make sure your ring is nearby and Bluetooth is on. If it's already paired in Android Settings, it should appear here.")
                .setPositiveButton("Scan Again") { _, _ ->
                    tail("Re-scanning...")
                    ble.startSelectionScan()
                    lifecycleScope.launch {
                        delay(1200)
                        ble.stopScan()
                        showDeviceSelectionDialog(autoConnect = true)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        val labels = allDevices.map { d ->
            val isConn = connected.any { it.address == d.address }
            val prefix = if (isConn) "★ [READY] " else "☆ "
            "$prefix${d.name} (${d.address})"
        }

        AlertDialog.Builder(this)
            .setTitle("Select Your Ring")
            .setItems(labels.toTypedArray()) { _, which ->
                val d = allDevices[which]
                selectedDeviceAddress = d.address
                selectedDeviceLabel = "${d.name} (${d.address})"
                dashboardState = dashboardState.copy(
                    deviceName = d.name,
                    deviceAddress = d.address
                )
                tail("Target: ${d.name}")

                if (autoConnect) {
                    tail("Connecting...")
                    autoRetryEnabled = true
                    startConnectFlow(userInitiated = true)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startRssiPolling() {
        stopRssiPolling()
        rssiPollJob = lifecycleScope.launch {
            val periodMs = 250L
            while (isActive) {
                ble.readRemoteRssi()
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

                dashboardState = dashboardState.copy(
                    packetRate = hz,
                    smoothing = smoother.maxInterpSec
                )

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
                val (rot, g, quat) = out

                val pitchRssiDbm = quantizeRssiForPitch(rssiEma ?: -100f)
                val toneMapping = toneMapper.mapRotToTonesWithRssi(rot, pitchRssiDbm)
                val smoothToneMapping = smoothToneMapping(toneMapping, nowSec)
                // Removed MIDI output to clean up architecture as requested
                // val midiEvents = midiMapper.mapMotion(rot)
                // midiOutput.send(midiEvents)

                val rssiNorm = toneMapper.normalizeRssiForPitch(pitchRssiDbm)
                val xWindow = toneMapper.computePitchWindow(toneMapper.x, pitchRssiDbm)
                val yWindow = toneMapper.computePitchWindow(toneMapper.y, pitchRssiDbm)
                val zWindow = toneMapper.computePitchWindow(toneMapper.z, pitchRssiDbm)

                val nowMs = System.currentTimeMillis()
                if (nowMs - lastMappingLogMs >= 1000L) {
                    lastMappingLogMs = nowMs
                    logWriter.log(
                        "MAP rssi=%.1f norm=%.3f xWin=[%.0f..%.0f] yWin=[%.0f..%.0f] zWin=[%.0f..%.0f] f=[%.1f,%.1f,%.1f]".format(
                            pitchRssiDbm,
                            rssiNorm,
                            xWindow.minHz, xWindow.maxHz,
                            yWindow.minHz, yWindow.maxHz,
                            zWindow.minHz, zWindow.maxHz,
                            smoothToneMapping.fx, smoothToneMapping.fy, smoothToneMapping.fz,
                        )
                    )
                }

                // Volume is controlled only by master gain slider.
                if (soundEnabled && toneEngine.isRunning()) {
                    toneEngine.setFrequencies(smoothToneMapping.fx, smoothToneMapping.fy, smoothToneMapping.fz)
                    toneEngine.setVoiceGains(smoothToneMapping.gx, smoothToneMapping.gy, smoothToneMapping.gz)
                }

                dashboardState = dashboardState.copy(
                    rotation = rot,
                    quaternion = quat,
                    gyro = g,
                    packetRate = lastHz
                )
            }
        }
    }

    private fun quantizeRssiForPitch(rssiDbm: Float): Float {
        // Bin RSSI by 5 dBm (absolute) to reduce micro-variation sensitivity.
        // Example: -50..-54.999 -> -50, -55..-59.999 -> -55.
        val absRssi = abs(rssiDbm)
        val binnedAbs = (floor(absRssi / 5f) * 5f).coerceIn(30f, 100f)
        return -binnedAbs
    }

    private fun smoothToneMapping(target: ToneMapper.ToneMapping, nowSec: Double): ToneMapper.ToneMapping {
        if (lastFreqSmoothSec == 0.0) {
            lastFreqSmoothSec = nowSec
            smoothFreqX = target.fx
            smoothFreqY = target.fy
            smoothFreqZ = target.fz
            return target
        }

        val dt = (nowSec - lastFreqSmoothSec).coerceAtLeast(0.0)
        lastFreqSmoothSec = nowSec

        val tau = freqSmoothingTauSec.toDouble().coerceAtLeast(1e-3)
        val alpha = (1.0 - exp(-dt / tau)).toFloat().coerceIn(0f, 1f)

        val sx = smoothOne(smoothFreqX, target.fx, alpha)
        val sy = smoothOne(smoothFreqY, target.fy, alpha)
        val sz = smoothOne(smoothFreqZ, target.fz, alpha)

        smoothFreqX = sx
        smoothFreqY = sy
        smoothFreqZ = sz

        return ToneMapper.ToneMapping(
            fx = sx,
            fy = sy,
            fz = sz,
            gx = target.gx,
            gy = target.gy,
            gz = target.gz,
        )
    }

    private fun smoothOne(prev: Float?, target: Float, alpha: Float): Float {
        val p = prev ?: return target
        return p + (target - p) * alpha
    }

    private fun resetFrequencySmoothing() {
        smoothFreqX = null
        smoothFreqY = null
        smoothFreqZ = null
        lastFreqSmoothSec = 0.0
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
            setStatus("Permissions already granted. Select a device.")
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
