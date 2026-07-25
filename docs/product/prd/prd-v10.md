# PRD - NumPairs 🗓️ v10 Quick Play & Daily Challenge

> Delivered product contract for the `v10 - Quick Play & Daily Challenge` milestone.
> The implemented v8 difficulty-selection and v9 game-feel product is the baseline entering v10.
> Delivery status: all six stages are implemented and validated; the GitHub milestone remains
> open for manual closure.

The **Current Baseline At Start Of v10** and **Delivery Stages** sections preserve the historical
planning baseline and dependency order used to deliver the milestone. Later implementation-status
statements and current references describe the shipped repository result.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle. Players complete hidden board
expressions using numbers from an ordered strip. Each solution pair contributes one addition tile
and one multiplication tile.

Generated play now offers:

- `Quick` (`3 Pairs Low`)
- `4 Pairs Low`
- `4 Pairs Medium`
- `8 Pairs Medium`
- `8 Pairs Hard`

Players can launch Quick directly, choose a supported difficulty for 4 Pairs and 8 Pairs, resume
one exact normal generated session, and replay the same challenge after completion.

v10 delivered two complementary ways to play:

- `Quick`, a replayable generated `3 Pairs Low` mode for brief, approachable sessions
- `Daily Challenge`, one deterministic `4 Pairs Low` puzzle for each device-local calendar date

Quick is a generated mode because it introduces a stable puzzle-size family. Daily Challenge is
not a generated mode or difficulty: it is a date-bound play cadence that selects one existing
generated challenge through a versioned daily recipe.

Daily progress, completion history, and the normal generated-session slot remain independent.
The milestone requires no server, account, remote database, or network connection.

---

## Product Goal

Give players one immediate short-session entry point and one lightweight daily reason to return
without changing the core NumPairs rules or adding competitive pressure.

The milestone should make generated play more approachable, preserve reliable normal sessions,
and establish deterministic daily content whose identity and history remain understandable across
process death, local-date rollover, and future application updates.

---

## Problem Statement

The implemented product has several related gaps:

- the smallest generated challenge still contains four solution pairs
- players leaving Tutorial do not have an explicitly short generated destination
- every normal generated puzzle is selected on demand, so there is no shared date-bound challenge
- the existing random seed source cannot guarantee that every player requests the same daily
  puzzle
- a date-derived seed alone does not preserve content identity if a generator or profile changes
- the one normal generated-session slot cannot also own Daily progress without one cadence
  replacing the other
- the product has no local daily completion identity, history surface, or non-spoiling share result
- local-date rollover and device-clock behavior are undefined

v10 should resolve these gaps without turning Daily Challenge into a server-backed live service,
making Quick part of authored Tutorial, or coupling Daily state to remembered generated-mode
difficulty.

---

## Target Users

- New players who want a brief first generated challenge after Tutorial
- Casual players who want to complete a puzzle in a few minutes
- Returning players who appreciate one recognizable daily ritual
- Offline players who should receive the complete v10 experience without an account
- Players who want to share completion without revealing the puzzle
- Contributors who need puzzle size, difficulty, cadence, session, and completion identity to have
  explicit ownership

---

## Current Baseline At Start Of v10

The product entering v10 includes:

- mandatory versioned onboarding and voluntary `How to play` replay
- generated `4 Pairs Low`, `4 Pairs Medium`, `8 Pairs Medium`, and `8 Pairs Hard`
- a sparse generated-challenge catalog with mode and difficulty as independent concepts
- deterministic, bounded, validated generation and difficulty assessment
- remembered difficulty selection for `4 Pairs` and `8 Pairs`
- one exact resumable normal generated session shared by every generated challenge
- safe normal-session replacement only after a validated successor is stored
- completion actions for `Play another` and `Back to menu`
- persistent accessible color themes and generated-game haptic and motion feedback
- stable strip-entry identity and support for repeated numeric values

The baseline does not include:

- a generated puzzle smaller than four pairs
- a player-facing Quick mode
- date-bound generated content
- a daily recipe or daily content version
- a separate resumable daily session
- local Daily completion history
- a calendar or Daily share result
- accounts, cloud state, timers, scores, streaks, or notifications

