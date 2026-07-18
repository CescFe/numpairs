# PRD - NumPairs 📈 v8 Difficulty Selection & Challenge Expansion

> Planned product contract for the v8 milestone. NumPairs delivered v9 before this deferred
> roadmap milestone, so the implemented v9 product is the baseline entering v8.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle. Players complete hidden board
expressions using numbers from an ordered strip. Each solution pair contributes one addition
tile and one multiplication tile, and generated play currently provides `4 Pairs Low` and
`8 Pairs Medium`.

The current product already supports different generated puzzle sizes and profile-specific
generation rules, but each generated mode resolves to exactly one profile. Players therefore
choose a size from the menu without choosing a difficulty, and the application cannot expose a
second calibrated challenge within the same size family.

v8 makes generated mode and difficulty explicit, independent product concepts. `4 Pairs` and
`8 Pairs` remain the size-based mode families; `Low`, `Medium`, and `Hard` become difficulty
tiers. The milestone adds `4 Pairs Medium` and `8 Pairs Hard`, introduces a mode-specific
difficulty selector, and remembers the last difficulty the player actively selected for each
mode.

Every supported challenge remains available from the beginning. v8 does not add player
progression, unlocks, completion counts, rewards, or statistics.

---

## Product Goal

Let players choose an appropriate deductive challenge within each generated puzzle size while
giving NumPairs a clear, maintainable foundation for calibrated difficulty expansion.

The milestone should increase content depth without turning difficulty into a synonym for larger
numbers, manufacturing unsupported size/difficulty combinations, or changing which valid puzzle
completions the game accepts.

---

## Problem Statement

The current one-mode-to-one-profile relationship creates related product and engineering limits:

- `4 Pairs` is permanently presented as Low even though the compact board can support a more
  demanding profile
- `8 Pairs` is permanently presented as Medium and cannot offer a deeper deductive challenge
- the menu cannot distinguish choosing a size from choosing a difficulty
- mode identity, profile selection, navigation, retained presentation state, and session
  restoration assume one profile per mode
- the word `hard` already describes mandatory generator rules internally, which conflicts with
  the player-facing Hard tier
- structural profile inputs do not by themselves detect a puzzle that is trivial, unsatisfiable,
  excessively ambiguous, or expensive to analyse
- copying raw Medium constants from sixteen strip entries to eight would couple the meaning of a
  tier to one puzzle size

v8 should resolve these limits before adding the new profiles, then integrate selection without
weakening the existing generated-session, onboarding, Tutorial, personalization, or feedback
contracts.

---

## Target Users

- Players who prefer the shorter `4 Pairs` board but want more deductive depth than Low
- Returning `8 Pairs Medium` players who want a harder generated challenge
- Players who expect each mode to remember their preferred difficulty
- Mobile-first players who need difficulty selection to stay clear and accessible
- Contributors who need size, difficulty, challenge identity, profile rules, and assessment to
  have explicit ownership

---

## Current Baseline At Start Of v8

The product entering v8 is the delivered v9 baseline.

That baseline includes:

- mandatory versioned onboarding and voluntary `How to play` replay
- generated `4 Pairs Low` and `8 Pairs Medium`
- deterministic, bounded, validated generated-puzzle requests
- stable generated-mode and generated-profile identities
- one exact resumable generated session shared across generated modes
- safe replacement only after a successor is generated, validated, and stored
- completion actions for `Play another` and `Back to menu`
- accessible color personalization and generated-game haptic and motion feedback
- profile-specific constraints and soft population-variety targets
- stable strip-entry identity and support for repeated numeric values

The baseline does not include:

- more than one difficulty in either generated mode
- a player-facing difficulty selector
- a persisted last-selected difficulty for each mode
- explicit generated-challenge identity between mode and profile
- generated difficulty assessment based on simulated resolution metrics
- `4 Pairs Medium` or `8 Pairs Hard`
- player progression or completion statistics

---

## Product Concepts

### Generated Mode

A generated mode is the stable, replayable size family selected from the normal menu.

v8 supports exactly:

- `4 Pairs`
- `8 Pairs`

Mode owns the puzzle-size context. It does not imply one difficulty.

### Difficulty Tier

A difficulty tier classifies intended deductive challenge independently from puzzle size.

