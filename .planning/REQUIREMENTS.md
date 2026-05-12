# Requirements — TextTwist (CS120 Lab06)

## v1 Requirements

### Setup (SETUP)

- [ ] **SETUP-01**: Maven project is generated with openjfx javafx-archetype-fxml, group id cs120, artifact id lab06, JavaFX 21
- [ ] **SETUP-02**: pom.xml includes javafx-controls, javafx-fxml, and JUnit 4.12 dependencies; Surefire pinned to 3.1.2
- [ ] **SETUP-03**: module-info.java is deleted; project runs as unnamed module
- [ ] **SETUP-04**: `mvn javafx:run` launches a blank JavaFX window from the project root
- [ ] **SETUP-05**: `twister_words/` folder (3.txt–10.txt) is present at the project root (not in src/)

### Dictionary (DICT)

- [ ] **DICT-01**: `WordDictionary` class accepts a folder path in its constructor
- [ ] **DICT-02**: `WordDictionary.initialize()` reads the correct word file (named by letter count), parses word count from first line, loads all words into a collection
- [ ] **DICT-03**: `WordDictionary.isValidWord(String wrd)` returns true if the word exists in the loaded collection, false otherwise
- [ ] **DICT-04**: `WordDictionary.randomWord(int n)` returns a random word of length n from the collection
- [ ] **DICT-05**: JUnit tests verify `isValidWord` with known valid and invalid words
- [ ] **DICT-06**: JUnit tests verify `randomWord` returns a non-null string of the correct length
- [ ] **DICT-07**: `@Before` test fixture asserts the dictionary file exists so failures are actionable

### Game Model (MODEL)

- [ ] **MODEL-01**: `TwistController` class holds `score`, `levelScore`, `level`, `targetWord`, `myDictionary` instance variables
- [ ] **MODEL-02**: `TwistController.shuffleLetters()` returns a List<String> of the target word's letters in shuffled order
- [ ] **MODEL-03**: `TwistController.checkGuessWord(String wrd)` validates the word via `myDictionary.isValidWord()` and returns `n*n*10` points for an n-letter word, or 0 if invalid
- [ ] **MODEL-04**: `TwistController` tracks which words have already been guessed; duplicate guesses return 0 points
- [ ] **MODEL-05**: `TwistController` has zero JavaFX imports (pure Java — required for JUnit testability)
- [ ] **MODEL-06**: Level 1 uses a 3-letter word set; Level n uses n+2 letters (Level 8 = 10 letters)

### GUI Layout (GUI)

- [ ] **GUI-01**: FXML layout file defines the game screen with containers for all UI regions
- [ ] **GUI-02**: External CSS file controls all visual styling; no inline `setStyle()` calls
- [ ] **GUI-03**: `ListView<String>` bound to `ObservableList<String>` displays successfully guessed words (scrollable)
- [ ] **GUI-04**: Guess word display area shows selected letters in selection order
- [ ] **GUI-05**: Score, level, and time-remaining labels are present in a dedicated display pane
- [ ] **GUI-06**: Twist, Enter, Last Word, and Clear buttons are present

### Game Controls (CTRL)

- [ ] **CTRL-01**: Letter buttons are created dynamically at episode start (one per letter in the target word)
- [ ] **CTRL-02**: Each letter button appends its letter to the guess display and disables itself when clicked
- [ ] **CTRL-03**: Clear button clears the guess display and re-enables all letter buttons
- [ ] **CTRL-04**: Twist button clears the guess word and reshuffles the letter buttons (same letters, new random order)
- [ ] **CTRL-05**: Enter button checks the guess word; awards points and adds to found-words list if valid; shows rejection feedback if invalid
- [ ] **CTRL-06**: Last Word button repopulates the guess display with the same letters as the last successful guess (in the same order); disabled until first successful guess per episode
- [ ] **CTRL-07**: Letter buttons track used state by button index (not letter character) to handle duplicate letters correctly

### Timer (TIMER)

- [ ] **TIMER-01**: Each episode starts a 120-second `javafx.animation.Timeline` countdown
- [ ] **TIMER-02**: Time remaining is displayed as mm:ss and decrements once per second
- [ ] **TIMER-03**: Timer label CSS class changes at ≤20 seconds remaining (warning state)
- [ ] **TIMER-04**: Timer label CSS class changes at ≤10 seconds remaining (critical state)
- [ ] **TIMER-05**: Timer expiry routes to a single `endEpisode()` method