---

## Product Concepts

### Quick

Quick is the player-facing identity for the generated `3 Pairs` mode.

The mode owns a puzzle size of:

- 3 solution pairs
- 6 strip entries
- 6 board tiles

v10 supports exactly one Quick challenge: `3 Pairs Low`. The challenge is registered explicitly in
the sparse generated-challenge catalog. Unsupported `3 Pairs Medium` and `3 Pairs Hard`
combinations are absent.

Quick is not a new difficulty tier and does not reinterpret Low. Its profile must be calibrated for
the smaller shape rather than produced by truncating `4 Pairs Low`.

### Daily Challenge

A Daily Challenge is the one date-bound playable puzzle selected by a daily recipe for a local
calendar date.

Daily Challenge owns cadence and daily identity. It does not own puzzle-size rules, difficulty
rules, or an independent generated profile.

The v10 recipe selects `4 Pairs Low` for every supported date.

### Daily Challenge Identity

One Daily Challenge identity contains:

- the device-local calendar date
- a stable daily recipe version

The selected generated challenge, profile, and generation seed must be consistent with that
identity, but derivable values should not become competing sources of truth.

The identity is independent from the date on which the puzzle is eventually completed. A puzzle
opened before midnight remains the Daily Challenge for its captured date.

### Daily Recipe

A Daily Recipe is one immutable versioned definition that owns:

- the selected generated challenge
- stable seed derivation from Daily Challenge identity
- an ordered deterministic fallback sequence
- the supported generation and validation policy

Every installation using the same recipe version must request candidates in the same order for the
same local calendar date. Generation may stop at the first valid candidate, but it must not fall
back to device randomness.

A release must characterize a documented future date corpus so an exhausted first candidate does
not make Daily availability accidental. Runtime generation remains bounded and reports typed
failure.

Changing the recipe requires a new stable version. A generator or profile change must not silently
change the meaning of an existing recipe version.

### Daily Session

A Daily Session is the exact playable lifecycle for one Daily Challenge identity.

NumPairs owns at most one Daily Session slot, separate from the existing normal generated-session
slot. The two slots may be resumable simultaneously and cannot replace, clear, or update one
another.

The Daily Session stores an exact versioned initial and current puzzle snapshot. Its recipe
version, selected challenge identity, and seed remain metadata and validation inputs; restoration
never regenerates historical progress from them.

### Daily Completion

A Daily Completion records that the puzzle for one Daily Challenge identity became solved on the
current installation.

Completion is derived from the committed current puzzle becoming solved. Opening a puzzle,
restoring it, viewing a completion surface, sharing, or displaying a fallback does not create a
completion record.

At most one completion record exists for each Daily Challenge identity. Completion records do not
contain a score, duration, action count, streak, reward, or full puzzle snapshot.

---

## Product Principles

- Keep the core NumPairs rules unchanged.
- Treat Quick as a size family and Low as its independent difficulty.
- Treat Daily Challenge as cadence over generated content, not as another size or difficulty.
- Make the Daily identity explicit and version its content recipe.
- Keep deterministic generation bounded and independent from device randomness.
- Restore exact persisted progress instead of regenerating historical content.
- Let normal and Daily sessions coexist without replacement or preference side effects.
- Record completion from solved domain state rather than a presentation event.
- Keep the daily calendar factual and non-competitive.
- Share completion without exposing puzzle content.
- Trust the device-local date while documenting the lack of anti-cheat guarantees.
- Preserve authored Tutorial ownership and recommend Quick only after learning.
- Complete authoritative documentation before production implementation.

---

## Core UX Expectations

### Normal Menu Hierarchy

The unlocked normal menu presents:

1. the state-aware Daily Challenge action
2. normal generated `Resume`, only while the normal slot is resumable
3. `Quick`
4. the `4 Pairs · <difficulty>` split primary CTA
5. the `8 Pairs · <difficulty>` split primary CTA
6. `How to play`
7. `Settings`

The final visual grouping and spacing may distinguish daily, generated-play, learning, and settings
roles, but Daily Challenge, normal Resume, and generated mode actions must remain clearly
identifiable on compact screens and at supported font scales.

