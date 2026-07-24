# ADR-006: Model Daily Challenge As A Versioned Local Cadence

## Context

NumPairs generated play models `3 Pairs`, `4 Pairs`, and `8 Pairs` as replayable puzzle-size
families. A sparse generated-challenge catalog binds each supported mode and difficulty to one
validated profile. Normal generated play owns one application-wide resumable session slot, and
starting another normal challenge safely replaces that slot.

v10 adds one Daily Challenge for each device-local calendar date. The first daily recipe selects
`4 Pairs Low`, but Daily behavior also requires:

- one stable date-bound content identity
- deterministic seed selection shared by every installation on the same recipe version
- exact progress restoration after process death
- one local completion per date
- a completion calendar
- coexistence with an unfinished normal generated session
- explicit behavior when the local date, recipe, or generator changes

Daily cadence, completion history, and local-date rollover do not belong to a generated puzzle
profile. Reusing the normal generated-session slot would also let Daily and normal play replace
one another.

## Options Considered

### Add Daily As A Generated Mode

Create a `daily` generated-mode identity and bind it to `4 Pairs Low`.

This would make cadence look like puzzle size. The new mode would duplicate the existing `4 Pairs`
shape, make difficulty ownership ambiguous, and force generation and session code to recover a
date and recipe from a mode that does not own either concept.

### Reuse The Normal Generated-Session Slot

Represent the daily puzzle as an ordinary `4 Pairs Low` generated session with a date-derived seed
and store completion history separately.

This reuses existing persistence directly, but starting Daily could replace an unfinished Quick,
4 Pairs, or 8 Pairs puzzle. Starting normal play could likewise erase Daily progress. Completion
would also require coordinating a session clear and a separate history write without one atomic
boundary.

### Add A Separate Versioned Daily Aggregate

Model Daily Challenge as a cadence that selects an existing generated challenge through an
immutable versioned recipe. Persist one independent aggregate containing an optional exact Daily
Session snapshot and local completion records.

The recipe owns deterministic candidate seed order. The aggregate owns atomic session replacement,
progress updates, completion recording, and session removal. Normal generated play remains
unchanged.

## Decision

NumPairs will model Daily Challenge as a versioned local cadence over an explicitly configured
generated challenge.

### Identity And Recipe

- `DailyChallengeId` combines one device-local calendar date with one stable
  `DailyRecipeVersion`.
- The canonical persisted date uses ISO-8601 `YYYY-MM-DD`.
- `DailyRecipeVersion` identifies one immutable challenge-selection and seed-schedule contract.
- The v10 recipe version is `daily-4-pairs-low-v1` and resolves to the existing `4 Pairs Low`
  generated challenge.
- Display copy, localized date text, profile parameters, and current clock state are not part of
  Daily Challenge identity.

The v10 recipe constructs one ASCII payload for each candidate:

`daily-4-pairs-low-v1|YYYY-MM-DD|candidate-index`

It hashes the UTF-8 bytes with 32-bit FNV-1a, using offset basis `0x811C9DC5`, prime `0x01000193`,
and defined 32-bit overflow. The resulting signed 32-bit bit pattern is the generation seed.
Candidate indexes `0..3` are attempted in ascending order.

The payload format, hash algorithm, constants, selected challenge, and four-candidate limit cannot
change without a new recipe version.

The generator remains unaware of dates and Daily semantics. It receives ordinary explicit
generated-puzzle requests. Daily coordination never falls back to device randomness, current
time-of-day entropy, or a different profile.

### Local-Date Boundary

Application composition supplies the current device-local date through an explicit clock
boundary. Domain identities and persistence do not read the Android clock directly.

The date is captured when a Daily entry request begins. A local-date change does not mutate an
already visible Daily Session. Menu resolution compares the current local date with persisted
Daily identity and does not expose an unfinished prior-date session for backfill.

The clock and time zone are trusted local inputs. Manual changes may make a matching stored
session or completion visible again; v10 does not add anti-cheat state.

### Daily Aggregate

NumPairs owns one application-private Daily aggregate containing:

- at most one exact versioned Daily Session snapshot
- zero or more local Daily Completion records

The Daily aggregate is separate from:

- the normal generated-session repository
- remembered difficulty preferences
- onboarding
- personalization and settings

One aggregate and one DataStore edit own the transition from an active solved puzzle to a
completion record with no resumable Daily Session. A second completion for the same local calendar
date is rejected even if a different recipe version is later resolved for that date.

### Daily Session Snapshot

The snapshot stores:

- a stable Daily Session id used for stale-callback protection
- canonical Daily Challenge identity
- the successful candidate index and derived seed
- exact initial `Puzzle`
- exact current `Puzzle`

The recipe version resolves the exact generated challenge. Mode, difficulty, profile parameters,
and display strings are therefore not persisted redundantly in the Daily snapshot.

Restoration resolves the stored recipe version, verifies its challenge against the puzzle shape
and snapshot metadata, and reads the exact current puzzle. It never regenerates historical
progress from the seed.

### Completion History

A Daily Completion record stores:

- canonical Daily Challenge identity

The identity supplies the completed local date and recipe version. Completion does not store a
score, elapsed time, action count, streak, reward, display label, or full puzzle.

Completion is recorded only by an identity-guarded atomic repository transition that receives a
solved current puzzle consistent with the active snapshot. Repeating the transition cannot create
another record.

Calendar presentation may preserve and display the completed date of a record whose recipe version
is no longer active. An active snapshot whose recipe version cannot be resolved is not resumable.

### Replacement And Rollover

An unfinished prior-date Daily Session remains stored but is not exposed as current after Menu
observes a later local date.

Starting the later Daily Challenge:

1. captures the new identity
2. resolves its immutable recipe
3. attempts deterministic candidate seeds in order
4. validates the generated puzzle
5. builds an exact new snapshot
6. atomically replaces the stale Daily Session only after the successor is stored

Failure or cancellation keeps the prior aggregate intact. The stale session remains hidden and
cannot be used for past-day play. A successful successor replaces only the Daily slot and does not
touch normal generated play.

### Local Storage And Transfer

The Daily aggregate uses a dedicated application-private DataStore file and is excluded from
Android cloud backup, legacy Auto Backup, and device-to-device transfer.

Reinstallation or application-data deletion removes the active Daily Session and completion
history. Account sync and cross-device transfer remain unsupported.

## Consequences

### Positive

- Daily cadence does not distort generated mode, difficulty, or profile ownership.
- Normal and Daily progress can be resumable simultaneously.
- Solved state, completion recording, and removal from Daily Resume are atomic.
- A recipe version gives date-derived content a stable compatibility boundary.
- The generator remains reusable and unaware of clocks or presentation.
- Exact snapshots protect progress from future generator changes.
- Completion history remains small, local, and independent from puzzle content.

### Negative

- Application composition owns a second resumable-session path.
- A versioned aggregate and codec must preserve both optional session state and growing completion
  history.
- Old recipe resolvers must remain available while their active snapshots may still be restored.
- Date rollover, clock changes, stale callbacks, recipe mismatches, and partial corruption require
  explicit test coverage.
- Local-only history can be reset or manipulated and cannot prove fair participation.

## Future Considerations

A later online product could publish curated daily content or synchronize completion history, but
that would require a new trust, migration, and account contract. It must not silently reinterpret
the local v10 identity.

A future recipe may select another existing challenge or use a new deterministic algorithm. It
must use a new stable recipe version and define how a same-date completion from an older recipe
suppresses duplicate Daily completion.
