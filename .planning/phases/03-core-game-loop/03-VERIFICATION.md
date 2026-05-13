---
phase: 03-core-game-loop
verified: 2026-05-12T22:00:00Z
status: human_needed
score: 11/11 must-haves verified
overrides_applied: 0
human_verification:
  - test: "Enter a valid non-target word (sub-word) at Level 1+"
    expected: "Guess display flashes green for 500ms, word appears in found-words list, score updates (n*n*10), Last Word enabled"
    why_human: "Level 1 (3-letter target) rarely has distinct valid sub-words; code path is implemented and confirmed by inspection but runtime exercise at Level 1 was not possible during human verification. Will be naturally exercised at Level 4+ in Phase 4."
---

# Phase 3: Core Game Loop Verification Report

**Phase Goal:** A player can click letter buttons, build a guess, submit it, earn points, use Clear/Twist/Last Word, and race against the 120-second timer — all working correctly for a single episode
**Verified:** 2026-05-12T22:00:00Z
**Status:** human_needed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Game starts automatically on window open — letter buttons appear, timer shows 2:00 and counts down | ✓ VERIFIED | `initialize()` calls `startGame()` which sets `timeLabel.setText("2:00")`, `secondsLeft=120`, calls `buildLetterButtons()`, creates and starts `gameTimeline`. Human verification Step 1 confirmed. |
| 2 | Clicking a letter button appends a gold tile to guessDisplay and disables that button | ✓ VERIFIED | `handleLetterButton(btn)`: `btn.setDisable(true)`, `pressedButtons.add(btn)`, `Label tile` with `.letter-slot` class added to `guessDisplay`. Human verification Step 2 confirmed. |
| 3 | Clear re-enables all letter buttons and empties guessDisplay | ✓ VERIFIED | `clearBtn` handler iterates `letterButtonArea.getChildren()`, calls `setDisable(false)` on each, clears `pressedButtons` and `guessDisplay`. Human verification Step 3 confirmed. |
| 4 | Twist replaces letter buttons with newly shuffled order, clears guess, and disables Last Word | ✓ VERIFIED | `twistBtn` handler: clears `pressedButtons`, `guessDisplay`, `letterButtonArea`, calls `buildLetterButtons()`, `lastWordBtn.setDisable(true)`. Human verification Step 4 confirmed. |
| 5 | Enter on a valid word adds it to the found-words list, updates score label, and flashes green for 500ms | ✓ VERIFIED | `enterBtn` handler: `foundWords.add(guessWord)`, `scoreLabel.setText("Score: " + twistController.getScore())`, `guessDisplay.getStyleClass().add("guess-valid")`, `PauseTransition(Duration.millis(500))` removes the class after. `.guess-valid` class defined with `#1b5e20` background in CSS. Code-confirmed; human tester noted 3-letter constraint. |
| 6 | Enter on an invalid or duplicate word flashes red for 500ms and clears the guess | ✓ VERIFIED | Invalid path: `guessDisplay.getStyleClass().add("guess-invalid")`, `PauseTransition(Duration.millis(500))`, removes class then clears guess. `.guess-invalid` defined with `#b71c1c` background. Human verification Step 5 confirmed. |
| 7 | Enter on the exact target word immediately ends the episode without the flash sequence | ✓ VERIFIED | Lines 103–105: `if (guessWord.equalsIgnoreCase(twistController.getTargetWord())) { endEpisode(); return; }` — executes before any `PauseTransition`. Human verification Step 9 confirmed. |
| 8 | Last Word is disabled until first valid word; clicking it restores that guess into the display | ✓ VERIFIED | FXML starts `lastWordBtn` with `disable="true"`. `lastWordBtn.setDisable(false)` called only on valid word. Handler restores `lastWordButtons` snapshot into `pressedButtons` and `guessDisplay`. Human verification Step 7 confirmed. |
| 9 | Timer label shows mm:ss, turns amber at <= 20s, turns red at <= 10s | ✓ VERIFIED | `onTick()`: `String.format("%d:%02d", secondsLeft/60, secondsLeft%60)`, `removeAll("timer-warning","timer-critical")` then conditionally adds the correct class. CSS defines `.timer-warning` with `#f0a500` and `.timer-critical` with `#e53935`. Human verification Step 8 confirmed. |
| 10 | Timer expiry calls endEpisode() — stops Timeline and disables all controls | ✓ VERIFIED | `onTick()` line 182: `if (secondsLeft <= 0) endEpisode()`. `endEpisode()` calls `gameTimeline.stop()`, disables `twistBtn`, `enterBtn`, `lastWordBtn`, `clearBtn`, and all children of `letterButtonArea`. Human verification Step 8 confirmed. |
| 11 | Score and levelScore update correctly: n*n*10 points per valid n-letter word | ✓ VERIFIED | `TwistController.checkGuessWord()`: `int n = upper.length(); int points = n*n*10; score += points; levelScore += points;`. `scoreLabel.setText("Score: " + twistController.getScore())` in `enterBtn` handler. `getLevelScore()` returns per-episode accumulation. |

