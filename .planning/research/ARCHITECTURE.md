# Architecture Research — JavaFX TextTwist

**Project:** CS120 Lab06 TextTwist
**Researched:** 2026-05-12
**Confidence:** HIGH — JavaFX 21 APIs are stable and well-documented; patterns below are from the official JavaFX FXML programming guide and standard MVC idioms for this framework.

---

## Component Boundaries

| Component | Responsibility | What It Owns |
|-----------|---------------|--------------|
| `WordDictionary` | Pure data layer. Loads word lists from `twister_words/[n].txt`. Answers two queries: is this word valid, give me a random word of length n. | `Map<Integer, List<String>>` of loaded word lists; file-reading logic; the randomness source (`Random`). No JavaFX imports. |
| `TwistController` | Game-logic model. Owns all mutable game state. Exposes methods the FXML controller calls; never touches UI nodes directly. | `score`, `levelScore`, `level`, `targetWord` (all plain Java fields or JavaFX Properties — see MVC Wiring); `myDictionary` reference; `shuffleLetters()`, `checkGuessWord(String)`; the set of already-found words for the current episode. |
| `PrimaryController` (FXML controller) | View controller. Handles every user event, updates every UI node, drives the countdown timer. Holds a reference to `TwistController` and calls its methods. Never contains game-rule logic. | `@FXML`-injected nodes (letter `Button[]`, guess `Label[]`, found-words `ListView`, score/level/time `Label`); the `Timeline` timer; the `currentGuess` list tracking which letter slots are occupied. |
| `primary.fxml` | Declarative view. Defines node IDs and layout. Wires event handlers to `PrimaryController` methods via `onAction="#handleEnter"` etc. | Node structure only — no logic. |
| `styles.css` | Visual presentation. Controls all color, font, spacing, button states (`:disabled`, `.letter-used`). | Zero logic — pure CSS selectors. |
| `App` (`Application` subclass) | Entry point only. Loads the FXML, sets the Scene, shows the Stage. | The primary `Stage`; calls `FXMLLoader.load()`. |

**Hard rule:** `WordDictionary` and `TwistController` must have zero `import javafx.*` statements. This keeps them unit-testable with JUnit 4.12 without needing a JavaFX runtime.

---

## Data Flow

### Startup

```
App.start()
  → FXMLLoader.load("primary.fxml")
      → instantiates PrimaryController (default constructor)
      → injects @FXML nodes into controller fields
      → calls PrimaryController.initialize()
          → new TwistController()          // model created here
          → new WordDictionary()           // model creates this internally
          → startNewEpisode()              // sets up level 1
              → model.targetWord set
              → letter buttons built/labeled
              → timer started
```

### Per User Action (Enter button as canonical example)

```
User clicks Enter
  → PrimaryController.handleEnter()
      → word = buildWordFromGuess()          // reads currentGuess state
      → points = model.checkGuessWord(word)  // model does ALL validation
      → if points > 0:
            model.score += points            // model updates state
            foundWordsListView.getItems().add(word)   // view updates list
            scoreLabel.setText(...)          // view reads model.score
            clearGuess()                     // view resets button states
            if word.equals(model.targetWord):
                handleEpisodeEnd()           // view drives state machine
      → if points == 0:
            shake animation or flash on guessDisplay   // view only
```

### Timer Tick

```
Timeline keyframe fires every 1 second
  → PrimaryController.onTick()
      → timeRemaining--
      → timeLabel.setText(formatTime(timeRemaining))
      → if timeRemaining == 0: handleEpisodeEnd()
```

### Episode End

```
PrimaryController.handleEpisodeEnd()
  → timer.stop()
  → threshold = (int) Math.floor(0.25 * max * max * 10)   // computed in view controller
  → if model.levelScore >= threshold AND model.level < 8:
        model.level++
        startNewEpisode()
  → else if model.levelScore < threshold:
        showGameOver()
  → else if model.level == 8:
        showGameWon()
```

