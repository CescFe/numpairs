package org.cescfe.numpairs.feature.daily

import java.time.DayOfWeek
import java.time.LocalDate
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class CurrentDailyChallengeResolverTest {
    @Test
    fun resolver_reads_the_local_date_once_and_combines_it_with_the_active_recipe() {
        var dateReadCount = 0
        val resolver = CurrentDailyChallengeResolver(
            localDateSource = DeviceLocalDateSource {
                dateReadCount += 1
                LocalDate.of(2026, 7, 25)
            }
        )

        val currentDailyChallenge = resolver.resolve()

        assertEquals(1, dateReadCount)
        assertEquals(LocalDate.of(2026, 7, 25), currentDailyChallenge.identity.localDate)
        assertEquals(DailyRecipes.WEEKLY_SCHEDULE_V2.version, currentDailyChallenge.identity.recipeVersion)
        assertSame(DailyRecipes.WEEKLY_SCHEDULE_V2, currentDailyChallenge.recipe)
        assertSame(GeneratedModes.THREE_PAIRS_MEDIUM, currentDailyChallenge.challenge)
    }

    @Test
    fun resolved_identity_does_not_change_when_the_local_date_source_changes() {
        var currentDate = LocalDate.of(2026, 12, 31)
        val resolver = CurrentDailyChallengeResolver(
            localDateSource = DeviceLocalDateSource { currentDate }
        )

        val firstResolution = resolver.resolve()
        currentDate = LocalDate.of(2027, 1, 1)

        assertEquals(LocalDate.of(2026, 12, 31), firstResolution.identity.localDate)
        assertSame(GeneratedModes.FOUR_PAIRS_MEDIUM, firstResolution.challenge)
        val secondResolution = resolver.resolve()
        assertEquals(LocalDate.of(2027, 1, 1), secondResolution.identity.localDate)
        assertSame(GeneratedModes.EIGHT_PAIRS_MEDIUM, secondResolution.challenge)
    }

    @Test
    fun resolver_selects_the_configured_challenge_for_every_captured_weekday() {
        val expectedChallenges = mapOf(
            DayOfWeek.MONDAY to GeneratedModes.THREE_PAIRS_LOW,
            DayOfWeek.TUESDAY to GeneratedModes.FOUR_PAIRS_LOW,
            DayOfWeek.WEDNESDAY to GeneratedModes.THREE_PAIRS_MEDIUM,
            DayOfWeek.THURSDAY to GeneratedModes.FOUR_PAIRS_MEDIUM,
            DayOfWeek.FRIDAY to GeneratedModes.EIGHT_PAIRS_MEDIUM,
            DayOfWeek.SATURDAY to GeneratedModes.THREE_PAIRS_MEDIUM,
            DayOfWeek.SUNDAY to GeneratedModes.FOUR_PAIRS_LOW
        )

        expectedChallenges.forEach { (dayOfWeek, expectedChallenge) ->
            val resolver = CurrentDailyChallengeResolver(
                localDateSource = DeviceLocalDateSource { dateFor(dayOfWeek) }
            )

            assertSame(expectedChallenge, resolver.resolve().challenge)
        }
    }

    @Test
    fun current_daily_challenge_rejects_an_identity_from_another_recipe() {
        val configuredRecipe = DailyRecipes.FOUR_PAIRS_LOW_V1

        assertThrows(IllegalArgumentException::class.java) {
            CurrentDailyChallenge(
                identity = configuredRecipe.identityFor(LocalDate.of(2026, 7, 25)).copy(
                    recipeVersion = DailyRecipeVersion("other-recipe")
                ),
                recipe = configuredRecipe
            )
        }
    }
}

private fun dateFor(dayOfWeek: DayOfWeek): LocalDate = LocalDate.of(2026, 8, 31).plusDays(
    (dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
)
