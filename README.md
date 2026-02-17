# Colmi R02 MIDI App (RingDemo)

Android (Kotlin) app that connects to a **Colmi / RF03 BLE ring** (ex: **R02_DA00**) and streams **motion packets**.  
It provides **live rot/g values**, **adaptive interpolation smoothing**, a **tail log**, and optional **3-tone sine audio** driven by rotation.

> Repo: https://github.com/ZBZFirst/ColmiR02MiDiApp  
> Release tag: **APK** (contains the downloadable APK). :contentReference[oaicite:0]{index=0}

---

## Features

- **BLE scan → connect → subscribe → start raw motion stream**
- Decodes **Type 3 motion** packets into:
  - `rot: (x, y, z)`
  - `g: (ax, ay, az)`
- **Adaptive smoothing / interpolation**
  - Auto mode adjusts smoothing time based on packet rate (pkt/s)
  - Manual mode uses the slider value
- **Tail log** (rolling console) + full file logging to storage
- **Connect / Retry** + **Disconnect**
- **Stop-lights behavior**
  - On disconnect, can send stop sequence + **reboot (`08`)** to stop ring LEDs (the reboot frame format matters).
- Optional **Sound**
  - 3 sine tones controlled by `rot` (example mapping: `freq = rot * 4`)

---

## Install (APK)

1. Open the repo’s **Releases** and pick the **APK** release. :contentReference[oaicite:1]{index=1}  
2. Download the `.apk` to your phone.
3. Install it:
   - Android will ask to allow installs from “unknown sources” for the browser/files app you used.
4. Launch **RingDemo / Colmi R02 MIDI App**.

### Permissions you should expect
- Android 12+ (API 31+): `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`
- Older devices: `ACCESS_FINE_LOCATION` may be required for scan results.

---

## How it works (high level)

1. **Scan** for the target device by:
   - MAC address: `30:35:47:33:DA:00`
   - or name: `R02_DA00`
2. **Connect GATT**
3. **Discover services**
4. **Enable notifications** (serialized CCCD writes)
5. Send command **`A10404`** (Start Raw Sensor Data)
6. Receive notifications → decode packets → update UI/log → smooth output
7. On disconnect:
   - Send stop sequence: `A102`, `0206`, then **reboot `08`** if needed

---

## Protocol notes (important)

The ring uses a **16-byte frame** for most commands:
- Bytes `0..14` contain the command (zero padded)
- Byte `15` is a checksum (`sum(bytes[0..14]) & 0xFF`)

However, **reboot `08`** may require the special frame:
- `08 00 00 00 ... 00 08`  
(i.e., byte0 = 0x08 and byte15 = 0x08), as seen in the RF03 OTA tool UI. :contentReference[oaicite:2]{index=2}

---

## Build (Android Studio)

**Requirements**
- Android Studio (recent stable)
- Gradle + Android plugin (whatever the project is already configured with)
- A real Android device (BLE testing is painful on emulators)

**Steps**
1. `git clone` this repo
2. Open in Android Studio
3. Plug in phone (USB debugging enabled)
4. Run the app (Debug)
5. For a shareable APK:
   - *Build* → *Generate Signed Bundle / APK*
   - Create/choose a keystore
   - Generate `app-release.apk`

---

## Troubleshooting

### “Scanning…” but nothing connects
- Confirm Bluetooth is ON
- Confirm permissions are granted
- Make sure the ring is advertising (not connected to another device)

### Connected, but no motion packets
- Notifications must be enabled on the correct UUIDs
- Start raw streaming command `A10404` must be sent after notification enable completes

### Disconnect works but LEDs keep flashing
- Use the stop sequence, then send **reboot (`08`)**
- Ensure the reboot uses the **special `08 ... 08` frame**, not the normal checksum frame.

---

## Project structure (files you’ll care about)

- `MainActivity.kt`  
  UI, log tail, smoothing controls, sound toggle, connect/retry buttons
- `BleRingClient.kt`  
  Scan/connect, GATT discovery, queued notification enables, command writes, stop sequence
- `Protocol.kt`  
  UUIDs + commands + framing logic
- `MotionCodec.kt`  
  Type 3 motion decode
- `RetargetingSmoother.kt`  
  “delay-by-1 packet” retarget interpolation
- `ToneEngine.kt`  
  3-oscillator sine output controlled by rotation

---

## Safety / disclaimer

This is a hobby/research tool that talks to a consumer BLE device.  
Firmware/protocols vary by ring model and firmware version; use at your own risk.

---

## License

See `LICENSE`.
