package org.cescfe.numpairs.feature.daily.share

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyPersonalBestOutcome
import org.cescfe.numpairs.domain.daily.DailyPersonalBestResult
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
    val movementPluralFormat: String,
    val personalRecordResultFormat: String,
    val personalRecordInvitation: String
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
        require(personalRecordResultFormat.isNotBlank()) {
            "Daily share personal-record result format must not be blank."
        }
        require(personalRecordInvitation.isNotBlank()) {
            "Daily share personal-record invitation must not be blank."
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
        completion: DailyCompletion,
        personalBestResult: DailyPersonalBestResult,
        copy: DailyCompletionShareCopy,
        locale: Locale
    ): DailyCompletionShareText {
        require(personalBestResult.currentElapsedTime == completion.elapsedTime) {
            "Daily sharing must use the completion's stable personal-best outcome."
        }
        val completedIdentity = completion.identity
        val challenge = recipeCatalog.challengeFor(completedIdentity)
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
        if (personalBestResult.outcome == DailyPersonalBestOutcome.PERSONAL_RECORD) {
            require(personalBestResult.category?.generatedChallengeId == challenge.id.value) {
                "A shared Daily personal record must belong to the completion's challenge."
            }
            val personalRecordResult = requireNotNull(formattedResult) {
                "A shared Daily personal record requires its authoritative duration."
            }
            return DailyCompletionShareText(
                value = "${copy.dailyName} · $localizedDate · $challengeName\n" +
                    "${String.format(locale, copy.personalRecordResultFormat, personalRecordResult)}\n" +
                    copy.personalRecordInvitation
            )
        }
        val completedStatus = formattedResult?.let { result ->
            String.format(locale, copy.completedResultStatusFormat, result)
        } ?: copy.completedStatus
        return DailyCompletionShareText(
            value = "${copy.dailyName} · $localizedDate\n" +
                "$challengeName · $completedStatus"
        )
    }
}
