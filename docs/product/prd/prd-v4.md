# PRD - NumPairs 🎨 v4 - Visual Design System & UI Refinement

> Historical note: this document captures the original visual design system & UI refinement scope. It remains as a historical snapshot and was later superseded.

## Product Summary

NumPairs is a native Android arithmetic deduction puzzle.

Players use numbers from a strip to complete hidden expressions on a board. Each pair of strip entries generates one addition result and one multiplication result, and the player must resolve the puzzle by deducing missing numbers, selecting operators, and matching pairings correctly.

v2 established the replayable generated `4 Pairs` loop. v3 improved comprehension through rules help and guided tutorial work. v4 focuses on visual quality, UI consistency, and a reusable design system foundation so the existing game can feel coherent before new content depth is added.

---

## Product Goal

Turn the current functional NumPairs experience into a polished, coherent, and reusable visual foundation for future milestones.

---

## Problem Statement

The product has a playable and understandable core loop, but the UI is still mostly shaped by default Material 3 styling plus component-local visual decisions.

That creates several risks:

- visual states can drift between chips, tiles, dialogs, selectors, and feedback surfaces
- spacing, shape, typography, and color choices are not yet documented as a system
- the default fallback palette still reads as a starter Material template rather than NumPairs-specific UI
- future features could add more screens and states without a shared visual language
- polish work may become subjective unless design decisions are captured before implementation

v4 should make the app:

- more visually intentional without changing the core rules
- easier to scan during play
- more consistent across menu, tutorial, generated gameplay, dialogs, selectors, and feedback states
- better prepared for future difficulty and mode expansion
- easier to maintain through shared visual defaults and documented component roles

---

## Target Users

- First-time players who need the UI hierarchy to make the puzzle approachable
- Casual puzzle players who expect a clean, readable mobile game experience
- Returning players who should be able to scan game state quickly
- Mobile-first players who should see one stable, premium NumPairs presentation regardless of device theme settings
- Contributors who need stable visual guidelines before adding future gameplay features

---

## Current Baseline At Start Of v4

The product entering v4 is the `v3 - Guided Play & Rules Onboarding` baseline.

That baseline currently includes:

- a branded Android launch experience with splash support
- a `Splash -> Menu` startup flow
- mode selection between `Tutorial` and generated `4 Pairs`
- a reusable game route and screen shared by gameplay modes
- generated low-difficulty `4 Pairs` puzzles
- tutorial-oriented instructional surfaces
- gameplay rules help available from generated `4 Pairs`
- an 8-tile result board and an 8-entry number strip
- direct strip editing and tile editing through contextual UI
- local incorrect-tile feedback and whole-puzzle completion feedback
- responsive Compose UI foundations
- Material 3 theming with legacy light/dark and dynamic color support from the current implementation
- first-pass visual identity documentation for the NumPairs logo and launcher direction
- documented interaction behavior and layout decisions

During v4 implementation, the visual-theme gap above is resolved by the fixed NumPairs theme. The startup window, splash colors, Compose `MaterialTheme`, and shared component defaults should remain aligned to the single-theme direction.

At the start of v4, that baseline did not yet include:

- a NumPairs-specific visual design system for app UI
- documented app-level color roles beyond the initial identity direction
- one finalized fixed premium app theme
- shared spacing, shape, typography, elevation, and motion decisions
- a documented component-state model for chips, tiles, selectors, dialogs, banners, and completion surfaces
- a visual QA baseline for the fixed default app theme
- broader content expansion such as additional difficulty levels or modes

---

## Product Principles

- Refine the existing product before expanding game content
- Preserve gameplay behavior unless a visual refinement exposes a clear usability issue
- Make arithmetic and deduction easier to scan, not more decorative
- Use Material 3 as the platform foundation while defining NumPairs-specific roles on top
- Keep the v1 shape-first identity direction as the brand anchor
- Use color to support state recognition, not as the only state indicator
- Prefer reusable component defaults over one-off styling
- Keep visual density mobile-first and readable on narrow devices
- Document decisions that future milestones should inherit