**Score:** 11/11 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `src/main/java/cs120/lab06/PrimaryController.java` | Full game loop controller wired to FXML | ✓ VERIFIED | 198 lines, all required methods present: `initialize`, `startGame`, `buildLetterButtons`, `handleLetterButton`, `onTick`, `endEpisode`. Imports `Timeline`, `KeyFrame`, `PauseTransition`, `Duration`. |
| `src/main/resources/cs120/lab06/styles.css` | `.guess-valid` and `.guess-invalid` feedback classes | ✓ VERIFIED | Section 9 contains `.guess-valid` (`#1b5e20` bg, `#43a047` border) and `.guess-invalid` (`#b71c1c` bg, `#e53935` border). `.guess-correct` is absent. All 11 section headers intact. |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `PrimaryController.initialize()` | `startGame()` | Direct call after `foundWordsList.setItems(foundWords)` | ✓ WIRED | Line 59: `startGame()` |
| `Timeline KeyFrame` | `onTick()` | Lambda `e -> onTick()` in `Duration.seconds(1)` KeyFrame | ✓ WIRED | Line 144: `new KeyFrame(Duration.seconds(1), e -> onTick())` |
| `endEpisode()` | `gameTimeline.stop()` | Direct call inside `endEpisode` | ✓ WIRED | Line 191: `gameTimeline.stop()` |
| `enterBtn handler` | `endEpisode()` | Target word match check before PauseTransition | ✓ WIRED | Line 103–105: `if (guessWord.equalsIgnoreCase(twistController.getTargetWord())) { endEpisode(); return; }` |
| `guessDisplay` | `guess-valid` / `guess-invalid` CSS | `getStyleClass().add("guess-valid"/"guess-invalid")` | ✓ WIRED | Lines 107, 117. Classes present in styles.css Section 9. |
| `timeLabel` | `timer-warning` / `timer-critical` CSS | `getStyleClass().removeAll(...).add(...)` in `onTick` | ✓ WIRED | Lines 176–180. Classes present in styles.css Section 4. |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|--------------|--------|-------------------|--------|
| `PrimaryController` (score display) | `twistController.getScore()` | `TwistController.checkGuessWord()` adds `n*n*10` to `score` | Yes — DB-equivalent via in-process model mutation | ✓ FLOWING |
| `PrimaryController` (letter buttons) | `twistController.shuffleLetters()` | `TwistController` shuffles `targetWord` chars from loaded dictionary | Yes — dictionary loaded from `twister_words/` files | ✓ FLOWING |
| `PrimaryController` (found words list) | `foundWords` ObservableList | `foundWords.add(guessWord)` on valid Enter | Yes — caller-supplied word after valid `checkGuessWord` | ✓ FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Compilation | `mvn compile -q` | EXIT:0 | ✓ PASS |
| All JUnit tests pass | `mvn test` | 10/10 (5 WordDictionaryTest + 5 TwistControllerTest) | ✓ PASS |
| No `setStyle()` calls | `grep -c 'setStyle' PrimaryController.java` | 0 | ✓ PASS |
| No `java.util.Timer` | `grep -c 'java.util.Timer' PrimaryController.java` | 0 | ✓ PASS |
| `endEpisode()` call count | `grep -c 'endEpisode' PrimaryController.java` | 3 (declaration + timer expiry + target word) | ✓ PASS |
| `.guess-valid` present in CSS | `grep -c 'guess-valid' styles.css` | 2 (selector + comment) | ✓ PASS |
| `.guess-correct` absent from CSS | `grep -c 'guess-correct' styles.css` | 0 | ✓ PASS |
| CSS section count | `grep -c 'SECTION' styles.css` | 11 | ✓ PASS |
| `TwistController` has no JavaFX imports | `grep -c 'import javafx' TwistController.java` | 0 | ✓ PASS |

