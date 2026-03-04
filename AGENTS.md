# AGENTS.md

## Purpose of this document
This app currently proves BLE connectivity + motion decoding + basic sound feedback. It is **not yet a usable MIDI controller product**.

This file documents:
1. what the app does today,
2. why current design limits performance/usability,
3. how we should evolve it into a low-latency, reliable MIDI controller,
4. which outputs/artifacts we should generate going forward.

---

## Current reality (what exists today)
- BLE scan/connect to a specific ring (`Protocol.targetAddress` / `targetName`).
- Notification subscription and command bootstrapping (`START_RAW_HEX`).
- Type-3 motion decoding (`MotionCodec`).
- Interpolation + smoothing (`RetargetingSmoother`) for stable UI/audio.
- RSSI polling + EMA-based gain control.
- Local synthesized audio via `ToneEngine` (3 sine voices).
- Log file + on-screen tail logging.

### Why this is not a MIDI controller yet
- No Android MIDI API output path (no `MidiManager` / virtual MIDI device / USB/BLE MIDI transport).
- No stable gesture-to-MIDI mapping layer with user-configurable mappings.
- No timing/scheduling guarantees for musical events (jitter-aware timestamping, clock integration).
- No preset/config persistence for controller mappings.
- No performance instrumentation (latency/jitter/drop-rate KPIs).

---


## Adding a new Colmi ring (onboarding flow)
Use a two-step approach:
1. **Discovery:** scan BLE network for likely devices (name/address hints).
2. **Compatibility probe:** connect candidate, discover services, then verify that required characteristics exist:
   - at least one notify UUID from `Protocol.notifyUuids`
   - at least one writable command UUID from `Protocol.cmdWriteUuids`

If compatibility check fails, disconnect and continue scanning. If it passes, continue subscription/start-stream sequence.

This avoids hard-locking to one ring while still rejecting incompatible BLE devices quickly.

---

## Product constraints from current findings
- Expanding gesture vocabulary aggressively is likely high-risk and time-consuming.
- Ring behavior may differ depending on companion ecosystem (e.g., QRING app and Gadgetbridge-like integrations), so gesture availability may not be fully under our control.
- **Scope decision:** target a reliable set of **up to 5 gestures max** first, instead of broad gesture expansion.
- Motion/accel tone control remains a core requirement: keep direct sound control and map axes to tones with per-axis enable/disable and configurable pitch ranges.

---

## Current flow (as-built)
1. `MainActivity` starts, binds UI, initializes logger, BLE client, and loops.
2. BLE scans and connects via `BleRingClient`.
3. GATT services discovered; notify/indicate enabled serially.
4. Start command sent after notification queue completes.
5. Incoming bytes decoded to motion, smoothed, shown in UI.
6. Optional RSSI polling updates graph + tone gain.
7. Disconnect sequence sends stop commands and optional reboot.

---

## Critical bottlenecks and risks

### 1) Architecture coupling
`MainActivity` owns too much orchestration (BLE state, DSP decisions, UI updates, retry logic, audio control). This raises risk of UI thread contention and makes real-time behavior hard to reason about.

**Better direction:** move to layered pipeline:
- `BleDataSource` (I/O only)
- `MotionProcessor` (decode/filter/features)
- `GestureEngine` (limited gesture detection, max 5)
- `MidiMapper` (semantic mapping)
- `ToneMapper` (axis-to-tone control)
- `OutputRouter` (MIDI + optional audio monitor)
- `UiViewModel` (presentation state only)

### 2) Poll-based RSSI + frequent logging overhead
250 ms polling and verbose logging can create unnecessary work and timing noise.

**Better direction:**
- make polling adaptive or event-driven when possible,
- downgrade high-frequency logs to sampled/trace levels,
- keep real-time path allocation-free where feasible.

### 3) No explicit latency budget
There is no stated end-to-end target (sensor packet -> MIDI event).

**Better direction:** define budgets, e.g.
- decode + filter: <= 2 ms
- mapping + route: <= 1 ms
- total median latency target: <= 12 ms (device-dependent)
- jitter p95 target: <= 4 ms

### 4) Audio prototype can mask MIDI requirements
`ToneEngine` is useful for feedback, but product value is MIDI output interoperability.

**Better direction:** treat synth audio as a first-class monitor/test mode while still prioritizing MIDI transport and mapping UX.