**Key principle:** The model (`TwistController`) never calls back into the view. Data flows one direction: user event → view controller → model method → return value → view controller updates UI. No listeners or callbacks are required for this assignment's scope.

---

## Timer Implementation

**Recommended: `javafx.animation.Timeline`**

Use `Timeline` with a `KeyFrame` that fires every 1 second on the JavaFX Application Thread. This is the correct choice for this project for three reasons:

1. It fires on the FX thread, so you can safely touch UI nodes (update the time `Label`) directly inside the handler — no `Platform.runLater()` needed.
2. It has `pause()`, `play()`, and `stop()` methods that map cleanly to the game state machine.
3. It is simpler than `ScheduledService` (which is designed for background work that produces a result) and more predictable than `AnimationTimer` (which fires every frame, not every second).

```java
// In PrimaryController
private Timeline countdownTimer;
private int timeRemaining;

private void startTimer() {
    timeRemaining = 120;
    countdownTimer = new Timeline(
        new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            timeLabel.setText(formatTime(timeRemaining));
            if (timeRemaining <= 0) {
                countdownTimer.stop();
                handleEpisodeEnd();
            }
        })
    );
    countdownTimer.setCycleCount(Timeline.INDEFINITE);
    countdownTimer.play();
}

private String formatTime(int totalSeconds) {
    return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
}
```

**Do not use `ScheduledService`** — it runs on a background thread and requires `Platform.runLater()` for every UI update, adding complexity with no benefit for a simple countdown.

**Do not use `AnimationTimer`** — it fires at 60 fps and requires manual delta-time accumulation to get 1-second intervals, which is error-prone and unnecessary.

---

## MVC Wiring

### How FXML Connects to the Controller

FXMLLoader performs this wiring automatically at load time:

1. The FXML file declares `fx:controller="cs120.lab06.PrimaryController"` as an attribute on the root node.
2. `FXMLLoader.load()` instantiates `PrimaryController` via its no-argument constructor.
3. Fields in `PrimaryController` annotated with `@FXML` are matched by their `fx:id` in the FXML and injected before `initialize()` is called.
4. `initialize()` is called once all `@FXML` fields are populated — this is where you create the model and set up the game.

```java
// PrimaryController.java
public class PrimaryController {

    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label timeLabel;
    @FXML private ListView<String> foundWordsList;
    @FXML private HBox letterButtonBox;   // container for dynamic letter buttons
    @FXML private HBox guessDisplayBox;   // container for guess letter labels

    private TwistController model;
    private Button[] letterButtons;
    private Label[] guessSlots;
    private List<Integer> currentGuessIndices;  // which letter indices are in the guess
    private String lastFoundWord = "";

    @FXML
    public void initialize() {
        model = new TwistController();
        currentGuessIndices = new ArrayList<>();
        startNewEpisode();
    }
    ...
}
```

### What TwistController Exposes (as plain Java — no JavaFX Properties needed)

The spec defines `TwistController` with plain Java fields. Keep them as plain types — the FXML controller reads them after each action and pushes updates to labels explicitly. This is simpler and matches the UML in the spec.

```java
// TwistController.java  (model — zero JavaFX imports)
public class TwistController {
    private int score;
    private int levelScore;
    private int level;
    private String targetWord;
    private WordDictionary myDictionary;
    private Set<String> foundWords;   // tracks words found this episode

    public TwistController() {
        myDictionary = new WordDictionary();
        score = 0;
        level = 1;
        foundWords = new HashSet<>();
    }

    public List<String> shuffleLetters() { ... }

    public int checkGuessWord(String guess) {
        if (!myDictionary.isValidWord(guess))    return 0;
        if (foundWords.contains(guess))          return 0;
        // must be formable from targetWord's letters
        if (!isFormableFrom(guess, targetWord))  return 0;
        int n = guess.length();
        foundWords.add(guess);
        int points = n * n * 10;
        levelScore += points;
        score += points;
        return points;
    }

    // Getters only — no setters exposed to the view
    public int getScore()      { return score; }
    public int getLevelScore() { return levelScore; }
    public int getLevel()      { return level; }
    public String getTargetWord() { return targetWord; }

    // Called by PrimaryController at episode start
    public void beginEpisode(int level) {
        this.level = level;
        this.levelScore = 0;
        foundWords.clear();
        int letterCount = level + 2;          // Level 1 = 3 letters
        targetWord = myDictionary.randomWord(letterCount);
    }
    ...
}
```

