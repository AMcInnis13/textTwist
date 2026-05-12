---
phase: 02-fxml-layout-css
plan: "02"
subsystem: fxml-layout
tags: [fxml, layout, javafx, ui]
dependency_graph:
  requires: []
  provides:
    - primary.fxml BorderPane layout with all 10 fx:id contracts locked
  affects:
    - PrimaryController (Plan 03 @FXML injection depends on these fx:ids)
    - styles.css (Plan 01 CSS classes referenced by styleClass attributes)
tech_stack:
  added: []
  patterns:
    - BorderPane as root container for game screen
    - fx:id contract locking pattern (Phase 2 locks names, Phase 3 wires handlers)
key_files:
  created: []
  modified:
    - src/main/resources/cs120/lab06/primary.fxml
decisions:
  - "BorderPane root with left=found-words panel, center=4-section VBox per CONTEXT.md D-01/D-03"
  - "lastWordBtn starts disable=true in FXML per CONTEXT.md D-10 and PITFALLS.md §8"
  - "No stylesheet reference in FXML — App.java attaches it in Plan 03"
  - "guessDisplay and letterButtonArea are empty in FXML — Phase 3 populates dynamically"
metrics:
  duration: "67 seconds"
  completed: "2026-05-12"
  tasks_completed: 1
  tasks_total: 1
  files_changed: 1
---

# Phase 2 Plan 02: FXML Layout — Full BorderPane Game Screen

## One-liner

Complete BorderPane FXML replacing Phase 1 VBox stub, with all 10 fx:id contracts locked for PrimaryController injection.

## What Was Built

The Phase 1 placeholder (`VBox` + single Label) in `primary.fxml` was completely replaced with a full game screen layout.

**Structure:**
- Root: `BorderPane` with `fx:controller="cs120.lab06.PrimaryController"`
- Left region: `VBox` containing a "FOUND WORDS" header `Label` and `ListView` (`fx:id=foundWordsList`, `prefWidth=180`)
- Center region: `VBox` with 4 children in vertical order:
  1. Info `HBox`: `scoreLabel` ("Score: 0"), `levelLabel` ("Level: 1"), `timeLabel` ("2:00") — both `info-label` and `time-label` styleClasses on timeLabel
  2. `FlowPane` (`fx:id=letterButtonArea`, `minHeight=70`, `alignment=CENTER`) — empty; Phase 3 populates dynamically
  3. `HBox` (`fx:id=guessDisplay`, `minHeight=50`, `alignment=CENTER`) — empty; Phase 3 adds Letter Labels programmatically
  4. Control `HBox`: Twist, Enter, Last Word (`disable=true`), Clear buttons left-to-right

**CSS classes applied in FXML** (defined in styles.css by Plan 01):
`.found-words-panel`, `.found-words-list`, `.section-header`, `.center-vbox`, `.info-hbox`, `.info-label`, `.time-label`, `.letter-area`, `.guess-display`, `.control-hbox`, `.control-btn`

## Tasks Completed

| Task | Name | Commit | Files |
|------|------|--------|-------|
| 1 | Replace primary.fxml with full BorderPane game screen layout | 7c7c0e4 | src/main/resources/cs120/lab06/primary.fxml |

## Verification Results

| Check | Result |
|-------|--------|
| Root is BorderPane (not VBox) | PASS — 7 occurrences of BorderPane |
| All 10 fx:ids present | PASS — grep -Ec returns 10 |
| No onAction attributes | PASS — 0 matches |
| No inline style="" attributes | PASS — 0 matches |
| letterButtonArea FlowPane has no children | PASS — self-closing element |
| guessDisplay HBox has no children | PASS — self-closing element |
| lastWordBtn has disable="true" | PASS |
| mvn compile | PASS — BUILD SUCCESS |

## Deviations from Plan

None — plan executed exactly as written.

## Known Stubs

None — this plan establishes structural FXML only. No data, no event handlers, no dynamic content expected in Phase 2.

## Threat Surface Scan

No new network endpoints, auth paths, file access patterns, or schema changes introduced. The only trust boundary (FXML fx:id → @FXML field injection) is mitigated: all 10 fx:ids verified present per T-02-03.

## Self-Check: PASSED

- [x] `src/main/resources/cs120/lab06/primary.fxml` — modified, present in worktree
- [x] Commit `7c7c0e4` — verified via `git rev-parse --short HEAD`
- [x] All 10 fx:id attributes verified by grep
- [x] mvn compile BUILD SUCCESS
