# NumPairs UI Behavior

## Overview

This document defines the current v10 menu, Quick, Daily Challenge, generated
difficulty selection, personalization, session routing, generated-game feedback, completion,
calendar, sharing, and in-puzzle interaction model for NumPairs.

It complements the game rules described in [game-rules.md](./game-rules.md) and focuses on:

1. Number strip behavior
2. Result grid behavior
3. Contextual editing flows
4. Gameplay top bar helper behavior
5. Quick, Daily Challenge, and generated difficulty selection
6. Normal and Daily session routing and completion behavior
7. Daily calendar and textual sharing
8. Persistent color personalization and generated-only feedback

This is the interaction baseline shared where applicable by Tutorial, Quick, normal generated
`4 Pairs` and `8 Pairs`, and Daily Challenge gameplay.

Required-onboarding behavior is documented in
[PRD v6](./product/prd/prd-v6.md). The reliable-session product contract is documented in
[PRD v7](./product/prd/prd-v7.md), and its storage boundary is documented in
[generated-session-persistence.md](./technical/generated-session-persistence.md).
Generated difficulty selection and challenge expansion are documented in
[PRD v8](./product/prd/prd-v8.md), and the sparse challenge catalog is recorded in
[ADR-005](./technical/adr/adr-005-model-sparse-generated-challenges.md).
Personalization and generated-game feedback are documented in
[PRD v9](./product/prd/prd-v9.md); the platform-branding boundary is recorded in
[ADR-004](./technical/adr/adr-004-keep-v9-platform-branding-static.md).
Quick and Daily Challenge are documented in
[PRD v10](./product/prd/prd-v10.md). Their persistence and architecture boundaries are recorded in
[daily-challenge-persistence.md](./technical/daily-challenge-persistence.md) and
[ADR-006](./technical/adr/adr-006-model-daily-challenge-as-versioned-local-cadence.md).

---

## Terminology

- **Strip item**: the visible chip and value state currently shown for one strip entry position in the number strip
- **Strip entry**: the unique game element behind a strip item, with stable identity even if visible values reorder
- **Editable run**: a contiguous strip segment made only of hidden and player-entered items, delimited by known items or strip edges
- **Operand slot**: one editable operand position in the top row of a tile
- **Operator slot**: the editable operator position in the top row of a tile
- **Contextual selector**: a small anchored popover or bubble used to choose a value for a grid slot
- **Entry dialog**: the dialog used to enter or edit a number in the strip
- **Rules helper**: an informational dialog opened from the game top app bar to explain core game rules
- **Usage indicator**: a compact `+` or `×` marker that shows whether a visible strip entry is already used by that operator family
- **Difficulty selector**: the anchored, mode-specific single-choice popup used to choose one
  supported generated challenge from the normal menu
- **Daily action**: the state-aware primary region that starts, continues, or reopens completion
  for the current local Daily Challenge
- **Calendar action**: the trailing Daily region that opens local Daily Completion History
- **Daily completion summary**: the content shown after completion or when reopening a completed
  current-date Daily Challenge without reconstructing its solved puzzle

In this document, strip items are rendered as chips.

---

## Interaction Principles

- Mobile-first behavior: interactions must not depend on hover
- Single tap is the primary gesture
- Grid editing happens directly from the tapped slot
- No prior strip selection is required to edit the grid
- Slot editing should use a small contextual selector instead of a full-screen flow whenever possible
- Rules help should explain the game without changing the current puzzle state

---

## Normal Menu, Quick, Daily Challenge, And Session Routing

The unlocked normal menu renders actions in this order:

1. the unified state-aware Daily Challenge split primary CTA
2. `Resume`, only while one valid unfinished normal generated session is available
3. the direct `Quick` primary CTA
4. the unified `4 Pairs · <difficulty>` split primary CTA
5. the unified `8 Pairs · <difficulty>` split primary CTA
6. `How to play`

