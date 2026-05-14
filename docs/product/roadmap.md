# NumPairs Product Roadmap (v2 -> v3)

## Purpose

This roadmap defines the remaining work in `v2 - Puzzle Generation & Replay Loop` and the product iterations that follow the current early v2 implementation baseline.

It is intentionally high-level, outcome-focused, and easy to update as implementation, feedback, and product priorities evolve.

---

## Current Baseline

### Early v2 implementation

- Branded launch experience with splash support
- `Splash -> Menu -> Tutorial -> Game` startup flow
- `Tutorial` uses the handcrafted seed puzzle as the current playable entry point
- Local tile validation and completion feedback remain in the existing game screen
- Improved UI clarity, accessibility semantics, and reset behavior
- No generated puzzles, replay loop from completion, persistence, or multiple playable modes yet

---

## Roadmap Principles

- Prioritize a polished and stable core before adding retention systems
- Group work by product outcomes rather than detailed task lists
- Revisit milestone scope at the end of each iteration

---

## Upcoming Milestones

### v2 - Puzzle Generation & Replay Loop

**Goal**

Introduce the first replayable NumPairs gameplay loop through puzzle generation, mode selection, and completion routing.

**High-level scope**

- Add a menu screen after the splash screen
- Add navigation flow between menu and game screens
- Keep the existing handcrafted puzzle as a `Tutorial` mode
- Add a generated `4 Pairs` mode
- Add replay and return-to-menu actions from the completion flow
- Define low-difficulty generation rules for `4 Pairs`
- Establish an initial low-difficulty classification model
- Implement a generator plus validation and solver services
- Isolate generation and validation logic from the UI layer
- Add tests for solver correctness and generated puzzle validity
- Update PRD and supporting product documentation to match the replayable gameplay loop

**Implementation note**

The menu-driven startup path and `Tutorial` entry point are already part of the current baseline. The remaining v2 focus is generated puzzle content, validation architecture, and replay actions after completion.

**Out of scope**

- Persistence or save state
- User progression systems
- Scoring systems and timer
- Daily puzzles
- Online features
- Advanced puzzle balancing
- Guaranteed unique puzzle solutions
- Additional puzzle modes beyond generated `4 Pairs`
- Full onboarding beyond the lightweight tutorial entry point

### v3 - Game Modes, Guidance & Progression

**Goal**

Build on the v2 loop with broader content systems, stronger guidance, and longer-term engagement mechanics once generation and replay are stable.

**High-level scope**

- Expand beyond `4 Pairs` into additional modes or puzzle sizes
- Introduce contextual help, onboarding refinement, and deeper tutorial support
- Add persistence or save state if replay sessions benefit from it
- Introduce difficulty progression, scoring, timer, or daily content where they strengthen the selected modes
- Add lightweight progression systems if product testing shows they improve retention

**Roadmap note**

The exact game mode set, persistence needs, and progression depth should be validated after v2 proves the replayable puzzle loop.

---

## Review Triggers

- Move persistence earlier if lack of session continuity becomes the main blocker after v2
- Split puzzle generation depth from v2 if replay and navigation can ship first with a narrower low-difficulty generator
- Reprioritize progression or scoring features based on playtesting feedback from the generated `4 Pairs` loop