### Quick Launch And Replay

- Quick launches its only supported `3 Pairs Low` challenge directly.
- Quick does not show a redundant difficulty selector in v10.
- Starting Quick participates in the existing normal resume-or-replace flow.
- Quick uses the normal generated-session slot.
- A stored Quick session appears through the normal `Resume` action with Quick-aware copy.
- `Play another` creates another `3 Pairs Low` puzzle.
- Quick uses the established generated-game haptics, correct-tile motion, completion celebration,
  safe replacement transition, failure handling, rules helper, and accessibility behavior.

Quick may be presented as the recommended first generated mode after Tutorial, but required
onboarding and voluntary Tutorial replay remain authored and unchanged.

### Daily Menu States

The Daily Challenge action has three player-facing states for the current local date:

- start today
- continue today
- completed today

Starting today creates the current Daily Challenge through its recipe and persists the exact
session before presenting it as ready.

Continue today restores the exact current puzzle when the stored Daily Session identity matches
today and remains unsolved.

Completed today opens the current Daily completion surface or calendar context without replaying
the puzzle or generating a replacement.

The Daily action does not consult or rewrite the remembered `4 Pairs` difficulty selection even
though the v10 recipe selects `4 Pairs Low`.

### Coexisting Resumable Sessions

An unfinished normal generated session and an unfinished Daily Session may coexist.

- the normal `Resume` action restores only the normal slot
- the Daily continue state restores only the Daily slot
- starting or replaying Quick, 4 Pairs, or 8 Pairs never mutates Daily state
- starting, continuing, or completing Daily never mutates the normal generated slot

Daily does not use the normal resume-or-replace dialog because it has a separate slot and one
content identity for the current date.

### Local-Date Rollover

The application reads the device-local calendar date when resolving the current Daily Challenge.
The device clock and time zone are trusted inputs.

If the local date changes while Daily gameplay remains visible:

- the active puzzle remains bound to the date captured by its Daily Challenge identity
- the screen does not replace or reset the puzzle at midnight
- solving it records completion for its captured Daily Challenge identity

After the player returns to the menu on a later local date:

- an unfinished prior-date Daily Session is no longer offered as resumable
- the current date resolves a new Daily Challenge identity
- starting the current Daily safely replaces the stale daily slot only after the successor is
  generated, validated, and stored
- the previous date cannot be reopened or backfilled through the calendar

Moving the device clock backward may expose a locally matching stored session or completion again.
v10 does not attempt to detect or punish manual clock changes.

### Daily Gameplay And Completion

Daily reuses the core generated Game screen, shared rules, assignments, validation, haptics,
correct-tile feedback, and completion celebration.

The Daily completion surface provides:

- `Share result`
- `View calendar`
- `Back to menu`

It does not provide `Play another`, change difficulty, restart Daily, or reveal the solution.

A completed Daily Challenge cannot be replayed as Daily. The same `4 Pairs Low` profile remains
available without restriction through normal generated play.

### Completion Calendar

The calendar provides a local monthly view.

- completed dates are visibly marked
- the current local date is identifiable
- future dates are disabled
- past dates without completion remain neutral and are not labelled as failures or missed streaks
- dates do not open historical gameplay
- changing months does not modify Daily Session or completion data
- state is communicated through text or semantics in addition to color

The calendar is a history surface, not a progression system. It does not show streaks, percentages,
rewards, rankings, or a completion target.

### Textual Sharing

Sharing uses the Android Sharesheet and contains localized text identifying:

- NumPairs Daily
- the Daily Challenge date or stable daily label
- `4 Pairs · Low`
- successful completion

The shared result must not contain:

- strip values
- board results
- solution pairs
- operators
- tile positions
- elapsed time
- score or action count

Sharing has no effect on session, completion, calendar, onboarding, or preference state.

---

## v10 Scope

### Documentation-First Contract

- Add this PRD and update the README before supporting document or code changes.
- Define Quick, Daily Challenge, Daily Challenge identity, Daily Recipe, Daily Session, and Daily
  Completion in the ubiquitous language.
