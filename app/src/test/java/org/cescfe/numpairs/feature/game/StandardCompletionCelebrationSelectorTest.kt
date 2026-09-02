package org.cescfe.numpairs.feature.game

import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StandardCompletionCelebrationSelectorTest {
    @Test
    fun reconstructed_context_keeps_the_same_selection_across_configuration_changes() {
        val selections = List(20) {
            StandardCompletionCelebrationSelector.select(
                context(
                    generatedChallengeId = "four-pairs-medium",
                    completionId = "session-42",
                    difficulty = DifficultyTier.MEDIUM
                )
            )
        }

        assertEquals(1, selections.distinct().size)
    }

    @Test
    fun low_uses_only_the_general_catalog_until_good_performance_is_authoritative() {
        val eligible = StandardCompletionCelebrationSelector.eligibleVariants(DifficultyTier.LOW)

        assertEquals(GENERAL_VARIANTS, eligible.toSet())
        assertFalse(StandardCompletionCelebration.KEEP_IT_UP in eligible)
        assertFalse(StandardCompletionCelebration.MEDIUM_HARD_IMPRESSIVE in eligible)
        assertFalse(StandardCompletionCelebration.HARD_UNSTOPPABLE in eligible)
    }

    @Test
    fun medium_adds_only_its_shared_difficulty_variant() {
        val eligible = StandardCompletionCelebrationSelector.eligibleVariants(DifficultyTier.MEDIUM)

        assertEquals(
            GENERAL_VARIANTS + StandardCompletionCelebration.MEDIUM_HARD_IMPRESSIVE,
            eligible.toSet()
        )
        assertFalse(StandardCompletionCelebration.KEEP_IT_UP in eligible)
        assertFalse(StandardCompletionCelebration.HARD_UNSTOPPABLE in eligible)
    }

    @Test
    fun hard_adds_both_declared_difficulty_variants() {
        val eligible = StandardCompletionCelebrationSelector.eligibleVariants(DifficultyTier.HARD)

        assertEquals(
            GENERAL_VARIANTS +
                StandardCompletionCelebration.MEDIUM_HARD_IMPRESSIVE +
                StandardCompletionCelebration.HARD_UNSTOPPABLE,
            eligible.toSet()
        )
        assertFalse(StandardCompletionCelebration.KEEP_IT_UP in eligible)
    }

    @Test
    fun every_runtime_eligible_variant_can_be_selected_without_crossing_difficulty_boundaries() {
        DifficultyTier.entries.forEach { difficulty ->
            val eligible = StandardCompletionCelebrationSelector.eligibleVariants(difficulty).toSet()
            val selected = (0..1_000).mapTo(mutableSetOf()) { completionIndex ->
                StandardCompletionCelebrationSelector.select(
                    context(
                        generatedChallengeId = "challenge-${difficulty.name.lowercase()}",
                        completionId = "completion-$completionIndex",
                        difficulty = difficulty
                    )
                )
            }

            assertEquals(eligible, selected)
            assertTrue(selected.all(eligible::contains))
        }
    }

    private fun context(generatedChallengeId: String, completionId: String, difficulty: DifficultyTier) =
        StandardCompletionCelebrationContext(
            generatedChallengeId = generatedChallengeId,
            completionId = completionId,
            difficulty = difficulty
        )

    private companion object {
        val GENERAL_VARIANTS = setOf(
            StandardCompletionCelebration.GREAT_WORK,
            StandardCompletionCelebration.EXCELLENT,
            StandardCompletionCelebration.YOU_ROCK,
            StandardCompletionCelebration.NAILED_IT,
            StandardCompletionCelebration.BRILLIANT
        )
    }
}
