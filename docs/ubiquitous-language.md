# Ubiquitous Language

## Puzzle
A complete game instance containing a board and a strip of available numbers.

## Generated Puzzle
A puzzle produced by a generator rather than authored by hand.

Generated puzzles are used by the `4 Pairs` mode.

## Initial Puzzle
The player-facing puzzle state shown at the start of play.

For generated `4 Pairs`, the initial puzzle is derived from a solved puzzle by hiding tile expressions and masking selected strip entries.

## Solved Puzzle
The fully resolved puzzle used internally as the source of truth for generation and validation.

A solved puzzle is not exposed directly to the player.

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

## Strip Entry
A unique game element within the strip.

A strip entry has:
- a stable identity
- a current strip item

Two strip entries may display the same numeric value and still remain distinct game elements.

## Strip Entry Identity
The stable identity attached to a strip entry across strip reordering.

Tile operands reference strip entry identity rather than only a raw numeric value or a visual position.

## Strip Item
A strip value state stored in a strip entry and displayed in one strip position.

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
A UI-facing shorthand for the visible numeric value exposed by a strip entry.

Domain rules operate on strip entries and strip entry identity, not on raw numbers alone.

## Visible Strip Entry
A strip entry whose current strip item exposes a numeric value to the player.

Visible strip entries can participate in operand selection.

## Operand Slot
One editable operand position in the top row of a tile expression.

A tile has:
- left operand slot
- right operand slot

## Operand Selection Choice
A domain representation of one selectable strip entry for a specific operand slot.

An operand selection choice includes:
- the strip entry identity
- the currently visible value
- operator-specific usage state
- current availability for selection

## Operand Selection Availability
The current selection status of a strip entry for a specific operand slot.

An operand selection choice may be:
- available
- exhausted
- blocked by the opposite operand already assigned in the same tile

## Solution
The correct assignment of operands and operators needed to solve the puzzle.

## Solution Pair
An unordered pair of strip entries that produces one addition tile and one multiplication tile in `4 Pairs`.

The addition and multiplication tiles for a solution pair must reference the same two strip entry identities.

## Puzzle Generator
A domain service that creates generated puzzles.

The v2 `4 Pairs` generator builds a solved puzzle first, derives the initial puzzle from it, and validates the generated result before it can be shown to the player.

## Puzzle Validator
A domain service or validation rule set that checks whether a puzzle is internally consistent and satisfies the expected generation constraints.

## Solver
An internal domain service used for validation and confidence in generated or handcrafted puzzles.

The solver is not a player-facing feature. It does not imply hints, solution reveal, or guaranteed unique solutions.

## Rules Helper
An informational gameplay UI surface that explains the core NumPairs rules.

The rules helper is not a hint system, tutorial engine, solver, or answer reveal feature.
