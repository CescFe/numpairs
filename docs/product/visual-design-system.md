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
- from default Material starter colors to a fixed NumPairs premium palette
- from local spacing and shape choices to reusable component guidance

The v1 identity principle still applies: NumPairs should be recognizable through symbols, arithmetic structure, component shapes, and clear layout.

v4 adds a stronger product UI direction on top of that foundation. The logo should still work in monochrome and adaptive contexts, but the app UI should now use one deliberate NumPairs theme instead of relying on Android dynamic color or system light/dark switching.

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

### Fixed Palette Over Dynamic Color

Dynamic color was useful during earlier prototype and polish phases, but it is no longer the v4 product direction.

v4 should implement a fixed NumPairs premium palette so the app has a stable visual identity across devices, screenshots, and Android versions.

### Single Default Theme

The v4 target is one fixed default theme with a premium dark-leaning presentation. It should remain stable regardless of the device system theme.

v4 should not implement separate light and dark app themes. If later product work needs alternate themes, that should become a deliberate future milestone.

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
- implement one fixed NumPairs color scheme
- ignore system light/dark theme for app presentation
- remove dynamic color from the default visual direction
- replace the starter purple fallback palette with the NumPairs premium palette
- expose NumPairs-specific visual defaults only where Material roles are too generic

### Color Strategy

The NumPairs UI should use a restrained premium dark-leaning color strategy:

- deep warm neutral background
- dark raised surfaces
- warm off-white primary text
- muted warm gray secondary text
- one jade-green brand and action accent color
- limited supporting state colors for player-owned focus and tutorial emphasis
- soft red for error and invalid states
- neutral gray for hidden and placeholder states

The app should avoid introducing multiple primary colors. The jade accent should carry brand personality, primary actions, selected states, positive states, and success feedback. Supporting state colors should stay narrow and functional rather than becoming alternate brand accents.

### Core Color Roles

The design system should describe colors by role before implementation assigns concrete values.

Recommended roles:

- `appBackground`: deep warm neutral screen background
- `surfaceBase`: dark surface for tiles, cards, dialogs, overlays, and sheets
- `surfaceRaised`: slightly lighter dark elevated surface with subtle tonal or shadow treatment
- `surfaceSubtle`: quiet grouped surface, such as the strip container
- `textPrimary`: warm off-white primary text
- `textSecondary`: muted warm gray supporting text
- `accent`: jade green brand and interaction accent
- `accentSoft`: lighter jade treatment for selection, active focus, and subtle highlights
- `focus`: cool focus treatment for player-owned or currently edited values
- `tutorialHighlight`: warm instructional highlight for guided tutorial focus
- `error`: soft red for invalid tiles and error feedback
- `errorSoft`: subtle red container or background treatment
- `hidden`: neutral gray for unknown values and placeholder states
- `borderSubtle`: low-contrast border for quiet component separation

### Final Implemented Palette

Implementation source:

- `app/src/main/java/org/cescfe/numpairs/ui/theme/Color.kt`
- `app/src/main/java/org/cescfe/numpairs/ui/theme/Theme.kt`
- `app/src/main/res/values/colors.xml`

Final v4 values:

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

- `primary` uses jade accent.
- `primaryContainer` uses jade soft and is reused for success containers.
- `secondary` uses player-owned focus while `secondaryContainer` stays neutral/subtle.
- `tertiary` uses tutorial highlight for guided focus borders.
- `error` and `errorContainer` use the soft red family.
- `background`, `surface`, `surfaceVariant`, and `surfaceContainer*` use the warm neutral surface family.

The native splash and window background use the same app background, and launcher/splash accent resources use the jade accent.

### Gameplay State Roles

Gameplay states should map consistently to the core color strategy:

- Known strip entries: stable dark or neutral surface with subtle border
- Hidden strip entries: neutral gray treatment that clearly communicates unknown value
- Player-entered strip entries: player-owned state with accent relationship, without overpowering the board
- Strip usage indicators: compact neutral `+` and `×` markers; available uses subtle surface treatment, used uses player-owned focus blue, and rule conflicts are not shown in the strip
- Selected or focused elements: accent or `accentSoft`
- Valid or completed states: jade green
- Invalid or incorrect states: soft red
- Completion success: jade green
- Completion invalid: soft red with actionable copy

### Color Usage Rules

- Use jade as the single brand and action accent.
- Keep focus blue, tutorial highlight, and error red limited to their state roles.
- Use color sparingly.
- Do not use color as the only state cue.
- Keep result numbers high contrast in every tile state.
- Keep error and invalid states noticeable but not visually overwhelming.
- Keep hidden and player-entered strip states distinct at a glance.
- Reserve the strongest accent treatment for primary actions, selected elements, focus, and positive feedback.
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

- Use deep warm neutral backgrounds to reduce visual fatigue and give the app a premium, calm feel.
- Use dark raised surfaces for tiles, cards, dialogs, and overlays.
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

