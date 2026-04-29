package org.cescfe.numpairs.domain.puzzle

data class Board(val tiles: List<Tile>) {
    init {
        require(tiles.size == TILE_COUNT) {
            "Board must contain exactly $TILE_COUNT tiles."
        }
    }

    val hasUnresolvedTiles: Boolean
        get() = tiles.any { tile -> tile.resolutionState == TileResolutionState.UNRESOLVED }

    fun tileAt(index: Int): Tile = tiles[index]

    companion object {
        const val TILE_COUNT = 8
    }
}
