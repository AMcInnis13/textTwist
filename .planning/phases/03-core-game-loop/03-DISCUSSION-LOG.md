# Phase 3: Core Game Loop - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-12
**Phase:** 3-Core Game Loop
**Areas discussed:** Invalid/valid word feedback, Twist: relabel vs recreate, Guess state tracking

---

## Invalid/Valid Word Feedback

### Q1: What happens when Enter is pressed with an invalid/duplicate word?

| Option | Description | Selected |
|--------|-------------|----------|
| Flash red, then auto-clear | Add CSS error class for ~0.5s (PauseTransition), then clear guess and re-enable buttons | ✓ |
| Flash red, keep the guess | Show flash but leave letters; player must Clear manually | |
| You decide | Let Claude pick | |

**User's choice:** Flash red, then auto-clear

---

### Q2: What happens when Enter is pressed with a valid word?

| Option | Description | Selected |
|--------|-------------|----------|
| Flash green, then auto-clear | Add CSS success class for ~0.5s, then clear guess and re-enable buttons | ✓ |
| Flash green, word stays briefly, then clears | ~0.8s hold before clearing | |
| No flash, just clear and update score | Skip animation entirely | |

**User's choice:** Flash green, then auto-clear

---

### Q3: CSS class names for feedback states on guessDisplay?

| Option | Description | Selected |
|--------|-------------|----------|
| .guess-valid and .guess-invalid | Matches kebab-case CSS convention from Phase 2 | ✓ |
| .valid-flash and .invalid-flash | More explicit about the flash behavior | |
| You decide | Claude picks names consistent with styles.css | |

**User's choice:** `.guess-valid` and `.guess-invalid`

---

## Twist: Relabel vs Recreate

### Q1: How should the letter button area update when Twist is clicked?

| Option | Description | Selected |
|--------|-------------|----------|
| Recreate buttons in new order | Clear FlowPane, call shuffleLetters(), build new Button[] with new handlers | ✓ |
| Relabel buttons in-place | Keep same Button objects, reassign text; buttons stay in same FlowPane positions | |

**User's choice:** Recreate buttons in new order

---

### Q2: What happens to Last Word after Twist?

| Option | Description | Selected |
|--------|-------------|----------|
| Disable Last Word after Twist | Predictable behavior; avoids stale button reference issues | ✓ |
| Last Word still works after Twist | Store last word as String, find matching buttons by character scan after Twist | |

**User's choice:** Disable Last Word after Twist

---

## Guess State Tracking

### Q1: How should PrimaryController track pressed letter buttons?

| Option | Description | Selected |
|--------|-------------|----------|
| List<Button> pressedButtons | Direct Button references; each object is unique even with duplicate letters | ✓ |
| List<Integer> of button indices | Parallel Button[] array + integer index list | |
| You decide | Claude picks cleanest approach | |

**User's choice:** `List<Button> pressedButtons`

---

### Q2: How to extract guess string on Enter?

| Option | Description | Selected |
|--------|-------------|----------|
| button.getText() concatenated in order | Stream pressedButtons, map to getText(), join | ✓ |
| Maintain a parallel StringBuilder | Append on click, clear on Clear/Twist/Enter | |

**User's choice:** `button.getText()` concatenated via stream

---

### Q3: What does each pressed letter look like in guessDisplay?

| Option | Description | Selected |
|--------|-------------|----------|
| Label with .letter-slot CSS class | Reuses Phase 2 CSS class designed exactly for this | ✓ |
| Button-like tiles (disabled Buttons) | Heavier alternative | |

**User's choice:** `Label` children with `.letter-slot` CSS class

---

## Claude's Discretion

- **GameState enum scope** — user skipped this area; Claude decides the minimal approach (boolean `playing` field or minimal PLAYING/ENDED enum). Phase 4 will extend it.
- **`buildLetterButtons()` helper structure** — Claude implements as private method in PrimaryController
- **`startGame()` helper** — Claude implements initialization sequence
- **PauseTransition duration** — 500ms for both valid and invalid feedback flashes (noted as specific detail)

## Deferred Ideas

- Visual polish (VFX-01, VFX-02, VFX-03) — Phase 4
- Level advancement (PROG-02, PROG-04, PROG-05, PROG-06) — Phase 4
- GameState GAME_OVER / WIN states — Phase 4
- Keyboard input — v2, out of scope
- Sound effects — out of scope
