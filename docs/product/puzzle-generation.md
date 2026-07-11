# NumPairs Puzzle Generation

## Document Status

- Status: product reference for generated puzzle construction
- Implemented profiles: generated `4 Pairs Low` and `8 Pairs Medium`
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

Profile definitions are not usable directly. They must be created through the generated-profile factory, which derives board, strip, and hidden-entry counts from puzzle size and returns either a validated profile or a non-empty set of typed compatibility violations.

Profile concerns are separated as follows:

- hard rules cover strip values, arithmetic results, product anchors, initial mask shape, required anchors, hidden runs, and distribution
- soft variety policy covers high-value masking frequencies and prime-product decoy frequencies
- generation execution concerns such as attempt limits and deterministic entropy are supplied to the generator rather than stored as passive profile flags

Soft targets that are structurally unreachable for the profile are rejected during profile creation. Bounded fallback remains available for a sampled plan that is infeasible only for the candidates explored during that generation run.

The application composition resolves a stable generated-mode identifier to one profile and creates one immutable hard-rule context for that profile. Candidate selection and final generated-puzzle validation receive that same context. Final validation returns a report with stable rule identifiers and context for every failed rule; it does not stop at the first failure.

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

Status: implemented.

Shape:

- 8 solution pairs
- 16 strip entries
- 16 board tiles

Strip values:

- range: `1..99`
- repetition policy: repeated values are allowed, but no value may appear more than twice
- `1`: allowed

Result constraints:

- multiplication result limit: `1000`
- board result duplicates: not allowed
- product anchor mix: 2 to 4 multiplication results should be greater than `198`

Initial masking:

- tile expressions: all hidden
- known strip entries: 6 to 7
- hidden strip entries: 9 to 10
- required anchors: none
- distribution: unrestricted
- hidden run limit: no more than 4 consecutive hidden strip entries
- high-value mask bias:
  - last strip entry hidden target: 20%
  - second-last strip entry hidden target: 40%
  - third-last strip entry hidden target: 40%

Generation expectations:

- deterministic generation support for tests: required
- bounded attempts / failure handling: required
- board tile shuffling: enabled
- prime-product decoy target: around 30% of generated puzzles should include one solution pair made of `1` and a prime number
- probabilistic targets should guide generation variety without making the generator hang when other hard constraints cannot satisfy them

Probabilistic target semantics:

- Percentages describe the intended frequency of the trait in the final generated-puzzle population, not only the probability of preferring that trait during search.
- One variation plan is sampled per generated puzzle. For every configured target, the plan explicitly requests both outcomes: inclusion or exclusion of a prime-product decoy, and hidden or known visibility for each targeted high-value strip entry.
- Candidate selection treats the complete variation plan as strict during bounded search and returns a matching puzzle whenever a compatible candidate is found.
- Hard validity constraints always take precedence. If a variation plan is infeasible, generation may relax the soft plan and return a hard-valid puzzle instead of failing or retrying without a bound.
- The built-in profile is characterized over the deterministic seed corpus `1..500`. Each observed target frequency must remain within `±5` percentage points of its configured percentage.

Validation expectations:

- the solved puzzle must satisfy all `8 Pairs Medium` value and result constraints
- the initial puzzle must follow the `8 Pairs Medium` masking policy
- the solved puzzle must be accepted by shared NumPairs completion validation
- unique-solution guarantees remain out of scope unless explicitly added to this profile

Solving-tip implications:

- `4 Pairs Low` prime-result guidance does not generally apply because `1` is allowed
- repeated values make strip-entry identity more important than numeric value alone
- product factor checks still apply, but the larger value range and product limit make them less beginner-oriented
- `8 Pairs Medium` may need separate player-facing tips if solving tips are exposed for this mode

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
