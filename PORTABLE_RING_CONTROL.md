# Portable Ring Control Plan

The stable boundary for future projects should be a small ring adapter that emits decoded motion samples and hides BLE command details.

## Adapter Contract

Each consumer should receive this shape:

```json
{"rotX":19,"rotY":64,"rotZ":23,"ax":0,"ay":0,"az":1}
```

The adapter owns:

- Device filters: `R02_DA00`, `R02`, `COLMI`, `QRING`.
- UART service: `6e40fff0-b5a3-f393-e0a9-e50e24dcca9e`.
- Write characteristic: `6e400002-b5a3-f393-e0a9-e50e24dcca9e`.
- Notify characteristic: `6e400003-b5a3-f393-e0a9-e50e24dcca9e`.
- Raw motion command: `A10404`.
- Stop raw motion command: `A102`.
- Type `3` packet decoding.

Consumers own:

- What ring motion controls.
- Zero/calibration poses.
- Axis mapping and inversion.
- Whether ring input is currently active.

## Reference Poses

Current seed values:

- `side1`: `(63,124,60)`
- `side2`: `(59,3,62)`
- `north`: `(19,64,23)`

Projects should expose a `Zero current pose` action and named pose shortcuts rather than hardcoding one universal hand position.

## Integration Pattern

1. Connect to the ring.
2. Enable raw motion.
3. Decode notifications into `rotX`, `rotY`, `rotZ`, `ax`, `ay`, `az`.
4. Keep the latest sample available even when project control is toggled off.
5. Apply samples only when the project-specific control toggle is on.
6. Stop raw motion before disconnect when possible.

This keeps ring support portable: the BLE and packet layer can be reused, while VentWaveforms, games, demos, or other viewers can each map the same sample stream to their own controls.
