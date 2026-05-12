# Pitfalls Research — JavaFX TextTwist

**Project:** CS120 Lab06 — JavaFX 21 + Maven word game
**Researched:** 2026-05-12
**Confidence note:** HIGH confidence on module system, threading, and resource loading (well-documented
JavaFX 21 behaviors). MEDIUM confidence on archetype-specific output (depends on openjfx archetype
version installed locally). LOW confidence items are flagged inline.

---

## Critical Pitfalls

These will cause the project to not compile, not run, or silently corrupt game state.

---

### 1. Missing or Wrong JavaFX Module Declarations in module-info.java

**Warning sign:** Compiles but throws `java.lang.module.FindException` or
`java.lang.IllegalAccessException` at runtime. FXML loads but controllers are null or events never fire.

**What goes wrong:** The openjfx fxml archetype generates a `module-info.java` that declares
`requires javafx.fxml` and `requires javafx.controls`, and opens the controller package to
`javafx.fxml`. If you rename packages, move controller classes, or add a model package, the
`opens` directive no longer covers the new location. FXML reflection silently fails — `@FXML`
fields stay null and event handlers are never wired.

**Prevention:**
- Every package that contains a class annotated with `@FXML` or instantiated by FXMLLoader must
  appear in an `opens <package> to javafx.fxml;` directive.
- The model package (WordDictionary, TwistController) does NOT need to be opened unless
  FXMLLoader instantiates those classes directly.
- Full working example for this project:

```java
module lab06 {
    requires javafx.controls;
    requires javafx.fxml;

    opens cs120.lab06 to javafx.fxml;        // controller class lives here
    exports cs120.lab06;                      // only needed if other modules import this
}
```

- If you split into sub-packages (e.g., `cs120.lab06.model`, `cs120.lab06.controller`),
  add a separate `opens` for each package that has `@FXML` annotations.

**Phase:** Address in Phase 1 (project scaffold). Lock module-info.java before writing any
controller code so you do not chase phantom null-pointer errors later.

---

### 2. Platform.runLater() Omitted on Timer UI Updates

**Warning sign:** Timer label updates work in testing but throw
`java.lang.IllegalStateException: Not on FX application thread` intermittently. Sometimes the
label updates but the score label does not, producing inconsistent display.

**What goes wrong:** The 120-second countdown requires a background thread or
`AnimationTimer`/`Timeline`. Any direct call to `label.setText(...)` from a non-FX thread
(e.g., a `java.util.Timer` task, a `Thread`, or a `ScheduledExecutorService` callback) violates
JavaFX's single-thread rule. The exception may only appear under load or may be swallowed by
the default uncaught exception handler, making the bug intermittent and hard to reproduce.

**Prevention — use `Timeline` instead of `java.util.Timer`:**

```java
// CORRECT: Timeline runs on the FX thread automatically
Timeline countdown = new Timeline(
    new KeyFrame(Duration.seconds(1), e -> {
        secondsLeft--;
        updateTimerLabel();   // safe — this runs on FX thread
        if (secondsLeft <= 0) endEpisode();
    })
);
countdown.setCycleCount(120);
countdown.play();
```

Never do this:

```java
// WRONG: Timer callbacks are NOT on the FX thread
new java.util.Timer().scheduleAtFixedRate(new TimerTask() {
    public void run() {
        timerLabel.setText(...);  // IllegalStateException
    }
}, 0, 1000);
```

If you must use a background thread, wrap every UI call in `Platform.runLater(() -> { ... })`.

**Phase:** Address in Phase 2 (timer implementation). Test explicitly by running the timer for
its full 120 seconds without touching the UI — any threading error appears then.

---

### 3. Dictionary Files Not Found at Runtime (getResourceAsStream vs FileReader)

**Warning sign:** `WordDictionary` constructor throws `NullPointerException` or
`FileNotFoundException` when run via `mvn javafx:run` but works when run from inside the IDE.
Works in one machine, fails when submitted.

**What goes wrong:** The spec places `twister_words/` at the project root, NOT inside
`src/main/resources`. If you load the files with `new FileReader("twister_words/3.txt")`, the
path resolves relative to the JVM working directory. When Maven runs the project, the working
directory is the project root — so it works. When the grader runs from a different directory or
packages the jar, the relative path breaks.

