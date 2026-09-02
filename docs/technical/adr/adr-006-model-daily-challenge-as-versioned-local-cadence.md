# ADR-006: Model Daily Challenge As A Versioned Local Cadence

## Context

NumPairs generated play models `3 Pairs`, `4 Pairs`, and `8 Pairs` as replayable puzzle-size
families. A sparse generated-challenge catalog binds each supported mode and difficulty to one
validated profile. Normal generated play owns one application-wide resumable session slot, and
starting another normal challenge safely replaces that slot.

v10 adds one Daily Challenge for each device-local calendar date. The first daily recipe selects
`4 Pairs Low`. A later product increment adds predictable weekly variety across the existing
`3 Pairs Low`, `4 Pairs Low`, `3 Pairs Medium`, `4 Pairs Medium`, and `8 Pairs Medium`
challenges. Daily behavior also requires:

- one stable date-bound content identity
- deterministic seed selection shared by every installation on the same recipe version
- exact progress restoration after process death
- one local completion per date
- durable no-pause elapsed timing from first presentation to solution
- a durable count of effective puzzle movements
- a durable count of player corrections, with unknown legacy values
- one history-derived personal best for each comparable generated challenge
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

NumPairs will model Daily Challenge as a versioned local cadence over explicitly configured
generated challenges.

### Identity And Recipe

- `DailyChallengeId` combines one device-local calendar date with one stable
  `DailyRecipeVersion`.
- The canonical persisted date uses ISO-8601 `YYYY-MM-DD`.
- `DailyRecipeVersion` identifies one immutable challenge-selection and seed-schedule contract.
- The legacy v10 recipe version is `daily-4-pairs-low-v1` and resolves every date to the existing
  `4 Pairs Low` generated challenge.
- The active recipe version is `daily-weekly-schedule-v2`. It derives one exact generated
  challenge from the captured date's `DayOfWeek`:
  - Monday: `3 Pairs Low`
  - Tuesday and Sunday: `4 Pairs Low`
  - Wednesday and Saturday: `3 Pairs Medium`
  - Thursday: `4 Pairs Medium`
  - Friday: `8 Pairs Medium`
- Display copy, localized date text, profile parameters, and current clock state are not part of
  Daily Challenge identity.

Each recipe constructs one ASCII payload for each candidate. The two implemented payloads are:

`daily-4-pairs-low-v1|YYYY-MM-DD|candidate-index`

`daily-weekly-schedule-v2|YYYY-MM-DD|candidate-index`

The recipe hashes the UTF-8 bytes with 32-bit FNV-1a, using offset basis `0x811C9DC5`, prime
`0x01000193`, and defined 32-bit overflow. The resulting signed 32-bit bit pattern is the
generation seed. Candidate indexes `0..3` are attempted in ascending order.

The payload format, hash algorithm, constants, day-to-challenge mapping, and four-candidate limit
cannot change within a recipe version. Challenge selection reads only the one captured local date;
it does not use Quick weighting, remembered difficulty, runtime randomness, or remote
configuration.

Both immutable recipe bindings, the configured-version resolver, one-time device-local date
capture, and the four-candidate seed schedule are implemented.

The generator remains unaware of dates and Daily semantics. It receives ordinary explicit
generated-puzzle requests. Daily coordination never falls back to device randomness, current
time-of-day entropy, or a different profile.

Ordered candidate execution is implemented over the existing configured generation use case.
Non-cancellation failures remain ordered and typed, exhaustion is explicit, and cancellation is
terminal.

### Local-Date Boundary

Application composition supplies the current device-local date through an explicit clock
boundary. Domain identities and persistence do not read the Android clock directly.

The date is captured when a Daily entry request begins. A local-date change does not mutate the
identity or scheduled challenge already resolved for that request. Menu resolution compares the
current local date with persisted Daily identity and does not expose an unfinished prior-date
session for backfill. A valid same-date session using the legacy recipe remains resumable after the
weekly recipe becomes active, so an upgrade never discards current-day progress.

The clock and time zone are trusted local inputs. Manual changes may make a matching stored
session or completion visible again; v10 does not add anti-cheat state.

The injectable device-local date source and current-Daily resolver are implemented. The source
reads the current instant in the current default device time zone, and each resolution captures
one `LocalDate` before constructing immutable Daily identity.

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
completion record, including its captured elapsed time when timing started, with no resumable Daily
Session. The same edit transfers the final movement count when the session owns one. A second
completion for the same local calendar date is rejected even if a different recipe version is
later resolved for that date. The edit also transfers the final correction count when the session
owns one.

