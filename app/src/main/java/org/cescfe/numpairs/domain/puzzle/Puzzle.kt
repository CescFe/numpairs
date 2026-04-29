package org.cescfe.numpairs.domain.puzzle

data class Puzzle(val board: Board, val strip: Strip) {
    val isIncomplete: Boolean
        get() = strip.hasHiddenEntries || board.hasUnresolvedTiles
}
