# Num Pairs Puzzle - UI Behavior

## Overview

This document defines the current interaction model for the Num Pairs UI.

It complements the game rules described in [game-rules.md](./game-rules.md) and focuses on:

1. Number strip behavior
2. Result grid behavior
3. Contextual editing flows

This is intended as an implementation target for the prototype. It favors clear and discoverable mobile interactions over advanced validation or optimization.

---

## Terminology

- **Strip item**: one position in the number strip
- **Editable run**: a contiguous strip segment made only of hidden and player-entered items, delimited by known items or strip edges
- **Operand slot**: one editable operand position in the top row of a tile
- **Operator slot**: the editable operator position in the top row of a tile
- **Contextual selector**: a small anchored popover or bubble used to choose a value for a grid slot
- **Entry dialog**: the dialog used to enter or edit a number in the strip

In this document, strip items are rendered as chips.

---

## Interaction Principles

- Mobile-first behavior: interactions must not depend on hover
- Single tap is the primary gesture
- Grid editing happens directly from the tapped slot
- No prior strip selection is required to edit the grid
- Slot editing should use a small contextual selector instead of a full-screen flow whenever possible

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

This document defines only local tile-level correctness feedback. It does not define advanced prevention, hints, or whole-puzzle validation rules.

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
- Shows the currently available numbers from the strip
- Repeated numeric values remain separate selectable options when they come from different strip entries
- Hidden strip items are not shown as selectable values
- Selecting a value immediately fills or replaces the operand slot and closes the sheet
- Closing the selector without choosing a value leaves the slot unchanged

Selector logic should treat strip entries as unique entities rather than grouping options only by numeric value. Any future operand-usage hinting must therefore be computed per strip entry.

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

The strip should communicate three things at a glance:

1. Which strip items were known from the beginning
2. Which strip items were entered by the player
3. Which strip items are still hidden

The grid should communicate two things at a glance:

1. Which slots are still hidden
2. Which slots already contain a chosen value

The grid should also communicate when a fully-known tile is currently incorrect.

Recommended visual direction for the first implementation:

- `Known strip item`: outlined chip
- `Player-entered strip item`: filled or tonal chip
- `Hidden strip item`: `?`
- Hidden grid slot: `?`
- Filled grid slot: chosen number or operator
- Active grid slot: temporary highlight while its contextual selector is open
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
