# ADR-001: Choose Native Android Application

## Context
We want to build a mobile application for the NumPairs puzzle game.  
At this stage, the goal is to deliver a simple MVP as fast as possible, focusing on UI rendering and core gameplay.

We considered different approaches:
- Native Android (Kotlin)
- Cross-platform (Flutter)
- Web application (responsive or PWA)

## Decision
We will build a native Android application using Kotlin.

## Rationale
- Simplicity for MVP: avoids additional abstraction layers and tooling
- Faster iteration: direct access to Android APIs and tooling (Android Studio, previews, emulator)
- Strong ecosystem: Jetpack libraries, Compose, and official tooling
- Learning value: deepens Android-native expertise
- Distribution: mobile apps are the natural medium for casual puzzle games
- No immediate need for cross-platform support

## Consequences

### Positive
- Faster initial development
- Better integration with Android platform
- Simpler architecture for MVP
- Easier debugging and tooling support

### Negative
- No iOS support (would require separate implementation or migration later)
- Potential future duplication if cross-platform is needed
- Platform lock-in at early stages

## Future Considerations
If the product grows and requires multi-platform support (iOS/web), we may:
- Re-evaluate Flutter or Kotlin Multiplatform
- Introduce a shared domain layer