---
phase: 01-scaffold-pure-java-model
verified: 2026-05-12T16:40:00Z
status: human_needed
score: 9/10 must-haves verified
overrides_applied: 0
re_verification: null
human_verification:
  - test: "Run `mvn javafx:run` from /Users/andrewmcinnis/Desktop/TextTwist in a display-capable terminal"
    expected: "A blank JavaFX window opens with no console errors (no NullPointerException, no ClassNotFoundException, no module-related errors); the window is closeable without errors"
    why_human: "No display is available in the automated environment. App.java and primary.fxml are structurally correct (mvn compile passes, FXML root element is valid VBox, fx:controller matches PrimaryController), but actual window rendering cannot be confirmed programmatically."
---

# Phase 1: Scaffold + Pure Java Model — Verification Report

**Phase Goal:** Maven project builds and launches; WordDictionary and TwistController are fully implemented and unit-tested with zero JavaFX imports.
**Verified:** 2026-05-12T16:40:00Z
**Status:** human_needed
**Re-verification:** No — initial verification

---

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | `mvn javafx:run` opens a blank JavaFX window with no console errors | ? UNCERTAIN | App.java and primary.fxml structurally correct; mvn compile passes; window rendering cannot be confirmed without a display |
| 2 | `mvn test` runs and reports all WordDictionary tests passing (isValidWord and randomWord verified; @Before existence check fires on missing file) | VERIFIED | Live run: WordDictionaryTest — Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 |
| 3 | TwistController.checkGuessWord() returns correct points (n*n*10) for valid words, 0 for invalid/duplicate words | VERIFIED | Live run: TwistControllerTest — Tests run: 5, Failures: 0, Errors: 0, Skipped: 0; testCheckGuessWord_validWord, testCheckGuessWord_invalidWord, testCheckGuessWord_duplicateRejected all pass |
| 4 | twister_words/3.txt through 10.txt are present at the project root and WordDictionary loads the correct file for a given letter count | VERIFIED | `ls twister_words/` confirms 3.txt–10.txt (plus 1.txt, 2.txt); initialize() uses `new File(folderPath + "/" + letterCount + ".txt")` confirmed in source |
| 5 | pom.xml has Surefire 3.1.2, mainClass=cs120.lab06.App (no module prefix) | VERIFIED | pom.xml line 73: `<version>3.1.2</version>`; line 64: `<mainClass>cs120.lab06.App</mainClass>` |
| 6 | No module-info.java anywhere under src/ | VERIFIED | `find src/ -name module-info.java` returns no results |
| 7 | WordDictionary.java has zero javafx imports, uses new File() for loading | VERIFIED | `grep -c "import javafx" WordDictionary.java` = 0; new File() confirmed at line 45 of source; getResourceAsStream absent |
| 8 | TwistController.java has zero javafx imports | VERIFIED | `grep -c "import javafx" TwistController.java` = 0 |
| 9 | checkGuessWord(targetWord) called twice returns 0 on second call | VERIFIED | testCheckGuessWord_duplicateRejected passes; guessedWords HashSet confirmed in source |
| 10 | shuffleLetters() returns same multiset of characters as targetWord | VERIFIED | testShuffleLetters_sameLetters passes; implementation uses Collections.shuffle on char list confirmed |

**Score:** 9/10 truths verified (1 requires human confirmation)

---

### Deferred Items

None.

---

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `pom.xml` | Maven build with JavaFX 21, JUnit 4.12, Surefire 3.1.2 | VERIFIED | groupId=cs120, artifactId=lab06, all three dependencies present, surefire 3.1.2, javafx-maven-plugin 0.0.8 |
| `src/main/java/cs120/lab06/App.java` | JavaFX Application entry point | VERIFIED | Exists; extends Application; loads primary.fxml |
| `src/main/resources/cs120/lab06/primary.fxml` | Minimal FXML stub for blank window | VERIFIED | Exists; VBox root element; fx:controller=cs120.lab06.PrimaryController |
| `src/main/java/cs120/lab06/WordDictionary.java` | Dictionary loader and word validator | VERIFIED | Exists; constructor, initialize(), isValidWord(), randomWord() all present and substantive |
| `src/test/java/cs120/lab06/WordDictionaryTest.java` | JUnit 4.12 tests for WordDictionary | VERIFIED | Exists; @Before, 5 @Test methods; all 5 pass live |
| `src/main/java/cs120/lab06/TwistController.java` | Game model — score/level/targetWord state | VERIFIED | Exists; all required fields and methods present and substantive |
| `src/test/java/cs120/lab06/TwistControllerTest.java` | JUnit 4.12 tests for TwistController | VERIFIED | Exists; @Before, 5 @Test methods; all 5 pass live |
| `twister_words/3.txt` through `twister_words/10.txt` | Dictionary word files at project root | VERIFIED | All 8 files present; confirmed by `ls twister_words/` |