Daily Challenge, `Resume`, Quick, and both generated-mode actions use the full-width primary CTA
treatment. The Daily CTA contains an expanding state-aware region and a trailing calendar region.
Each generated-mode CTA contains an expanding play region and a trailing difficulty region. Split
regions have no visual gap and use a themed vertical divider.

`How to play` uses the lower-emphasis secondary treatment. A themed settings icon action in the
top-right corner of the Menu TopAppBar opens Personalization and exposes a localized accessibility
description.

The localized normal `Resume` accessibility description identifies the saved mode and difficulty,
for example `Resume 4 Pairs · Medium puzzle` or `Resume Quick · Low puzzle`.

The application derives normal menu resumability only from the one normal generated-session slot.
Missing, solved, unknown-mode, mode/profile-mismatched, corrupt, and unsupported snapshots do not
expose normal `Resume`.

Selecting `Resume` opens the saved mode and exact current puzzle without generation.

Selecting a generated-mode play region starts its displayed challenge immediately. The displayed
challenge uses the mode-specific fallback on first use and the last supported difficulty the
player selected for that mode thereafter. Its visible label uses the same typography and weight
as the other normal-menu button labels. Compact or increased-font layouts may ellipsize visible
text while preserving the full localized challenge in accessibility semantics.

### Daily Challenge Menu Action

The Daily Challenge row uses the established unified split primary CTA geometry:

- the expanding primary region starts, continues, or reopens completion for the current local
  Daily Challenge
- the trailing region opens the calendar
- a themed vertical divider separates the two button semantics
- both regions keep the established minimum interactive target
- the trailing region uses a calendar icon whose localized action description is independent from
  the primary state

Status: implemented in the unlocked Menu with state-aware primary navigation and an independent
calendar region.

The primary label and semantics resolve from one captured current local date:

- no completion and no matching resumable Daily Session: `Daily challenge`
- no completion and one exact matching resumable Daily Session: `Continue daily`
- any completion record for the current local date: `Daily completed`

The completed label, selected semantics, and completion state description communicate completion
without relying on color or iconography alone. Missing, prior-date, solved-without-completion,
unsupported-recipe, recipe-mismatched, malformed, and corrupt Daily snapshots never expose
`Continue daily`.

Activating `Daily challenge`:

1. captures the current local date and recipe identity
2. opens Daily gameplay in a loading state
3. attempts the same bounded deterministic candidate sequence for that identity
4. publishes gameplay only after the exact Daily Session snapshot is stored

Duplicate taps do not launch duplicate generation. Failure or cancellation shows a recoverable
Daily failure state with retry and back-to-menu actions. Retry repeats the same recipe candidate
sequence and never substitutes device randomness. A prior-date Daily snapshot remains intact until
a current-date successor is stored, but it remains hidden from play.

Activating `Continue daily` restores the exact current puzzle without generation or mutation.

Activating `Daily completed` opens the current-date Daily completion summary. It does not restore
the cleared solved session, generate another puzzle, or change completion history.

The calendar region opens the calendar from every Daily state without starting, restoring,
replacing, or completing a puzzle.

Daily state does not read or write the normal generated-session slot or remembered `4 Pairs`
difficulty. Normal generated actions do not read or write Daily state.

### Quick Action

Status: implemented.

Quick uses one full-width primary CTA labelled `Quick`. It has no trailing difficulty action while
`3 Pairs Low` is its only supported challenge.

Its accessibility description identifies the complete challenge as Quick, 3 Pairs, Low. The
gameplay title and normal Resume copy use `Quick · Low`, while the stable generated mode remains
`3 Pairs`.

Activating Quick starts the explicit `3 Pairs Low` challenge through the normal generated-session
flow. If no normal session is resumable, it starts directly. If a normal session is resumable, it
uses the shared resume-or-replace dialog. Quick never reads, replaces, or resumes Daily state.

