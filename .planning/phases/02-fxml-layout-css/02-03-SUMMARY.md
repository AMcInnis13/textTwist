---
phase: 02-fxml-layout-css
plan: "03"
subsystem: controller-wiring
tags: [javafx, fxml, controller, css, integration]
dependency_graph:
  requires:
    - 02-01 (styles.css)
    - 02-02 (primary.fxml with fx:id contracts)
  provides:
    - PrimaryController with all 10 @FXML fields wired
    - App.java attaching styles.css to Scene at 700x520
  affects:
    - Phase 3 PrimaryController (adds event handlers and game model to same fields)
tech_stack:
  added:
    - javafx.collections.FXCollections / ObservableList
  patterns:
    - CSS attachment via scene.getStylesheets().add(App.class.getResource(...).toExternalForm())
    - @FXML field injection with fx:id name matching
    - ObservableList bound to ListView in initialize()
key_files:
  created: []
  modified:
    - src/main/java/cs120/lab06/PrimaryController.java
    - src/main/java/cs120/lab06/App.java
decisions:
  - "CSS attached in App.java start() via getStylesheets().add() — not FXML @import — per 02-CONTEXT.md Claude's Discretion"
  - "No null-check on getResource() — NPE on startup is the correct fast-fail if styles.css is missing"
  - "initialize() declares only foundWords binding — no game model, no timer (Phase 3 work)"
metrics:
  duration: "~5 minutes"
  completed: "2026-05-12"
  tasks_completed: 2
  tasks_total: 3
  files_created: 0
  files_modified: 2
---

# Phase 2 Plan 03: Controller Wiring + CSS Integration Summary

## One-Liner

PrimaryController gains all 10 @FXML fields and ObservableList binding; App.java attaches styles.css to the 700x520 Scene — integrating Plans 01 and 02 into a runnable styled game screen.

## What Was Built

**Task 1 — PrimaryController @FXML declarations:**

Replaced the Phase 1 stub (single empty `initialize()`) with a full field-declared controller:

| Field | Type | Matches fx:id |
|-------|------|---------------|
| `foundWordsList` | `ListView<String>` | foundWordsList |
| `letterButtonArea` | `FlowPane` | letterButtonArea |
| `guessDisplay` | `HBox` | guessDisplay |
| `scoreLabel` | `Label` | scoreLabel |
| `levelLabel` | `Label` | levelLabel |
| `timeLabel` | `Label` | timeLabel |
| `twistBtn` | `Button` | twistBtn |
| `enterBtn` | `Button` | enterBtn |
| `lastWordBtn` | `Button` | lastWordBtn |
| `clearBtn` | `Button` | clearBtn |

`initialize()` creates `foundWords = FXCollections.observableArrayList()` and calls `foundWordsList.setItems(foundWords)`.

**Task 2 — App.java CSS + resize:**

Two changes to `start()`:
1. Scene dimensions changed from `640, 480` to `700, 520`
2. Added `scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm())` after scene creation

**Checkpoint:** Human visual verification required — `mvn javafx:run` must display all 5 UI regions with dark theme.

## Commits

| Task | Description | Commit |
|------|-------------|--------|
| 1 | PrimaryController: 10 @FXML fields + initialize() binding | fda62e5 |
| 2 | App.java: attach styles.css, resize to 700x520 | 1aaf756 |

## Verification Results (pre-checkpoint)

| Check | Result |
|-------|--------|
| @FXML private ListView<String> foundWordsList present | PASS |
| foundWordsList.setItems(foundWords) in initialize() | PASS |
| No TwistController/WordDictionary imports in PrimaryController | PASS |
| No setStyle() in any .java file | PASS (0 matches) |
| getStylesheets().add( present in App.java | PASS |
| 700, 520 dimensions in App.java | PASS |
| mvn compile | PASS — BUILD SUCCESS |

## Deviations from Plan

None — plan executed exactly as written. Both tasks matched plan specifications precisely.

## Known Stubs

- `letterButtonArea` (FlowPane) is empty — Phase 3 populates dynamically
- `guessDisplay` (HBox) is empty — Phase 3 adds Labels programmatically
- All four control buttons have no event handlers yet — Phase 3 wires them

These stubs are intentional and documented in the plan. They do not prevent Plan 03's goal (styled game screen visible on launch).

## Threat Surface Scan

No new network endpoints, auth paths, or schema changes introduced. The two trust boundaries from the plan's threat model:

- T-02-06 (@FXML field name vs fx:id mismatch): Mitigated — all 10 field names verified to match fx:ids in primary.fxml. mvn compile passes.
- T-02-07 (TwistController zero-JavaFX-import rule): Mitigated — PrimaryController contains no TwistController or WordDictionary imports.

## Self-Check: PASSED (partial — awaiting human checkpoint)

- [x] `src/main/java/cs120/lab06/PrimaryController.java` — modified, all 10 @FXML fields present
- [x] `src/main/java/cs120/lab06/App.java` — modified, getStylesheets().add() and 700x520 present
- [x] Commit `fda62e5` — verified in git log
- [x] Commit `1aaf756` — verified in git log
- [x] mvn compile BUILD SUCCESS
- [ ] Human visual checkpoint — pending (mvn javafx:run 10-check verification)
