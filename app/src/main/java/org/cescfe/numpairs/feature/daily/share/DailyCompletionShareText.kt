package org.cescfe.numpairs.feature.daily.share

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.feature.daily.DailyChallengeNameCopy
import org.cescfe.numpairs.feature.daily.DailyChallengeNameFormatter
import org.cescfe.numpairs.feature.daily.DailyCompletionResultFormatter
import org.cescfe.numpairs.feature.daily.DailyElapsedTimeFormatter
import org.cescfe.numpairs.feature.daily.DailyMovementCountFormatter
import org.cescfe.numpairs.feature.daily.DailyRecipeCatalog
import org.cescfe.numpairs.feature.daily.DailyRecipes
import org.cescfe.numpairs.feature.daily.pluralQuantity

data class DailyCompletionShareCopy(
    val dailyName: String,
    val challengeNames: DailyChallengeNameCopy,
    val completedStatus: String,
    val completedResultStatusFormat: String,
    val movementSingularFormat: String,
    val movementPluralFormat: String
) {
    init {
        require(dailyName.isNotBlank()) {
            "Daily share name must not be blank."
        }
        require(completedStatus.isNotBlank()) {
            "Daily share completed status must not be blank."
        }
        require(completedResultStatusFormat.isNotBlank()) {
            "Daily share completed-result status format must not be blank."
        }
        require(movementSingularFormat.isNotBlank()) {
            "Daily share singular movement format must not be blank."
        }
        require(movementPluralFormat.isNotBlank()) {
            "Daily share plural movement format must not be blank."
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
        val challengeName = DailyChallengeNameFormatter(recipeCatalog).format(
            identity = completedIdentity,
            copy = copy.challengeNames
        )
        val localizedDate = completedIdentity.localDate.format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        )
        val formattedElapsedTime = completion.elapsedTime?.let(DailyElapsedTimeFormatter::format)
        val formattedMovementCount = completion.movementCount?.let { movementCount ->
            String.format(
                locale,
                if (movementCount.pluralQuantity() == 1) {
                    copy.movementSingularFormat
                } else {
                    copy.movementPluralFormat
                },
                DailyMovementCountFormatter.format(movementCount)
            )
        }
        val formattedResult = DailyCompletionResultFormatter.format(
            formattedElapsedTime = formattedElapsedTime,
            formattedMovementCount = formattedMovementCount
        )
        val completedStatus = formattedResult?.let { result ->
            String.format(locale, copy.completedResultStatusFormat, result)
        } ?: copy.completedStatus
        return DailyCompletionShareText(
            value = "${copy.dailyName} · $localizedDate\n" +
                "$challengeName · $completedStatus"
        )
    }
}
