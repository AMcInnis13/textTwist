# Phase 2: FXML Layout + CSS - Context

**Gathered:** 2026-05-12
**Status:** Ready for planning

<domain>
## Phase Boundary

Replace the Phase 1 stub FXML with a complete game screen layout. All five UI regions must be present and visually styled using an external CSS file only — no game logic, no event handlers beyond stubs, no dynamic button creation. The phase ends when `mvn javafx:run` displays a styled game screen with all required regions visible, and SceneBuilder opens the FXML without errors.

Requirements in scope: GUI-01, GUI-02, GUI-03, GUI-04, GUI-05, GUI-06

</domain>

<decisions>
## Implementation Decisions

### Layout Structure
- **D-01:** Root container is `BorderPane`. `primary.fxml` is completely replaced (not amended); the Phase 1 stub VBox and its placeholder label are removed.
- **D-02:** `BorderPane` left region: `ListView<String>` found-words panel, bound to an `ObservableList<String>` declared in `PrimaryController`. fx:id = `foundWordsList`. `prefWidth` set in FXML (e.g., 180px) so the panel occupies the left column.
- **D-03:** `BorderPane` center region: a `VBox` containing four sections top-to-bottom:
  1. Score/info panel (HBox with score, level, and time Labels — fx:ids: `scoreLabel`, `levelLabel`, `timeLabel`)
  2. Letter buttons container (`FlowPane` with fx:id=`letterButtonArea` — Phase 3 populates dynamically; styled with a visible border and min height so it is visible in Phase 2)
  3. Guess display (`HBox` with fx:id=`guessDisplay` — starts empty; Phase 3 adds Labels programmatically)
  4. Control buttons (`HBox` with four Buttons in order: Twist, Enter, Last Word, Clear; fx:ids: `twistBtn`, `enterBtn`, `lastWordBtn`, `clearBtn`)
- **D-04:** Control buttons are a single horizontal `HBox` — all four side-by-side in one row.

### Visual Theme
- **D-05:** Dark game aesthetic. Background color: `#1a1a2e` (near-black navy) applied to the root `BorderPane` via `.root` in CSS. The overall feel is game-like, not form-like.
- **D-06:** Letter buttons base color: gold/amber `#f0a500`; `-fx-text-fill: #1a1a2e` (dark text for contrast). Disabled state: opacity 0.4, `-fx-background-color: #555` (grayed out). Button shape: `border-radius` 8px, bold font, 14–16px, generous padding.
- **D-07:** Found-words left panel: slightly lighter dark shade (`#16213e`) with an inset border (`-fx-border-color: #0f3460; -fx-border-insets: 2`). Distinct from center but stays in the same dark family.
- **D-08:** Timer label: large monospace font (24px+). Two CSS classes trigger on time thresholds — per CLAUDE.md: `.timer-warning` (≤20s, amber/yellow text), `.timer-critical` (≤10s, red text, optionally bold). These classes are defined in CSS even though the timer logic runs in Phase 3.
- **D-09:** All text on dark backgrounds uses light color (`#e0e0e0` or white). Score/level/time labels use the same light text.
- **D-10:** Control buttons (Twist, Enter, Last Word, Clear) use a secondary style — distinct from letter buttons. A dark background with colored border or a muted gray/blue palette (e.g., `#0f3460` with white text). Last Word button starts visually disabled (Phase 3 enables it after first successful guess; Phase 2 can show it disabled or normal — planner decides).

### Guess Display Format
- **D-11:** Guess display is an `HBox` with fx:id=`guessDisplay`, alignment CENTER, spacing 4px. It starts empty in Phase 2. Phase 3 adds `Label` children programmatically as letters are selected; Phase 3 clears children on Clear/Twist/Enter.
- **D-12:** Each dynamically-added letter Label gets CSS class `.letter-slot`: bordered tile appearance, monospace font, padding ~10px 12px, amber/gold fill (`#f0a500`) matching letter buttons but with dark text. This class is defined in CSS in Phase 2 even though labels are only added in Phase 3.
- **D-13:** `HBox` minimum height is set in FXML or CSS so the guess display area is visible even when empty (no letters selected yet).

