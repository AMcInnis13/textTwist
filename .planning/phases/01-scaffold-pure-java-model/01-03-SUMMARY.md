---
phase: 1
plan: "03"
subsystem: model
tags: [game-model, junit, pure-java, scoring, duplicate-detection, shuffle]
dependency_graph:
  requires: [01-02]
  provides: [TwistController.java, TwistControllerTest.java]
  affects: [Phase 3 PrimaryController wires to TwistController]
tech_stack:
  added: []
  patterns: [HashSet for O(1) duplicate detection, Collections.shuffle with Random, TDD with getTargetWord() to avoid hardcoding random words]
key_files:
  created:
    - src/main/java/cs120/lab06/TwistController.java
    - src/test/java/cs120/lab06/TwistControllerTest.java
  modified: []
decisions:
  - "checkGuessWord does NOT verify formability (letter subset check) per D-10 — Phase 3 concern"
  - "Duplicate tracking via HashSet<String> guessedWords, reset per episode per D-11"
  - "shuffleLetters() uses Collections.shuffle(list, new Random()) per D-12"
  - "Zero JavaFX imports — pure Java only per D-13"
  - "Tests use getTargetWord() rather than hardcoding a word — randomWord() makes hardcoding fragile"
  - "score persists across episodes; levelScore and guessedWords reset in beginEpisode()"
metrics:
  duration: "~5 minutes"
  completed: "2026-05-12"
  tasks_completed: 2
  files_created: 2
---

# Phase 1 Plan 03: TwistController Summary

**One-liner:** Pure-Java TwistController game model with n*n*10 scoring, HashSet duplicate rejection, and Collections.shuffle letter shuffler, verified by 5 JUnit 4.12 tests.

## What Was Built

`TwistController.java` — pure-Java game state engine with zero JavaFX imports:
- Constructor `TwistController(String folderPath)` instantiates `WordDictionary`, sets `level=1, score=0, levelScore=0`
- `beginEpisode(int letterCount)` calls `myDictionary.initialize(letterCount)`, sets `targetWord = myDictionary.randomWord(letterCount)`, resets `levelScore=0`, clears `guessedWords` (score persists)
- `checkGuessWord(String wrd)` normalizes to uppercase, rejects null/empty/duplicate/invalid with 0, otherwise scores `n*n*10`, updates `score` and `levelScore`, adds word to `guessedWords`
- `shuffleLetters()` converts `targetWord` chars to `List<String>`, shuffles with `Collections.shuffle(list, new Random())`, returns list
- Getters: `getScore()`, `getLevelScore()`, `getLevel()`, `getTargetWord()`

`TwistControllerTest.java` — 5 JUnit 4.12 tests:
1. `testCheckGuessWord_validWord` — `checkGuessWord(getTargetWord())` returns `n*n*10`
2. `testCheckGuessWord_invalidWord` — ZZZZZZZZZ, empty string, and null all return 0
3. `testCheckGuessWord_duplicateRejected` — second call for same word returns 0, score frozen
4. `testShuffleLetters_sameLetters` — sorted shuffled list equals sorted targetWord char list (multiset)
5. `testScoreAccumulation` — initial score/levelScore are 0; after valid guess both equal points earned

## mvn test Output

```
[INFO] Running cs120.lab06.WordDictionaryTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.095 s
[INFO] Running cs120.lab06.TwistControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.005 s
[INFO]
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

## Verification

| Check | Result |
|-------|--------|
| TwistController.java exists | CONFIRMED |
| Zero javafx imports (`grep -c "import javafx"` = 0) | CONFIRMED |
| checkGuessWord(targetWord) returns n*n*10 | CONFIRMED |
| checkGuessWord(targetWord) second call returns 0 | CONFIRMED |
| checkGuessWord("ZZZZZZZZZ") returns 0 | CONFIRMED |
| shuffleLetters() same length and same multiset as targetWord | CONFIRMED |
| mvn test — 10 tests (5 + 5), 0 failures, 0 errors | CONFIRMED |
| BUILD SUCCESS | CONFIRMED |

## Phase 1 ROADMAP Success Criteria — Final Status

| Criterion | Status |
|-----------|--------|
| 1. Maven project builds and `mvn javafx:run` launches without errors | CONFIRMED (Plan 01) |
| 2. WordDictionary loads correct file and isValidWord works | CONFIRMED (Plan 02) |
| 3. TwistController.checkGuessWord scores n*n*10 and rejects duplicates | CONFIRMED (Plan 03) |
| 4. `mvn test` passes all JUnit 4 tests (10 total, 0 failures) | CONFIRMED (Plan 03) |

## Commit History

| Task | Commit | Description |
|------|--------|-------------|
| T01 | eb79547 | feat(01-03): implement TwistController pure-Java game model |
| T02 | 15fe2fe | feat(01-03): add TwistControllerTest — 5 JUnit 4.12 tests, all passing |

## Deviations from Plan

None — plan executed exactly as written.

## Known Stubs

None.

## Threat Flags

No new security surface introduced. TwistController accepts arbitrary String input in `checkGuessWord()` and mitigates T-01-05 (Tampering) as planned: null/empty guard, uppercase normalization, and HashSet duplicate rejection all implemented.

## Self-Check: PASSED

- TwistController.java: FOUND at src/main/java/cs120/lab06/TwistController.java
- TwistControllerTest.java: FOUND at src/test/java/cs120/lab06/TwistControllerTest.java
- Commit eb79547: FOUND in git log
- Commit 15fe2fe: FOUND in git log
- Zero javafx imports: CONFIRMED (grep -c returns 0)
- mvn test 10/10 passing: CONFIRMED
