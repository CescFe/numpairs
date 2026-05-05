# NumPairs

NumPairs is a native Android puzzle game inspired by arithmetic grid challenges.

The project is being built iteratively, starting with a small playable prototype and expanding later with stronger gameplay rules, better presentation, and more game modes.

---

## Current Status

### Version: v0 - Playable Prototype

Current milestone focus:

- Single Android screen
- 8-tile puzzle board
- 8-number strip
- Responsive layout for strip and board
- Bounded tile sizing to avoid stretched cards on wide screens
- Larger visual emphasis for tile results
- Initial puzzle domain model
- Lightweight Compose UI instrumented tests for `GameScreen`

Not included yet:

- Scoring
- Persistence
- Animations
- Difficulty levels
- Multiple puzzles
- Backend or user accounts

---

## Tech Stack

- Kotlin
- Jetpack Compose
- Gradle Kotlin DSL
- Android Studio

---

## Domain Modeling

The puzzle core uses a small domain layer guided by the project's ubiquitous language and DDD-inspired modeling.

Current core concepts:

- `Puzzle`
- `Board`
- `Tile`
- `Expression`
- `Operator`
- `Strip`

---

## Project Structure

```text
app/
├── src/main/java/org/cescfe/numpairs/
│   ├── MainActivity.kt
│   ├── domain/puzzle/
│   └── ui/
│       ├── components/
│       ├── screen/
│       └── theme/
├── src/test/java/org/cescfe/numpairs/
│   └── domain/puzzle/
└── src/androidTest/java/org/cescfe/numpairs/
    └── ui/screen/
docs/
├── product/
├── technical/adr/
├── game-rules.md
└── ubiquitous-language.md
```

---

## Documentation

- Product Requirements Document (PRD): `docs/product/prd/prd-v0.md`
- Product roadmap: `docs/product/roadmap.md`
- UX decisions: `docs/product/ux-decisions.md`
- Architectural Decision Records (ADR): `docs/technical/adr/`
- Game rules: `docs/game-rules.md`
- Ubiquitous Language: `docs/ubiquitous-language.md`

---

## Roadmap

- Current baseline: `v0 - Playable Prototype`
- Planned milestones: `v1 - Product Polish & Technical Hardening`, `v2 - Content & Session Flow`, `v3 - Game Modes, Guidance & Progression`
- Canonical roadmap: `docs/product/roadmap.md`

---

## Run Locally

1. Open the project in Android Studio
2. Sync Gradle
3. Run on emulator or Android device

## Testing

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented UI tests on a connected emulator/device
./gradlew connectedDebugAndroidTest
```

---

## Code Quality

```bash
# Check all formatting
./gradlew spotlessCheck

# Apply formatting
./gradlew spotlessApply

# Run all quality checks
./gradlew check
```

---

## License

MIT License
