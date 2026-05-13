---
status: partial
phase: 03-core-game-loop
source: [03-VERIFICATION.md]
started: 2026-05-12T22:00:00Z
updated: 2026-05-12T22:00:00Z
---

## Current Test

Deferred to Phase 4 — requires Level 4+ (4-letter words) to exercise reliably.

## Tests

### 1. Enter valid non-target word (sub-word) runtime exercise

expected: At Level 4+, entering a valid word that is NOT the target word causes the guess display to flash green for ~500ms, the word appears in the found-words list, score label updates (n*n*10), and the Last Word button becomes enabled. The episode does NOT end immediately.
result: [pending — deferred to Phase 4]

## Summary

total: 1
passed: 0
issues: 0
pending: 1
skipped: 0
blocked: 0

## Gaps

Note: All other Phase 3 human verification steps (startup, letter click, clear, twist, enter-invalid, last word, timer states, target-word-end) were confirmed passing during the Plan 03-02 checkpoint. This single pending item is a runtime exercise constraint of Level 1 (3-letter words rarely have sub-words distinct from the target) — the code path is confirmed correct by inspection and will be naturally exercised in Phase 4.