### Difficulty Selector

The trailing difficulty region inside each generated-mode split primary CTA opens an anchored
single-choice popup without leaving the normal menu. It retains the established minimum touch
target and changes from the supplied down arrow to the supplied up arrow while expanded. The play
and difficulty regions remain distinct button semantics, and at most one mode popup is open.

The popup shows only challenges present in the supported generated-challenge catalog:

- `4 Pairs` shows `Low` and `Medium`
- `8 Pairs` shows `Medium` and `Hard`

All shown options are enabled from the beginning. The popup has no locked option, progress
indicator, completion requirement, reward, or explanation of how to unlock content. Selection is
communicated through label and control state rather than color alone, and every option and action
keeps the established minimum touch target and readable text-scaling behavior.

On opening, the selected option is the last supported difficulty the player explicitly chose for
that mode. The two modes remember their choices independently. A missing, corrupt, unknown, or
unsupported stored value is presented using `Low` for `4 Pairs` and `Medium` for `8 Pairs` without
rewriting storage.

Tapping a supported difficulty makes it the current option and immediately persists that explicit
choice for the selected mode. It updates the associated play region and closes the popup without
starting a puzzle. Merely opening or dismissing the popup or displaying a fallback does not write a
preference. Tapping outside, pressing system back, or activating the expanded difficulty region
dismisses the popup without changing the generated-session slot.

The popup's logical end edge aligns with the trailing difficulty region's logical end edge and
expands inward, so it remains inside the menu content bounds on compact and wide layouts. The
alignment follows layout direction rather than assuming a physical right edge.

The generated-mode play region identifies the exact requested challenge, for example
`4 Pairs · Medium`. Activating it starts the existing resume-or-replace routing for that challenge.
Difficulty is fixed once play begins; generated gameplay and completion do not expose a
change-difficulty action.

### Resume Or Replace

Activating Quick or a generated-mode play region while a normal generated session is resumable
opens the same modal choice:

- primary: `Resume`
- secondary: start a new puzzle for the requested challenge

Same-challenge, same-mode/different-difficulty, and different-mode selections use one concise
supporting message: `You have an unfinished <saved mode> · <saved difficulty> puzzle.` The message
uses the larger body text treatment. The secondary label always identifies the requested
replacement challenge as `New <selected mode> · <selected difficulty>` for 4 Pairs and 8 Pairs or
`New Quick` for Quick. The primary action's accessibility description identifies the saved
challenge.

The primary action uses the shared primary CTA treatment and established button shape.

The choice dialog has no visible cancel, back, close, or third action. Tapping outside or pressing system back dismisses it without navigation, generation, or session mutation. Action handling is deduplicated.

Selecting the Daily calendar action, `How to play`, Settings, guided first run, or Tutorial never
replaces or updates either session slot.

### Guided First Run And Tutorial Replay

A fresh installation routes from Splash into the three-step Learn basics Tutorial before this
menu is available. Required playback exposes a low-emphasis `Skip tutorial` action from every
step. Requesting skip opens a confirmation dialog whose recommended action continues Tutorial and
whose explicit alternative skips anyway. Only confirmed skip unlocks Menu early; it does not open
a final validation puzzle.

Completing the third authored Tutorial step also unlocks Menu directly. Completed and skipped
outcomes open Menu on later launches. Clearing all application data or reinstalling starts a new
local first run; clearing only the application cache does not. `How to play` starts the same
three-step content voluntarily from Step 1, remains dismissible, and never changes resolved
first-run state. The in-game `Play tutorial` action uses the same content in an overlay while
preserving the generated puzzle underneath.

## Personalization

Selecting the settings action in the unlocked Menu TopAppBar opens Personalization without
changing the current navigation, onboarding, or generated-session state.

The screen presents exactly five color themes:

1. Warm
2. Frost
3. Obsidian
4. Terminal
5. Ember

