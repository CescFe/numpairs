# NumPairs Puzzle Generation

## Document Status

- Status: product reference for generated puzzle construction
- Current implemented profile: generated `4 Pairs Low`
- Planned v5 profile: generated `8 Pairs Medium`, to be defined before implementation
- Related references:
  - `docs/product/prd/prd-v5.md`
  - `docs/game-rules.md`
  - `docs/ubiquitous-language.md`

This document owns generated puzzle construction, difficulty profiles, solved-to-initial masking, validation expectations, deterministic generation, and failure bounds.

---

## Shared Generation Model

Generated puzzles start from a fully solved puzzle and derive the initial player-facing puzzle from it.

Shared rules:

- The strip contains an even number of stable strip entries.
- Each solution pair contributes exactly two board tiles:
  - one addition tile
  - one multiplication tile
- Pairing and validation use strip-entry identity, not only numeric value.
- Strip values are displayed in ascending numeric order.
- Board tile order may be shuffled independently from strip order.
- Initial puzzles hide tile expressions while keeping tile results visible.
- Generated puzzles must be validated before display.

The solved puzzle is the generator's source of truth. The initial puzzle is the player-facing masked version.

---

## `4 Pairs Low`

Shape:

- 4 solution pairs
- 8 strip entries
- 8 board tiles

Strip and result rules:

- Strip values are distinct integers in `2..20`.
- Strip values are sorted ascending.
- Multiplication results must not exceed `150`.
- All 8 board results must be distinct.

Initial masking:

- All tile expressions start hidden.
- Exactly 3 strip entries are known.
- Exactly 5 strip entries are hidden.
- The highest strip value is always known.
- Known entries should be distributed across the strip and belong to different solution pairs where possible.
- No more than 2 hidden strip entries should appear consecutively when possible.

These constraints keep the first generated mode approachable, reduce arithmetic load, and avoid duplicate-result ambiguity.

---

## `8 Pairs Medium`

`8 Pairs Medium` is the planned v5 profile for the larger generated mode.

The profile must be defined before generator implementation. Required decisions:

- strip value range
- whether values may repeat
- whether `1` is allowed
- multiplication result limit
- duplicate board-result policy
- known/hidden strip entry counts
- anchor placement and distribution rules
- maximum hidden-run expectations
- deterministic generation expectations
- bounded failure behavior
- which low-difficulty solving tips still apply

Until those decisions are documented, implementation should not assume that `8 Pairs Medium` simply doubles `4 Pairs Low`.

---

## Validation Expectations

Validation should confirm that:

- strip entry count and board tile count match the selected profile
- the solved puzzle satisfies the selected profile's value and result constraints
- each strip entry is used once in addition and once in multiplication
- addition and multiplication tiles for a pair reference the same unordered strip-entry pair
- the initial puzzle hides all tile expressions
- the initial puzzle follows the selected profile's strip masking policy
- the generated puzzle satisfies shared NumPairs rules

Guaranteed unique solutions are not required unless explicitly added to a future profile. Generated puzzles should never be shown if they fail internal consistency validation.

---

## Documentation Boundaries

- `docs/game-rules.md` owns core NumPairs rules shared by handcrafted and generated puzzles.
- `docs/product/prd/prd-v5.md` owns v5 product scope.
- `docs/ubiquitous-language.md` owns shared terminology.
