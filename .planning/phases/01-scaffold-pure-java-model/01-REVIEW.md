---
phase: 01-scaffold-pure-java-model
reviewed: 2026-05-12T00:00:00Z
depth: standard
files_reviewed: 8
files_reviewed_list:
  - pom.xml
  - src/main/java/cs120/lab06/App.java
  - src/main/java/cs120/lab06/PrimaryController.java
  - src/main/java/cs120/lab06/WordDictionary.java
  - src/main/java/cs120/lab06/TwistController.java
  - src/main/resources/cs120/lab06/primary.fxml
  - src/test/java/cs120/lab06/WordDictionaryTest.java
  - src/test/java/cs120/lab06/TwistControllerTest.java
findings:
  critical: 1
  warning: 4
  info: 3
  total: 8
status: issues_found
---

# Phase 1: Code Review Report

**Reviewed:** 2026-05-12T00:00:00Z
**Depth:** standard
**Files Reviewed:** 8
**Status:** issues_found

## Summary

Phase 1 delivers a working Maven scaffold, a pure-Java `WordDictionary`, a pure-Java `TwistController`, a minimal FXML stub, and JUnit 4 tests for both model classes. The architectural constraints from CLAUDE.md are respected: no JavaFX imports in either model class, dictionary loaded via `new File()`, Surefire 3.1.2, no `module-info.java`, and `mainClass` without a module prefix.

The most serious defect is a crash in `randomWord()` when `words` is empty — `nextInt(0)` throws `IllegalArgumentException` unchecked. Four warnings cover: `randomWord`'s unused and misleading `n` parameter, the `App.setRoot` static-`null` crash path, a missing `@Test` for `beginEpisode` resetting `levelScore` across episodes, and a test that relies on a hardcoded dictionary word. Three informational items cover dead code, a naming divergence, and a test that could give false confidence.

---

## Critical Issues

### CR-01: `randomWord()` crashes with `IllegalArgumentException` if `words` is empty

**File:** `src/main/java/cs120/lab06/WordDictionary.java:87`
**Issue:** `new Random().nextInt(list.size())` throws `IllegalArgumentException` when `list.size() == 0`. This happens in two real scenarios: (1) `randomWord()` is called before `initialize()` (the `HashSet` is constructed empty and never populated), and (2) a dictionary file exists but contains only the count line with no word entries. In both cases the exception is unchecked and propagates to the caller as an undiagnosed crash rather than an actionable error message.

`TwistController.shuffleLetters()` has the same category of crash (`NullPointerException` on `targetWord.toCharArray()`) if called before `beginEpisode()`, but that is a misuse-before-init pattern also present here and mitigated by the test setup. The `randomWord` case is more dangerous because `TwistController.beginEpisode()` calls `randomWord()` directly — a bad or empty file silently produces a cryptic `IllegalArgumentException` rather than the clear `RuntimeException` already thrown by `initialize()`.

**Fix:**
```java
public String randomWord(int n) {
    if (words.isEmpty()) {
        throw new IllegalStateException(
            "randomWord() called on empty dictionary — call initialize() first");
    }
    ArrayList<String> list = new ArrayList<>(words);
    return list.get(new Random().nextInt(list.size()));
}
```

---

## Warnings

### WR-01: `randomWord(int n)` parameter `n` is silently ignored — callers can get wrong-length words

**File:** `src/main/java/cs120/lab06/WordDictionary.java:85-88`
**Issue:** The `n` parameter is declared but never used. The method returns any word from the currently loaded set regardless of length. If a caller passes `n=5` but the last `initialize()` call was for `n=3`, they silently get 3-letter words. This is a latent correctness bug for Phase 3, where `PrimaryController` will drive level progression and could call `beginEpisode` and `randomWord` in sequences that expose this trap. The Javadoc says "not used for filtering" but does not warn that the burden is entirely on the caller.

**Fix (option A — validate):** Add a guard that confirms the loaded set matches the expected length, or assert that `initialize(n)` was the last call. Because the set is loaded at the `initialize` call site, a simple documented contract is sufficient for Phase 1, but the method should at minimum warn:
```java
/**
 * @param n expected word length — MUST match the letterCount passed to the
 *          most recent {@code initialize(n)} call. This parameter is NOT used
 *          for filtering; passing a mismatched value silently returns words of
 *          the wrong length.
 */
public String randomWord(int n) { ... }
```

**Fix (option B — remove the parameter):** Since `n` is never used and `initialize` already scopes the loaded set, drop the parameter entirely and update callers:
```java
public String randomWord() {
    if (words.isEmpty()) { ... }
    ArrayList<String> list = new ArrayList<>(words);
    return list.get(new Random().nextInt(list.size()));
}
```

---

### WR-02: `App.setRoot()` will throw `NullPointerException` if called before `start()`

