# Phase 4: Level Progression + Polish - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-12
**Phase:** 4-Level Progression + Polish
**Areas discussed:** Result overlay style, Level advance flow, Restart behavior

---

## Result Overlay Style

| Option | Description | Selected |
|--------|-------------|----------|
| JavaFX Alert dialog | 3 lines of code, blocks until dismissed, looks like a native OS dialog. Spec says 'dialog' is acceptable. | ✓ |
| Custom overlay pane | Semi-transparent VBox/StackPane on top of the game board. More polished but ~20 lines more code and FXML changes. | |

**User's choice:** JavaFX Alert dialog

| Option | Description | Selected |
|--------|-------------|----------|
| Just the outcome message | e.g. 'Advanced to Level 2!' / 'Game Over — not enough points.' / 'You Win!' | ✓ |
| Outcome + score summary | Adds 'Level score: 180 / Threshold: 90' for advance/game-over, 'Final score: 1040' for game-won. | |

**User's choice:** Just the outcome message

| Option | Description | Selected |
|--------|-------------|----------|
| INFORMATION with OK button | Same Alert.AlertType for all outcomes — advance, game-over, and game-won all show a single OK button. | ✓ |
| Different types per outcome | INFORMATION for advance, WARNING for game-over, NONE for game-won. | |

**User's choice:** INFORMATION with OK button

**Notes:** Keep it simple and uniform — all outcomes use the same Alert type.

---

## Level Advance Flow

| Option | Description | Selected |
|--------|-------------|----------|
| Next episode starts immediately | Player clicks OK on the Alert, board resets with new target word and fresh timer. No extra step. | ✓ |
| Show a Start button first | After dismissing the Alert, a Start/Continue button appears before next episode begins. | |

**User's choice:** Next episode starts immediately

| Option | Description | Selected |
|--------|-------------|----------|
| Reset everything except score | foundWords cleared, timer back to 2:00, buttons rebuilt. Cumulative score preserved. | ✓ |
| Preserve found-words list across levels | List keeps growing to show entire game history. Timer and buttons still reset. | |

**User's choice:** Reset everything except score

**Notes:** Clean slate per episode, but carry forward total score.

---

## Restart Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Yes — game resets from Level 1 | After OK on game-over Alert, calls startGame() — score 0, Level 1, new target word. Same for game-won. | ✓ |
| No — game freezes at game-over | Controls stay disabled, player relaunches the app to play again. | |

**User's choice:** Yes — game resets from Level 1

| Option | Description | Selected |
|--------|-------------|----------|
| Same — both restart from Level 1 | Both game-over and game-won call startGame(). Consistent. | ✓ |
| Win shows final score before restart | game-won Alert includes 'Final score: X', game-over just resets. Minor content difference. | |

**User's choice:** Same — both restart from Level 1

**Notes:** Simple and consistent — all terminal states lead back to Level 1 via startGame().

---

## Claude's Discretion

- Alert title: `"TextTwist"` (consistent branding, not outcome-specific)
- `beginNextEpisode()` vs. inline advance logic: Claude decides whether to extract a private helper

## Deferred Ideas

None — discussion stayed within Phase 4 scope.
