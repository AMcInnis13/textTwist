# TextTwist (CS120 Lab06)

## What This Is

A JavaFX word game (CS120 Lab06) where the player is shown a scrambled set of letters and must form as many valid words as possible within a 2-minute timer. Letters are selected by clicking buttons; points are awarded per word length. The game progresses through 8 levels, growing from 3-letter to 10-letter word sets.

## Core Value

A fully playable game loop — the player can select letters, submit words, earn points, and advance through levels before time runs out.

## Requirements

### Validated

*(Validated in Phase 1: Scaffold + Pure Java Model — 2026-05-12)*
- [x] Maven project configured with openjfx fxml archetype, group id cs120, artifact id lab06, JavaFX 21
- [x] JUnit 4.12 test dependency in pom.xml
- [x] WordDictionary class reads twister_words/[n].txt files (first line = word count, remaining lines = uppercase words)
- [x] WordDictionary.isValidWord(String) validates a word against the dictionary for the current letter count
- [x] WordDictionary.randomWord(int n) returns a random word of length n from the dictionary
- [x] JUnit tests for WordDictionary (isValidWord and randomWord)
- [x] TwistController class tracks: score (int), levelScore (int), level (int), targetWord (String), myDictionary (WordDictionary)
- [x] TwistController.shuffleLetters() returns scrambled letters of the target word as List<String>
- [x] TwistController.checkGuessWord(String) validates and returns points (n*n*10) or 0

*(Validated in Phase 2: FXML Layout + CSS — 2026-05-12)*
- [x] JavaFX MVC GUI with FXML layout and external CSS file
- [x] Guess word display showing letters in the order selected (up to 10)
- [x] Scrollable list of successfully found words (no re-use allowed)
- [x] Score / level / time display panel

*(Validated in Phase 3: Core Game Loop — 2026-05-12)*
- [x] Letter buttons (one per letter in current set), disabled when used, re-enabled on new guess
- [x] Twist button: clears guess word, reshuffles letter set
- [x] Enter button: validates guess word, awards points; submitting the target word ends episode
- [x] Last Word button: repopulates guess with previous successful guess
- [x] Clear button: clears guess word, returns all letters to the letter set
- [x] 120-second countdown timer per episode, displayed as mm:ss
- [x] Scoring: n*n*10 per valid n-letter word; cumulative and per-episode scores tracked

### Active
- [ ] Level 1 starts with 3-letter word set; Level n requires n+2 letters (up to Level 8 = 10 letters)
- [ ] End-of-episode evaluation: advance level / stay at level / game over based on score threshold
- [ ] Minimum score to continue: floor(0.25 * max² * 10) where max = letter count
- [ ] Game won when Level 8 (10-letter words) is completed
- [ ] End-of-episode overlay/dialog showing result before board resets or stops

### Out of Scope

- Multiplayer or online leaderboards — single-player class assignment
- Custom dictionary import — twister_words/ folder is the fixed source
- Save/load game state — not specified in lab requirements

## Context

- CS120 Lab06 graded assignment; must follow the spec in textTwist.pdf
- Dictionary files located at twister_words/ (project root, not src/); files named 3.txt–10.txt
- Word files: first line is the word count, subsequent lines are uppercase words
- MVC architecture enforced: model (WordDictionary, TwistController), view (FXML), controller (JavaFX controller class)
- The PDF includes UML for WordDictionary (Appendix A) and TwistController (Appendix B) — implementation must match those signatures

## Constraints

- **Tech stack**: Java + JavaFX 21, Maven (openjfx fxml archetype) — mandated by spec
- **Testing**: JUnit 4.12 — specified in pom.xml dependency
- **CSS**: Styling must be controlled by an external CSS file ("impeccable style")
- **Dictionary location**: twister_words/ must live at project root, not inside src/

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Java + JavaFX 21 over web/Python | Class spec mandates it | — Pending |
| Separate dictionary file per word length | Files named by length (3.txt–10.txt), first line = count | — Pending |
| Scoring formula: n*n*10 | Spec: n-letter word = n*10 pts per letter = n² * 10 total | — Pending |
| Minimum threshold: floor(0.25 * max² * 10) | Spec formula for staying alive per episode | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition:**
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone:**
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-05-12 — Phase 3 complete; Phase 4 (Level Progression + Polish) is next*
