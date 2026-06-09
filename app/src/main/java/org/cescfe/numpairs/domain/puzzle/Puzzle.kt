package org.cescfe.numpairs.domain.puzzle

data class Puzzle(val board: Board, val strip: Strip) {
    init {
        require(strip.entries.size >= MIN_STRIP_ENTRY_COUNT) {
            "Puzzle strip must contain at least $MIN_STRIP_ENTRY_COUNT entries."
        }
        require(strip.entries.size % 2 == 0) {
            "Puzzle strip entry count must be even."
        }
        require(board.tiles.size == strip.entries.size) {
            "Puzzle board tile count must match strip entry count."
        }
    }

    val isIncomplete: Boolean
        get() = strip.hasHiddenEntries || board.hasUnresolvedTiles

    val completionState: PuzzleCompletionState
        get() = when {
            isIncomplete -> PuzzleCompletionState.INCOMPLETE
            hasIncorrectTiles -> PuzzleCompletionState.INCORRECT_TILES
            hasMissingResolvedTileAssignments -> PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES
            hasMismatchedSumProductPairings -> PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS
            hasInvalidStripEntryUsage -> PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE
            else -> PuzzleCompletionState.SOLVED
        }

    val isSolved: Boolean
        get() = completionState == PuzzleCompletionState.SOLVED

    private companion object {
        const val MIN_STRIP_ENTRY_COUNT = 2
    }
}

enum class PuzzleCompletionState {
    INCOMPLETE,
    INCORRECT_TILES,
    MISSING_STRIP_ENTRY_IDENTITIES,
    MISMATCHED_SUM_PRODUCT_PAIRINGS,
    INVALID_STRIP_ENTRY_USAGE,
    SOLVED
}
