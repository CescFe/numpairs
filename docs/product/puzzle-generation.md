# NumPairs Puzzle Generation

## Document Status

- Status: v2 product reference for generated puzzle content
- Applies to: generated `4 Pairs` mode
- Related references:
  - `docs/product/prd/prd-v2.md`
  - `docs/game-rules.md`
  - `docs/ubiquitous-language.md`

This document records the first generation model for NumPairs v2. It defines how low-difficulty generated puzzles are produced and how a fully solved puzzle is converted into the initial player-facing state.

---

## Purpose

The v2 generator should create replayable `4 Pairs` puzzles that are easy to understand, internally valid, and consistent with the existing game rules.

The generator should prioritize clarity over variety. Advanced balancing, progression, and guaranteed unique solutions remain out of scope for v2.

---

## 4 Pairs Mode

A generated `4 Pairs` puzzle has:

- 8 strip entries
- 4 solution pairs
- 8 board tiles
- one addition tile and one multiplication tile for each solution pair

Each strip entry is a unique game element with a stable identity. Pairing and validation operate on strip entry identity, not only on numeric value.

The strip is displayed in ascending numeric order. Board tile order may be shuffled independently from strip order.

---

## Generation Flow

Generation should start from a fully solved puzzle and then derive the initial puzzle shown to the player.

1. Generate 8 distinct strip values in the inclusive range `2..20`.
2. Sort the values in ascending order and assign stable strip entry identities.
3. Create 4 disjoint unordered pairs from the 8 strip entries.
4. Generate 2 solved tiles for each pair:
   - one addition expression
   - one multiplication expression
5. Reject and resample puzzles that violate the low-difficulty constraints.
6. Shuffle the 8 generated tiles for board presentation.
7. Convert the solved puzzle into an initial puzzle by hiding all tile expressions and masking selected strip entries.
8. Validate the initial puzzle before presenting it to the player.

The solved puzzle is the generator's internal source of truth. The initial puzzle is the player-facing version derived from it.

---

## Low-Difficulty Rules

The first generated mode should use a narrow low-difficulty rule set:

- Strip values are positive integers in the inclusive range `2..20`.
- Strip values do not repeat.
- Strip values are displayed in ascending order.
- Multiplication results should not exceed `150`.
- All 8 board results should be distinct.
- Each solution pair contributes exactly one sum result and one product result.
- Tile expressions start hidden: left operand, operator, and right operand are all hidden.
- Grid results are visible from the start.

These constraints are intentionally conservative. They reduce arithmetic load, avoid duplicate-result ambiguity, and keep the generated puzzles suitable for a first replayable mode.

---

## Initial State Masking

The initial player-facing puzzle should hide all tile expressions and hide 5 of the 8 strip entries.

Exactly 3 strip entries should be known from the start:

- The highest strip value is always known.
- Known strip entries should be distributed across the sorted strip.
- Known strip entries should belong to different solution pairs where possible.
- The mask should avoid long hidden runs; for v2, no more than 2 hidden strip entries should appear consecutively when possible.

The distributed known entries act as anchors for deduction. They should make the puzzle approachable without revealing a complete pair outright as the default behavior.

---

## Validation Expectations

Generated puzzles must be validated before presentation.

Validation and solver behavior are internal generation safeguards. They are not player-facing v2 features and do not imply hints, solution reveal, or any UI for inspecting solver output.

Validation should confirm that:

- The strip has exactly 8 entries.
- The board has exactly 8 tiles.
- The strip is sorted and follows the selected low-difficulty value constraints.
- The solved puzzle contains 4 valid solution pairs.
- Each strip entry is used once in an addition expression and once in a multiplication expression.
- Addition and multiplication tiles for a pair reference the same unordered strip entry pair.
- The initial puzzle hides all tile expressions.
- The initial puzzle exposes exactly 3 known strip entries and hides exactly 5 strip entries.
- The generated puzzle satisfies the v2 low-difficulty rules.

Guaranteed unique solutions are not required for v2. The solver may validate internal consistency and solveability, but uniqueness is explicitly out of scope. Generated puzzles should never be shown if they fail internal consistency validation.

---

## Documentation Boundaries

`docs/game-rules.md` remains the source of truth for core NumPairs rules shared by handcrafted and generated puzzles.

`docs/product/prd/prd-v2.md` remains the historical product reference for the v2 generation milestone.

This document is the v2 reference for generated puzzle construction, low-difficulty assumptions, and the solved-to-initial puzzle transformation.