- Record the durable Daily identity, recipe, session, and history boundaries in an ADR.
- Document the calibrated `3 Pairs Low` profile before implementing it.
- Document menu, rollover, calendar, completion, sharing, localization, and accessibility behavior.
- Align generation, persistence, game-rule, UI, and current-product references.

At delivery start, no production or test-code implementation for v10 began until the planned
product, domain, generation, persistence, and UI documentation was delivered.

### Quick Profile And Generated Integration

- Add a validated `3 Pairs Low` generated profile.
- Keep generation deterministic for tests and bounded on attempts, search work, assessment, and
  cancellation.
- Characterize the smaller challenge over a deterministic seed corpus.
- Register a stable `3 Pairs` generated mode and one supported Low challenge.
- Integrate Quick with normal generated generation, snapshots, restoration, replacement, replay,
  presentation ownership, titles, localization, rules help, and feedback.
- Add direct Quick menu and navigation behavior without a one-option selector.

### Versioned Daily Generation

- Add stable Daily Challenge and recipe identities independent from Android display text.
- Resolve the device-local date through an explicit controllable clock boundary.
- Derive deterministic candidate seeds without platform or device randomness.
- Attempt a fixed ordered fallback sequence under bounded generation.
- Validate a documented future date corpus for the v10 recipe.
- Resolve the recipe to the exact supported `4 Pairs Low` challenge.
- Keep recipe identity separate from remembered generated difficulty preferences.

### Daily Persistence And History

- Add one application-scoped Daily Session repository independent from normal generated sessions.
- Persist one versioned exact Daily Session snapshot.
- Protect update, completion, clear, and replacement operations with stable identity.
- Keep the prior daily slot intact while a current-date successor is pending or fails.
- Add local completion storage keyed by Daily Challenge identity.
- Make completion recording idempotent and derived from solved current puzzle state.
- Recover safely from malformed, unsupported, stale, or mismatched local data.
- Keep Daily Session and completion history local to the installation and excluded from Android
  backup and device transfer.

### Daily And Quick Surfaces

- Add the state-aware Daily Challenge action and direct Quick action to the normal menu hierarchy.
- Preserve normal Resume and both generated difficulty selectors.
- Add Daily loading, ready, failure, continuation, stale-date, and completed states.
- Add the monthly completion calendar.
- Add Daily-specific completion actions and Android Sharesheet integration.
- Reuse established NumPairs components, theme roles, typography, shapes, spacing, and semantics.
- Keep layouts usable across compact, wide, text-scaled, LTR, and RTL configurations.

### End-To-End Quality

- Protect deterministic Quick and Daily generation with non-device tests.
- Protect independent normal and Daily session ownership across process recreation.
- Protect rollover, stale callbacks, replacement failure, idempotent completion, and corrupt data.
- Protect Menu, Quick, Daily, calendar, completion, and share semantics with focused Compose or
  navigation coverage where rendering matters.
- Preserve Tutorial, onboarding, settings, themes, difficulty selection, generated feedback, and
  normal session behavior.
- Complete the Compose design-system consistency pass for every affected surface.
- Align current documentation with the implemented result.

---

## Delivery Stages

### Stage 1 - Authoritative Product And Architecture Contract

Outcome: v10 scope, terminology, profile rules, Daily identity, persistence, rollover, menu,
calendar, and sharing behavior are documented before production work.

Work:

1. Add the v10 PRD and update the README.
2. Document the Quick generated challenge and `3 Pairs Low` profile.
3. Record the Daily identity and persistence architecture.
4. Document Menu, calendar, completion, rollover, and sharing behavior.

### Stage 2 - Quick Generated Play

Outcome: players can start, resume, replace, complete, and replay a calibrated `3 Pairs Low`
challenge through the normal generated-play contract.

Work:

1. Implement and characterize the validated `3 Pairs Low` profile.
2. Register the `3 Pairs` mode and Quick challenge.
3. Integrate Quick with normal generated sessions and presentation.
4. Add direct Quick menu, navigation, localization, and focused UI coverage.

### Stage 3 - Deterministic Daily Identity And Generation

Outcome: each local date and recipe version resolves one bounded deterministic `4 Pairs Low`
Daily Challenge.

Work:

1. Implement Daily Challenge and recipe identity.
2. Add the controllable local-date boundary and deterministic seed schedule.
3. Implement ordered deterministic fallback over existing generation.
4. Characterize and protect the v10 future date corpus.

### Stage 4 - Independent Daily Session And Completion History

Outcome: Daily progress and local completion survive process death without occupying or mutating
the normal generated-session slot.

Work:

1. Add the versioned Daily Session snapshot and codec.
2. Add identity-guarded Daily Session persistence and safe replacement.
3. Coordinate Daily generation, restoration, progress, rollover, and completion.
4. Add idempotent local completion history and corruption recovery.

### Stage 5 - Daily Menu, Calendar, And Sharing

Outcome: players can discover, start, continue, complete, review, and share the current Daily
Challenge through accessible v10 surfaces.

Work:

1. Integrate the state-aware Daily action and direct Quick action into the Menu hierarchy.
2. Add Daily gameplay and completion routing.
3. Add the local monthly calendar.
4. Add non-spoiling textual sharing through the Android Sharesheet.

### Stage 6 - End-To-End Quality And Product Alignment

Outcome: Quick and Daily remain coherent with every existing product boundary and the implemented
behavior is reflected by current documentation.

Work:

1. Add cross-feature and process-recreation regressions.
2. Verify local-date rollover, failure, cancellation, stale identity, and reduced-feedback paths.
3. Complete localization, accessibility, compact-layout, RTL, and design-system passes.
4. Align current product, generation, session, UI, and README references.
5. Run milestone formatting, unit, lint, and instrumented-test compilation validation.

---

## Out Of Scope

- Accounts, sign-in, servers, remote configuration, or cloud synchronization
- Cross-device Daily Session or completion history
- Anti-cheat guarantees for clock changes, time-zone changes, data deletion, or reinstallation
- Streaks, scores, timers, rankings, achievements, rewards, or progression gates
- Notifications, reminders, widgets, or calendar-system integration
- Playing, completing, replaying, or backfilling past Daily Challenges
- More than one Daily Challenge per local date
- Player-selected Daily difficulty or rotating Daily profiles
- Curated remote puzzles or remotely changing the daily recipe
- A `3 Pairs Medium` or `3 Pairs Hard` challenge
- Replacing authored Tutorial puzzles with generated Quick puzzles
- Daily hints, answer reveal, or solution sharing
- New operators or changes to core NumPairs completion rules
- Persisting transient dialogs, drafts, selectors, animations, calendar scroll, or Sharesheet state

---

## Success Criteria

- Quick is modeled as the generated `3 Pairs` size family and presented through one explicit
  `3 Pairs Low` challenge.
- Players can start and replay Quick with 3 solution pairs, 6 strip entries, and 6 board tiles.
- Quick generation is validated, deterministic for tests, bounded, calibrated, and characterized
  for the smaller puzzle shape.
- Quick uses normal generated resume, safe replacement, completion, replay, feedback, localization,
  and accessibility behavior without a redundant difficulty selector.
- Each device-local calendar date and Daily Recipe version resolves one bounded deterministic
  `4 Pairs Low` Daily Challenge.
- Installations using the same recipe version request the same candidate sequence for the same
  local date.
- Daily restores exact committed progress without regenerating from seed.
- Daily and normal generated sessions can remain resumable simultaneously without replacing,
  clearing, or updating one another.
- Solving Daily records at most one local completion for its Daily Challenge identity.
- Prior-date incomplete Daily Sessions are not exposed for backfill after returning to Menu on a
  later local date.
- Completed dates appear in an accessible local monthly calendar without streak or missed-day
  pressure.
- Daily completion can be shared textually without exposing puzzle content.
- Device-clock manipulation, reinstall, and data deletion remain documented local-trust limits
  rather than partially implemented anti-cheat.
- Existing `4 Pairs`, `8 Pairs`, difficulty selection, normal Resume, Tutorial, onboarding,
  settings, personalization, reliable sessions, and generated-game feedback remain coherent.
- Documentation precedes implementation, every stage is delivered through dependency-ordered
  atomic issues and Pull Requests, and milestone-level validation succeeds.