---

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `App.java` | `primary.fxml` | `App.class.getResource("primary.fxml")` | WIRED | loadFXML("primary") in App.java resolves to primary.fxml |
| `WordDictionaryTest.java` | `twister_words/3.txt` | `new File("twister_words/3.txt")` in @Before | WIRED | @Before asserts file exists with absolute path in message; test passes live |
| `WordDictionary.java` | `twister_words/{n}.txt` | `new File(folderPath + "/" + letterCount + ".txt")` in initialize() | WIRED | Confirmed in source line 44; tests read real dictionary data live |
| `TwistController.java` | `WordDictionary.java` | `myDictionary.isValidWord(wrd)` in checkGuessWord() | WIRED | Source line 73: `if (!myDictionary.isValidWord(upper))`; confirmed in source |
| `TwistController.java` | `twister_words/{n}.txt` | `myDictionary.initialize(letterCount)` in beginEpisode() | WIRED | Source line 49; tests confirm dictionary loads and random word is returned |

---

### Data-Flow Trace (Level 4)

Not applicable — no dynamic-rendering components in Phase 1. All artifacts are pure-Java model classes and test classes with no JSX/FXML data rendering. The FXML stub is a static label.

---

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| mvn test passes 10 tests, 0 failures | `mvn test` from project root | Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS | PASS |
| WordDictionaryTest 5/5 passing | observed in test run | Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 | PASS |
| TwistControllerTest 5/5 passing | observed in test run | Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 | PASS |
| Surefire 3.1.2 auto-detected JUnit4Provider | observed in test run output | "Using auto detected provider org.apache.maven.surefire.junit4.JUnit4Provider" | PASS |
| mvn javafx:run opens blank window | requires display | SKIPPED — no display in automated environment | ? SKIP |

---

### Probe Execution

No probe scripts declared in PLAN frontmatter or found under `scripts/*/tests/probe-*.sh`.

