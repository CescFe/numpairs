# PRD - NumPairs (v5)

## Document Status

- Status: canonical PRD for the `v5 - Bigger Challenges with 8 Pairs` milestone
- Supersedes: `docs/product/prd/prd-v4.md`
- Feature baseline inherited from: `v4 - Visual Design System & UI Refinement`
- Current gameplay expansion direction: support larger generated puzzle sizes through reusable game mode and puzzle size modeling
- Related references:
  - `README.md`
  - `docs/product/roadmap.md`
  - `docs/product/puzzle-generation.md`
  - `docs/product/visual-design-system.md`
  - `docs/product/visual-direction.md`
  - `docs/product/ux-decisions.md`
  - `docs/ui-behavior.md`
  - `docs/product/rules-helper.md`
  - `docs/product/tutorial.md`
  - `docs/game-rules.md`
  - `docs/ubiquitous-language.md`

This document defines the product baseline that v5 is intended to establish.

`prd-v0.md`, `prd-v1.md`, `prd-v2.md`, `prd-v3.md`, and `prd-v4.md` remain in the repository as historical milestone snapshots. `prd-v5.md` is the canonical reference for the current NumPairs product direction, scope, and documentation alignment.

Implementation alignment note: v5 should add `8 Pairs` as a larger generated mode without changing the core NumPairs rules, the existing generated `4 Pairs` behavior, or the Tutorial learning flow.

---

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle.

Players use numbers from a strip to complete hidden expressions on a board. Each pair of strip entries generates one addition result and one multiplication result, and the player must resolve the puzzle by deducing missing numbers, selecting operators, and matching pairings correctly.

v2 established the replayable generated `4 Pairs` loop. v3 improved comprehension through rules help and guided tutorial work. v4 refined the visual system and app presentation. v5 expands the replayable generated experience by introducing `8 Pairs`, a larger puzzle size with 8 pairs, 16 board tiles, and 16 strip numbers.

`8 Pairs` should use the same expression model, operator set, operand/result constraints, validation behavior, interaction model, tile states, and completion flow as generated `4 Pairs`.

---

## Product Goal

Add a larger generated puzzle mode that gives players a bigger challenge while keeping NumPairs' rules, interactions, validation behavior, and visual system coherent.

The product goal is not to add new mechanics. The goal is to make puzzle size a reusable part of the product model so `4 Pairs`, `8 Pairs`, and future generated sizes can be supported without parallel implementations.

---

## Problem Statement

The current generated gameplay experience is centered on `4 Pairs`.

That mode proves the core replayable loop, but it gives players only one generated puzzle size. As the product grows, treating `4 Pairs` as a fixed special case creates several risks:

- generated-mode logic can become duplicated when larger puzzle sizes are added
- board and strip assumptions can remain hard-coded to 8 tiles and 8 strip entries
- validation and completion behavior can drift between modes if each mode is implemented separately
- generation may become slower or less reliable as the puzzle size increases
- a larger puzzle can become cramped on phone screens if layout decisions are not explicit
- documentation can keep describing generated play as `4 Pairs` only even after more modes exist

v5 should make the app:

- capable of offering `8 Pairs` as a first-class generated mode
- reusable enough to support future puzzle sizes without a major rewrite
- careful to preserve existing `4 Pairs` and Tutorial behavior
- clear about which rules are shared across modes and which details are size-specific
- practical to use on supported phone screens despite the larger board and strip

---

## Target Users

- Returning players who understand `4 Pairs` and want a larger challenge
- Casual puzzle players who expect the same controls and rules across generated modes
- Mobile-first players who need the larger board and strip to remain readable and tappable
- Contributors who need generated mode behavior to be modeled through reusable concepts rather than one-off mode code
- Product maintainers who need documentation to distinguish shared rules from puzzle-size-specific decisions

---

## Current Baseline At Start Of v5

The product entering v5 is the `v4 - Visual Design System & UI Refinement` baseline.