v8 defines exactly:

- `Low`
- `Medium`
- `Hard`

The tier is a shared product classification, not a reusable bag of identical absolute constants.
Each supported mode/tier combination may require its own calibrated counts and thresholds to
produce comparable deductive depth.

### Generated Challenge

A generated challenge is one supported combination of generated mode, difficulty tier, and
resolved generated puzzle profile.

Only explicitly configured challenges exist. The application must not construct the full
mode/difficulty product automatically.

### Generated Puzzle Profile

A generated puzzle profile contains the calibrated generation and assessment policies for one
challenge. It remains independent from Android strings, selection preference, lock state,
navigation, and presentation-only capabilities.

---

## Supported Challenge Matrix

| Generated mode | Low | Medium | Hard |
| --- | --- | --- | --- |
| `4 Pairs` | Existing | New in v8 | Unsupported |
| `8 Pairs` | Unsupported | Existing | New in v8 |

All four supported challenges are enabled for every player after required onboarding. Unsupported
cells are absent from selection rather than displayed as locked or unavailable content.

---

## Product Principles

- Treat generated mode as the size family and difficulty as an independent dimension.
- Support a deliberate sparse challenge catalog rather than every possible combination.
- Calibrate the same difficulty tier for each supported size instead of copying absolute counts.
- Define difficulty primarily through deductive structure, not only arithmetic magnitude.
- Validate generated puzzles before display and keep generation and assessment bounded.
- Preserve every completion that satisfies the shared NumPairs rules as a valid completion.
- Keep difficulty fixed for the lifetime of a generated session.
- Remember an explicit player choice without turning it into progression.
- Preserve the single resumable generated-session slot and safe replacement contract.
- Keep Tutorial, onboarding, personalization, generated feedback, and core game rules coherent.
- Complete the product and technical documentation before changing production code.

---

## Core UX Expectations

### Mode And Difficulty Selection

- Keep `4 Pairs` and `8 Pairs` as the generated-mode actions in the normal menu.
- Selecting a generated mode opens a dedicated selector for that size family.
- The `4 Pairs` selector shows exactly `Low` and `Medium`.
- The `8 Pairs` selector shows exactly `Medium` and `Hard`.
- Every shown difficulty is enabled; the selector has no locks or progression indicators.
- The current selection is communicated without relying only on color.
- A primary action identifies the selected mode and difficulty before starting play.
- System back and the visible back action return to the normal menu without starting a puzzle.

### Remembered Selection

- Remember the last supported difficulty the player actively selects for each mode.
- Keep the `4 Pairs` and `8 Pairs` selections independent.
- Use `4 Pairs Low` when no supported `4 Pairs` preference is available.
- Use `8 Pairs Medium` when no supported `8 Pairs` preference is available.
- Persist an option when the player actively chooses it in the selector.
- Opening the selector, resolving a fallback, resuming, restoring, or replaying does not implicitly
  rewrite the remembered selection.
- Missing, corrupt, unknown, or unsupported stored values fall back safely without blocking play.

### Session, Resume, And Replay

- Starting a challenge creates or safely replaces the one generated-session slot through the
  existing resume-or-new-puzzle flow.
- A generated session retains its exact mode and profile identity.
- `Resume` restores the exact stored challenge and current puzzle without consulting or changing
  the remembered selector default.
- `Play another` generates another puzzle for the current mode and difficulty.
- Difficulty cannot change while a generated puzzle is active.
- Game titles, Resume accessibility descriptions, and resume-or-replace presentation identify the
  relevant mode and difficulty.
- Selecting a different challenge never discards a resumable session until its validated successor
  is safely stored.

### Completion

- Solving a generated puzzle continues to show `Play another` and `Back to menu`.
- Completion does not increment a counter, unlock content, award a reward, or change the remembered
  difficulty.
- v8 does not add a `Change difficulty` completion action.

---

## v8 Scope

### Documentation-First Contract

- Add this PRD before implementation.
- Record the durable mode, difficulty, challenge, and profile relationship.
- Document the calibrated `4 Pairs Medium` and `8 Pairs Hard` rules.
- Document the bounded assessment contract and metric semantics.
- Align generated-puzzle, ubiquitous-language, UI, reliable-session, and current-product references.
- Resolve documentation ambiguity before encoding a new rule.

