package org.cescfe.numpairs

import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class InitialPuzzleTest {
    @Test
    fun initial_strip_starts_with_known_and_hidden_items() {
        val stripItems = initialPuzzle.strip.items

        assertEquals(5, stripItems.count { it == StripItem.Hidden })
        assertEquals(3, stripItems.count { it is StripItem.Known })
        assertFalse(stripItems.any { it is StripItem.PlayerEntered })
    }

    @Test
    fun initial_tiles_start_with_hidden_expressions() {
        val boardTiles = initialPuzzle.board.tiles

        boardTiles.forEach { tile ->
            assertEquals(Expression.Operand.Hidden, tile.expression.leftOperand)
            assertEquals(Operator.Hidden, tile.expression.operator)
            assertEquals(Expression.Operand.Hidden, tile.expression.rightOperand)
        }
    }

    @Test
    fun initial_tiles_keep_their_expected_results() {
        val boardTiles = initialPuzzle.board.tiles

        assertEquals(listOf(223, 222, 52, 100, 31, 150, 35, 250), boardTiles.map { it.result })
    }
}
