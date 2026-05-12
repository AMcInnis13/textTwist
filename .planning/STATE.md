# STATE.md

## Project Reference
See: .planning/PROJECT.md

**Core value:** Functional game loop — players can select letters, submit words, earn points, and advance through levels before time runs out.
**Current phase:** Phase 2
**Status:** Phase 2 complete — all 3 plans done (2026-05-12)

## Phase Status

| Phase | Name | Status |
|-------|------|--------|
| 1 | Scaffold + Pure Java Model | Complete (2026-05-12) |
| 2 | FXML Layout + CSS | Complete (2026-05-12) |
| 3 | Core Game Loop | Not Started |
| 4 | Level Progression + Polish | Not Started |

## Current Position

**Phase:** 2 — FXML Layout + CSS (complete)
**Plan:** 03 — Controller Wiring + CSS Integration (complete)
**Progress:** [####################] Phase 2/4 done; Phase 3 next

## Performance Metrics

| Metric | Value |
|--------|-------|
| Phases complete | 2 / 4 |
| Requirements mapped | 34 / 34 |
| Plans complete | 0 / ? |

## Accumulated Context

### Key Decisions Locked In
- Delete module-info.java immediately after archetype generation (JUnit 4 / unnamed module incompatibility)
- Pin Surefire to 3.1.2 (2.x silently skips tests on JDK 17+)
- Use `new File("twister_words/" + n + ".txt")` — NOT getResourceAsStream (files are at project root, not classpath)
- Use `javafx.animation.Timeline` for countdown — NOT java.util.Timer (FX thread requirement)
- Track letter button state by array index, NOT by character (duplicate letter correctness)
- TwistController must have zero JavaFX imports (JUnit testability)
- Single endEpisode() entry point for both timer expiry and target-word-guessed paths
- CSS attached in App.java start() via getStylesheets().add() — not FXML @import (Phase 2, Plan 03)
- No null-check on App.class.getResource("styles.css") — NPE is correct fast-fail if file is missing

### Pitfalls to Watch
- Surefire 2.x: `mvn test` reports 0 tests run silently — pin to 3.1.2
- Dictionary FileNotFoundException if `mvn javafx:run` is not run from project root
- CSS properties without `-fx-` prefix are silently ignored by JavaFX
- CSS class accumulation on timer label — remove old class before adding new
- Last Word button NPE — initialize as disabled; enable only after first successful word per episode

### Todos
- [ ] Run `mvn javafx:run` gate check after Phase 1 scaffold
- [ ] Confirm win condition exact behavior with spec PDF (immediate vs. threshold at Level 8)
- [ ] Confirm `isFormableFrom` check requirement (guess letters must be a subset of target word letters)

### Blockers
None

## Session Continuity

**Last updated:** 2026-05-12
**Stopped at:** Phase 2 complete — all 3 plans done
**Next action:** Begin Phase 3 (Core Game Loop) — PrimaryController event handlers, Timeline timer, letter button management