---

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|------------|-------------|--------|----------|
| SETUP-01 | 01-01 | Maven project with openjfx archetype, groupId=cs120, artifactId=lab06, JavaFX 21 | SATISFIED | pom.xml: groupId=cs120, artifactId=lab06, javafx.version=21 — archetype deviated but manual creation achieves identical contract |
| SETUP-02 | 01-01 | pom.xml includes javafx-controls, javafx-fxml, JUnit 4.12, Surefire 3.1.2 | SATISFIED | All four confirmed in pom.xml; surefire version 3.1.2 at line 73 |
| SETUP-03 | 01-01 | module-info.java deleted; project runs as unnamed module | SATISFIED | `find src/ -name module-info.java` returns nothing |
| SETUP-04 | 01-01 | `mvn javafx:run` launches a blank JavaFX window | NEEDS HUMAN | mvn compile passes; App.java + primary.fxml structurally correct; display unavailable for runtime confirmation |
| SETUP-05 | 01-01 | twister_words/ folder (3.txt–10.txt) present at project root | SATISFIED | All 8 files confirmed present |
| DICT-01 | 01-02 | WordDictionary accepts folder path in constructor | SATISFIED | `public WordDictionary(String folderPath)` confirmed in source |
| DICT-02 | 01-02 | initialize() reads correct word file, parses word count, loads all words | SATISFIED | Source lines 42-58: reads file, skips count line, populates HashSet; confirmed by passing tests |
| DICT-03 | 01-02 | isValidWord() returns true if word exists, false otherwise | SATISFIED | Source lines 68-73; testIsValidWord_knownWord and testIsValidWord_invalidWord pass |
| DICT-04 | 01-02 | randomWord(n) returns a random word of length n from the collection | SATISFIED | Source lines 85-88; testRandomWord_returnsCorrectLength passes 10 iterations |
| DICT-05 | 01-02 | JUnit tests verify isValidWord with known valid and invalid words | SATISFIED | testIsValidWord_knownWord (CAT, cat) and testIsValidWord_invalidWord (ZZZZZZZZZ, "") both pass |
| DICT-06 | 01-02 | JUnit tests verify randomWord returns non-null string of correct length | SATISFIED | testRandomWord_returnsCorrectLength (10 iterations) and testRandomWord_returnsDistinctWords (100 iterations, >=5 distinct) both pass |
| DICT-07 | 01-02 | @Before test fixture asserts dictionary file exists with actionable message | SATISFIED | WordDictionaryTest @Before line 33-36; TwistControllerTest @Before line 29-33; both include absolute path in failure message |
| MODEL-01 | 01-03 | TwistController holds score, levelScore, level, targetWord, myDictionary fields | SATISFIED | Source lines 19-24: all 6 fields declared private; guessedWords also present |
| MODEL-02 | 01-03 | shuffleLetters() returns List of target word's letters in shuffled order | SATISFIED | Source lines 92-99; testShuffleLetters_sameLetters passes (multiset equality confirmed) |
| MODEL-03 | 01-03 | checkGuessWord validates via isValidWord, returns n*n*10 or 0 | SATISFIED | Source lines 65-82; testCheckGuessWord_validWord and testCheckGuessWord_invalidWord pass |
| MODEL-04 | 01-03 | TwistController tracks guessed words; duplicates return 0 | SATISFIED | Source lines 70-72: guessedWords.contains(upper) check; testCheckGuessWord_duplicateRejected passes |
| MODEL-05 | 01-03 | TwistController has zero JavaFX imports | SATISFIED | `grep -c "import javafx" TwistController.java` = 0 |
| MODEL-06 | 01-03 | Level 1 uses 3-letter word set; Level n uses n+2 letters | SATISFIED (contract) | beginEpisode(int letterCount) accepts the caller-computed letter count; the n+2 formula is PrimaryController's responsibility (Phase 3 per architecture decisions D-14); TwistController correctly exposes the interface needed to enforce this at the call site |

---

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| `WordDictionary.java` | 87 | `new Random().nextInt(list.size())` — crashes with IllegalArgumentException if words is empty (called before initialize() or on empty file) | Warning | Existing tests do not exercise this path; runtime crash surface for Phase 3 if PrimaryController calls beginEpisode() on a missing or empty dictionary file. Code review CR-01 documents this. No TBD/FIXME markers. |

No `TBD`, `FIXME`, or `XXX` markers found in any Phase 1 source or test file. No placeholder or stub anti-patterns found.

---

### Human Verification Required

#### 1. Blank JavaFX Window Launch

**Test:** From a terminal with a display (macOS desktop), run `mvn javafx:run` from `/Users/andrewmcinnis/Desktop/TextTwist`

**Expected:** A JavaFX window opens (title may be anything). No exceptions appear in the console output — specifically no NullPointerException, ClassNotFoundException, or module-related errors. The window is closeable without error.

**Why human:** The automated environment has no display attached. `mvn compile` succeeds and the FXML / App.java structure is verified correct, but actual JavaFX window rendering requires a display and cannot be confirmed programmatically.

---

### Gaps Summary

No gaps blocking goal achievement. The single uncertain item (SETUP-04 / blank window launch) requires human verification due to display availability, not a detected code defect. All model classes, test classes, build configuration, and dictionary files are fully implemented and verified against the codebase.

The code review (01-REVIEW.md) identified one warning-level defect in `randomWord()` (empty-dictionary crash path, CR-01) that does not affect Phase 1 correctness — all tests pass — but should be addressed before Phase 3 wires PrimaryController to TwistController.

---

_Verified: 2026-05-12T16:40:00Z_
_Verifier: Claude (gsd-verifier)_