### Probe Execution

No probes declared or applicable for this phase (no `scripts/*/tests/probe-*.sh` files exist).

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|------------|-------------|--------|----------|
| CTRL-01 | 03-02 | Letter buttons created dynamically at episode start | ✓ SATISFIED | `buildLetterButtons()` creates one `Button` per letter from `twistController.shuffleLetters()`; called in `startGame()` |
| CTRL-02 | 03-02 | Letter button appends letter, disables itself | ✓ SATISFIED | `handleLetterButton(btn)`: `btn.setDisable(true)`, tile added to `guessDisplay` |
| CTRL-03 | 03-02 | Clear button clears guess, re-enables all letter buttons | ✓ SATISFIED | `clearBtn` handler re-enables all `letterButtonArea` children, clears `pressedButtons` and `guessDisplay` |
| CTRL-04 | 03-02 | Twist reshuffles letters, clears guess | ✓ SATISFIED | `twistBtn` handler: clear + `buildLetterButtons()` (new shuffle) + `lastWordBtn.setDisable(true)` |
| CTRL-05 | 03-01, 03-02 | Enter awards points for valid words, shows rejection for invalid | ✓ SATISFIED | `checkGuessWord()` returns 0 or `n*n*10`; green/red flash paths implemented with `PauseTransition(500ms)` |
| CTRL-06 | 03-02 | Last Word repopulates guess from last successful guess; disabled until first success | ✓ SATISFIED | FXML `disable="true"`, enabled only on valid word, `lastWordButtons` snapshot restored on click |
| CTRL-07 | 03-02 | Letter buttons tracked by button index (object reference), not character value | ✓ SATISFIED | `pressedButtons` is `List<Button>` tracking `Button` objects. `buildLetterButtons()` creates a distinct `Button` object per letter even for duplicates. Last Word replays exact button objects from `lastWordButtons` snapshot. |
| TIMER-01 | 03-02 | Episode starts 120-second `javafx.animation.Timeline` countdown | ✓ SATISFIED | `gameTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> onTick()))`, `setCycleCount(INDEFINITE)`, `play()` in `startGame()` |
| TIMER-02 | 03-02 | Time displayed as mm:ss, decrements once per second | ✓ SATISFIED | `onTick()`: `secondsLeft--`, `String.format("%d:%02d", secondsLeft/60, secondsLeft%60)` |
| TIMER-03 | 03-02 | Timer label CSS class changes at <=20s (warning) | ✓ SATISFIED | `onTick()` adds `timer-warning` when `secondsLeft <= 20` (and not <= 10); `.timer-warning` defined in CSS Section 4 (`#f0a500`) |
| TIMER-04 | 03-02 | Timer label CSS class changes at <=10s (critical) | ✓ SATISFIED | `onTick()` adds `timer-critical` when `secondsLeft <= 10`; `.timer-critical` defined in CSS Section 4 (`#e53935`) |
| TIMER-05 | 03-02 | Timer expiry routes to single `endEpisode()` | ✓ SATISFIED | `if (secondsLeft <= 0) endEpisode()` in `onTick()` |
| PROG-01 | 03-02 | `endEpisode()` is single entry point for timer expiry and target-word-guessed | ✓ SATISFIED | Exactly 3 occurrences: method declaration (line 189), timer expiry in `onTick()` (line 183), target word match in `enterBtn` handler (line 104) |
| PROG-03 | 03-02 | Guessing the target word ends the episode immediately | ✓ SATISFIED | Phase 3 scope: immediate episode end via `endEpisode()` confirmed. Level advance (PROG-04) is Phase 4 scope. |
| PROG-07 | 03-02 | Scoring: `n * n * 10` where n = guessed word length | ✓ SATISFIED | `TwistController.checkGuessWord()`: `int n = upper.length(); int points = n*n*10;` |
| PROG-08 | 03-02 | Score (total) and levelScore (per-episode) both tracked and displayed | ⚠ PARTIAL | **Tracked:** Both `score` and `levelScore` are updated in `TwistController.checkGuessWord()` and accessible via `getScore()`/`getLevelScore()`. **Displayed:** `scoreLabel` shows cumulative score ("Score: N"). `levelScore` is NOT displayed in the UI — no label bound to `getLevelScore()`. Phase 3 SC-4 says "update correctly throughout" (not "display both"), so the Phase 3 roadmap contract is met. The full PROG-08 requirement text says "tracked and displayed" — the `levelScore` display is deferred to Phase 4 where it is needed for end-of-episode threshold evaluation. |

