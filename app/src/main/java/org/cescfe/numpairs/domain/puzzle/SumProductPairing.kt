package org.cescfe.numpairs.domain.puzzle

val Puzzle.hasMismatchedSumProductPairings: Boolean
    get() {
        if (isIncomplete) {
            return false
        }

        if (board.tiles.any { tile -> tile.resolutionState != TileResolutionState.CORRECT }) {
            return false
        }

        val resolvedAssignments = resolvedTileAssignments()
        if (resolvedAssignments.size != board.tiles.size) {
            return false
        }

        val additionPairs = resolvedAssignments
            .filter { assignment -> assignment.operator == Operator.ADDITION }
            .map(IndexedResolvedTileAssignment::unorderedStripEntryPair)
            .toSet()
        val multiplicationPairs = resolvedAssignments
            .filter { assignment -> assignment.operator == Operator.MULTIPLICATION }
            .map(IndexedResolvedTileAssignment::unorderedStripEntryPair)
            .toSet()

        return additionPairs != multiplicationPairs
    }

private data class UnorderedStripEntryPair(val firstStripEntryId: Int, val secondStripEntryId: Int)

private val IndexedResolvedTileAssignment.unorderedStripEntryPair: UnorderedStripEntryPair
    get() {
        val firstStripEntryId = minOf(leftOperand.stripEntryId, rightOperand.stripEntryId)
        val secondStripEntryId = maxOf(leftOperand.stripEntryId, rightOperand.stripEntryId)

        return UnorderedStripEntryPair(
            firstStripEntryId = firstStripEntryId,
            secondStripEntryId = secondStripEntryId
        )
    }
