# NumPairs Product Roadmap (v1 -> v3)

## Purpose

This roadmap defines the next product iterations after the current `v0 - Playable Prototype`.

It is intentionally high-level, outcome-focused, and easy to update as implementation, feedback, and product priorities evolve.

---

## Current Baseline

### v0 - Playable Prototype

- Single-screen playable puzzle
- Local tile validation and completion flow
- Initial domain model and Compose UI foundation
- No persistence, scoring, multiple puzzles, or game modes yet

---

## Roadmap Principles

- Prioritize a polished and stable core before adding retention systems
- Group work by product outcomes rather than detailed task lists
- Revisit milestone scope at the end of each iteration

---

## Upcoming Milestones

### v1 - Product Polish & Technical Hardening

**Goal**

Turn the prototype into a stable, clear, and maintainable foundation ready for feature expansion.

**High-level scope**

- Improve feedback for incorrect tile and puzzle states
- Add reliable reset tile interaction
- Design the first-pass NumPairs logo and launcher icon
- Add a branded splash screen
- Refine UI clarity, interaction flows, and accessibility semantics
- Introduce AppNavigation to orchestrate app startup and future navigation flow
- Refactor code for readability and maintainability
- Clarify MVVM architecture boundaries
- Improve test signal by increasing useful coverage and removing low-value tests
- Update PRD and supporting product documentation to match the implemented product

**Out of scope**

- Scoring system and timer
- Persistence or save state
- Difficulty modes
- Multiple puzzles
- Broader multiscreen content flows beyond splash-to-game startup

### v2 - Content & Session Flow

**Goal**

Expand NumPairs from a single isolated puzzle into a reusable play loop with navigable content and session continuity.

**High-level scope**

- Add navigation between the main product screens
- Support multiple puzzles and puzzle selection or browsing
- Introduce puzzle generation or another scalable content pipeline
- Persist in-progress and completed puzzles
- Establish an initial difficulty classification model

**Roadmap note**

If puzzle generation becomes too large for the milestone, it can be split without changing the main goal of navigation, content continuity, and replayable sessions.

### v3 - Game Modes, Guidance & Progression

**Goal**

Increase depth, onboarding, and long-term engagement once the core content loop is stable.

**High-level scope**

- Add distinct game modes built on the core puzzle loop
- Introduce tutorial and contextual help flows
- Add support tools such as an integrated calculator or prime-number reference
- Add timer and scoring where they strengthen the selected game modes
- Add progression and lightweight gamification systems
- Refine how difficulty supports mode selection and progression pacing

**Roadmap note**

The exact game mode set, scoring model, and progression depth should be validated after v2 establishes the broader puzzle flow.

---

## Review Triggers

- Move tutorial or help features earlier if onboarding becomes the main blocker after v1
- Split puzzle generation from v2 if content creation complexity slows delivery
- Reprioritize progression features based on real usage and playtesting feedback
