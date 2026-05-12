# Phase 2: FXML Layout + CSS - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-12
**Phase:** 2-FXML Layout + CSS
**Areas discussed:** Layout Structure, Visual Theme, Guess Display Format

---

## Layout Structure

### Root Container

| Option | Description | Selected |
|--------|-------------|----------|
| BorderPane (Recommended) | Left=found-words, center=game area, bottom=controls. Natural for a sidebar layout. | ✓ |
| VBox (vertical stack) | All regions stacked top-to-bottom. Simpler FXML but list takes vertical space. | |
| GridPane | Precise row/column control, most verbose FXML. | |

**User's choice:** BorderPane

---

### Found-Words List Position

| Option | Description | Selected |
|--------|-------------|----------|
| Right panel (Recommended) | Classic word-game sidebar on the right. | |
| Left panel | Mirrored — list on left, game area on right. | ✓ |

**User's choice:** Left panel

---

### Center Region Organization

| Option | Description | Selected |
|--------|-------------|----------|
| VBox: score panel → letter area → guess display → control buttons (Recommended) | Top-to-bottom flow inside the center region. | ✓ |
| VBox: letter area → guess display → buttons (score in BorderPane top bar) | Score lives in a full-width top bar. | |

**User's choice:** VBox with score panel → letter area → guess display → control buttons

---

### Control Button Layout

| Option | Description | Selected |
|--------|-------------|----------|
| Single horizontal row (HBox) (Recommended) | All 4 buttons side-by-side. Compact, game-like. | ✓ |
| Two rows of 2 (GridPane) | More space per button. | |
| You decide | Claude picks arrangement. | |

**User's choice:** Single horizontal HBox

---

## Visual Theme

### Overall Mood

| Option | Description | Selected |
|--------|-------------|----------|
| Dark game aesthetic (Recommended) | Dark bg, bright colorful letter buttons, high contrast. | ✓ |
| Light/clean | White/off-white, blue or green accents. | |
| You decide | Claude picks a theme. | |

**User's choice:** Dark game aesthetic

---

### Letter Button Color

| Option | Description | Selected |
|--------|-------------|----------|
| Gold/amber (#f0a500) (Recommended) | Classic TextTwist look, warm and game-like. | ✓ |
| Blue (#4a90d9) | Cooler, more modern. | |
| You decide | Claude picks a color. | |

**User's choice:** Gold/amber (#f0a500)

---

### Found-Words Panel Treatment

| Option | Description | Selected |
|--------|-------------|----------|
| Slightly lighter dark panel with inset border (Recommended) | Same dark family, distinct region. | ✓ |
| Contrasting accent panel | Noticeably different color to make sidebar pop. | |
| You decide | Claude decides. | |

**User's choice:** Slightly lighter dark with inset border

---

## Guess Display Format

### Display Structure

| Option | Description | Selected |
|--------|-------------|----------|
| HBox of individual letter Labels (Recommended) | Up to 10 Labels, each with .letter-slot class. Per-letter CSS targeting. | ✓ |
| Single Label (full word) | One Label whose text grows. Simpler but no per-letter styling. | |

**User's choice:** HBox of individual letter Labels

---

### Slot Strategy

| Option | Description | Selected |
|--------|-------------|----------|
| Grow dynamically (Recommended) | HBox starts empty; Labels added programmatically in Phase 3. | ✓ |
| Fixed empty slots (10 pre-created) | All 10 slots pre-created with blank text and visible tile style. | |

**User's choice:** Grow dynamically

---

### CSS Class for Letter Tiles

| Option | Description | Selected |
|--------|-------------|----------|
| .letter-slot with bordered tile look (Recommended) | Visible border, padding, monospace, amber/gold fill. | ✓ |
| .guess-letter with minimal styling | Just font and color, no border box. | |
| You decide | Claude picks class name and style. | |

**User's choice:** .letter-slot with bordered tile look

---

## Claude's Discretion

- **Letter area Phase 2 placeholder:** Empty `FlowPane` with fx:id=`letterButtonArea`, styled with border and `minHeight` so region is visible without placeholder buttons
- **Window size:** ~700×520px set in App.java Scene constructor
- **CSS file loading mechanism:** App.java vs FXML import — planner to decide
- **Control button styling:** Secondary palette (dark blue/gray) to distinguish from letter buttons

## Deferred Ideas

- Keyboard input support — v2, out of scope
- Sound effects — v2, out of scope
- Word-length coloring in ListView — v2 requirement, future phase
- Score threshold progress bar — nice-to-have, Phase 4 candidate
