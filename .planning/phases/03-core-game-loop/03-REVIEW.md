---
phase: 03-core-game-loop
reviewed: 2026-05-13T02:53:24Z
depth: standard
files_reviewed: 2
files_reviewed_list:
  - src/main/java/cs120/lab06/PrimaryController.java
  - src/main/resources/cs120/lab06/styles.css
findings:
  critical: 3
  warning: 2
  info: 2
  total: 7
status: issues_found
---

# Phase 03: Code Review Report

**Reviewed:** 2026-05-13T02:53:24Z
**Depth:** standard
**Files Reviewed:** 2
**Status:** issues_found

## Summary

Both files implement the Phase 3 game loop contract. The CSS file is largely correct — all properties use `-fx-` prefixes, no `setStyle()` calls exist in Java, and `javafx.animation.Timeline` is used exclusively (no `java.util.Timer`). The single `endEpisode()` entry point contract is met.

Three correctness bugs were found in `PrimaryController.java`. Two concern the interaction between `PauseTransition` callbacks and concurrent game state changes (Twist during flash, timer expiry during flash), both of which leave the UI in a broken, unrecoverable state. One concerns the Last Word handler using stale button references after Twist, which silently breaks the letter-disable invariant. Two warnings cover a dead CSS rule and a missing `foundWords` reset that will corrupt state if `startGame()` is ever called more than once.

---

## Critical Issues

### CR-01: PauseTransition `onFinished` re-enables letter buttons after `endEpisode()` fires during the flash window

**File:** `src/main/java/cs120/lab06/PrimaryController.java:108-115` and `117-126`

**Issue:** `endEpisode()` calls `gameTimeline.stop()` and disables all letter buttons, but any `PauseTransition` that was already started before the timer expired continues running on the FX thread. When it fires (up to 500 ms later), its `onFinished` lambda executes `letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(false))`. This re-enables all letter buttons after the episode has ended, allowing the player to click letters and submit guesses in a state where `playing == false`. The `if (!playing) return` guard in `handleLetterButton` blocks the click handler, but the buttons appear gold and interactive — and pressing Enter in this state will silently no-op due to the `playing` guard — which is a confusing, broken UI state.

This can be reproduced reliably: submit a valid (non-target) word with fewer than 500 ms left on the timer. The green flash fires, then `onTick` fires, `endEpisode()` runs, disables everything — then 500 ms later `onFinished` re-enables the letter buttons.

**Fix:** Check `playing` inside both `onFinished` lambdas before re-enabling buttons:

```java
pause.setOnFinished(e2 -> {
    guessDisplay.getStyleClass().remove("guess-valid");
    pressedButtons.clear();
    guessDisplay.getChildren().clear();
    if (playing) {
        letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(false));
    }
});
```

Apply the same guard to the `guess-invalid` `onFinished` lambda (lines 119-124).

---

### CR-02: Twist during a PauseTransition flash disables all newly-built letter buttons

**File:** `src/main/java/cs120/lab06/PrimaryController.java:82-89` and `108-115`

**Issue:** The `twistBtn` handler has no mechanism to cancel or coordinate with an in-flight `PauseTransition`. If the player submits a valid word and then immediately clicks Twist within the 500 ms flash window, the following sequence occurs:

1. Enter handler starts a `PauseTransition`, `pressedButtons` still holds the submitted buttons.
2. Twist fires: `pressedButtons.clear()`, `letterButtonArea.getChildren().clear()`, `buildLetterButtons()` populates `letterButtonArea` with fresh `Button` objects.
3. 500 ms later, `onFinished` runs: `pressedButtons.clear()` (already empty, harmless), `guessDisplay.getChildren().clear()` (harmless), then `letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(false))` — this iterates the **new** buttons from `buildLetterButtons()` and calls `setDisable(false)` on them. Normally that is correct. But if it was the **invalid** branch: `onFinished` calls `setDisable(false)` on all new buttons, which is fine. However, for the **valid** branch, there is an additional problem: if the user then selects letters (clicking new buttons, disabling them), `onFinished` fires and **re-enables those mid-press buttons**, corrupting `pressedButtons` vs the UI (buttons enabled but still in `pressedButtons`).

