# NumPairs Visual Design System

## Purpose

This document defines the v4 visual design direction for the NumPairs app UI.

The goal is to establish a practical design system for implementation, not a speculative brand book. It should guide reusable Compose theme decisions, shared component defaults, and visual QA for the existing product screens.

This document belongs to the `v4 - Visual Design System & UI Refinement` milestone.

---

## Relationship To Existing Visual Direction

`docs/product/visual-direction.md` remains the source of truth for the v1 logo, launcher icon, and initial shape-first identity direction.

This document extends that identity into the app UI:

- from logo to interface language
- from isolated component styling to shared design decisions
- from default Material starter colors to NumPairs-specific semantic roles
- from local spacing and shape choices to reusable component guidance

The v1 identity principle still applies: NumPairs should be shape-first, not color-first. Color should support hierarchy and state, but the product should remain recognizable through symbols, arithmetic structure, component shapes, and clear layout.

---

## Product Tone

NumPairs should feel:

- clear
- focused
- precise
- calm
- modern
- approachable
- lightly game-like without becoming noisy

The product should avoid feeling like:

- a raw Material template
- a dense spreadsheet
- a casino-style number game
- a children's math worksheet
- a heavily illustrated puzzle app that distracts from deduction

The visual system should make the arithmetic puzzle easier to parse. Decoration is secondary to comprehension.

---

## Design Principles

### Readability Before Expression

Numbers, operators, unknown slots, and target results are the core content. Typography, spacing, and state treatments should make them easier to scan before adding visual personality.

### State Clarity

Players should quickly understand what is known, hidden, editable, selected, incorrect, matched, completed, or unavailable. Color may help, but shape, border, typography, labels, icons, and semantics should carry state too.

### Material 3 Foundation

NumPairs should continue using Material 3 for platform fit, accessibility defaults, and Android conventions. The design system should define NumPairs-specific roles on top of Material 3 rather than replacing it wholesale.

### Dynamic Color With Guardrails

Android dynamic color can remain supported, but the app should not depend on dynamic color to feel designed. A non-dynamic fallback palette should be intentional and NumPairs-specific.

### Compact Mobile-First Density

The UI should be comfortable on phones first. Puzzle elements should stay compact enough to keep the strip, board, and feedback readable without feeling cramped.

### Reusable Component Decisions

When a visual rule applies across screens or states, it should become a shared default or documented component role instead of staying as an isolated one-off value.

---

## Theme Direction

### Material Theme

`NumPairsTheme` should remain the app-level theme entry point.

Recommended direction:

- keep Material 3 as the base
- keep light and dark theme support
- keep dynamic color support where it remains accessible
- define a deliberate non-dynamic fallback color scheme
- avoid using the default starter purple palette as the long-term fallback identity
- expose NumPairs-specific visual defaults only where Material roles are too generic

### Semantic Color Roles

The design system should describe colors by role before implementation assigns concrete values.

Recommended roles:

- `appBackground`: default app background
- `surfaceBase`: standard screen and card surface
- `surfaceRaised`: dialogs, sheets, and elevated surfaces
- `surfaceSubtle`: strip container and quiet grouped areas
- `primaryAction`: primary player action, such as starting generated `4 Pairs`
- `secondaryAction`: secondary player action, such as Tutorial entry
- `chipKnown`: known starting strip entries
- `chipHidden`: hidden strip entries that invite player input
- `chipPlayerEntered`: strip entries entered by the player
- `tileNormal`: normal puzzle tile surface
- `tileActive`: highlighted tile or active slot treatment
- `tileIncorrect`: fully known expression that does not match the target result
- `tileMismatchedPairing`: fully known expression that is arithmetically valid but uses the wrong pairing relationship, if exposed by the UI
- `completionSuccess`: solved puzzle feedback
- `completionInvalid`: completed puzzle that still violates puzzle constraints

These roles can map to Material `ColorScheme` values, custom defaults, or a small wrapper around Material roles. The implementation should choose the least complex approach that keeps the UI consistent.

### Color Usage Rules