**PROG-08 display gap note:** The `levelScore` value is correctly tracked in the model but has no corresponding UI label in Phase 3. The Phase 3 success criterion (SC-4) says "update correctly throughout" — which is verified. The full requirement text says "tracked and displayed" — the display half will be exercised naturally in Phase 4 (PROG-02 requires levelScore >= threshold evaluation, which implies it will be shown in the end-of-episode overlay). This is not a Phase 3 blocker given SC-4 wording, but Phase 4 must complete the display.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| None | — | — | — | — |

No `TBD`, `FIXME`, `XXX`, `TODO`, `HACK`, or `PLACEHOLDER` markers found in modified files (`PrimaryController.java`, `styles.css`). No `setStyle()` calls. No `java.util.Timer` usage. No stub return patterns.

### Human Verification Required

#### 1. Enter Valid Non-Target Word (sub-word) Runtime Exercise

**Test:** Run `mvn javafx:run`. At Level 4 or higher (Phase 4), enter a valid word that is NOT the target word — e.g., if target is "PLANTS", enter "PLAN" or "ANTS". Click Enter.

**Expected:** Guess display flashes green for approximately 500ms. Word appears in the found-words list. Score label updates (e.g., 4-letter word = 4x4x10 = 160 points). Letter buttons re-enable after the flash. Last Word button becomes enabled.

**Why human:** This code path (valid non-target word) is fully implemented and passes code inspection. The green flash PauseTransition path (lines 107–115) is distinct from the target-word end-episode path. However, Level 1 (3-letter target) makes it difficult to produce a sub-word in practice — the target word is typically the only 3-letter word in its own letter set. The reviewer confirmed this during Phase 3 human verification. Full runtime exercise requires Level 4+ which becomes available in Phase 4.

---

### Gaps Summary

No blocking gaps. All 11 observable truths verified against the codebase. All 16 requirement IDs from the PLAN frontmatter are accounted for.

One partial finding: PROG-08 "levelScore displayed" — the per-episode score is tracked correctly in `TwistController` but has no UI label in Phase 3. This is consistent with Phase 3 SC-4 wording ("update correctly") and will be completed in Phase 4 when PROG-02 (threshold evaluation) necessitates showing levelScore in the end-of-episode overlay or result screen. This does not block Phase 3 acceptance.

One human verification item remains: full runtime exercise of the valid-non-target-word path. The code is verified by inspection; the runtime gap is a constraint of Level 1's word set, not an implementation deficiency.

---

_Verified: 2026-05-12T22:00:00Z_
_Verifier: Claude (gsd-verifier)_