### Daily Session Snapshot

The snapshot stores:

- a stable Daily Session id used for stale-callback protection
- canonical Daily Challenge identity
- the successful candidate index and derived seed
- exact initial `Puzzle`
- exact current `Puzzle`
- an optional timing start instant stored as Unix epoch milliseconds
- an optional authoritative movement count
- an optional authoritative correction count

The recipe version and canonical local date resolve the exact generated challenge. Mode,
difficulty, profile parameters, and display strings are therefore not persisted redundantly in the
Daily snapshot.

Restoration resolves the stored recipe version, verifies its challenge against the puzzle shape
and snapshot metadata, and reads the exact current puzzle. It never regenerates historical
progress from the seed.

New snapshots start with a movement count of zero. Snapshots migrated from an aggregate version
that predates movement tracking retain an absent count for the rest of the session because earlier
corrections, clears, and resets cannot be reconstructed from the Current Puzzle.

New snapshots also start with a correction count of zero. Snapshots migrated from an aggregate
version that predates correction tracking retain an absent count for the rest of the session;
Current Puzzle state cannot reveal how many earlier rectifications led to it.

### Elapsed Timing

Daily elapsed time uses non-negative millisecond precision. An active session starts without a
timing anchor because generation and persistence occur before the puzzle is presented. An
identity-guarded repository transition records one UTC timing start instant when the playable
puzzle is first presented. A repeated start request returns the original instant and cannot reset
the timer.

Timing does not pause for navigation, backgrounding, device locking, configuration change, or
process recreation. The first transition to a solved current puzzle captures the elapsed duration
before completion persistence and presentation work. Completion requires an elapsed duration if
the active session has a timing start and rejects a duration if the active session has no timing
start. Retrying completion reuses the originally captured duration.

The timing start uses the trusted device clock so an unfinished timer can be reconstructed after
process death or device restart. Runtime presentation may use a monotonic clock between persisted
anchors. Manual device-clock changes can affect reconstructed elapsed time; the local-only product
does not claim anti-cheat guarantees.

### Movement Count

Daily movement tracking uses one non-negative 64-bit count. An effective movement is one
player-driven durable Current Puzzle mutation, including a committed or cleared strip value,
operand assignment, operator assignment, or non-pristine tile reset. Invalid, transient, no-op,
navigation, and lifecycle interactions do not count.

The repository persists the Current Puzzle and exact movement count in the same identity-guarded
progress edit. A retry may repeat the current count, and a later update may jump forward when an
earlier write failed, but the count cannot regress or change between known and unknown. Safe
increment rejects overflow rather than constructing an invalid value.

The completion transition validates the same consistency rule and stores the supplied final count
atomically with session removal. A migrated active session with no count remains unknown through
progress and completion; tracking is never started partway through it.

### Correction Count

Puzzle correction tracking uses a separate non-negative 64-bit count. The game mutation boundary
classifies the intent of each effective committed action instead of inferring it from display
position or raw numeric equality. Changing or clearing a player-entered strip value, reassigning an
already assigned operand, changing an assigned operator, and resetting a non-pristine tile each
count once. First assignments, invalid or unchanged actions, transient selectors, navigation, and
lifecycle events do not count. Cascading effects remain part of the originating single correction.

The repository persists Current Puzzle, movement count, and correction count atomically under the
same stable session identity. Equal retry and forward-progress rules apply independently to both
counts; neither may regress, overflow, or cross between known and unknown. Completion freezes and
transfers the final correction count through persistence failure and retry. A migrated session with
an unknown count remains unknown for its remaining lifecycle.

### Completion History

A Daily Completion record stores:

- canonical Daily Challenge identity
- authoritative elapsed time in milliseconds for a newly timed completion
- authoritative movement count for a newly tracked completion
- authoritative correction count for a newly tracked completion

The identity supplies the completed local date and recipe version. A completion migrated from the
version-1 aggregate explicitly has no elapsed time, movement count, or correction count. A
completion migrated from version 2 preserves its elapsed time and has neither count. A completion
migrated from version 3 additionally preserves movement count while correction count remains
unknown. Presentation or sharing must not fabricate an absent value. Completion does not store an
exact completion instant, score, streak, reward, display label, or full puzzle.

Completion is recorded only by an identity-guarded atomic repository transition that receives a
solved current puzzle consistent with the active snapshot. Repeating the transition cannot create
another record.

