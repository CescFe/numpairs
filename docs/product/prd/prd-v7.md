# PRD - NumPairs 🔁 v7 Reliable Sessions & Replay Controls

> Implemented product reference for the v7 milestone. The product entering this milestone was the completed v6 Guided First Run baseline.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle. Players complete hidden board expressions using numbers from an ordered strip. Each strip pair produces one addition tile and one multiplication tile, and generated play currently supports the replayable `4 Pairs Low` and `8 Pairs Medium` modes.

v6 added a reliable first-run contract by persisting versioned onboarding progress. Generated puzzle sessions still live only in memory: they survive recomposition and configuration recreation, but Android process death loses the active puzzle and every committed player assignment.

v7 makes the most recently opened generated puzzle resumable. It introduces one local session slot shared by generated modes, a conditional `Resume` action in the normal menu, and an explicit resume-or-new-puzzle choice when a generated-mode action would replace unfinished play.

---

## Product Goal

Let players safely leave and resume the most recently opened unfinished generated puzzle without adding accounts, multiple save slots, or broader progression systems.

The session contract should make `8 Pairs Medium` practical for longer play while keeping generated-mode selection, replacement, and recovery behavior simple and predictable.

---

## Problem Statement

Generated sessions currently survive only while their activity-scoped presentation owners remain alive. Android may terminate the application process after the player leaves the app, causing the current puzzle and all committed strip and tile assignments to disappear.

The existing product also lacks a menu-level contract for choosing between unfinished play and a new generated puzzle. Without that contract:

- a long `8 Pairs Medium` attempt can be lost after process death
- the menu cannot communicate that unfinished play is available
- selecting a generated mode cannot distinguish resuming from replacement
- a failed replacement could discard the only recoverable session if persistence is handled eagerly
- seed-only restoration could change the puzzle after generator implementation changes
- adding one independent saved session per mode would introduce parallel save slots that the product does not need

v7 should establish one reliable session lifecycle without changing NumPairs rules or turning the menu into a save-game manager.

---

## Target Users

- Players who solve `8 Pairs Medium` across longer or interrupted sessions
- Players whose Android process is terminated while NumPairs is in the background
- Returning players who want one clear way to resume their latest generated puzzle
- Players who intentionally choose a fresh puzzle instead of unfinished play
- Contributors who need an explicit persistence and replacement contract

---

## Current Baseline At Start Of v7

The product entering v7 is the completed `v6 - Guided First Run` baseline.

That baseline includes:

- mandatory versioned onboarding for fresh installations
- resumable onboarding checkpoints and safe voluntary `How to play` replay
- generated `4 Pairs Low` and `8 Pairs Medium`
- stable generated-mode and generated-profile identities
- deterministic, bounded, validated generated-puzzle requests
- stable strip-entry identity across strip reordering
- activity-scoped generated and game presentation state
- generated sessions that survive recomposition and configuration recreation
- replay that keeps the completed puzzle visible until a validated replacement is ready
- recoverable generation failure handling
- local DataStore-backed preferences for onboarding and action discovery

The baseline does not include:

- restoration of a generated session after Android process death
- a durable snapshot of the initial and current generated puzzle
- a normal-menu `Resume` action
- a resume-or-new-puzzle choice before generated-mode replacement
- a single-session ownership contract shared across generated modes
- safe recovery from incompatible or corrupt generated-session data

---

## Product Principles

- Preserve one generated session, not one session per mode.
- Resume the exact committed puzzle state rather than regenerating from its seed.
- Treat the seed as session metadata and diagnostic identity, not as the persisted puzzle.
- Keep Tutorial, onboarding, and generated-session state independent.
- Replace a resumable session only after its successor is generated, validated, and stored successfully.
- Persist domain-significant player changes, not transient UI presentation.
- Keep generated-mode selection explicit whenever unfinished play exists.
- Keep the gameplay TopAppBar and in-puzzle interaction model unchanged.
- Recover safely from incompatible local data without blocking new play.
- Keep the feature local, account-free, and small enough to reason about as one session lifecycle.

---

## Core UX Expectations

- The normal menu order is:
  1. `Resume`, only while an unfinished generated session is resumable
  2. `Play 4 Pairs`
  3. `Play 8 Pairs`
  4. `How to play`
- `Resume` uses the same primary CTA treatment as the generated-mode actions.
- Selecting `Resume` opens the stored mode and exact committed puzzle state without generation.
- Selecting either generated-mode action while a resumable session exists opens a modal choice.
- The modal always offers `Resume` as its primary action.
- The modal secondary action starts a new puzzle for the mode the player selected.
- Same-mode and different-mode selection may use different supporting copy or secondary labels for clarity, but neither is a separate protection flow.
- The modal has no visible cancel, back, or close action.
- Tapping outside the modal or pressing system back dismisses it without changing the session.
- Selecting `How to play` never replaces or modifies the generated session.
- The gameplay TopAppBar remains unchanged.

