# Phase 1: Scaffold + Pure Java Model - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-12
**Phase:** 1-Scaffold + Pure Java Model
**Areas discussed:** Maven bootstrap, case handling, checkGuessWord scope, dictionary ownership (all resolved by Claude's discretion — user skipped interactive discussion)

---

## Area Selection

| Option | Description | Selected |
|--------|-------------|----------|
| Maven bootstrap | Run archetype vs. write files directly | (Claude's discretion) |
| Case handling | Auto-uppercase in isValidWord vs. caller responsibility | (Claude's discretion) |
| checkGuessWord scope | Dictionary lookup only vs. formability check | (Claude's discretion) |
| Dictionary ownership | TwistController accepts path param vs. hardcodes | (Claude's discretion) |

**User's choice:** "Skip to context" — user deferred all gray areas to Claude
**Notes:** User also mentioned "gui design" which was redirected to Phase 2 as a deferred idea.

---

## Claude's Discretion

All four gray areas were resolved by Claude based on CLAUDE.md constraints, REQUIREMENTS.md, and project context:

- **Maven bootstrap:** Use `mvn archetype:generate` (standard approach; generates correct pom.xml structure)
- **Case handling:** Auto-uppercase in `isValidWord` (dictionary files are uppercase; defensive normalization reduces caller errors)
- **checkGuessWord scope:** Dictionary lookup only (formability enforced by Phase 3 UI button state; adding it to model is redundant)
- **Dictionary ownership:** Constructor accepts path param (enables unit testing without filesystem assumptions)

## Deferred Ideas

- **GUI design** — Mentioned by user; belongs to Phase 2 (FXML Layout + CSS)
- **isFormableFrom check** — Considered for checkGuessWord; deferred; Phase 3 UI makes it redundant
