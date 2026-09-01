# NumPairs Game Rules

## Overview

The game consists of two main areas:

1. **Number Strip**
2. **Result Grid**

The player must deduce hidden numbers and match them correctly with the grid tiles.

---

## Number Strip

The number strip is a sequence of positive integers with the following properties:

- Numbers are displayed in **ascending order**
- The ascending order must still be preserved after player-entered values are added or edited
- Repeated values are allowed
- Each strip entry is a unique game element even when two entries display the same numeric value
- Some numbers may be hidden
- Hidden numbers must be deduced by the player

For current behavior, this means known strip items stay fixed while player-entered values may be reordered within adjacent editable positions so the strip remains sorted.

A player-entered strip item may be cleared, returning the same strip entry to a hidden state. Every tile operand that references that strip entry becomes hidden again, while the rest of each affected tile remains unchanged.

Example:

1, 2, ?, 4, 4, ?, 7, 9

---

## Result Grid

The number of tiles in the grid depends on the puzzle size.

Each solution pair contributes exactly two tiles:

- one addition tile
- one multiplication tile

Supported generated challenges use:

- `3 Pairs Low` and `3 Pairs Medium`: 3 solution pairs and 6 board tiles
- `4 Pairs Low` and `4 Pairs Medium`: 4 solution pairs and 8 board tiles
- `8 Pairs Medium` and `8 Pairs Hard`: 8 solution pairs and 16 board tiles

The player-facing generated play options are:

- `Quick Low` and `Quick Medium`: 35% of new puzzles use the matching 3 Pairs challenge and
  65% use the matching 4 Pairs challenge
- `Classic Medium` and `Classic Hard`: use the matching 8 Pairs challenge

Quick and Classic change puzzle duration, size selection, and difficulty profile, not the pairing,
operator, usage, or completion rules. Daily Challenge uses this deterministic weekly schedule:

- Monday and Thursday: `3 Pairs Low`
- Tuesday, Friday, and Saturday: `4 Pairs Low`
- Wednesday: `3 Pairs Medium`
- Sunday: `4 Pairs Medium`

The schedule selects one exact generated challenge from the captured device-local date. It does
not use Quick weighting or remembered difficulty.

Each tile has two rows:

### Top Row (Unknown Expression)

Represents an operation composed of:

- left operand
- operator symbol
- right operand

These values are initially unknown and must be deduced.

Example:

? ? ?

---

### Bottom Row (Known Result)

The result of the operation is visible to the player from the beginning.

Example:

12

---

## Core Rule

The numbers from the strip must be grouped into pairs.

Each pair of numbers generates:

- one **sum**
- one **product**

These values correspond to two results present in the grid.

Usage is tracked per strip entry, not per numeric value. In other words, if the strip contains two visible `25` values, they are still two distinct usable entries.

For operand selection and validation rules, each strip entry may be consumed:

- once in an addition expression
- once in a multiplication expression

Example:

Pair: (2, 3)

- Sum = 5
- Product = 6

Therefore, two grid tiles must contain results **5** and **6**.

---

## Objective

The player wins when:

- All hidden strip numbers are correctly deduced
- All numbers are correctly paired
- All grid operations are correctly resolved

## Daily Challenge Movements

Daily Challenge records one movement for each effective player-driven mutation of the durable
puzzle state. These mutations include:

- committing or clearing one player-entered strip value
- assigning or changing one operand
- assigning or changing one operator
- resetting one non-pristine tile

A durable mutation counts once even when its UI interaction has several transient steps. If one
interaction commits two distinct durable mutations, each mutation counts separately. Invalid
input, confirming an unchanged value, resetting an already pristine tile, opening or closing a
selector, navigation, backgrounding, device locking, configuration changes, and process
recreation do not count.

The movement count starts at zero for a newly created Daily Session and is recorded with its
Current Puzzle. Historical sessions and completions created before movement tracking keep the
count unknown because their earlier actions cannot be reconstructed from puzzle state.
