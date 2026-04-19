# Num Pairs Puzzle - UI Behavior

## Overview

This document defines the first interaction model for the Num Pairs UI.

It complements the game rules described in [game-rules.md](./game-rules.md) and focuses on:

1. Number strip behavior
2. Result grid behavior
3. Editing and selection flows

This is intended as a first implementation target. It favors clear and discoverable mobile interactions over advanced validation or optimization.

---

## Terminology

- **Strip item**: one position in the number strip
- **Operand slot**: one editable operand position in the top row of a tile
- **Entry dialog**: the dialog used to enter or edit a number

In this document, strip items are rendered as chips.

---

## Interaction Principles

- Mobile-first behavior: interactions must not depend on hover
- Single tap is the primary gesture
- Editing must be explicit and discoverable
- Visual states must clearly distinguish origin, selection, and usage

---

## Number Strip States

Each strip item has an origin state and may also have an interaction state.

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

### Interaction States

- **Selected**
  - The strip item is currently active and ready to be assigned to an operand slot
  - Uses a clear highlight in addition to its origin style

- **Assigned**
  - The strip item has already been used in at least one grid assignment
  - Uses a secondary visual treatment to show it has been used without hiding its value

Only one strip item can be selected at a time.

---

## Number Strip Interactions

### Hidden Strip Item

- Single tap on a `?` strip item opens the entry dialog
- After confirmation, the strip item changes from `Hidden` to `Player-entered`

### Defined Strip Item

A defined strip item is any strip item that currently shows a number, whether `Known` or `Player-entered`.

- Single tap on a defined strip item selects it
- Single tap on the currently selected strip item deselects it
- Single tap on another defined strip item moves the selection to that item

### Edit Flow

Editing must not rely on double tap.

- When a `Player-entered` strip item is selected, the UI must expose an explicit `Edit` action
- Triggering `Edit` reopens the entry dialog with the current value prefilled
- `Known` strip items do not expose the `Edit` action

---

## Result Grid Interactions

The top row of each tile contains editable operand slots.

- If the player taps an empty operand slot while a strip item is selected, the selected number is assigned to that slot
- If the player taps an operand slot with no selected strip item, no assignment is made
- After assignment, the strip item may remain selected so the player can continue assigning it if needed

This document defines only the basic assignment gesture. It does not define advanced assignment constraints yet.

---

## Entry Dialog Behavior

The same dialog is used for both creation and editing.

### Create Mode

- Opened by tapping a `?` strip item
- Lets the player enter a number
- Confirming stores the number in that strip item

### Edit Mode

- Opened through the explicit `Edit` action on a selected `Player-entered` strip item
- Shows the current value prefilled
- Confirming replaces the previous value with the new one

---

## Visual Guidance

The strip should communicate three things at a glance:

1. Which strip items were known from the beginning
2. Which strip items were entered by the player
3. Which strip item is currently selected for assignment

Recommended visual direction for the first implementation:

- `Known strip item`: outlined chip
- `Player-entered strip item`: filled or tonal chip
- `Selected`: stronger highlight layered on top of the strip item style
- `Assigned`: subtle used state that does not remove readability

Exact colors, spacing, and animation can be refined later.

---

## Out of Scope For This Version

The following behaviors are intentionally left for future tickets:

- Advanced validation while entering a number
- Context-aware value suggestions
- Alternative gestures beyond the primary tap flow
- Detailed rules for replacing or removing grid assignments
