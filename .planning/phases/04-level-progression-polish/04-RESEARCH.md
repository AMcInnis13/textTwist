# Phase 4: Level Progression + Polish — Research

**Researched:** 2026-05-12
**Domain:** JavaFX PrimaryController extension — endEpisode routing, level advance loop, Alert dialogs, bug fixes
**Confidence:** HIGH

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**D-01:** Use `Alert.AlertType.INFORMATION` with a single OK button for all three outcomes (advance, game-over, game-won).
**D-02:** Alert content = outcome message only. Messages:
- Advance: `"Advanced to Level [N]!"` (N = new level number)
- Game over: `"Game Over — not enough points."`
- Game won: `"You Win!"`

**D-03:** After advance Alert is dismissed, the next episode starts immediately — no extra Start button. `showAndWait()` blocks; execution resumes inline in `endEpisode()`.

**D-04:** On level advance, reset in PrimaryController: clear `foundWords`, reset `secondsLeft` to 120, clear `guessDisplay` children, clear `pressedButtons`, clear `lastWordLetters`, disable `lastWordBtn`, rebuild letter buttons via `buildLetterButtons()`, update `levelLabel`, start a new `Timeline`. Cumulative score is preserved (`TwistController.beginEpisode()` does NOT reset `score`).

**D-05:** After game-over Alert dismissed, call `startGame()` — full reset: score 0, Level 1 (3-letter words), fresh timer.

**D-06:** Both game-over and game-won restart identically via `startGame()` after Alert dismissal.

**D-07 (WR-01):** Change `lastWordButtons` field from `List<Button>` to `List<String> lastWordLetters`. Store on successful Enter: `pressedButtons.stream().map(Button::getText).collect(Collectors.toList())`. On Last Word press: iterate `lastWordLetters`, for each letter find the first enabled button in `letterButtonArea` with matching text, disable it, add to `pressedButtons`, add `.letter-slot` Label to `guessDisplay`.

**D-08 (WR-02):** `startGame()` must (a) stop `gameTimeline` if non-null before creating a new one, and (b) call `foundWords.clear()` before reinitializing.

**D-09:** PrimaryController maintains `int letterCount` field (initialized to 3). On each level advance: `letterCount++`. Level number shown = `letterCount - 2`. Do NOT use `TwistController.getLevel()` for display.

### Claude's Discretion

None — all decisions are locked.

### Deferred Ideas (OUT OF SCOPE)

Keyboard input, sound effects, per-level animation, list-cell coloring, hint system. These are v2 requirements.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| PROG-02 | End-of-episode evaluation: if levelScore >= floor(0.25 * max² * 10), advance; otherwise game over | Score threshold formula confirmed from CONTEXT.md specifics; `TwistController.getLevelScore()` is the live value to compare |
| PROG-04 | Advancing a level increments letter count by 1 and loads a new target word from the larger dictionary | `TwistController.beginEpisode(letterCount)` already handles dictionary reload and target word selection |
| PROG-05 | If minimum score not met, display game-over state | Alert.AlertType.INFORMATION + `startGame()` after dismissal |
| PROG-06 | Game is won when Level 8 (10-letter words) is completed | Win condition: `letterCount == 10` at time `endEpisode()` is called |
| VFX-01 | Valid word entry triggers positive feedback on guess display | Already implemented in Phase 3 (.guess-valid CSS class + PauseTransition); no new code needed |
| VFX-02 | Invalid word entry triggers rejection feedback | Already implemented in Phase 3 (.guess-invalid CSS class + PauseTransition); no new code needed |
| VFX-03 | End-of-episode result shown (advance/game over/game won) with overlay or dialog | Alert.AlertType.INFORMATION with showAndWait() satisfies the spec requirement |
</phase_requirements>

---

## Summary

Phase 4 is a focused extension of `PrimaryController.java`. The game loop machinery (letter buttons, timer, scoring, flash feedback) is fully in place from Phase 3. What is missing is the routing logic inside `endEpisode()` that evaluates whether the player earned enough points and branches to advance/game-over/game-won, and the supporting mechanics to start the next episode cleanly.

The two bug fixes (WR-01 and WR-02) from the Phase 3 review are mandatory prerequisites. WR-01 converts `lastWordButtons` from stale Button references to a `List<String>`, which is the only approach that survives `buildLetterButtons()` being called again on level advance. WR-02 stops the old Timeline and clears `foundWords` before any re-initialization, preventing double-tick corruption and display contamination across games.

