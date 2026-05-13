---
phase: 03-core-game-loop
plan: 02
subsystem: controller
tags: [javafx, timeline, game-loop, event-handlers, css-feedback]
status: checkpoint — awaiting human-verify (Task 3)

# Dependency graph
requires:
  - phase: 03-core-game-loop
    plan: 01
    provides: ".guess-valid and .guess-invalid CSS classes in styles.css"
provides:
  - "PrimaryController full game loop: startGame(), buildLetterButtons(), onTick(), endEpisode()"
  - "Five button handlers: letter click, Clear, Twist, Enter (valid + invalid paths), Last Word"
  - "Timeline timer: 120s countdown, amber at <=20s, red at <=10s, endEpisode() at 0"
affects:
  - 03-03 (human-verify checkpoint — player runs mvn javafx:run and checks 9 behaviors)

# Tech tracking
tech-stack:
  added:
    - "javafx.animation.Timeline — 1-second KeyFrame countdown timer"
    - "javafx.animation.PauseTransition — 500ms guess feedback flash"
    - "javafx.util.Duration — Duration.seconds(1) and Duration.millis(500)"
  patterns:
    - "boolean playing guard — all button handlers return immediately when !playing"
    - "pressedButtons List<Button> — tracks current guess by object reference (not character)"
    - "lastWordButtons snapshot — copy of pressedButtons saved on successful Enter"
    - "endEpisode() single entry point — timer expiry and target-word-match both route here"
    - "CSS class toggle: getStyleClass().add/remove for transient classes; removeAll for timer classes"

key-files:
  created: []
  modified:
    - src/main/java/cs120/lab06/PrimaryController.java

key-decisions:
  - "Combined Task 1 and Task 2 into a single Write operation — both tasks modify the same file; wrote complete implementation atomically before committing; all acceptance criteria verified before commit"
  - "Button handlers wired inside startGame() so they are wired once per game lifecycle before Timeline starts"
  - "clearBtn handler uses letterButtonArea.getChildren() iteration to re-enable all letter buttons — same pattern as Twist and Enter onFinished"
  - "lastWordBtn handler has no playing guard in body — button is disabled by endEpisode() so the handler never fires when episode ended"

# Metrics
duration: 15min
completed: 2026-05-13
---

# Phase 3 Plan 02: PrimaryController Game Loop — CHECKPOINT

**Complete PrimaryController with all 8 interaction contracts wired: letter click, Clear, Twist, Enter (valid + invalid), Last Word, Timeline tick, endEpisode**

## Status: CHECKPOINT — Awaiting Human Verify (Task 3)

Tasks 1 and 2 are complete and committed. Task 3 is a `checkpoint:human-verify` gate requiring the user to run `mvn javafx:run` and validate all 9 behavioral checks.

## Performance

- **Duration:** ~15 min
- **Started:** 2026-05-13T02:20:00Z
- **Completed (Tasks 1-2):** 2026-05-13T02:33:00Z
- **Tasks complete:** 2 of 3 (Task 3 is checkpoint gate)
- **Files modified:** 1

## Task Commits

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1+2 | Add fields, startGame(), handlers, onTick(), endEpisode() | ae279e4 | src/main/java/cs120/lab06/PrimaryController.java |

Note: Tasks 1 and 2 were written as a single complete implementation in one atomic commit. The full file was written once after reading all context, then verified with `mvn compile -q` (EXIT:0) and `mvn test` (10/10 pass) before committing.

## Accomplishments

### Task 1: Instance fields and infrastructure
- Added `TwistController twistController`, `Timeline gameTimeline`, `List<Button> pressedButtons`, `List<Button> lastWordButtons`, `int secondsLeft`, `boolean playing`
- Implemented `startGame()`: initializes TwistController("twister_words"), calls `beginEpisode(3)`, sets labels, wires all button handlers, calls `buildLetterButtons()`, creates and starts Timeline
- Implemented `buildLetterButtons()`: clears FlowPane, calls `shuffleLetters()`, creates one `Button` per letter with `.letter-btn` CSS class and `handleLetterButton(btn)` action

### Task 2: All event handlers, onTick, endEpisode
- `handleLetterButton(btn)`: disabled button + pressedButtons.add + Letter tile with `.letter-slot`
- `clearBtn` handler: re-enables all FlowPane buttons, clears pressedButtons and guessDisplay
- `twistBtn` handler: clears guess and buttons, calls buildLetterButtons(), disables lastWordBtn
- `enterBtn` handler: derives guess string, calls checkGuessWord(), routes to valid (green flash, score update, lastWord snapshot, target-word check) or invalid (red flash) path; endEpisode() called immediately if target word matched
- `lastWordBtn` handler: restores lastWordButtons snapshot into pressedButtons and guessDisplay
- `onTick()`: decrements secondsLeft, formats mm:ss, removeAll timer classes then adds correct one, calls endEpisode() at <=0
- `endEpisode()`: sets playing=false, stops Timeline, disables all controls and letter buttons

