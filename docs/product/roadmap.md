# NumPairs Product Roadmap (v4)

## Purpose

This roadmap defines the active `v4 - Visual Design System & UI Refinement` milestone and the product iterations that may follow the guided play foundation established in v3.

It is intentionally high-level, outcome-focused, and easy to update as implementation, feedback, and product priorities evolve.

---

## Current Baseline

### Completed v3 product baseline

- Branded launch experience with splash support
- `Splash -> Menu` startup flow
- Mode selection between `Tutorial` and generated `4 Pairs`
- Reusable game route and screen shared by gameplay modes
- Generated low-difficulty `4 Pairs` puzzles
- Internal generation, validation, and solver services for generated puzzles
- Generated puzzle replay from the completion flow
- Return-to-menu action from generated puzzle completion
- Isolated game state between tutorial and generated modes
- Gameplay rules helper available from generated `4 Pairs`
- Tutorial-oriented instructional content and authored learning flow
- Documented in-puzzle interaction model

The current baseline does not yet include a reusable visual design system for app UI or a fully refined NumPairs-specific presentation layer.

---

## Roadmap Principles

- Make the existing product clear, polished, and consistent before expanding content depth
- Group work by product outcomes rather than detailed task lists
- Preserve the core puzzle interaction model unless a usability issue justifies changing it
- Keep generated puzzle architecture isolated from visual presentation decisions
- Avoid introducing hints, answer reveal, progression, scoring, or persistence until the core experience is visually stable
- Revisit milestone scope at the end of each iteration

---

## Upcoming Milestones

### v4 - Visual Design System & UI Refinement

**Goal**

Turn the current functional NumPairs experience into a polished, coherent, and reusable visual foundation for future milestones.

**High-level scope**

- Add `docs/product/prd/prd-v4.md` as the canonical product reference
- Add `docs/product/visual-design-system.md` as the v4 design-system reference
- Define NumPairs visual principles for app UI
- Extend the v1 shape-first identity direction into gameplay and app surfaces
- Establish semantic visual roles for colors, typography, shape, spacing, elevation, and motion
- Define an intentional non-dynamic fallback palette while preserving Android dynamic color support
- Refine the menu as an intentional product entry point
- Refine the puzzle screen visual hierarchy while preserving documented interaction behavior
- Refine strip chips, puzzle tiles, selectors, top bars, dialogs, tutorial surfaces, and completion feedback
- Improve consistency between Tutorial, generated `4 Pairs`, rules help, and feedback states
- Keep state communication accessible and avoid relying on color alone
- Add or update previews, tests, and manual visual QA checks where they materially reduce risk
- Update README, roadmap, UX decisions, and UI behavior docs when implementation decisions become concrete

**Out of scope**

- New puzzle modes beyond `4 Pairs`
- Additional difficulty levels
- Puzzle rule changes
- Puzzle generation changes
- Adaptive hints
- Solver-backed help
- Puzzle-specific answer reveal
- User progression
- Scoring
- Persistence or save state
- Daily puzzles
- Full brand redesign
- Broad navigation restructuring

### v5 - Difficulty And Mode Expansion (Provisional)

**Goal**

Build on the v4 visual foundation by expanding content depth once the core product feels clear, polished, and visually consistent.

**Possible scope**

- Add additional difficulty profiles for generated `4 Pairs`
- Explore additional puzzle sizes or modes
- Improve puzzle balancing and reveal policies
- Revisit whether generated puzzles should require stronger solution uniqueness guarantees
- Consider progression, scoring, timer, daily puzzles, or persistence if playtesting shows they strengthen the selected modes
- Reassess convenience tools such as the in-game calculator proposal

**Roadmap note**

The exact v5 shape should be decided after v4 confirms whether the existing gameplay loop has a strong enough visual and UI foundation for broader content expansion.

---

## Review Triggers

- Keep visual refinement in focus if players understand the rules but still struggle to scan strip states, tile states, or completion feedback
- Prioritize difficulty expansion if v4 makes the game clear and polished but generated `4 Pairs` feels too shallow
- Prioritize additional modes if playtesting shows that different puzzle shapes are more valuable than deeper `4 Pairs`
- Move persistence earlier only if lack of session continuity becomes the main blocker
- Keep solver-backed hints and answer reveal out of scope unless they become a deliberate product direction, not a helper or polish feature
