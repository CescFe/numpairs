# Architecture Decision Records

This directory records durable architectural decisions for NumPairs. Use the index below to find
the decision relevant to a change, then read the linked record for its context, alternatives,
decision, and consequences.

| ADR                                                                    | Decision                                                                                                     |
|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| [ADR-001](adr-001-choose-native-android-app.md)                        | Build NumPairs as a native Android application with Kotlin.                                                  |
| [ADR-002](adr-002-use-jetpack-compose.md)                              | Use Jetpack Compose for the UI layer.                                                                        |
| [ADR-003](adr-003-use-stable-strip-entry-identity.md)                  | Model operand selection with stable strip-entry identity rather than numeric values or visual positions.     |
| [ADR-004](adr-004-keep-v9-platform-branding-static.md)                 | Keep system branding static while the selected theme controls in-app colors.                                 |
| [ADR-005](adr-005-model-sparse-generated-challenges.md)                | Model the supported generated challenge catalog as a sparse combination of puzzle size and difficulty.       |
| [ADR-006](adr-006-model-daily-challenge-as-versioned-local-cadence.md) | Model Daily Challenge as a deterministic, versioned, device-local cadence with its own persistence boundary. |
| [ADR-007](adr-007-separate-play-options-from-generated-modes.md)       | Keep player-facing play options separate from stable generated mode identities.                              |

When a cross-cutting change introduces or revises a durable architectural decision, add or update
an ADR and keep this index current.