The most damaging concrete case: user presses Enter (invalid word), Twist fires during flash, `buildLetterButtons()` creates new buttons, `onFinished` runs `setDisable(false)` — which is correct for the new buttons — but `pressedButtons` was cleared by Twist so it's already empty. Actually this particular case is benign. The **dangerous** case is: valid word Enter (buttons for that word are in `pressedButtons` and still disabled) → user presses Twist during flash → `pressedButtons.clear()`, new buttons created → `onFinished` fires → `setDisable(false)` on new buttons, but the old disabled buttons are now detached. Effectively the new buttons that the user may have partially clicked get re-enabled. If the user clicked any new buttons between the Twist and the `onFinished` fire, those new buttons get force-re-enabled while still in `pressedButtons`, causing tiles visible in `guessDisplay` but corresponding buttons no longer disabled.

**Fix:** Stop the in-flight `PauseTransition` before rebuilding, or disable Twist during the flash window. The simplest approach is to hold a reference to the active pause and cancel it on Twist:

```java
private PauseTransition activePause;

// In Twist handler, before buildLetterButtons():
if (activePause != null) {
    activePause.stop();
    activePause = null;
    guessDisplay.getStyleClass().removeAll("guess-valid", "guess-invalid");
}

// When creating each PauseTransition, assign it:
activePause = new PauseTransition(Duration.millis(500));
activePause.setOnFinished(e2 -> {
    activePause = null;
    // ... rest of cleanup
});
activePause.play();
```

---

### CR-03: Last Word after Twist disables stale detached buttons, leaving current letter buttons fully enabled

**File:** `src/main/java/cs120/lab06/PrimaryController.java:129-139`

**Issue:** `lastWordButtons` stores references to the `Button` objects that existed in `letterButtonArea` at the time a valid word was entered. When the player clicks Twist, `letterButtonArea.getChildren().clear()` detaches those buttons from the scene and `buildLetterButtons()` creates entirely new `Button` instances. `lastWordButtons` still holds references to the old, now-detached buttons.

When the player clicks Last Word after a Twist:
- `pressedButtons = new ArrayList<>(lastWordButtons)` — old detached buttons
- `for (Button btn : pressedButtons) btn.setDisable(true)` — disables old **detached** buttons (no visual effect)
- Tiles are built from `btn.getText()` on the old buttons — the letter text is correct
- But the **current** buttons in `letterButtonArea` that represent the same letters are **not disabled**

The player sees gold tiles in the guess display but the corresponding letter buttons in the letter area remain enabled. They can click those same letters again, appending duplicate tiles to `guessDisplay` and building a multi-letter guess that breaks the letter-count invariant.

CLAUDE.md rule 4 states: "Track letter button state by index — not by character value; duplicate letters require separate button objects." The root cause here is exactly this: tracking by reference to old objects rather than by position in the current live button list.

**Fix:** After Twist, invalidate `lastWordButtons` and disable the Last Word button so it can only be used before the next Twist. This matches the existing Twist handler behavior (`lastWordBtn.setDisable(true)` at line 88) — but that call only happens when the Twist button is pressed, not when Last Word is clicked and then Twist is pressed. Verify the disable call on line 88 is already there. It is — so the bug only manifests if: (1) valid word is entered, (2) Last Word is clicked (restoring guess), (3) Twist is clicked. After step 3, `lastWordBtn` is disabled, so this specific three-step sequence is safe. However, the reverse order — (1) valid word entered, (2) Twist pressed, (3) Last Word NOT yet disabled — cannot occur because line 88 disables `lastWordBtn` on every Twist.

Re-examining: `lastWordBtn.setDisable(true)` at line 88 IS called in the Twist handler before `buildLetterButtons()`. So immediately after Twist, `lastWordBtn` is disabled. The only window where Last Word can be clicked while `lastWordButtons` holds old references that are still in `letterButtonArea` is **between** a valid guess entry and the next Twist — which is the correct behavior. The bug described above (Twist then Last Word) is actually blocked because Twist disables Last Word.

**Revised assessment:** CR-03 is downgraded — the disable on line 88 prevents the broken sequence. However, there IS still a subtle issue: the Last Word handler sets `pressedButtons = new ArrayList<>(lastWordButtons)` and iterates it to disable buttons and build tiles. After this, if the user calls Twist, the Twist handler's `buildLetterButtons()` creates new buttons, but **`pressedButtons` now holds the old Last Word buttons**, which are detached. The `PauseTransition` path (from a subsequent Enter on this rebuilt guess) will then call `setDisable(false)` on those detached old buttons, not the new ones. This is the same class of stale-reference bug and it is real for the sequence: Last Word → Twist (disabled, so impossible) — again blocked. The disable chain is correct for Phase 3.

