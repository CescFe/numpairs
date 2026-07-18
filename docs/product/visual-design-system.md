# NumPairs Visual Design System

## Purpose

This document defines the current NumPairs visual design system. It originated in the
`v4 - Visual Design System & UI Refinement` milestone and is updated through
`v9 - Game Feel & Personalization`.

The goal is to establish a practical design system for implementation, not a speculative brand book. It should guide reusable Compose theme decisions, shared component defaults, and visual QA for the existing product screens.

Sections explicitly labeled as historical preserve the v4 implementation and QA context;
the current contract is the five-theme v9 system described first.

---

## Relationship To Existing Visual Direction

[visual-direction.md](./visual-direction.md) remains the source of truth for the v1 logo,
launcher icon, and initial shape-first identity direction.

This document extends that identity into the app UI:

- from logo to interface language
- from isolated component styling to shared design decisions
- from default Material starter colors to five app-owned NumPairs palettes
- from local spacing and shape choices to reusable component guidance

The v1 identity principle still applies: NumPairs should be recognizable through symbols, arithmetic structure, component shapes, and clear layout.

v4 added the Warm visual baseline. v9 preserves that foundation and adds deliberate Warm,
Frost, Obsidian, Terminal, and Ember color themes instead of relying on Android dynamic color
or system light/dark switching. The selected palette changes appearance and in-app branding;
the logo geometry and every non-color design token remain stable.

---

## Product Identity

NumPairs should feel:

- elegant
- minimalist
- intelligent
- calm
- modern
- premium

NumPairs should not feel:

- childish
- educational
- arcade-like
- overly playful
- technology-centric
- like a raw Material template

The visual system should make the arithmetic puzzle easier to parse. Decoration is secondary to comprehension.

---

## Core Visual Principle

The board is the protagonist.

Every screen and component should support the puzzle experience without competing for attention.

Priority hierarchy:

1. Puzzle board
2. Number strip
3. Completion feedback
4. Navigation and actions
5. Decorative elements

Numbers and expressions should remain the primary visual focus. Color, surfaces, spacing, typography, and motion should reinforce puzzle solving rather than distract from it.

---

## Design Principles

### Readability Before Expression

Numbers, operators, unknown slots, and target results are the core content. Typography, spacing, and state treatments should make them easier to scan before adding visual personality.

### Board-First Hierarchy

The board should receive the strongest visual clarity. The strip, controls, helper surfaces, and completion states should support the board without creating visual noise.

### State Clarity

Players should quickly understand what is known, hidden, editable, selected, valid, invalid, completed, or unavailable. Color may help, but shape, border, typography, labels, icons, and semantics should carry state too.

### Premium Minimalism

The app should rely on spacing before decoration, typography before color, and hierarchy before effects. Avoid unnecessary gradients, excessive color, heavy shadows, and decorative elements that do not improve puzzle comprehension.

### Material 3 Foundation

NumPairs should continue using Material 3 for platform fit, accessibility defaults, and Android conventions. The design system should define NumPairs-specific visual decisions on top of Material 3 rather than replacing it wholesale.

### App-Owned Palettes Over Dynamic Color

Dynamic color was useful during earlier prototype and polish phases, but it is no longer the
product direction.

NumPairs defines its own finite palettes so the app has a designed visual identity across
devices, screenshots, and Android versions.

### Five Color-Only Themes

Warm remains the default and safe fallback. Frost, Obsidian, Terminal, and Ember are
user-selectable alternatives persisted by stable identity.

Themes change color only. Typography, font sizes, shapes, spacing, elevation, layout, touch
targets, interactions, and gameplay rules do not vary by theme.

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
- map one of five explicit NumPairs color schemes from the persisted theme identity
- ignore system light/dark theme for app presentation
- remove dynamic color from the default visual direction
- use Warm as the missing, corrupt, or unsupported preference fallback
- expose NumPairs-specific visual defaults only where Material roles are too generic

### Color Strategy

The NumPairs UI separates two color ownership groups:

