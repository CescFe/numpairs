package org.cescfe.numpairs.domain.fourpairs

import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleSolution

data class FourPairsGenerationRequest(
    val seed: Long? = null,
    val difficulty: FourPairsDifficulty = FourPairsDifficulty.LOW
)

data class FourPairsGeneratedPuzzle(
    val initialPuzzle: Puzzle,
    val solution: PuzzleSolution,
    val difficulty: FourPairsDifficulty
) {
    init {
        require(solution.isSolutionFor(initialPuzzle)) {
            "Generated puzzle solution must solve the initial puzzle."
        }
    }
}

enum class FourPairsDifficulty {
    LOW
}