### Letter Button State Management

Do NOT use ObservableList binding for letter button disabled states — it is over-engineering for this assignment and introduces complexity the spec does not require.

Use a simple parallel `boolean[] letterUsed` array in `PrimaryController`. When a letter button is clicked:

```java
@FXML
public void handleLetterClick(ActionEvent e) {
    Button clicked = (Button) e.getSource();
    int idx = (int) clicked.getUserData();     // set at button creation time
    if (letterUsed[idx]) return;

    letterUsed[idx] = true;
    clicked.setDisable(true);                  // CSS :disabled styles it visually
    currentGuessIndices.add(idx);
    updateGuessDisplay();
}
```

Build the letter buttons dynamically in `startNewEpisode()` — do not hardcode 10 buttons in FXML, because the count varies by level (3 to 10 letters).

```java
private void startNewEpisode() {
    model.beginEpisode(model.getLevel());
    List<String> shuffled = model.shuffleLetters();

    letterButtonBox.getChildren().clear();
    letterButtons = new Button[shuffled.size()];
    letterUsed = new boolean[shuffled.size()];

    for (int i = 0; i < shuffled.size(); i++) {
        Button btn = new Button(shuffled.get(i));
        btn.setUserData(i);
        btn.setOnAction(this::handleLetterClick);
        btn.getStyleClass().add("letter-btn");   // CSS hook
        letterButtons[i] = btn;
        letterButtonBox.getChildren().add(btn);
    }

    guessSlots = new Label[10];
    guessDisplayBox.getChildren().clear();
    for (int i = 0; i < 10; i++) {
        Label lbl = new Label("_");
        lbl.getStyleClass().add("guess-slot");
        guessSlots[i] = lbl;
        guessDisplayBox.getChildren().add(lbl);
    }

    currentGuessIndices.clear();
    foundWordsList.getItems().clear();
    updateScoreDisplay();
    startTimer();
}
```

### Clear / Twist / Last Word Buttons

- **Clear:** set all `letterUsed[i] = false`, all `letterButtons[i].setDisable(false)`, `currentGuessIndices.clear()`, reset guess display labels to `"_"`.
- **Twist:** call `Clear` logic, then call `model.shuffleLetters()` and relabel each button from the new shuffle order. Do NOT rebuild buttons — just update `setText()`.
- **Last Word:** populate `currentGuessIndices` from `lastFoundWord`'s characters, re-match each to a free letter slot, disable those buttons, update guess display. Store `lastFoundWord` in `PrimaryController` after each successful find.

---

## Game State Machine

Keep state as an enum in `PrimaryController` (not in the model — the model has no concept of UI phases).

```java
private enum GameState { PLAYING, EPISODE_END, GAME_OVER, GAME_WON }
private GameState gameState = GameState.PLAYING;
```

Transitions:

```
PLAYING
  → EPISODE_END   when timer hits 0 OR target word is guessed
  → (evaluate threshold)
      → PLAYING       if score >= threshold AND level < 8  (level increments, new episode starts)
      → GAME_OVER     if score < threshold
      → GAME_WON      if level == 8 AND target word guessed OR timer expired with passing score
```

In `EPISODE_END`, `GAME_OVER`, and `GAME_WON` states, all buttons should be disabled and an overlay/dialog shown. Use a semi-transparent `VBox` overlay stacked in the root `StackPane`, toggled visible/invisible, to display the end-of-episode result without opening a new window.

