package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.domain.puzzle.support.additionTile
import org.junit.Assert.assertEquals
import org.junit.Test

class BoardTest {
    @Test
    fun supports_variable_tile_counts() {
        listOf(2, 4, 8).forEach { tileCount ->
            assertEquals(
                tileCount,
                Board(tiles = validTiles(tileCount)).tiles.size
            )
        }
    }

    private fun validTiles(count: Int): List<Tile> = (1..count).map { leftOperand ->
        additionTile(
            leftOperand = leftOperand,
            rightOperand = 1
        )
    }
}
