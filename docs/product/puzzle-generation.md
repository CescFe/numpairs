# NumPairs Puzzle Generation

## Document Status

- Status: product reference for generated puzzle construction
- Implemented profiles: generated `4 Pairs Low`, `4 Pairs Medium`, `8 Pairs Medium`, and
  `8 Pairs Hard`
- v8 generated challenge matrix: implemented
- Related references:
  - `docs/product/prd/prd-v5.md`
  - `docs/product/prd/prd-v7.md`
  - `docs/product/prd/prd-v8.md`
  - `docs/technical/generated-session-persistence.md`
  - `docs/technical/adr/adr-005-model-sparse-generated-challenges.md`
  - `docs/game-rules.md`
  - `docs/ubiquitous-language.md`

This document owns generated puzzle construction, difficulty profiles, solved-to-initial masking, validation expectations, deterministic generation, and failure bounds. The generated-session persistence and replacement boundary is documented in `docs/technical/generated-session-persistence.md`.

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

## Generated Challenges And Difficulty Profiles

A generated mode is the stable puzzle-size family. A difficulty tier classifies intended
deductive challenge independently from size. A generated challenge binds one supported mode,
difficulty tier, and validated generated puzzle profile.

Profiles define size, difficulty, value constraints, result constraints, masking, variety,
assessment policy, and validation expectations for one challenge. Absolute profile values remain
calibrated per challenge: Medium has shared product meaning, but `4 Pairs Medium` does not copy raw
sixteen-entry counts from `8 Pairs Medium`.

The supported v8 catalog is sparse:

| Generated mode | Low | Medium | Hard |
| --- | --- | --- | --- |
| `4 Pairs` | Supported | Supported | Unsupported |
| `8 Pairs` | Unsupported | Supported | Supported |

Unsupported combinations are absent rather than synthesized. The durable configuration decision
is recorded in
[ADR-005](../technical/adr/adr-005-model-sparse-generated-challenges.md).

Profile definitions are not usable directly. They must be created through the generated-profile factory, which derives board, strip, and hidden-entry counts from puzzle size and returns either a validated profile or a non-empty set of typed compatibility violations.

The profile factory owns deterministic validation limits for eligible-pair catalog expansions, pair-selection states, and mask states. These limits are supplied at creation time and are not part of the resulting difficulty profile. When a definition exceeds the supported validation envelope, creation stops with the stable `profile.validation-work-limit` violation and reports the work kind, configured limit, and consumed work.

Profile concerns are separated as follows:

- mandatory constraints cover strip values, arithmetic results, product anchors, initial mask shape, required anchors, hidden runs, and distribution
- soft variety policy covers high-value masking frequencies and prime-product decoy frequencies
- difficulty-assessment policy defines bounded acceptance semantics over an already valid candidate
- generation execution concerns such as attempt limits and deterministic entropy are supplied to the generator rather than stored as passive profile flags

Soft targets that are structurally unreachable for the profile are rejected during profile creation. Bounded fallback remains available for a sampled plan that is infeasible only for the candidates explored during that generation run.

The application resolves stable mode and profile identities to one configured challenge and creates
one immutable mandatory-constraint context for its profile. Candidate selection and final
generated-puzzle validation receive that same context. Final validation returns a report with
stable rule identifiers and context for every failed rule; it does not stop at the first failure.

## Difficulty Assessment

Difficulty assessment is a platform-independent analysis of a valid generated initial puzzle. It
does not drive Compose UI, reveal answers to the player, or change shared completion validation.
Its purpose is to reject content that is unsatisfiable, trivial for its intended tier, or beyond a
bounded supported ambiguity envelope.

The evaluator reasons in canonical domain facts:

- one pair fact associates an unordered pair of strip entries with one sum result and one product
  result
- swapping left and right operands does not create a new candidate or solution
- committing the same facts in a different UI or board order does not create a new solution
- stable strip-entry identity remains authoritative for usage and pairing constraints
- solution counts collapse cases related only by a global permutation of equal-valued strip-entry
  identities, because that permutation exposes the same arithmetic deductions

The last rule affects assessment metrics only. During play, repeated strip entries remain distinct
game elements and any completed puzzle satisfying shared entry-usage and sum/product pairing rules
is valid.

Assessment reports:

- initial plausible pair/result candidate count
- initial and total forced-deduction counts
- first forced-deduction depth, measured as the minimum number of chained propagation layers needed
  to prove the first fact without a speculative commitment
- maximum branching factor observed during bounded search
- ambiguous states explored
- bounded valid-solution equivalence count
- known-entry count and longest hidden run
- known-strip and unambiguous-result anchor counts
- repeated-value group count
- plausible decoy count, where a locally arithmetic-compatible candidate is eliminated only by
  cross-pair or global usage constraints

