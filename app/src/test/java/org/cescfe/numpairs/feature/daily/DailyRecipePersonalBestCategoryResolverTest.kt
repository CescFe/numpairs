package org.cescfe.numpairs.feature.daily

import java.time.LocalDate
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyPersonalBestCategory
import org.cescfe.numpairs.domain.daily.DailyPersonalBestHistory
import org.cescfe.numpairs.domain.daily.DailyPersonalBestOutcome
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.assertDailyElapsedTimeEquals
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyRecipePersonalBestCategoryResolverTest {
    private val resolver = DailyRecipePersonalBestCategoryResolver()

    @Test
    fun weekly_recipe_resolves_all_five_exact_generated_challenge_categories() {
        val firstMonday = LocalDate.of(2026, 8, 31)
        val expectedChallenges = listOf(
            GeneratedModes.THREE_PAIRS_LOW,
            GeneratedModes.FOUR_PAIRS_LOW,
            GeneratedModes.THREE_PAIRS_MEDIUM,
            GeneratedModes.FOUR_PAIRS_MEDIUM,
            GeneratedModes.EIGHT_PAIRS_MEDIUM,
            GeneratedModes.THREE_PAIRS_MEDIUM,
            GeneratedModes.FOUR_PAIRS_LOW
        )

        expectedChallenges.forEachIndexed { dayOffset, challenge ->
            val identity = DailyRecipes.WEEKLY_SCHEDULE_V2.identityFor(
                firstMonday.plusDays(dayOffset.toLong())
            )

            assertEquals(
                DailyPersonalBestCategory(challenge.id.value),
                resolver.categoryFor(identity)
            )
        }
    }

    @Test
    fun legacy_timed_history_resolves_to_four_pairs_low_without_migration() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(LocalDate.of(2026, 8, 30))

        assertEquals(
            DailyPersonalBestCategory(GeneratedModes.FOUR_PAIRS_LOW.id.value),
            resolver.categoryFor(identity)
        )
    }

    @Test
    fun legacy_four_pairs_low_time_is_the_baseline_for_a_weekly_recipe_completion() {
        val legacyCompletion = DailyCompletion(
            identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(LocalDate.of(2026, 8, 30)),
            elapsedTime = DailyElapsedTime(5_000)
        )
        val weeklyCompletion = DailyCompletion(
            identity = DailyRecipes.WEEKLY_SCHEDULE_V2.identityFor(LocalDate.of(2026, 9, 1)),
            elapsedTime = DailyElapsedTime(4_000)
        )
        val history = DailyPersonalBestHistory(
            completions = listOf(legacyCompletion),
            categoryResolver = resolver
        )

        val result = history.resultFor(weeklyCompletion)

        assertEquals(DailyPersonalBestOutcome.PERSONAL_RECORD, result.outcome)
        assertDailyElapsedTimeEquals(5_000, result.previousBestElapsedTime)
        assertDailyElapsedTimeEquals(4_000, result.bestElapsedTime)
    }

    @Test
    fun unsupported_recipe_does_not_resolve_a_personal_best_category() {
        assertNull(
            resolver.categoryFor(
                DailyChallengeId(
                    localDate = LocalDate.of(2026, 8, 30),
                    recipeVersion = DailyRecipeVersion("retired-unsupported-recipe")
                )
            )
        )
    }
}
