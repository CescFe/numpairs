# NumPairs

NumPairs is a native Android puzzle game inspired by arithmetic grid challenges.

The project is being built iteratively, starting with a small playable prototype and expanding later with stronger gameplay rules, better presentation, and more game modes.

---

## Current Status

### Implemented baseline: v2 - Puzzle Generation & Replay Loop

Historical snapshots: [prd-v0.md](./docs/product/prd/prd-v0.md), [prd-v1.md](./docs/product/prd/prd-v1.md), [prd-v2.md](./docs/product/prd/prd-v2.md)

### Active product milestone: v3 - Guided Play & Rules Onboarding

Canonical product reference: [prd-v3.md](./docs/product/prd/prd-v3.md)

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
│   ├── domain/fourpairs/
│   ├── domain/puzzle/
│   ├── feature/fourpairs/
│   ├── feature/game/
│   │   ├── GameRoute.kt
│   │   ├── presentation/
│   │   └── ui/
│   │       └── components/
│   ├── feature/menu/
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

- `data/puzzle/seed`: handcrafted puzzle data currently used by Tutorial and ready to be replaced by authored tutorial content.
- `domain/puzzle`: puzzle rules, validation, assignments, and core domain types.
- `domain/fourpairs`: generated `4 Pairs` rules, generation, and validation-facing domain models.
- `feature/fourpairs`: generated `4 Pairs` route and puzzle provider wiring.
- `feature/game`: reusable Game feature entry point plus its `presentation` and `ui` subpackages.
- `feature/game/ui/components`: Game-specific Compose building blocks such as tiles and chips.
- `feature/menu`: menu entry point for selecting Tutorial or generated `4 Pairs`.
- `ui/navigation` and `ui/theme`: app-level navigation wiring and shared theming.

---

## Documentation

- Canonical PRD: [prd-v3.md](./docs/product/prd/prd-v3.md)
- Product Requirements Documents: `docs/product/prd/`
- Product roadmap: [roadmap.md](./docs/product/roadmap.md)
- Rules helper requirements: [rules-helper.md](./docs/product/rules-helper.md)
- Puzzle generation: [puzzle-generation.md](./docs/product/puzzle-generation.md)
- UX decisions: `docs/product/ux-decisions.md`
- Future in-game calculator proposal: [in-game-calculator.md](./docs/product/in-game-calculator.md)
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
