# PRD - NumPairs (v5)

## Document Status

- Status: canonical PRD for `v5 - Bigger Challenges with 8 Pairs`
- Supersedes: `docs/product/prd/prd-v4.md`
- Baseline inherited from: `v4 - Visual Design System & UI Refinement`
- Product direction: support larger generated puzzle sizes through reusable mode and puzzle-size modeling
- Related references:
  - `README.md`
  - `docs/product/puzzle-generation.md`
  - `docs/product/visual-design-system.md`
  - `docs/product/ux-decisions.md`
  - `docs/ui-behavior.md`
  - `docs/product/rules-helper.md`
  - `docs/product/tutorial.md`
  - `docs/game-rules.md`
  - `docs/ubiquitous-language.md`

`prd-v0.md` through `prd-v4.md` remain historical snapshots. `prd-v5.md` is the active product reference.

---

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle. Players complete hidden board expressions using numbers from a strip. Each strip pair produces one addition tile and one multiplication tile, and completion is validated through strip-entry identity, pair matching, and tile correctness.

v5 adds `8 Pairs` as a larger generated mode with 8 pairs, 16 strip entries, and 16 board tiles. It should preserve the same core rules, expression model, operator set, validation behavior, interaction model, tile states, and completion flow as generated `4 Pairs`.

---

## Product Goal

Add a larger generated puzzle mode without adding new mechanics.

The implementation should make puzzle size a reusable product concept so `4 Pairs`, `8 Pairs`, and future generated sizes can share generation, validation, game-screen, and navigation patterns instead of becoming parallel implementations.

---

## Problem Statement

Generated play currently assumes `4 Pairs`. That limits replayable depth and creates technical risk if larger modes are added by copying existing code.

v5 should address these risks:

- hard-coded 8-entry strip and 8-tile board assumptions
- duplicated generated-mode routing, provider, and session logic
- validation or completion drift between generated modes
- slower or unbounded generation for larger puzzles
- cramped board and strip layout on phone screens
- documentation that describes generated play as `4 Pairs` only

---

## Target Users

- Returning players who want a larger challenge after learning `4 Pairs`
- Casual puzzle players who expect the same rules and controls across generated modes
- Mobile-first players who need 16 tiles and 16 strip entries to remain readable
- Contributors who need generated modes to be modeled through reusable concepts

---

## Current Baseline At Start Of v5

The v4 baseline includes:

- `Splash -> Menu` startup
- Tutorial and generated `4 Pairs` entry points
- reusable Game route and screen behavior
- generated low-difficulty `4 Pairs`
- internal generation, validation, and solver services
- completion replay and return-to-menu actions
- isolated Tutorial and generated-mode state
- rules helper and solving tips for generated `4 Pairs`
- an 8-entry strip and 8-tile board for generated `4 Pairs`
- direct strip editing, tile editing, validation feedback, and success feedback
- fixed NumPairs visual theme and documented visual system
- documented game rules, UI behavior, UX rationale, and terminology

The baseline does not yet include:

- generated `8 Pairs`
- a menu entry point for larger generated puzzles
- 16-entry strip or 16-tile board rendering
- a reusable puzzle-size model covering both generated modes
- generation and validation coverage for larger puzzle sizes
- documented layout decisions for larger boards and strips

---

## Product Principles

- Expand generated content without changing core rules
- Treat puzzle size as configuration, not duplicated game logic
- Preserve Tutorial and generated `4 Pairs` behavior
- Validate generated puzzles before display
- Keep generation bounded and deterministic where needed for tests
- Reuse the v4 visual system for both generated modes
- Document shared rules once and keep size-specific decisions in generation and UX docs

---

## Core UX Expectations

- Menu offers Tutorial, generated `4 Pairs`, and generated `8 Pairs`
- `8 Pairs` starts a generated puzzle with 16 strip entries and 16 board tiles
- Players use the same interactions as `4 Pairs`: edit strip entries, assign operands/operators, reset tiles, validate state, and complete the puzzle
- Tile states, strip values, validation feedback, and completion feedback remain understandable in the larger layout
- No scoring, timers, progression, persistence, or new assistance systems are introduced

---

## v5 Scope

### Game Mode And Puzzle Size Model

- Add `8 Pairs` as a first-class generated game mode
- Introduce or refine shared mode/size configuration for:
  - mode id and display name
  - pair count
  - strip entry count
  - board tile count
  - generated vs Tutorial/static behavior where relevant
  - UI layout hints where needed
- Align generated `4 Pairs` with the shared model without behavior changes

### Puzzle Generation And Validation

