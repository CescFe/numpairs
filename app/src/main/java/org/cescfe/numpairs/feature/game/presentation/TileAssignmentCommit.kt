package org.cescfe.numpairs.feature.game.presentation

data class TileAssignmentCommit(
    val tileIndex: Int,
    val madeTileCorrect: Boolean,
    val madePuzzleSolved: Boolean = false
) {
    init {
        require(tileIndex >= 0) {
            "Tile assignment commits require a non-negative tile index."
        }
        require(!madePuzzleSolved || madeTileCorrect) {
            "A tile assignment can solve the puzzle only when it makes its tile correct."
        }
    }
}
