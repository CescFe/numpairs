# NumPairs

NumPairs is a native Android arithmetic deduction puzzle.

Players complete hidden expressions on a board by using numbers from a strip. The project is built iteratively through documented product milestones.

---

## Current Status

### Current implemented product baseline: v10

- 🗓️ [PRD v10 Quick Play & Daily Challenge](./docs/product/prd/prd-v10.md) defines the
  implemented Quick mode and deterministic local Daily Challenge.
- 📈 [PRD v8 Difficulty Selection & Challenge Expansion](./docs/product/prd/prd-v8.md) defines
  the current generated challenge catalog and mode-specific difficulty selection.
- ✨ [PRD v9 Game Feel & Personalization](./docs/product/prd/prd-v9.md) defines the current color-personalization and generated-game feedback baseline.
- 🔁 [PRD v7 Reliable Sessions & Replay Controls](./docs/product/prd/prd-v7.md) remains the foundation for the one-slot generated-session and safe-replay contract.
- 🎓 [PRD v6 Guided First Run](./docs/product/prd/prd-v6.md) defines the focused, explicitly
  skippable first-run Tutorial shown before the normal menu.

NumPairs now provides five persistent color-only themes: Warm, Frost, Obsidian, Terminal,
and Ember. Typography, shapes, spacing, elevation, layout, controls, and gameplay meanings
remain shared across them.

Generated play supports Quick as `3 Pairs Low`, plus `4 Pairs Low`, `4 Pairs Medium`,
`8 Pairs Medium`, and `8 Pairs Hard`. Players choose a supported difficulty independently for the
4 Pairs and 8 Pairs modes, while Quick launches its only challenge directly. Every generated
challenge adds subtle accepted-assignment haptics, newly-correct tile motion, a brief completion
celebration, and a successor-ready replay transition. This feedback is not added to Tutorial or
onboarding; sound and error haptics are not implemented.

Daily Challenge selects one deterministic `4 Pairs Low` puzzle for each device-local calendar
date. Its exact progress and local completion history use a separate versioned aggregate, so one
normal generated session and one Daily Session may remain resumable at the same time. The
state-aware Menu action, monthly completion calendar, and non-spoiling textual share result work
without accounts or a server.

### Active milestone: v10

- Implementation and milestone validation are complete.
- The GitHub milestone intentionally remains open for manual closure.

Historical milestone snapshots:

- 🔧 [PRD v0 Playable Prototype](./docs/product/prd/prd-v0.md)
- ✨ [PRD v1 Product Polish & Technical Hardening](./docs/product/prd/prd-v1.md)
- 🧩 [PRD v2 Puzzle Generation & Replay Loop](./docs/product/prd/prd-v2.md)
- 🎓 [PRD v3 Guided Play & Rules Onboarding](./docs/product/prd/prd-v3.md)
- 🎨 [PRD v4 Visual Design System & UI Refinement](./docs/product/prd/prd-v4.md)
- 🧠 [PRD v5 Bigger Challenges with 8 Pairs](./docs/product/prd/prd-v5.md)

PRDs preserve the product requirements and planning context for each milestone. Current
generated-mode and profile behavior is also documented in
[puzzle-generation.md](./docs/product/puzzle-generation.md).

All five supported generated profiles are implemented. Normal generated play stores one exact
resumable session shared by every generated challenge, restores committed progress after process
death, and keeps the current puzzle visible until a validated successor is stored and ready.
Daily progress and completion history remain independently persisted in their own aggregate.

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
├── technical/
│   ├── adr/
│   ├── code-quality.md
│   └── delivery-workflow.md
├── game-rules.md
└── ubiquitous-language.md
```

Core responsibilities:

- `domain`: puzzle model, rules, validation, assignments, and generated puzzle logic.
- `feature`: Menu, Tutorial, Quick, Daily Challenge, generated modes, and reusable Game behavior.
- `data`: seed puzzle, persistence-backed preferences, generated-session snapshot, and Daily
  aggregate.
- `ui`: app navigation, theme, and shared visual defaults.

---

## Documentation

- Product Requirements Documents: `docs/product/prd/`
- Visual design system: [visual-design-system.md](./docs/product/visual-design-system.md)
- Rules helper requirements: [rules-helper.md](./docs/product/rules-helper.md)
- Puzzle generation: [puzzle-generation.md](./docs/product/puzzle-generation.md)
- Current UI behavior: [ui-behavior.md](./docs/ui-behavior.md)
- Generated-session persistence: [generated-session-persistence.md](./docs/technical/generated-session-persistence.md)
- Daily Challenge persistence: [daily-challenge-persistence.md](./docs/technical/daily-challenge-persistence.md)
- Platform branding decision: [ADR-004](./docs/technical/adr/adr-004-keep-v9-platform-branding-static.md)
- Daily cadence decision: [ADR-006](./docs/technical/adr/adr-006-model-daily-challenge-as-versioned-local-cadence.md)
- UX decisions: `docs/product/ux-decisions.md`
- Architectural Decision Records: `docs/technical/adr/`
- Delivery workflow: [delivery-workflow.md](./docs/technical/delivery-workflow.md)
- Code quality guidelines: [code-quality.md](./docs/technical/code-quality.md)
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
