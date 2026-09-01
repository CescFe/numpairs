package org.cescfe.numpairs.feature.daily.share

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.daily.DailyCompletion

data class DailyCompletionSharePayload(val text: DailyCompletionShareText, val chooserTitle: String) {
    init {
        require(chooserTitle.isNotBlank()) {
            "Daily share chooser title must not be blank."
        }
    }
}

class AndroidDailyCompletionSharePayloadFactory(
    private val resources: Resources,
    private val formatter: DailyCompletionShareTextFormatter = DailyCompletionShareTextFormatter()
) {
    fun create(completion: DailyCompletion): DailyCompletionSharePayload {
        val locale = resources.configuration.locales[0]
        val fourPairsName = resources.getString(R.string.four_pairs_screen_title)
        val lowDifficultyName = resources.getString(R.string.generated_difficulty_low)
        return DailyCompletionSharePayload(
            text = formatter.format(
                completion = completion,
                copy = DailyCompletionShareCopy(
                    dailyName = resources.getString(R.string.daily_share_name),
                    challengeName = resources.getString(
                        R.string.generated_challenge_title,
                        fourPairsName,
                        lowDifficultyName
                    ),
                    completedStatus = resources.getString(
                        R.string.daily_share_completed_status
                    ),
                    completedResultStatusFormat = resources.getString(
                        R.string.daily_share_completed_in_status
                    ),
                    movementSingularFormat = resources.getQuantityString(
                        R.plurals.daily_movement_count,
                        1
                    ),
                    movementPluralFormat = resources.getQuantityString(
                        R.plurals.daily_movement_count,
                        2
                    )
                ),
                locale = locale
            ),
            chooserTitle = resources.getString(R.string.daily_share_chooser_title)
        )
    }
}

sealed interface DailyCompletionShareLaunchResult {
    data object Launched : DailyCompletionShareLaunchResult

    data object Unavailable : DailyCompletionShareLaunchResult
}

fun interface DailyCompletionShareLauncher {
    fun launch(payload: DailyCompletionSharePayload): DailyCompletionShareLaunchResult
}

class AndroidDailyCompletionShareLauncher(private val context: Context) : DailyCompletionShareLauncher {
    override fun launch(payload: DailyCompletionSharePayload): DailyCompletionShareLaunchResult = try {
        val chooserIntent = DailyCompletionShareIntentFactory.create(payload).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(chooserIntent)
        DailyCompletionShareLaunchResult.Launched
    } catch (_: ActivityNotFoundException) {
        DailyCompletionShareLaunchResult.Unavailable
    }
}

object DailyCompletionShareIntentFactory {
    fun create(payload: DailyCompletionSharePayload): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = DAILY_SHARE_MIME_TYPE
            putExtra(Intent.EXTRA_TEXT, payload.text.value)
        }
        return Intent.createChooser(sendIntent, payload.chooserTitle)
    }
}

internal const val DAILY_SHARE_MIME_TYPE = "text/plain"
