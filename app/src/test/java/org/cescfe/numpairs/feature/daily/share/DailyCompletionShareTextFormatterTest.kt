package org.cescfe.numpairs.feature.daily.share

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.feature.daily.DailyRecipes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyCompletionShareTextFormatterTest {
    @Test
    fun timed_result_contains_the_shared_truncated_completion_duration() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        val locale = Locale.US

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(identity, elapsedMilliseconds = 125_999),
            copy = englishCopy(),
            locale = locale
        )
        val expectedDate = localizedDate(identity, locale)

        assertEquals(
            "NumPairs Daily · $expectedDate\n4 Pairs · Low · Completed in 02:05",
            text.value
        )
    }

    @Test
    fun durations_of_sixty_minutes_or_more_keep_unbounded_minutes() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(identity, elapsedMilliseconds = 3_661_999),
            copy = englishCopy(),
            locale = Locale.US
        )

        assertEquals(
            "NumPairs Daily · Jul 25, 2026\n4 Pairs · Low · Completed in 61:01",
            text.value
        )
    }

    @Test
    fun formatter_localizes_display_copy_without_changing_the_canonical_identity() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2028, 2, 29)
        )
        val canonicalDate = identity.canonicalLocalDate
        val locale = Locale.forLanguageTag("es-ES")

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(identity, elapsedMilliseconds = 65_432),
            copy = DailyCompletionShareCopy(
                dailyName = "NumPairs Daily",
                challengeName = "4 pares · Baja",
                completedStatus = "Completado",
                completedInStatusFormat = "Completado en %1\$s"
            ),
            locale = locale
        )
        val expectedDate = localizedDate(identity, locale)

        assertEquals(
            "NumPairs Daily · $expectedDate\n4 pares · Baja · Completado en 01:05",
            text.value
        )
        assertEquals("2028-02-29", canonicalDate)
        assertEquals(canonicalDate, identity.canonicalLocalDate)
    }

    @Test
    fun legacy_completion_without_duration_keeps_the_existing_share_copy() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        val locale = Locale.US

        val text = DailyCompletionShareTextFormatter().format(
            completion = completion(identity, elapsedMilliseconds = null),
            copy = englishCopy(),
            locale = locale
        )
        val expectedDate = localizedDate(identity, locale)

        assertEquals(
            "NumPairs Daily · $expectedDate\n4 Pairs · Low · Completed",
            text.value
        )
    }

    @Test
    fun unknown_recipe_completion_cannot_be_shared() {
        val identity = DailyChallengeId(
            localDate = LocalDate.of(2026, 7, 25),
            recipeVersion = DailyRecipeVersion("unknown-daily-recipe")
        )

        assertThrows(IllegalArgumentException::class.java) {
            DailyCompletionShareTextFormatter().format(
                completion = completion(identity, elapsedMilliseconds = 65_432),
                copy = englishCopy(),
                locale = Locale.US
            )
        }
    }

    @Test
    fun localized_copy_requires_every_visible_field() {
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(dailyName = " ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(challengeName = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(completedStatus = "")
        }
        assertThrows(IllegalArgumentException::class.java) {
            englishCopy(completedInStatusFormat = " ")
        }
    }
}

private fun completion(identity: DailyChallengeId, elapsedMilliseconds: Long?): DailyCompletion = DailyCompletion(
    identity = identity,
    elapsedTime = elapsedMilliseconds?.let(::DailyElapsedTime)
)

private fun englishCopy(
    dailyName: String = "NumPairs Daily",
    challengeName: String = "4 Pairs · Low",
    completedStatus: String = "Completed",
    completedInStatusFormat: String = "Completed in %1\$s"
): DailyCompletionShareCopy = DailyCompletionShareCopy(
    dailyName = dailyName,
    challengeName = challengeName,
    completedStatus = completedStatus,
    completedInStatusFormat = completedInStatusFormat
)

private fun localizedDate(identity: DailyChallengeId, locale: Locale): String = identity.localDate.format(
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
)
