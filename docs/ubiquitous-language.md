# Ubiquitous Language

## Puzzle
A complete game instance containing a board and a strip of available numbers.

## Board
The main area where the puzzle tiles are displayed.

## Grid
The row and column arrangement of the board.

## Tile
A single puzzle unit displayed on the board.

A tile is composed of:
- top row:
    - left operand
    - operator
    - right operand
- bottom row:
    - result

## Tile Resolution State
The validation state of a tile based on how complete its expression is and whether it matches the tile result.

A tile may be:
- unresolved
- correct
- incorrect

## Unresolved Tile
A tile whose expression is not fully known yet because at least one operand or the operator is still hidden.

## Correct Tile
A tile whose expression is fully known and whose evaluated value matches the tile result.

## Incorrect Tile
A tile whose expression is fully known but whose evaluated value does not match the tile result.

An incorrect tile is a valid gameplay state that the player can later correct.

## Expression
The arithmetic expression formed by two operands and one operator.

## Operator
The arithmetic operation used in a tile expression.

Supported operators:
- addition
- multiplication

## Strip
The horizontal area containing the available numbers for the puzzle.

## Strip Item
A single position within the strip.

A strip item may be:
- hidden
- known from the start of the puzzle
- completed by the player during play

## Hidden Strip Item
A strip item whose value is not visible to the player at the beginning of the puzzle.

## Known Strip Item
A strip item whose value is visible to the player at the beginning of the puzzle.

## Player-entered Strip Item
A strip item whose value was entered by the player after starting from a hidden state.

## Available Number
A number contained in a strip item that can be used in tile expressions.

## Operand Slot
One editable operand position in the top row of a tile expression.

A tile has:
- left operand slot
- right operand slot

## Solution
The correct assignment of operands and operators needed to solve the puzzle.
