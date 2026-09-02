package org.cescfe.numpairs.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.R

@Composable
internal fun StandardCompletionCelebration.localizedCopy(): GameSuccessOverlayCopy = when (this) {
    StandardCompletionCelebration.GREAT_WORK -> GameSuccessOverlayCopy(
        message = stringResource(R.string.completion_celebration_great_work_title),
        supportingText = stringResource(R.string.completion_celebration_great_work_supporting_text)
    )

    StandardCompletionCelebration.EXCELLENT -> GameSuccessOverlayCopy(
        message = stringResource(R.string.completion_celebration_excellent_title),
        supportingText = stringResource(R.string.completion_celebration_excellent_supporting_text)
    )

    StandardCompletionCelebration.YOU_ROCK -> GameSuccessOverlayCopy(
        message = stringResource(R.string.completion_celebration_you_rock_title),
        supportingText = stringResource(R.string.completion_celebration_you_rock_supporting_text)
    )

    StandardCompletionCelebration.NAILED_IT -> GameSuccessOverlayCopy(
        message = stringResource(R.string.completion_celebration_nailed_it_title),
        supportingText = stringResource(R.string.completion_celebration_nailed_it_supporting_text)
    )

    StandardCompletionCelebration.BRILLIANT -> GameSuccessOverlayCopy(
        message = stringResource(R.string.completion_celebration_brilliant_title),
        supportingText = stringResource(R.string.completion_celebration_brilliant_supporting_text)
    )

    StandardCompletionCelebration.KEEP_IT_UP -> GameSuccessOverlayCopy(
        message = stringResource(R.string.completion_celebration_keep_it_up_title),
        supportingText = stringResource(R.string.completion_celebration_keep_it_up_supporting_text)
    )

    StandardCompletionCelebration.MEDIUM_HARD_IMPRESSIVE -> GameSuccessOverlayCopy(
        message = stringResource(R.string.completion_celebration_impressive_title),
        supportingText = stringResource(R.string.completion_celebration_impressive_supporting_text)
    )

    StandardCompletionCelebration.HARD_UNSTOPPABLE -> GameSuccessOverlayCopy(
        message = stringResource(R.string.completion_celebration_unstoppable_title),
        supportingText = stringResource(R.string.completion_celebration_unstoppable_supporting_text)
    )
}
