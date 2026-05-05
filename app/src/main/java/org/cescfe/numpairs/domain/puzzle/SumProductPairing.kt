package org.cescfe.numpairs.domain.puzzle

val Puzzle.hasMismatchedSumProductPairings: Boolean
    get() = mismatchedSumProductPairingTileIndexes.isNotEmpty()

val Puzzle.mismatchedSumProductPairingTileIndexes: List<Int>
    get() {
        if (isIncomplete) {
            return emptyList()
        }

        if (board.tiles.any { tile -> tile.resolutionState != TileResolutionState.CORRECT }) {
            return emptyList()
        }

        val resolvedAssignments = resolvedTileAssignments()
        if (resolvedAssignments.size != board.tiles.size) {
            return emptyList()
        }

        val additionPairs = resolvedAssignments.unorderedStripEntryPairsFor(operator = Operator.ADDITION)
        val multiplicationPairs = resolvedAssignments.unorderedStripEntryPairsFor(
            operator = Operator.MULTIPLICATION
        )
        val mismatchedPairs = (additionPairs - multiplicationPairs) + (multiplicationPairs - additionPairs)

        return resolvedAssignments.filter { assignment ->
            assignment.unorderedStripEntryPair in mismatchedPairs
        }.map(IndexedResolvedTileAssignment::tileIndex)
    }

private data class UnorderedStripEntryPair(val firstStripEntryId: Int, val secondStripEntryId: Int)

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
