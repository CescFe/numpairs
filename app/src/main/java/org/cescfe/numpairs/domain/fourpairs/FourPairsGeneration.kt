package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.puzzle.Puzzle

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

enum class FourPairsDifficulty {
    LOW
}
