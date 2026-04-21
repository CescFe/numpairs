package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleSamplesTest {
    @Test
    fun prototype_matches_board_and_strip_shape() {
        val prototype = PuzzleSamples.prototype

        assertEquals(Board.TILE_COUNT, prototype.board.tiles.size)
        assertEquals(Strip.NUMBER_COUNT, prototype.strip.items.size)
        assertTrue(prototype.strip.items.all { it is StripItem.Known })
        assertTrue(prototype.board.tiles.all { tile -> tile.result == tile.expression.evaluate() })
    }
}
