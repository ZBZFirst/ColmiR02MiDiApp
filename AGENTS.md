# AGENTS.md

## Project overview
This repository is an Android app (`app` module) that connects to a specific BLE ring, subscribes to ring notifications, decodes motion packets, smooths motion values for UI/audio, and optionally synthesizes audio whose gain is driven by RSSI proximity.

Primary code package: `com.example.ringdemo`.

## End-to-end app flow
1. **Startup (`MainActivity`)**
   - Binds XML views, initializes log file output, wires UI controls, creates BLE client, starts periodic loops (dashboard + log flush), and requests Bluetooth permissions.
2. **Connect flow**
   - `BleRingClient.startConnectFlow()` performs a clean disconnect and starts BLE scan.
   - Scanner logs nearby devices and auto-connects to the target ring by `Protocol.targetAddress` or `Protocol.targetName`.
3. **GATT setup**
   - On connection: requests high-throughput link settings (connection priority, MTU, preferred PHY), discovers services, and enables notifications/indications for `Protocol.notifyUuids` using a serialized descriptor-write queue.
4. **Command bootstrapping**
   - After notification setup completes, `START_RAW_HEX` is sent to begin the data stream.
5. **Incoming data path**
   - Notification payloads are logged and routed through `MotionCodec.decodeType3Motion()`.
   - Decoded values are ingested into `RetargetingSmoother`.
   - Dashboard loop samples smoother at ~30 Hz and updates UI (`rot`, `g`, packet rate).
6. **Adaptive smoothing**
   - Packet rate is estimated once per second; if auto mode is enabled, interpolation time is adjusted based on rate EMA and clamped by slider cap.
7. **RSSI + audio path**
   - Optional RSSI poll loop calls `readRemoteRssi()` every 250 ms.
   - RSSI callback updates graph samples, computes RSSI EMA, classifies zone (`ACTIVE`/`ROAMING`), and sets `ToneEngine` master gain.
   - Audio frequencies always come from smoothed rotation; loudness comes from RSSI logic.
8. **Disconnect/retry behavior**
   - Disconnect button sends stop sequence (optionally reboot) before disconnect.
   - If disconnected unexpectedly and auto-retry is enabled, reconnect is attempted after delay.

## Core components and responsibilities
- `MainActivity.kt`
  - Owns UI state, permissions, auto-retry orchestration, log tail display, adaptive smoothing controls, RSSI visualizer toggle, and sound toggle.
- `BleRingClient.kt`
  - Encapsulates BLE scanning, GATT connection state, notification enable queue, command writes, RSSI reads, and stop/disconnect sequencing.
- `Protocol.kt`
  - Defines target identity, notify/write UUIDs, and command framing/checksum rules.
- `MotionCodec.kt`
  - Decodes Type-3 motion packets into rotation-like values and acceleration in g.
- `RetargetingSmoother.kt`
  - Handles interpolation/retargeting to reduce motion discontinuities.
- `ToneEngine.kt`
  - Real-time 3-voice sine synthesis via `AudioTrack`; frequency and gain are updated from app state.
- `RssiPlotView.kt`
  - Lightweight custom view for plotting recent RSSI samples.
- `LogWriter.kt`
  - Buffered timestamped logfile writer in app external files dir.

## Runtime outputs created by the app
1. **Persistent log files (primary output)**
   - Path format: `<external-files>/logs/ring_yyyyMMdd_HHmmss.log`.
   - Includes status transitions, scan/connect lifecycle, command writes, packet summaries, interpolation tuning events, and RSSI read failures.
2. **On-screen tail log**
   - Recent in-memory log lines shown in reverse order in `tvTail` (bounded queue).
3. **Live telemetry UI**
   - `tvRot`, `tvG`, `tvRate`, interpolation status text, and status banner.
4. **RSSI graph output**
   - In-memory time series (bounded) rendered by `RssiPlotView`.
5. **Audio output**
   - Mono synthesized audio stream from `ToneEngine`, with frequencies from motion and gain from RSSI zone/EMA.

## Repository/build artifacts you will see
- `app/release/app-release.apk` and `app/release/output-metadata.json`: generated release artifacts currently tracked.
- `app/release/baselineProfiles/.../*.dm`: generated baseline profile artifacts currently tracked.

## Practical guidance for future agents
- Keep BLE descriptor writes serialized; avoid parallel CCCD writes.
- Preserve deferred command behavior: streaming start should occur only after notifications are fully enabled.
- Respect the distinction between:
  - motion frequency mapping (`setFrequencies`) and
  - proximity loudness mapping (`setGain`).
- If adjusting RSSI thresholds, update comments and mapping expectations together.
- Log lifecycle/status changes generously; this app relies heavily on logfile diagnostics in the field.