That baseline includes:

- a branded Android launch experience with splash support
- a `Splash -> Menu` startup flow
- mode selection between `Tutorial` and generated `4 Pairs`
- a reusable game route and screen shared by gameplay modes
- generated low-difficulty `4 Pairs` puzzles
- internal generation, validation, and solver services for generated puzzles
- generated puzzle replay from the completion flow
- return-to-menu action from generated puzzle completion
- isolated game state between Tutorial and generated modes
- gameplay rules help available from generated `4 Pairs`
- tutorial-oriented instructional content and authored learning flow
- an 8-tile result board and an 8-entry number strip for generated `4 Pairs`
- direct strip editing and tile editing through contextual UI
- local incorrect-tile feedback and whole-puzzle completion feedback
- a fixed NumPairs visual theme independent from Android dynamic color and system light/dark theme
- documented visual design system decisions for colors, typography, spacing, shape, component states, and accessibility expectations
- documented in-puzzle interaction behavior and layout rationale

At the start of v5, that baseline does not yet include:

- generated `8 Pairs`
- a menu entry point for larger generated puzzles
- 16-tile board rendering in generated play
- a 16-entry strip in generated play
- a reusable puzzle size model that fully separates mode configuration from `4 Pairs` implementation details
- validation and generation coverage for larger generated puzzle sizes
- documented layout decisions for larger generated boards and strips
- updated documentation that presents generated play as supporting both `4 Pairs` and `8 Pairs`

---

## Product Principles

- Expand generated content without changing the core rules
- Treat puzzle size as product configuration, not as duplicated game logic
- Preserve `4 Pairs` behavior unless a change is explicitly documented as shared architecture work
- Keep Tutorial independent from generated-mode expansion
- Validate generated puzzles before showing them to players
- Bound generation work so the app does not hang when a larger puzzle cannot be generated
- Make larger layouts usable through clear hierarchy, spacing, and scrolling decisions
- Reuse the v4 visual system instead of creating a separate look for `8 Pairs`
- Document shared rules and size-specific behavior before expanding further

---

## Core UX Expectations

- Players can choose `8 Pairs` from the Menu Screen
- Players can still choose Tutorial and generated `4 Pairs` from the Menu Screen
- Choosing `8 Pairs` starts a generated puzzle with 16 strip numbers and 16 board tiles
- `8 Pairs` follows the same rules and interaction model as generated `4 Pairs`
- Players can select strip numbers, assign hidden values, reset player-entered values, validate tile state, and complete the puzzle
- Tile states remain understandable with a larger board
- Strip values remain individually readable and selectable with 16 entries
- Completion and validation feedback remain visible and understandable in the larger layout
- The larger puzzle does not introduce scoring, progression, timers, persistence, or new assistance systems
- Existing Tutorial and generated `4 Pairs` flows continue to work unchanged

---

## v5 Scope

### Game Mode And Puzzle Size Model

- Add `8 Pairs` as a first-class generated game mode
- Model `8 Pairs` consistently with the existing generated `4 Pairs` mode
- Introduce or refine a reusable game mode / puzzle size abstraction that can describe:
  - mode id
  - display name
  - pair count
  - board tile count
  - strip size
  - generated vs Tutorial/static behavior where relevant
  - layout hints where needed by the UI
- Migrate or align `4 Pairs` with the shared model without changing its behavior
- Avoid duplicating domain logic that can be shared through the reusable mode / puzzle size abstraction
- Ensure `8 Pairs` defines:
  - 8 pairs
  - 16 strip numbers
  - 16 board tiles
  - the same expression model used by `4 Pairs`
  - the same operator set, operand/result constraints, validation rules, tile states, and completion rules used by `4 Pairs`

### Puzzle Generation And Validation

- Extend puzzle generation to support `8 Pairs`
- Generate valid `8 Pairs` puzzles before showing them to the player
- Define a valid generated puzzle as one that:
  - satisfies the shared NumPairs expression rules
  - has a consistent solved board and strip
  - can be completed through the existing gameplay interaction model
  - is accepted by the existing validation / solver approach
