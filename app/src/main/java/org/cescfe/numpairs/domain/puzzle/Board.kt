package org.cescfe.numpairs.domain.puzzle

data class Board(val tiles: List<Tile>) {
    val hasUnresolvedTiles: Boolean
        get() = tiles.any { tile -> tile.resolutionState == TileResolutionState.UNRESOLVED }
}
