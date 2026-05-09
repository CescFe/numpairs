package org.cescfe.numpairs.domain.puzzle.support

import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile

data class TileAssignmentSpec(
    val leftEntryId: Int,
    val operator: Operator,
    val rightEntryId: Int,
    val result: Int? = null
)

fun tileAssignment(leftEntryId: Int, operator: Operator, rightEntryId: Int, result: Int? = null): TileAssignmentSpec =
    TileAssignmentSpec(
        leftEntryId = leftEntryId,
        operator = operator,
        rightEntryId = rightEntryId,
        result = result
    )

fun hiddenTile(result: Int): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Hidden,
        operator = Operator.Hidden,
        rightOperand = Expression.Operand.Hidden
    ),
    result = result
)

fun additionTile(leftOperand: Int, rightOperand: Int, result: Int = leftOperand + rightOperand): Tile = Tile(
    expression = Expression(
        leftOperand = leftOperand,
        operator = Operator.ADDITION,
        rightOperand = rightOperand
    ),
    result = result
)

fun assignedTile(
    leftEntryId: Int,
    leftValue: Int,
    operator: Operator,
    rightEntryId: Int,
    rightValue: Int,
    result: Int? = null
): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Known(
            value = leftValue,
            stripEntryId = leftEntryId
        ),
        operator = operator,
        rightOperand = Expression.Operand.Known(
            value = rightValue,
            stripEntryId = rightEntryId
        )
    ),
    result = result ?: operator.apply(leftOperand = leftValue, rightOperand = rightValue)
)

fun assignedTile(
    stripValues: List<Int>,
    leftEntryId: Int,
    operator: Operator,
    rightEntryId: Int,
    result: Int? = null
): Tile = assignedTile(
    leftEntryId = leftEntryId,
    leftValue = stripValues[leftEntryId],
    operator = operator,
    rightEntryId = rightEntryId,
    rightValue = stripValues[rightEntryId],
    result = result
)

fun tileWithoutStripIdentity(
    leftOperand: Int,
    operator: Operator,
    rightOperand: Int,
    result: Int = operator.apply(leftOperand = leftOperand, rightOperand = rightOperand)
): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Known(value = leftOperand),
        operator = operator,
        rightOperand = Expression.Operand.Known(value = rightOperand)
    ),
    result = result
)

fun boardOf(vararg tiles: Tile): Board = Board(tiles = tiles.toList())

fun stripOf(vararg items: StripItem): Strip = Strip.fromItems(items = items.toList())

fun knownStrip(values: List<Int>): Strip = Strip.fromItems(items = values.map(StripItem::Known))

fun knownPuzzleWithAssignments(stripValues: List<Int>, vararg tileAssignments: TileAssignmentSpec): Puzzle = Puzzle(
    board = boardOf(
        *tileAssignments.map { assignment ->
            assignedTile(
                stripValues = stripValues,
                leftEntryId = assignment.leftEntryId,
                operator = assignment.operator,
                rightEntryId = assignment.rightEntryId,
                result = assignment.result
            )
        }.toTypedArray()
    ),
    strip = knownStrip(values = stripValues)
)

fun defaultKnownStripValues(): List<Int> = (1..Strip.NUMBER_COUNT).toList()

fun Puzzle.withTile(index: Int, tile: Tile): Puzzle = copy(
    board = Board(
        tiles = board.tiles.toMutableList().apply {
            set(index, tile)
        }
    )
)