- Do not use color as the only state cue.
- Keep result numbers high contrast in every tile state.
- Keep error and invalid states noticeable but not visually overwhelming.
- Keep hidden and player-entered strip states distinct at a glance.
- Reserve strong accent color for interactive focus, active selection, or primary action.
- Avoid making every surface colorful; the board needs room for state colors to matter.

---

## Typography Direction

Typography should make arithmetic content clear before it creates personality.

Recommended roles:

- App title: Material title style with stable weight and no oversized hero treatment
- Screen title: compact top bar title
- Menu action label: readable label text with clear hierarchy between primary and secondary actions
- Strip chip label: short numeric label optimized for one to three digits and `?`
- Tile expression: clear operand and operator text with enough size to tap and scan
- Tile result: strongest numeric emphasis in the tile
- Dialog title: concise heading
- Dialog body: comfortable reading size for helper and tutorial copy
- Badge or micro-label text: only where it reduces ambiguity

Implementation notes:

- Prefer Material typography roles before adding custom text styles.
- Avoid viewport-scaled typography.
- Preserve readable line heights.
- Test one, two, and three-digit values.
- Test Android font scaling for puzzle-critical text.

---

## Shape And Surface Direction

The visual system should feel geometric and approachable, consistent with the logo direction.

Recommended shape guidance:

- Puzzle tiles: rounded rectangles, visually stable, not overly pill-shaped
- Strip chips: compact rounded chips with clear state borders
- Dialogs and sheets: Material 3 shapes unless a NumPairs-specific reason emerges
- Menu actions: consistent button shape and spacing
- Highlighted slots: small, precise shape treatment around the active arithmetic element

Surface guidance:

- Avoid nested card-heavy layouts.
- Use grouped surfaces where they clarify the puzzle structure, especially the strip.
- Keep tiles visually distinct from the page background.
- Keep shadows and elevation subtle; borders and tonal contrast are usually clearer for puzzle states.

---

## Spacing And Layout Direction

NumPairs should use a restrained spacing system that keeps puzzle content scannable.

Recommended direction:

- Use a 4dp-based spacing rhythm.
- Keep screen horizontal padding comfortable on phones.
- Keep the strip and board close enough to feel related.
- Preserve the current strip-above-board reading flow unless usability findings justify a layout change.
- Keep board tile width bounded so tiles do not stretch awkwardly on wider screens.
- Maintain enough gap between tiles for tap clarity and visual grouping.
- Use modal surfaces and bottom sheets without crowding content against device edges.

Existing layout rationale remains documented in `docs/product/ux-decisions.md`.

---

## Component Direction

### App Shell And Menu

The menu is the first usable app screen after splash. It should look like a deliberate product entry point.

Expectations:

- show `NumPairs` clearly
- make generated `4 Pairs` the primary replayable action
- present Tutorial as a guided learning option
- avoid a marketing landing-page layout
- keep actions large enough for touch
- keep copy concise

### Gameplay Top Bar

Expectations:

- keep back navigation predictable
- keep the mode title readable
- keep helper and tutorial actions visually consistent
- avoid overloading the top bar with too many icons

### Number Strip

The strip should communicate the player's available numbers at a glance.

State expectations:

- Known: stable starting value, lower emphasis than editable states
- Hidden: unresolved entry, clearly invites input
- Player-entered: player-owned value, distinct from known values
- Highlighted: visible during guided/tutorial focus without depending only on color
- Disabled or unavailable, if introduced later: visually subdued and semantically clear

### Puzzle Tiles

Tiles are the main puzzle-reading surface.

State expectations:

- Normal: clear target result and editable expression slots
- Active slot: focused element is identifiable without disrupting layout
- Incorrect: expression area is marked while target result remains legible
- Mismatched pairing: distinct from simple arithmetic error if this state is surfaced
- Highlighted/tutorial focus: visible but not confused with incorrect state
- Completed/correct: avoid adding noisy success styling per tile unless needed

### Selectors And Entry Dialogs

Selectors should feel connected to the element being edited.

Expectations:

- operand selection remains compact and number-first
- operator selection remains small and direct
- strip entry dialog remains clear and focused
- disabled options remain readable but clearly unavailable
- current helper badges or micro-indicators stay low-noise

### Rules, Tutorial, And Helper Surfaces

