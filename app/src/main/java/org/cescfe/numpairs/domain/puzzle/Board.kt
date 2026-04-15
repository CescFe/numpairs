package org.cescfe.numpairs.domain.puzzle

data class Board(val tiles: List<Tile>) {
    init {
        require(tiles.size == TILE_COUNT) {
            "Board must contain exactly $TILE_COUNT tiles."
        }
    }

    fun tileAt(index: Int): Tile = tiles[index]

    companion object {
        const val TILE_COUNT = 8
    }
}