- Reuse existing validation and solver logic where possible
- Preserve existing `4 Pairs` generation behavior
- Add deterministic generation support where useful for tests
- Ensure generation has bounded attempts or clear failure handling so the app does not hang if a puzzle cannot be generated
- Keep unique-solution guarantees out of scope unless they are already provided by the current validation approach
- Ensure generated `8 Pairs` puzzles can be completed end-to-end

### Navigation And Menu

- Add an `8 Pairs` entry point to the Menu Screen
- Keep the existing Tutorial and generated `4 Pairs` entry points unchanged
- Ensure the selected mode is correctly passed to the Game Screen
- Make the new mode feel like a natural extension of the current menu structure
- Ensure Tutorial may continue using its existing fixed puzzle/setup and is not forced into generated-mode behavior by the shared architecture

### Game Screen Representation

- Render an `8 Pairs` board with 16 tiles
- Render an `8 Pairs` strip with 16 numbers
- Use an explicit layout strategy for the larger mode, such as a `4x4` board or another layout that remains readable on supported phone screens
- Preserve existing interactions:
  - selecting strip numbers
  - assigning values to hidden operands/results
  - resetting player-entered values
  - validating tile state
  - showing validation feedback
  - completing the puzzle
- Ensure all existing tile states remain understandable in the larger layout
- Preserve existing generated `4 Pairs` Game Screen behavior

### UI/UX Refinement

- Refine the `8 Pairs` layout so it remains usable on small Android screens
- Review board density, spacing, touch targets, scrolling behavior, and visual hierarchy
- Ensure the 16-number strip remains readable, individually selectable, and practical to interact with
- Avoid making the larger mode feel cramped or visually overwhelming
- Ensure there is no clipped text, overlapping UI, or unusable touch target at common compact phone widths
- Ensure completion and validation feedback remain visible and understandable with the larger layout
- Reuse the existing visual design system introduced in v4
- Avoid a full adaptive layout redesign beyond what is needed to make `8 Pairs` usable

### Testing And QA

- Add unit tests for the shared game mode / puzzle size configuration
- Add unit tests for `8 Pairs` domain configuration
- Add unit tests for `8 Pairs` puzzle generation
- Add unit tests to verify generated `8 Pairs` puzzles are valid according to the current validation / solver approach
- Add deterministic generation tests using fixed seeds where useful
- Add tests to ensure `4 Pairs` behavior remains unchanged after introducing the shared abstraction
- Add a generation sanity test to catch obvious performance or bounded-attempt regressions
- Add UI tests or preview coverage for the new Menu Screen entry point
- Add UI tests or preview coverage for the 16-tile Game Screen layout where valuable
- Add coverage for the selected mode being passed correctly from Menu Screen to Game Screen

### Documentation

- Add `docs/product/prd/prd-v5.md` as the canonical PRD
- Update `README.md` to identify `prd-v5.md` as the canonical product reference once v5 becomes the active milestone
- Update `docs/product/roadmap.md` to reflect v5 as the active milestone and v4 as the completed baseline
- Update `docs/product/puzzle-generation.md` to describe generated puzzle sizes instead of only generated `4 Pairs`
- Update `docs/game-rules.md` if any wording assumes that generated play only has 4 pairs
- Update `docs/ui-behavior.md` if the larger mode changes user-facing layout or interaction behavior
- Update `docs/product/ux-decisions.md` with layout rationale for the larger board and strip
- Update `docs/ubiquitous-language.md` when reusable game mode, puzzle size, or `8 Pairs` terminology becomes part of the shared language
- Keep `docs/product/tutorial.md` focused on Tutorial behavior rather than generated-mode expansion
- Keep `docs/product/rules-helper.md` focused on rules-help content and availability
- Keep `docs/product/visual-design-system.md` as the source of truth for visual roles reused by both generated modes