A forced deduction is a canonical fact present in every remaining valid solution equivalence
class. The bounded solution count distinguishes none, one, and multiple up to its configured cap;
it is not a unique-solution requirement.

Assessment returns typed assessed, unsatisfiable, cancelled, and work-limit outcomes. The same
puzzle and execution policy must produce the same metrics. Candidate expansions and cancellation
checks are bounded independently from generation search.

Every new v8 profile requires:

- a completed assessment within its work policy
- at least one valid solution equivalence class
- at least one opening fact derivable by constraint propagation without committing a speculative
  branch
- profile-specific structural and decoy observations described below

Numerical score bands for branching, depth, and ambiguous-state metrics must be based on the
deterministic characterization of existing profiles. Characterization may tighten a profile's
documented assessment policy before that profile is implemented, but v8 does not assign arbitrary
weights or unevidenced global score thresholds in advance.

## Population Variety Semantics

Configured percentages describe the intended frequency of a trait in the final generated-puzzle
population, not only the probability of preferring that trait during search.

- One deterministic variation plan is sampled per generated puzzle.
- For every configured target, the plan requests the included or excluded outcome explicitly.
- Candidate selection treats the complete plan as strict during bounded search and returns a
  matching puzzle whenever a compatible candidate is found.
- Mandatory validity constraints always take precedence.
- If a plan is infeasible for the candidates explored within the execution policy, generation may
  relax the soft plan and return a mandatory-valid puzzle instead of failing or searching without
  a bound.
- Built-in profile frequencies are characterized over deterministic seed corpus `1..500`.
- Each observed target remains within `±5` percentage points of its configured percentage.

## Generation Execution And Recovery

Every run is an explicit request containing its profile, seed, and positive execution policy. The policy bounds both top-level attempts and candidate-expansion search work. Exhaustion and cancellation return typed outcomes carrying the profile identifier, consumed attempts, consumed work, and candidate-rejection diagnostics. Final aggregate-validation violations remain in those diagnostics.

Value-pair and strip-mask searches share the same budget and observe cancellation. Strip-mask selection enumerates candidate masks lazily, so it does not first materialize all combinations in memory.

The application executes generation on an injected non-main dispatcher. The generated-mode presentation owner exposes loading, ready, restoration, resume-unavailable, and recoverable-failure states. Replay keeps the completed session visible until a replacement is stored and ready, and duplicate replay requests are ignored.

Successful generation persists the exact initial puzzle before publishing it as playable.
Generated play keeps one application-wide versioned session slot shared by all configured
challenges. The slot stores the exact initial and current puzzles plus stable session, mode,
profile, and seed metadata. Mode/profile identity resolves the exact challenge. Process-death
restoration reads that snapshot directly; it never regenerates historical content from the seed.

Committed puzzle changes update the current snapshot through the stable session id, solved puzzles clear resumability, and stale callbacks cannot mutate a replacement. Generation, storage failure, or cancellation leaves the previous unfinished slot intact. See `docs/technical/generated-session-persistence.md` for the storage, ordering, validation, and backup contract.

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

### `4 Pairs Medium`

Status: implemented in v8.

Shape:

- 4 solution pairs
- 8 strip entries
- 8 board tiles

Strip values:

- range: `1..40`
- occurrence limit: no value may appear more than twice
- repeated-value groups: at most 1 distinct value may repeat
- repeated-value population target: around 35% of generated puzzles should contain exactly 1
  repeated-value group
- `1`: allowed

Result constraints:

- multiplication result limit: `400`
- board result duplicates: not allowed
- product anchor mix: 1 to 2 multiplication results should be greater than `80`, the maximum sum
  of two allowed strip values

Initial masking:

- tile expressions: all hidden
- known strip entries: 3
- hidden strip entries: 5
- required fixed anchors: none
- pair distribution: known entries must belong to at least 2 distinct solution pairs
- hidden run limit: no more than 3 consecutive hidden strip entries
- high-value mask bias:
  - last strip entry hidden target: 25%
  - second-last strip entry hidden target: 40%

Generation expectations:

- deterministic generation support for tests: required
- bounded attempts, search work, failure, and cancellation: required
- board tile shuffling: enabled
- prime-product decoy target: around 30% of generated puzzles should include exactly 1 solution
  pair made of `1` and a prime number
- shared population-variety semantics apply to repetition, high-value masking, and prime-product
  decoys

Assessment expectations:

- assessment must complete within the configured Medium work policy
- at least 1 valid solution equivalence class must exist; uniqueness is not required
- at least 1 opening fact must be derivable without speculative commitment
- at least 1 locally plausible decoy must remain after direct arithmetic filtering
- the first forced fact must require at least 1 propagation layer
- characterization must demonstrate more opening ambiguity than `4 Pairs Low` without exceeding the
  bounded Medium envelope

Solving-tip implications:

- prime results are no longer guaranteed addition tiles because `1` is allowed
- the highest strip value is no longer guaranteed visible
- one repeated-value group can make arithmetic values interchangeable while stable entry identity
  remains authoritative
- longer hidden runs require more use of sorted-neighbor and cross-pair constraints
- v8 intentionally does not revise the current Low-specific solving-tips surface

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
- probabilistic targets should guide generation variety without making the generator hang when other mandatory constraints cannot satisfy them

The shared population-variety semantics apply to high-value masking and prime-product decoys.

Validation expectations:

- the solved puzzle must satisfy all `8 Pairs Medium` value and result constraints
- the initial puzzle must follow the `8 Pairs Medium` masking policy
- the solved puzzle must be accepted by shared NumPairs completion validation
- one or more valid completions may exist; uniqueness is not required

Solving-tip implications:

- `4 Pairs Low` prime-result guidance does not generally apply because `1` is allowed
- repeated values make strip-entry identity more important than numeric value alone
- product factor checks still apply, but the larger value range and product limit make them less beginner-oriented
- `8 Pairs Medium` may need separate player-facing tips if solving tips are exposed for this mode

---

### `8 Pairs Hard`

Status: implemented in v8.

Shape:

- 8 solution pairs
- 16 strip entries
- 16 board tiles

Strip values:

- range: `1..99`
- occurrence limit: no value may appear more than twice
- repeated-value groups: 1 to 2 distinct values must repeat
- `1`: allowed

Result constraints:

- multiplication result limit: `1000`
- board result duplicates: not allowed
- product anchor mix: 0 to 1 multiplication results may be greater than `198`, the maximum sum of
  two allowed strip values

Initial masking:

- tile expressions: all hidden
- known strip entries: 4 to 5
- hidden strip entries: 11 to 12
- required fixed anchors: none
- pair distribution: known entries must belong to at least 3 distinct solution pairs
- hidden run limit: no more than 5 consecutive hidden strip entries
- high-value mask bias:
  - last strip entry hidden target: 60%
  - second-last strip entry hidden target: 70%
  - third-last strip entry hidden target: 70%

Generation expectations:

- deterministic generation support for tests: required
- bounded attempts, search work, failure, and cancellation at sixteen-entry scale: required
- board tile shuffling: enabled
- prime-product decoy target: around 60% of generated puzzles should include exactly 1 solution
  pair made of `1` and a prime number
- shared population-variety semantics apply to high-value masking and prime-product decoys

Assessment expectations:

- assessment must complete within the configured Hard work policy
- at least 1 valid solution equivalence class must exist; uniqueness is not required
- between 1 and 7 opening facts must be derivable without speculative commitment; the existing
  `8 Pairs Medium` characterization exposes all 8 solution facts immediately
- at least 3 locally plausible decoys must remain after direct arithmetic filtering
- at least 1 ambiguous state must remain after direct local filtering
- the maximum observed branching factor must be at least 2
- characterization must demonstrate greater deductive depth and ambiguity than `8 Pairs Medium`
  without exceeding the bounded Hard envelope

Solving implications:

- very few results are guaranteed products solely because they exceed the maximum possible sum
- repeated values and deliberately hidden high values weaken direct strip anchors
- longer hidden runs increase reliance on cross-pair, global usage, and sorted-strip propagation
- decoys should be eliminated through deductions rather than blind trial-and-error

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

Generated puzzles may have more than one valid completion. Every completion satisfying shared
NumPairs rules remains valid, and generated puzzles should never be shown if they fail internal
consistency validation or their selected profile's assessment acceptance policy.

---

## Documentation Boundaries

- `docs/game-rules.md` owns core NumPairs rules shared by handcrafted and generated puzzles.
- `docs/product/prd/prd-v5.md` owns v5 product scope.
- `docs/product/prd/prd-v7.md` owns the reliable-session and replay-control product contract.
- `docs/product/prd/prd-v8.md` owns the difficulty-selection and challenge-expansion product contract.
- `docs/technical/generated-session-persistence.md` owns the implemented session storage and coordination boundary.
- `docs/technical/adr/adr-005-model-sparse-generated-challenges.md` owns the durable sparse challenge-catalog decision.
- `docs/ubiquitous-language.md` owns shared terminology.