Selecting a theme applies it immediately across the Compose application and persists its
stable identity for later launches. Warm is the default and fallback for missing or
unsupported stored values. Selection is communicated by label and state, not color alone.

Themes change only appearance colors. Typography, shapes, spacing, elevation, layout,
touch geometry, controls, and the semantic meaning of success, error, selection, tutorial,
and hidden states remain shared.

The screen also exposes one `Game haptics` preference. It defaults to enabled, persists
independently from onboarding and generated sessions, and controls only accepted-assignment
haptics in generated games. Android's system touch-feedback setting remains authoritative.
There are no sound, error-haptic, typography, shape, motion, or difficulty controls on the
Personalization screen. Generated difficulty is selected only through the mode-specific popup in
the normal menu.

In-app NumPairs branding follows the selected appearance palette. The system splash and
launcher stay static and Warm. The packaged monochrome icon remains available for Android
system-themed icons, whose tint is controlled by the launcher rather than NumPairs.

## Normal Generated Completion And Replay

A solved normal Quick, 4 Pairs, or 8 Pairs puzzle shows exactly:

- primary: `Play another`
- secondary: `Back to menu`

When a player action solves a generated puzzle, the board gives one brief restrained pulse
and the completion surface enters once. Recomposition, restoration, preview state, and an
already-solved initial puzzle do not replay the celebration. Completion actions remain
usable throughout the final visual state.

`Play another` runs the existing bounded generation and safe-replacement pipeline for the exact
mode and difficulty of the completed session. It does not consult or rewrite either remembered
selector default. The solved puzzle remains visible while its successor is generating, validating,
or being
stored. Failure or cancellation keeps the completion surface available. Only after a
successor is safely stored and adopted does a brief entrance transition introduce it; the
transition is transient and is not persisted.

Solving clears menu resumability before `Back to menu` returns to the menu. Completion does not
record progression or completion counts. There is no `Change difficulty`, restart, timer, or
additional completion action.

---

## Daily Gameplay, Completion, And Rollover

Status: application composition, gameplay, recoverable failure, immediate completion,
completed-today summary, calendar return routing, and sharing integration implemented; the
state-aware Menu entry is implemented.

Daily gameplay reuses the generated Game screen and the `4 Pairs Low` challenge selected by its
recipe. It does not expose a Daily difficulty selector or consult the remembered normal `4 Pairs`
difficulty.

The gameplay TopAppBar title identifies Daily and the captured local date using localized concise
date copy. Its full accessibility description also identifies `4 Pairs · Low`. Back navigation
returns to the normal menu without changing Daily progress.

If the device-local date changes while Daily remains visible:

- the visible puzzle and title keep the captured Daily Challenge identity
- the route does not regenerate, replace, dismiss, or reset at midnight
- accepted progress continues to persist under the captured identity
- solving records completion for the captured date

After returning to Menu on a later local date, an unfinished prior-date Daily Session no longer
appears as `Continue daily`. Starting the new current-date Daily safely replaces it only after a
successor is stored. The prior date cannot be opened through the calendar.

### Daily Completion

When a committed player action solves Daily, the existing generated completion celebration runs
once. The atomic completion transition records the Daily Challenge identity and removes the active
Daily Session before the completed Menu state is published.

The immediate completion surface keeps the solved board visible and shows exactly:

- primary: `Share result`
- secondary: `View calendar`
- tertiary navigation: `Back to menu`

It does not show `Play another`, restart, change difficulty, solution reveal, timer, score, or
replay.

If the player later activates `Daily completed`, NumPairs opens a lightweight Daily completion
summary for the current date. Because the solved session snapshot has been removed, the summary
does not reconstruct or display the solved board. It identifies the date and `4 Pairs · Low` and
shows the same Share result, View calendar, and Back to menu actions.

Opening either completion surface does not add another completion record. Pressing system back
returns to the normal menu.

### Daily Calendar

