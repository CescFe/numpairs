# Generated Session Persistence

## Document Status

- Status: implemented normal generated-session persistence, remembered selectors, and v10 Quick
  challenge identity
- Product contracts: `docs/product/prd/prd-v7.md`, `docs/product/prd/prd-v8.md`, and
  `docs/product/prd/prd-v10.md`
- Related generation reference: `docs/product/puzzle-generation.md`
- Related domain decision: `docs/technical/adr/adr-005-model-sparse-generated-challenges.md`
- Implemented Daily boundary: `docs/technical/daily-challenge-persistence.md`

This document owns the persistence and coordination boundary for the single resumable normal
generated puzzle. It does not own Daily Challenge persistence and does not redefine puzzle rules,
generation profiles, or menu copy.

---

## Scope And Ownership

NumPairs stores at most one normal generated session for the whole application. The slot may
belong to any challenge in the supported generated-challenge catalog: Quick `3 Pairs Low`,
`4 Pairs Low`, `4 Pairs Medium`, `8 Pairs Medium`, or `8 Pairs Hard`. There is no independent
normal save per mode, normal generated history, account sync, or manual save management.

`MainActivity` creates one application-scoped `GeneratedSessionRepository` from
application-private storage and passes it only through generated-play and unlocked-navigation
composition. Tutorial and onboarding do not receive generated-session persistence callbacks and
cannot replace the slot.

The v10 Daily Challenge owns a separate aggregate and may remain resumable at the same time. Daily
operations never mutate this repository, and normal generated operations never mutate the Daily
aggregate. See `docs/technical/daily-challenge-persistence.md`.

The remembered difficulty selections are a separate preference aggregate, not generated-session
state. Session creation and restoration may read a challenge choice supplied by navigation, but
the generated-session repository never owns or mutates selector defaults.

---

## Remembered Difficulty Selection

The application keeps one stable difficulty id for each generated mode that exposes a difficulty
selector in local preference storage. The preference boundary exposes an observable effective
selection only for configured selector modes and accepts only configured mode/difficulty pairs
present in the supported challenge catalog.

The effective fallback is `Low` for `4 Pairs` and `Medium` for `8 Pairs`. Quick exposes only Low,
has no selector fallback or remembered preference, and rejects preference writes. Missing,
corrupt, unknown, and no-longer-supported stored values resolve to the selector mode's fallback
without writing it back. The only operation that writes a remembered selection is an explicit
supported option choice made by the player in that mode's anchored difficulty popup in the normal
menu.

Opening or dismissing the popup, showing a fallback, pressing a mode's play region, resuming or
restoring a session, replacing a session, completing a puzzle, and using `Play another` do not
write this preference. The two mode values remain independent, and neither completion nor any
other v8 behavior stores progression, locks, completion counts, rewards, or statistics.

The remembered values live in the dedicated Preferences DataStore file
`datastore/generated_difficulty_selection.preferences_pb`. This keeps the aggregate and its
corruption recovery independent from personalization, onboarding, and the resumable generated
session. The file stores one stable difficulty id under each selector-enabled generated-mode
identity; it does not store a Quick value, display copy, profile parameters, completion data, or
transient selector state.

`MainActivity` creates one application-scoped remembered-difficulty repository. The unlocked Menu
route observes both effective selections and receives the write operation used by its
mode-specific popups. Menu resume, generated-session restoration, generated gameplay, and
`Play another` resolve their exact stored challenge without mutating this preference boundary.

---

## Versioned Snapshot

Schema version `1` continues to store:

- stable generated-session id
- generated-mode id
- generated-profile id
- generation seed
- exact initial `Puzzle`
- exact current `Puzzle`

The seed is diagnostic and generation metadata. Restoration never reruns the generator because a
generator or profile implementation may change after the session was created.

The stored mode/profile pair resolves one exact supported `GeneratedChallenge`; restoration does
not assume that a mode has only one profile. Quick uses `three-pairs` with `3-pairs-low`; the
existing `4 Pairs Low` and `8 Pairs Medium` ids retain their meaning, so valid schema-1 snapshots
for those challenges remain compatible. An unknown mode, unknown profile, unsupported pair, or
profile whose declared mode differs from the stored mode is invalid session data and is exposed
as an empty slot.

