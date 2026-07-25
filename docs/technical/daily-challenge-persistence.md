# Daily Challenge Persistence

## Document Status

- Status: Daily identity, recipe resolution, and deterministic seed schedule implemented; local
  date access, session persistence, and completion history planned
- Product contract: `docs/product/prd/prd-v10.md`
- Architecture decision:
  `docs/technical/adr/adr-006-model-daily-challenge-as-versioned-local-cadence.md`
- Related generation reference: `docs/product/puzzle-generation.md`
- Normal generated-session boundary: `docs/technical/generated-session-persistence.md`

This document owns the v10 persistence and coordination boundary for one local Daily Challenge
aggregate. It does not redefine generated puzzle profiles, normal generated sessions, menu copy,
calendar layout, or core NumPairs rules.

---

## Scope And Ownership

NumPairs stores one application-private Daily aggregate containing:

- at most one Daily Session snapshot
- local Daily Completion records

The aggregate is independent from the one normal generated-session slot. An unfinished Daily
Session and an unfinished normal Quick, 4 Pairs, or 8 Pairs session may coexist. Operations on one
repository never read, replace, update, clear, or complete the other.

`MainActivity` creates one application-scoped Daily repository and one application-scoped
device-local date source. Daily feature coordination receives those dependencies alongside the
existing configured generated-challenge catalog and generation factory.

Tutorial, onboarding, remembered difficulty selection, Personalization, and Settings do not
receive Daily mutation callbacks.

---

## Daily Identity And Recipe Resolution

`DailyChallengeId` contains:

- canonical local date in ISO-8601 `YYYY-MM-DD`
- stable Daily Recipe version

The v10 recipe version `daily-4-pairs-low-v1` resolves the existing `4 Pairs Low` challenge. For
candidate indexes `0..3`, it hashes the UTF-8 payload
`daily-4-pairs-low-v1|YYYY-MM-DD|candidate-index` with 32-bit FNV-1a and uses the resulting signed
32-bit bit pattern as the generation seed. The payload, hash constants, overflow semantics,
challenge, order, and candidate limit belong to that recipe version.

Daily coordination captures the current local date once per entry request and constructs the
identity before generation begins. A midnight or time-zone transition does not rewrite the
identity of an in-flight or visible request.

The recipe resolver accepts only configured recipe versions. An unknown recipe makes an active
snapshot unsupported, but a completion record keeps its canonical completed date for calendar
history.

The platform-independent identity types, v10 recipe catalog, exact `4 Pairs Low` binding, and
four-candidate FNV-1a seed schedule are implemented. Current device-local date capture and
candidate generation remain outside this implemented boundary.

---

## Versioned Aggregate

Schema version `1` stores:

- an optional Daily Session snapshot
- a collection of Daily Completion records

The snapshot stores:

- stable Daily Session id
- canonical local date
- Daily Recipe version
- successful zero-based candidate index
- derived generation seed
- exact initial `Puzzle`
- exact current `Puzzle`

The selected generated mode, difficulty, and profile are derived from the recipe version and are
not persisted again. The candidate index and seed must agree with the recipe mapping.

The initial and current puzzles preserve:

- board tile order and results
- expression operands, operators, and strip-entry references
- strip entry ids, order, and origin state

Snapshot construction rejects changed board results, changed strip-entry identity sets, changed
known values, invalid initial player-entered values, mismatched recipe seed data, and puzzle shape
that does not match the recipe challenge.

Each completion record stores:

- canonical local date
- Daily Recipe version

The codec rejects duplicate completion identities and more than one completion record for the same
local date. It does not store a completion timestamp, score, duration, action count, display text,
or puzzle.

The deterministic codec returns typed decoded, unsupported-version, and invalid-data outcomes.
Malformed active-session data must not fabricate a resumable puzzle or completion. The
implementation should preserve independently valid completion records where the selected encoding
can isolate a malformed optional session; otherwise aggregate corruption recovers to an empty
local Daily state.

---

## Repository Contract

The repository exposes one observable state containing:

- nullable active Daily Session snapshot
- completed Daily Challenge identities

It owns these atomic mutations:

- `replaceSession(snapshot)` adopts a generated successor only when its date has no completion
- `updateCurrentPuzzle(expectedSessionId, puzzle)` updates only the owning unsolved session
- `complete(expectedSessionId, expectedDailyChallengeId, solvedPuzzle)` records one completion and
  removes the owning active session in the same edit

Every mutation validates stable session identity inside the DataStore edit. A callback from a
stale screen cannot update or complete a successor.

