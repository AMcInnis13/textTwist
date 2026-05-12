# Research Summary — TextTwist (CS120 Lab06)

**Synthesized:** 2026-05-12
**Sources:** STACK.md, FEATURES.md, ARCHITECTURE.md, PITFALLS.md
**Overall confidence:** HIGH (stack is fully spec-mandated; architecture patterns are stable JavaFX 21 idioms)

---

## Definitive Stack

| Component | Choice | Version | Non-Negotiable? |
|-----------|--------|---------|-----------------|
| Language | Java | 21 (LTS) | Yes — spec-mandated |
| UI framework | JavaFX | 21 | Yes — spec-mandated |
| Build tool | Maven | 3.9.x | Yes — spec-mandated |
| Project archetype | javafx-archetype-fxml (openjfx) | 0.0.6 | Yes — spec-mandated |
| JavaFX Maven plugin | javafx-maven-plugin (openjfx) | 0.0.8 | Minimum — do not go lower |
| Testing | JUnit | 4.12 | Yes — spec-mandated |
| Surefire plugin | maven-surefire-plugin | 3.1.2 | Use 3.1.2, NOT 2.22.2 — 2.x silently skips tests on JDK 21 |
| Styling | External CSS file | — | Yes — spec requires "impeccable style" via external CSS |
| Dictionary I/O | Java standard library (Scanner / File) | — | No external dep; files are at project root, not on classpath |
| module-info.java | DELETE IT | — | Drop it — JUnit 4.12 and module boundaries cause silent test-skip bugs; unnamed module path works fine for a lab |

**Run command:** `mvn javafx:run` (always from project root; never `java -jar`)
**Test command:** `mvn test`

---

## Build Order

1. **Project scaffold — get a blank window running**
   Generate from archetype (`javafx-archetype-fxml 0.0.6`). Immediately delete `module-info.java`. Pin Surefire to 3.1.2. Verify `javafx-controls` AND `javafx-fxml` both present. Run `mvn javafx:run`. Gate: blank window appears without errors.

2. **`WordDictionary` — pure Java, no JavaFX imports**
   Load files with `new File("twister_words/" + n + ".txt")`. Implement `isValidWord(String)` and `randomWord(int n)`. Write `WordDictionaryTest.java` immediately with `@Before` existence check. Gate: `mvn test` shows passing tests.

3. **`TwistController` — game model, zero JavaFX imports**
   Fields: `score`, `levelScore`, `level`, `targetWord`, `myDictionary`, `Set<String> foundWords`. Methods: `beginEpisode(int)`, `checkGuessWord(String)`, `shuffleLetters()`, getters only. Gate: scoring logic unit-testable.

4. **`twist.fxml` skeleton — layout only, no logic**
   Root `StackPane` containing a `BorderPane` (game) and hidden `VBox` (episode-end overlay). Static containers with finalized `fx:id` names. Letter buttons NOT in FXML — built dynamically in code. Gate: FXML opens in SceneBuilder without errors.

5. **`styles.css` — alongside step 4**
   At `src/main/resources/css/styles.css`. Loaded via `getClass().getResource("/css/styles.css").toExternalForm()`. Must cover: `.letter-btn` normal + `:disabled`, `.guess-slot`, `#timeLabel` + `.timer-warning` + `.timer-critical`, overlay panel. All properties use `-fx-` prefix. Gate: CSS loads; buttons visually differ when disabled.

6. **`PrimaryController` stub — wire Enter button end-to-end first**
   `@FXML` fields for every `fx:id`. `initialize()` creates model, calls `startNewEpisode()`. `startNewEpisode()` dynamically builds letter buttons (uses `btn.setUserData(i)` for index; `boolean[] letterUsed` parallel array). Wire Enter first, verify full cycle, then add other handlers. Gate: valid/invalid word cycle works.

7. **Complete `PrimaryController` — all handlers, timer, state machine**
   `Timeline` countdown (fires on FX thread — never `java.util.Timer`). Timer CSS class toggling (remove old class before adding new). Clear, Twist (= Clear + relabel), Last Word (= Clear + replay index sequence) handlers. Single `endEpisode()` method called by BOTH timer expiry AND target-word-guessed. `GameState` enum (`PLAYING`, `EPISODE_END`, `GAME_OVER`, `GAME_WON`) in `PrimaryController`. Gate: full level 1 playable end-to-end.

8. **Integration pass — all 8 levels**
   Verify level 1 (3 letters) through level 8 (10 letters). Verify threshold formula at each level. Verify advance / retry / game-won paths. Gate: full game completable to win state.

---

## Critical Decisions

