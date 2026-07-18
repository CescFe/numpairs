package org.cescfe.numpairs.feature.game.presentation

data class TileAssignmentCommit(val tileIndex: Int, val madeTileCorrect: Boolean) {
    init {
        require(tileIndex >= 0) {
            "Tile assignment commits require a non-negative tile index."
        }
    }
}