- **Appearance colors** vary by theme: backgrounds, surfaces, text, outlines, brand primary,
  brand accent, and decorative in-app branding.
- **Gameplay semantic colors** preserve meaning across themes: success/completion,
  error/invalid, player selection/focus, tutorial highlight, hidden/placeholder, and
  available/used indicators.

Exact semantic lightness and containers may adapt for contrast on light or dark surfaces,
but their recognizable families and non-color cues stay consistent. A theme must never make
an error look like an ordinary brand accent or a selected value look completed.

Theme character:

| Theme | Appearance direction |
| --- | --- |
| Warm | Deep olive-neutral surfaces, warm cream text, and jade brand accents |
| Frost | Very light cool-blue surfaces, blue brand primary, and restrained gold accent |
| Obsidian | Charcoal and warm-black surfaces with elegant warm highlights |
| Terminal | Black and near-black surfaces with phosphor-inspired green branding |
| Ember | Cream and warm-neutral surfaces with orange and restrained red-orange branding |

### Core Color Roles

The design system should describe colors by role before implementation assigns concrete values.

Appearance roles use Material `ColorScheme` for background, surface levels, content,
outlines, primary/secondary/tertiary branding, containers, and scrim.

NumPairs semantic roles are kept in a separate theme-local contract:

- `success`, `onSuccess`, `successContainer`, and `onSuccessContainer`
- `error`, `onError`, `errorContainer`, and `onErrorContainer`
- `selection`, `onSelection`, `selectionContainer`, and `onSelectionContainer`
- `tutorialHighlight` and `onTutorialHighlight`
- `hiddenContainer`, `onHiddenContainer`, and `hiddenBorder`

### Implemented Theme Definitions

Implementation source:

- `app/src/main/java/org/cescfe/numpairs/ui/theme/Color.kt`
- `app/src/main/java/org/cescfe/numpairs/ui/theme/Theme.kt`
- `app/src/main/java/org/cescfe/numpairs/ui/theme/NumPairsThemeDefinition.kt`

Warm preserves the final v4 values:

- App background: `#2F332A`
- Surface base: `#3A3F34`
- Surface raised: `#454B3E`
- Surface subtle: `#363B31`
- Text primary: `#F2EDE2`
- Text secondary: `#C8C1B4`
- Border: `#5B6253`
- Border subtle: `#4A5145`
- Jade accent: `#9CBD7B`
- Jade accent content: `#25301D`
- Jade soft container: `#46583A`
- Jade soft content: `#E3F0D4`
- Player-owned focus: `#AFC7E8`
- Player-owned focus content: `#162538`
- Tutorial highlight: `#D7B56D`
- Tutorial highlight content: `#33270E`
- Error: `#E58A7A`
- Error soft container: `#5A3733`
- Error soft content: `#FFDAD4`
- Scrim: `#CC000000`

Material role mapping:

- `primary` and its container carry the active theme's brand direction.
- `secondary` and `tertiary` remain appearance roles; gameplay selection and tutorial
  meaning come from the separate semantic contract.
- Material `error` remains aligned with the semantic error family, while gameplay consumes
  the explicit semantic role.
- `background`, `surface`, `surfaceVariant`, and `surfaceContainer*` use each theme's
  appearance surface family.

In-app branding consumes the active appearance palette. The native splash, window
background, and launcher resources remain static Warm under
[ADR-004](../technical/adr/adr-004-keep-v9-platform-branding-static.md).

### Gameplay State Roles

Gameplay states should map consistently to the core color strategy:

- Known strip entries: stable dark or neutral surface with subtle border
- Hidden strip entries: neutral gray treatment that clearly communicates unknown value
- Player-entered strip entries: player-owned state with accent relationship, without overpowering the board
- Strip usage indicators: compact neutral `+` and `×` markers; available uses subtle surface
  treatment, used uses semantic selection, and rule conflicts are not shown in the strip
- Selected or focused elements: semantic selection family
- Valid or completed states: semantic success family
- Invalid or incorrect states: semantic error family
- Completion success: semantic success with non-color confirmation
- Completion invalid: semantic error with actionable copy

