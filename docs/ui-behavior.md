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
- A player-entered strip item may expose an explicit `Edit` action
- Triggering `Edit` reopens the entry dialog with the current value prefilled

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

---

## Result Grid Interactions

- Tapping a hidden operand slot opens a contextual selector for that slot
- Tapping a filled operand slot reopens the contextual selector so the value can be replaced
- Tapping a hidden operator slot opens a contextual selector for that slot
- Tapping a filled operator slot reopens the contextual selector so the value can be replaced
- No strip item selection step is required before editing a grid slot

This document defines only the basic editing gesture. It does not define advanced validation or correctness rules yet.

---

## Contextual Selector Behavior

The contextual selector is the primary interaction used to edit the grid.

### Operand Slot Mode

- Opened by tapping a left or right operand slot
- Shows the currently available numbers from the strip
- Hidden strip items are not shown as selectable values
- Selecting a value fills or replaces the operand slot
- Closing the selector without choosing a value leaves the slot unchanged

### Operator Slot Mode

- Opened by tapping the operator slot
- Shows exactly two options:
  - `+`
  - `x`
- Selecting an option fills or replaces the operator slot
- Closing the selector without choosing a value leaves the slot unchanged

The selector should appear anchored to the tapped slot, using a compact popover or bubble-style presentation.

---

## Entry Dialog Behavior

The entry dialog is used only for strip items, not for grid slot assignment.

### Create Mode

- Opened by tapping a `?` strip item
- Lets the player enter a number
- Confirming stores the number in that strip item

### Edit Mode

- Opened through the explicit `Edit` action on a player-entered strip item
- Shows the current value prefilled
- Confirming replaces the previous value with the new one

---

## Visual Guidance

The strip should communicate three things at a glance:

1. Which strip items were known from the beginning
2. Which strip items were entered by the player
3. Which strip items are still hidden

The grid should communicate two things at a glance:

1. Which slots are still hidden
2. Which slots already contain a chosen value

Recommended visual direction for the first implementation:

- `Known strip item`: outlined chip
- `Player-entered strip item`: filled or tonal chip
- `Hidden strip item`: `?`
- Hidden grid slot: `?`
- Filled grid slot: chosen number or operator
- Active grid slot: temporary highlight while its contextual selector is open

Exact colors, spacing, and animation can be refined later.

---

## Out of Scope For This Version

The following behaviors are intentionally left for future tickets:

- Advanced validation while entering or selecting values
- Context-aware filtering of operand slot options
- Automatic prevention of invalid operator or operand choices
- Drag and drop interactions
- Detailed rules for clearing filled grid slots
