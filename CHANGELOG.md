# CHANGELOG

This changelog tracks roadmap execution for this project.

It uses `AGENTS.md` as the source-of-truth reference for:
- current app reality,
- product constraints (including max 5 reliable gestures),
- target architecture,
- required outputs,
- implementation priorities.

## [Unreleased]

### Added
- Added compatibility-probe onboarding flow for new Colmi rings (scan candidate -> GATT UUID check -> accept/reject and continue scanning).
- Initial `CHANGELOG.md` with milestone-driven tracking tied directly to `AGENTS.md`.
- Execution checklist for turning the roadmap into shippable work.
- Added RSSI gain scaling slider control so RSSI-to-audio gain response can be tuned live.

### Roadmap milestones (from AGENTS.md)
- [x] M1: Add MIDI output service + minimal mapper (single CC + Note). _(implemented in commit 3211202 + follow-up wiring)_
- [x] M2: Add axis-to-tone mapper controls: _(implemented in this iteration)_
  - [x] X/Y/Z axis toggles.
  - [x] Up to 3 tone lanes (mapped to X/Y/Z voices).
  - [x] Per-lane frequency min/max range controls.
- [ ] M3: Implement fixed gesture engine with max 5 gestures + debouncing.
- [ ] M4: Extract orchestration logic from `MainActivity` into pipeline components.
- [ ] M5: Add persisted mapping presets + calibration storage.
- [ ] M6: Add latency/jitter instrumentation and a visible dashboard.
- [ ] M7: Reduce high-frequency logging/allocations in hot paths.

### Output readiness checklist (from AGENTS.md)
- [x] Runtime: MIDI event stream output (basic CC stream + endpoint routing attempt).
- [x] Runtime: Tone mapping monitor output (axis toggles + per-axis range controls in UI).
- [ ] Runtime: Session performance report (packet rate, decode failures, median/p95 latency, jitter, reconnects).
- [ ] Runtime: Structured machine-readable logs.
- [ ] Runtime: Mapping snapshot export/import.
- [ ] Build/Dev: Macrobenchmark + microbenchmark results.
- [ ] Build/Dev: Protocol test vectors.
- [ ] Build/Dev: Golden mapping tests (sensor frames -> MIDI/tone outputs).
- [ ] Build/Dev: Gesture-capability matrix (including QRING/Gadgetbridge-like scenarios).

### Notes
- Keep this file updated whenever milestones are started/completed.
- When checking off a milestone, include the commit hash/PR title in the line item for traceability.
