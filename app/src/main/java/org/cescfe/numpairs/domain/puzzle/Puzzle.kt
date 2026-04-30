package org.cescfe.numpairs.domain.puzzle

data class Puzzle(val board: Board, val strip: Strip) {
    val isIncomplete: Boolean
        get() = strip.hasHiddenEntries || board.hasUnresolvedTiles

    val completionState: PuzzleCompletionState
        get() = when {
            isIncomplete -> PuzzleCompletionState.INCOMPLETE
            board.tiles.any { tile -> tile.resolutionState == TileResolutionState.INCORRECT } ->
                PuzzleCompletionState.INCORRECT_TILES
            resolvedTileAssignments().size != board.tiles.size ->
                PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES
            hasMismatchedSumProductPairings -> PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS
            hasInvalidStripEntryUsage -> PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE
            else -> PuzzleCompletionState.SOLVED
        }

    val isSolved: Boolean
        get() = completionState == PuzzleCompletionState.SOLVED
}

enum class PuzzleCompletionState {
    INCOMPLETE,
    INCORRECT_TILES,
    MISSING_STRIP_ENTRY_IDENTITIES,
    MISMATCHED_SUM_PRODUCT_PAIRINGS,
    INVALID_STRIP_ENTRY_USAGE,
    SOLVED
}