The Alert dialog pattern is the simplest possible overlay — no FXML, no new scene, no custom stage. `showAndWait()` on the FX thread blocks execution at the point of the call; when the player dismisses it, code execution resumes at the next line in `endEpisode()`. This means the routing logic after the Alert call is clean sequential code.

**Primary recommendation:** Implement everything in `PrimaryController.java` only. Extract a `beginNextEpisode(int newLetterCount)` private helper to centralize the level-advance reset logic (called from `endEpisode()` on advance, and from `startGame()` for initial setup), keeping `startGame()` as the full-reset wrapper.

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Score threshold evaluation | PrimaryController (JavaFX controller) | — | Reads `twistController.getLevelScore()` and `letterCount`; result drives UI branching |
| Alert dialog (VFX-03) | PrimaryController (JavaFX controller) | — | JavaFX Alert is a scene-graph object; must be created and shown on the FX thread |
| Level counter tracking | PrimaryController (JavaFX controller) | — | TwistController.getLevel() is stale/unused; letterCount field in PrimaryController is canonical |
| Episode reset on advance | PrimaryController (JavaFX controller) | TwistController.beginEpisode() | UI state is reset by PrimaryController; model state (new target word, levelScore clear) is reset by TwistController.beginEpisode() |
| Bug fix WR-01 (lastWordLetters) | PrimaryController (JavaFX controller) | — | Field and handler are both in PrimaryController |
| Bug fix WR-02 (startGame guards) | PrimaryController (JavaFX controller) | — | Timeline lifecycle and foundWords are owned by PrimaryController |
| Win condition check | PrimaryController (JavaFX controller) | — | letterCount == 10 check lives alongside the threshold check in endEpisode() |

---

## Standard Stack

### Core (already in pom.xml — no new dependencies)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| javafx-controls | 21 | Alert dialog, Button, Label, ListView | Already present; Alert is in javafx.controls module |
| javafx-fxml | 21 | FXML loading, @FXML injection | Already present |
| javafx.animation.Timeline | 21 (built-in) | Countdown timer | CLAUDE.md mandates Timeline over java.util.Timer |

No new Maven dependencies are needed for Phase 4. `javafx.scene.control.Alert` is part of `javafx-controls`, which is already in pom.xml. [VERIFIED: reading existing PrimaryController imports and CLAUDE.md]

### No New Libraries

Phase 4 adds zero new dependencies. All required classes are already imported or importable from the existing `javafx-controls` artifact.

---

## Architecture Patterns

### System Architecture: endEpisode() Routing

```
Timer expires (onTick) ─────────┐
                                 ▼
Target word guessed (Enter) ──► endEpisode()
                                 │
                            [stop Timeline]
                            [disable controls]
                                 │
                     ┌───────────┴───────────┐
                     │                       │
              [letterCount == 10?]           │
              AND [threshold met OR          │
               target word guessed]          │
                     │ YES         │ NO      │
                     ▼             ▼         │
               GAME WON      [threshold    │
               Alert            met?]      │
               showAndWait()     │           │
               startGame()   YES │  NO       │
                                 ▼   ▼       │
                              ADVANCE  GAME  │
                              Alert    OVER  │
                              show()   Alert │
                              Wait()   show()│
                              begin    Wait()│
                              Next()   start │
                              Episode  Game()│
```

### Level Advance Flow

```
endEpisode() confirms threshold met AND letterCount < 10
  → show Alert("Advanced to Level N!")
  → Alert.showAndWait() [blocks FX thread until OK]
  → letterCount++
  → twistController.beginEpisode(letterCount)  [reloads dictionary, picks new word, resets levelScore]
  → reset UI state (foundWords, secondsLeft, labels, buttons)
  → buildLetterButtons()
  → start new Timeline
  → playing = true
```

### Private Helper: beginNextEpisode(int newLetterCount)

Extracting shared reset code avoids duplication between `startGame()` (full reset) and level-advance path (partial reset preserving cumulative score):

