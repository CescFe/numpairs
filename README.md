# NumPairs

NumPairs is a native Android arithmetic deduction puzzle.

Players complete hidden expressions on a board by using numbers from a strip. The project is built iteratively through documented product milestones.

---

## Current Status

### Current implemented product baseline: v12

- 📦 [PRD v12 App Versioning & Google Play Delivery](./docs/product/prd/prd-v12.md) defines the
  app-release identity and staged Google Play delivery contract independently from product and
  persisted-data versions.

NumPairs now provides five persistent color-only themes: Warm, Frost, Obsidian, Terminal,
and Ember. Typography, shapes, spacing, elevation, layout, controls, and gameplay meanings
remain shared across them.

v11 groups normal generated play into Quick and Classic while preserving the stable internal
3 Pairs, 4 Pairs, and 8 Pairs challenge identities. Quick exposes Low and Medium and selects
the matching 3 Pairs challenge 35% of the time and 4 Pairs challenge 65% of the time. Classic
exposes the original full-board `8 Pairs Medium` and `8 Pairs Hard` challenges. Generated feedback
continues using subtle accepted-assignment haptics, newly-correct tile motion, a brief completion
celebration, and a successor-ready replay transition.

Daily Challenge selects one deterministic `4 Pairs Low` puzzle for each device-local calendar
date. Its exact progress and local completion history use a separate versioned aggregate, so one
normal generated session and one Daily Session may remain resumable at the same time. The
state-aware Menu action, monthly completion calendar, and non-spoiling textual share result work
without accounts or a server.

### Delivered milestone: v11

- The documentation-first product and architecture contract is implemented.
- Quick and Classic are delivered through dependency-ordered atomic issues and Pull Requests.
- Daily Challenge retains its deterministic v10 recipe and independent persistence boundary.

Historical milestone snapshots:

- 🔧 [PRD v0 Playable Prototype](./docs/product/prd/prd-v0.md)
- ✨ [PRD v1 Product Polish & Technical Hardening](./docs/product/prd/prd-v1.md)
- 🧩 [PRD v2 Puzzle Generation & Replay Loop](./docs/product/prd/prd-v2.md)
- 🎓 [PRD v3 Guided Play & Rules Onboarding](./docs/product/prd/prd-v3.md)
- 🎨 [PRD v4 Visual Design System & UI Refinement](./docs/product/prd/prd-v4.md)
- 🧠 [PRD v5 Bigger Challenges with 8 Pairs](./docs/product/prd/prd-v5.md)
- 🎓 [PRD v6 Guided First Run](./docs/product/prd/prd-v6.md)
- 🔁 [PRD v7 Reliable Sessions & Replay Controls](./docs/product/prd/prd-v7.md)
- 📈 [PRD v8 Difficulty Selection & Challenge Expansion](./docs/product/prd/prd-v8.md)
- ✨ [PRD v9 Game Feel & Personalization](./docs/product/prd/prd-v9.md)
- 🗓️ [PRD v10 Quick Play & Daily Challenge](./docs/product/prd/prd-v10.md)

PRDs preserve the product requirements and planning context for each milestone. Current
generated-mode and profile behavior is also documented in
[puzzle-generation.md](./docs/product/puzzle-generation.md).

The generated catalog contains six implemented profiles, including the v11
`3 Pairs Medium` profile. Normal generated play stores one exact
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
- `feature`: Menu, Tutorial, Quick, Classic, Daily Challenge, generated modes, and reusable Game
  behavior.
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
- Generated play-option decision: [ADR-007](./docs/technical/adr/adr-007-separate-play-options-from-generated-modes.md)
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
