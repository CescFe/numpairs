package org.cescfe.numpairs.feature.daily

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyChallengeId

data class DailyChallengeTitle(val visibleText: String, val accessibilityText: String)

class DailyChallengeTitleFormatter {
    fun format(
        identity: DailyChallengeId,
        dailyName: String,
        challengeName: String,
        locale: Locale
    ): DailyChallengeTitle {
        require(dailyName.isNotBlank()) {
            "Daily name must not be blank."
        }
        require(challengeName.isNotBlank()) {
            "Daily challenge name must not be blank."
        }
        val localizedDate = identity.localDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        )
        val visibleText = "$dailyName · $localizedDate"
        return DailyChallengeTitle(
            visibleText = visibleText,
            accessibilityText = "$visibleText, $challengeName"
        )
    }
}
