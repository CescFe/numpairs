package org.cescfe.numpairs

import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile

val initialPuzzle: Puzzle = Puzzle(
    board = Board(
        tiles = listOf(
            hiddenExpressionTile(result = 223),
            hiddenExpressionTile(result = 222),
            hiddenExpressionTile(result = 52),
            hiddenExpressionTile(result = 100),
            hiddenExpressionTile(result = 31),
            hiddenExpressionTile(result = 150),
            hiddenExpressionTile(result = 35),
            hiddenExpressionTile(result = 250)
        )
    ),
    strip = Strip.fromItems(
        items = listOf(
            StripItem.Hidden,
            StripItem.Hidden,
            StripItem.Known(6),
            StripItem.Hidden,
            StripItem.Known(25),
            StripItem.Hidden,
            StripItem.Hidden,
            StripItem.Known(222)
        )
    )
)

private fun hiddenExpressionTile(result: Int): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Hidden,
        operator = Operator.Hidden,
        rightOperand = Expression.Operand.Hidden
    ),
    result = result
)
