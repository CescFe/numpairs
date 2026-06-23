package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.StripEntry
import org.cescfe.numpairs.domain.puzzle.model.TileResolutionState

internal val Puzzle.hasIncorrectTiles: Boolean
    get() = board.tiles.any { tile -> tile.resolutionState == TileResolutionState.INCORRECT }

internal val Puzzle.hasMissingResolvedTileAssignments: Boolean
    get() {
        val resolvedAssignments = resolvedAssignmentsForCompletedBoardOrNull() ?: return false
        return resolvedAssignments.size != board.tiles.size
    }

val Puzzle.hasInvalidStripEntryUsage: Boolean
    get() {
        val resolvedAssignments = validatedResolvedAssignmentsOrNull() ?: return false
        val stripEntryIds = strip.entries.map(StripEntry::id)
        val additionUsageCounts = resolvedAssignments.usageCountsFor(operator = Operator.ADDITION)
        val multiplicationUsageCounts = resolvedAssignments.usageCountsFor(operator = Operator.MULTIPLICATION)

        return stripEntryIds.any { stripEntryId ->
            additionUsageCounts[stripEntryId] != 1 || multiplicationUsageCounts[stripEntryId] != 1
        }
    }

val Puzzle.hasMismatchedSumProductPairings: Boolean
    get() = mismatchedSumProductPairingTileIndexes.isNotEmpty()

val Puzzle.mismatchedSumProductPairingTileIndexes: List<Int>
    get() {
        val resolvedAssignments = validatedResolvedAssignmentsOrNull() ?: return emptyList()
        val mismatchedPairs = resolvedAssignments.mismatchedUnorderedPairs()

        return resolvedAssignments.mapNotNull { assignment ->
            assignment.takeIf { it.unorderedStripEntryPair in mismatchedPairs }?.tileIndex
        }
    }

private data class UnorderedStripEntryPair(val firstStripEntryId: Int, val secondStripEntryId: Int)

private fun Puzzle.resolvedAssignmentsForCompletedBoardOrNull(): List<IndexedResolvedTileAssignment>? {
    if (isIncomplete || hasIncorrectTiles) {
        return null
    }

    return resolvedTileAssignments()
}

private fun Puzzle.validatedResolvedAssignmentsOrNull(): List<IndexedResolvedTileAssignment>? {
    val resolvedAssignments = resolvedAssignmentsForCompletedBoardOrNull() ?: return null
    return resolvedAssignments.takeIf { assignments -> assignments.size == board.tiles.size }
}

private fun List<IndexedResolvedTileAssignment>.usageCountsFor(operator: Operator): Map<Int, Int> =
    filter { assignment ->
        assignment.operator == operator
    }.flatMap { assignment ->
        listOf(assignment.leftOperand.stripEntryId, assignment.rightOperand.stripEntryId)
    }.groupingBy { stripEntryId ->
        stripEntryId
    }.eachCount()

private fun List<IndexedResolvedTileAssignment>.mismatchedUnorderedPairs(): Set<UnorderedStripEntryPair> {
    val additionPairs = unorderedStripEntryPairsFor(operator = Operator.ADDITION)
    val multiplicationPairs = unorderedStripEntryPairsFor(operator = Operator.MULTIPLICATION)

    return (additionPairs - multiplicationPairs) + (multiplicationPairs - additionPairs)
}

private fun List<IndexedResolvedTileAssignment>.unorderedStripEntryPairsFor(
    operator: Operator
): Set<UnorderedStripEntryPair> = filter { assignment ->
    assignment.operator == operator
}.map(IndexedResolvedTileAssignment::unorderedStripEntryPair)
    .toSet()

private val IndexedResolvedTileAssignment.unorderedStripEntryPair: UnorderedStripEntryPair
    get() {
        val firstStripEntryId = minOf(leftOperand.stripEntryId, rightOperand.stripEntryId)
        val secondStripEntryId = maxOf(leftOperand.stripEntryId, rightOperand.stripEntryId)

        return UnorderedStripEntryPair(
            firstStripEntryId = firstStripEntryId,
            secondStripEntryId = secondStripEntryId
        )
    }
