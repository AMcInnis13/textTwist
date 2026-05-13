# Phase 3: Core Game Loop - Context

**Gathered:** 2026-05-12
**Status:** Ready for planning

<domain>
## Phase Boundary

Wire all PrimaryController event handlers for a single complete game episode. The player clicks letter buttons to build a guess, submits via Enter, clears via Clear, reshuffles via Twist, and recalls the last valid word via Last Word — all while a 120-second javafx.animation.Timeline counts down. The episode ends when the timer expires or the player guesses the target word (all letters). Phase 3 delivers a fully playable single episode with correct scoring and timer states.

Requirements in scope: CTRL-01, CTRL-02, CTRL-03, CTRL-04, CTRL-05, CTRL-06, CTRL-07, TIMER-01, TIMER-02, TIMER-03, TIMER-04, TIMER-05, PROG-01, PROG-03, PROG-07, PROG-08

Out of scope: level advancement evaluation, game-over/win overlays, valid/invalid visual polish (VFX-01, VFX-02, VFX-03) — those are Phase 4.

</domain>

<decisions>
## Implementation Decisions

### Guess State Representation
- **D-01:** PrimaryController maintains `List<Button> pressedButtons` to track the in-progress guess in press order. Direct Button references satisfy CLAUDE.md's index-tracking rule (each Button object is unique, even with duplicate letters) without requiring a parallel integer array.
- **D-02:** Guess string is derived on-demand at Enter time by concatenating `button.getText()` for each button in pressedButtons. No parallel StringBuilder needed.
- **D-03:** guessDisplay shows pressed letters as `Label` children with CSS class `.letter-slot` (defined in styles.css, Phase 2). PrimaryController creates a new `Label(button.getText())`, calls `getStyleClass().add("letter-slot")`, and appends it to `guessDisplay.getChildren()` on each button press.

### Letter Button Creation (CTRL-01, CTRL-07)
- **D-04:** `beginEpisode()` in PrimaryController calls `twistController.beginEpisode(letterCount)`, then `buildLetterButtons()` — a helper that calls `shuffleLetters()`, creates one `Button` per letter, adds each to `letterButtonArea.getChildren()`, and wires the click handler. No static buttons in FXML; all created dynamically.
- **D-05:** Letter buttons are created fresh at episode start (and on Twist). The all-Buttons list that was in the FlowPane constitutes the "letter button set" — no separate parallel `Button[]` array needed; `pressedButtons` tracks state by object reference (D-01).

### Clear Button (CTRL-03)
- **D-06:** Clear re-enables every button in `letterButtonArea.getChildren()` (cast to `Button`) and clears `pressedButtons` and `guessDisplay.getChildren()`. This is safe because the FlowPane children list IS the letter button set.

### Twist Button (CTRL-04)
- **D-07:** Twist RECREATES letter buttons in a new shuffled order (not relabeling in-place). Implementation: clear `letterButtonArea.getChildren()`, clear `pressedButtons`, clear `guessDisplay.getChildren()`, call `buildLetterButtons()` again (which calls `shuffleLetters()` for the new order).
- **D-08:** After Twist, Last Word button is disabled. User must earn another valid guess within the episode to re-enable it. This avoids stale button reference issues since buttons are recreated.

### Last Word Button (CTRL-06)
- **D-09:** PrimaryController maintains `List<Button> lastWordButtons` — a copy of `pressedButtons` saved at the moment of a successful Enter. When Last Word is pressed: copy `lastWordButtons` into `pressedButtons`, disable each button in lastWordButtons, and rebuild `guessDisplay.getChildren()` with `.letter-slot` Labels.
- **D-10:** Last Word is initialized as disabled in FXML (`disable="true"` — already set in Phase 2). It is enabled after the first successful valid-word guess in an episode. It is disabled again after a Twist (D-08).

### Enter / Rejection Feedback (CTRL-05)
- **D-11:** On invalid or duplicate word: add CSS class `.guess-invalid` to `guessDisplay`, wait ~500ms via `PauseTransition`, then remove `.guess-invalid`, clear the guess (clear pressedButtons, clear guessDisplay children, re-enable all buttons). Player starts fresh.
- **D-12:** On valid word: add CSS class `.guess-valid` to `guessDisplay`, wait ~500ms via `PauseTransition`, then remove `.guess-valid` and clear the guess. Score and found-words list update immediately (before the pause) so the player sees the updated state during the flash.
- **D-13:** CSS classes `.guess-invalid` and `.guess-valid` must be added to `styles.css`. `.guess-invalid` = red background/border; `.guess-valid` = green background/border. Consistent with the dark theme (dark text on colored background).

### Timer (TIMER-01 through TIMER-05)
- **D-14:** `javafx.animation.Timeline` with a `KeyFrame` every 1 second. Fires on the FX thread — safe to update timeLabel directly in the handler.
- **D-15:** Timer label displays `mm:ss` format (e.g., "2:00", "0:59"). `timeLabel.setText(String.format("%d:%02d", minutes, seconds))`.
- **D-16:** CSS class management on timeLabel follows CLAUDE.md pitfall: remove the old class before adding the new one. At ≤20s: remove `.timer-critical` (if present), add `.timer-warning`. At ≤10s: remove `.timer-warning` (if present), add `.timer-critical`. At start/above 20s: ensure both classes removed.
- **D-17:** Timer expiry calls `endEpisode()` and stops the Timeline. `endEpisode()` is the single entry point for both timer expiry and target-word-guessed events (CLAUDE.md rule).

### Target Word Detection (PROG-03)
- **D-18:** After each successful Enter (checkGuessWord returns > 0 points), compare the submitted word (uppercase) to `twistController.getTargetWord()` (uppercase). If equal, call `endEpisode()` immediately — episode ends before the normal flash-and-clear sequence completes. Stops the Timeline.