```java
// [CITED: 04-CONTEXT.md D-04]
private void beginNextEpisode(int newLetterCount) {
    letterCount = newLetterCount;
    twistController.beginEpisode(letterCount);
    playing = true;
    secondsLeft = 120;
    pressedButtons.clear();
    lastWordLetters.clear();
    foundWords.clear();
    guessDisplay.getChildren().clear();
    letterButtonArea.getChildren().clear();
    lastWordBtn.setDisable(true);
    timeLabel.setText("2:00");
    timeLabel.getStyleClass().removeAll("timer-warning", "timer-critical");
    levelLabel.setText("Level: " + (letterCount - 2));
    buildLetterButtons();
    gameTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> onTick()));
    gameTimeline.setCycleCount(Timeline.INDEFINITE);
    gameTimeline.play();
}
```

`startGame()` then becomes:
```java
private void startGame() {
    if (gameTimeline != null) gameTimeline.stop();  // WR-02
    twistController = new TwistController("twister_words");
    scoreLabel.setText("Score: 0");
    beginNextEpisode(3);
}
```

### Alert Pattern (VFX-03)

```java
// [CITED: 04-CONTEXT.md specifics section]
Alert alert = new Alert(Alert.AlertType.INFORMATION);
alert.setTitle("TextTwist");
alert.setHeaderText(null);
alert.setContentText("Advanced to Level 2!");
alert.showAndWait();
// execution resumes here after player clicks OK
```

`showAndWait()` is safe to call from the FX thread — it enters a nested event loop, pumps FX events, and returns only after the dialog is closed. [ASSUMED: standard JavaFX behavior for Alert.showAndWait() modal blocking]

### WR-01 Fix: lastWordLetters (List<String>) Restoration Loop

```java
// [CITED: 03-REVIEW.md WR-01 fix section]
// In enterBtn handler (valid path), store letters not buttons:
lastWordLetters = pressedButtons.stream()
    .map(Button::getText)
    .collect(Collectors.toList());

// In lastWordBtn handler:
guessDisplay.getChildren().clear();
pressedButtons.clear();
List<Node> available = new ArrayList<>(letterButtonArea.getChildren());
for (String letter : lastWordLetters) {
    for (Node node : available) {
        Button btn = (Button) node;
        if (!btn.isDisabled() && btn.getText().equals(letter)) {
            handleLetterButton(btn);
            available.remove(btn);
            break;
        }
    }
}
```

The inner loop iterates `available` (a mutable copy, not `letterButtonArea.getChildren()` directly) so `available.remove(btn)` does not throw `ConcurrentModificationException`. [VERIFIED: reading existing PrimaryController.java structure]

### CR-01 Fix: PauseTransition playing guard

Both `onFinished` lambdas (guess-valid and guess-invalid paths) must guard before re-enabling buttons:

```java
// [CITED: 03-REVIEW.md CR-01 fix]
pause.setOnFinished(e2 -> {
    guessDisplay.getStyleClass().remove("guess-valid");
    pressedButtons.clear();
    guessDisplay.getChildren().clear();
    if (playing) {
        letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(false));
    }
});
```

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Modal result dialog | Custom stage, custom scene | `Alert.AlertType.INFORMATION` + `showAndWait()` | Single-button INFORMATION Alert is exactly the spec's "overlay or dialog" requirement; zero scene/stage code |
| Blocking until player acknowledges | Polling, boolean flags, custom event | `Alert.showAndWait()` | Built-in FX nested event loop; resumes sequential code after OK is clicked |
| Dictionary reload on level advance | Custom re-initialization code | `TwistController.beginEpisode(newLetterCount)` | Already reloads the correct word file, picks random word, resets levelScore, clears guessedWords |

**Key insight:** All complexity in this phase is routing logic, not new UI machinery. The heavy lifting (Timeline, letter buttons, CSS feedback) is already built.

---

## Common Pitfalls

### Pitfall 1: Threshold Evaluated Against Wrong Score Field

**What goes wrong:** Comparing `twistController.getScore()` (cumulative) against the threshold instead of `twistController.getLevelScore()` (per-episode). The player always passes the threshold after scoring anything in a prior level.

**Why it happens:** Both score fields exist on TwistController; using the wrong one is a one-word mistake.

**How to avoid:** Formula is explicit — `int threshold = (int)(0.25 * letterCount * letterCount * 10)`. Compare `twistController.getLevelScore() >= threshold`.

**Warning signs:** Game never shows "Game Over" even when player scores 0 in an episode.

---

### Pitfall 2: letterCount Used Before Increment for Alert Message

**What goes wrong:** Alert shows "Advanced to Level 1!" when it should show "Advanced to Level 2!" — because `letterCount` is still 3 when the message is formatted, but the display level is `letterCount - 2 = 1`.

