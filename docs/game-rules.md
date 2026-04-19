# Num Pairs Puzzle - Game Rules

## Overview

The game consists of two main areas:

1. **Number Strip**
2. **Result Grid**

The player must deduce hidden numbers and match them correctly with the grid tiles.

---

## Number Strip

The number strip is a sequence of positive integers with the following properties:

- Numbers are displayed in **ascending order**
- Repeated values are allowed
- Some numbers may be hidden
- Hidden numbers must be deduced by the player

Example:

1, 2, ?, 4, 4, ?, 7, 9

---

## Result Grid

The grid contains 8 tiles.

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
