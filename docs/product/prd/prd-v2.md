# PRD - NumPairs (v2)

## Document Status

- Status: canonical PRD for the `v2 - Puzzle Generation & Replay Loop` milestone
- Supersedes: `docs/product/prd/prd-v1.md`
- Current implementation baseline: `v1 - Product Polish & Technical Hardening`
- Related references:
  - `README.md`
  - `docs/product/roadmap.md`
  - `docs/product/puzzle-generation.md`
  - `docs/ui-behavior.md`
  - `docs/product/ux-decisions.md`
  - `docs/game-rules.md`
  - `docs/ubiquitous-language.md`

This document defines the product baseline that v2 is intended to establish.

`prd-v0.md` and `prd-v1.md` remain in the repository as historical milestone snapshots. `prd-v2.md` is the canonical reference for the current NumPairs product direction, scope, and documentation alignment.

---

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle.

Players use numbers from a strip to complete hidden expressions on a board. Each pair of strip entries generates one addition result and one multiplication result, and the player must resolve the puzzle by deducing missing numbers, selecting operators, and matching pairings correctly.

v2 turns that single handcrafted puzzle foundation into the first replayable gameplay loop by adding a menu, a generated `4 Pairs` mode, and post-completion navigation.

---

## Product Goal

Introduce the first replayable NumPairs session loop without expanding beyond a focused, low-difficulty puzzle foundation.

---

## Problem Statement

The current app proves the core puzzle interaction, but it still behaves like a single isolated puzzle session.

After v1, the product still lacks:

- a menu where players can choose how to start
- a replayable source of puzzle content
- a validated generation pipeline for new puzzles
- a completion flow that keeps the player inside the app loop
- documentation that treats replayable puzzle sessions as the active product direction

v2 should make the game:

- replayable without restarting the app
- easier to expand into additional puzzle modes later
- more trustworthy by validating generated puzzles before presentation
- clearer as a product, not just as a prototype implementation

---

## Target Users

- Casual puzzle players looking for short replayable sessions
- Players who enjoy mental math and deduction
- Mobile-first players who want an understandable first mode before deeper systems are added

---

## Current Baseline At Start Of v2

The product entering v2 is the implemented `v1 - Product Polish & Technical Hardening` baseline.

That baseline currently includes:

- a branded Android launch experience with splash support
- app-level navigation orchestration through `AppNavigation`
- a single playable game screen backed by one handcrafted seed puzzle
- an 8-tile result board and an 8-entry number strip
- hidden, known, and player-entered strip states
- direct tile editing through contextual selectors
- local incorrect-tile feedback for fully known wrong expressions
- whole-puzzle completion feedback for valid and invalid completed puzzle states
- responsive Compose UI foundations and improved accessibility semantics

That baseline does not yet include:

- a menu screen
- tutorial versus generated mode selection
- generated puzzle content
- puzzle validation as an explicit generation-time service
- replay from the completion flow
- persistence or save state

---

## Product Principles

- Clarity over cleverness
- Replayability before retention systems
- Generation logic must be isolated from the UI layer
- Validation should protect product quality without blocking future experimentation
- Architecture should support future puzzle sizes, difficulty levels, and modes without forcing them into v2
- Documentation should distinguish implemented baseline, active milestone intent, and historical snapshots

---

## Core UX Expectations

- App launch should move from splash to a clear menu entry point
- The menu should make the difference between `Tutorial` and generated `4 Pairs` play obvious
- `Tutorial` mode should preserve the current handcrafted puzzle experience as a stable learning path
- Generated `4 Pairs` mode should feel consistent with the existing in-puzzle interaction model documented in `docs/ui-behavior.md`
- Players should never be shown a generated puzzle that has not passed internal validation
- Puzzle completion should offer clear next steps: start another generated puzzle or return to the menu
- Replay should not require force-closing or relaunching the app
- Low-difficulty generated puzzles should favor clarity and solvability over novelty or advanced balancing

---

## v2 Scope

### Gameplay Loop

- Add a menu screen after the splash screen
- Add navigation flow between menu and game screens
- Add `Tutorial` mode using the existing handcrafted seed puzzle
- Add a generated `Mode 4 Pairs` entry point
- Allow the player to start a new generated puzzle after puzzle completion
- Allow returning to the menu from the completion flow

