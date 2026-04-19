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
