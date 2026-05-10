# NumPairs

NumPairs is a native Android puzzle game inspired by arithmetic grid challenges.

The project is being built iteratively, starting with a small playable prototype and expanding later with stronger gameplay rules, better presentation, and more game modes.

---

## Current Status

### Version: v0 - Playable Prototype

Completed: [prd-v0.md](./docs/product/prd/prd-v0.md)

### Active milestone: v1 - Product Polish & Technical Hardening

Current implementation baseline: [prd-v1.md](./docs/product/prd/prd-v1.md)

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
│   ├── data/puzzle/seed/
│   ├── domain/puzzle/
│   ├── feature/game/
│   │   ├── GameRoute.kt
│   │   ├── presentation/
│   │   └── ui/
│   │       └── components/
│   └── ui/
│       ├── navigation/
│       └── theme/
docs/
├── product/
├── technical/adr/
├── game-rules.md
└── ubiquitous-language.md
```

Current responsibilities are split as follows:

- `data/puzzle/seed`: bootstrap puzzle data used by the current prototype.
- `domain/puzzle`: puzzle rules, validation, assignments, and core domain types.
- `feature/game`: the Game feature entry point plus its `presentation` and `ui` subpackages.
- `feature/game/ui/components`: Game-specific Compose building blocks such as tiles and chips.
- `ui/navigation` and `ui/theme`: app-level navigation wiring and shared theming.

---

## Documentation

- Product Requirements Documents: `docs/product/prd/`
- Product roadmap: [roadmap.md](./docs/product/roadmap.md)
- UX decisions: `docs/product/ux-decisions.md`
- Architectural Decision Records: `docs/technical/adr/`
- Game rules: `docs/game-rules.md`
- Ubiquitous Language: `docs/ubiquitous-language.md`

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
