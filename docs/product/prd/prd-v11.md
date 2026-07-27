# PRD - NumPairs 🎮 v11 Simplified Play Modes

> Product contract for the `v11 - Simplified Play Modes` milestone.
> Status: delivered.
> The v10 Quick Play and Daily Challenge product was the implementation baseline.

## Product Summary

Before v11, NumPairs generated play exposed implementation-shaped choices: Quick, 4 Pairs, and
8 Pairs. v11 replaces those three normal-play entries with two delivered player-facing play
options:

- `Quick`, for shorter generated puzzles using either 3 or 4 solution pairs
- `Classic`, for the original full board using 8 solution pairs

Daily Challenge remains a separate date-bound cadence. It continues selecting the exact
`4 Pairs Low` challenge through the immutable v10 recipe.

Internally, `3 Pairs`, `4 Pairs`, and `8 Pairs` remain stable generated-mode identities and puzzle
sizes. Quick and Classic are application-level ways to choose among those exact challenges; they
do not replace the size-based generated-mode model or change the normal generated-session schema.

## Product Goal

Make the normal Menu easier to understand by presenting play intent instead of three separate
puzzle-size choices, while preserving calibrated difficulty, exact session restoration, and
Daily determinism.

## Player-Facing Taxonomy

The unlocked Menu presents:

1. the state-aware Daily Challenge action
2. normal generated `Resume`, only while the normal slot is resumable
3. `Quick · <difficulty>`
4. `Classic · <difficulty>`
5. `How to play`
6. `Settings`

The supported normal-play matrix is:

| Play option | Low | Medium | Hard |
| --- | --- | --- | --- |
| `Quick` | 35% `3 Pairs Low`, 65% `4 Pairs Low` | 35% `3 Pairs Medium`, 65% `4 Pairs Medium` | Unsupported |
| `Classic` | Unsupported | `8 Pairs Medium` | `8 Pairs Hard` |

Unsupported combinations are absent rather than disabled, locked, or synthesized.

## Product Concepts

### Generated Play Option

A Generated Play Option is a player-facing normal-play choice that exposes supported
difficulties and resolves each confirmed new-puzzle request to one exact Generated Challenge.

A play option owns:

- its stable application identity
- its player-facing name
- its supported difficulty choices
- its challenge-selection policy
- its remembered difficulty preference

A play option does not own puzzle size, generation constraints, exact session state, or Daily
cadence.

### Quick

Quick is the shorter normal-play option. It exposes Low and Medium and selects between the
matching 3 Pairs and 4 Pairs challenges for every confirmed new puzzle:

- selector buckets `0..34`: 3 Pairs
- selector buckets `35..99`: 4 Pairs

Each bucket is equally likely. The selection is independent for every new puzzle; v11 adds no
streak smoothing, adaptive difficulty, player-history weighting, or remote configuration.

Quick describes relative session length compared with Classic. It no longer promises one exact
pair count.

### Classic

Classic is the original full-board normal-play option:

- 8 solution pairs
- 16 strip entries
- 16 board tiles

Classic Medium resolves `8 Pairs Medium`. Classic Hard resolves `8 Pairs Hard`. Classic performs
no weighted size selection.

### Generated Mode And Generated Challenge

Generated Mode remains the stable internal puzzle-size family. The implemented modes remain:

- `3 Pairs`
- `4 Pairs`
- `8 Pairs`

Generated Challenge remains one exact supported combination of generated mode, difficulty, and
profile. v11 adds `3 Pairs Medium`; it does not rename or replace existing mode, challenge, or
profile identities.

## Quick Selection Semantics

One weighted selection occurs only when a new Quick puzzle has been confirmed:

- selecting Quick with no resumable normal session
- confirming `New Quick · <difficulty>` in the resume-or-replace dialog
- selecting `Play another` after completing Quick

The selected exact challenge remains fixed for:

- the complete generation attempt
- retry after generation failure
- safe persistence before presentation
- gameplay and process recreation
- normal Resume

Retry may request a new generation seed for the same selected challenge. It must not select
another size.

Cancelling or dismissing the resume-or-replace dialog does not select a challenge, consume a
player-visible random result, or mutate the stored session.

## Difficulty Selection And Compatibility

Quick remembers Low or Medium and defaults to Low. Classic remembers Medium or Hard and defaults
to Medium.

Existing supported preferences migrate as follows:

| Existing preference | v11 preference |
| --- | --- |
| `4 Pairs Low` | `Quick Low` |
| `4 Pairs Medium` | `Quick Medium` |
| `8 Pairs Medium` | `Classic Medium` |
| `8 Pairs Hard` | `Classic Hard` |

Missing, corrupt, unknown, or unsupported values resolve to the documented fallback without
changing generated or Daily session state.

Choosing a difficulty persists the option preference without starting a puzzle. Starting,
resuming, retrying, replacing, completing, or replaying a puzzle does not rewrite it.

