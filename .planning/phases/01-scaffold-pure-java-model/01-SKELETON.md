# Walking Skeleton — TextTwist (CS120 Lab06)

**Phase:** 1
**Generated:** 2026-05-12

## Capability Proven End-to-End

A blank JavaFX 21 window opens via `mvn javafx:run` from the project root, and `mvn test` discovers and passes all WordDictionary JUnit 4 tests that read from `twister_words/` — confirming the full stack from Maven build to file I/O to unit test runner.

## Architectural Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Build tool | Maven 3.9.x (openjfx javafx-archetype-fxml 0.0.6) | Spec-mandated |
| UI framework | JavaFX 21 | Spec-mandated |
| Testing | JUnit 4.12 + Surefire 3.1.2 | Spec-mandated; 3.1.2 required for JDK 21 (2.x silently skips tests) |
| File I/O | java.io.File + Scanner | twister_words/ at project root, not classpath — getResourceAsStream returns null |
| Module system | Unnamed module (delete module-info.java) | JUnit 4.12 not module-aware; unnamed module path is zero-friction for a lab |
| Model-view separation | WordDictionary + TwistController = zero JavaFX imports | JUnit testability without an FX runtime |
| Timer (future) | javafx.animation.Timeline only | FX thread safety — java.util.Timer calls label.setText from wrong thread |
| Letter button state (future) | boolean[] indexed by position, not character value | Duplicate letters require separate button objects |
| End-of-episode routing (future) | Single endEpisode() entry point | Both timer expiry and target-word-guessed must route through it |
| CSS (future) | External CSS file only, no inline setStyle() | Spec grades "impeccable style" via external CSS |
| mainClass in pom.xml | cs120.lab06.App (no module prefix) | No module-info.java = unnamed module; module prefix causes launch failure |

## Stack Touched in Phase 1

- [x] Project scaffold (Maven archetype, pom.xml, module-info.java deleted)
- [x] App startup — blank JavaFX window via App.java + primary.fxml stub
- [x] File I/O — WordDictionary reads twister_words/{n}.txt via new File(...)
- [x] Unit tests — WordDictionary + TwistController tests via JUnit 4.12 / Surefire 3.1.2
- [x] Dev run — `mvn javafx:run` from project root (no errors)

## Out of Scope (Deferred to Later Slices)

- FXML game screen layout (Phase 2)
- All game controls and event handlers (Phase 3+)
- Timer, scoring display, CSS (Phase 2+)
- Level progression and game-over flows (Phase 4)
- isFormableFrom check in checkGuessWord (deferred per D-10; UI enforces formability via letter button disable)

## Subsequent Slice Plan

- Phase 2: Game screen renders — twist.fxml layout + styles.css, all regions visible, no logic
- Phase 3: Core game loop — letter selection, word submission, timer, scoring
- Phase 4: Level progression (all 8 levels), end-of-episode evaluation, visual feedback polish
