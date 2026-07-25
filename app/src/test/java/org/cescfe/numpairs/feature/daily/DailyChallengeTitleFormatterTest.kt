package org.cescfe.numpairs.feature.daily

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyChallengeTitleFormatterTest {
    @Test
    fun title_uses_the_captured_identity_and_localizes_only_its_display_date() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2028, 2, 29)
        )
        val locale = Locale.forLanguageTag("es-ES")
        val localizedDate = identity.localDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        )

        val title = DailyChallengeTitleFormatter().format(
            identity = identity,
            dailyName = "Daily",
            challengeName = "4 pares · Baja",
            locale = locale
        )

        assertEquals("Daily · $localizedDate", title.visibleText)
        assertEquals(
            "Daily · $localizedDate, 4 pares · Baja",
            title.accessibilityText
        )
        assertEquals("2028-02-29", identity.canonicalLocalDate)
    }

    @Test
    fun changing_the_locale_does_not_change_the_captured_daily_identity() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )
        val formatter = DailyChallengeTitleFormatter()

        val english = formatter.format(
            identity = identity,
            dailyName = "Daily",
            challengeName = "4 pairs · Low",
            locale = Locale.US
        )
        val catalan = formatter.format(
            identity = identity,
            dailyName = "Daily",
            challengeName = "4 parelles · Baixa",
            locale = Locale.forLanguageTag("ca-ES")
        )

        assertEquals("2026-07-25", identity.canonicalLocalDate)
        assertEquals(true, english.visibleText != catalan.visibleText)
    }

    @Test
    fun title_requires_complete_localized_copy() {
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )

        assertThrows(IllegalArgumentException::class.java) {
            DailyChallengeTitleFormatter().format(
                identity = identity,
                dailyName = " ",
                challengeName = "4 pairs · Low",
                locale = Locale.US
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyChallengeTitleFormatter().format(
                identity = identity,
                dailyName = "Daily",
                challengeName = "",
                locale = Locale.US
            )
        }
    }
}