## Session Identity And Presentation

The one normal generated-session slot remains shared by every generated challenge. A snapshot
continues storing the exact generated-mode and profile identities, generation seed, initial
puzzle, and current puzzle.

No play-option value is added to the snapshot because it is derivable from the exact configured
challenge:

- 3 Pairs and 4 Pairs normal challenges present as Quick
- 8 Pairs normal challenges present as Classic

Existing valid schema-1 snapshots remain compatible:

- stored 3 Pairs and 4 Pairs sessions resume as `Quick · <difficulty>`
- stored 8 Pairs sessions resume as `Classic · <difficulty>`

Pair counts may appear as structural or accessibility information. They are not separate
player-selectable normal modes.

## Menu, Replacement, And Replay

Quick and Classic use the established split primary CTA:

- the primary region starts the option using the selected difficulty
- the trailing region opens the supported single-choice difficulty popup

While a normal generated session is resumable, selecting Quick or Classic opens the existing
resume-or-replace dialog. The saved action identifies the saved Quick or Classic challenge. The
new-puzzle action identifies the requested option and difficulty without revealing or selecting
the random Quick size before confirmation.

`Play another`:

- performs a fresh weighted selection for Quick at the completed difficulty
- starts the same exact Classic challenge at the completed difficulty
- does not consult or rewrite the remembered Menu preference
- preserves the solved puzzle until a validated successor is stored

## Daily Challenge Compatibility

Daily Challenge remains a cadence, not a Generated Play Option.

The immutable recipe `daily-4-pairs-low-v1` continues selecting exact `4 Pairs Low` content with
its existing deterministic seed schedule. It does not call the Quick selector, read Quick
difficulty, or adopt Quick probabilities.

Daily may identify `4 Pairs Low` as structural content in accessibility or share text. This does
not reintroduce 4 Pairs as a normal Menu option.

Daily Session persistence, completion history, calendar, rollover, sharing, and its independence
from the normal generated-session slot remain unchanged.

## `3 Pairs Medium` Profile Expectations

`3 Pairs Medium` must be calibrated for the smaller shape rather than copied or truncated from
`4 Pairs Medium`.

The profile must:

- contain 3 solution pairs, 6 strip entries, and 6 board tiles
- preserve the shared Medium meaning of greater opening ambiguity than Low
- define explicit value, result, masking, variety, and bounded assessment policies
- support deterministic generation and reproducible characterization
- demonstrate a meaningful distinction from `3 Pairs Low`
- remain within documented attempt, search-work, assessment, and cancellation bounds

If characterization cannot establish a credible Medium distinction for the smaller shape, the
profile must be recalibrated before Menu exposure.

## Accessibility And Localization

- Quick and Classic names, difficulties, actions, state descriptions, and replacement copy are
  localized.
- Accessibility copy may include the resolved pair count once an exact challenge exists.
- Before Quick selection, accessibility identifies the requested option and difficulty without
  claiming one pair count.
- Selection state is communicated through semantics and text, not color alone.
- Split actions preserve minimum touch targets, logical ordering, compact layouts, supported font
  scales, LTR, and RTL behavior.

## Delivery Stages

1. Document this product contract and the play-option architecture.
2. Implement and characterize `3 Pairs Medium`.
3. Add Quick and Classic play-option resolution with controllable weighted selection.
4. Migrate remembered difficulty ownership to Quick and Classic.
5. Replace the normal Menu entries and integrate selection with session routing and replay.
6. Protect compatibility, align current documentation, and complete milestone validation.

## Out Of Scope

- Changing core pairing, operator, usage, or completion rules
- Renaming persisted generated-mode, challenge, or profile identities
- Changing the generated-session snapshot schema
- Making Daily Challenge probabilistic or changing its v10 recipe
- Quick Hard, Classic Low, 3 Pairs Hard, 4 Pairs Hard, or 8 Pairs Low
- Adaptive weighting, streak smoothing, progression, locks, scores, timers, or rewards
- Accounts, servers, remote configuration, analytics-driven weighting, or cloud synchronization
- Replacing authored Tutorial content

## Success Criteria

- The normal Menu exposes Quick and Classic instead of separate Quick, 4 Pairs, and 8 Pairs
  actions.
- Quick Low and Medium each select 3 Pairs in exactly 35 of 100 uniform buckets and 4 Pairs in the
  remaining 65.
- `3 Pairs Medium` is validated, bounded, calibrated, and characterized.
- Resume and retry keep the exact challenge; Quick replay performs a fresh weighted selection.
- Existing supported difficulty preferences migrate to the corresponding play option.
- Existing valid generated-session snapshots resume with Quick or Classic presentation.
- Classic continues providing the original 16-number, 16-result board.
- Daily continues resolving deterministic exact `4 Pairs Low` content independently.
- Localization, accessibility, compact layout, RTL, unit, lint, and instrumented-test compilation
  validation succeed.
