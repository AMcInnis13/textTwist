---
phase: 1
plan: "01"
subsystem: scaffold
tags: [maven, javafx, surefire, scaffold]
dependency_graph:
  requires: []
  provides: [pom.xml, App.java, primary.fxml, PrimaryController.java]
  affects: [all subsequent plans in Phase 1]
tech_stack:
  added: [JavaFX 21, JUnit 4.12, Surefire 3.1.2, javafx-maven-plugin 0.0.8]
  patterns: [Maven unnamed-module project, FXML MVC stub]
key_files:
  created:
    - pom.xml
    - src/main/java/cs120/lab06/App.java
    - src/main/java/cs120/lab06/PrimaryController.java
    - src/main/resources/cs120/lab06/primary.fxml
  modified: []
decisions:
  - "Manually created project structure instead of moving archetype output — archetype generated package cs120 instead of cs120.lab06; manual creation was cleaner"
  - "Used Surefire 3.1.2 per D-03 / CLAUDE.md (overrides STACK.md recommendation of 2.22.2)"
  - "mainClass=cs120.lab06.App with no module prefix — unnamed module after deleting module-info.java"
  - "javafx-maven-plugin 0.0.8 for correct --add-modules injection on JDK 21+"
metrics:
  duration: "~10 minutes"
  completed: "2026-05-12"
  tasks_completed: 2
  files_created: 4
---

# Phase 1 Plan 01: Scaffold Summary

**One-liner:** Maven project bootstrapped with JavaFX 21, JUnit 4.12, Surefire 3.1.2, unnamed-module path (no module-info.java), blank-window stub ready for Phase 2 game screen.

## What Was Built

A compilable Maven project structure for TextTwist (CS120 Lab06) with:

- `pom.xml` — groupId=cs120, artifactId=lab06, JavaFX 21 dependencies, Surefire 3.1.2, javafx-maven-plugin 0.0.8
- `App.java` — JavaFX Application entry point in package cs120.lab06; loads primary.fxml
- `PrimaryController.java` — Minimal FXML controller stub (Phase 1 only)
- `primary.fxml` — VBox with a label; blank window stub for Phase 1

## Verified Artifacts

| Artifact | Status |
|----------|--------|
| `pom.xml` groupId=cs120, artifactId=lab06 | CONFIRMED |
| javafx-controls 21 dependency | CONFIRMED |
| javafx-fxml 21 dependency | CONFIRMED |
| junit 4.12 (scope=test) dependency | CONFIRMED |
| maven-surefire-plugin 3.1.2 | CONFIRMED |
| mainClass=cs120.lab06.App (no module prefix) | CONFIRMED |
| No module-info.java under src/ | CONFIRMED |
| src/main/java/cs120/lab06/App.java | CONFIRMED |
| src/main/resources/cs120/lab06/primary.fxml | CONFIRMED |
| twister_words/ at project root (3.txt–10.txt) | CONFIRMED |
| mvn compile — BUILD SUCCESS | CONFIRMED |
| mvn test — BUILD SUCCESS (0 tests, Surefire 3.1.2) | CONFIRMED |

## Commit History

| Task | Commit | Description |
|------|--------|-------------|
| T01 | 2b6a811 | feat(01-01): scaffold Maven project with JavaFX 21 and Surefire 3.1.2 |
| T02 | (no code changes) | Verification task — all programmatic checks passed |

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Archetype generated wrong package structure**
- **Found during:** Task 1
- **Issue:** `mvn archetype:generate` with `javafx-archetype-fxml` 0.0.6 generated files into `lab06/` subdirectory with package `cs120` (not `cs120.lab06`). Files were: `cs120/App.java`, `cs120/PrimaryController.java`, `cs120/primary.fxml`, `cs120/secondary.fxml`, `module-info.java`. The pom.xml also had compiler source/target set to Java 11.
- **Fix:** Deleted the generated `lab06/` subdirectory and manually created the correct Maven project structure with package `cs120.lab06`, Java 21 compiler settings, and all required pom.xml configurations.
- **Files modified:** pom.xml, src/main/java/cs120/lab06/App.java, src/main/java/cs120/lab06/PrimaryController.java, src/main/resources/cs120/lab06/primary.fxml
- **Commit:** 2b6a811

## mvn test Output Confirming Surefire 3.1.2

```
[INFO] --- surefire:3.1.2:test (default-test) @ lab06 ---
[INFO] No tests to run.
[INFO] BUILD SUCCESS
```

## Note on mvn javafx:run

Task T02 requires a GUI window to verify. This was not run in the automated environment (no display). All programmatically verifiable criteria pass:
- App.java correctly calls `loadFXML("primary")` which resolves to `primary.fxml` via `App.class.getResource(...)`
- `primary.fxml` has a valid `VBox` root element that FXMLLoader can load
- `PrimaryController.java` is in package `cs120.lab06` matching the `fx:controller` attribute in primary.fxml
- `mvn compile` produces BUILD SUCCESS with no errors

The human gate for `mvn javafx:run` opening a blank window without errors is the next manual verification step.

## Self-Check: PASSED

- pom.xml: FOUND at /Users/andrewmcinnis/Desktop/TextTwist/pom.xml
- App.java: FOUND at src/main/java/cs120/lab06/App.java
- primary.fxml: FOUND at src/main/resources/cs120/lab06/primary.fxml
- Commit 2b6a811: FOUND in git log
- No module-info.java: CONFIRMED (find src/ -name module-info.java returns nothing)
- Surefire 3.1.2 in pom.xml: CONFIRMED
- mainClass cs120.lab06.App: CONFIRMED