**Reclassifying CR-03 to WARNING** — see WR-01 below.

---

## Warnings

### WR-01: `lastWordButtons` holds stale button references after Twist — fragile invariant maintained only by `setDisable`

**File:** `src/main/java/cs120/lab06/PrimaryController.java:88` and `101`

**Issue:** The correctness of the Last Word feature depends on the Twist handler always calling `lastWordBtn.setDisable(true)` (line 88) before `buildLetterButtons()`. This works in Phase 3, but the invariant is implicit and brittle. If Phase 4 adds any code path that calls `buildLetterButtons()` without also disabling `lastWordBtn` (e.g., advancing to the next level re-builds buttons), the stale-reference bug will silently activate: `lastWordButtons` holds old detached `Button` objects, `btn.setDisable(true)` has no effect on the current visible buttons, and the player can double-select letters.

CLAUDE.md rule 4 says to track by index. The fix is to store `lastWordLetters` as a `List<String>` (letter text only), not `List<Button>`, and look up matching live buttons in `letterButtonArea` when restoring:

```java
private List<String> lastWordLetters = new ArrayList<>();

// In enterBtn handler (valid word path), replace:
lastWordButtons = new ArrayList<>(pressedButtons);
// With:
lastWordLetters = pressedButtons.stream()
    .map(Button::getText)
    .collect(Collectors.toList());

// In lastWordBtn handler, replace the body with:
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

### WR-02: `foundWords` list is never cleared — `startGame()` is not safe to call a second time

**File:** `src/main/java/cs120/lab06/PrimaryController.java:57-59` and `64`

**Issue:** `foundWords` is initialized once in `initialize()` and never cleared in `startGame()`. If Phase 4 calls `startGame()` again (e.g., for a new game after level completion), found words from the previous episode will persist in the list display. The score and level labels are reset by `startGame()`, but the found-words list is not. `secondsLeft` is reset to 120, `playing` to true, and a new `TwistController` is constructed — but the old `foundWords` content remains visible on screen.

Additionally, the old `gameTimeline` is not stopped before creating a new one. If `startGame()` is called while the old timeline is still running (theoretically possible in Phase 4), two timelines will fire `onTick()` simultaneously, causing `secondsLeft` to decrement twice per second.

**Fix:**
```java
private void startGame() {
    if (gameTimeline != null) {
        gameTimeline.stop();
    }
    foundWords.clear();          // reset found words list display
    pressedButtons.clear();      // clear any in-progress guess state
    lastWordButtons.clear();
    // ... rest of startGame
}
```

---

## Info

### IN-01: Dead CSS rule — `-fx-text-fill` on `.root` does not cascade to `Label` nodes in JavaFX

**File:** `src/main/resources/cs120/lab06/styles.css:17`

**Issue:** The `.root` block sets `-fx-text-fill: #e0e0e0`. In JavaFX, `-fx-text-fill` is not an inherited property — it does not cascade through the scene graph to `Label`, `Button`, or other text-bearing nodes. This rule has no visible effect; it is dead CSS. The labels that need light-colored text correctly use their own CSS class selectors (`.info-label`, `.time-label`) that set `-fx-text-fill` explicitly.

**Fix:** Remove the dead line to avoid confusion:
```css
.root {
    -fx-background-color: #1a1a2e;
    -fx-font-family: "System";
    /* remove: -fx-text-fill: #e0e0e0; */
}
```

---

### IN-02: Handler wiring inside `startGame()` re-registers on every call — `setOnAction` silently replaces, but is misleading

**File:** `src/main/java/cs120/lab06/PrimaryController.java:73-141`

**Issue:** `clearBtn`, `twistBtn`, `enterBtn`, and `lastWordBtn` handlers are registered with `setOnAction` inside `startGame()`. `setOnAction` replaces the previous handler (not additive), so for Phase 3's single-call lifecycle this is harmless. However, placing handler wiring inside `startGame()` rather than `initialize()` implies they need to be re-wired each time a game starts, which is not true — the handlers are stateless closures that reference instance fields. In Phase 4, when `startGame()` is called again to advance levels, the handlers will be re-registered unnecessarily, and any developer scanning `initialize()` will not find where the button handlers are set.

**Fix:** Move the four control-button `setOnAction` calls to `initialize()`, after `startGame()`. Keep only game-state initialization logic inside `startGame()`.

---

_Reviewed: 2026-05-13T02:53:24Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