Existing layout rationale remains documented in `docs/product/ux-decisions.md`.

---

## Component Direction

### App Shell And Menu

The menu is the first usable app screen after splash. It should look like a deliberate product entry point, not a prototype placeholder.

Expectations:

- show `NumPairs` clearly
- make generated `4 Pairs` the primary replayable action
- present Tutorial as a guided learning option
- make Tutorial and `4 Pairs` entries feel like proper product cards or actions
- avoid a marketing landing-page layout
- keep actions large enough for touch
- keep copy concise

### Buttons

Primary button:

- accent color background
- white text
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
- Use player-owned focus blue fill, matching border, and contrasting content for used markers.
- Do not use jade for strip usage because jade implies success or correctness.
- Do not use red for strip usage because conflicts belong to selector badges, local tile feedback, contextual messages, and final invalid feedback.
- Keep an opaque or subtle marker surface so the chip surface behind the marker does not visually bleed through.
- Preserve accessibility content descriptions for the operator and state descriptions for available versus used.

Narrow-screen readability is a hard constraint for this feature. If persistent usage markers compete with a strip value, reduce the number of columns and add balanced rows before reducing label clarity or enlarging the markers.

The implemented strip indicator treatment was checked by static Compose/layout review on 2026-06-21. The indicators overlay the chip edge instead of taking space from the centered label; strip labels remain single-line; chip tap behavior remains unchanged; and the indicator text uses the puzzle micro-label style so it scales with Android font settings without becoming the dominant chip content. This pass intentionally did not run on an emulator or physical device.

### Puzzle Tiles

Tiles are the main puzzle-reading surface.

State expectations:

- Normal: clear target result and editable expression slots
- Active slot: focused element is identifiable without disrupting layout
- Valid or correct, if surfaced: jade green, restrained
- Incorrect or invalid: soft red, attached to the expression or problem state
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

- success state uses jade green and feels rewarding but restrained
- invalid completion uses soft red and remains actionable
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
- advanced animations for v4
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
- aesthetic refinements must not reduce puzzle clarity

---

## Visual Consistency Rules

- Use jade as the single brand and action accent.
- Keep supporting state colors limited to focus, tutorial highlight, and error roles.
- Use color sparingly.
- Rely on spacing before decoration.
- Rely on typography before color.
- Rely on hierarchy before effects.
- Preserve clarity over aesthetics.
- Every visual element should reinforce puzzle solving, not distract from it.

---

## Implementation Guidance

Preferred implementation approach:

- keep changes incremental
- start with theme, palette, typography, and shared component defaults
- remove dynamic color from the default theme path for v4
- refactor duplicated visual constants only when there is a real shared role
- use existing component APIs where possible
- add component previews for important state combinations
- verify UI manually in the single default theme and narrow-screen states
- do not add alternate theme variants in v4

Likely implementation areas:

- `app/src/main/java/org/cescfe/numpairs/ui/theme/`
- `app/src/main/java/org/cescfe/numpairs/feature/menu/ui/`
- `app/src/main/java/org/cescfe/numpairs/feature/game/ui/`
- `app/src/main/java/org/cescfe/numpairs/feature/game/ui/components/`
- `app/src/main/java/org/cescfe/numpairs/feature/tutorial/`

The design system should not require a parallel UI framework. It should make the existing Compose implementation more coherent.

---

## v4 Visual QA Record

This QA record captures the documentation and static implementation review for the v4 visual baseline. It intentionally does not represent an emulator or physical-device run.

Review scope:

- Date: 2026-06-19; strip usage indicator follow-up on 2026-06-21
- Method: static review of Compose implementation, theme tokens, component defaults, previews, and existing UI test fixtures
- Device/emulator execution: not performed
- Reason device/emulator execution was not performed: this pass is constrained to avoid running anything against an emulator or physical device

Main flows checked by implementation review:

- Menu: `MenuScreen` uses the fixed `NumPairsTheme`, one primary generated `4 Pairs` CTA, one lower-emphasis Tutorial action, centered bounded content, and the jade app title.
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
- shape and symbols survive dark mode, small sizes, and launcher contexts
- a shape-first identity still fits a premium puzzle game better than a purely palette-driven brand

### Use A Single Fixed Premium Theme

Decision: v4 should implement one fixed NumPairs theme instead of relying on Android dynamic color or system light/dark theme switching.

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

- `docs/ui-behavior.md` already defines the active interaction baseline
- broad behavior changes would turn the milestone into a UX redesign
- visual polish can be evaluated more clearly when behavior remains stable

---

## Open Decisions

- Whether to introduce a small NumPairs-specific theme wrapper beyond Material `ColorScheme`
- Whether to keep all current tile and chip corner radii or normalize them further
- Whether screenshot or golden testing is worth adding during v4
- Whether launcher or splash assets need visual adjustments after UI refinement
- Exact motion treatment for completion and tutorial focus states

These open decisions should be resolved during v4 implementation only when the implementation needs them.
