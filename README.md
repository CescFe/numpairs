# NumPairs

NumPairs is a native Android arithmetic deduction puzzle.

Players complete hidden expressions on a board by using numbers from a strip. The project is built iteratively through documented product milestones.

---

## Current Status

### Implemented baseline: v5 - Shared Generated Modes

Historical snapshots:
- 🔧 [PRD v0 Playable Prototype](./docs/product/prd/prd-v0.md)
- ✨ [PRD v1 Product Polish & Technical Hardening](./docs/product/prd/prd-v1.md)
- 🧩 [PRD v2 Puzzle Generation & Replay Loop](./docs/product/prd/prd-v2.md)
- 🎓 [PRD v3 Guided Play & Rules Onboarding](./docs/product/prd/prd-v3.md)
- 🎨 [PRD v4 Visual Design System & UI Refinement](./docs/product/prd/prd-v4.md)
- 🧠 [PRD v5 Bigger Challenges with 8 Pairs](./docs/product/prd/prd-v5.md)

The PRDs are historical planning snapshots. Current generated-mode and profile behavior is documented in [puzzle-generation.md](./docs/product/puzzle-generation.md).

Both built-in generated profiles are implemented: `4 Pairs Low` and `8 Pairs Medium`.

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
│   ├── domain/
│   ├── feature/
│   ├── data/
│   └── ui/
docs/
├── product/
├── technical/adr/
├── game-rules.md
└── ubiquitous-language.md
```

Core responsibilities:

- `domain`: puzzle model, rules, validation, assignments, and generated puzzle logic.
- `feature`: Menu, Tutorial, generated modes, and reusable Game screen behavior.
- `data`: seed puzzle and persistence-backed preferences.
- `ui`: app navigation, theme, and shared visual defaults.

---

## Documentation

- Canonical PRD: [prd-v5.md](./docs/product/prd/prd-v5.md)
- Product Requirements Documents: `docs/product/prd/`
- Visual design system: [visual-design-system.md](./docs/product/visual-design-system.md)
- Rules helper requirements: [rules-helper.md](./docs/product/rules-helper.md)
- Puzzle generation: [puzzle-generation.md](./docs/product/puzzle-generation.md)
- UX decisions: `docs/product/ux-decisions.md`
- Future in-game calculator proposal: [in-game-calculator.md](docs/backlog/in-game-calculator.md)
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