### Color Usage Rules

- Use the selected appearance palette for brand and primary action treatments.
- Keep selection, tutorial, success, error, hidden, and usage colors limited to their
  semantic roles.
- Use color sparingly.
- Do not use color as the only state cue.
- Keep result numbers high contrast in every tile state.
- Keep error and invalid states noticeable but not visually overwhelming.
- Keep hidden and player-entered strip states distinct at a glance.
- Distinguish primary action, selection, and positive feedback even when a theme's brand hue
  is close to one of those semantic families.
- Avoid making every surface colorful; the board needs room for state colors to matter.

---

## Typography Direction

Typography should make arithmetic content clear before it creates personality.

### UI Typography

Use Inter for general UI typography where practical.

Inter should be used for:

- menus
- buttons
- titles
- navigation
- dialogs
- helper text
- tutorial text
- supporting copy

Intended characteristics:

- modern
- clean
- highly legible
- professional

### Puzzle Typography

Use JetBrains Mono for puzzle content where practical.

JetBrains Mono should be used for:

- tile expressions
- tile results
- strip numbers
- puzzle-specific numeric labels

Intended characteristics:

- precise
- distinctive numeric glyphs
- strong mathematical feel
- clear separation between UI content and gameplay content

### Typography Roles

Recommended roles:

- App title: refined title style with stable weight and no oversized hero treatment
- Screen title: compact top bar or header title
- Menu action label: readable label text with clear hierarchy between primary and secondary actions
- Strip chip label: monospaced numeric label optimized for one to three digits and `?`
- Tile expression: monospaced operand and operator text with enough size to tap and scan
- Tile result: strongest numeric emphasis in the tile
- Dialog title: concise heading
- Dialog body: comfortable reading size for helper and tutorial copy
- Badge or micro-label text: only where it reduces ambiguity

Implementation notes:

- Prefer Material typography roles as the structural baseline.
- Apply Inter and JetBrains Mono through the theme or shared text styles rather than local one-offs.
- Provide sensible fallbacks if custom font integration is deferred.
- Avoid viewport-scaled typography.
- Preserve readable line heights.
- Test one, two, and three-digit values.
- Test Android font scaling for puzzle-critical text.

### Final Implemented Typography

Implementation source:

- `app/src/main/java/org/cescfe/numpairs/ui/theme/Type.kt`
- `app/src/main/java/org/cescfe/numpairs/ui/theme/NumPairsTextStyles.kt`
- `app/src/main/res/font/`

Final font families:

- General UI: Inter, bundled as regular, medium, semibold, and bold weights.
- Puzzle typography: JetBrains Mono, bundled as regular, medium, semibold, and bold weights.

Material typography baseline:

- `displayLarge`: Inter bold, `40sp` / `48sp`
- `displayMedium`: Inter bold, `36sp` / `44sp`
- `displaySmall`: Inter bold, `32sp` / `40sp`
- `headlineLarge`: Inter semibold, `32sp` / `40sp`
- `headlineMedium`: Inter semibold, `28sp` / `34sp`
- `headlineSmall`: Inter semibold, `24sp` / `32sp`
- `titleLarge`: Inter semibold, `22sp` / `28sp`
- `titleMedium`: Inter semibold, `18sp` / `24sp`
- `titleSmall`: Inter medium, `14sp` / `20sp`
- `bodyLarge`: Inter regular, `16sp` / `24sp`
- `bodyMedium`: Inter regular, `14sp` / `20sp`
- `bodySmall`: Inter regular, `12sp` / `16sp`
- `labelLarge`: Inter semibold, `15sp` / `20sp`
- `labelMedium`: Inter medium, `13sp` / `18sp`
- `labelSmall`: Inter medium, `11sp` / `16sp`

Puzzle text styles:

- Tile expression: JetBrains Mono semibold, `18sp` / `24sp`
- Compact tile expression: JetBrains Mono medium, `14sp` / `20sp`, used for operands with three or more characters
- Tile result: JetBrains Mono semibold, `32sp` / `48sp`
- Strip value: JetBrains Mono medium, `14sp` / `20sp`
- Operand option: JetBrains Mono semibold, `18sp` / `24sp`
- Numeric input: JetBrains Mono regular, `16sp` / `24sp`
- Operator option: JetBrains Mono regular, `16sp` / `24sp`
- Selected operator option: JetBrains Mono semibold, `16sp` / `24sp`
- Puzzle micro-label: JetBrains Mono medium, `11sp` / `16sp`

Puzzle-critical text uses `sp` units, not viewport-scaled values. Tile operands switch to the compact expression style for three-digit values to preserve legibility inside bounded tile widths.

---

## Shape And Surface Direction

The visual system should feel clean, structured, spacious, and easy to scan.

Recommended shape guidance:

- Primary buttons: large corner radius, comfortable touch target
- Secondary buttons: same shape family as primary buttons, lower visual weight
- Icon buttons: simple circular or rounded container with minimal visual weight
- Puzzle tiles: rounded rectangles, visually stable, not overly pill-shaped
- Strip chips: compact rounded chips with clear state borders
- Dialogs and sheets: clean dark raised surfaces with restrained shape treatment
- Menu cards or actions: consistent radius and spacing
- Highlighted slots: small, precise shape treatment around the active arithmetic element

Surface guidance:

- Use the selected theme's background and surface hierarchy while preserving comparable
  contrast and visual weight.
- Use raised surfaces for tiles, cards, dialogs, and overlays.
- Avoid nested card-heavy layouts.
- Use grouped surfaces where they clarify the puzzle structure, especially the strip.
- Keep tiles visually distinct from the page background.
- Keep shadows and elevation subtle; borders and tonal contrast are usually clearer for puzzle states.

---

## Spacing And Layout Direction

NumPairs should use a restrained spacing system that keeps puzzle content scannable.

Recommended direction:

- Use spacing before decoration.
- Use a 4dp-based spacing rhythm.
- Keep screen horizontal padding comfortable on phones.
- Keep the strip and board close enough to feel related.
- Preserve the current strip-above-board reading flow unless usability findings justify a layout change.
- Keep board tile width bounded so tiles do not stretch awkwardly on wider screens.
- Maintain enough gap between tiles for tap clarity and visual grouping.
- Use modal surfaces and bottom sheets without crowding content against device edges.

Existing layout rationale remains documented in
[ux-decisions.md](./ux-decisions.md).

---

## Component Direction

### App Shell And Menu

The menu is the first usable app screen after splash. It should look like a deliberate product entry point, not a prototype placeholder.

Expectations:

- show `NumPairs` clearly
- make generated modes prominent replayable actions
- present Tutorial as a guided learning option
- expose Personalization as a lower-emphasis unlocked-menu action
- make Tutorial, Personalization, and generated-mode entries feel like proper product actions
- avoid a marketing landing-page layout
- keep actions large enough for touch
- keep copy concise

### Buttons

Primary button:

- accent color background
- contrasting `onPrimary` content
- large corner radius
- comfortable touch target
- used for the main action on the screen

Secondary button:

- neutral surface
- border-only or low-emphasis treatment
- less visual weight than the primary button
- used for alternative actions

Icon button:

- simple circular or rounded container
- minimal visual weight
- used for navigation, help, settings, and compact actions

### Gameplay Top Bar

The top bar should reduce the generic Android look and keep focus on gameplay.

Expectations:

- minimal visual weight
- compact header treatment where practical
- context-focused title
- predictable back navigation
- visually consistent helper and tutorial actions
- no overloading with too many icons
- more screen space for gameplay

### Number Strip

The strip should communicate the player's available numbers at a glance.

State expectations:

- Known: stable starting value, lower emphasis than editable states
- Hidden: unresolved entry using neutral gray and clear placeholder treatment
- Player-entered: player-owned value, distinct from known values
- Usage indicators: compact persistent `+` and `×` markers for visible strip entries
- Unused: both operator-family markers read as available
- Addition-used: `+` reads as used while `×` remains available
- Multiplication-used: `×` reads as used while `+` remains available
- Fully-used: both operator-family markers read as used
- Selected or highlighted: visible accent treatment without depending only on color
- Disabled or unavailable, if introduced later: visually subdued and semantically clear

The strip usage treatment should be more compact than the operand selector badges. It should support persistent scanning without turning each chip into a dense mini-card. The numeric value remains the primary content of the chip.

Used versus available marker states must not rely on color alone. Keep the `+` and `×` symbols visible and pair color with at least one additional cue such as fill, border, weight, opacity, or state description.

Final compact treatment:

- Render the indicators as two small pill markers over the chip's top edge.
- Keep hidden strip chips indicator-free.
- Use the same `+` and `×` semantics as the operand selector badges.
- Use subtle surface, muted content, and subtle border for available markers.
- Use semantic selection fill, matching border, and contrasting content for used markers.
- Do not use theme brand or semantic success for strip usage because those imply action or
  correctness.
- Do not use semantic error for strip usage because conflicts belong to selector badges,
  local tile feedback, contextual messages, and final invalid feedback.
- Keep an opaque or subtle marker surface so the chip surface behind the marker does not visually bleed through.
- Preserve accessibility content descriptions for the operator and state descriptions for available versus used.

Narrow-screen readability is a hard constraint for this feature. If persistent usage markers compete with a strip value, reduce the number of columns and add balanced rows before reducing label clarity or enlarging the markers.

The implemented strip indicator treatment was checked by static Compose/layout review on 2026-06-21. The indicators overlay the chip edge instead of taking space from the centered label; strip labels remain single-line; chip tap behavior remains unchanged; and the indicator text uses the puzzle micro-label style so it scales with Android font settings without becoming the dominant chip content. This pass intentionally did not run on an emulator or physical device.

### Puzzle Tiles

Tiles are the main puzzle-reading surface.

State expectations:

- Normal: clear target result and editable expression slots
- Active slot: focused element is identifiable without disrupting layout
- Valid or correct, if surfaced: semantic success, restrained
- Incorrect or invalid: semantic error, attached to the expression or problem state
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
- operand selector usage badges remain contextual and more legible than strip usage markers
- strip usage markers and operand selector badges use the same `+` / `×` semantics
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

- success state uses the semantic success contract and feels rewarding but restrained
- invalid completion uses the semantic error contract and remains actionable
- incorrect tile state remains local and editable
- feedback surfaces should not obscure next steps
- color, iconography, and text should work together

---

## Motion Direction

Motion should be subtle and functional.

The implemented v9 generated-game feedback adds:

- a brief scale response when a committed player action makes a tile correct
- one restrained board response and completion-overlay entrance when a player action solves
  the puzzle
- a short entrance for a replacement puzzle only after its successor session is safely
  stored and adopted

Those motions are enabled only by generated `4 Pairs` and `8 Pairs` routes. Tutorial,
required onboarding, voluntary `How to play`, restored state, and recomposition do not gain
or replay v9 feedback. Motion does not resize layout or touch targets, block actions, or
carry meaning that is absent from the final static state.

Avoid:

- long decorative animation
- motion that delays interaction
- high-energy transitions that fight puzzle concentration
- motion that is required to understand state
- persisted animation progress or feedback replay

When Android animation duration is disabled, each treatment reaches the same usable final
state immediately.

---

## Accessibility Requirements

Visual refinement must preserve or improve accessibility.

Requirements:

- interactive targets should remain at least `48dp` under the shared UI contract
- small text should reach at least `4.5:1` contrast
- large text and meaningful graphics should reach at least `3:1` contrast
- color-coded states must also have non-color cues
- content descriptions should remain meaningful for interactive elements
- state descriptions should remain meaningful for validation states
- dialogs and sheets should support predictable back and dismissal behavior
- UI should remain usable with increased font scale
- icons without visible text should have clear content descriptions
- aesthetic refinements must not reduce puzzle clarity