Instructional surfaces should share one content style.

Expectations:

- concise headings
- short explanatory text
- clear spacing between sections
- stable close and navigation actions
- no answer reveal styling
- visual relationship to the game screen without feeling like a separate product

### Completion And Validation Feedback

Feedback should be obvious and recoverable.

Expectations:

- success state feels rewarding but restrained
- invalid completion remains actionable
- incorrect tile state remains local and editable
- feedback surfaces should not obscure next steps
- color, iconography, and text should work together

---

## Motion Direction

Motion should be subtle and functional.

Recommended uses:

- opening and closing dialogs or sheets
- highlighting tutorial focus
- confirming completion
- small state transitions for selected or active elements

Avoid:

- long decorative animation
- motion that delays interaction
- high-energy transitions that fight puzzle concentration
- motion that is required to understand state

---

## Accessibility Requirements

Visual refinement must preserve or improve accessibility.

Requirements:

- touch targets should remain at least 48dp where practical
- text and icon contrast should meet accessible contrast expectations
- color-coded states must also have non-color cues
- content descriptions should remain meaningful for interactive elements
- state descriptions should remain meaningful for validation states
- dialogs and sheets should support predictable back and dismissal behavior
- UI should remain usable with increased font scale
- icons without visible text should have clear content descriptions

---

## Implementation Guidance

Preferred implementation approach:

- keep changes incremental
- start with theme and shared component defaults
- refactor duplicated visual constants only when there is a real shared role
- use existing component APIs where possible
- add component previews for important state combinations
- verify UI manually in representative light/dark and dynamic-color states

Likely implementation areas:

- `app/src/main/java/org/cescfe/numpairs/ui/theme/`
- `app/src/main/java/org/cescfe/numpairs/feature/menu/ui/`
- `app/src/main/java/org/cescfe/numpairs/feature/game/ui/`
- `app/src/main/java/org/cescfe/numpairs/feature/game/ui/components/`
- `app/src/main/java/org/cescfe/numpairs/feature/tutorial/`

The design system should not require a parallel UI framework. It should make the existing Compose implementation more coherent.

---

## Decisions

### Keep Material 3 As The Base

Decision: continue building on Material 3 rather than replacing it with a custom UI toolkit.

Rationale:

- aligns with the existing implementation
- keeps Android behavior familiar
- preserves accessibility and platform support
- lets v4 focus on refinement instead of rebuilding primitives

### Keep The Shape-First Identity

Decision: carry forward the v1 logo direction as the identity anchor.

Rationale:

- NumPairs' core identity is the unresolved arithmetic expression
- shape and symbols survive dynamic color, dark mode, and small sizes
- a shape-first system fits a puzzle game better than a palette-first identity at this stage

### Define A Non-Dynamic Fallback Palette

Decision: v4 should replace starter fallback colors with intentional NumPairs fallback colors.

Rationale:

- dynamic color is not available on all devices
- screenshots, documentation, and older Android versions need a designed default
- the current fallback palette can read as an uncustomized Material template

### Treat Gameplay State As A Design-System Concern

Decision: known, hidden, player-entered, active, incorrect, mismatched, success, and invalid states should be documented visual roles.

Rationale:

- these states appear across multiple components
- inconsistent styling would hurt puzzle comprehension
- future modes will need the same state vocabulary

### Preserve The Existing Interaction Model

Decision: v4 should refine visuals without changing core puzzle interactions by default.

Rationale:

- `docs/ui-behavior.md` already defines the active interaction baseline
- broad behavior changes would turn the milestone into a UX redesign
- visual polish can be evaluated more clearly when behavior remains stable

---

## Open Decisions

- Final non-dynamic light and dark fallback palette
- Whether to introduce a small NumPairs-specific theme wrapper beyond Material `ColorScheme`
- Exact typography tuning for tile expressions and results
- Whether to keep all current tile and chip corner radii or normalize them further
- Whether screenshot or golden testing is worth adding during v4
- Whether launcher or splash assets need visual adjustments after UI refinement
- Exact motion treatment for completion and tutorial focus states

These open decisions should be resolved during v4 implementation only when the implementation needs them.