Status: monthly calendar model, read-only Compose surface, application destination, and
completion-surface and trailing Menu return routing implemented.

The Daily calendar is a dedicated local-history destination with:

- back navigation
- a localized month and year heading
- previous-month and next-month actions
- localized weekday headings and locale-appropriate first day of week
- a seven-column informational date grid

It opens on the month containing the captured current local date. Previous-month navigation is
available. Next-month navigation is disabled while the current month is displayed, so future
months cannot be browsed.

Calendar date states are:

- **Today**
  - uses a visible outline or equivalent non-fill cue
  - exposes a localized `Today` semantic description
- **Completed**
  - uses a check mark or equivalent stable non-color cue
  - exposes a localized `Completed` semantic description
- **Today and completed**
  - exposes both states without one cue replacing the other
- **Past without completion**
  - remains visually neutral
  - is described as a date, not as missed, failed, or incomplete
- **Future**
  - uses subdued informational treatment
  - is unavailable
- **Outside the displayed month**
  - is omitted or rendered as a non-interactive empty grid position

Date cells are informational and do not open gameplay, completion details, or sharing. They do not
need to simulate interactive button semantics. Month navigation actions retain the established
minimum touch target.

Changing month, opening or closing the calendar, and reading a completion record never mutate
Daily Session, completion history, normal generated state, onboarding, or preferences. The
calendar has no streak, percentage, reward, best time, score, ranking, or missed-day treatment.

Back navigation returns to the surface that opened the calendar: Menu for the trailing Menu action
and the applicable Daily completion surface for `View calendar`. Calendar navigation is transient;
process recreation does not require restoring the displayed month or caller.

### Daily Textual Sharing

Status: non-spoiling localized text formatting and the Android Sharesheet boundary implemented;
integrated with valid immediate and reopened Daily completion surfaces.

`Share result` opens the Android Sharesheet with localized `text/plain` content containing:

- `NumPairs Daily`
- the completed local date
- `4 Pairs · Low`
- an explicit completed status

One representative structure is:

```text
NumPairs Daily · 25 Jul 2026
4 Pairs · Low · Completed
```

The localized date and completion text may vary by locale, but the shared result never contains
strip values, board results, pairs, operators, tile positions, a solved-board encoding, time,
score, action count, streak, or ranking.

The Sharesheet chooser title is localized. Cancelling sharing, returning from the target
application, or having no compatible target does not change completion, calendar, session, or
navigation state and must not crash NumPairs. Sharesheet state is transient and is never restored
after process death.

Past calendar dates do not expose sharing in v10.

### v10 Visual And Accessibility Contract

- Daily reuses `NumPairsComponents.PrimarySplitCtaButton`.
- Quick and normal Resume reuse `NumPairsComponents.PrimaryCtaButton`.
- Daily split regions and month navigation expose distinct button semantics and localized action
  descriptions.
- Start, continue, completed, today, historical completion, and future-date states never rely only
  on color.
- Daily completion may reuse the success semantic role, but completion text and check state remain
  authoritative.
- Menu and completion content remain vertically scrollable when compact height or font scale
  requires it.
- Visible labels may adapt or wrap only within documented component bounds; accessibility
  descriptions preserve complete challenge and state copy.
- Calendar weekday order, first day of week, dates, month labels, and navigation respect locale
  and layout direction.
- Every user-facing label, state description, chooser title, and shared string comes from Android
  resources.
- Menu, calendar, and sharing actions do not emit assignment haptics.
- Quick and Daily accepted assignments, newly correct tiles, and player-caused completion use the
  existing preference- and system-aware generated feedback.
- Restoration, completed-summary reopening, calendar display, local-date rollover, and sharing do
  not replay haptics or completion motion.
- Reduced or disabled system motion reaches the same usable final state immediately.
- Cross-theme previews and focused Compose coverage protect the new states and semantic roles.

---

## Generated-Game Feedback

