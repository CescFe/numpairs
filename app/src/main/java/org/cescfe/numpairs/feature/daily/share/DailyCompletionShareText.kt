package org.cescfe.numpairs.feature.daily.share

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.feature.daily.DailyRecipeCatalog
import org.cescfe.numpairs.feature.daily.DailyRecipes

data class DailyCompletionShareCopy(val dailyName: String, val challengeName: String, val completedStatus: String) {
    init {
        require(dailyName.isNotBlank()) {
            "Daily share name must not be blank."
        }
        require(challengeName.isNotBlank()) {
            "Daily share challenge name must not be blank."
        }
        require(completedStatus.isNotBlank()) {
            "Daily share completed status must not be blank."
        }
    }
}

@JvmInline
value class DailyCompletionShareText(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Daily completion share text must not be blank."
        }
    }
}

class DailyCompletionShareTextFormatter(private val recipeCatalog: DailyRecipeCatalog = DailyRecipes.catalog) {
    fun format(
        completedIdentity: DailyChallengeId,
        copy: DailyCompletionShareCopy,
        locale: Locale
    ): DailyCompletionShareText {
        recipeCatalog.resolve(completedIdentity.recipeVersion)
        val localizedDate = completedIdentity.localDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        )
        return DailyCompletionShareText(
            value = "${copy.dailyName} · $localizedDate\n" +
                "${copy.challengeName} · ${copy.completedStatus}"
        )
    }
}