### Claude's Discretion
- **Letter area Phase 2 appearance:** `FlowPane` with fx:id=`letterButtonArea` gets a styled border and `minHeight` in CSS so it appears as a visible region even with no children. This satisfies the Phase 2 success criterion ("letter buttons area visible") without adding placeholder buttons.
- **Window size:** `App.java` sets Scene dimensions to approximately 700×520px. This is large enough for all regions without scrolling at standard resolution.
- **CSS file location:** `src/main/resources/cs120/lab06/styles.css`. Loaded via `scene.getStylesheets().add(...)` in `App.java` (or `@import` in FXML header — planner decides which is cleaner).
- **fx:id naming:** All fx:id names listed in D-03 above are locked for Phase 3 wiring. Changing them after Phase 2 would require updating PrimaryController field names.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Specification
- `textTwist.pdf` — Lab06 spec; the UI appendix or screenshots define the expected screen layout and regions. Agents must check this for any layout specifics not captured here.
- `CLAUDE.md` — Critical implementation rules: all styling via external CSS (no `setStyle()`); CSS class accumulation pitfall on timer label (remove old class before adding new); fx:id architecture overview; `timer-warning` and `timer-critical` class names are referenced in CLAUDE.md

### Planning Artifacts
- `.planning/REQUIREMENTS.md` — Phase 2 requirements: GUI-01 through GUI-06 (full text); also TIMER-03 and TIMER-04 define the CSS class names for timer states (must be defined in CSS now)
- `.planning/research/FEATURES.md` — UX patterns for JavaFX word game: HBox-of-Labels guess display rationale, ListView scrolling, CSS tile styling, timer color escalation pattern (§§1–8)
- `.planning/research/ARCHITECTURE.md` — Component boundaries; `styles.css` role; `PrimaryController` responsibility list; data flow from App startup through FXML controller

### Existing Source Files (must be modified)
- `src/main/resources/cs120/lab06/primary.fxml` — Current Phase 1 stub; completely replaced in this phase
- `src/main/java/cs120/lab06/PrimaryController.java` — Current stub; gets all `@FXML` field declarations and an `initialize()` stub that wires `foundWordsList` to `FXCollections.observableArrayList()`
- `src/main/java/cs120/lab06/App.java` — May need CSS stylesheet registration; check whether FXML controller or App.java is the better place to attach the stylesheet

### New File to Create
- `src/main/resources/cs120/lab06/styles.css` — Does not exist yet; must be created. All visual rules live here.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `TwistController.java` and `WordDictionary.java` — Complete, no changes in Phase 2
- `App.java` — Already loads `primary.fxml` via `FXMLLoader` and creates the `Scene`; just needs CSS stylesheet URL attached

### Established Patterns
- No CSS file exists yet — Phase 2 establishes the stylesheet from scratch
- `primary.fxml` is a minimal stub (VBox + placeholder Label) — completely replaced
- `PrimaryController.java` is a minimal stub (empty `initialize()`) — gains `@FXML` field injections

### Integration Points
- `ObservableList<String> foundWords` declared in `PrimaryController`, passed to `foundWordsList.setItems(foundWords)` in `initialize()` — this is the GUI-03 binding
- All `@FXML` fx:id names defined in D-03 must match the field names in `PrimaryController`; Phase 3 wires event handlers to the same fields
- CSS class `.timer-warning` and `.timer-critical` are referenced by Phase 3's `Timeline` logic — define them in `styles.css` now

</code_context>

<specifics>
## Specific Ideas

- Dark navy background `#1a1a2e`, letter buttons gold `#f0a500` — specific hex values locked by user choice
- Single horizontal row for control buttons (Twist → Enter → Last Word → Clear, left to right)
- Found-words list on the LEFT (user specifically chose left over right sidebar)
- Guess display grows dynamically — no pre-seeded placeholder slots
- `.letter-slot` is the locked CSS class name for guess-display letter tiles

</specifics>

<deferred>
## Deferred Ideas

- **Keyboard input** — Out of scope per PROJECT.md; not discussed
- **Sound effects** — Out of scope per PROJECT.md; not discussed
- **Word-length coloring in ListView** — v2 requirement (REQUIREMENTS.md §v2); deferred
- **Minimum score progress bar** — Nice-to-have per FEATURES.md; not in Phase 2 or 3 scope; Phase 4 candidate

</deferred>

---

*Phase: 2-FXML Layout + CSS*
*Context gathered: 2026-05-12*
