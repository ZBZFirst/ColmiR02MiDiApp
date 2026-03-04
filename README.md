# ColmiR02MiDiApp

Android prototype app for connecting to a Colmi ring over BLE, decoding motion packets, and mapping movement to real-time audio feedback.

## Current capabilities

- BLE scan/connect with compatibility checks against expected notify/write characteristics.
- Ring motion packet decode (`MotionCodec`) and smoothing (`RetargetingSmoother`).
- RSSI visualizer with live plotting.
- Synth monitor (`ToneEngine`) with:
  - user-controlled master gain,
  - per-axis enable toggles (X/Y/Z),
  - per-axis frequency ranges,
  - RSSI-driven pitch window + rotation-to-pitch mapping.
- Optional WAV trigger mode from RSSI threshold with repeat/interpolation controls.
- Local file logging and on-screen tail log.

## Project status

This is still a prototype and not yet a full MIDI controller product. MIDI mapping/output scaffolding exists, but end-to-end product hardening (latency budget instrumentation, presets UX, robust gesture layer) is ongoing.

## Build

```bash
./gradlew :app:assembleDebug
```

## Test

```bash
./gradlew test
```

## Release notes

A release APK may be generated from Android Studio or Gradle signing config. Keep secrets/keystores out of version control.