---

## Build Order

Build in this exact dependency chain — each item can only be coded after its prerequisites exist:

**1. `WordDictionary` (no dependencies)**
- Reads `twister_words/[n].txt`
- `isValidWord(String)`, `randomWord(int n)`
- Write JUnit tests immediately — this class is pure Java and fully testable before JavaFX exists

**2. `TwistController` (depends on WordDictionary)**
- Fields: `score`, `levelScore`, `level`, `targetWord`, `myDictionary`
- `shuffleLetters()`, `checkGuessWord(String)`, `beginEpisode(int)`
- Can be partially tested with JUnit (verify scoring formula, verify isFormableFrom logic)

**3. `primary.fxml` skeleton (no code dependencies)**
- Root `StackPane` containing a `BorderPane` (game area) and a `VBox` overlay (hidden initially)
- Static containers: `HBox` for letters, `HBox` for guess display, `ListView`, `Label` row for score/level/time, `HBox` for action buttons (Twist, Enter, Last Word, Clear)
- Set `fx:controller="cs120.lab06.PrimaryController"` and assign `fx:id` to all containers and labels the controller needs

**4. `PrimaryController` stub (depends on FXML skeleton)**
- `@FXML` field declarations matching the FXML `fx:id` names
- `initialize()` that creates the model and calls `startNewEpisode()`
- Wire one button (Enter) end-to-end to verify the full stack before adding remaining handlers

**5. `styles.css` (can start alongside step 3)**
- Letter button normal and `:disabled` states
- Guess slot labels
- Score panel layout
- Overlay panel styling

**6. Complete `PrimaryController` (depends on steps 2–5 working)**
- Add all event handlers
- Add `Timeline` timer
- Add state machine
- Add episode-end evaluation logic

**7. Integration test / polish**
- Verify level progression 1→8
- Verify scoring thresholds at each level
- Verify Twist, Last Word, Clear edge cases

---

## Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| Plain Java fields in `TwistController`, not JavaFX Properties | Keeps the model free of JavaFX imports, making JUnit tests possible without a running FX runtime. The view controller explicitly refreshes labels after each action — sufficient for this assignment's scope. |
| Dynamic button creation in code, not 10 fixed buttons in FXML | Letter count varies 3–10 by level. Static FXML buttons would require hiding/showing buttons and tracking which are "active," which is more complex than rebuilding. |
| `boolean[] letterUsed` parallel array, not ObservableList binding | Binding is idiomatic JavaFX but overkill here. A plain array matches the mental model of "which slots are used" and is easier to debug. |
| `Timeline` for countdown, not `ScheduledService` or `AnimationTimer` | `Timeline` fires on the FX thread (safe for UI updates), has clean play/pause/stop lifecycle, and is the standard idiom for periodic animation/timer tasks in JavaFX. |
| Game state enum in `PrimaryController`, not in model | The model has no concept of "show a dialog." State machine transitions trigger UI changes, so they belong in the view controller. The model just tracks score/level numbers. |
| `StackPane` root with overlay `VBox` | Avoids opening modal dialogs (which complicate the JavaFX lifecycle). The overlay can be shown/hidden with `setVisible(true/false)` and styled entirely via CSS. |

---

## Sources

- JavaFX 21 FXML Introduction (openjfx.io) — FXMLLoader lifecycle, `@FXML`, `initialize()` contract
- JavaFX 21 `javafx.animation.Timeline` API — KeyFrame, setCycleCount, play/stop
- CS120 Lab06 spec (`textTwist.pdf`) — UML for WordDictionary and TwistController, scoring formulas, level structure
- PROJECT.md — requirement list, constraints, key decisions
- **Confidence:** HIGH for all JavaFX API claims (stable since JavaFX 8, unchanged in 21); HIGH for MVC separation pattern; MEDIUM for overlay-vs-dialog choice (either works, overlay is simpler).