Calendar presentation may preserve and display the completed date of a record whose recipe version
is no longer active. An active snapshot whose recipe version cannot be resolved is not resumable.

### Personal-Best Derivation

Daily personal bests are derived from authoritative completion history instead of persisted in a
second mutable cache. One personal-best category is the stable identity of the exact generated
challenge resolved by the completion's recipe and canonical date. The supported recipe history
currently resolves five independent categories:

- `3 Pairs Low`
- `4 Pairs Low`
- `3 Pairs Medium`
- `4 Pairs Medium`
- `8 Pairs Medium`

The lowest non-null millisecond-precision elapsed time in each category is its best. Existing timed
`daily-4-pairs-low-v1` completions resolve naturally to the same `4 Pairs Low` category as matching
weekly-recipe completions. A completion with no elapsed time or whose recipe cannot safely resolve
an exact generated challenge cannot establish or improve a best. Such unsupported completion
records remain available to calendar history.

For one completion, its previous best is the minimum qualifying duration in the same category on
an earlier canonical Daily date. The date establishes which history existed before that result; it
does not participate in the duration comparison. A first timed category result is a baseline, not
a personal record. A later result is a personal record only when its duration is strictly lower
than the previous best. Equal and slower durations are not records. Movement count, streak, and
other generated challenges do not affect the comparison.

The first unsolved-to-solved in-memory transition freezes one result containing the current
duration, previous best, resulting best, and baseline, personal-record, or non-record outcome before
completion persistence begins. Persistence failure and retry reuse that result. After successful
persistence or process recreation, the same result is reconstructed from completion history
without reading the clock. The aggregate schema remains unchanged because neither a cached best nor
the derived outcome is persisted.

### Aggregate Schema Migration

Daily correction tracking changes the aggregate schema from version 3 to version 4. The version-4
codec reads all earlier binary layouts explicitly. Version 1 preserves exact progress and every
completion identity while mapping timing, movement, and correction data to unknown. Version 2
additionally preserves the exact timing start and completion elapsed time while movement and
correction data remain unknown. Version 3 additionally preserves movement data while mapping
correction data to unknown. Unsupported future versions and invalid payloads retain the existing
safe-recovery behavior; versions 1, 2, and 3 are not treated as unsupported or empty.

### Replacement And Rollover

An unfinished prior-date Daily Session remains stored but is not exposed as current after Menu
observes a later local date.

Starting the later Daily Challenge:

1. captures the new identity
2. resolves its immutable recipe
3. selects the exact challenge scheduled for the captured day of week
4. attempts deterministic candidate seeds in order
5. validates the generated puzzle
6. builds an exact new snapshot
7. atomically replaces the stale Daily Session only after the successor is stored

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
- A timing start cannot be reset, and a completion preserves the exact captured millisecond
  duration across recreation and later sharing.
- Movement progress and completion preserve one authoritative non-negative count without
  fabricating historical values.
- Correction progress and completion preserve a separate authoritative non-negative count without
  fabricating zero for historical sessions.
- Personal bests remain derivable from authoritative completion history without a second cache,
  and each exact generated challenge remains independently comparable.
- A recipe version gives date-derived content a stable compatibility boundary.
- Weekly variety reuses the sparse generated-challenge catalog without persisting derivable mode,
  difficulty, or profile metadata.
- The generator remains reusable and unaware of clocks or presentation.
- Exact snapshots protect progress from future generator changes.
- Completion history remains small, local, and independent from puzzle content.

### Negative

- Application composition owns a second resumable-session path.
- A versioned aggregate and codec must preserve both optional session state and growing completion
  history.
- Old recipe resolvers must remain available while their active snapshots may still be restored.
- Trusted local-clock changes can alter reconstructed elapsed time and cannot establish competitive
  integrity.
- Historical outcome reconstruction uses canonical Daily date order because completion records do
  not store a separate completion instant.
- Date rollover, clock changes, stale callbacks, recipe mismatches, and partial corruption require
  explicit test coverage.
- Local-only history can be reset or manipulated and cannot prove fair participation.

## Future Considerations

A later online product could publish curated daily content or synchronize completion history, but
that would require a new trust, migration, and account contract. It must not silently reinterpret
existing local Daily identities.

A future recipe may select another existing challenge or use a new deterministic algorithm. It
must use a new stable recipe version, preserve resolvers needed by stored sessions and
completions, and define how a same-date completion from an older recipe suppresses duplicate Daily
completion.