**File:** `src/main/java/cs120/lab06/App.java:26-28`
**Issue:** `scene` is a `static` field initialized to `null`. `setRoot()` is package-private (no access modifier) and can be called from any class in `cs120.lab06` at any time — including before `Application.start()` has run. Calling `scene.setRoot(...)` before `start()` produces a `NullPointerException` with no helpful message. This will become a real risk in Phase 2/3 when `PrimaryController` needs to trigger scene transitions.

**Fix:** Add a null guard or document the precondition explicitly:
```java
static void setRoot(String fxml) throws IOException {
    if (scene == null) {
        throw new IllegalStateException("setRoot() called before Application.start()");
    }
    scene.setRoot(loadFXML(fxml));
}
```

---

### WR-03: No test verifies that `beginEpisode()` resets `levelScore` across episodes

**File:** `src/test/java/cs120/lab06/TwistControllerTest.java`
**Issue:** `testScoreAccumulation` confirms `levelScore` starts at 0 after the first `beginEpisode(3)` in `setUp()`. However, there is no test that calls `beginEpisode()` a second time and confirms `levelScore` resets to 0 while `score` continues to accumulate. This is one of the two explicit behavioral contracts called out in the CLAUDE.md scoring rules ("cumulative score persists across episodes"). A regression here would be silent: the existing tests would still pass even if `beginEpisode` accidentally reset `score`.

**Fix:** Add a test:
```java
@Test
public void testBeginEpisode_resetsLevelScoreNotScore() {
    String target = controller.getTargetWord();
    int pts = controller.checkGuessWord(target);
    assertTrue("First episode must earn points", pts > 0);
    int scoreAfter = controller.getScore();

    controller.beginEpisode(3); // start a new episode
    assertEquals("levelScore must reset to 0 after beginEpisode", 0, controller.getLevelScore());
    assertEquals("score must NOT reset after beginEpisode", scoreAfter, controller.getScore());
}
```

---

### WR-04: `testIsValidWord_knownWord` hardcodes "CAT" — brittle if dictionary content changes

**File:** `src/test/java/cs120/lab06/WordDictionaryTest.java:61`
**Issue:** The test asserts `dict.isValidWord("CAT")` returns `true` based on a code comment claiming "CAT is confirmed present in twister_words/3.txt". This is verified correct today (CAT is on line 131 of 3.txt), but the test will silently become a false positive if the dictionary file is ever regenerated or modified to remove CAT. The test should either load the word dynamically or use a more robust confirmation mechanism. The same fragility exists in the `TwistControllerTest` plan comment that mentioned CAT (now resolved by using `getTargetWord()` in the actual test).

**Fix:** Confirm the word exists programmatically, or at minimum make the test fail loudly with the dictionary's actual content on failure:
```java
@Test
public void testIsValidWord_knownWord() {
    // Pick the first word from the loaded dictionary to avoid hardcoding
    String knownWord = dict.randomWord(3);
    assertTrue("A word returned by randomWord() must be valid: " + knownWord,
               dict.isValidWord(knownWord));
    // Verify lowercase normalization works for the same word
    assertTrue("Lowercase version must also be valid: " + knownWord.toLowerCase(),
               dict.isValidWord(knownWord.toLowerCase()));
}
```

---

## Info

### IN-01: `App.setRoot()` is dead code in Phase 1

**File:** `src/main/java/cs120/lab06/App.java:26-28`
**Issue:** `setRoot()` is never called in Phase 1. It is intentional scaffolding for Phase 2/3, but static dead code methods that reference static mutable state (`scene`) increase the surface area for the WR-02 crash described above. No action required if Phase 2 plans to use it, but track it.

---

### IN-02: FXML file is named `primary.fxml` but CLAUDE.md architecture spec names it `twist.fxml`

**File:** `src/main/resources/cs120/lab06/primary.fxml`
**Issue:** The CLAUDE.md architecture section says "View: twist.fxml — FXML layout (fx:id contracts locked in Phase 2)". The Phase 1 scaffold uses `primary.fxml` as a placeholder. Phase 2 must either rename the file and update `App.java:20` to `loadFXML("twist")`, or update CLAUDE.md. If this divergence is not resolved before Phase 2 wires `@FXML` fields, the controller annotations will silently not bind.

---

### IN-03: `testRandomWord_returnsCorrectLength` gives false confidence — the `n` parameter is ignored

**File:** `src/test/java/cs120/lab06/WordDictionaryTest.java:80-89`
**Issue:** The test calls `dict.randomWord(3)` and asserts the returned word has length 3. This passes only because `initialize(3)` was called in `setUp()`, which loaded the 3-letter dictionary. The test does not prove that `randomWord` enforces the length constraint — it proves that the 3-letter dictionary happens to contain 3-letter words. If `randomWord(5)` were called after `initialize(3)`, the test would still pass and show no anomaly. The test name implies a length guarantee that the implementation does not provide. Ideally addressed by fixing WR-01 (removing or bounding the `n` parameter).

---

_Reviewed: 2026-05-12T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
