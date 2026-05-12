# Features Research — JavaFX TextTwist

**Domain:** Letter-selection word game (TextTwist clone)
**Researched:** 2026-05-12
**Confidence:** MEDIUM (training-data knowledge of JavaFX + word game UX; web/Context7 lookups blocked in this session)

---

## Table Stakes (Must Have)

These are the UX behaviors that, if absent, make the game feel broken or unplayable. They
are the minimum bar for a product that feels intentional rather than like a skeleton.

| Feature | Why Essential | Complexity |
|---------|--------------|------------|
| Letter buttons visually disable on selection | Players must see which letters are in-play vs available; without this, double-selection bugs surface and the game state is unreadable | Low — `button.setDisable(true)` + CSS `.button:disabled` rule |
| Guess word area clears instantly on Clear/Twist/Enter | Any lag or residual state here causes confusion about what is being submitted | Low — clear a Label or HBox of letter Labels |
| Visual confirmation on correct word (brief highlight or color flash) | Without feedback, players don't know if Enter worked; they may click repeatedly | Low-Med — `PseudoClass` + CSS transition or a Timeline fade |
| Visual rejection feedback on wrong word (shake or red flash) | Silent failure feels like a bug; players need to know the word was rejected, not that the button didn't register | Low-Med — Timeline-based color flash on the guess display |
| Found-words list updates immediately on successful guess | The scrollable list is the player's primary score record; delayed updates break trust | Low — `ObservableList` bound to a `ListView` auto-updates |
| Timer turns red / changes color at low time (≤10s or ≤20s) | This is universal in countdown UX; its absence makes the timer feel decorative | Low — Timeline listener checks remaining seconds, applies CSS class |
| Timer display in mm:ss format | Specified by the assignment; also matches player expectation from real TextTwist | Low — `String.format("%02d:%02d", min, sec)` |
| Score label updates immediately after each accepted word | Delayed score updates feel like a scoring bug | Low — bind Label to a `SimpleIntegerProperty` or update in handler |
| All letter buttons re-enabled when Clear is pressed | If any button stays disabled after Clear, the board is permanently broken | Low — iterate buttons, setDisable(false) |
| Twist reshuffles letter order visibly | Players use Twist when stuck; if the order looks the same they assume it failed | Low — shuffle List<String>, rebuild button order |
| Enter requires at least one letter selected | Submitting an empty guess must be silently ignored (not crash, not show error) | Low — guard clause in handler |
| Level/score display shows current level number and score | Players need to know what level they're on and what they're working toward | Low |
| End-of-episode result screen (advance / retry / game over) | Without this, players don't understand why the board changed | Low-Med — simple Dialog or overlayPane |

---

## Differentiators (Nice to Have)

These add polish and delight but are not required for the assignment grade or basic playability.

| Feature | Value | Complexity |
|---------|-------|------------|
| Animated letter button highlight on hover | Makes the UI feel responsive and game-like vs a form | Low — CSS `:hover` pseudo-class, scale transform |
| "Word already found" feedback (distinct from "invalid word") | Reduces player frustration when they type a word they already submitted | Low — check found-words set before dictionary lookup |
| Minimum score threshold indicator (progress bar or threshold label) | Players can see how close they are to advancing without doing mental math | Low-Med — Label showing threshold vs current levelScore |
| Subtle timer pulse or animation in final 10 seconds | Increases tension without being annoying | Med — CSS keyframe animation on timer label |
| Correct-word sound cue (system beep or AudioClip) | Standard in word games; creates satisfying feedback loop | Med — `AudioClip` or `Media`; requires an audio file |
| Letter buttons slightly larger / more game-styled than default | Default JavaFX buttons look like desktop form controls, not a game board | Low — CSS padding, border-radius, font-weight |
| Found-words list sorted alphabetically or by length | Easier to scan; reduces "did I already find that?" confusion | Low — `FXCollections.sort()` after each add |
| "Last Word" button visually disabled when no previous word exists | Without this, pressing Last Word on a fresh board is confusing | Low — track previousWord state, enable/disable accordingly |
| Distinct color per word length in found-words list | Makes the list visually scannable; common in TextTwist clones | Med — custom `ListCell` factory with CSS class per length |

---

## Anti-Features (Deliberately Avoid)

| Feature | Why Skip |
|---------|---------|
| Keyboard input for letter selection | The spec defines letter buttons as the input method; adding keyboard shortcuts adds scope and may conflict with the FXML/controller wiring the grader expects |
| Animated tile-flip or 3D letter effects | High complexity for a JavaFX assignment; CSS 3D transforms are fragile across JVM versions and add zero grade value |
| Hint system (reveal a letter) | Not in spec; changes game balance; out of scope per PROJECT.md |
| Pause button | Not specified; the 120-second timer is a core mechanic — pausing undermines it and adds state machine complexity |
| Online dictionary validation (network call) | Assignment uses local twister_words/ files; network adds latency, failure modes, and scope the grader won't evaluate |
| Per-letter animation delays on Twist | Looks impressive but is disproportionate complexity for a CS120 assignment; can mask shuffle bugs |
| Score history / statistics across sessions | PROJECT.md explicitly excludes save/load game state |
| Custom CSS themes / theme switcher | One coherent visual style is the goal; a theme switcher adds UI scope without adding game quality |

---

## UX Patterns