`complete` additionally requires:

- the supplied puzzle is solved
- it remains consistent with the active initial puzzle
- its Daily Challenge identity has no completion
- its local date has no completion under another recipe version

The operation is idempotent for already completed Daily identity and returns a typed outcome that
distinguishes completed, already completed, stale session, and invalid puzzle. It never clears a
session before the completion record is durable.

Daily gameplay forwards only committed domain puzzle changes. Draft text, open selectors,
dialogs, overlays, highlights, calendar month, scroll position, Sharesheet state, animations, and
other presentation state are not persisted.

---

## Lifecycle

### Resolve Current Daily State

For the captured local date:

1. If any completion record owns the date, expose completed-today state.
2. Otherwise, if the active snapshot has the exact current Daily Challenge identity, resolves its
   recipe, remains valid, and is unsolved, expose continue-today state.
3. Otherwise, expose start-today state.

A prior-date session is retained safely but is not exposed as resumable.

### Create And Replace

1. Capture local date and resolve the Daily Challenge identity.
2. Resolve the immutable recipe and selected generated challenge.
3. Attempt recipe candidate seeds in ascending index order.
4. Stop at the first generated and validated puzzle.
5. Build a new snapshot with identical initial and current puzzles.
6. Store the snapshot through `replaceSession`.
7. Publish the playable Daily Session.

The previous Daily slot remains intact while generation or storage is pending. Failure,
cancellation, or exhaustion leaves it intact. A successful stored successor replaces a stale
prior-date session before the new puzzle is shown.

Normal generated-session state is not consulted or mutated.

### Restore

Restoration requires:

- expected stable Daily Session id
- identity equal to the captured current Daily Challenge identity
- supported recipe version
- seed and candidate index matching that recipe
- exact puzzle shape matching the recipe challenge
- valid unsolved current puzzle
- no completion for the same identity or date

Restoration presents the exact current puzzle without generation, device randomness, remembered
difficulty lookup, or repository writes.

Missing, stale, prior-date, solved, mismatched, corrupt, completed, or unsupported snapshots are
not resumable. Prior-date content is not offered through the calendar.

### Progress And Completion

Committed strip values, operand assignments, operator assignments, and tile resets update the
active current puzzle through the stable Daily Session id.

When the puzzle becomes solved, the presentation owner orders the final mutation after earlier
progress writes and calls the atomic completion operation. The operation records the Daily
Challenge identity and removes the active session together. The solved game remains visible in
memory for Share result, View calendar, and Back to menu.

A late progress or completion callback cannot mutate a later Daily Session because the stable
session ids differ.

### Local-Date Rollover

An active route keeps its captured Daily Challenge identity when the date changes. Completion is
recorded for that captured identity.

After returning to Menu on a later local date, the old unfinished session is not resumable.
Starting the new Daily Challenge uses safe replacement. Moving the device clock back may make an
exact matching unsolved session visible again if it has not been replaced and its date has no
completion.

The repository does not maintain a monotonic trusted date or anti-cheat ledger.

---

## Local Storage And Transfer

The versioned aggregate is encoded in one dedicated Preferences DataStore file:

`datastore/daily_challenge.preferences_pb`

One file keeps session replacement, completion recording, and resumability removal within a
single atomic DataStore edit. Room is not required because the product queries one optional
session and a small local set of completion identities rather than an independently mutable
relational history.

The file is excluded from:

- legacy Android Auto Backup in `res/xml/backup_rules.xml`
- Android cloud backup in `res/xml/data_extraction_rules.xml`
- Android device-to-device transfer in `res/xml/data_extraction_rules.xml`

Reinstallation or application-data deletion resets both Daily progress and the completion
calendar. Cache deletion does not.

---

## Verification Boundaries

The non-device test suite must protect:

- deterministic aggregate round trips and malformed or unsupported versions
- snapshot identity, recipe, seed, candidate-index, shape, and initial/current consistency
- rejection of duplicate identity and same-date completion records
- independent normal and Daily repositories
- one-slot Daily replacement and stale-callback guards
- exact restoration without regeneration or writes
- safe prior-date replacement, failure, cancellation, and exhaustion
- ordered committed progress
- atomic solved completion and active-session removal
- idempotent repeated completion
- DataStore recreation and corruption recovery
- trusted local-date rollover and clock-back behavior

Instrumented sources should protect the application-composition, Menu, Daily continuation,
completion, calendar, and process-recreation boundaries where rendering or Android lifecycle
matters. Local delivery compiles instrumented sources without starting an emulator.