---

## Suggested Implementation Phases

1. Introduce the shared game mode / puzzle size model.
2. Migrate or align generated `4 Pairs` with the shared model without behavior changes.
3. Extend generation and validation support for `8 Pairs`.
4. Add deterministic generation coverage for tests.
5. Add the `8 Pairs` Menu Screen entry point and navigation wiring.
6. Refine the Game Screen layout for 16 tiles and 16 strip numbers.
7. Add focused unit, UI, and preview coverage.
8. Update PRD-adjacent documentation once implementation decisions become concrete.

---

## Out Of Scope

- New gameplay rules
- New operators
- New scoring system
- Persistence or save state
- Daily puzzles
- User progression
- Online features
- Difficulty expansion beyond the existing generation assumptions
- Guaranteed unique puzzle solutions, unless already supported by the current validation approach
- Adaptive hints
- Solver-backed help
- Puzzle-specific answer reveal
- Advanced animations
- Tablet-specific redesign
- Full adaptive layout redesign beyond what is needed for `8 Pairs`
- Changes to Tutorial content unless required by shared architecture
- Changes to generated `4 Pairs` gameplay behavior

---

## Success Criteria

- Users can choose `8 Pairs` from the Menu Screen
- `8 Pairs` starts a generated puzzle with 16 strip numbers and 16 board tiles
- `8 Pairs` follows the same core rules as generated `4 Pairs`
- `8 Pairs` uses the same expression model, operator set, operand/result constraints, validation rules, tile states, and completion rules as generated `4 Pairs`
- Generated `8 Pairs` puzzles are validated before being shown to the player
- Generated `8 Pairs` puzzles can be completed end-to-end
- Generation is deterministic where needed for tests
- Generation failure is bounded and handled cleanly
- The larger board and strip remain readable and usable on supported phone screens
- The 16-number strip remains practical to interact with
- Existing Tutorial and generated `4 Pairs` flows continue working unchanged
- Game mode handling is reusable enough to support future puzzle sizes without major rewrites
- Tests cover the new mode and protect existing `4 Pairs` behavior
- Documentation reflects `8 Pairs` as a supported generated game mode

---

## Documentation Alignment Notes

- `README.md` should identify `prd-v5.md` as the canonical product reference while treating v4 as the implemented baseline
- `docs/product/roadmap.md` should treat `v4 - Visual Design System & UI Refinement` as the completed baseline and `v5 - Bigger Challenges with 8 Pairs` as the active milestone
- `docs/product/puzzle-generation.md` should document both generated `4 Pairs` and generated `8 Pairs`, including shared construction rules, size-specific configuration, deterministic generation expectations, validation expectations, and bounded failure behavior
- `docs/game-rules.md` should remain the source of truth for the underlying puzzle rules and should describe shared generated-mode rules without implying that generated puzzles always have 4 pairs
- `docs/ui-behavior.md` should remain the source of truth for in-puzzle interaction behavior and should be extended only when the larger mode creates user-facing layout or interaction behavior worth documenting
- `docs/product/ux-decisions.md` should capture layout rationale for the larger board, 16-entry strip, density, spacing, scrolling behavior, and compact-screen tradeoffs
- `docs/ubiquitous-language.md` should define or refine shared terms such as generated mode, game mode, puzzle size, pair count, `4 Pairs`, and `8 Pairs`
- `docs/product/visual-design-system.md` should remain the source of truth for shared visual roles and should only change if `8 Pairs` requires a reusable visual-system decision
- `docs/product/tutorial.md` should continue to describe Tutorial content and should not become coupled to generated `8 Pairs`
- `docs/product/rules-helper.md` should continue to describe rules-help content and availability; it should only change if help availability changes for generated modes

The intentional scope boundary is that v5 expands generated puzzle size while preserving the core NumPairs rules and existing gameplay behavior. It does not add new mechanics, progression, scoring, persistence, online features, or a new assistance system.