The initial and current puzzles preserve:

- board tile order and results
- expression operands, operators, and strip-entry references
- strip entry ids, order, and origin state (`Hidden`, `Known`, or `PlayerEntered`)

Snapshot construction rejects changed board results, changed strip-entry identity sets, changed known values, and invalid initial player-entered values. Puzzle completion remains derived from `currentPuzzle.isSolved`; no separate persisted completion flag is a second source of truth.

The deterministic codec returns typed decoded, unsupported-version, or invalid-data outcomes. Unsupported or invalid session bytes are exposed as an empty slot, so they do not block startup or a later replacement.

---

## Repository Contract

The repository exposes one observable nullable snapshot and three atomic mutations:

- `replace(snapshot)` adopts a successor
- `updateCurrentPuzzle(expectedSessionId, puzzle)` updates only the owning session
- `clear(expectedSessionId)` clears only the owning session

Update and clear compare the expected stable id inside the DataStore edit. A callback from an older screen therefore cannot update or clear a newer replacement.

Generated gameplay forwards only committed domain `Puzzle` changes. Draft text, open selectors, dialogs, overlays, highlights, scroll position, and other presentation state are not persisted. The generated presentation owner orders progress writes so a slower older update cannot finish after a newer update for the same session.

---

## Lifecycle

### Create And Replace

1. Resolve the requested supported challenge and generate and validate a puzzle through its
   bounded generation pipeline.
2. Build a new versioned snapshot with identical initial and current puzzles.
3. Store the snapshot.
4. Publish the playable successor.

The previous slot remains intact while generation or storage is pending. Failure or cancellation keeps it intact. A successful stored successor becomes the single session before it is shown as ready.

### Restore

Resume identifies the expected stable session id. Restoration reads the slot and requires matching
session id plus a mode/profile pair that still resolves the exact stored challenge, and an unsolved
current puzzle. It then presents the exact current puzzle without invoking generation, consulting
the remembered selector default, or mutating either repository.

Missing, stale, mismatched, solved, corrupt, or unsupported sessions are not presented as resumable gameplay. The route offers a safe return to the menu.

### Progress And Completion

Committed strip values, operand assignments, operator assignments, and tile resets replace `currentPuzzle` for the active id. When the puzzle becomes solved, the same identity guard clears the slot. The solved game remains visible in memory for its completion actions, but the normal menu no longer exposes `Resume`.

`Play another` uses the create-and-replace pipeline with the completed session's exact mode and
profile. It does not consult or update the remembered selector default. A late clear from the
solved session cannot clear the successor because their stable ids differ.

---

## Local Storage And Transfer

The snapshot is encoded as one byte-array value in the dedicated Preferences DataStore file:

`datastore/generated_session.preferences_pb`

Room or another relational database is not used because the product owns one versioned aggregate with atomic replacement, not a queryable collection.

The file is excluded from:

- legacy Android Auto Backup in `res/xml/backup_rules.xml`
- Android cloud backup in `res/xml/data_extraction_rules.xml`
- Android device-to-device transfer in `res/xml/data_extraction_rules.xml`

This keeps the session local to the installation and avoids restoring a transient unfinished puzzle as cross-device progression.

The implemented Daily aggregate uses its own separately excluded
`datastore/daily_challenge.preferences_pb` file.

---

## Verification Boundaries

The non-device test suite protects:

- deterministic snapshot round trips and malformed/versioned input
- one-slot replacement, identity-guarded update/clear, and DataStore recreation
- persistence before readiness
- exact restoration without generation or writes
- ordered committed progress and solved clearing
- stale callback rejection
- replacement success, failure, cancellation, and duplicate-request handling
- simultaneous Quick and Daily restoration across repository recreation without cross-slot
  replacement, clearing, or completion side effects

Instrumented sources compile the menu, dialog, resume, completion, and committed-change navigation regressions. Repository work does not require starting an emulator during delivery validation.
