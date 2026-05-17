package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.Strip

data class FourPairsSolution(
    val stripEntries: List<FourPairsSolvedStripEntry>,
    val tileAssignments: List<IndexedResolvedTileAssignment>
) {
    init {
        require(stripEntries.size == Strip.NUMBER_COUNT) {
            "Four Pairs solutions require exactly ${Strip.NUMBER_COUNT} strip entries."
        }
        require(stripEntryIds.size == Strip.NUMBER_COUNT) {
            "Four Pairs solution strip entry ids must be unique."
        }
        require(tileAssignments.size == Board.TILE_COUNT) {
            "Four Pairs solutions require exactly ${Board.TILE_COUNT} tile assignments."
        }
        require(tileIndexes == EXPECTED_TILE_INDEXES) {
            "Four Pairs solution tile assignments must cover every board tile exactly once."
        }
        require(assignedStripEntryIds.all { stripEntryId -> stripEntryId in stripEntryIds }) {
            "Four Pairs solution tile assignments must reference solution strip entry ids."
        }
    }

    val stripEntryIds: Set<Int>
        get() = stripEntries.map(FourPairsSolvedStripEntry::stripEntryId).toSet()

    private val tileIndexes: Set<Int>
        get() = tileAssignments.map(IndexedResolvedTileAssignment::tileIndex).toSet()

    private val assignedStripEntryIds: List<Int>
        get() = tileAssignments.flatMap { assignment ->
            listOf(
                assignment.leftOperand.stripEntryId,
                assignment.rightOperand.stripEntryId
            )
        }

    private companion object {
        val EXPECTED_TILE_INDEXES: Set<Int> = (0 until Board.TILE_COUNT).toSet()
    }
}

data class FourPairsSolvedStripEntry(val stripEntryId: Int, val value: Int) {
    init {
        require(stripEntryId >= 0) {
            "Solved strip entry id must be non-negative."
        }
        require(value > 0) {
            "Solved strip entry value must be a positive integer."
        }
    }
}
