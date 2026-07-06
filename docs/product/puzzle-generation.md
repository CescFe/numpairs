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

## Difficulty Profiles

Generated puzzle profiles define size, value constraints, result constraints, masking, and validation expectations for each generated mode.

### `4 Pairs Low`

Status: implemented.

Shape:

- 4 solution pairs
- 8 strip entries
- 8 board tiles

Strip values:

- range: `2..20`
- uniqueness: values do not repeat
- `1`: excluded

Result constraints:

- multiplication result limit: `150`
- board result duplicates: not allowed

Initial masking:

- tile expressions: all hidden
- known strip entries: 3
- hidden strip entries: 5
- required anchor: highest strip value is known
- distribution: known entries should be spread across the strip and belong to different solution pairs where possible
- hidden run limit: no more than 2 consecutive hidden strip entries when possible

Generation expectations:

- generation uses bounded attempts and rejects invalid candidates
- board tiles may be shuffled independently from strip order

Validation expectations:

- the solved puzzle satisfies all `4 Pairs Low` value and result constraints
- the initial puzzle follows the `4 Pairs Low` masking policy
- the solved puzzle is accepted by shared NumPairs completion validation

Solving-tip implications:

- prime board results are strong addition-tile signals because `1` is excluded
- factor checks remain approachable because values are small and products are capped
- the visible highest strip value acts as a deduction anchor
- player-facing low-difficulty tips are documented in `docs/product/solving-tips-low-difficulty.md`

These constraints keep the first generated mode approachable, reduce arithmetic load, and avoid duplicate-result ambiguity.

---

### `8 Pairs Medium`

Status: planned for v5; must be fully defined before generator implementation.

Shape:

- 8 solution pairs
- 16 strip entries
- 16 board tiles

Strip values:

- range: TBD
- uniqueness/repetition policy: TBD
- ordering: ascending
- `1`: TBD

Result constraints:

- multiplication result limit: TBD
- board result duplicate policy: TBD

Initial masking:

- tile expressions: all hidden
- known strip entries: TBD
- hidden strip entries: TBD
- required anchors: TBD
- distribution: TBD
- hidden run limit: TBD

Generation expectations:

- deterministic generation support for tests: required
- bounded attempts / failure handling: required
- board tile shuffling: TBD

Validation expectations:

- the solved puzzle must satisfy all `8 Pairs Medium` value and result constraints
- the initial puzzle must follow the `8 Pairs Medium` masking policy
- the solved puzzle must be accepted by shared NumPairs completion validation
- unique-solution guarantees remain out of scope unless explicitly added to this profile

Solving-tip implications:

- TBD: decide which `4 Pairs Low` tips still apply
- TBD: decide whether `8 Pairs Medium` needs separate player-facing tips

Until the TBD decisions are resolved, implementation should not assume that `8 Pairs Medium` simply doubles `4 Pairs Low`.

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
