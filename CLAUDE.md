# TextTwist — CS120 Lab06

## Project

A JavaFX 21 word game built with Maven. Players unscramble letters to form words within a 120-second timer, progressing through 8 levels (3–10 letter words).

**Spec:** `textTwist.pdf` (lab06, cs120)
**Dictionary:** `twister_words/` at project root — files named by word length (3.txt–10.txt), first line = word count

## GSD Workflow

This project uses Get Shit Done (GSD) for structured execution.

- Planning artifacts live in `.planning/`
- Current roadmap: `.planning/ROADMAP.md` (4 phases)
- Requirements: `.planning/REQUIREMENTS.md`
- State: `.planning/STATE.md`

**To start work:** `/gsd-discuss-phase 1` then `/gsd-plan-phase 1`

## Critical Implementation Rules

1. **Never use `java.util.Timer`** — use `javafx.animation.Timeline` for the countdown; it fires on the FX thread
2. **Dictionary files via `new File()`** — `twister_words/` is at project root, not on the classpath; `getResourceAsStream` will return null
3. **`TwistController` must have zero JavaFX imports** — required for JUnit 4.12 testability
4. **Track letter button state by index** — not by character value; duplicate letters require separate button objects
5. **Single `endEpisode()` entry point** — both timer expiry and target-word-guessed must route through it
6. **All styling via external CSS** — no inline `setStyle()` calls; the spec grades on "impeccable style"
7. **Surefire 3.1.2** — 2.x silently skips JUnit 4 tests on JDK 21
8. **Delete `module-info.java`** — JUnit 4.12 is not module-aware; unnamed module path works fine

## Architecture

```
Model (pure Java, no JavaFX):
  WordDictionary     — loads word files, isValidWord(), randomWord()
  TwistController    — score/level/targetWord state, checkGuessWord(), shuffleLetters()

View (JavaFX):
  twist.fxml         — FXML layout (fx:id contracts locked in Phase 2)
  styles.css         — all visual states including .letter-btn, .timer-warning, .timer-critical

Controller (JavaFX):
  PrimaryController  — @FXML wiring, Timeline timer, GameState enum, letter button management
```

## Scoring

- n-letter word = n × n × 10 points (e.g., 5-letter word = 250 pts)
- Episode threshold to continue = floor(0.25 × max² × 10) where max = letter count
- Level n uses n+2 letters; Level 1 = 3 letters, Level 8 = 10 letters

## Run / Test

```bash
mvn javafx:run    # must run from project root (dictionary path depends on working dir)
mvn test          # JUnit 4.12 tests for WordDictionary
```
