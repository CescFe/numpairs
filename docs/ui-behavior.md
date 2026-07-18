# NumPairs UI Behavior

## Overview

This document defines the current menu, generated-session routing, completion, and in-puzzle interaction model for NumPairs.

It complements the game rules described in [game-rules.md](./game-rules.md) and focuses on:

1. Number strip behavior
2. Result grid behavior
3. Contextual editing flows
4. Gameplay top bar helper behavior
5. Generated-session menu, replacement, and completion behavior

This is the interaction baseline shared where applicable by Tutorial, generated `4 Pairs`, and generated `8 Pairs` gameplay.

Required-onboarding behavior is documented in `docs/product/prd/prd-v6.md`. The reliable-session product contract is documented in `docs/product/prd/prd-v7.md`, and its storage boundary is documented in `docs/technical/generated-session-persistence.md`.

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

## Normal Menu And Generated Session Routing

The unlocked normal menu renders actions in this order:

1. `Resume`, only while one valid unfinished generated session is available
2. `Play 4 Pairs`
3. `Play 8 Pairs`
4. `How to play`

`Resume` and both generated-mode actions use the primary CTA treatment. `How to play` remains the final secondary action. The localized `Resume` accessibility description identifies whether the saved puzzle belongs to 4 or 8 Pairs.

The application derives menu resumability from the one global generated-session slot. Missing, solved, unknown-mode, mode/profile-mismatched, corrupt, and unsupported snapshots do not expose `Resume`.

Selecting `Resume` opens the saved mode and exact current puzzle without generation.

Selecting either generated-mode action while a resumable session exists opens the same modal choice:

- primary: `Resume`
- secondary: start a new puzzle for the mode the player selected

Both same-mode and different-mode selections use one concise supporting message: `You have an unfinished <saved mode> puzzle.` The message uses the larger body text treatment. The secondary label always identifies the requested replacement mode as `New <selected mode>`.

The primary action uses the shared primary CTA treatment and established button shape.

The choice dialog has no visible cancel, back, close, or third action. Tapping outside or pressing system back dismisses it without navigation, generation, or session mutation. Action handling is deduplicated.

Selecting `How to play`, entering required onboarding, or using Tutorial never replaces or updates the generated session.

### Generated Completion And Replay

A solved generated puzzle shows exactly:

- primary: `Play another`
- secondary: `Back to menu`

`Play another` runs the existing bounded generation and safe-replacement pipeline. The solved puzzle remains visible while its successor is pending. Failure or cancellation keeps the completion surface available; a successfully stored successor replaces it.

Solving clears menu resumability before `Back to menu` returns to the menu. There is no `Change difficulty`, restart, timer, or additional completion action.

---

## Game Top Bar

Gameplay screens should show:

- a back navigation action
- the current mode title
- an optional rules helper action

The rules helper action should be available in generated `4 Pairs` for v3. It should not be shown in Tutorial because Tutorial has its own guided instructional surface. It is intentionally not part of the menu screen in the first implementation.

Reliable sessions do not add a new-puzzle, restart, resume, or overflow action to the gameplay TopAppBar. Session choices remain in the normal menu and completion surface.

### Rules Helper Behavior

- Tapping the rules helper action opens a modal dialog.
- The helper dialog explains core rules using concise, player-facing language.
- The helper is informational only and must not reveal puzzle-specific answers, hidden values, pairings, hints, or automated answer output.
- The helper can be closed by tapping a visible close icon.
- Tapping outside the dialog closes the helper.
- System back closes the helper before triggering the route-level back behavior.
- Closing the helper preserves the current puzzle state.

For content scope and product boundaries, see `docs/product/rules-helper.md`.

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

This document defines local tile-level correctness feedback and operand/operator selection behavior. It does not define whole-puzzle validation feedback, splash, menu, or completion-routing behavior.

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

Exact colors, spacing, and animation can be refined later.

---

## Out of Scope For This Version

The following behaviors are intentionally left for future tickets:

- Advanced validation while entering or selecting values
- Context-aware filtering of operand slot options
- Automatic prevention of invalid operator or operand choices
- Drag and drop interactions
- Detailed rules for clearing filled grid slots