`getResourceAsStream()` only finds files on the classpath. Since `twister_words/` is NOT inside
`src/main/resources`, it is NOT on the classpath, so `getResourceAsStream("twister_words/3.txt")`
also fails.

**Prevention:** The safest approach for this project setup is `FileReader` with a path built
relative to the system property `user.dir`, combined with a clear contract about where the
directory must live:

```java
// Reads relative to wherever Maven runs (project root)
String path = "twister_words/" + n + ".txt";
try (Scanner sc = new Scanner(new File(path))) {
    int count = sc.nextInt(); sc.nextLine();
    while (sc.hasNextLine()) words.add(sc.nextLine().trim());
}
```

Document that the grader must run `mvn javafx:run` from the project root. Alternatively, copy
`twister_words/` into `src/main/resources` and use `getResourceAsStream` — but verify with the
spec whether the directory location is mandated.

**Phase:** Address in Phase 1 (WordDictionary implementation). Write and pass the JUnit tests
before writing any GUI code to confirm the path resolves correctly.

---

### 4. pom.xml Missing javafx-controls or Wrong Plugin Version

**Warning sign:** `mvn compile` succeeds but `mvn javafx:run` fails with
`ClassNotFoundException: javafx.scene.control.Button` or
`Error: JavaFX runtime components are missing`.

**What goes wrong:** The openjfx fxml archetype sometimes generates a pom.xml that includes
`javafx-fxml` but omits `javafx-controls`. Every `Button`, `Label`, `ListView`, and `TextField`
lives in `javafx-controls`. Without it, the module graph is incomplete and the app crashes on
launch.

Additionally, if the `javafx-maven-plugin` version in the archetype is older than `0.0.8`,
the `--add-modules` JVM flag is not injected correctly for JDK 17+, causing the runtime to
not find JavaFX modules even when they are on the classpath.

**Prevention:** Verify pom.xml contains ALL three after archetype generation:

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>21</version>
</dependency>

<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>   <!-- minimum; use 0.0.8 or higher -->
    <configuration>
        <mainClass>cs120.lab06/cs120.lab06.App</mainClass>
    </configuration>
