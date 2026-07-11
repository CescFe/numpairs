package org.cescfe.numpairs.domain.puzzle.validation

import org.cescfe.numpairs.domain.puzzle.assignment.ResolvedPuzzleAnalysis
import org.cescfe.numpairs.domain.puzzle.assignment.analyzeResolvedPuzzle
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.TileResolutionState

internal val Puzzle.hasIncorrectTiles: Boolean
    get() = board.tiles.any { tile -> tile.resolutionState == TileResolutionState.INCORRECT }

internal val Puzzle.resolvedAssignmentCompletionState: PuzzleCompletionState?
    get() {
        val analysis = resolvedAnalysisForCompletedBoardOrNull() ?: return null
        return when {
            analysis.unresolvedAssignments.isNotEmpty() -> PuzzleCompletionState.MISSING_STRIP_ENTRY_IDENTITIES
            analysis.mismatchedSolutionPairs.isNotEmpty() -> PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS
            analysis.hasInvalidStripEntryUsage -> PuzzleCompletionState.INVALID_STRIP_ENTRY_USAGE
            else -> null
        }
    }

val Puzzle.hasInvalidStripEntryUsage: Boolean
    get() {
        val analysis = validatedResolvedAssignmentsOrNull() ?: return false
        return analysis.hasInvalidStripEntryUsage
    }

val Puzzle.hasMismatchedSumProductPairings: Boolean
    get() = mismatchedSumProductPairingTileIndexes.isNotEmpty()

val Puzzle.mismatchedSumProductPairingTileIndexes: List<Int>
    get() {
        val analysis = validatedResolvedAssignmentsOrNull() ?: return emptyList()

        return analysis.resolvedAssignments.mapNotNull { assignment ->
            assignment.tileIndex.takeIf { tileIndex ->
                analysis.solutionPairByTileIndex.getValue(tileIndex) in analysis.mismatchedSolutionPairs
            }
        }
    }

private fun Puzzle.resolvedAnalysisForCompletedBoardOrNull(): ResolvedPuzzleAnalysis? {
    if (isIncomplete || hasIncorrectTiles) {
        return null
    }

    return analyzeResolvedPuzzle()
}

private fun Puzzle.validatedResolvedAssignmentsOrNull(): ResolvedPuzzleAnalysis? =
    resolvedAnalysisForCompletedBoardOrNull()
        ?.takeIf { analysis -> analysis.unresolvedAssignments.isEmpty() }

private val ResolvedPuzzleAnalysis.hasInvalidStripEntryUsage: Boolean
    get() {
        val additionUsageCounts = usageCountsFor(operator = Operator.ADDITION)
        val multiplicationUsageCounts = usageCountsFor(operator = Operator.MULTIPLICATION)

        return stripEntryIds.any { stripEntryId ->
            additionUsageCounts[stripEntryId] != 1 || multiplicationUsageCounts[stripEntryId] != 1
        }
    }
