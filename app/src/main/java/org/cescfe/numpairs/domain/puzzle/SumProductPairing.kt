package org.cescfe.numpairs.domain.puzzle

val Puzzle.hasMismatchedSumProductPairings: Boolean
    get() = mismatchedSumProductPairingTileIndexes.isNotEmpty()

val Puzzle.mismatchedSumProductPairingTileIndexes: List<Int>
    get() {
        val resolvedAssignments = resolvedAssignmentsForPairingCheck() ?: return emptyList()
        val mismatchedPairs = resolvedAssignments.mismatchedUnorderedPairs()

        return resolvedAssignments.mapNotNull { assignment ->
            assignment.takeIf { it.unorderedStripEntryPair in mismatchedPairs }?.tileIndex
        }
    }

private data class UnorderedStripEntryPair(val firstStripEntryId: Int, val secondStripEntryId: Int)

private fun Puzzle.resolvedAssignmentsForPairingCheck(): List<IndexedResolvedTileAssignment>? {
    if (isIncomplete) {
        return null
    }

    if (board.tiles.any { tile -> tile.resolutionState != TileResolutionState.CORRECT }) {
        return null
    }

    val resolvedAssignments = resolvedTileAssignments()
    return resolvedAssignments.takeIf { assignments -> assignments.size == board.tiles.size }
}

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
