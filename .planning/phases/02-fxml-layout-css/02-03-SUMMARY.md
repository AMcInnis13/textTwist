---
phase: 02-fxml-layout-css
plan: "03"
subsystem: ui
tags: [javafx, fxml, css, controller, observablelist, scene]
dependency_graph:
  requires:
    - phase: 02-01
      provides: styles.css at src/main/resources/cs120/lab06/styles.css
    - phase: 02-02
      provides: primary.fxml with all 10 fx:ids locked
  provides:
    - PrimaryController with all 10 @FXML fields declared and ObservableList binding in initialize()
    - App.java with CSS attached and scene sized to 700x520
    - Running game screen with full dark theme — confirmed by human visual verification
  affects:
    - Phase 3 PrimaryController (adds event handlers, Timeline, game model wiring on top of existing @FXML fields)
    - Phase 3 App.java (no changes expected — CSS attachment and scene size are done)
tech_stack:
  added:
    - javafx.collections.FXCollections / ObservableList<String>
  patterns:
    - CSS attached in App.java start() via scene.getStylesheets().add() — not FXML @import
    - No null-check on App.class.getResource("styles.css") — NPE is correct fast-fail if file is missing
    - @FXML initialize() method used for ObservableList wiring only — no game model, no timer
key_files:
  created: []
  modified:
    - src/main/java/cs120/lab06/PrimaryController.java
    - src/main/java/cs120/lab06/App.java
decisions:
  - "CSS attached via scene.getStylesheets().add(App.class.getResource(\"styles.css\").toExternalForm()) in App.java start() — chosen over FXML @import for explicit control and consistent classpath resolution"
  - "No null-check on getResource() — NullPointerException on startup is the intended fast-fail if styles.css is missing"
  - "initialize() does only foundWordsList.setItems(foundWords) — no game model wiring; all Phase 3 wiring will extend this method"
patterns-established:
  - "PrimaryController initialize(): only binds ObservableList; all game logic added in Phase 3"
  - "App.start(): scene creation then immediate stylesheet attachment in two sequential lines"
requirements-completed:
  - GUI-02
  - GUI-03
metrics:
  duration: "~20 min (split across two sessions with human-verify checkpoint)"
  completed: "2026-05-12"
  tasks_completed: 2
  tasks_total: 2
  files_created: 0
  files_modified: 2
---

# Phase 2 Plan 03: Controller Wiring + CSS Integration Summary

**PrimaryController wired with all 10 @FXML fields and ObservableList binding; App.java attaches styles.css and sizes scene to 700x520; human-verified dark game screen renders at runtime**

## Performance

- **Duration:** ~20 min (split across two sessions with human-verify checkpoint)
- **Completed:** 2026-05-12
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments

- PrimaryController.java: all 10 @FXML fields declared matching primary.fxml fx:ids exactly; initialize() binds foundWordsList to an ObservableList<String>
- App.java: scene resized from 640x480 to 700x520; styles.css attached via getStylesheets().add() immediately after Scene constructor
- Human visual verification checkpoint passed (all 10 checks): dark navy background, five visible regions, gold buttons, Last Word disabled at startup, no console exceptions

## Task Commits

1. **Task 1: Add @FXML declarations and ObservableList binding** - `fda62e5` (feat)
2. **Task 2: Attach CSS stylesheet and resize Scene in App.java** - `1aaf756` (feat)

## Files Created/Modified

- `src/main/java/cs120/lab06/PrimaryController.java` — Added 10 @FXML field declarations, ObservableList<String> foundWords field, and initialize() binding foundWordsList.setItems(foundWords)
- `src/main/java/cs120/lab06/App.java` — Changed Scene dimensions from 640x480 to 700x520; added scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm()) after scene creation

## Post-Plan Verification Results

All 8 automated checks passed:

| Check | Command | Result |
|-------|---------|--------|
| Compile | mvn compile | Zero errors |
| CSS attachment | grep "getStylesheets().add(" App.java | 1 match |
| Scene size | grep "700, 520" App.java | 1 match |
| @FXML count | grep "@FXML" PrimaryController.java (wc -l) | 11 (10 fields + initialize annotation) |
| ObservableList binding | grep "foundWordsList.setItems" PrimaryController.java | 1 match |
| No setStyle() | grep -r "setStyle" src/main/java/ | 0 results |
| No TwistController import | grep "TwistController" PrimaryController.java | 0 results |
| Human visual verify | mvn javafx:run + 10-point checklist | Approved |

## Decisions Made

- CSS attached in App.java via `getStylesheets().add()` — not FXML `@import` — for explicit control and no ambiguity about load order
- No null-check on `App.class.getResource("styles.css")` — NullPointerException on startup is the correct fast-fail (absent file is immediately obvious rather than silently running unstyled)
- `initialize()` restricted to ObservableList wiring only — no game model, no timer, no label text updates (FXML sets initial text values; Phase 3 adds all game logic)

## Deviations from Plan

None — plan executed exactly as written.

## Known Stubs

- `letterButtonArea` (FlowPane) is empty — Phase 3 populates letter buttons dynamically
- `guessDisplay` (HBox) is empty — Phase 3 adds Labels programmatically as letters are selected
- All four control buttons have no event handlers yet — Phase 3 wires them

These stubs are intentional and documented in the plan. They do not prevent Plan 03's goal (styled game screen visible on launch). Phase 3 resolves all three stubs.

## Threat Flags

No new security-relevant surface introduced. Files modified are pure controller wiring and scene configuration.

Threat mitigations from plan's threat model applied:
- T-02-06 (@FXML field name vs fx:id mismatch): All 10 field names match fx:ids exactly; mvn compile verified no type mismatches; human visual checkpoint confirmed no null-panel failures at runtime
- T-02-07 (TwistController zero-JavaFX-import rule): grep confirms zero TwistController or WordDictionary references in PrimaryController.java

## Issues Encountered

None.

## Next Phase Readiness

Phase 2 is complete. Phase 3 (Core Game Loop) can begin immediately:
- PrimaryController has all 10 @FXML fields ready for event handler wiring
- foundWords ObservableList is bound and ready to receive words
- CSS classes (.letter-btn, .timer-warning, .timer-critical, .letter-slot, .guess-correct, .guess-invalid) are defined and waiting for Phase 3 to apply them
- Timeline timer can be added to initialize() without structural changes

No blockers for Phase 3.

---
*Phase: 02-fxml-layout-css*
*Completed: 2026-05-12*

## Self-Check: PASSED

- [x] `src/main/java/cs120/lab06/PrimaryController.java` exists with 10 @FXML fields
- [x] `src/main/java/cs120/lab06/App.java` contains getStylesheets().add( and 700, 520
- [x] Commit `fda62e5` exists in git log (Task 1)
- [x] Commit `1aaf756` exists in git log (Task 2)
- [x] mvn compile exits with zero errors
- [x] grep -r "setStyle" src/main/java/ returns 0 results
- [x] grep "TwistController" PrimaryController.java returns 0 results
- [x] Human checkpoint approved (all 10 visual checks passed)
