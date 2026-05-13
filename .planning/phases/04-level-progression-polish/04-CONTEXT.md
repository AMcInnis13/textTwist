# Phase 4: Level Progression + Polish - Context

**Gathered:** 2026-05-12
**Status:** Ready for planning

<domain>
## Phase Boundary

Extend `endEpisode()` with score threshold evaluation to determine advance / game-over / game-won outcomes. Show the result via a JavaFX Alert dialog. On advance, reset the board and start the next episode at a higher letter count. On game-over or game-won, show the result then reset fully back to Level 1. Fix two known bugs from Phase 3 review (WR-01, WR-02) that would corrupt state during level transitions.

Requirements in scope: PROG-02, PROG-04, PROG-05, PROG-06, VFX-01, VFX-02, VFX-03

Note: VFX-01 (.guess-valid flash) and VFX-02 (.guess-invalid flash) are already implemented and working from Phase 3. Phase 4 does NOT need to add or change them.

Out of scope: keyboard input, sound effects, per-level animation, list-cell coloring, hint system (v2 requirements).

</domain>

<decisions>
## Implementation Decisions

### End-of-Episode Result Overlay (VFX-03)
- **D-01:** Use `Alert.AlertType.INFORMATION` with a single OK button for all three outcomes (advance, game-over, game-won). Spec says "overlay or dialog" — Alert satisfies this. Same Alert type for all outcomes keeps the implementation uniform.
- **D-02:** Alert content = just the outcome message (no score summary). Messages:
  - Advance: `"Advanced to Level [N]!"` (where N = new level number)
  - Game over: `"Game Over — not enough points."`
  - Game won: `"You Win!"`

### Level Advance Flow (PROG-02, PROG-04)
- **D-03:** After the player dismisses the advance Alert, the next episode starts immediately — no extra Start button. The Alert's `showAndWait()` call blocks until the player clicks OK, then execution resumes in `endEpisode()` and begins the new episode inline.
- **D-04:** On level advance, reset the following in PrimaryController: clear `foundWords` list, reset `secondsLeft` to 120, clear `guessDisplay` children, clear `pressedButtons`, clear `lastWordLetters`, disable `lastWordBtn`, rebuild letter buttons via `buildLetterButtons()`, update `levelLabel` to the new level number, start a new `Timeline`. Cumulative score is preserved automatically — `TwistController.beginEpisode()` does NOT reset `score`.

### Restart Behavior (PROG-05, PROG-06)
- **D-05:** After game-over Alert is dismissed, call `startGame()` — full reset: score to 0, Level 1 (3-letter words), fresh timer. Same behavior for game-won.
- **D-06:** Both game-over and game-won restart identically — both call `startGame()` after the Alert is dismissed. No content distinction between the two reset paths.

### Bug Fixes (non-negotiable, from Phase 3 review)
- **D-07 (WR-01):** Change `lastWordButtons` field from `List<Button>` to `List<String> lastWordLetters`. On successful Enter, store letters: `pressedButtons.stream().map(Button::getText).collect(Collectors.toList())`. On Last Word press: iterate `lastWordLetters`, for each letter find the first enabled button in `letterButtonArea` with matching text, disable it, add to `pressedButtons`, add a `.letter-slot` Label to `guessDisplay`. This handles duplicate letters correctly and survives level-advance button rebuilds.
- **D-08 (WR-02):** `startGame()` must (a) stop `gameTimeline` if it is non-null before creating a new one, and (b) call `foundWords.clear()` before reinitializing the list binding.

### Level Tracking
- **D-09:** PrimaryController maintains `int letterCount` field (initialized to 3). On each level advance: `letterCount++`. The level number shown to the player = `letterCount - 2` (since Level 1 uses 3 letters). Update `levelLabel.setText("Level: " + (letterCount - 2))` after each advance. `TwistController.getLevel()` returns a stale `1` — do NOT use it for display; use `letterCount - 2` directly.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Specification
- `textTwist.pdf` — Lab06 spec; verify exact win/game-over wording and overlay requirements
- `CLAUDE.md` — Critical rules: Timeline (not java.util.Timer), no inline setStyle(), single endEpisode() entry point, track letter buttons by index not character, TwistController zero JavaFX imports

