package org.cescfe.numpairs.feature.daily

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
        assertEquals(DailyRecipes.FOUR_PAIRS_LOW_V1.version, currentDailyChallenge.identity.recipeVersion)
        assertSame(DailyRecipes.FOUR_PAIRS_LOW_V1, currentDailyChallenge.recipe)
        assertSame(GeneratedModes.FOUR_PAIRS_LOW, currentDailyChallenge.recipe.challenge)
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
        assertEquals(LocalDate.of(2027, 1, 1), resolver.resolve().identity.localDate)
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
