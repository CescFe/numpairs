package org.cescfe.numpairs.domain.puzzle

object PuzzleSamples {
    val prototype: Puzzle = Puzzle(
        board = Board(
            tiles = listOf(
                Tile(
                    expression = Expression(
                        leftOperand = 1,
                        operator = Operator.ADDITION,
                        rightOperand = 2
                    ),
                    result = 3
                ),
                Tile(
                    expression = Expression(
                        leftOperand = 2,
                        operator = Operator.MULTIPLICATION,
                        rightOperand = 3
                    ),
                    result = 6
                ),
                Tile(
                    expression = Expression(
                        leftOperand = 3,
                        operator = Operator.ADDITION,
                        rightOperand = 4
                    ),
                    result = 7
                ),
                Tile(
                    expression = Expression(
                        leftOperand = 4,
                        operator = Operator.MULTIPLICATION,
                        rightOperand = 5
                    ),
                    result = 20
                ),
                Tile(
                    expression = Expression(
                        leftOperand = 5,
                        operator = Operator.ADDITION,
                        rightOperand = 1
                    ),
                    result = 6
                ),
                Tile(
                    expression = Expression(
                        leftOperand = 6,
                        operator = Operator.MULTIPLICATION,
                        rightOperand = 1
                    ),
                    result = 6
                ),
                Tile(
                    expression = Expression(
                        leftOperand = 7,
                        operator = Operator.ADDITION,
                        rightOperand = 2
                    ),
                    result = 9
                ),
                Tile(
                    expression = Expression(
                        leftOperand = 8,
                        operator = Operator.MULTIPLICATION,
                        rightOperand = 1
                    ),
                    result = 8
                )
            )
        ),
        strip = Strip(
            items = listOf(
                StripItem.Known(1),
                StripItem.Known(2),
                StripItem.Known(3),
                StripItem.Known(4),
                StripItem.Known(25),
                StripItem.Known(6),
                StripItem.Known(7),
                StripItem.Known(888)
            )
        )
    )
}
