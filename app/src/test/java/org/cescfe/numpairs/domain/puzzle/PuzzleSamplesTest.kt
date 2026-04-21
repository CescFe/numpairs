package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun prototype_tiles_have_valid_results() {
        val boardTiles = PuzzleSamples.prototype.board.tiles

        assertTrue(boardTiles.all { tile -> tile.result == tile.expression.evaluate() })
    }
}