### isFormableFrom check
- **D-19:** NOT needed as a separate code check. Letter buttons are created from the target word's letters only — any guess formed by clicking buttons is implicitly a valid subset. No `isFormableFrom` implementation required in Phase 3.

### Initialization Sequence
- **D-20:** `PrimaryController.initialize()` (called by FXMLLoader) sets up the `foundWords` ObservableList, then immediately calls `startGame()` which initializes `TwistController("twister_words")`, calls `twistController.beginEpisode(3)` (Level 1, 3-letter words), calls `buildLetterButtons()`, and starts the Timeline. No "Start" button needed — game begins on window open.
- **D-21:** `TwistController` field is declared in `PrimaryController` as a non-FXML instance field, initialized in `startGame()`.

### GameState
- **D-22:** A simple `boolean playing` field (or a minimal `GameState` enum with PLAYING/ENDED) guards against clicks after episode end. Phase 4 expands state management. Claude decides the minimal approach.

### Scoring Display (PROG-07, PROG-08)
- **D-23:** `scoreLabel` and `levelLabel` are updated immediately in the Enter handler after a valid word. Format: `scoreLabel.setText("Score: " + twistController.getScore())`. Level label unchanged during Phase 3 (single episode at Level 1).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Specification
- `textTwist.pdf` — Lab06 spec; verify endEpisode behavior and exact visual feedback descriptions
- `CLAUDE.md` — Critical implementation rules: Timeline (not java.util.Timer), no inline setStyle(), CSS class accumulation pitfall on timer label, single endEpisode() entry point, track letter buttons by index (not character), TwistController zero JavaFX imports

### Requirements
- `.planning/REQUIREMENTS.md` — Phase 3 requirements: CTRL-01 through CTRL-07, TIMER-01 through TIMER-05, PROG-01, PROG-03, PROG-07, PROG-08 (full text)

### Prior Phase Artifacts
- `.planning/phases/02-fxml-layout-css/02-CONTEXT.md` — All fx:id names, CSS class names (.timer-warning, .timer-critical, .letter-slot, .letter-btn), layout structure decisions from Phase 2
- `.planning/phases/02-fxml-layout-css/02-03-PLAN.md` — PrimaryController Phase 2 wiring details, App.java CSS attachment approach

### Existing Source Files (must be modified)
- `src/main/java/cs120/lab06/PrimaryController.java` — Has all @FXML fields; needs event handlers, Timeline, game model wiring, and new helper methods
- `src/main/resources/cs120/lab06/styles.css` — Needs `.guess-valid` and `.guess-invalid` added for feedback states

### Source Files (no changes)
- `src/main/java/cs120/lab06/TwistController.java` — Complete; use `beginEpisode()`, `checkGuessWord()`, `shuffleLetters()`, `getTargetWord()`, `getScore()`, `getLevelScore()`, `getLevel()`
- `src/main/java/cs120/lab06/WordDictionary.java` — No changes
- `src/main/resources/cs120/lab06/primary.fxml` — No changes; all fx:ids already locked

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `TwistController.beginEpisode(int letterCount)` — loads dictionary for n-letter words, picks random target word, resets levelScore and guessedWords; call at game start and level advance
- `TwistController.checkGuessWord(String wrd)` — validates word, returns n*n*10 or 0; handles duplicates and invalid words internally; call on Enter
- `TwistController.shuffleLetters()` — returns `List<String>` of target word letters in random order; call in buildLetterButtons() and Twist handler
- `TwistController.getTargetWord()` — returns current target word uppercase; compare against guess for PROG-03 check

### Established Patterns
- All CSS via class selectors (no setStyle) — add/remove class strings via `node.getStyleClass().add()` / `remove()`
- CSS timer class management: `timeLabel.getStyleClass().removeAll("timer-warning", "timer-critical")` then add the needed class — avoid class accumulation
- `foundWords` ObservableList already bound to `foundWordsList` in `initialize()` — just call `foundWords.add(word)` to update the list view
- `lastWordBtn` starts with `disable="true"` in FXML — already correct

### Integration Points
- `letterButtonArea` (FlowPane) — Phase 3 adds/removes Button children dynamically; no FXML children expected
- `guessDisplay` (HBox) — Phase 3 adds/removes Label children with `.letter-slot` class; no FXML children expected
- All four control buttons (`twistBtn`, `enterBtn`, `lastWordBtn`, `clearBtn`) — Phase 3 wires `setOnAction` handlers

</code_context>

<specifics>
## Specific Ideas

- Flash duration: ~500ms (`PauseTransition(Duration.millis(500))`) for both valid and invalid feedback
- `buildLetterButtons()` is a private helper in PrimaryController — called from `startGame()` and Twist handler
- `startGame()` is a private helper — called from `initialize()`; initializes TwistController and Timeline
- Guess string on Enter: `pressedButtons.stream().map(Button::getText).collect(Collectors.joining())`

</specifics>

<deferred>
## Deferred Ideas

- **Visual polish (VFX-01, VFX-02, VFX-03)** — green/red highlight polish, end-of-episode overlay — Phase 4
- **Level advancement logic (PROG-02, PROG-04, PROG-05, PROG-06)** — game-over/win evaluation, advancing letter count — Phase 4
- **GameState GAME_OVER / WIN states** — Phase 4 will extend whatever enum Phase 3 introduces
- **Keyboard input** — v2 requirement; out of scope
- **Sound effects** — out of scope

</deferred>

---

*Phase: 3-Core Game Loop*
*Context gathered: 2026-05-12*
