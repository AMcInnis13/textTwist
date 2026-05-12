---
phase: 1
plan: "02"
subsystem: model
tags: [dictionary, junit, pure-java, surefire]
dependency_graph:
  requires: [01-01]
  provides: [WordDictionary.java, WordDictionaryTest.java]
  affects: [01-03 (TwistController depends on WordDictionary)]
tech_stack:
  added: []
  patterns: [HashSet for O(1) word lookup, new File() for project-root-relative file access, JUnit 4 @Before existence assertion]
key_files:
  created:
    - src/main/java/cs120/lab06/WordDictionary.java
    - src/test/java/cs120/lab06/WordDictionaryTest.java
  modified: []
decisions:
  - "Used new File(folderPath + '/' + letterCount + '.txt') per D-05 and CLAUDE.md rule — never getResourceAsStream"
  - "HashSet<String> for O(1) isValidWord lookup per D-07"
  - "initialize() clears existing set before loading to support re-initialization at different letter counts"
  - "RuntimeException wraps IOException from Scanner — surfaces missing-file errors clearly"
  - "randomWord() converts HashSet to ArrayList then uses Random.nextInt(size) per D-08"
  - "Test @Before asserts file exists with absolute path in failure message per D-16 and Pitfall 14"
metrics:
  duration: "~5 minutes"
  completed: "2026-05-12"
  tasks_completed: 2
  files_created: 2
---

# Phase 1 Plan 02: WordDictionary Summary

**One-liner:** Pure-Java WordDictionary loader using HashSet + new File() with 5 passing JUnit 4.12 tests confirming CAT lookup, randomWord length, and distribution.

## What Was Built

`WordDictionary.java` — a pure-Java dictionary loader with zero JavaFX imports:
- Constructor `WordDictionary(String folderPath)` stores the path
- `initialize(int letterCount)` opens `folderPath/letterCount.txt` via `new File()`, reads the integer word-count on line 1 (discards it), then populates a `HashSet<String>` with trimmed uppercase words
- `isValidWord(String wrd)` normalizes to uppercase and delegates to `HashSet.contains()`; returns false for null or empty input
- `randomWord(int n)` converts the HashSet to ArrayList and returns `list.get(new Random().nextInt(list.size()))`

`WordDictionaryTest.java` — 5 JUnit 4.12 tests:
1. `testFileExists` — asserts twister_words/3.txt is accessible (DICT-07)
2. `testIsValidWord_knownWord` — CAT (upper and lowercase) returns true
3. `testIsValidWord_invalidWord` — ZZZZZZZZZ and empty string return false
4. `testRandomWord_returnsCorrectLength` — 10 iterations, each result has length 3
5. `testRandomWord_returnsDistinctWords` — 100 calls yield >= 5 distinct words (Pitfall 15 mitigation)

## mvn test Output

```
[INFO] Running cs120.lab06.WordDictionaryTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.093 s
[INFO]
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

CAT was confirmed present in `twister_words/3.txt` before writing the test.

## Verification

| Check | Result |
|-------|--------|
| WordDictionary.java exists | CONFIRMED |
| Zero javafx imports | CONFIRMED (grep returns 0) |
| uses new File() | CONFIRMED |
| does NOT use getResourceAsStream | CONFIRMED |
| isValidWord("CAT") after initialize(3) | CONFIRMED true |
| isValidWord("ZZZZZZZZZ") | CONFIRMED false |
| randomWord(3) returns non-null, length 3 | CONFIRMED |
| mvn test — 5 tests, 0 failures, 0 errors | CONFIRMED |
| All tests pass (BUILD SUCCESS) | CONFIRMED |

## Commit History

| Task | Commit | Description |
|------|--------|-------------|
| T01 | 7dcb73f | feat(01-02): implement WordDictionary pure-Java dictionary loader |
| T02 | 44cb13a | feat(01-02): add WordDictionaryTest — 5 JUnit 4.12 tests, all passing |

## Deviations from Plan

None — plan executed exactly as written.

## Known Stubs

None.

## Threat Flags

No new security surface introduced. WordDictionary reads from a fixed project-root path (`twister_words/`); the path is not user-supplied. This matches the threat model in the plan (T-01-03, T-01-04 both accepted).

## Self-Check: PASSED

- WordDictionary.java: FOUND at src/main/java/cs120/lab06/WordDictionary.java
- WordDictionaryTest.java: FOUND at src/test/java/cs120/lab06/WordDictionaryTest.java
- Commit 7dcb73f: FOUND in git log
- Commit 44cb13a: FOUND in git log
- Zero javafx imports: CONFIRMED
- mvn test 5/5 passing: CONFIRMED