No production or test-code refactor for v8 begins until the planned product, domain, generation,
selection, and session documentation is delivered.

### Technical Preparation

- Rename mandatory generator hard-rule terminology to constraint terminology before adding Hard.
- Separate generated mode, difficulty tier, supported challenge, and resolved profile.
- Allow one generated mode to expose multiple validated challenges.
- Reject duplicate, mismatched, or unsupported challenge configuration.
- Resolve generation, retained presentation state, and resumable sessions through challenge identity.
- Preserve current behavior while only `4 Pairs Low` and `8 Pairs Medium` are configured.
- Keep mandatory constraints, soft variety targets, and difficulty assessment as distinct concerns.

### Bounded Difficulty Assessment

- Add a deterministic, platform-independent assessment boundary.
- Simulate canonical domain deductions rather than Compose interactions or UI action ordering.
- Bound assessment work and cancellation explicitly.
- Report typed success, unsatisfiable, cancellation, and work-limit outcomes.
- Measure the documented resolution and structural metrics, including:
  - initial plausible candidates
  - forced deductions
  - depth before the first forced deduction
  - maximum branching factor
  - explored ambiguous states
  - bounded valid-solution count
  - known entries and hidden runs
  - anchors, repeated values, and plausible decoys
- Canonicalize irrelevant left/right, action-order, and repeated-value symmetries as documented.
- Characterize the existing profiles before using assessment to calibrate new content.
- Do not use assessment to reject a player completion that satisfies the shared game rules.

### `4 Pairs Medium`

- Add one Medium challenge for the existing `4 Pairs` mode.
- Reuse the semantic Medium difficulty axes with calibration for eight strip entries.
- Increase deductive challenge through controlled repetition, weaker fixed anchors, longer hidden
  runs, and decoys rather than merely copying the `8 Pairs Medium` numeric envelope.
- Keep generation deterministic for tests and bounded on failure.
- Protect the documented profile and population-variety expectations with automated tests.

The supporting generation reference owns the exact profile constraints and assessment acceptance
semantics. Numerical assessment bands may be locked only after deterministic baseline
characterization; v8 must not invent unevidenced score thresholds before the evaluator exists.

### `8 Pairs Hard`

- Add one Hard challenge for the existing `8 Pairs` mode.
- Keep arithmetic within the documented `8 Pairs Medium` envelope unless the supporting generation
  contract explicitly requires otherwise.
- Increase deductive depth through fewer known values, weaker obvious result anchors, controlled
  repetition, longer hidden sequences, and stronger decoys.
- Reject candidates that assessment identifies as trivial, unsatisfiable, or beyond the bounded
  supported envelope.
- Keep generation deterministic for tests and bounded on failure at sixteen-entry scale.

The supporting generation reference owns the exact profile constraints and assessment acceptance
semantics. Numerical assessment bands may be locked only after deterministic baseline
characterization; v8 must not invent unevidenced score thresholds before the evaluator exists.

### Persistent Mode Defaults

- Store stable selected difficulty identity rather than display text or profile parameters.
- Keep the preference independent from onboarding, personalization, generated sessions, and
  completion state.
- Expose an observable mode-specific selection with safe supported fallbacks.
- Persist only explicit supported player selections.
- Preserve preferences across application recreation.

### Difficulty Selection Surface

- Add reusable, state-driven Compose content for one selected generated mode.
- Keep domain rules, persistence, generation, and navigation out of the content Composable.
- Reuse established NumPairs components, tokens, typography, shapes, spacing, and semantic roles.
- Preserve minimum touch targets, readable text scaling, and non-color selection cues.
- Provide previews and focused UI or presentation coverage for both supported mode selectors.

### End-To-End Challenge Integration

- Route both generated-mode menu actions through difficulty selection.
- Generate the actively selected supported challenge.
- Preserve selected challenge identity through loading, failure, safe replacement, restoration, and
  replay.
- Keep challenge-aware titles, localization, and accessibility semantics coherent.
- Preserve v9 generated-game haptics, correct-tile feedback, completion celebration, and successor
  transition for every supported challenge.
- Preserve Tutorial, onboarding, personalization, and one-slot reliable-session behavior.

---

