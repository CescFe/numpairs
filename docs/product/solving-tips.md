# Solving Tips Requirements

## Document Status

- Status: product/UI reference for the gameplay solving tips surface
- Applies to: generated `4 Pairs` gameplay mode
- Related references:
  - `docs/product/rules-helper.md`
  - `docs/product/solving-tips-low-difficulty.md`
  - `docs/product/tutorial.md`
  - `docs/product/puzzle-generation.md`
  - `docs/game-rules.md`
  - `docs/ubiquitous-language.md`

This document defines the intended Solving tips surface before implementation. It records what problem 'Solving tips' should solve, how it differs from Rules helper and Tutorial, and what the first interactive practice CTA should teach.

The complete static dialog tip list is intentionally not finalized in this document yet.

---

## Purpose

Solving tips should help players build practical puzzle-solving habits while they are already inside generated `4 Pairs`.

The surface should answer a different question from Rules helper:

- Rules helper explains what the game rules are.
- Solving tips explain how a player can start reasoning through a puzzle.

Solving tips are general strategy guidance. They should not inspect the current puzzle, reveal answers, choose the next move, expose solver output, or adapt to the player's exact board state.

---

## Active Learning Model

Solving tips should include active learning through a `Practice tips` CTA.

The CTA should open an authored practice experience that lets the player apply selected strategies in a small controlled puzzle. This practice should be short, focused, and separate from the broader `Learn basics` tutorial flow.

The first practice experience should:

- use authored puzzle content rather than generated puzzle content
- teach strategy through real puzzle actions
- avoid contextual answer reveal
- avoid solver-backed suggestions
- preserve the underlying generated `4 Pairs` puzzle when opened as an overlay
- show local completion feedback when the practice puzzle is solved

The first practice experience should not become a second full onboarding tutorial. It should assume the player already understands the core rules from Rules helper and `Learn basics`.

---

## Relationship To Existing Learning Surfaces

### Rules Helper

Rules helper is the quick reference for core rules. It should stay concise and explanatory.

Solving tips may assume those rules and focus on strategy. It should not duplicate the full rules explanation.

### Tutorial

Tutorial teaches the core game model through guided authored content.

Solving tips practice should be narrower than Tutorial. It should teach selected tactical reasoning patterns for generated `4 Pairs`, not the entire game.

### Generated `4 Pairs`

Solving tips should be available from generated `4 Pairs` gameplay.

Opening and closing Solving tips or its practice overlay must not reset:

- generated puzzle identity
- player-entered strip values
- tile assignments
- puzzle outcome state
- completion state

---

## `Practice tips` CTA Scope

The first `Practice tips` CTA should include exactly two low-difficulty practice steps:

1. Identify a sum using a prime result.
2. Narrow a product by checking factors, then confirm the matching pair's sum.

These steps are selected because they teach high-value reasoning patterns for current low-difficulty generated `4 Pairs` puzzles.

The low-difficulty assumptions behind these steps are documented in `docs/product/solving-tips-low-difficulty.md`.

---

## Non-Goals

Solving tips must not:

- reveal hidden strip values from the current puzzle
- identify correct pairings for the current puzzle
- suggest the current puzzle's next move
- call or expose the solver
- autofill strip entries or tile expressions
- replace Rules helper
- replace the `Learn basics` tutorial
- imply that all future difficulty levels support the same strategies

---

## Open Content Work

The static dialog tip list still needs product refinement before implementation.

Future documentation updates should decide:

- which concise tips appear directly in the dialog
- which tips are only taught in `Practice tips`
- which tips are low-difficulty-specific
- which tips belong to future difficulty levels only
