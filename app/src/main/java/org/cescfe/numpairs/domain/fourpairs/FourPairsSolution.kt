package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.puzzle.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.resolvedTileAssignments

data class FourPairsSolution(val solvedPuzzle: Puzzle) {
    init {
        require(solvedPuzzle.isSolved) {
            "Four Pairs solutions require a solved puzzle."
        }
    }

    val stripEntryIds: Set<Int>
        get() = solvedPuzzle.strip.entries.map { stripEntry -> stripEntry.id }.toSet()

    val tileAssignments: List<IndexedResolvedTileAssignment>
        get() = solvedPuzzle.resolvedTileAssignments()
}
