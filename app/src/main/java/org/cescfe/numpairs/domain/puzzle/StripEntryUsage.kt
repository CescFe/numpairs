package org.cescfe.numpairs.domain.puzzle

val Puzzle.hasInvalidStripEntryUsage: Boolean
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

        val stripEntryIds = strip.entries.map(StripEntry::id)
        val additionUsageCounts = resolvedAssignments.usageCountsFor(operator = Operator.ADDITION)
        val multiplicationUsageCounts = resolvedAssignments.usageCountsFor(operator = Operator.MULTIPLICATION)

        return stripEntryIds.any { stripEntryId ->
            additionUsageCounts[stripEntryId] != 1 || multiplicationUsageCounts[stripEntryId] != 1
        }
    }

private fun List<IndexedResolvedTileAssignment>.usageCountsFor(operator: Operator): Map<Int, Int> = filter { assignment ->
    assignment.operator == operator
}.flatMap { assignment ->
    listOf(assignment.leftOperand.stripEntryId, assignment.rightOperand.stripEntryId)
}.groupingBy { stripEntryId ->
    stripEntryId
}.eachCount()