| Decision | Correct Choice | Why Wrong Choice Breaks Things |
|----------|---------------|-------------------------------|
| Surefire version | 3.1.2 | 2.22.2 silently skips all JUnit 4 tests on JDK 17+; `mvn test` reports 0 tests run |
| module-info.java | Delete it | JUnit 4 is not module-aware; keeps `InaccessibleObjectException` in tests |
| Timer implementation | `javafx.animation.Timeline` | `java.util.Timer` runs off FX thread; `label.setText()` throws `IllegalStateException` |
| Dictionary file loading | `new File("twister_words/" + n + ".txt")` | `getResourceAsStream` returns null — files are at project root, not on classpath |
| Letter button state tracking | `boolean[] letterUsed` indexed by position | Tracking by character value breaks on duplicate letters |
| Letter button creation | Dynamically in `startNewEpisode()` | Static FXML buttons require hiding/showing complexity |
| Model-view separation | `TwistController` has zero JavaFX imports | If model imports JavaFX, JUnit tests require a running FX runtime |
| Twist button behavior | Twist = Clear + relabel buttons | Twisting without clearing leaves display out of sync with button state |
| End-of-episode routing | Single `endEpisode()` for both paths | Separate handlers means one path silently skips the threshold check |
| Scoring variable | `guessWord.length() * guessWord.length() * 10` | Using `targetWord.length()` awards wrong points for sub-word guesses |

---

## Top Pitfalls

| Pitfall | When It Hits | Prevention |
|---------|-------------|------------|
| Surefire 2.x silently skips all tests | Phase 1, first `mvn test` | Pin Surefire to 3.1.2; run `mvn test -X` if count is 0 |
| Dictionary `FileNotFoundException` outside project root | Phase 1 and at submission | `new File("twister_words/" + n + ".txt")`; `@Before` existence assertion |
| `IllegalStateException: Not on FX application thread` | Phase 2, timer runs | Use `Timeline` only; wrap background UI calls in `Platform.runLater()` |
| Letter button state desync after Clear / Last Word | Phase 3, manual testing | Track by index; Clear re-enables ALL; Last Word calls Clear first then replays index sequence |
| NPE on Last Word before first guess | Phase 3, first click | Initialize Last Word button as disabled; enable only after first successful word |
| Duplicate words accepted | Phase 3, repeated submission | `foundWords.contains(guess)` check in `checkGuessWord()` before awarding points |
| Level off-by-one loading wrong word file | Phase 4, level 1 startup | `letterCount = level + 2` explicitly; assert files `3.txt`–`10.txt` exist at startup |
| Episode threshold bypassed when target word guessed | Phase 4, happy-path test | Route BOTH timer expiry and target-word-guessed through single `endEpisode()` |
| CSS properties ignored — missing `-fx-` prefix | Phase 5, first style run | All JavaFX CSS uses `-fx-background-color` not `background-color` |
| CSS class accumulation on timer label | Phase 7, timer running | Remove old class before adding new |

---

## Feature Scope

### Must Have (table stakes — graded)

| Feature | Notes |
|---------|-------|
| Letter buttons disable on selection, re-enable on Clear | Core mechanic |
| Guess display updates on each click, clears on Clear/Enter/Twist | Central feedback loop |
| Correct word: green flash or highlight | Without this, Enter feels broken |
| Incorrect word: red flash | Silent failure looks like a bug |
| Timer mm:ss, 2:00 to 0:00 | Spec-mandated |
| Timer turns yellow ≤20s, red ≤10s | Universal countdown UX; likely in rubric |
| Found-words ListView updates immediately | `ObservableList` + `ListView` handles automatically |
| Score and level labels update immediately | Delayed updates look like scoring bugs |
| Twist reshuffles buttons visibly | Must look different or players think it failed |
| Last Word disabled until first successful guess | NPE prevention + correct UX |
| All 8 levels functional (3–10 letters) | Core progression mechanic |
| End-of-episode evaluation (advance / game over / game won) | Without this, board changes are inexplicable |
| Scoring: `n * n * 10` per guessed word length | Spec-mandated |
| Threshold: `floor(0.25 * max * max * 10)` | Spec-mandated advancement condition |
| External CSS with distinct visual states | Spec explicitly grades "impeccable style" |
| Duplicate word rejection | Implied by spec; trivial to add in `checkGuessWord` |

### Skip (out of scope)

| Feature | Why |
|---------|-----|
| Keyboard input for letter selection | Spec uses buttons; adds scope |
| Hint system | Not in spec |
| Pause button | Not in spec; adds state machine complexity |
| Animated tile-flip / 3D effects | Disproportionate complexity |
| Sound effects | Requires `javafx-media` dep; no grade value |
| Online dictionary validation | Spec uses local files |
| Save/load game state | PROJECT.md explicitly excludes |

---

## Open Questions

| Question | Recommendation |
|----------|---------------|
| Is deleting `module-info.java` acceptable per spec? | Delete it — spec mandates the archetype, not module-info. Confirm with instructor if uncertain. |
| Does the grader run `mvn javafx:run` from the project root? | Yes — document this in a README. Dictionary path depends on Maven working directory. |
| Is `isFormableFrom` check required (words must use only available letters)? | Yes — core mechanic. `checkGuessWord` must verify the guess can be formed from target word's letters. |
| Win condition at Level 8: immediate or apply threshold? | Route through `endEpisode()` consistently. Verify exact condition in spec PDF. |