### Requirements
- `.planning/REQUIREMENTS.md` — Phase 4 requirements: PROG-02, PROG-04, PROG-05, PROG-06, VFX-01, VFX-02, VFX-03 (full text)

### Prior Phase Artifacts
- `.planning/phases/03-core-game-loop/03-CONTEXT.md` — All Phase 3 decisions locked: guess state (D-01 to D-23), timer behavior, endEpisode() skeleton, PauseTransition pattern
- `.planning/phases/03-core-game-loop/03-REVIEW.md` — WR-01 and WR-02 findings with full details

### Existing Source Files (must be modified)
- `src/main/java/cs120/lab06/PrimaryController.java` — Extend endEpisode(), fix lastWordButtons (WR-01), fix startGame() (WR-02), add letterCount field, add level-advance logic
- `src/main/resources/cs120/lab06/styles.css` — No changes needed; .guess-valid and .guess-invalid already defined and working

### Source Files (no changes expected)
- `src/main/java/cs120/lab06/TwistController.java` — beginEpisode() preserves cumulative score; no new methods needed
- `src/main/java/cs120/lab06/WordDictionary.java` — No changes
- `src/main/resources/cs120/lab06/primary.fxml` — No changes needed; Alert dialog is code-only

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `TwistController.beginEpisode(int letterCount)` — call with new letterCount on level advance; resets levelScore and guessedWords, preserves cumulative score. Also used in startGame() for full reset.
- `endEpisode()` — already stops Timeline and disables controls; Phase 4 adds the score evaluation + Alert + routing logic here
- `buildLetterButtons()` — already handles fresh button creation; call again on level advance
- `PauseTransition` pattern — established in Phase 3 for timed UI state changes; not needed for Alert (Alert.showAndWait() blocks)

### Established Patterns
- All CSS via class selectors only — no setStyle() anywhere
- `foundWords.add(word)` to update ListView — foundWords.clear() on reset
- `gameTimeline.stop()` before any new Timeline — must guard null check (first call in startGame() has no prior timeline)
- `timeLabel.getStyleClass().removeAll("timer-warning", "timer-critical")` before adding new class — avoids accumulation

### Integration Points
- `endEpisode()` is the single routing hub: evaluate levelScore threshold → branch to advance / game-over / game-won → show Alert → act on dismissal
- `startGame()` is the full-reset entry point (called from initialize() and after game-over/won)
- Level advance shares most of startGame()'s reset logic but preserves cumulative score — consider extracting a private `beginNextEpisode(int newLetterCount)` helper to avoid duplication

</code_context>

<specifics>
## Specific Ideas

- Alert shows inside `endEpisode()` after stopping the Timeline and disabling controls. Use `Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle("TextTwist"); alert.setHeaderText(null); alert.setContentText("..."); alert.showAndWait();` then route based on outcome.
- Score threshold formula: `floor(0.25 * max * max * 10)` where `max = letterCount` at time of episode end. Java: `(int)(0.25 * letterCount * letterCount * 10)`.
- `levelCount - 2` maps letter count to level number for display and win condition check. Win condition: `letterCount == 10` AND player either guessed target word OR met threshold at timer expiry.
- The `lastWordLetters` duplicate-handling loop: iterate list in order, match to first ENABLED button in `letterButtonArea.getChildren()` with matching text — disable immediately so subsequent iterations can't reuse the same button.

</specifics>

<deferred>
## Deferred Ideas

- None — discussion stayed within phase scope.

</deferred>

---

*Phase: 4-Level Progression + Polish*
*Context gathered: 2026-05-12*
