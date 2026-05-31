# NumPairs Product Roadmap (v3 -> v4)

## Purpose

This roadmap defines the active `v3 - Guided Play & Rules Onboarding` milestone and the product iterations that may follow the completed v2 replayable gameplay loop.

It is intentionally high-level, outcome-focused, and easy to update as implementation, feedback, and product priorities evolve.

---

## Current Baseline

### Completed v2 implementation

- Branded launch experience with splash support
- `Splash -> Menu` startup flow
- Mode selection between `Tutorial` and generated `4 Pairs`
- Reusable game route and screen shared by tutorial and generated modes
- `Tutorial` currently uses the handcrafted prototype seed puzzle
- Generated low-difficulty `4 Pairs` puzzles
- Internal generation, validation, and solver services for generated puzzles
- Generated puzzle replay from the completion flow
- Return-to-menu action from generated puzzle completion
- Isolated game state between tutorial and generated modes

The current baseline does not yet include a gameplay rules helper or an authored tutorial learning path.

---

## Roadmap Principles

- Teach the core game clearly before expanding content depth
- Group work by product outcomes rather than detailed task lists
- Keep generated puzzle architecture isolated from tutorial content
- Avoid introducing hints, answer reveal, or progression systems until the learning experience is clearer
- Revisit milestone scope at the end of each iteration

---

## Upcoming Milestones

### v3 - Guided Play & Rules Onboarding

**Goal**

Improve first-time player understanding by adding accessible rule help across game modes and replacing the prototype tutorial entry with a real guided tutorial experience.

**High-level scope**

- Add a rules/help action to the game screen top bar
- Make the helper available in Tutorial and generated `4 Pairs`
- Explain strip numbers, hidden values, board tiles, operands, operators, pair relationships, and completion validation
- Keep the helper informational only, without puzzle-specific hints or solver output
- Replace the current prototype tutorial puzzle with a basic authored tutorial puzzle
- Clarify the difference between Tutorial and generated `4 Pairs`
- Keep tutorial behavior isolated from generated mode state
- Keep rules helper content reusable across modes
- Keep tutorial puzzle/content separate from generated puzzle providers
- Add tests for helper availability, helper dismissal, state preservation, and tutorial entry content
- Update PRD and supporting product documentation to match the guided play scope

**Out of scope**

- New puzzle modes beyond `4 Pairs`
- Additional difficulty levels
- Adaptive hints
- Solver-backed help
- Puzzle-specific answer reveal
- User progression
- Scoring
- Persistence or save state
- Full multi-step onboarding system
- Advanced tutorial animations

### v4 - Difficulty And Mode Expansion (Provisional)

**Goal**

Build on the v3 learning foundation by expanding content depth once players can understand and replay the core game loop.

**Possible scope**

- Add additional difficulty profiles for generated `4 Pairs`
- Explore additional puzzle sizes or modes
- Improve puzzle balancing and reveal policies
- Revisit whether generated puzzles should require stronger solution uniqueness guarantees
- Consider progression, scoring, timer, daily puzzles, or persistence if playtesting shows they strengthen the selected modes
- Reassess convenience tools such as the in-game calculator proposal

**Roadmap note**

The exact v4 shape should be decided after v3 confirms whether rules help and the tutorial MVP are enough for new players to start generated `4 Pairs` confidently.

---

## Review Triggers

- Keep onboarding work in focus if players still fail to understand strip entries, pair relationships, or completion validation after v3
- Prioritize difficulty expansion if v3 makes the game clear but generated `4 Pairs` feels too shallow
- Prioritize additional modes if playtesting shows that different puzzle shapes are more valuable than deeper `4 Pairs`
- Move persistence earlier only if lack of session continuity becomes the main blocker
- Keep solver-backed hints and answer reveal out of scope unless they become a deliberate product direction, not a helper feature
