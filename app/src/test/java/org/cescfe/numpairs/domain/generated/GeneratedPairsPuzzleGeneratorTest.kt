package org.cescfe.numpairs.domain.generated

import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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

    @Test
    fun generator_returns_a_hard_valid_fallback_when_the_soft_mask_plan_is_infeasible() {
        val baseProfile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val profile = baseProfile.copy(
            initialStripMaskPolicy = baseProfile.initialStripMaskPolicy.copy(
                highValueMaskTargets = listOf(
                    HighValueMaskTarget(
                        rankFromHighest = 1,
                        targetHiddenProbability = ProbabilityPercent(100)
                    )
                )
            )
        )
        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 2026,
            maxAttempts = 1
        ).generateWithSolution()

        assertGeneratedInitialPuzzleStructure(
            puzzle = generatedPuzzle.initialPuzzle,
            profile = profile
        )
        assertTrue(generatedPuzzle.initialPuzzle.strip.entries.last().item is StripItem.Known)
    }
}
