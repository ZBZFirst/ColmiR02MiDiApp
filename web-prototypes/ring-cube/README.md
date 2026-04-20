# COLMI Ring Control Prototype

Standalone browser prototype for testing the motion mapping before wiring it into the VentWaveforms 3D viewer.

Open `index.html` in a browser and use the sliders to rotate the ring model. The page also includes:

- Direct Web Bluetooth controls for the known R02 target:
  - Device name: `R02_DA00`
  - Device address in the Android app: `30:35:47:33:DA:00`
  - UART service: `6e40fff0-b5a3-f393-e0a9-e50e24dcca9e`
  - Write characteristic: `6e400002-b5a3-f393-e0a9-e50e24dcca9e`
  - Notify characteristic: `6e400003-b5a3-f393-e0a9-e50e24dcca9e`
- A JavaScript port of `MotionCodec.decodeType3Motion`.
- A ring visual with one marked reference point for rotation orientation.
- A side-on neutral basis of `(19,64,23)`.
- Three named reference poses:
  - `Side 1`: `(63,124,60)`
  - `Side 2`: `(59,3,62)`
  - `North`: `(19,64,23)`
- `Use` applies a named pose as the zero reference, while `Capture` stores the live ring value into that named slot.
- A `Zero pose` button that makes the current ring pose the neutral ring orientation.
- Per-axis mapping and invert controls for tuning the feel without changing BLE decode math.
- Circular zeroing for the `0..127` rotation edge, useful for side-flat values such as `(63,124,60)` and `(59,3,62)`.
- A packet hex field for testing captured type `3` ring packets.
- A `ws://localhost:8766` bridge input that accepts decoded motion JSON:

```json
{"rotX":19,"rotY":64,"rotZ":23,"ax":0,"ay":0,"az":1}
```

## Command Notes

The page frames command hex into the ring's 16-byte command format by padding the payload through byte 14 and writing byte 15 as the low byte of the sum.

- `A10404`: enable raw sensor data.
- `A102`: disable raw sensor data.
- `0204`: enable camera motion feedback.
- `0206`: disable camera feedback.
- `03`: get charging and battery state.
- `08`: reboot ring.
- `10`: blink twice.

The Android prototype already owns the more reliable BLE connection path. If browser BLE is not available on the current browser, the next integration step is to add a small bridge there that emits decoded `MotionSample` values to this page, then reuse the same adapter shape in VentWaveforms.