---

## v7 Scope

### Single Resumable Generated Session

- Store at most one resumable generated session for the application.
- Let the session belong to either `4 Pairs Low` or `8 Pairs Medium`.
- Treat the stored session as the most recently opened unfinished generated puzzle.
- Let an untouched but opened generated puzzle remain resumable because the player may have spent time analysing it.
- Do not let Tutorial, required onboarding, or voluntary `How to play` replay replace the generated session.
- Remove resumability when the puzzle is completed or successfully replaced.
- Do not expose a completed puzzle through the normal-menu `Resume` action.
- Do not present multiple slots, per-mode saves, session history, or save management.

### Persisted Session Contract

Persist a versioned representation containing enough information to reconstruct the session exactly:

- session schema version
- stable session identifier
- generated-mode identifier
- generated-profile identifier
- generation seed
- initial player-facing puzzle snapshot
- current committed puzzle snapshot

The snapshots must preserve:

- board tile order and visible results
- strip entry order, stable identities, and item origin states
- player-entered strip values
- assigned operand values and their strip-entry identities
- assigned operators

Puzzle completion remains derived from the restored puzzle model. A separately persisted completion Boolean must not become an alternative source of truth.

Seed-only restoration is insufficient. A generator or profile implementation may change after an application update, so restoration must not require the current generator to reproduce historical content.

### Persistence Boundaries

- Persist every valid, committed strip or tile mutation.
- Treat a confirmed strip value, operand assignment, operator assignment, and tile reset as committed puzzle changes.
- Do not persist unconfirmed text drafts.
- Do not persist an open strip editor, operand selector, operator selector, rules helper, solving-tips dialog, scroll position, or success overlay presentation state.
- Validate restored data before converting it into a playable session.
- Discard only the affected session if its schema is unsupported, its mode/profile is unknown, or its puzzle violates construction invariants.
- Never let incompatible or corrupt session data prevent application startup or new generated play.

### Normal Menu Resume Action

- Observe the single session repository from the unlocked normal menu.
- Show `Resume` only when the repository exposes a valid unfinished generated session.
- Place `Resume` before `Play 4 Pairs`.
- Render `Resume`, `Play 4 Pairs`, and `Play 8 Pairs` with the shared primary CTA treatment.
- Keep `How to play` as the final secondary action.
- Use `Resume` in English.
- Localize the action naturally in every supported language rather than forcing a literal translation.
- Give the action accessibility semantics that identify the mode being resumed.
- Navigate directly to the saved mode and puzzle without a generation request.

### Generated-Mode Selection Dialog

When either `Play 4 Pairs` or `Play 8 Pairs` is selected while a resumable session exists:

- show a modal dialog before starting generation
- identify that an unfinished puzzle is available
- offer `Resume` as the primary CTA
- offer a new puzzle for the selected mode as the secondary CTA
- do not add a visible cancel or close action
- dismiss without side effects on outside tap
- dismiss without side effects on system back
- ignore duplicate action taps while a choice is being handled

For same-mode selection:

- explain that the player can resume the unfinished puzzle or generate a new puzzle
- use a concise secondary label such as `New puzzle`

For different-mode selection:

- explain that the unfinished puzzle can be resumed or replaced by the selected mode
- keep the primary label as `Resume`
- let the secondary label identify the selected mode, such as `Play 8 Pairs`

Both cases use the same session ownership and replacement rules. Different copy exists only to make the selected outcome clear.

### Safe New-Puzzle Replacement

- Keep the existing resumable session stored while a requested new puzzle is generating.
- Generate and validate the requested mode using the existing bounded generation pipeline.
- Replace the stored session only after the new initial puzzle has been stored successfully.
- Preserve the previous session when generation fails or is cancelled.
- Keep duplicate generation requests deduplicated.
- If replacement fails before entering a playable new session, allow the player to return to the menu where `Resume` still targets the previous session.
- Make the new puzzle the single resumable session as soon as it is successfully adopted.

### Completion And Replay

- Stop exposing `Resume` when the current puzzle reaches the solved state.
- Keep the completion surface focused on:
  - `Play another`
  - `Back to menu`
- Let `Play another` use the existing validated replay pipeline.
- Do not add `Change difficulty` or an equivalent third completion action.
- Do not add an in-game `Restart puzzle` action in v7.
- Preserve the existing rule that a completed session remains visible while its replacement is generated.