The generated feedback contract applies to normal Quick, generated `4 Pairs`, generated `8 Pairs`,
and Daily Challenge. Guided first run, voluntary `How to play`, Tutorial, authored practice, and
the generic game surface do not opt into it.

After a generated-game action commits an accepted strip value, operand, or operator:

- one subtle platform confirmation haptic is requested when the NumPairs preference is
  enabled
- Android may suppress the request when system touch feedback is disabled
- no haptic is emitted for opening or dismissing selectors, editing draft text, unavailable
  options, resets, restoration, recomposition, or rejected/error states

When that committed player action changes a tile from not correct to correct, that tile
performs one small scale response without changing its bounds, layout, or touch target. If
the tile later becomes incorrect and a new player action makes it correct again, the new
transition may respond again.

When the same commit solves the whole generated puzzle, the completion celebration described
above runs once. Sound and error haptics are not part of the current behavior. Motion is
never required to understand the result, does not block interaction, and reaches the same
final state when system animation duration is disabled.

---

## Game Top Bar

Gameplay screens should show:

- a back navigation action
- the current challenge title, such as `Quick · Low`, `4 Pairs · Low`, `8 Pairs · Hard`, or
  `Daily · <localized date>`; Tutorial retains its authored title
- an optional rules helper action

The rules helper action remains available in generated `4 Pairs`. v10 also makes it available in
Quick and Daily because both use the documented Low learning principles. It should not be shown in
Tutorial because Tutorial has its own guided instructional surface. It is intentionally not part
of the menu screen.

Normal and Daily session persistence do not add a new-puzzle, restart, resume, share, calendar, or
overflow action to the gameplay TopAppBar. Session and completion choices remain in the normal
menu and the applicable completion surface.

The existing Low-specific rules helper and solving tips may remain available in `4 Pairs Medium`
for v8. Aligning that learning content with Medium is a separate follow-up and does not alter the
challenge identity or generated puzzle rules.

### Rules Helper Behavior

- Tapping the rules helper action opens a modal dialog.
- The helper dialog explains core rules using concise, player-facing language.
- The helper is informational only and must not reveal puzzle-specific answers, hidden values, pairings, hints, or automated answer output.
- The helper can be closed by tapping a visible close icon.
- Tapping outside the dialog closes the helper.
- System back closes the helper before triggering the route-level back behavior.
- Closing the helper preserves the current puzzle state.

For content scope and product boundaries, see
[rules-helper.md](./product/rules-helper.md).

---

## Number Strip States

Each strip item has an origin state.

### Origin States

- **Hidden strip item**
  - Displayed as `?`
  - Indicates a number that has not yet been provided by the player

- **Known strip item**
  - Displayed as a known starting number from the puzzle
  - Uses an outlined style to distinguish it from player-entered strip items
  - Is not editable

- **Player-entered strip item**
  - Displayed as a number entered by the player
  - Uses a filled or tonal style to distinguish it from known strip items
  - Is editable

### Usage Indicator States

Visible strip entries should expose persistent operator-family usage indicators in the strip so players can scan number availability without opening the operand selector.

Usage indicators are computed per strip entry, not per numeric value. If the same number appears more than once, each repeated value keeps its own independent usage state.

Hidden strip items do not show operator-family usage indicators because they do not yet expose a selectable number.

The final strip treatment renders the two operator-family markers as compact `+` and `×` pills over the chip's top edge, not inside the numeric label area. This keeps the numeric value centered, preserves the chip tap target, and avoids changing chip height when usage state changes.

For each visible strip entry, the combined usage state is:

- **Unused**
  - Neither `+` nor `×` has been used for this strip entry
  - Both operator-family markers should read as available

- **Addition-used**
  - The strip entry is already used in an addition expression
  - The `+` marker should read as used
  - The `×` marker should read as available

