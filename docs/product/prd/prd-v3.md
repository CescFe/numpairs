# PRD - NumPairs 🎓 v3 Guided Play & Rules Onboarding

> Historical note: this document captures the original guided play % rules onboarding scope. It remains as a historical snapshot and was later superseded.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle.

Players use numbers from a strip to complete hidden expressions on a board. Each pair of strip entries generates one addition result and one multiplication result, and the player must resolve the puzzle by deducing missing numbers, selecting operators, and matching pairings correctly.

v2 established the first replayable gameplay loop with a menu, generated low-difficulty `4 Pairs` puzzles, validation-backed generation, and replay actions after completion.

v3 focuses on player understanding. It adds accessible rule help inside gameplay and turns the current Tutorial entry from a prototype seed puzzle into an intentional learning path.

---

## Product Goal

Help first-time players understand NumPairs' core mechanics before expanding into additional difficulty levels, puzzle modes, or progression systems.

---

## Problem Statement

The v2 product loop is replayable, but it still assumes that players can infer the rules from the puzzle screen.

After v2, the product still has two onboarding gaps:

- rules are not available from inside gameplay
- Tutorial mode still uses the old handcrafted prototype puzzle rather than a deliberately authored learning experience

This creates risk for new players because generated `4 Pairs` puzzles can be technically valid and replayable while still being hard to understand without external explanation.

v3 should make the game:

- easier to learn without leaving the puzzle screen
- clearer about the relationship between Tutorial and generated `4 Pairs`
- better prepared for future difficulties and modes
- careful not to introduce hints, solver-backed assistance, or answer reveal as part of basic help

---

## Target Users

- First-time players who need the rules explained before replaying generated puzzles
- Casual puzzle players who want lightweight help without a long onboarding flow
- Returning players who need a quick reminder of core mechanics
- Mobile-first players who should be able to understand rules inside the app

---

## Current Baseline At Start Of v3

The product entering v3 is the implemented `v2 - Puzzle Generation & Replay Loop` baseline.

That baseline currently includes:

- a branded Android launch experience with splash support
- a `Splash -> Menu` startup flow
- mode selection between `Tutorial` and generated `4 Pairs`
- a reusable game route and screen shared by tutorial and generated modes
- a handcrafted seed puzzle currently used by Tutorial
- generated low-difficulty `4 Pairs` puzzles
- an 8-tile result board and an 8-entry number strip
- exactly 3 known strip entries and 5 hidden strip entries for generated `4 Pairs`
- hidden tile expressions in generated initial puzzles
- internal generation, validation, and solver services for generated puzzles
- isolated game state per mode and generated replay
- completion actions for generated `4 Pairs`: start a new puzzle or return to the menu

That baseline does not yet include:

- a gameplay rules helper for generated `4 Pairs`
- rule help accessible from the game top bar
- authored tutorial puzzle scenarios designed as a learning path
- tutorial-specific guidance content
- adaptive hints, answer reveal, or solver-backed help
- additional difficulty levels or puzzle modes

---

## Product Principles

- Teach before expanding content breadth
- Keep help concise, accessible, and available on demand
- Preserve the puzzle-solving experience: help explains rules, not answers
- Use the existing game interaction model whenever possible
- Keep Tutorial content separate from generated puzzle providers
- Keep generated `4 Pairs` behavior stable while onboarding improves around it
- Documentation should distinguish core rules, helper presentation, and tutorial content

---

## Core UX Expectations

- Players can open basic rules help from generated `4 Pairs`
- The helper is reachable from the game screen top bar
- The helper explains the rules without changing puzzle state
- The helper does not reveal puzzle-specific answers, pairings, or solver output
- Tutorial mode clearly behaves as a learning entry point, not as a renamed prototype puzzle
- Tutorial mode should prepare players for generated `4 Pairs`
- Generated `4 Pairs` should remain the replayable mode introduced in v2
- Normal gameplay interactions should remain consistent between Tutorial and generated `4 Pairs`

---

## v3 Scope

### Rules Help

- Add a rules/help action to the game screen top bar
- Make the helper available in generated `4 Pairs`
- Show a concise explanation of:
  - strip numbers
  - hidden values
  - board tiles
  - operands and operators
  - the addition and multiplication relationship for each pair
  - how completion is validated
- Keep the helper informational only
- Do not reveal puzzle-specific answers or hints
- Do not expose solver output

### Tutorial Mode

- Replace the current prototype tutorial experience with basic authored tutorial puzzle scenarios
- Design tutorial content around teaching the core rules step by step
- Clarify the difference between Tutorial and generated `4 Pairs`
- Ensure tutorial behavior remains isolated from generated mode state
- Preserve normal game interactions where possible instead of creating a separate tutorial UI system
- Prepare room for future tutorial iterations without requiring a rewrite

### Architecture

- Keep rules helper content reusable for gameplay modes that opt into it
- Keep tutorial puzzle scenarios and tutorial content separate from generated puzzle providers
- Avoid coupling tutorial-specific guidance to generated `4 Pairs`
- Keep generated puzzle generation and validation unchanged unless a tutorial requirement explicitly needs a shared abstraction
- Prepare room for future guided onboarding without building a full onboarding framework in v3

---

## Out Of Scope

- New puzzle modes beyond `4 Pairs`
- Additional difficulty levels
- Adaptive hints
- Solver-backed help
- Puzzle-specific answer reveal
- User progression
- Scoring
- Persistence or save state
- Full multi-step onboarding system
- Advanced tutorial animations
- In-game calculator implementation

---

## Success Criteria

- Users can open basic rules help from generated `4 Pairs`
- Rules help explains the game without revealing puzzle-specific answers
- Tutorial mode uses intentional authored learning puzzle scenarios, not the old prototype seed by accident
- Tutorial mode teaches the core NumPairs mechanics clearly enough to prepare users for generated `4 Pairs`
- Generated `4 Pairs` behavior from v2 remains unchanged
- Tutorial and generated mode state remain isolated
- Documentation reflects rules help and tutorial behavior