- Extend generation to support `8 Pairs`
- Define and implement an `8 Pairs` difficulty profile before generator work
- Generate a solved puzzle first, derive the initial player-facing puzzle, then validate before display
- Reuse existing validation and solver logic where possible
- Preserve generated `4 Pairs` behavior
- Support deterministic generation for tests
- Use bounded attempts or clear failure handling
- Keep unique-solution guarantees out of scope unless already supported by the current validation approach

A valid generated puzzle must:

- satisfy shared NumPairs rules
- have a consistent solved board and strip
- be completable through the existing interaction model
- be accepted by the current validation/solver approach

### Navigation And Menu

- Add an `8 Pairs` menu entry
- Pass the selected generated mode to the Game screen correctly
- Keep Tutorial and generated `4 Pairs` entry points unchanged
- Keep Tutorial independent from generated-mode architecture

### Game Screen And Layout

- Render 16 tiles and 16 strip entries for `8 Pairs`
- Use a responsive board with 2-4 columns depending on width
- Render the 16-entry strip in two rows of 8 entries by default
- Preserve existing tile, strip, validation, and completion interactions
- Avoid clipped text, overlapping UI, and unusable touch targets on compact phones

### Testing And QA

- Cover shared puzzle-size configuration
- Cover `8 Pairs` generation, determinism, validation, and failure bounds
- Protect generated `4 Pairs` behavior after shared refactors
- Cover menu entry, navigation, 16-entry strip rendering, 16-tile board rendering, and basic interactions
- Add completion coverage with a controlled solvable `8 Pairs` puzzle

### Documentation

- Keep `docs/product/prd/prd-v5.md` as the active PRD
- Update `docs/product/puzzle-generation.md` with generated puzzle sizes and difficulty profiles
- Update `docs/game-rules.md` only if wording incorrectly assumes generated play always has 4 pairs
- Update `docs/ui-behavior.md` only for user-facing layout or interaction changes
- Update `docs/product/ux-decisions.md` with larger-board and larger-strip layout rationale
- Update `docs/ubiquitous-language.md` with shared mode, puzzle-size, and `8 Pairs` terms
- Keep Tutorial, rules helper, and visual-system docs focused on their existing source-of-truth roles

---

## Suggested Implementation Phases

1. Define the `8 Pairs` difficulty profile in documentation.
2. Introduce shared generated mode / puzzle-size configuration.
3. Align generated `4 Pairs` with the shared model.
4. Generalize generation and validation for larger puzzle sizes.
5. Implement `8 Pairs` generation and provider wiring.
6. Add menu/navigation support.
7. Refine Game screen layout for 16 tiles and 16 strip entries.
8. Add focused tests and update supporting docs.

---

## Out Of Scope

- New gameplay rules or operators
- Scoring, timers, progression, persistence, daily puzzles, online features
- Difficulty expansion beyond the initial `8 Pairs` profile
- Guaranteed unique solutions unless already supported by current validation
- Adaptive hints, solver-backed help, or answer reveal
- Advanced animations
- Tablet-specific redesign
- Full adaptive layout redesign beyond what `8 Pairs` needs
- Tutorial content changes unless required by shared architecture
- Generated `4 Pairs` gameplay changes

---

## Success Criteria

- Users can choose `8 Pairs` from the Menu Screen
- `8 Pairs` starts a generated puzzle with 16 strip entries and 16 board tiles
- `8 Pairs` follows the same core rules and interaction model as generated `4 Pairs`
- Generated `8 Pairs` puzzles are validated before display and can be completed end-to-end
- Generation is deterministic where needed for tests and bounded on failure
- The larger board and strip remain readable and usable on supported phone screens
- Tutorial and generated `4 Pairs` flows continue working unchanged
- Shared mode handling can support future puzzle sizes without major rewrites
- Tests cover the new mode and protect existing behavior
- Documentation reflects `8 Pairs` as a supported generated mode

---

## Documentation Alignment Notes

- `README.md` identifies `prd-v5.md` as the canonical product reference and v4 as the implemented baseline
- `docs/product/puzzle-generation.md` owns generated construction rules, difficulty profiles, masking, validation, determinism, and failure bounds
- `docs/game-rules.md` owns core rules shared by handcrafted and generated puzzles
- `docs/ui-behavior.md` owns in-puzzle interaction behavior
- `docs/product/ux-decisions.md` owns layout rationale for larger boards and strips
- `docs/ubiquitous-language.md` owns shared terminology
- `docs/product/visual-design-system.md` owns reusable visual roles
- `docs/product/tutorial.md` and `docs/product/rules-helper.md` stay focused on onboarding and help behavior

The scope boundary is that v5 expands generated puzzle size while preserving core rules and existing gameplay behavior.
