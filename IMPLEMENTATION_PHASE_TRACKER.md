# RSSI/Pitch Refactor Implementation Tracker

This tracker captures the agreed implementation plan and progress for the audio-control refactor.

## Goal (agreed behavior)
- Gain slider controls loudness only.
- RSSI controls pitch window selection.
- Rotation (0..125) controls pitch position inside the RSSI window.
- Smoothing remains, applied to normalized rotation or final frequency.

---

## Phase checklist

- [ ] **Phase 1 — Decouple loudness from RSSI**
  - [ ] Replace `rssiGainScale` semantics with master `Gain` control.
  - [ ] Remove RSSI->gain runtime path (`gainFromEma` / `applyRssiAudio`).
  - [ ] Update UI labels from RSSI gain scaling to master gain.
  - **Completion notes:** _pending_

- [ ] **Phase 2 — Implement RSSI-defined pitch window**
  - [ ] Add RSSI normalization helper for `[-100..-30] -> [0..1]`.
  - [ ] Define RSSI-driven pitch window (`windowMinHz/windowMaxHz`) inside base axis ranges.
  - [ ] Wire latest RSSI into tone mapping pipeline.
  - **Completion notes:** _pending_

- [ ] **Phase 3 — Rotation selects pitch inside RSSI window**
  - [ ] Use `rotNorm = clamp(rotAxis / 125, 0..1)`.
  - [ ] Compute `freqHz = lerp(windowMinHz, windowMaxHz, rotNorm)` per enabled axis.
  - [ ] Remove legacy `/255` normalization behavior.
  - **Completion notes:** _pending_

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

- [x] **Tracker initialized**
  - **Date:** 2026-03-04
  - **Notes:** Created this checklist from agreed plan; ready to mark phases complete as implementation lands.
