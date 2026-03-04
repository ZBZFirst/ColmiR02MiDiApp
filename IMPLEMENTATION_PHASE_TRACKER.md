# RSSI/Pitch Refactor Implementation Tracker

This tracker captures the agreed implementation plan and progress for the audio-control refactor.

## Goal (agreed behavior)
- Gain slider controls loudness only.
- RSSI controls pitch window selection.
- Rotation (0..125) controls pitch position inside the RSSI window.
- Smoothing remains, applied to normalized rotation or final frequency.

---

## Phase checklist

- [x] **Phase 1 — Decouple loudness from RSSI**
  - [x] Replace `rssiGainScale` semantics with master `Gain` control.
  - [x] Remove RSSI->gain runtime path (`gainFromEma` / `applyRssiAudio`).
  - [x] Update UI labels from RSSI gain scaling to master gain.
  - **Completion notes:** Completed in commit for Phase 1. Gain slider now directly sets `toneEngine` master gain, RSSI no longer calls gain mapping, and UI label text was updated to `Gain`.

- [x] **Phase 2 — Implement RSSI-defined pitch window**
  - [x] Add RSSI normalization helper for `[-100..-30] -> [0..1]`.
  - [x] Define RSSI-driven pitch window (`windowMinHz/windowMaxHz`) inside base axis ranges.
  - [x] Wire latest RSSI into tone mapping pipeline.
  - **Completion notes:** Implemented a sliding pitch-window policy in `ToneMapper` (RSSI-normalized `[-100..-30] -> [0..1]`, fixed span ratio inside base axis ranges) and wired `MainActivity` dashboard loop to pass latest RSSI EMA into tone mapping.

- [x] **Phase 3 — Rotation selects pitch inside RSSI window**
  - [x] Use `rotNorm = clamp(rotAxis / 125, 0..1)`.
  - [x] Compute `freqHz = lerp(windowMinHz, windowMaxHz, rotNorm)` per enabled axis.
  - [x] Remove legacy `/255` normalization behavior.
  - **Completion notes:** Updated `ToneMapper` axis normalization from `/255` to `/125` via `rotationMax = 125f`, so pitch selection uses the agreed 0..125 rotation domain inside the RSSI-defined window.

- [ ] **Phase 4 — Smoothing placement refinement**
  - [ ] Keep existing packet smoothing and/or add dedicated frequency glide stage.
  - [ ] Apply smoothing to final `freqHz` (or normalized rotation) for stable pitch transitions.
  - [ ] Tune defaults for low packet rate (~3 pkt/sec).
  - **Completion notes:** _pending_

- [ ] **Phase 5 — Telemetry / debug visibility**
  - [ ] Surface `rssiDbm`, `rssiNorm`, pitch window, rotNorm, and final freq in diagnostics.
  - [ ] Add concise structured logs for mapping debug without high-frequency spam.
  - **Completion notes:** _pending_

- [ ] **Phase 6 — Validation / regression checks**
  - [ ] Manual matrix for RSSI tiers, rotation sweeps, and gain independence.
  - [ ] Add deterministic mapping unit tests.
  - [ ] Confirm WAV trigger path remains unaffected unless explicitly changed.
  - **Completion notes:** _pending_

---

## Progress log

- [x] **Phase 3 completed**
  - **Date:** 2026-03-04
  - **Notes:** Rotation now normalizes against 125 (not 255), so final frequency selection inside RSSI windows matches the spec.

- [x] **Phase 2 completed**
  - **Date:** 2026-03-04
  - **Notes:** RSSI now controls pitch-window placement (not volume); tone mapper consumes RSSI EMA from the dashboard loop.

- [x] **Phase 1 completed**
  - **Date:** 2026-03-04
  - **Notes:** Decoupled loudness from RSSI; RSSI path retained for visualization/triggering only.

- [x] **Tracker initialized**
  - **Date:** 2026-03-04
  - **Notes:** Created this checklist from agreed plan; ready to mark phases complete as implementation lands.
