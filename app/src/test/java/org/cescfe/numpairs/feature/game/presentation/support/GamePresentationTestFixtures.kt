package org.cescfe.numpairs.feature.game.presentation.support

import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.domain.puzzle.support.TileAssignment
import org.cescfe.numpairs.domain.puzzle.support.assignedTile
import org.cescfe.numpairs.domain.puzzle.support.defaultKnownStripValues
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.cescfe.numpairs.feature.game.presentation.GameViewModel

fun solvedTileAssignments(): List<TileAssignment> = listOf(
    TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
    TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
    TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
    TileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2),
    TileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
    TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
    TileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
    TileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
)

fun solvedPuzzleWithKnownStripAndAssignments(): Puzzle = knownPuzzleWithAssignments(
    stripValues = defaultKnownStripValues(),
    *solvedTileAssignments().toTypedArray()
)

fun incompletePuzzleOneOperatorSelectionAwayFromMismatchedCompletion(): Puzzle = Puzzle(
    board = Board(
        tiles = listOf(
            assignedTile(
                leftEntryId = 0,
                leftValue = 1,
                operator = Operator.ADDITION,
                rightEntryId = 1,
                rightValue = 2
            ),
            tileWithHiddenOperator(resultOperator = Operator.MULTIPLICATION),
            assignedTile(
                leftEntryId = 2,
                leftValue = 3,
                operator = Operator.ADDITION,
                rightEntryId = 3,
                rightValue = 4
            ),
            assignedTile(
                leftEntryId = 1,
                leftValue = 2,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 3,
                rightValue = 4
            ),
            assignedTile(
                leftEntryId = 4,
                leftValue = 5,
                operator = Operator.ADDITION,
                rightEntryId = 5,
                rightValue = 6
            ),
            assignedTile(
                leftEntryId = 5,
                leftValue = 6,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 4,
                rightValue = 5
            ),
            assignedTile(
                leftEntryId = 6,
                leftValue = 7,
                operator = Operator.ADDITION,
                rightEntryId = 7,
                rightValue = 8
            ),
            assignedTile(
                leftEntryId = 7,
                leftValue = 8,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 6,
                rightValue = 7
            )
        )
    ),
    strip = Strip.fromItems(items = defaultKnownStripValues().map(StripItem::Known))
)

fun incompletePuzzleOneOperatorSelectionAwayFromSolvedCompletion(): Puzzle = Puzzle(
    board = Board(
        tiles = listOf(
            assignedTile(
                leftEntryId = 0,
                leftValue = 1,
                operator = Operator.ADDITION,
                rightEntryId = 1,
                rightValue = 2
            ),
            tileWithHiddenOperator(
                resultOperator = Operator.MULTIPLICATION,
                leftStripEntryId = 1,
                rightStripEntryId = 0
            ),
            assignedTile(
                leftEntryId = 2,
                leftValue = 3,
                operator = Operator.ADDITION,
                rightEntryId = 3,
                rightValue = 4
            ),
            assignedTile(
                leftEntryId = 3,
                leftValue = 4,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 2,
                rightValue = 3
            ),
            assignedTile(
                leftEntryId = 4,
                leftValue = 5,
                operator = Operator.ADDITION,
                rightEntryId = 5,
                rightValue = 6
            ),
            assignedTile(
                leftEntryId = 5,
                leftValue = 6,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 4,
                rightValue = 5
            ),
            assignedTile(
                leftEntryId = 6,
                leftValue = 7,
                operator = Operator.ADDITION,
                rightEntryId = 7,
                rightValue = 8
            ),
            assignedTile(
                leftEntryId = 7,
                leftValue = 8,
                operator = Operator.MULTIPLICATION,
                rightEntryId = 6,
                rightValue = 7
            )
        )
    ),
    strip = Strip.fromItems(items = defaultKnownStripValues().map(StripItem::Known))
)

fun GameViewModel.enterStripValue(index: Int, value: String) {
    onStripItemTapped(index = index)
    onStripItemEntryInputChanged(draftText = value)
    onStripItemEntryInputConfirmed()
}

private fun tileWithHiddenOperator(
    resultOperator: Operator,
    leftStripEntryId: Int = 0,
    rightStripEntryId: Int = 2
): Tile {
    val leftValue = leftStripEntryId + 1
    val rightValue = rightStripEntryId + 1

    return Tile(
        expression = Expression(
            leftOperand = Expression.Operand.Known(
                value = leftValue,
                stripEntryId = leftStripEntryId
            ),
            operator = Operator.Hidden,
            rightOperand = Expression.Operand.Known(
                value = rightValue,
                stripEntryId = rightStripEntryId
            )
        ),
        result = resultOperator.apply(leftValue, rightValue)
    )
}