### Puzzle Generation

- Define generation rules for the `4 Pairs` mode
- Implement a basic generator for low-difficulty puzzles
- Establish an initial low-difficulty classification model for generated puzzles
- Use `docs/product/puzzle-generation.md` as the v2 reference for generation flow, low-difficulty rules, and solved-to-initial puzzle masking
- Implement an internal puzzle validation and solver service
- Validate generated puzzles before presenting them to the player
- Support deterministic generation where useful for tests and debugging
- Prepare the model so future difficulty expansion does not require UI-layer redesign

The v2 generated `4 Pairs` profile is intentionally narrow:

- 8 strip entries and 8 board tiles
- 4 solution pairs
- one addition tile and one multiplication tile per solution pair
- distinct strip values in the inclusive range `2..20`
- multiplication results no greater than `150`
- distinct board results
- all tile expressions hidden in the initial player-facing puzzle
- exactly 3 known strip entries and 5 hidden strip entries in the initial player-facing puzzle

Generation starts from a solved puzzle, derives the initial player-facing puzzle by hiding tile expressions and masking strip entries, and validates the result before presentation. The solved puzzle and solver remain internal implementation details; v2 does not expose solutions or solver output to the player.

### Architecture

- Isolate puzzle generation logic from UI code
- Introduce clear generation, validation, and gameplay domain boundaries
- Keep the handcrafted tutorial puzzle as a separate content source from generated puzzles
- Prepare extensible puzzle and mode models for future growth

### Testing

- Add unit tests for puzzle validation logic
- Add unit tests that verify generated puzzles are internally valid
- Validate the existing handcrafted seed puzzle through the solver service
- Cover deterministic generation behavior where it materially improves confidence

### Documentation

- Add `docs/product/prd/prd-v2.md` as the canonical PRD
- Keep `docs/product/prd/prd-v0.md` and `docs/product/prd/prd-v1.md` as historical snapshots
- Document puzzle generation constraints and low-difficulty assumptions
- Update roadmap, README, and supporting product docs to align with the v2 gameplay loop

---

## Out Of Scope

- Persistence or save state
- User progression systems
- Scoring systems
- Daily puzzles
- Online or backend features
- Advanced puzzle balancing
- Guaranteed unique puzzle solutions
- Player-facing solver, hints, or solution reveal
- Advanced animations or transitions
- Full onboarding flows beyond the lightweight tutorial mode entry point
- Additional puzzle modes beyond generated `4 Pairs`

---

## Success Criteria

- Users can navigate from splash to menu
- Users can choose between `Tutorial` and generated `4 Pairs`
- Generated puzzles can be completed end-to-end
- Generated puzzles are validated before being shown
- Puzzle completion allows replay without restarting the app
- The completion flow also allows returning to the menu
- Puzzle generation architecture is isolated, testable, and extensible
- The existing handcrafted seed puzzle passes validation through the solver service
- Documentation reflects v2 as the active product reference while preserving v0 and v1 as historical snapshots

---

## Documentation Alignment Notes

- `README.md` should identify `prd-v2.md` as the canonical product reference while still distinguishing the implemented v1 baseline from the active v2 milestone
- `docs/product/roadmap.md` should treat `v1 - Product Polish & Technical Hardening` as the current implemented baseline and `v2 - Puzzle Generation & Replay Loop` as the next milestone
- `docs/product/puzzle-generation.md` should define the generated `4 Pairs` construction process, low-difficulty constraints, and initial-state masking rules
- `docs/ui-behavior.md` should remain the source of truth for in-puzzle interaction behavior shared by tutorial and generated modes; it intentionally does not define splash, menu, or replay routing
- `docs/product/ux-decisions.md` continues to capture layout and visual rationale that applies to the puzzle screen carried forward into v2
- `docs/game-rules.md` and `docs/ubiquitous-language.md` remain the source of truth for the core rules and terminology used by both handcrafted and generated puzzles

The only intentional scope difference is that `docs/ui-behavior.md` is narrower than this PRD: it specifies puzzle-screen interaction details, while this PRD covers the broader product loop, generation architecture, and navigation expectations introduced in v2.
