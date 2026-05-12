# STATE.md

## Project Reference
See: .planning/PROJECT.md

**Core value:** Functional game loop — players can select letters, submit words, earn points, and advance through levels before time runs out.
**Current phase:** Phase 1
**Status:** Ready to execute

## Phase Status

| Phase | Name | Status |
|-------|------|--------|
| 1 | Scaffold + Pure Java Model | Ready to execute (3 plans) |
| 2 | FXML Layout + CSS | Not Started |
| 3 | Core Game Loop | Not Started |
| 4 | Level Progression + Polish | Not Started |

## Current Position

**Phase:** 1 — Scaffold + Pure Java Model
**Plan:** 3 plans across 3 waves — ready to execute
**Progress:** [----------] 0%

## Performance Metrics

| Metric | Value |
|--------|-------|
| Phases complete | 0 / 4 |
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
**Next action:** Run `/gsd-execute-phase 1` to execute Phase 1 plans
