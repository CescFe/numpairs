package org.cescfe.numpairs.domain.generated

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GeneratedPairsPuzzleGeneratorTest {

    @Test
    fun generate_returns_the_initial_player_facing_puzzle() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 2026
        ).generateWithSolution()
        val initialPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 2026
        ).generate()

        assertEquals(generatedPuzzle.initialPuzzle, initialPuzzle)
        assertGeneratedInitialPuzzleStructure(
            puzzle = initialPuzzle,
            profile = profile
        )
    }

    @Test
    fun generator_rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPairsPuzzleGenerator(
                profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
                maxAttempts = 0
            )
        }
    }

    @Test
    fun generator_fails_after_bounded_attempts_when_profile_cannot_be_satisfied() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val impossibleProfile = profile.copy(
            resultConstraints = profile.resultConstraints.copy(
                maxMultiplicationResult = 1
            )
        )

        assertThrows(IllegalStateException::class.java) {
            GeneratedPairsPuzzleGenerator(
                profile = impossibleProfile,
                seed = 2026,
                maxAttempts = 1
            ).generateWithSolution()
        }
    }
}