**Why it happens:** The advance message uses the NEW level number. Level number = `letterCount - 2` AFTER incrementing.

**How to avoid:** Increment `letterCount` first (or compute new level before showing Alert):
```java
letterCount++;
int newLevel = letterCount - 2;
alert.setContentText("Advanced to Level " + newLevel + "!");
```

**Warning signs:** Alert always shows "Level 1" regardless of actual level reached.

---

### Pitfall 3: Win Condition Too Narrow

**What goes wrong:** Win condition only checks `letterCount == 10 AND target word guessed` — but PROG-02 + PROG-06 together mean win also triggers when timer expires at level 8 and the threshold IS met (player did not guess the target word but scored enough).

**Why it happens:** Conflating the two `endEpisode()` trigger paths (target-word-guessed vs. timer-expiry).

**How to avoid:** Win condition in `endEpisode()`:
```
if (letterCount == 10 AND (targetWordGuessed OR levelScore >= threshold))
    → GAME WON
```
This covers both the "target word ends episode" path and the "timer expiry with enough points" path.

**Warning signs:** Player completes Level 8 via point threshold, sees "Game Over" instead of "You Win!".

---

### Pitfall 4: Double Timeline After Level Advance

**What goes wrong:** `beginNextEpisode()` creates a new `Timeline` without stopping the old one. Two timelines fire `onTick()` simultaneously, decrementing `secondsLeft` at 2x rate.

**Why it happens:** WR-02 fix adds the null/stop guard to `startGame()`, but `beginNextEpisode()` is a new helper that also creates a Timeline.

**How to avoid:** Always stop `gameTimeline` before reassigning it — in `beginNextEpisode()`:
```java
if (gameTimeline != null) gameTimeline.stop();
gameTimeline = new Timeline(...);
```

**Warning signs:** Timer counts down twice as fast; game ends in 60 seconds instead of 120.

---

### Pitfall 5: foundWords Not Cleared on Level Advance

**What goes wrong:** Previous episode's guessed words appear in the found-words list at the start of the next episode.

**Why it happens:** WR-02 mentions clearing `foundWords` in `startGame()`, but `beginNextEpisode()` also clears the list — if the call is missed, old words persist.

**How to avoid:** `foundWords.clear()` is inside `beginNextEpisode()`, called by both `startGame()` and the level-advance path.

**Warning signs:** Found-words list grows monotonically across levels; old words from Level 1 visible during Level 2.

---

### Pitfall 6: Playing Flag Not Reset on Level Advance

**What goes wrong:** After `endEpisode()` sets `playing = false`, the level-advance path shows the Alert and calls `beginNextEpisode()` — but if `playing` is not set back to `true` inside `beginNextEpisode()`, all button guards (`if (!playing) return`) will fire and the new episode is unplayable.

**Why it happens:** `playing = true` is set in `startGame()` for the initial game; easy to omit from `beginNextEpisode()`.

**How to avoid:** `beginNextEpisode()` must set `playing = true` and also re-enable all control buttons (twistBtn, enterBtn, clearBtn) that were disabled by `endEpisode()`.

**Warning signs:** New episode starts (letter buttons visible, timer running) but clicking anything does nothing.

---

### Pitfall 7: Control Buttons Not Re-enabled After Level Advance

**What goes wrong:** `endEpisode()` calls `twistBtn.setDisable(true)`, `enterBtn.setDisable(true)`, `clearBtn.setDisable(true)`. If `beginNextEpisode()` does not explicitly re-enable them, the next episode is visible but uninteractable.

**Why it happens:** `startGame()` in Phase 3 never needed to re-enable them (it only runs at game start when buttons are already enabled). Level advance is the first time a game continues from a disabled-controls state.

**How to avoid:** `beginNextEpisode()` must include:
```java
twistBtn.setDisable(false);
enterBtn.setDisable(false);
clearBtn.setDisable(false);
```

**Warning signs:** Same as Pitfall 6 — episode appears active but buttons don't respond.

---

## Code Examples

### endEpisode() Skeleton with Full Routing