### Local Data And Backup

- Store the session in application-private local storage through a dedicated persistence boundary.
- Keep the session independent from existing onboarding and action-discovery preferences.
- Use atomic replacement semantics suitable for one small structured record.
- Exclude the generated session from cloud backup and device-to-device transfer.
- Treat uninstalling or clearing application data as removal of the session.
- Do not require a relational database for the single-session contract.

---

## Stages

### Stage 1 - Durable Session Foundation

Outcome: the application has one tested, versioned persistence boundary capable of storing, validating, restoring, updating, and clearing an exact generated-puzzle session.

Work:

1. Introduce the single generated-session contract, stable session identity, and persistence-facing snapshot model.
2. Implement a dedicated atomic local repository and versioned serializer.
3. Map snapshots to and from the current puzzle domain model while preserving strip-entry identity.
4. Define corrupt or incompatible data recovery and backup exclusion.
5. Expose committed puzzle changes from game presentation without leaking transient UI state into persistence.

### Stage 2 - Generated Session Lifecycle

Outcome: generated play creates, restores, updates, completes, and safely replaces the one durable session.

Work:

1. Restore an existing session without running the generator.
2. Persist newly generated sessions before presenting them as resumable.
3. Persist every committed puzzle mutation through generated-session coordination.
4. Remove resumability when a puzzle is solved.
5. Preserve the previous session until replacement generation and storage succeed.
6. Preserve existing loading, failure, cancellation, replay, and request-deduplication behavior.

### Stage 3 - Resume-Aware Menu And Mode Selection

Outcome: the normal menu exposes the conditional `Resume` CTA and resolves every generated-mode selection through the documented modal choice while a session exists.

Work:

1. Render the conditional `Resume` CTA in the required menu order and visual hierarchy.
2. Navigate `Resume` to the stored mode and session without generation.
3. Add the shared generated-mode selection dialog.
4. Provide clear same-mode and different-mode copy with `Resume` primary and selected new-puzzle action secondary.
5. Support outside-tap and system-back dismissal without a visible cancel action.
6. Localize and test the flow in English, Spanish, and Catalan.

### Stage 4 - End-To-End Reliability And Product Alignment

Outcome: the complete v7 contract is protected across restoration, failure, completion, accessibility, and documentation boundaries.

Work:

1. Add end-to-end navigation and UI regression coverage for resume and replacement flows.
2. Verify persistence across repository and application recreation without starting an emulator.
3. Verify invalid stored data fails safely and does not affect onboarding or Tutorial.
4. Align completion copy with `Play another`.
5. Update generated-session, UI behavior, terminology, backup, and current-product documentation.
6. Run milestone-level formatting, unit, lint, and instrumented-test compilation validation.

---

## Out Of Scope

- More than one resumable generated session
- One saved session per mode
- Multiple manual save slots or session history
- User accounts or cloud synchronization
- Cloud backup or device-to-device session transfer
- Exact navigation-destination restoration after process death
- Restoration of transient dialogs, drafts, focus, or scroll position
- In-game `New puzzle` or `Restart puzzle` TopAppBar actions
- Any gameplay TopAppBar change
- Timers, scoring, streaks, achievements, progression, or statistics
- Daily puzzles or remote puzzle identity
- New generated sizes, profiles, rules, operators, or solver guarantees
- Persistence of Tutorial puzzle edits or incomplete authored-stage edits
- A relational database introduced only for this session

---

## Success Criteria

- Only the most recently opened unfinished generated puzzle is resumable.
- `4 Pairs Low` and `8 Pairs Medium` do not retain independent resumable sessions.
- The normal menu shows `Resume` first only while a valid unfinished session exists.
- `Resume` restores the exact committed strip and board state after Android process death.
- Restoration preserves stable strip-entry identities and repeated-value assignments.
- Selecting either generated-mode CTA while a session exists shows the documented modal.
- The modal always uses `Resume` as its primary action.
- Same-mode and different-mode selections communicate the selected new-puzzle outcome clearly.
- Outside tap and system back dismiss the modal without changing the session.
- Starting a new generated puzzle replaces the old session only after generation, validation, and storage succeed.
- A failed or cancelled replacement leaves the previous session resumable.
- Completing the puzzle removes `Resume` and preserves the existing completion loop.
- Tutorial, onboarding, rules help, and solving tips never replace the generated session.
- Corrupt or incompatible session data never prevents startup or new play.
- The gameplay TopAppBar remains unchanged.
- Supported-language copy, accessibility semantics, unit tests, and compiled instrumented tests cover the v7 behavior.