### 1. Letter Button State Machine
Each button has exactly three states: available (enabled, default style), selected (disabled,
highlighted — the letter is in the guess word), and absent-from-set (disabled, grayed — not
part of the current level). On Clear or correct submission, all current-level buttons return
to available. Use a CSS PseudoClass (`selected`) or simply `.setDisable(true)` plus a
secondary CSS rule on `:disabled` to visually distinguish the two disabled states.

Recommended approach: keep a parallel `List<Button>` that mirrors the current letter set.
When a letter button is clicked, disable it and append the letter to the guess display. Clear
iterates the list and re-enables all. This is simpler than tracking indices through the
FXML node tree.

### 2. Guess Word Display
Standard pattern: a fixed-width HBox (or FlowPane) of Labels, one per selected letter, using
a monospace or block font. This is cleaner than a single TextField because it visually
mirrors the letter-button metaphor and prevents the player from typing directly. Max 10
children (matching the Level 8 word length). Clear empties the HBox children; each letter
click adds a Label child.

Avoid a TextField here — it invites keyboard input the spec doesn't require and makes the
button-state sync harder.

### 3. Timer Color Escalation (Universal Pattern)
Standard word-game timer UX follows this gradient:
- 120–21 seconds: neutral/white — no urgency
- 20–11 seconds: yellow/amber — approaching
- 10–0 seconds: red, optionally bold or larger — critical

Implement with a `javafx.animation.Timeline` that fires every 1000ms, decrements the counter,
formats the label, and applies a CSS style class (`timer-warning`, `timer-critical`) by calling
`label.getStyleClass()`. Remove the previous class before adding the new one to avoid class
accumulation bugs.

### 4. Scrollable Found-Words List
Use `ListView<String>` bound to an `ObservableList<String>`. JavaFX `ListView` is scroll-capable
by default when its content exceeds its visible height — no manual `ScrollPane` wrapping needed.
Set a fixed `prefHeight` in FXML (e.g., 200–300px) so the list doesn't collapse or expand
with content. Adding to the `ObservableList` automatically updates the view.

If words should be sorted, call `FXCollections.sort(observableList)` after each add. For word-length
coloring, use a `Callback<ListView<String>, ListCell<String>>` cell factory.

### 5. Visual Feedback Without External Libraries
JavaFX Timeline is the standard tool for timed visual effects without third-party dependencies:
- **Correct word flash**: Set label background to green, schedule a KeyFrame at 400ms to revert
- **Wrong word flash**: Set label background to red, revert at 400ms
- **Timer pulse**: Use a `ScaleTransition` on the timer label in the final 10 seconds

All effects should use CSS classes toggled via `getStyleClass().add/remove`, not inline
`setStyle()` calls — this keeps the CSS file as the single source of style truth (matches
the "impeccable style" grading criterion).

### 6. End-of-Episode Feedback
Use a `javafx.scene.control.Alert` (type INFORMATION or custom) or a semi-transparent overlay
Pane laid over the game board. The overlay pattern is more game-like; Alert is simpler and
requires less layout work. For a CS120 assignment, Alert is the pragmatic choice unless the
grader's rubric specifically mentions visual polish for this screen.

Show: result (advance / stay / game over), level score, threshold, and a button to continue.

### 7. "Last Word" Button Semantics
This button is unusual. The correct UX: it repopulates the guess display with the letters of
the last successfully found word (in the same order), re-disabling those letter buttons, so
the player can extend it with a suffix. Edge cases to guard: no previous word (button should
be disabled or no-op), previous word's letters no longer available in current letter set
(impossible by design since level only changes after episode end).

### 8. CSS File Structure for a Game UI
The spec requires an external CSS file. For a game-style look:
- Use a dark or saturated background color for the game board region (not plain white)
- Letter buttons: `border-radius: 8px`, `font-weight: bold`, `font-size: 16px`, padding `10px 14px`
- Disabled letter buttons: lower opacity (0.4) and a distinct background to read as "used"
- Timer label: monospace font (`font-family: monospace`), large size (24px+)
- Found-words list: slightly inset appearance (inset border or darker background)

JavaFX CSS uses `-fx-` prefixed properties (e.g., `-fx-background-color`, `-fx-font-size`).
Standard CSS property names without the prefix do not apply to JavaFX nodes.

---

## Assignment-Specific Notes

- The spec grades on "impeccable style" via an external CSS file. This means visual polish is
  explicitly graded, not optional. Table-stakes styling (button states, timer color, distinct
  regions) is likely part of the rubric even if not listed line-by-line.
- The MVC constraint (model/FXML view/controller) means no business logic in event handlers —
  all word validation goes through `TwistController.checkGuessWord()`, not inline in the
  FXML controller. Graders will check this.
- UML in appendices A and B is the authoritative method signature contract. Features that
  require adding public methods not in the UML are risky — add private helpers only.

---

## Sources

- JavaFX 21 official documentation (openjfx.io) — HIGH confidence for API behavior
- Training-data knowledge of TextTwist (original GameHouse release) and word-game UX conventions — MEDIUM confidence
- CS120 PROJECT.md spec requirements — HIGH confidence (primary source)
- JavaFX CSS Reference Guide (openjfx.io/javadoc/21) — HIGH confidence for `-fx-` property behavior
- Note: WebSearch and Bash tool calls were unavailable in this research session; ecosystem
  search findings are based on training data and should be validated against current JavaFX
  community patterns if significant uncertainty exists.