</plugin>
```

The `mainClass` value must include the module name prefix (`moduleName/fully.qualified.Main`)
when using `module-info.java`.

**Phase:** Address in Phase 1 (project scaffold). Run `mvn javafx:run` on a blank App.java
before writing any game code.

---

### 5. Letter Button State Desync (Used vs. Available Tracking)

**Warning sign:** After pressing Clear, some letter buttons stay disabled. After pressing
Last Word then Clear, the letter buttons end up in an inconsistent state — some letters from
the last word are disabled even though the guess field is empty. After a new episode starts,
buttons from the previous episode are still visible.

**What goes wrong:** Letter buttons map to individual characters in the target word. Each button
represents one position in the word (not one unique letter — a word with two E's needs two
separate buttons). The common mistake is tracking "which letters are used" by letter value
instead of by button identity (index/reference).

Clear must re-enable ALL buttons, not just the ones whose letters appear in the current guess
string — because duplicate letters create ambiguity about which physical button was pressed.

Last Word must re-enable all current buttons first, then disable exactly the buttons
corresponding to the positions used to spell the last successful word.

**Prevention:**
- Store buttons in a `List<Button>` ordered by position in the target word.
- Track "which buttons are used" as a `List<Integer>` of indices (not a `Set<Character>`).
- Clear operation: iterate all buttons, call `setDisable(false)`.
- Last Word operation: call Clear logic first, then replay the index sequence from the last
  accepted guess to re-disable the right buttons.
- On new episode: remove all letter buttons from their container, rebuild from the new word's
  letters, re-add to the layout.

**Phase:** Address in Phase 3 (letter button wiring). This is the most common source of subtle
UI bugs in word games. Write a manual test checklist: (a) select 3 letters, press Clear —
all 3 re-enabled; (b) guess a word, press Last Word — exactly those letters disabled again;
(c) advance episode — all old buttons gone, new buttons present.

---

## Common Mistakes

---

### 6. openjfx fxml Archetype Generates Wrong Package Structure

**How it manifests:** The archetype generates `App.java` and `AppController.java` in a flat
package. If you rename the controller or move it to a sub-package without updating
`module-info.java` AND the FXML `fx:controller` attribute, the app launches to a blank window
with no errors. FXMLLoader silently ignores a missing controller class when the module graph
prevents reflection.

**Fix:**
1. After renaming, update `fx:controller="cs120.lab06.YourController"` in the FXML file.
2. Update `opens cs120.lab06` in module-info.java to match.
3. Verify with `mvn javafx:run` immediately after any package/class rename.

---

### 7. Duplicate Word Submission Allowed

**How it manifests:** Player submits "CAT" twice. Both are accepted and score is doubled.
The scrollable word list shows "CAT" twice.

**Fix:** `TwistController.checkGuessWord()` must check against a `Set<String>` (or a
`List` with `.contains()`) of already-accepted words before awarding points. Return 0 if
the word was already found. The spec implies this — "no re-use allowed."

---

### 8. "Last Word" Button Enabled Before Any Word Is Guessed

**How it manifests:** Player clicks Last Word immediately at game start. `lastWord` field
is null. `NullPointerException` in the event handler.

**Fix:** Initialize Last Word button as `setDisable(true)`. Enable it only after the first
successful word submission. Reset to disabled at the start of each new episode (the last
accepted word from a previous episode is no longer relevant).

---

### 9. Level Progression Off-By-One (Level vs. Letter Count)

**How it manifests:** Level 1 should use 3-letter words. The formula is
`letterCount = level + 2`. If `level` starts at 0 instead of 1, Level 0 loads `2.txt` which
does not exist. If level starts at 1 correctly but the file name is computed as `level.txt`
instead of `(level + 2).txt`, Level 1 loads `1.txt` which also does not exist.

**Fix:** Keep `level` as the game-level integer (1–8). Derive letter count explicitly:

```java
int letterCount = level + 2;  // Level 1 = 3 letters, Level 8 = 10 letters
```

Assert at startup that files `3.txt` through `10.txt` all exist and are non-empty.

---

### 10. End-of-Episode Score Threshold Applied at Wrong Time

**How it manifests:** Player guesses the full target word (winning the episode). The game
immediately advances to the next level WITHOUT checking the minimum score threshold. Or:
the minimum score check fires on every word guess instead of only at episode end.

**What the spec says:** The episode ends when EITHER the timer expires OR the full target
word is guessed. The threshold check — `floor(0.25 * max² * 10)` — determines advance/stay/
game-over. The threshold applies in both cases.

**Fix:** Route BOTH `timerExpired()` and `targetWordGuessed()` through a single
`endEpisode()` method that contains all threshold logic. Do not inline the check in the
guess-word handler.

---

### 11. Scoring Formula Applied to Word Length Including Bonus

**How it manifests:** Score for a 5-letter word is computed as `5 * 5 * 10 = 250` but the
player entered a 3-letter substring of the target and expects `3 * 3 * 10 = 90`. The variable
`n` in `checkGuessWord` gets the wrong value.

**Fix:** `n` must be the length of the GUESSED word, not the target word.
`return guessWord.length() * guessWord.length() * 10;` — where `guessWord` has already been
validated as non-empty and present in the dictionary.

---

### 12. Twist Reshuffles but Doesn't Clear the Guess Field

**How it manifests:** Player presses Twist. Letter buttons are reshuffled. But the guess
field still shows letters that were selected before the Twist. Those letters are now out of
sync with the button state (the buttons whose positions were selected are re-enabled after
the shuffle, so the player can select the same physical position twice).

**Fix:** Twist must: (1) clear the guess field, (2) re-enable all letter buttons,
(3) shuffle and redisplay letter buttons. Same as Clear, plus the shuffle step.

---

## Testing Gotchas

---

### 13. JUnit 4 + Maven Surefire Compatibility on JDK 17+

**Issue:** JUnit 4.12 tests run fine when invoked from the IDE but fail silently with Maven
(`mvn test` reports 0 tests run) on JDK 17+.

**Cause:** Maven Surefire plugin version 2.x (the archetype default) does not support
JDK 17's module encapsulation. It cannot fork the JVM correctly for modular projects and
may simply skip all tests without error output.

**Prevention:**
- Pin Surefire to version 3.x in pom.xml:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.1.2</version>
</plugin>
```