## Delivery Stages

### Stage 1 - Authoritative Product Contract

Outcome: v8 scope, decisions, terminology, profile rules, assessment semantics, selector behavior,
and session interactions are documented before code changes.

Work:

1. Add the v8 PRD.
2. Record the generated challenge architecture decision and align ubiquitous language.
3. Document the new profiles, constraint/variety/assessment boundaries, and evaluator contract.
4. Document selector, preference, navigation, session, replay, title, and accessibility behavior.

### Stage 2 - Behavior-Preserving Technical Foundation

Outcome: existing generated play uses unambiguous constraint terminology and can resolve multiple
supported challenges per mode without adding new content yet.

Work:

1. Rename mandatory generated hard rules as constraints.
2. Introduce independent difficulty-tier and generated-challenge concepts.
3. Replace the one-mode-to-one-profile registry with a validated sparse challenge catalog.
4. Preserve existing generation, navigation, session, replay, Tutorial, onboarding, and v9 behavior.

### Stage 3 - Assessment And Challenge Expansion

Outcome: bounded difficulty evidence protects the two new documented challenges.

Work:

1. Implement and test deterministic bounded difficulty assessment.
2. Characterize `4 Pairs Low` and `8 Pairs Medium`.
3. Implement and calibrate `4 Pairs Medium`.
4. Implement and calibrate `8 Pairs Hard`.

### Stage 4 - Remembered Difficulty Selection

Outcome: each mode exposes all and only its supported difficulties and remembers explicit player
choice independently.

Work:

1. Add the local mode-specific selection repository and safe fallbacks.
2. Add the reusable difficulty-selection screen.
3. Route menu selection and Play through the selected challenge.

### Stage 5 - End-To-End Quality And Product Alignment

Outcome: challenge identity remains coherent through normal play, reliable sessions, safe replay,
localization, accessibility, and the v9 experience contracts.

Work:

1. Integrate challenge-aware Resume, replacement, titles, and accessibility copy.
2. Verify replay preserves the current challenge and does not rewrite mode defaults.
3. Verify existing and new challenges across generation, persistence, feedback, and failure paths.
4. Complete the Compose design-system consistency pass.
5. Run milestone formatting, unit, lint, and instrumented-test compilation validation.

---

## Out Of Scope

- Player progression, unlocks, completion counts, rewards, streaks, achievements, or statistics
- Locked difficulty presentation or progress indicators
- Changing puzzle completion to require one authored, generated, or solver-preferred solution
- Rejecting a player completion that satisfies the shared NumPairs rules
- Changing difficulty during an active generated puzzle
- A global difficulty setting shared by `4 Pairs` and `8 Pairs`
- A `Change difficulty` completion action
- Custom mode or arbitrary player-authored generation profiles
- Automatically supporting every size/difficulty combination
- New puzzle sizes, operators, or changes to core NumPairs rules
- Revising Low-specific solving tips when they are shown in `4 Pairs Medium`
- Adaptive hints, answer reveal, or exposing solver deductions to the player
- Scoring, timers, daily puzzles, accounts, cloud synchronization, or online services

---

## Success Criteria

- Generated mode is modeled and presented as the puzzle-size family.
- Difficulty is modeled independently as Low, Medium, or Hard.
- The supported catalog contains exactly `4 Pairs Low`, `4 Pairs Medium`, `8 Pairs Medium`, and
  `8 Pairs Hard`.
- Players can inspect and select Low or Medium for `4 Pairs` and Medium or Hard for `8 Pairs`.
- Every supported difficulty is available without progression gates.
- Each mode restores its last actively selected supported difficulty with the documented fallback.
- Selecting, resuming, restoring, replacing, and replaying preserve the documented preference and
  session ownership boundaries.
- Difficulty remains fixed during a generated session, and `Play another` keeps the same challenge.
- The two new profiles satisfy their documented constraints, variety targets, and bounded assessment
  contracts.
- Assessment remains deterministic, bounded, platform-independent, and separate from player
  completion validity.
- Existing `4 Pairs Low`, `8 Pairs Medium`, Tutorial, onboarding, reliable sessions,
  personalization, and v9 generated feedback remain coherent.
- Documentation precedes technical refactoring, and technical refactoring precedes new challenge
  implementation.
