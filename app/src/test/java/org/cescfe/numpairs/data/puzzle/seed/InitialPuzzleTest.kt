package org.cescfe.numpairs.data.puzzle.seed

import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.junit.Assert
import org.junit.Test

class InitialPuzzleTest {
    @Test
    fun initial_strip_starts_with_known_and_hidden_items() {
        val stripItems = initialPuzzle.strip.items

        Assert.assertEquals(5, stripItems.count { it == StripItem.Hidden })
        Assert.assertEquals(3, stripItems.count { it is StripItem.Known })
        Assert.assertFalse(stripItems.any { it is StripItem.PlayerEntered })
    }

    @Test
    fun initial_tiles_start_with_hidden_expressions() {
        val boardTiles = initialPuzzle.board.tiles

        boardTiles.forEach { tile ->
            Assert.assertEquals(Expression.Operand.Hidden, tile.expression.leftOperand)
            Assert.assertEquals(Operator.Hidden, tile.expression.operator)
            Assert.assertEquals(Expression.Operand.Hidden, tile.expression.rightOperand)
        }
    }

    @Test
    fun initial_tiles_keep_their_expected_results() {
        val boardTiles = initialPuzzle.board.tiles

        Assert.assertEquals(
            listOf(223, 222, 52, 100, 31, 150, 35, 250),
            boardTiles.map { it.result })
    }
}