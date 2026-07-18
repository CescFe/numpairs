# ADR-005: Model A Sparse Catalog Of Generated Challenges

## Context

Generated NumPairs play currently exposes two stable modes:

- `4 Pairs`, which resolves to `4 Pairs Low`
- `8 Pairs`, which resolves to `8 Pairs Medium`

The application models each generated mode as one stable mode id plus exactly one generated
puzzle profile. Navigation, generation composition, retained presentation state, and generated
session restoration consequently assume a one-to-one mode/profile relationship.

v8 keeps generated mode as the puzzle-size family and introduces Low, Medium, and Hard as an
independent difficulty dimension. The supported content matrix is intentionally incomplete:

| Generated mode | Low | Medium | Hard |
| --- | --- | --- | --- |
| `4 Pairs` | Supported | Supported | Unsupported |
| `8 Pairs` | Unsupported | Supported | Supported |

The architecture needs to represent those four supported combinations without making Android
presentation state part of the puzzle profile, duplicating a mode for every difficulty, or
manufacturing unsupported combinations.

## Options Considered

### Keep One Profile Per Generated Mode

Add a new generated mode id for each difficulty, such as `four-pairs-medium`, while keeping the
existing configuration shape.

This preserves the current registry but changes the meaning of generated mode from the stable
size family to one complete challenge. Menu selection, mode-specific preferences, titles, and
session presentation would have to recover the shared `4 Pairs` or `8 Pairs` family by parsing or
duplicating metadata. It also keeps size and difficulty coupled under a different name.

### Generate The Full Size/Difficulty Product

Model puzzle size and difficulty independently and construct every combination automatically.

This expresses the dimensions directly but implies support for `4 Pairs Hard` and `8 Pairs Low`,
which v8 does not define. Filtering those combinations later would create a second source of truth
for supported content and allow invalid profiles to reach application composition.

### Configure A Sparse Generated Challenge Catalog

Keep generated mode as the stable size family, define difficulty tier independently, and register
only supported generated challenges. Each challenge binds exactly one mode, difficulty tier, and
validated generated puzzle profile.

The mode owns stable family identity and puzzle size. The profile owns calibrated generation and
assessment policy. The challenge is the application configuration that establishes their
supported relationship.

## Decision

NumPairs will use a validated sparse catalog of generated challenges.

- `GeneratedMode` identifies the replayable puzzle-size family.
- `GeneratedPuzzleSize` owns pair, strip-entry, and board-tile counts.
- `DifficultyTier` classifies intended deductive challenge as Low, Medium, or Hard.
- `GeneratedPuzzleProfile` owns the calibrated mandatory constraints, soft variety policy, and
  difficulty-assessment policy for one supported challenge.
- `GeneratedChallenge` binds one mode, one difficulty tier, and one validated profile.
- A generated mode exposes one or more explicitly configured challenges.
- Unsupported mode/difficulty combinations do not exist in the catalog.

Catalog validation must reject:

- duplicate mode identities
- duplicate challenge or profile identities
- more than one challenge with the same difficulty inside one mode
- a profile whose puzzle size differs from its mode
- a profile whose declared difficulty differs from its challenge

Android strings, selection state, persisted player preference, navigation destinations, and
challenge-specific learning capabilities remain outside the domain profile.

Generated session snapshots continue storing stable mode and profile ids. Their combination
resolves the exact configured challenge, so difficulty does not become a redundant persisted
field. Existing `4 Pairs Low` and `8 Pairs Medium` snapshots remain representable without a schema
change.

## Consequences

### Positive

- Puzzle size and difficulty have explicit, independent meanings.
- The application can add `4 Pairs Medium` and `8 Pairs Hard` without duplicating modes.
- Unsupported combinations are unrepresentable through normal catalog resolution.
- Mode-specific selection preferences can use stable mode and difficulty identities.
- Generation, ViewModel ownership, resume, replacement, and replay can resolve one exact challenge.
- Profiles remain independent from Android and player-specific state.
- A future custom mode can use a separate product concept without adding `CUSTOM` to the closed
  difficulty tiers.

### Negative

- Application composition becomes more explicit than the current one-profile mode registry.
- Callers that previously accepted only a generated mode must distinguish family selection from a
  playable challenge.
- Registry and restoration tests must cover mismatched and unsupported mode/profile combinations.
- Presentation keys must include challenge identity so two difficulties in one mode cannot share
  retained gameplay state accidentally.

## Future Considerations

If a future milestone supports every difficulty for every size, it may generate catalog entries
from validated definitions, but the resulting supported challenges should remain explicit.

Custom generation settings should be modeled as their own configuration boundary rather than as a
fourth difficulty tier. Any future snapshot change should preserve one authoritative challenge
identity and avoid persisting derivable size or difficulty values redundantly.
