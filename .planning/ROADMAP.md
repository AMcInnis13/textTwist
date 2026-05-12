# ROADMAP.md — TextTwist (CS120 Lab06)

## Milestone 1: Playable TextTwist Game

### Phases

- [ ] **Phase 1: Scaffold + Pure Java Model** - Maven project runs; dictionary and game model fully unit-tested
- [ ] **Phase 2: FXML Layout + CSS** - Game screen renders with all UI regions styled; no logic yet
- [ ] **Phase 3: Core Game Loop** - Player can select letters, submit words, earn points, and play against the timer
- [ ] **Phase 4: Level Progression + Polish** - All 8 levels function; game-over, win, and visual feedback states complete

---

## Phase Details

### Phase 1: Scaffold + Pure Java Model
**Goal:** Maven project builds and launches; WordDictionary and TwistController are fully implemented and unit-tested with zero JavaFX imports
**Mode:** mvp
**Depends on:** Nothing
**Requirements:** SETUP-01, SETUP-02, SETUP-03, SETUP-04, SETUP-05, DICT-01, DICT-02, DICT-03, DICT-04, DICT-05, DICT-06, DICT-07, MODEL-01, MODEL-02, MODEL-03, MODEL-04, MODEL-05, MODEL-06
**Success Criteria**:
1. `mvn javafx:run` from the project root opens a blank JavaFX window with no errors in the console
2. `mvn test` runs and reports all WordDictionary tests passing (isValidWord and randomWord verified; @Before existence check fires on missing file)
3. TwistController.checkGuessWord() returns correct points (n*n*10) for valid words, 0 for invalid/duplicate words — verifiable via unit test or manual inspection with no FX runtime
4. twister_words/3.txt through 10.txt are present at the project root and WordDictionary loads the correct file for a given letter count
**Plans:** 3 plans

Plans:
- [ ] 01-01-PLAN.md — Maven scaffold: archetype generate, delete module-info.java, pin Surefire 3.1.2, verify blank window opens
- [ ] 01-02-PLAN.md — WordDictionary implementation + JUnit 4 tests (isValidWord, randomWord, @Before file check)
- [ ] 01-03-PLAN.md — TwistController implementation + JUnit 4 tests (checkGuessWord scoring, duplicate detection, shuffleLetters)

### Phase 2: FXML Layout + CSS
**Goal:** The game screen FXML renders all UI regions correctly in SceneBuilder and at runtime, with all visual states driven entirely by the external CSS file
**Mode:** mvp
**Depends on:** Phase 1
**Requirements:** GUI-01, GUI-02, GUI-03, GUI-04, GUI-05, GUI-06
**Success Criteria**:
1. `mvn javafx:run` displays the game screen with visible regions for: letter buttons area, guess word display, found-words list, score/level/time panel, and all four control buttons (Twist, Enter, Last Word, Clear)
2. No inline setStyle() calls exist in any Java source file; all visual styling is in styles.css and applied via CSS class selectors
3. The found-words ListView is bound to an ObservableList and scrolls when entries are added manually in a test run
4. SceneBuilder opens twist.fxml without errors and all fx:id references resolve
**Plans:** TBD
**UI hint**: yes

### Phase 3: Core Game Loop
**Goal:** A player can click letter buttons, build a guess, submit it, earn points, use Clear/Twist/Last Word, and race against the 120-second timer — all working correctly for a single episode
**Mode:** mvp
**Depends on:** Phase 2
**Requirements:** CTRL-01, CTRL-02, CTRL-03, CTRL-04, CTRL-05, CTRL-06, CTRL-07, TIMER-01, TIMER-02, TIMER-03, TIMER-04, TIMER-05, PROG-01, PROG-03, PROG-07, PROG-08
**Success Criteria**:
1. Clicking letter buttons appends letters to the guess display in order and disables each button; clicking Clear returns all buttons to enabled and clears the display; Twist reshuffles the button labels into a new visible order while clearing the guess
2. Submitting a valid word via Enter adds it to the found-words list, updates the score display immediately (n*n*10 points), and prevents the same word from being submitted again; submitting an invalid word shows rejection feedback and does not alter score
3. The timer counts down from 2:00 in mm:ss format; the timer label visually changes at ≤20 seconds (warning) and again at ≤10 seconds (critical); timer expiry routes to endEpisode()
4. Guessing the target word (all letters) immediately ends the episode via the same endEpisode() entry point; both score (cumulative) and levelScore (per-episode) update correctly throughout
**Plans:** TBD
**UI hint**: yes

### Phase 4: Level Progression + Polish
**Goal:** All 8 levels are completable from level 1 (3-letter) through level 8 (10-letter); end-of-episode evaluation (advance/game-over/win) works correctly; valid/invalid feedback is visually distinct
**Mode:** mvp
**Depends on:** Phase 3
**Requirements:** PROG-02, PROG-04, PROG-05, PROG-06, VFX-01, VFX-02, VFX-03
**Success Criteria**:
1. At episode end, if levelScore >= floor(0.25 * max^2 * 10) the player advances to the next level with an increased letter count and new target word; if not, game-over state is displayed
2. Completing Level 8 (10-letter words) triggers the game-won state with a visible overlay or dialog — distinct from the game-over state
3. Valid word entry produces a visible green highlight or flash on the guess display; invalid word entry produces a visible red highlight or flash — both are perceptible without reading any text
4. The end-of-episode result (advance / game over / game won) is shown as a clear overlay or dialog before the board resets or stops
**Plans:** TBD
**UI hint**: yes

---

## Progress Table

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Scaffold + Pure Java Model | 0/3 | Not started | - |
| 2. FXML Layout + CSS | 0/? | Not started | - |
| 3. Core Game Loop | 0/? | Not started | - |
| 4. Level Progression + Polish | 0/? | Not started | - |
