package com.example.ringdemo

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.SystemClock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BleRingClient(
    private val context: Context,
    private val onLog: (String) -> Unit,
    private val onBytes: (uuid: UUID, value: ByteArray) -> Unit,
    private val onState: (String) -> Unit = {},
    private val onRssi: (rssiDbm: Int) -> Unit = {},
) {
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner

    private var gatt: BluetoothGatt? = null

    // CCCD for notify/indicate enable
    private val CCCD_UUID: UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Scan debug / throttling
    private var isScanning: Boolean = false
    private var scanStartMs: Long = 0L
    private var uniqueSeenCount: Int = 0
    private val lastSeenMsByAddress = ConcurrentHashMap<String, Long>()

    fun getIsScanning(): Boolean = isScanning
    fun getUniqueSeenCount(): Int = uniqueSeenCount

    // IMPORTANT: Android requires descriptor writes be serialized.
    private val notifyQueue: ArrayDeque<BluetoothGattCharacteristic> = ArrayDeque()
    private var enablingNotifies: Boolean = false

    // We defer START_RAW (and any other single pending command) until notifications are enabled
    private var pendingCommandHex: String? = null
    private var rssiReadInFlight: Boolean = false

    // =========================
    // Command queue (serializes writes, write-to-all UUIDs)
    // =========================
    private val cmdQueue: ArrayDeque<ByteArray> = ArrayDeque()
    private var cmdQueueRunning: Boolean = false

    // stop-sequence flags
    private var disconnectAfterStop: Boolean = false
    private var stopSendReboot: Boolean = false

    // =========================
    // Public: connect / retry entrypoint
    // =========================
    fun startConnectFlow() {
        // Clean restart of the whole pipeline (button-friendly)
        disconnect()
        startScan()
    }

    // =========================
    // Public: RSSI read (connected)
    // =========================
    @SuppressLint("MissingPermission")
    fun readRemoteRssi(): Boolean {
        val g = gatt ?: return false

        // Only one RSSI read at a time
        if (rssiReadInFlight) return true  // treat as "already running" not an error

        val ok = g.readRemoteRssi()
        if (ok) rssiReadInFlight = true
        return ok
    }

    // =========================
    // Scan
    // =========================
    @SuppressLint("MissingPermission")
    fun startScan() {
        val s = scanner ?: run {
            onLog("No BLE scanner available")
            onState("Disconnected")
            return
        }
        if (isScanning) {
            onLog("Already scanning.")
            onState("Scanning")
            return
        }

        isScanning = true
        scanStartMs = SystemClock.elapsedRealtime()
        uniqueSeenCount = 0
        lastSeenMsByAddress.clear()

        onLog("Scanning... (logging discoveries)")
        onState("Scanning")

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        s.startScan(null, settings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        scanner?.stopScan(scanCallback)
        isScanning = false
        onLog("Scan stopped. uniqueSeen=$uniqueSeenCount")
    }

    // =========================
    // Disconnect
    // =========================
    @SuppressLint("MissingPermission")
    fun disconnect() {
        try { gatt?.disconnect() } catch (_: Exception) {}
        try { gatt?.close() } catch (_: Exception) {}

        gatt = null

        notifyQueue.clear()
        enablingNotifies = false
        pendingCommandHex = null

        cmdQueue.clear()
        cmdQueueRunning = false
        disconnectAfterStop = false
        stopSendReboot = false
        rssiReadInFlight = false

        onLog("Disconnected.")
        onState("Disconnected")
    }

    // --- Scan callback ---
    private val scanCallback = object : ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device ?: return
            val name = device.name ?: ""
            val addr = device.address ?: return
            val rssi = result.rssi

            val nowMs = SystemClock.elapsedRealtime()

            val firstTime = !lastSeenMsByAddress.containsKey(addr)
            if (firstTime) uniqueSeenCount += 1

            // Throttle: one line per device per 1500ms
            val lastMs = lastSeenMsByAddress[addr] ?: 0L
            if (nowMs - lastMs >= 1500L) {
                lastSeenMsByAddress[addr] = nowMs
                val tSec = (nowMs - scanStartMs) / 1000.0
                onLog(
                    "scan t=%.1fs rssi=%4d addr=%s name=%s".format(
                        tSec, rssi, addr, if (name.isBlank()) "<no-name>" else name
                    )
                )
            }

            // target check
            if (addr.equals(Protocol.targetAddress, ignoreCase = true) || name == Protocol.targetName) {
                onLog("Found TARGET: name=$name addr=$addr rssi=$rssi (connecting)")
                onState("Connecting")
                stopScan()
                connect(device)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (r in results) onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, r)
        }

        override fun onScanFailed(errorCode: Int) {
            onLog("Scan failed: $errorCode")
            onState("Disconnected")
        }
    }

    // =========================
    // Connect / GATT
    // =========================
    @SuppressLint("MissingPermission")
    private fun connect(device: BluetoothDevice) {
        onLog("Connecting GATT...")
        onState("Connecting")
        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            onLog("GATT state change: status=$status newState=$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                onLog("Connected. Discovering services...")
                onState("Discovering services")
                g.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onLog("Disconnected (state callback). status=$status")
                cleanupGattFromCallback(g)
                onState("Disconnected")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            onLog("Services discovered: status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                onState("Disconnected")
                return
            }

            onState("Subscribing")

            // Enable notifications/indications (SERIALIZED via queue)
            enableAllNotificationsQueued(g)

            // Defer START_RAW until queue completes
            pendingCommandHex = Protocol.START_RAW_HEX
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val uuid = characteristic.uuid
            val value = characteristic.value ?: return
            onBytes(uuid, value)
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            onLog("Char write cb: ${characteristic.uuid} status=$status")

            // advance command queue when we get a write callback
            cmdQueueRunning = false
            kickCmdQueue()
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            onLog("Desc write: ${descriptor.uuid} status=$status")
            enableNextNotifyFromQueue(g)
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            onLog("MTU changed: mtu=$mtu status=$status")
        }

        // ✅ RSSI callback
        override fun onReadRemoteRssi(g: BluetoothGatt, rssi: Int, status: Int) {
            rssiReadInFlight = false
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onRssi(rssi)
            } else {
                onLog("Read RSSI failed status=$status")
            }
        }
    }

    // =========================
    // Notifications (CCCD) - queued / serialized
    // =========================
    @SuppressLint("MissingPermission")
    private fun enableAllNotificationsQueued(g: BluetoothGatt) {
        notifyQueue.clear()
        enablingNotifies = true

        for (uuid in Protocol.notifyUuids) {
            val ch = findCharacteristicByUuid(g, uuid)
            if (ch == null) {
                onLog("Notify char not found: $uuid")
                continue
            }
            notifyQueue.addLast(ch)
        }

        onLog("Queueing ${notifyQueue.size} notification enables...")
        enableNextNotifyFromQueue(g)
    }

    @SuppressLint("MissingPermission")
    private fun cleanupGattFromCallback(g: BluetoothGatt) {
        rssiReadInFlight = false
        enablingNotifies = false
        pendingCommandHex = null
        notifyQueue.clear()
        cmdQueue.clear()
        cmdQueueRunning = false
        disconnectAfterStop = false
        stopSendReboot = false

        try { g.close() } catch (_: Exception) {}
        if (gatt == g) gatt = null
    }


    @SuppressLint("MissingPermission")
    private fun enableNextNotifyFromQueue(g: BluetoothGatt) {
        if (!enablingNotifies) return

        val ch = notifyQueue.removeFirstOrNull()
        if (ch == null) {
            enablingNotifies = false
            onLog("All notifications enabled (queue empty).")

            // Flush any deferred single command (START_RAW, etc.)
            flushPendingCommandIfReady()

            // If a stop-and-disconnect was requested during subscribe, run it now.
            if (disconnectAfterStop) {
                onLog("Deferred stop requested during subscribe; running stop sequence now.")
                stopLightsAndDisconnect(sendReboot = stopSendReboot)
            }
            return
        }

        val supportsNotify = hasNotify(ch)
        val supportsIndicate = hasIndicate(ch)

        if (!supportsNotify && !supportsIndicate) {
            onLog("Skip enable (no notify/indicate): uuid=${ch.uuid} props=${propsString(ch.properties)}")
            enableNextNotifyFromQueue(g)
            return
        }

        val useIndicate = supportsIndicate && !supportsNotify // prefer NOTIFY if available
        val ok = enableNotify(g, ch, indicate = useIndicate)

        onLog(
            "Enable step uuid=${ch.uuid} ok=$ok mode=${if (useIndicate) "INDICATE" else "NOTIFY"} props=${propsString(ch.properties)}"
        )

        // If descriptor write couldn't start, keep going
        if (!ok) enableNextNotifyFromQueue(g)
    }

    private fun flushPendingCommandIfReady() {
        val g = gatt ?: return
        val hex = pendingCommandHex ?: return
        pendingCommandHex = null

        onLog("Sending pending command AFTER notifications: $hex")
        val ok = tryWriteCommandAll(g, hex)
        onLog("tryWriteCommandAll($hex) => $ok")

        if (ok) onState("Streaming")
    }

    private fun propsString(p: Int): String {
        val parts = ArrayList<String>()
        if ((p and BluetoothGattCharacteristic.PROPERTY_READ) != 0) parts.add("READ")
        if ((p and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) parts.add("WRITE")
        if ((p and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) parts.add("WRITE_NR")
        if ((p and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) parts.add("NOTIFY")
        if ((p and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) parts.add("INDICATE")
        return parts.joinToString("|")
    }

    private fun hasNotify(ch: BluetoothGattCharacteristic): Boolean =
        (ch.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0

    private fun hasIndicate(ch: BluetoothGattCharacteristic): Boolean =
        (ch.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0

    @SuppressLint("MissingPermission")
    private fun enableNotify(gatt: BluetoothGatt, ch: BluetoothGattCharacteristic, indicate: Boolean): Boolean {
        val ok = gatt.setCharacteristicNotification(ch, true)
        if (!ok) return false

        val cccd = ch.getDescriptor(CCCD_UUID) ?: return false
        cccd.value = if (indicate)
            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        else
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

        return gatt.writeDescriptor(cccd) // async -> onDescriptorWrite
    }

    // =========================
    // Command writing (try-all like Python)
    // =========================
    @SuppressLint("MissingPermission")
    private fun tryWriteCommandAll(
        g: BluetoothGatt,
        hex: String,
        forceWithResponse: Boolean = false
    ): Boolean {
        val payload = Protocol.framedCommandFor(hex)

        for (uuid in Protocol.cmdWriteUuids) {
            val ch = findCharacteristicByUuid(g, uuid) ?: continue

            val canWrite =
                (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                        (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0

            if (!canWrite) {
                onLog("cmd uuid=$uuid not writable props=${propsString(ch.properties)}")
                continue
            }

            val canWriteNoResp =
                (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0

            ch.writeType = when {
                forceWithResponse -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                canWriteNoResp -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                else -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }

            ch.value = payload
            val ok = g.writeCharacteristic(ch)

            onLog("cmd write try uuid=$uuid ok=$ok writeType=${ch.writeType} hex=$hex")
            if (ok) return true
        }

        return false
    }

    // =========================
    // write-to-all + queued stop sequence
    // =========================
    @SuppressLint("MissingPermission")
    private fun writePayloadToAllCmdUuids(payload: ByteArray): Boolean {
        val g = gatt ?: return false
        var anyStarted = false

        for (uuid in Protocol.cmdWriteUuids) {
            val ch = findCharacteristicByUuid(g, uuid) ?: continue

            val writable =
                (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                        (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
            if (!writable) continue

            val canWriteNoResp =
                (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
            ch.writeType = if (canWriteNoResp)
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            ch.value = payload
            val ok = g.writeCharacteristic(ch)

            onLog("cmd write-to-all try uuid=$uuid ok=$ok writeType=${ch.writeType}")
            if (ok) anyStarted = true
        }

        return anyStarted
    }

    @SuppressLint("MissingPermission")
    private fun kickCmdQueue() {
        if (cmdQueueRunning) return
        val g = gatt ?: return

        if (cmdQueue.isEmpty()) {
            cmdQueueRunning = false
            if (disconnectAfterStop) {
                disconnectAfterStop = false
                onLog("Stop sequence done -> disconnecting.")
                disconnect()
            }
            return
        }

        cmdQueueRunning = true
        val payload = cmdQueue.removeFirst()

        val started = writePayloadToAllCmdUuids(payload)
        if (!started) {
            // Could be busy/disconnected; try to keep draining so we don't deadlock.
            cmdQueueRunning = false
            kickCmdQueue()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopLightsAndDisconnectReliable(sendReboot: Boolean = false) {
        val g = gatt ?: run {
            disconnect()
            return
        }

        // If notifications are still enabling, defer
        if (enablingNotifies) {
            onLog("stopReliable: deferred until notify enable completes")
            disconnectAfterStop = true
            stopSendReboot = sendReboot
            pendingCommandHex = null
            return
        }

        // Prefer the UART RX write char if present (the one that is ok=true in your log)
        val uartUuid = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
        val ch = findCharacteristicByUuid(g, uartUuid)

        if (ch == null) {
            onLog("stopReliable: UART write char not found; falling back to stopLightsAndDisconnect()")
            stopLightsAndDisconnect(sendReboot)
            return
        }

        fun writeWithResponse(hex: String): Boolean {
            val payload = Protocol.framedCommand(hex)
            ch.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT // reliable
            ch.value = payload
            val ok = g.writeCharacteristic(ch)
            onLog("stopReliable write hex=$hex ok=$ok writeType=${ch.writeType}")
            return ok
        }

        Thread {
            try {
                writeWithResponse(Protocol.STOP_RAW_HEX)
                Thread.sleep(200)
                writeWithResponse(Protocol.STOP_RAW_HEX)
                Thread.sleep(200)
                writeWithResponse(Protocol.STOP_CAMERA_HEX)
                Thread.sleep(200)

                if (sendReboot) {
                    writeWithResponse(Protocol.REBOOT_HEX)
                    Thread.sleep(600)
                }
            } catch (_: Exception) {
            }

            disconnect()
        }.start()
    }

    @SuppressLint("MissingPermission")
    fun stopLightsAndDisconnect(sendReboot: Boolean = true, onDone: (() -> Unit)? = null) {
        val g = gatt
        if (g == null) {
            disconnect()
            onDone?.invoke()
            return
        }

        Thread {
            try {
                // Stop RAW twice
                writeCommand(Protocol.STOP_RAW_HEX)
                Thread.sleep(200)
                writeCommand(Protocol.STOP_RAW_HEX)
                Thread.sleep(200)

                // Stop camera feedback
                writeCommand(Protocol.STOP_CAMERA_HEX)
                Thread.sleep(200)

                // THE HAMMER: reboot stops LEDs reliably
                if (sendReboot) {
                    writeCommand(Protocol.REBOOT_HEX) // forced WRITE_TYPE_DEFAULT inside writeCommand()
                    Thread.sleep(650)
                }
            } catch (_: Exception) {
            }

            disconnect()
            onDone?.invoke()
        }.start()
    }

    // Backwards-compatible helper name
    @SuppressLint("MissingPermission")
    fun stopThenDisconnect(sendReboot: Boolean = false, onDone: (() -> Unit)? = null) {
        stopLightsAndDisconnect(sendReboot = sendReboot)
        onDone?.invoke()
    }

    @SuppressLint("MissingPermission")
    fun writeCommand(hex: String) {
        val g = gatt ?: run {
            onLog("writeCommand: no gatt")
            return
        }
        if (enablingNotifies) {
            onLog("writeCommand deferred until notify enable completes: $hex")
            pendingCommandHex = hex
            return
        }

        val clean = hex.replace(" ", "").replace("-", "").trim().uppercase()
        val forceWithResponse = (clean == Protocol.REBOOT_HEX)

        val ok = tryWriteCommandAll(g, clean, forceWithResponse)
        onLog("writeCommand($clean) => $ok forceWithResponse=$forceWithResponse")
    }

    private fun findCharacteristicByUuid(g: BluetoothGatt, uuid: UUID): BluetoothGattCharacteristic? {
        for (svc in g.services) {
            val ch = svc.getCharacteristic(uuid)
            if (ch != null) return ch
        }
        return null
    }
}