---

## Visual Consistency Rules

- Use the active theme's appearance roles for brand, surfaces, text, and primary actions.
- Use the NumPairs semantic contract for success, error, selection, tutorial, hidden, and
  operator-usage meaning.
- Keep typography, shapes, spacing, elevation, layout, and touch geometry invariant.
- Use color sparingly.
- Rely on spacing before decoration.
- Rely on typography before color.
- Rely on hierarchy before effects.
- Preserve clarity over aesthetics.
- Every visual element should reinforce puzzle solving, not distract from it.

---

## Current v9 Implementation Guidance

Preferred implementation approach:

- keep changes incremental
- resolve a stable persisted theme identity and fall back safely to Warm
- keep appearance `ColorScheme` roles separate from NumPairs gameplay semantic roles
- refactor duplicated visual constants only when there is a real shared role
- use existing component APIs where possible
- use the shared five-theme preview parameter for Menu, Game, and Personalization
- validate every theme's defined role pairs and narrow-screen states
- do not add theme-dependent typography, shapes, spacing, elevation, layout, or controls

Likely implementation areas:

- `app/src/main/java/org/cescfe/numpairs/ui/theme/`
- `app/src/main/java/org/cescfe/numpairs/feature/menu/ui/`
- `app/src/main/java/org/cescfe/numpairs/feature/game/ui/`
- `app/src/main/java/org/cescfe/numpairs/feature/game/ui/components/`
- `app/src/main/java/org/cescfe/numpairs/feature/tutorial/`

The design system should not require a parallel UI framework. It should make the existing Compose implementation more coherent.

---

## Historical v4 Visual QA Record

This QA record captures the documentation and static implementation review that established
the Warm v4 visual baseline. References to one fixed theme and jade branding in this record
describe that historical pass, not the current five-theme contract. It intentionally does
not represent an emulator or physical-device run.

Review scope:

- Date: 2026-06-19; strip usage indicator follow-up on 2026-06-21
- Method: static review of Compose implementation, theme tokens, component defaults, previews, and existing UI test fixtures
- Device/emulator execution: not performed
- Reason device/emulator execution was not performed: this pass is constrained to avoid running anything against an emulator or physical device

Main flows checked by implementation review:

- Menu: `MenuScreen` uses the fixed `NumPairsTheme`, primary CTAs for the implemented generated modes, one lower-emphasis Tutorial action, centered bounded content, and the jade app title.
- Tutorial: `TutorialRoute` and `TutorialOverlayHost` reuse the same themed game surface and instructional overlay path instead of introducing a separate visual language.
- Generated `4 Pairs`: `FourPairsRoute` uses the shared `GameScreen`, fixed theme, completion actions, Rules Helper entry point, and Solving Tips entry point.
- Rules Helper: `RulesHelperDialog` uses the raised surface, shared shape, Inter content hierarchy, themed close action, scroll fades, and a low-noise Tutorial action.
- Solving Tips: `SolvingTipsDialog` mirrors the Rules Helper surface, content hierarchy, close action, scroll fades, and practice action.
- Selectors: operand selection, operator selection, and strip entry dialog use shared raised/subtle surfaces, rounded shapes, monospaced numeric text, state borders, and semantic descriptions.
- Invalid feedback: incorrect tiles use error container, error border, error expression color, preserved result contrast, and state descriptions; invalid whole-puzzle feedback uses the error banner.
- Success overlay: solved completion uses jade success container, scrim, concise confirmation copy, and themed replay/menu actions.

Narrow-screen readability review:

