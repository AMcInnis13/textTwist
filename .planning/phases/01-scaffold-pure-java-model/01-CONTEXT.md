# Phase 1: Scaffold + Pure Java Model - Context

**Gathered:** 2026-05-12
**Status:** Ready for planning

<domain>
## Phase Boundary

Bootstrap the Maven project using the openjfx fxml archetype, delete boilerplate, and implement two pure-Java model classes (`WordDictionary`, `TwistController`) with JUnit 4 unit tests. The phase ends when `mvn javafx:run` opens a blank window and `mvn test` reports all WordDictionary tests passing. Zero JavaFX imports anywhere in the model layer.

</domain>

<decisions>
## Implementation Decisions

### Maven Bootstrap
- **D-01:** Generate project with `mvn archetype:generate` using the `javafx-archetype-fxml` archetype (groupId=cs120, artifactId=lab06, JavaFX 21). This produces a correct Maven layout and pom.xml baseline.
- **D-02:** Delete `module-info.java` immediately after generation — JUnit 4.12 is not module-aware; unnamed module path is sufficient.
- **D-03:** Pin Surefire to 3.1.2 in pom.xml — 2.x silently skips JUnit 4 tests on JDK 21.
- **D-04:** Keep the archetype's generated App.java and primary.fxml in place (or minimal stub) so `mvn javafx:run` opens a blank window — Phase 1's SETUP-04 gate. Flesh out the UI in Phase 2.

### WordDictionary
- **D-05:** Constructor signature: `WordDictionary(String folderPath)` — stores the path. `initialize(int letterCount)` opens the file via `new File(folderPath + "/" + letterCount + ".txt")` (never `getResourceAsStream` — file is at project root, not on classpath).
- **D-06:** `isValidWord(String wrd)` normalizes input to uppercase before lookup. Dictionary files contain uppercase words; normalization makes calling code simpler and prevents silent mismatches.
- **D-07:** Words stored in a `HashSet<String>` — O(1) lookup for `isValidWord`.
- **D-08:** `randomWord(int n)` — convert the loaded `HashSet` to a `List`, pick a random index via `Random`. The `n` parameter drives which file was loaded (caller must have called `initialize(n)` first).

### TwistController
- **D-09:** Constructor signature: `TwistController(String folderPath)` — accepts the dictionary root path, not hardcoded. This lets unit tests pass `"twister_words/"` explicitly and makes the class testable without filesystem assumptions.
- **D-10:** `checkGuessWord(String wrd)` does dictionary lookup only — it does NOT verify that the guess letters are a subset of the target word. Formability is enforced entirely by letter button state in Phase 3 (UI prevents the user from forming invalid letter combinations). No isFormableFrom logic needed here.
- **D-11:** Duplicate tracking via a `Set<String> guessedWords` (e.g., `HashSet<String>`) — reset each episode. Duplicate submissions return 0 points.
- **D-12:** `shuffleLetters()` — converts `targetWord` chars to a `List<String>` of single-character strings, shuffles in-place with `Collections.shuffle(list, new Random())`, returns the shuffled list.
- **D-13:** Zero JavaFX imports — pure Java only. Required for JUnit testability without an FX runtime.
- **D-14:** `level` starts at 1; `score` and `levelScore` start at 0. Level n uses n+2 letters (Level 1 = 3-letter words, Level 8 = 10-letter words).

### JUnit Tests (Phase 1 scope)
- **D-15:** Tests in `src/test/java/cs120/lab06/` following Maven conventions.
- **D-16:** `@Before` fixture asserts the dictionary file exists and fails with a clear message if not — satisfies DICT-07 and surfaces file-path issues immediately.
- **D-17:** Phase 1 test scope covers `WordDictionary` (DICT-05, DICT-06, DICT-07). Basic `TwistController` unit tests (checkGuessWord scoring, duplicate detection) are a bonus if straightforward — not required by Phase 1 success criteria.

### Claude's Discretion
- Word collection type: `HashSet<String>` (fast lookup, sufficient for this use case)
- `randomWord` implementation: convert to ArrayList, use `Random.nextInt(size)` — standard approach
- Whether to add TwistController unit tests in Phase 1 (beyond what the spec requires) is Claude's call based on effort

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Specification
- `textTwist.pdf` — Lab06 spec; Appendix A = WordDictionary UML (method signatures to match), Appendix B = TwistController UML (method signatures to match); implementation must conform exactly

### Planning Artifacts
- `.planning/REQUIREMENTS.md` — Full requirement list; Phase 1 covers SETUP-01–05, DICT-01–07, MODEL-01–06
- `CLAUDE.md` — Critical implementation rules; includes architecture overview, scoring formula, run/test commands, and key pitfalls

### Dictionary Files
- `twister_words/` — Already present at project root; files named `3.txt` through `10.txt`; first line = word count (integer), subsequent lines = uppercase words (one per line)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `twister_words/` — Dictionary files already present at project root; no generation needed

### Established Patterns
- No existing src/ — fresh project; archetype generates the initial structure

### Integration Points
- `WordDictionary` is instantiated by `TwistController`; `TwistController` is instantiated by `PrimaryController` (Phase 3). Phase 1 only needs to make the model classes work standalone.
- The folder path `"twister_words/"` is the production value; tests pass it explicitly so there is no ambient dependency on the working directory during testing (test runner must be invoked from project root per `CLAUDE.md`).

</code_context>

<specifics>
## Specific Ideas

- Blank window for SETUP-04: the archetype's generated `App.java` and `primary.fxml` are sufficient — no need to build a real game screen yet
- The spec's UML appendices define exact method signatures; treat them as a contract, not a suggestion

</specifics>

<deferred>
## Deferred Ideas

- **GUI design** — Belongs to Phase 2 (FXML Layout + CSS). No UI decisions made here.
- **isFormableFrom check** — Considered for `checkGuessWord`; deferred. Phase 3's UI (letter button disable-on-click) makes a model-level formability check redundant. Revisit in Phase 3 if the spec appendix requires it.

</deferred>

---

*Phase: 1-Scaffold + Pure Java Model*
*Context gathered: 2026-05-12*
