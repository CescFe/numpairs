package org.cescfe.numpairs.domain.puzzle

object PuzzleSamples {
    val prototype: Puzzle = Puzzle(
        board = Board(
            tiles = listOf(
                hiddenExpressionTile(result = 3),
                hiddenExpressionTile(result = 6),
                hiddenExpressionTile(result = 7),
                hiddenExpressionTile(result = 20),
                hiddenExpressionTile(result = 6),
                hiddenExpressionTile(result = 6),
                hiddenExpressionTile(result = 9),
                hiddenExpressionTile(result = 8)
            )
        ),
        strip = Strip(
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
}

private fun hiddenExpressionTile(result: Int): Tile = Tile(
    expression = Expression(
        leftOperand = Expression.Operand.Hidden,
        operator = Operator.Hidden,
        rightOperand = Expression.Operand.Hidden
    ),
    result = result
)
