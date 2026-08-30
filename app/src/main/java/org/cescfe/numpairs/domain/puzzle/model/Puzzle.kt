package org.cescfe.numpairs.domain.puzzle.model

import org.cescfe.numpairs.domain.puzzle.validation.hasIncorrectTiles
import org.cescfe.numpairs.domain.puzzle.validation.resolvedAssignmentCompletionState

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
            else -> resolvedAssignmentCompletionState ?: PuzzleCompletionState.SOLVED
        }

    val isSolved: Boolean
        get() = completionState == PuzzleCompletionState.SOLVED

    fun withUpdatedStripEntry(index: Int, value: Int): Puzzle {
        val currentStripEntry = requireNotNull(strip.entries.getOrNull(index)) {
            "Strip item index must be within the strip bounds."
        }
        val updatedStrip = strip.withUpdatedEntry(index = index, value = value)

        return copy(
            board = board.withUpdatedStripEntryOperandValue(
                stripEntryId = currentStripEntry.id,
                value = value
            ),
            strip = updatedStrip
        )
    }

    private companion object {
        const val MIN_STRIP_ENTRY_COUNT = 2
    }
}

private fun Board.withUpdatedStripEntryOperandValue(stripEntryId: Int, value: Int): Board = copy(
    tiles = tiles.map { tile ->
        tile.copy(
            expression = tile.expression.withUpdatedStripEntryOperandValue(
                stripEntryId = stripEntryId,
                value = value
            )
        )
    }
)

private fun Expression.withUpdatedStripEntryOperandValue(stripEntryId: Int, value: Int): Expression = copy(
    leftOperand = leftOperand.withUpdatedStripEntryOperandValue(stripEntryId = stripEntryId, value = value),
    rightOperand = rightOperand.withUpdatedStripEntryOperandValue(stripEntryId = stripEntryId, value = value)
)

private fun Expression.Operand.withUpdatedStripEntryOperandValue(stripEntryId: Int, value: Int): Expression.Operand =
    when (this) {
        Expression.Operand.Hidden -> this
        is Expression.Operand.Known -> if (this.stripEntryId == stripEntryId) copy(value = value) else this
    }

enum class PuzzleCompletionState {
    INCOMPLETE,
    INCORRECT_TILES,
    MISSING_STRIP_ENTRY_IDENTITIES,
    MISMATCHED_SUM_PRODUCT_PAIRINGS,
    INVALID_STRIP_ENTRY_USAGE,
    SOLVED
}