```java
// [CITED: 04-CONTEXT.md decisions D-01 through D-09 and specifics]
private void endEpisode() {
    playing = false;
    gameTimeline.stop();
    twistBtn.setDisable(true);
    enterBtn.setDisable(true);
    lastWordBtn.setDisable(true);
    clearBtn.setDisable(true);
    letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(true));

    int threshold = (int)(0.25 * letterCount * letterCount * 10);
    boolean thresholdMet = twistController.getLevelScore() >= threshold;

    if (letterCount == 10 && thresholdMet) {
        // Win condition: completed Level 8
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TextTwist");
        alert.setHeaderText(null);
        alert.setContentText("You Win!");
        alert.showAndWait();
        startGame();
    } else if (thresholdMet) {
        // Advance to next level
        letterCount++;
        int newLevel = letterCount - 2;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TextTwist");
        alert.setHeaderText(null);
        alert.setContentText("Advanced to Level " + newLevel + "!");
        alert.showAndWait();
        beginNextEpisode(letterCount);
    } else {
        // Game over
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("TextTwist");
        alert.setHeaderText(null);
        alert.setContentText("Game Over — not enough points.");
        alert.showAndWait();
        startGame();
    }
}
```

Note on win condition: the target-word-guessed path calls `endEpisode()` immediately — at that point `thresholdMet` may be false (player guessed the target word but had 0 other points). Per PROG-06 and D-05, completing Level 8 by guessing the target word also triggers game-won. The condition above correctly handles this: if the target word was guessed at Level 8, `letterCount == 10` is already true but `thresholdMet` may be false. The planner must decide whether to use a `targetWordGuessed` flag or restructure the condition. See Open Questions #1.

### startGame() After WR-02 Fix

```java
// [CITED: 03-REVIEW.md WR-02 fix + 04-CONTEXT.md D-08]
private void startGame() {
    if (gameTimeline != null) {
        gameTimeline.stop();
    }
    twistController = new TwistController("twister_words");
    scoreLabel.setText("Score: 0");
    beginNextEpisode(3);
}
```

### WR-01: lastWordLetters Store and Restore

Store (inside enterBtn valid-word path):
```java
// [CITED: 03-REVIEW.md WR-01]
lastWordLetters = pressedButtons.stream()
    .map(Button::getText)
    .collect(Collectors.toList());
lastWordBtn.setDisable(false);
```

