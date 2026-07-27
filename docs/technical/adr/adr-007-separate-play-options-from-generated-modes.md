# ADR-007: Separate Player-Facing Play Options From Generated Modes

## Context

ADR-005 defines `GeneratedMode` as one stable puzzle-size family. The implemented catalog contains
3 Pairs, 4 Pairs, and 8 Pairs modes, and exact normal sessions persist mode and profile identities.

v11 simplifies the normal Menu to:

- Quick, which selects a 3 Pairs or 4 Pairs challenge at the requested difficulty
- Classic, which selects an 8 Pairs challenge

Making one Generated Mode span 3 Pairs and 4 Pairs would contradict the size invariant, invalidate
catalog assumptions, and either rename or reinterpret persisted identities.

## Options Considered

### Merge 3 Pairs And 4 Pairs Into One Generated Mode

This would make the mode size ambiguous, require catalog and snapshot migration, and weaken
profile-size validation.

### Rename Persisted Modes To Quick And Classic

This matches display names but makes Quick unable to own one size and breaks valid stored
`three-pairs`, `four-pairs`, and `eight-pairs` identities without adding gameplay value.

### Add Player-Facing Generated Play Options

Keep exact generated modes and challenges unchanged. Add an application-level selection concept
that groups challenges for discovery, difficulty preference, weighted selection, and
presentation.

## Decision

NumPairs will model Quick and Classic as stable Generated Play Options outside the domain profile
and generated-session persistence boundaries.

- `GeneratedMode` continues identifying one puzzle-size family.
- `GeneratedChallenge` continues binding one mode, difficulty, and validated profile.
- `GeneratedPlayOption` identifies one player-facing normal-play choice.
- A play option exposes explicit difficulty policies that resolve one exact configured challenge.
- Quick owns weighted selection between matching 3 Pairs and 4 Pairs challenges.
- Classic owns direct selection of matching 8 Pairs challenges.
- Remembered difficulty belongs to the play option.
- A successful normal session stores only its exact mode/profile pair; play-option identity is
  derivable and is not persisted redundantly.
- Daily recipes continue resolving exact generated challenges and do not use play-option policies.

The Quick selector uses 100 equally likely buckets:

- `0..34` resolves the matching 3 Pairs challenge
- `35..99` resolves the matching 4 Pairs challenge

Selection occurs after a new-puzzle request is confirmed. Resume and retry do not select again.
Quick `Play another` creates a new selection request; Classic replay resolves the same exact
challenge for its completed difficulty.

## Consequences

### Positive

- Player-facing organization can evolve without changing puzzle-size identity.
- Existing schema-1 snapshots remain compatible.
- Weighted selection remains outside generation profiles and the generator.
- Difficulty preference ownership matches the visible Menu choice.
- Daily determinism remains isolated from normal-play randomness.
- Selection can be tested exhaustively with controlled buckets.

### Negative

- Application composition must distinguish a requested play option from a resolved challenge.
- Presentation needs an explicit challenge-to-option mapping.
- Completion replay may delegate to navigation when the next Quick challenge can differ in size.
- Legacy mode-keyed preferences require compatibility migration.

## Rejected Follow-Ups

The decision does not introduce adaptive weighting, remote configuration, a generic rule engine,
or a new persisted session field. Those concerns are not required by the v11 product contract.