- If tests are still skipped, add the `--add-opens` JVM arg for reflection:

```xml
<configuration>
    <argLine>--add-opens java.base/java.lang=ALL-UNNAMED</argLine>
</configuration>
```

- Run `mvn test -X` (debug mode) to see if tests are discovered but skipped vs. never found.

---

### 14. WordDictionary JUnit Tests Depend on Working Directory

**Issue:** `WordDictionaryTest` passes when run from the IDE (working directory = project root)
but fails when run via `mvn test` if Maven's Surefire forks the JVM with a different working
directory.

**Prevention:** In the test class, assert that the word file exists before any test runs:

```java
@Before
public void setUp() {
    File f = new File("twister_words/3.txt");
    assertTrue("Dictionary file not found at expected path: " + f.getAbsolutePath(),
               f.exists());
}
```

This makes the failure message actionable ("wrong directory") instead of a cryptic
NullPointerException inside WordDictionary.

---

### 15. randomWord() Not Tested for Uniform Distribution (Low Risk, Easily Missed)

**Issue:** `randomWord(int n)` uses `Random.nextInt(count)` where `count` is read from the
first line of the file. If the file has 500 words but the first-line count says 450, words
after position 450 are never returned. This is a silent data bug — the method returns valid
words, just not all of them.

**Prevention:** In the JUnit test for `randomWord`, call the method 200 times and assert that
at least 5 distinct words were returned (a statistical floor — if only 1 unique word is ever
returned, something is wrong with the random selection). This does not fully validate
distribution but catches the total-failure case.

---

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| Project scaffold (Phase 1) | Archetype generates wrong module-info.java opens | Run `mvn javafx:run` on blank app before writing any game code |
| Phase 1 | pom.xml missing javafx-controls | Verify all three JavaFX artifacts present after archetype generation |
| WordDictionary (Phase 1) | File path breaks outside project root | Use `new File("twister_words/" + n + ".txt")`, add @Before existence check in tests |
| WordDictionary tests (Phase 1) | Surefire 2.x silently skips tests on JDK 17 | Pin Surefire to 3.1.2 immediately |
| Timer (Phase 2) | UI updated from non-FX thread | Use `Timeline` instead of `java.util.Timer`; never call label.setText from a background thread |
| Letter buttons (Phase 3) | Tracking by letter value instead of button index | Use `List<Integer>` of selected indices, not `Set<Character>` |
| Last Word button (Phase 3) | NPE on first use; stale state after episode change | Disable on start; reset on episode start; replay index sequence |
| Guess submission (Phase 3) | Duplicate words accepted | Check against `Set<String>` of found words before awarding points |
| Level progression (Phase 4) | Off-by-one in level-to-letter-count mapping | Explicit `letterCount = level + 2`; assert files exist at startup |
| End-of-episode logic (Phase 4) | Threshold check bypassed when target word guessed | Route timer expiry and full-word guess through single `endEpisode()` |
| Scoring (Phase 3) | n computed from target word length, not guessed word length | Score = `guessLength * guessLength * 10` |
| Twist button (Phase 3) | Guess field not cleared before reshuffle | Twist = Clear + shuffle; share the Clear code path |

---

## Sources

- JavaFX 21 module system documentation: https://openjfx.io/openjfx-docs/ (HIGH confidence)
- Maven javafx-maven-plugin configuration: https://github.com/openjfx/javafx-maven-plugin (HIGH confidence)
- JavaFX threading model (Platform.runLater, Timeline): JavaFX API docs, package javafx.animation (HIGH confidence)
- JUnit 4 + Surefire 3.x compatibility: https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit.html (HIGH confidence)
- Letter button state management, dictionary loading, Last Word edge cases: derived from project spec analysis + standard JavaFX game patterns (MEDIUM confidence — field-verify during implementation)
