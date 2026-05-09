package org.cescfe.numpairs.domain.puzzle

import org.junit.Assert.assertThrows
import org.junit.Test
import org.cescfe.numpairs.domain.puzzle.support.additionTile

class BoardTest {
    @Test
    fun requires_exactly_eight_tiles() {
        listOf(0, 7, 9).forEach { tileCount ->
            assertThrows(IllegalArgumentException::class.java) {
                Board(tiles = validTiles(tileCount))
            }
        }
    }

    @Test
    fun accepts_eight_tiles() {
        Board(tiles = validTiles(Board.TILE_COUNT))
    }

    private fun validTiles(count: Int): List<Tile> = (1..count).map { leftOperand ->
        additionTile(
            leftOperand = leftOperand,
            rightOperand = 1
        )
    }
}