---

## Core UX Expectations

- The first screen should feel like a complete app menu, not a prototype placeholder
- Generated `4 Pairs` should remain the primary replayable gameplay entry
- Tutorial should still feel related to the same product system as generated play
- Strip entries should clearly communicate hidden, known, and player-entered states
- Board tiles should clearly communicate hidden slots, filled slots, target results, incorrect expressions, and other validation states
- Dialogs, sheets, popovers, banners, and overlays should share a coherent visual language
- The UI should remain readable in the single fixed default app theme
- Accessibility should remain part of visual quality: contrast, touch targets, text scaling, and non-color state cues matter
- Existing rules, puzzle generation, tutorial behavior, and replay behavior should remain stable

---

## v4 Scope

### Visual Design System Foundation

- Define the NumPairs app UI design principles in `docs/product/visual-design-system.md`
- Establish how the v1 logo and shape-first identity direction extend into app UI
- Define the intended relationship between Material 3 and NumPairs-specific component roles
- Document color roles for:
  - app background and surfaces
  - primary player actions
  - known strip entries
  - hidden strip entries
  - player-entered strip entries
  - active or highlighted puzzle elements
  - incorrect expressions
  - mismatched pairings
  - success and invalid completion feedback
- Document typography expectations for:
  - app and screen titles
  - menu actions
  - strip chip labels
  - tile expressions
  - tile results
  - dialog titles and body copy
  - tutorial and helper content
- Document shape, spacing, elevation, and motion principles for reusable UI components
- Define accessibility expectations for contrast, touch targets, text scaling, focus order, and non-color state cues

### UI Refinement

- Refine the menu screen so it presents Tutorial and generated `4 Pairs` as intentional product choices
- Refine the puzzle screen visual hierarchy while preserving the documented interaction model
- Refine strip, board, tile, selector, top bar, dialog, banner, and overlay presentation
- Improve consistency between Tutorial, generated gameplay, rules help, and completion feedback
- Replace starter-template and dynamic colors with one fixed NumPairs premium theme
- Keep app presentation independent from Android dynamic color and system light/dark theme
- Keep the current responsive layout strategy unless specific spacing or readability issues are found during refinement

### Technical Implementation

- Introduce shared visual defaults in the app theme or component defaults where they reduce duplication and drift
- Move reusable styling decisions out of isolated feature components when doing so improves maintainability
- Keep feature-specific component APIs stable unless visual refinement requires a small, justified API adjustment
- Add or update Compose previews for representative component states where they materially help visual review
- Avoid a broad UI rewrite; refine the existing Compose structure incrementally

---

## Out Of Scope

- New puzzle modes beyond generated `4 Pairs`
- Additional difficulty levels
- Changes to core puzzle rules
- Changes to puzzle generation constraints
- Adaptive hints
- Solver-backed help
- Puzzle-specific answer reveal
- User progression
- Scoring
- Persistence or save state
- Daily puzzles
- Backend or online features
- Full brand redesign
- Complex illustration system
- Advanced animations or game-like transitions
- Custom font licensing or typography work that blocks implementation
- Broad navigation restructuring beyond visual refinement of existing screens

---

## Success Criteria

- `docs/product/visual-design-system.md` defines the visual decisions needed to implement v4 consistently
- The app has a NumPairs-specific visual direction beyond default Material starter styling
- The menu, tutorial surfaces, generated gameplay, rules help, selectors, dialogs, and feedback states feel like one product
- Chips and tiles communicate their states clearly without relying on color alone
- The single fixed default theme remains readable
- Core gameplay behavior remains unchanged unless a change is explicitly documented
- Existing v3 functionality remains stable after UI refinement
- The codebase has clearer shared visual defaults for future features to reuse
- Documentation reflects v4 as the active product reference