### Game Progression (PROG)

- [ ] **PROG-01**: `endEpisode()` is the single entry point for both timer expiry and target-word-guessed events
- [ ] **PROG-02**: End-of-episode evaluation: if levelScore ≥ floor(0.25 × max² × 10), player may advance or continue; otherwise game over
- [ ] **PROG-03**: Guessing the target word (all letters) ends the episode immediately and advances to next level
- [ ] **PROG-04**: Advancing a level increments letter count by 1 and loads a new target word from the larger dictionary
- [ ] **PROG-05**: If minimum score is not met, the game displays game-over state
- [ ] **PROG-06**: Game is won when Level 8 (10-letter words) is completed
- [ ] **PROG-07**: Scoring uses the length of the guessed word (n), not the target word length: `n * n * 10`
- [ ] **PROG-08**: Score (total) and levelScore (per-episode) are both tracked and displayed

### Visual Feedback (VFX)

- [ ] **VFX-01**: Valid word entry triggers a visible positive feedback state on the guess display (green highlight or flash)
- [ ] **VFX-02**: Invalid word entry triggers a visible rejection feedback state (red highlight or flash)
- [ ] **VFX-03**: End-of-episode result is shown (advance / game over / game won) with a clear overlay or dialog

---

## v2 Requirements (Deferred)

- Keyboard input support for letter selection
- Sound effects (requires javafx-media)
- Per-letter animation on Twist
- ListCell coloring by word length in found-words list
- Hint system

---

## Out of Scope

- Multiplayer — single-player class assignment
- Online/network dictionary — local twister_words/ files only
- Save/load game state — not in spec
- Custom dictionary import — fixed file structure required by spec
- Theme switcher — one coherent external CSS style required

---

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| SETUP-01 | Phase 1 | Pending |
| SETUP-02 | Phase 1 | Pending |
| SETUP-03 | Phase 1 | Pending |
| SETUP-04 | Phase 1 | Pending |
| SETUP-05 | Phase 1 | Pending |
| DICT-01 | Phase 1 | Pending |
| DICT-02 | Phase 1 | Pending |
| DICT-03 | Phase 1 | Pending |
| DICT-04 | Phase 1 | Pending |
| DICT-05 | Phase 1 | Pending |
| DICT-06 | Phase 1 | Pending |
| DICT-07 | Phase 1 | Pending |
| MODEL-01 | Phase 1 | Pending |
| MODEL-02 | Phase 1 | Pending |
| MODEL-03 | Phase 1 | Pending |
| MODEL-04 | Phase 1 | Pending |
| MODEL-05 | Phase 1 | Pending |
| MODEL-06 | Phase 1 | Pending |
| GUI-01 | Phase 2 | Pending |
| GUI-02 | Phase 2 | Pending |
| GUI-03 | Phase 2 | Pending |
| GUI-04 | Phase 2 | Pending |
| GUI-05 | Phase 2 | Pending |
| GUI-06 | Phase 2 | Pending |
| CTRL-01 | Phase 3 | Pending |
| CTRL-02 | Phase 3 | Pending |
| CTRL-03 | Phase 3 | Pending |
| CTRL-04 | Phase 3 | Pending |
| CTRL-05 | Phase 3 | Pending |
| CTRL-06 | Phase 3 | Pending |
| CTRL-07 | Phase 3 | Pending |
| TIMER-01 | Phase 3 | Pending |
| TIMER-02 | Phase 3 | Pending |
| TIMER-03 | Phase 3 | Pending |
| TIMER-04 | Phase 3 | Pending |
| TIMER-05 | Phase 3 | Pending |
| PROG-01 | Phase 3 | Pending |
| PROG-03 | Phase 3 | Pending |
| PROG-07 | Phase 3 | Pending |
| PROG-08 | Phase 3 | Pending |
| PROG-02 | Phase 4 | Pending |
| PROG-04 | Phase 4 | Pending |
| PROG-05 | Phase 4 | Pending |
| PROG-06 | Phase 4 | Pending |
| VFX-01 | Phase 4 | Pending |
| VFX-02 | Phase 4 | Pending |
| VFX-03 | Phase 4 | Pending |