---

## Target architecture (how we SHOULD build this)

### A) Data pipeline
`BLE Packet` -> `Decoder` -> `Sensor Frame Stream` -> `Feature Extractor` -> (`GestureEngine` + `MidiMapper` + `ToneMapper`) -> `Output`

- **Decoder:** parse protocol packets, validate checksums/lengths, produce typed frames.
- **Feature extractor:** derive stable controls (tilt, twist velocity, gesture onset/offset, stillness).
- **GestureEngine (scoped):** detect and stabilize **up to 5 gestures** only; avoid open-ended expansion.
- **MidiMapper:** configurable transforms to MIDI events:
  - tilt X -> CC (example CC1/mod wheel)
  - tilt Y -> CC (example CC74/brightness)
  - twist -> Pitch Bend or CC
  - gestures (max 5) -> Note/CC triggers
- **ToneMapper:**
  - map accel/rotation axes to up to 3 tones,
  - allow axis enable/disable (X/Y/Z on/off),
  - allow independent min/max frequency range per tone/axis.
- **Output:** Android MIDI + optional BLE MIDI/USB MIDI + local audio monitor.

### B) Threading model
- BLE callback thread: ingest only, no heavy compute.
- Processing coroutine/worker: decode/filter/gesture/map.
- Output worker: MIDI/audio dispatch with timestamp support.
- UI thread: render sampled state, never own business logic.

### C) Configuration model
Add a persisted mapping schema (JSON or Proto DataStore):
- MIDI channel + CC/note numbers,
- gesture map (fixed list up to 5),
- per-axis tone enable flags,
- per-axis/tone frequency ranges (min/max Hz),
- smoothing constants and dead zones,
- preset names + quick switching.

### D) Reliability model
- State machine with explicit states: `Idle -> Scanning -> Connecting -> Subscribing -> Streaming -> Degraded -> Reconnecting`.
- Backoff strategy for retries.
- Heartbeat/staleness detection to mute/hold MIDI safely.
- Capability checks for companion-app-dependent behaviors; degrade gracefully when unavailable.

---

## Outputs we should create going forward

### Runtime outputs (must-have)
1. **MIDI event stream output**
   - Primary product output: CC/Note/Pitch events routed to selected MIDI endpoint.
2. **Tone mapping control output**
   - Real-time monitor state: enabled axes, current frequencies, and active tone ranges.
3. **Session performance report**
   - Per run metrics: packet rate, decode failures, median/p95 latency, jitter, reconnect count.
4. **Structured logs**
   - Keep text logs, but also output machine-readable structured events for analysis.
5. **Mapping snapshot export**
   - Export/import controller preset and calibration values.

### Build/dev outputs (must-have)
1. **Benchmark results**
   - Macrobenchmark for UI jank and startup.
   - Microbenchmarks for decode/filter/map throughput.
2. **Protocol test vectors**
   - Sample packet corpus + expected decode outputs for regression tests.
3. **Golden mapping tests**
   - Deterministic tests from sensor frames -> expected MIDI events and tone outputs.
4. **Gesture-capability matrix**
   - Document which gestures are usable across tested ring/app combinations (including QRING and Gadgetbridge-like scenarios).

---

## Implementation priorities (recommended order)
1. Add MIDI output service + minimal mapper (single CC + Note).
2. Add axis-to-tone mapper controls:
   - X/Y/Z axis toggles,
   - up to 3 tone lanes,
   - per-lane frequency min/max range controls.
3. Implement fixed gesture engine with **max 5 gestures** and stable debouncing.
4. Extract logic from `MainActivity` into pipeline components.
5. Introduce persisted mapping presets and calibration.
6. Add latency/jitter instrumentation and dashboard.
7. Reduce runtime logging overhead in fast paths.

---

## Practical guidance for future agents
- Preserve serialized descriptor writes for CCCD setup.
- Keep command bootstrapping deferred until subscriptions are complete.
- Avoid adding high-frequency allocations/logging in callback paths.
- Any change to smoothing, gesture detection, or tone mapping should include measurable latency/jitter impact notes.
- Treat “max 5 reliable gestures” as a hard product boundary unless explicit new evidence supports expansion.
- Prioritize MIDI correctness/interoperability and controllable tone mapping over UI polish unless explicitly requested.
