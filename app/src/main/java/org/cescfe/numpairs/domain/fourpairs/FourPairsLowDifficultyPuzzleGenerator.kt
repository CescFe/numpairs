package org.cescfe.numpairs.domain.fourpairs

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

/**
 * Legacy compatibility wrapper for generated 4 Pairs Low puzzles.
 * Runtime generation should use GeneratedPairsPuzzleGenerator with GeneratedPuzzleProfiles.FOUR_PAIRS_LOW.
 */
@Deprecated(
    message = "Use GeneratedPairsPuzzleGenerator with GeneratedPuzzleProfiles.FOUR_PAIRS_LOW."
)
class FourPairsLowDifficultyPuzzleGenerator(random: Random = Random.Default, maxAttempts: Int = DEFAULT_MAX_ATTEMPTS) {
    private val delegate = GeneratedPairsPuzzleGenerator(
        profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
        random = random,
        maxAttempts = maxAttempts
    )

    constructor(seed: Int) : this(random = Random(seed))

    fun generate(): Puzzle = delegate.generate()

    fun generateWithSolution(): FourPairsGeneratedPuzzle {
        val generatedPuzzle = delegate.generateWithSolution()

        return FourPairsGeneratedPuzzle(
            initialPuzzle = generatedPuzzle.initialPuzzle,
            solvedPuzzle = generatedPuzzle.solvedPuzzle
        )
    }

    companion object {
        private const val DEFAULT_MAX_ATTEMPTS = 50
    }
}
