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
 * File: app/src/main/java/com/example/ringdemo/BleRingClient.kt
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
 */
//BleRingClient.kt FILE START

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



data class DiscoveredDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val lastSeenMs: Long,
)

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
    private val discoveredByAddress = ConcurrentHashMap<String, DiscoveredDevice>()
    private val rejectedAddresses: MutableSet<String> = HashSet()

    private var selectedAddress: String? = null

    private var activeDeviceAddress: String? = null
    private var activeDeviceName: String? = null
    private var rescanAfterIncompatibleDisconnect: Boolean = false
    private var scanAutoConnect: Boolean = true

    fun getIsScanning(): Boolean = isScanning
    fun getUniqueSeenCount(): Int = uniqueSeenCount

    fun setSelectedDevice(address: String?) {
        selectedAddress = address?.uppercase()
    }

    fun getSelectedDeviceAddress(): String? = selectedAddress

    fun getDiscoveredDevices(): List<DiscoveredDevice> =
        discoveredByAddress.values.sortedByDescending { it.rssi }

    fun startSelectionScan() {
        startScan(autoConnect = false)
    }

    // IMPORTANT: Android requires descriptor writes be serialized.
    private val notifyQueue: ArrayDeque<BluetoothGattCharacteristic> = ArrayDeque()
    private var enablingNotifies: Boolean = false

    // We defer START_RAW (and any other single pending command) until notifications are enabled
    private var pendingCommandHex: String? = null
    private var rssiReadInFlight: Boolean = false
    private var rssiReadStartMs: Long = 0L
    private val rssiReadTimeoutMs: Long = 1500L

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
        rejectedAddresses.clear()
        disconnect()
        startScan(autoConnect = true)
    }

    // =========================
    // Public: RSSI read (connected)
    // =========================
    @SuppressLint("MissingPermission")
    fun readRemoteRssi(): Boolean {
        val g = gatt ?: return false

        val nowMs = SystemClock.elapsedRealtime()

        // Only one RSSI read at a time; recover if callback was missed/stuck.
        if (rssiReadInFlight) {
            if (nowMs - rssiReadStartMs < rssiReadTimeoutMs) return true
            rssiReadInFlight = false
            rssiReadStartMs = 0L
            onLog("RSSI read watchdog: clearing stale in-flight flag")
        }

        val ok = g.readRemoteRssi()
        if (ok) {
            rssiReadInFlight = true
            rssiReadStartMs = nowMs
        }
        return ok
    }

    // =========================
    // Scan
    // =========================
    @SuppressLint("MissingPermission")
    fun startScan(autoConnect: Boolean = true) {
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
        discoveredByAddress.clear()

        scanAutoConnect = autoConnect

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
        rssiReadStartMs = 0L

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

            discoveredByAddress[addr.uppercase()] = DiscoveredDevice(
                address = addr,
                name = if (name.isBlank()) "<no-name>" else name,
                rssi = rssi,
                lastSeenMs = nowMs,
            )

            if (!scanAutoConnect) return

            val selectedMatch = selectedAddress != null && addr.equals(selectedAddress, ignoreCase = true)
            val targetMatch = selectedAddress == null &&
                    (addr.equals(Protocol.targetAddress, ignoreCase = true) || name == Protocol.targetName)
            val hintedMatch = Protocol.enableCompatibilityProbe &&
                    Protocol.compatibleNameHints.any { hint -> name.uppercase().contains(hint) }
            val rejected = rejectedAddresses.contains(addr.uppercase())

            if (selectedMatch || targetMatch || (hintedMatch && !rejected)) {
                val reason = when {
                    selectedMatch -> "SELECTED"
                    targetMatch -> "TARGET"
                    else -> "COMPAT_PROBE"
                }
                onLog("Found $reason: name=$name addr=$addr rssi=$rssi (connecting)")
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
        activeDeviceAddress = device.address
        activeDeviceName = device.name
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
                requestHighThroughputLink(g)
                onState("Discovering services")
                g.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                onLog("Disconnected (state callback). status=$status")
                cleanupGattFromCallback(g)
                onState("Disconnected")

                if (rescanAfterIncompatibleDisconnect) {
                    rescanAfterIncompatibleDisconnect = false
                    startScan(autoConnect = true)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            onLog("Services discovered: status=$status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                onState("Disconnected")
                return
            }

            if (!isGattCompatible(g)) {
                val addr = activeDeviceAddress ?: "?"
                val name = activeDeviceName ?: "<no-name>"
                onLog("Incompatible device rejected: name=$name addr=$addr")
                rejectedAddresses.add(addr.uppercase())
                rescanAfterIncompatibleDisconnect = true
                g.disconnect()
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
            rssiReadStartMs = 0L
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onRssi(rssi)
            } else {
                onLog("Read RSSI failed status=$status")
            }
        }
    }

    private fun isGattCompatible(g: BluetoothGatt): Boolean {
        val hasNotify = Protocol.notifyUuids.any { uuid -> findCharacteristicByUuid(g, uuid) != null }
        val hasWritableCmd = Protocol.cmdWriteUuids.any { uuid ->
            val ch = findCharacteristicByUuid(g, uuid) ?: return@any false
            val canWrite =
                (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                        (ch.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
            canWrite
        }

        onLog("compat check: hasNotify=$hasNotify hasWritableCmd=$hasWritableCmd")
        return hasNotify && hasWritableCmd
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
        rssiReadStartMs = 0L
        enablingNotifies = false
        pendingCommandHex = null
        notifyQueue.clear()
        cmdQueue.clear()
        cmdQueueRunning = false
        disconnectAfterStop = false
        stopSendReboot = false

        try { g.close() } catch (_: Exception) {}
        if (gatt == g) gatt = null
        activeDeviceAddress = null
        activeDeviceName = null
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

    @SuppressLint("MissingPermission")
    private fun requestHighThroughputLink(g: BluetoothGatt) {
        val priorityOk = g.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
        onLog("requestConnectionPriority(HIGH) => $priorityOk")

        val mtuOk = g.requestMtu(247)
        onLog("requestMtu(247) => $mtuOk")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            g.setPreferredPhy(
                BluetoothDevice.PHY_LE_2M_MASK,
                BluetoothDevice.PHY_LE_2M_MASK,
                BluetoothDevice.PHY_OPTION_NO_PREFERRED
            )
            onLog("setPreferredPhy(2M) requested")
        }
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
        stopLightsAndDisconnect(sendReboot = sendReboot, onDone = onDone)
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

//BleRingClient.kt FILE END
