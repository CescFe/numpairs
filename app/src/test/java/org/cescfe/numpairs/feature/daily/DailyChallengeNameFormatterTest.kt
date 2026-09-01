package org.cescfe.numpairs.feature.daily

import java.time.LocalDate
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyChallengeNameFormatterTest {
    @Test
    fun formatter_resolves_every_scheduled_weekday_and_the_legacy_recipe_from_identity() {
        val copy = englishCopy()
        val scenarios = listOf(
            LocalDate.of(2026, 8, 31) to "3 Pairs · Low",
            LocalDate.of(2026, 9, 1) to "4 Pairs · Low",
            LocalDate.of(2026, 9, 2) to "3 Pairs · Medium",
            LocalDate.of(2026, 9, 3) to "4 Pairs · Medium",
            LocalDate.of(2026, 9, 4) to "8 Pairs · Medium",
            LocalDate.of(2026, 9, 5) to "3 Pairs · Medium",
            LocalDate.of(2026, 9, 6) to "4 Pairs · Low"
        )
        val formatter = DailyChallengeNameFormatter()

        scenarios.forEach { (date, expectedName) ->
            assertEquals(
                expectedName,
                formatter.format(DailyRecipes.WEEKLY_SCHEDULE_V2.identityFor(date), copy)
            )
        }
        assertEquals(
            "4 Pairs · Low",
            formatter.format(
                DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(LocalDate.of(2026, 9, 2)),
                copy
            )
        )
    }

    @Test
    fun formatter_rejects_missing_or_blank_localized_challenge_names() {
        assertThrows(IllegalArgumentException::class.java) {
            DailyChallengeNameCopy(emptyMap())
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyChallengeNameCopy(mapOf(GeneratedModes.FOUR_PAIRS_LOW.id to " "))
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyChallengeNameFormatter().format(
                identity = DailyRecipes.WEEKLY_SCHEDULE_V2.identityFor(LocalDate.of(2026, 9, 2)),
                copy = DailyChallengeNameCopy(
                    mapOf(GeneratedModes.FOUR_PAIRS_LOW.id to "4 Pairs · Low")
                )
            )
        }
    }
}

private fun englishCopy(): DailyChallengeNameCopy = DailyChallengeNameCopy(
    namesByChallengeId = mapOf(
        GeneratedModes.THREE_PAIRS_LOW.id to "3 Pairs · Low",
        GeneratedModes.FOUR_PAIRS_LOW.id to "4 Pairs · Low",
        GeneratedModes.THREE_PAIRS_MEDIUM.id to "3 Pairs · Medium",
        GeneratedModes.FOUR_PAIRS_MEDIUM.id to "4 Pairs · Medium",
        GeneratedModes.EIGHT_PAIRS_MEDIUM.id to "8 Pairs · Medium"
    )
)