- Menu content is bounded to `360dp`, fills narrower widths, and keeps actions at the shared `52dp` height.
- Game content scrolls vertically, uses `16dp` horizontal screen padding, and keeps the strip above the board.
- Board tiles reflow by available width and stay within `112dp` to `144dp`, with `12dp` gaps and centered rows.
- Tile expression slots keep stable widths and minimum heights; operator slots stay fixed at `28dp`.
- The number strip uses at most eight columns. Eight entries use one row when their labels fit; sixteen entries use two rows of eight by default. On narrower widths or larger font scales, it reduces to balanced rows before chips become too narrow.
- Strip usage indicators render as compact `+` / `×` overlays above visible strip chips, so they do not reduce the centered label's available row space.
- Strip chips have a bounded width, `48dp` minimum height, `4dp` horizontal gaps, and reserved vertical space for usage indicators so adjacent rows do not overlap.
- Dialog and helper content uses constrained scroll regions (`420dp` helper content max height, `320dp` operand sheet max height).
- The strip layout should remain on the visual QA watch list for future device passes, especially for larger accessibility font scales and future profiles with wider numeric values.

Increased font-scale review for puzzle-critical text:

- Puzzle-critical text uses `sp` values and shared JetBrains Mono styles rather than viewport-scaled typography.
- Three-character tile operands switch to the compact tile expression style.
- Tile result typography remains the strongest numeric emphasis at `32sp` / `48sp`.
- Strip labels use JetBrains Mono `14sp` / `20sp`; strip usage indicators use the puzzle micro-label style at `11sp` / `16sp`.
- Strip chips keep a `48dp` minimum touch height, and the indicator overlay does not change chip height or tap behavior.
- Three-digit strip labels and compact indicators were checked by static implementation review for increased font-scale risk; any future increase in marker size should receive a device visual pass.
- Tile expression slots keep a `40dp` minimum height.
- Existing UI test fixtures include large operand coverage for three-digit tile operands and stable tap target bounds, but this pass did not execute those tests on a device.

---

## Historical v4 Decisions

These decisions explain the origin of the current system. Where noted, v9 deliberately
supersedes their v4-only scope.

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
- shape and symbols survive dark mode, small sizes, and launcher contexts
- a shape-first identity still fits a premium puzzle game better than a purely palette-driven brand

### Use A Single Fixed Premium Theme

Decision: v4 should implement one fixed NumPairs theme instead of relying on Android dynamic color or system light/dark theme switching.

Current status: superseded by v9's five explicit color-only themes. The app-owned,
non-dynamic palette principle and Warm fallback remain.

Rationale:

- the app needs a stable, recognizable visual identity
- a single jade accent creates stronger product consistency than device-derived colors
- screenshots, documentation, and all Android versions need the same designed baseline
- the previous starter purple fallback reads as an uncustomized Material template

### Make The Board The Protagonist

Decision: visual hierarchy should prioritize the puzzle board above navigation chrome, decorative elements, and secondary actions.

Rationale:

- the board is the core product experience
- puzzle readability matters more than visual decoration
- supporting UI should reduce cognitive load, not compete for attention

### Use Inter For UI And JetBrains Mono For Puzzle Content

Decision: v4 should separate general UI typography from puzzle typography.

Rationale:

- Inter supports a clean, modern, premium product UI
- JetBrains Mono gives puzzle numbers and expressions a precise mathematical feel
- separating UI and puzzle text strengthens hierarchy and scanability

### Treat Gameplay State As A Design-System Concern

Decision: known, hidden, player-entered, selected, valid, invalid, completed, and unavailable states should be documented visual roles.

Rationale:

- these states appear across multiple components
- inconsistent styling would hurt puzzle comprehension
- future modes will need the same state vocabulary

### Preserve The Existing Interaction Model

Decision: v4 should refine visuals without changing core puzzle interactions by default.

Rationale:

- [ui-behavior.md](../ui-behavior.md) already defines the active interaction baseline
- broad behavior changes would turn the milestone into a UX redesign
- visual polish can be evaluated more clearly when behavior remains stable

---

## Historical v4 Open Decisions

These questions were recorded at the end of v4. v9 resolved the color wrapper, platform
branding, and generated completion-motion questions through the explicit semantic theme
contract, [ADR-004](../technical/adr/adr-004-keep-v9-platform-branding-static.md), and the
generated-only feedback implementation. Shape normalization and screenshot/golden testing
remain possible future work.

No unresolved historical item changes the current v9 behavior documented above.
