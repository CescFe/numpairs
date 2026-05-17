package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.Strip

data class FourPairsGenerationRequest(
    val seed: Long? = null,
    val difficulty: FourPairsDifficulty = FourPairsDifficulty.LOW
)

data class FourPairsGeneratedPuzzle(
    val initialPuzzle: Puzzle,
    val solution: FourPairsSolution,
    val difficulty: FourPairsDifficulty
) {
    init {
        val initialStripEntryIds = initialPuzzle.strip.entries.map { stripEntry -> stripEntry.id }.toSet()

        require(solution.stripEntryIds == initialStripEntryIds) {
            "Generated puzzle solution must reference the same strip entry ids as the initial puzzle."
        }
    }
}

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

enum class FourPairsDifficulty {
    LOW
}

sealed interface FourPairsValidationResult {
    val isValid: Boolean

    data object Valid : FourPairsValidationResult {
        override val isValid: Boolean = true
    }

    data class Invalid(val failures: Set<FourPairsValidationFailure>) : FourPairsValidationResult {
        init {
            require(failures.isNotEmpty()) {
                "Invalid Four Pairs validation results require at least one failure."
            }
        }

        override val isValid: Boolean = false
    }
}

enum class FourPairsValidationFailure {
    NO_SOLUTION,
    INVALID_SOLUTION,
    OUTSIDE_LOW_DIFFICULTY_PROFILE
}