## Verification Results

```
mvn compile -q  → EXIT:0
mvn test        → 10/10 tests pass (WordDictionaryTest 5/5, TwistControllerTest 5/5)
```

Acceptance criteria checklist:
- [x] `private Timeline gameTimeline` — present
- [x] `private List<Button> pressedButtons` — present
- [x] `private List<Button> lastWordButtons` — present
- [x] `private boolean playing` — present
- [x] `private int secondsLeft` — present
- [x] `startGame()` called from `initialize()` — present
- [x] `twistController = new TwistController("twister_words")` — present
- [x] `gameTimeline.setCycleCount(Timeline.INDEFINITE)` — present
- [x] `buildLetterButtons()` called from `startGame()` and Twist handler — present (3 call sites)
- [x] `btn.getStyleClass().add("letter-btn")` — present
- [x] No `setStyle()` calls — confirmed (grep count: 0)
- [x] `TwistController.java` unmodified — confirmed (git diff shows only PrimaryController.java)
- [x] `private void endEpisode()` — present
- [x] `private void onTick()` — present
- [x] `gameTimeline.stop()` inside endEpisode — present
- [x] `playing = false` inside endEpisode — present
- [x] `removeAll("timer-warning", "timer-critical")` inside onTick — present
- [x] `String.format("%d:%02d"` inside onTick — present
- [x] `Duration.millis(500)` for PauseTransition — 2 occurrences (valid and invalid paths)
- [x] `guessDisplay.getStyleClass().add("guess-valid")` — present
- [x] `guessDisplay.getStyleClass().add("guess-invalid")` — present
- [x] `guessDisplay.getStyleClass().remove("guess-valid")` — present
- [x] `guessDisplay.getStyleClass().remove("guess-invalid")` — present
- [x] `endEpisode()` appears 3 times (declaration + timer expiry + target-word match) — confirmed
- [x] No `java.util.Timer` — confirmed (Timeline only)

## Files Created/Modified

- `src/main/java/cs120/lab06/PrimaryController.java` — full game loop implementation (154 lines added to 44-line skeleton)

## Decisions Made

- Written as one atomic Write operation (Tasks 1+2 in same file); separated conceptually per plan but committed once after full verification
- Button handlers wired inside `startGame()` per the plan spec, run once before Timeline starts
- `javafx.scene.Node` imported for the `clearBtn` handler loop (avoids unchecked cast warning without importing Node)
- `pressedButtons.isEmpty()` guard on Enter returns silently with no feedback — per UI-SPEC edge case table

## Deviations from Plan

### Combined Task 1+2 Write
- **Rule applied:** None — not a deviation from plan intent, just implementation efficiency
- **What happened:** Tasks 1 and 2 both modify `PrimaryController.java`. Rather than write a partial file for Task 1 (with stub handlers) and then rewrite for Task 2, the complete implementation was written once and committed atomically after verifying all acceptance criteria for both tasks
- **Impact:** Single commit `ae279e4` covers both task sets; all acceptance criteria verified before commit

## Checkpoint State

**Task 3 (checkpoint:human-verify):** Awaiting user verification.

**To verify:** Run `mvn javafx:run` from `/Users/andrewmcinnis/Desktop/TextTwist` and check all 9 behavioral steps:
1. STARTUP: 3 letter buttons, timer "2:00" counting down, Score: 0, Level: 1
2. LETTER CLICK: button grays out, gold tile appears in guess display
3. CLEAR: all buttons re-enable, guess display empties
4. TWIST: buttons replaced in new shuffled order, guess clears
5. ENTER INVALID: red flash 500ms, guess clears, buttons re-enable
6. ENTER VALID: green flash 500ms, word in found list, score updates, Last Word enabled
7. LAST WORD: last valid guess restored in display, those buttons disabled
8. TIMER STATES: amber at 0:20, red at 0:10, all disabled at 0:00
9. TARGET WORD: guessing all letters in exact target word ends episode immediately (no flash delay)

**Resume signal:** Type "approved" if all 9 pass, or describe failures.

## Known Stubs

None — all game behaviors for a single episode are fully implemented. Phase 4 stubs (level advancement, game-over overlay, score threshold) are explicitly deferred by the plan and not present as code stubs.

## Threat Flags

No new threat surface introduced beyond what the plan's threat model covers. PrimaryController makes in-process calls to TwistController only; no new network, file, or trust-boundary crossings.

---
*Phase: 03-core-game-loop*
*Plan: 02*
*Checkpoint reached: 2026-05-13*
