package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PuzzleSamplesTest {
    @Test
    fun prototype_strip_starts_with_known_and_hidden_items() {
        val stripItems = PuzzleSamples.prototype.strip.items

        assertEquals(3, stripItems.count { it == StripItem.Hidden })
        assertEquals(5, stripItems.count { it is StripItem.Known })
        assertFalse(stripItems.any { it is StripItem.PlayerEntered })
    }

    @Test
    fun prototype_tiles_start_with_hidden_expressions() {
        val boardTiles = PuzzleSamples.prototype.board.tiles

        boardTiles.forEach { tile ->
            assertEquals(Expression.Operand.Hidden, tile.expression.leftOperand)
            assertEquals(Operator.Hidden, tile.expression.operator)
            assertEquals(Expression.Operand.Hidden, tile.expression.rightOperand)
        }
    }

    @Test
    fun prototype_tiles_keep_their_expected_results() {
        val boardTiles = PuzzleSamples.prototype.board.tiles

        assertEquals(listOf(3, 6, 7, 20, 6, 6, 9, 8), boardTiles.map { it.result })
    }
}