- **Multiplication-used**
  - The strip entry is already used in a multiplication expression
  - The `×` marker should read as used
  - The `+` marker should read as available

- **Fully-used**
  - The strip entry is already used once in addition and once in multiplication
  - Both `+` and `×` markers should read as used

Operator-family usage indicators are informational. Assignment availability is still governed by the puzzle rules, including cases where a strip entry is temporarily assigned to a tile whose operator is still hidden.

The indicators must not rely on color alone. The symbols, border or fill treatment, opacity, and accessibility state descriptions should all help communicate used versus available states.

Used strip indicators use the player-owned focus blue treatment. Available strip indicators use the subtle surface treatment. Strip usage indicators must not show the red conflict treatment; live rule conflicts are handled by local tile feedback, the contextual conflict message, and operand selector badges.

The strip remains a narrow-screen constraint. Usage indicators must stay compact enough that visible strip values, including three-digit values, remain readable. When a chip would become too narrow, the strip uses fewer columns and balanced additional rows rather than compressing the label.

Implementation QA notes for the current strip indicator treatment:

- The strip layout uses at most eight columns, `4dp` horizontal gaps, bounded chip widths, and balanced rows. Sixteen entries render as two rows of eight when their labels fit.
- Increased font-scale behavior is handled by measuring the widest visible or editable label before choosing columns. Strip labels and indicators retain their shared `sp` text styles, and this remains subject to future device visual QA.

---

## Number Strip Interactions

### Hidden Strip Item

- Single tap on a `?` strip item opens the entry dialog
- After confirmation, the strip item changes from `Hidden` to `Player-entered`

### Known Strip Item

- A known strip item is displayed as information for the player
- Tapping a known strip item does not start a grid assignment flow

### Player-entered Strip Item

- Editing must not rely on double tap
- Single tap on a player-entered strip item reopens the entry dialog with the current value prefilled

### Strip Ordering

- The strip must remain visually sorted in ascending order
- When adjacent editable strip items form an editable run, player-entered values are kept in ascending order within that run
- Known strip items never move
- Hidden strip items remain hidden until they are filled
- Reordering is limited to player-entered values inside the affected editable run

---

## Result Grid States

The top row of each tile contains three editable positions:

1. left operand slot
2. operator slot
3. right operand slot

Each position may be shown in one of these states:

- **Hidden**
  - Displayed as `?`

- **Filled**
  - Displayed as the currently chosen number or operator

A tile as a whole may also be shown in one of these validation states:

- **Unresolved**
  - At least one operand or the operator is still hidden
  - The tile is not shown as invalid yet

- **Correct**
  - The expression is fully known and matches the tile result

- **Incorrect**
  - The expression is fully known but does not match the tile result
  - The tile remains editable so the player can correct it

---

## Result Grid Interactions

- Tapping a hidden operand slot opens a contextual selector for that slot
- Tapping a filled operand slot reopens the contextual selector so the value can be replaced
- Tapping a hidden operator slot opens a contextual selector for that slot
- Tapping a filled operator slot reopens the contextual selector so the value can be replaced
- No strip item selection step is required before editing a grid slot

This section defines local tile-level correctness feedback and operand/operator selection
behavior. It does not define whole-puzzle validation feedback, splash, menu, or
completion-routing behavior.

### Tile Validation Feedback

- A tile is shown as invalid only when its expression is fully known and does not match the tile result
- A partially-filled tile is not shown as invalid
- An invalid tile remains fully editable
- The invalid styling is removed as soon as the tile becomes correct again

---

## Contextual Selector Behavior

The contextual selector is the primary interaction used to edit the grid.

### Operand Slot Mode

