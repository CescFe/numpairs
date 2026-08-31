package org.cescfe.numpairs.feature.daily.share

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.feature.daily.DailyElapsedTimeFormatter
import org.cescfe.numpairs.feature.daily.DailyRecipeCatalog
import org.cescfe.numpairs.feature.daily.DailyRecipes

data class DailyCompletionShareCopy(
    val dailyName: String,
    val challengeName: String,
    val completedStatus: String,
    val completedInStatusFormat: String
) {
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
        require(completedInStatusFormat.isNotBlank()) {
            "Daily share completed-in status format must not be blank."
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
    fun format(completion: DailyCompletion, copy: DailyCompletionShareCopy, locale: Locale): DailyCompletionShareText {
        val completedIdentity = completion.identity
        recipeCatalog.resolve(completedIdentity.recipeVersion)
        val localizedDate = completedIdentity.localDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        )
        val completedStatus = completion.elapsedTime?.let { elapsedTime ->
            String.format(
                locale,
                copy.completedInStatusFormat,
                DailyElapsedTimeFormatter.format(elapsedTime)
            )
        } ?: copy.completedStatus
        return DailyCompletionShareText(
            value = "${copy.dailyName} · $localizedDate\n" +
                "${copy.challengeName} · $completedStatus"
        )
    }
}
