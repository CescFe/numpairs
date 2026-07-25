package org.cescfe.numpairs.feature.daily.share

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.feature.daily.DailyRecipes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyCompletionShareTextFormatterTest {
    @Test
    fun formatted_result_contains_only_the_required_two_line_completion_copy() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        val locale = Locale.US
        val text = DailyCompletionShareTextFormatter().format(
            completedIdentity = identity,
            copy = DailyCompletionShareCopy(
                dailyName = "NumPairs Daily",
                challengeName = "4 Pairs · Low",
                completedStatus = "Completed"
            ),
            locale = locale
        )
        val expectedDate = identity.localDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        )

        assertEquals(
            "NumPairs Daily · $expectedDate\n4 Pairs · Low · Completed",
            text.value
        )
    }

    @Test
    fun formatter_localizes_display_date_without_changing_the_canonical_identity() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2028, 2, 29)
        )
        val canonicalDate = identity.canonicalLocalDate
        val locale = Locale.forLanguageTag("es-ES")

        val text = DailyCompletionShareTextFormatter().format(
            completedIdentity = identity,
            copy = DailyCompletionShareCopy(
                dailyName = "NumPairs Daily",
                challengeName = "4 pares · Baja",
                completedStatus = "Completado"
            ),
            locale = locale
        )
        val expectedDate = identity.localDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        )

        assertEquals(
            "NumPairs Daily · $expectedDate\n4 pares · Baja · Completado",
            text.value
        )
        assertEquals("2028-02-29", canonicalDate)
        assertEquals(canonicalDate, identity.canonicalLocalDate)
    }

    @Test
    fun unknown_recipe_identity_cannot_be_shared() {
        val identity = DailyChallengeId(
            localDate = LocalDate.of(2026, 7, 25),
            recipeVersion = DailyRecipeVersion("unknown-daily-recipe")
        )

        assertThrows(IllegalArgumentException::class.java) {
            DailyCompletionShareTextFormatter().format(
                completedIdentity = identity,
                copy = DailyCompletionShareCopy(
                    dailyName = "NumPairs Daily",
                    challengeName = "4 Pairs · Low",
                    completedStatus = "Completed"
                ),
                locale = Locale.US
            )
        }
    }

    @Test
    fun localized_copy_requires_every_visible_field() {
        assertThrows(IllegalArgumentException::class.java) {
            DailyCompletionShareCopy(
                dailyName = " ",
                challengeName = "4 Pairs · Low",
                completedStatus = "Completed"
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyCompletionShareCopy(
                dailyName = "NumPairs Daily",
                challengeName = "",
                completedStatus = "Completed"
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyCompletionShareCopy(
                dailyName = "NumPairs Daily",
                challengeName = "4 Pairs · Low",
                completedStatus = ""
            )
        }
    }
}