- Opened by tapping a left or right operand slot
- Uses a compact bottom sheet rather than a confirmation dialog
- Shows the currently visible strip entries from the strip
- Repeated numeric values remain separate selectable options when they come from different strip entries
- Hidden strip items are not shown as selectable values
- Selecting a value immediately fills or replaces the operand slot and closes the sheet
- Closing the selector without choosing a value leaves the slot unchanged
- The selector does not specially highlight the currently assigned operand when reopened
- Each visible strip entry in the operand selector shows contextual `+` and `×` usage badges derived from the current board state
- Operand selector usage badges should remain richer and more legible than the persistent strip indicators because the player is actively choosing an operand in this context
- The persistent strip indicators and operand selector badges must use the same semantic meaning for `+`, `×`, used, and available
- A strip entry becomes unavailable once it is already assigned twice anywhere on the board, even if one or both assignments still belong to tiles whose operator is hidden
- Reopening a slot keeps that slot's current strip entry selectable for reassignment there, even if the entry is otherwise exhausted
- The `+` and `×` indicators remain informational; disabled options are driven by strip-entry availability rather than by the badges alone

Operand selection logic should treat strip entries as unique entities rather than grouping options only by numeric value. Operator-usage indicators in both the strip and operand selector must therefore be computed per strip entry.

### Operator Slot Mode

- Opened by tapping the operator slot
- Uses a small anchored contextual popup rather than a full modal dialog
- Shows exactly two options:
  - `+`
  - `x`
- Selecting an option immediately fills or replaces the operator slot and closes the popup
- Closing the selector without choosing a value leaves the slot unchanged

The operand selector should use a compact bottom sheet presentation without extra confirmation buttons.

The operator selector should appear anchored to the tapped slot, using a compact popover or bubble-style presentation without extra confirmation buttons.

---

## Entry Dialog Behavior

The entry dialog is used only for strip items, not for grid slot assignment.

### Create Mode

- Opened by tapping a `?` strip item
- Lets the player enter a number
- Confirming stores the number in that strip item

### Edit Mode

- Opened by tapping a player-entered strip item
- Shows the current value prefilled
- Confirming replaces the previous value with the new one
- After confirmation, player-entered values may be reordered inside the same editable run to preserve ascending order

---

## Visual Guidance

The strip should communicate four things at a glance:

1. Which strip items were known from the beginning
2. Which strip items were entered by the player
3. Which strip items are still hidden
4. Which visible strip entries have already been used for `+`, `×`, both, or neither

The grid should communicate two things at a glance:

1. Which slots are still hidden
2. Which slots already contain a chosen value

The grid should also communicate when a fully-known tile is currently incorrect.

The operand selector should communicate operator-family usage with minimal, low-noise signals rather than verbose per-option text.

Recommended visual direction for the first implementation:

- `Known strip item`: outlined chip
- `Player-entered strip item`: filled or tonal chip
- `Hidden strip item`: `?`
- Visible strip item usage: compact persistent `+` / `×` indicators
- Hidden grid slot: `?`
- Filled grid slot: chosen number or operator
- Active grid slot: temporary highlight while its contextual selector is open
- Operand selector option: number-first card with contextual `+` / `×` badges
- Incorrect tile: subtle error-tinted container
- Incorrect tile: error border
- Incorrect tile: expression row in error color
- Incorrect tile: result keeps its normal emphasis so the target value stays legible

Exact appearance colors vary by selected theme. Semantic state meanings, spacing, and shared
motion contracts remain stable.

---

## Out of Scope For This Version

The following behaviors are intentionally left for future tickets:

- Difficulty changes during an active generated puzzle
- Difficulty locks, player progression, normal generated completion tracking, rewards, and
  statistics
- Daily streaks, scores, timers, rankings, achievements, reminders, and notifications
- Playing, sharing, or backfilling past Daily Challenge dates
- Player-selected or rotating Daily difficulty
- Revising Low-specific solving tips for `4 Pairs Medium`
- Advanced validation while entering or selecting values
- Context-aware filtering of operand slot options
- Automatic prevention of invalid operator or operand choices
- Drag and drop interactions
- Detailed rules for clearing filled grid slots