Restore (lastWordBtn handler):
```java
guessDisplay.getChildren().clear();
pressedButtons.clear();
List<Node> available = new ArrayList<>(letterButtonArea.getChildren());
for (String letter : lastWordLetters) {
    for (Node node : available) {
        Button btn = (Button) node;
        if (!btn.isDisabled() && btn.getText().equals(letter)) {
            handleLetterButton(btn);
            available.remove(btn);
            break;
        }
    }
}
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `List<Button> lastWordButtons` — stores live Button refs | `List<String> lastWordLetters` — stores text only | Phase 4 (WR-01 fix) | Last Word survives `buildLetterButtons()` calls on level advance |
| `startGame()` unsafe to call twice | `startGame()` stops old Timeline + clears foundWords first | Phase 4 (WR-02 fix) | Game-over reset works correctly; no double-tick corruption |
| `endEpisode()` stops game, no routing | `endEpisode()` evaluates threshold, shows Alert, routes to advance/game-over/game-won | Phase 4 | All 8 levels are completable |

---

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `Alert.showAndWait()` on the FX thread safely enters a nested event loop and resumes sequential code after dialog dismissal | Architecture Patterns — Alert Pattern | If wrong, dialog causes FX thread deadlock; planner should note as a manual test point |
| A2 | `twistController.getLevelScore()` correctly reflects only the current episode's score after `beginEpisode()` resets it | Standard Stack — endEpisode routing | If wrong, threshold check is always correct or always wrong; verified by reading TwistController source: `levelScore = 0` in `beginEpisode()` [VERIFIED] |
| A3 | Win condition at Level 8 covers both the target-word-guessed path (may have 0 threshold) and the timer-expiry-with-threshold path | Common Pitfalls — Pitfall 3 | If win condition is threshold-only, target-word-guessed at Level 8 would fall through to game-over; needs explicit planner decision — see Open Questions #1 |

A2 is marked [VERIFIED] — TwistController.java line 52 confirms `levelScore = 0` in `beginEpisode()`.

---

## Open Questions (RESOLVED)

1. **Win condition at Level 8: threshold required or target-word-guessed sufficient?**
   **[RESOLVED]** — `targetWordGuessed` boolean flag added to PrimaryController; win condition is `(letterCount == 10 && (targetWordGuessed || thresholdMet))`. This covers both the target-word-guessed entry path (where threshold may not be met) and the timer-expiry path. Implemented in Task 1 (field addition) and Task 2 (endEpisode() branching).
   - What we know: PROG-06 says "Game is won when Level 8 (10-letter words) is completed." PROG-03 says "Guessing the target word ends the episode immediately and advances to next level." The combination implies guessing the target word at Level 8 = game won, regardless of threshold.
   - What's unclear: The CONTEXT.md specifics say "Win condition: `letterCount == 10` AND player either guessed target word OR met threshold at timer expiry." This is partially locked, but the `endEpisode()` code skeleton must handle both the target-word-guessed entry (where threshold may not be met) and the timer-expiry entry.
   - Recommendation: Track a `boolean targetWordGuessed` flag, set to true before calling `endEpisode()` from the Enter handler. Win condition = `letterCount == 10 && (targetWordGuessed || thresholdMet)`. This matches the CONTEXT.md specifics verbatim.

2. **CR-02 (PauseTransition / Twist race): fix or defer?**
   **[DEFERRED]** — The playing guard (CR-01) is implemented in Task 1 (both PauseTransition onFinished blocks guard `if (playing)` before re-enabling buttons). The additional `activePause` cancel-on-twist fix (CR-02) is deferred to v2: the Twist-during-flash race is a rare edge-case interaction in a CS120 lab context, and CONTEXT.md does not include this as a locked decision for Phase 4. Full activePause cancel-on-twist is out of scope.
   - What we know: The Phase 3 review flagged that clicking Twist during a 500ms flash can corrupt state. The fix requires an `activePause` field and cancel-on-twist logic.
   - What's unclear: CONTEXT.md does not explicitly require this fix in Phase 4. It is not a WR or CR listed in the locked decisions.
   - Recommendation: Fix it in Phase 4 alongside CR-01 (which IS required) since both are in the same PauseTransition block. The `activePause` field pattern is low-risk and prevents a reproducible bug.

3. **IN-02 (handler wiring inside startGame): refactor or leave?**
   **[RESOLVED]** — Handler wiring moved to `initialize()` in Task 1 step 5. All four `setOnAction` blocks (clearBtn, twistBtn, enterBtn, lastWordBtn) are registered once in `initialize()`, not re-registered on every `startGame()` call.
   - What we know: Phase 3 review flagged moving `setOnAction` calls to `initialize()`. CONTEXT.md does not lock this refactor.
   - What's unclear: Whether this refactor is in Phase 4 scope.
   - Recommendation: Apply it as part of the Phase 4 cleanup pass, since `startGame()` is being restructured anyway. Zero behavioral risk.

---

## Environment Availability

Step 2.6: SKIPPED — Phase 4 is a pure code extension. No new external tools, services, runtimes, or CLI utilities beyond the existing Maven/JavaFX project. Dictionary files at `twister_words/` are already verified present from Phase 1.

---

## Sources

### Primary (HIGH confidence)
- `src/main/java/cs120/lab06/PrimaryController.java` [VERIFIED] — current state of all fields, handlers, endEpisode(), startGame()
- `src/main/java/cs120/lab06/TwistController.java` [VERIFIED] — beginEpisode() resets levelScore and guessedWords, preserves score; getLevelScore() returns per-episode value
- `.planning/phases/04-level-progression-polish/04-CONTEXT.md` [VERIFIED] — all locked decisions D-01 through D-09
- `.planning/phases/03-core-game-loop/03-REVIEW.md` [VERIFIED] — CR-01, WR-01, WR-02 with exact fix code
- `src/main/resources/cs120/lab06/styles.css` [VERIFIED] — .guess-valid and .guess-invalid already defined; no CSS changes needed
- `CLAUDE.md` [VERIFIED] — critical rules: Timeline, no setStyle(), single endEpisode(), track by index

### Secondary (MEDIUM confidence)
- None required — all research was from in-project sources

### Tertiary (LOW confidence — see Assumptions Log)
- A1: Alert.showAndWait() nested event loop behavior [ASSUMED based on training knowledge of JavaFX modal blocking]

---

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new dependencies; Alert is in existing javafx-controls artifact
- Architecture: HIGH — all patterns derived from verified existing source code and locked CONTEXT.md decisions
- Pitfalls: HIGH — WR-01, WR-02, CR-01 verified from Phase 3 review; Pitfalls 2-7 derived from reading the actual code

**Research date:** 2026-05-12
**Valid until:** Phase 4 execution (project-specific research; not time-limited by ecosystem changes)
