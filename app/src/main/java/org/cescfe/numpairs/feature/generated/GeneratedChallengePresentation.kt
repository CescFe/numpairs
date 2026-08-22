package org.cescfe.numpairs.feature.generated

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier

@StringRes
internal fun GeneratedModeConfiguration.titleResourceIdOrNull(): Int? = when (id) {
    GeneratedModes.THREE_PAIRS.id,
    GeneratedModes.FOUR_PAIRS.id -> R.string.quick_screen_title

    GeneratedModes.EIGHT_PAIRS.id -> R.string.classic_screen_title

    else -> null
}

@Composable
internal fun GeneratedModeConfiguration.localizedTitle(): String = titleResourceIdOrNull()?.let { titleResourceId ->
    stringResource(id = titleResourceId)
} ?: id.value

@Composable
internal fun DifficultyTier.localizedTitle(): String = stringResource(
    id = when (this) {
        DifficultyTier.LOW -> R.string.generated_difficulty_low
        DifficultyTier.MEDIUM -> R.string.generated_difficulty_medium
        DifficultyTier.HARD -> R.string.generated_difficulty_hard
    }
)

@StringRes
internal fun GeneratedPlayOptionConfiguration.titleResourceId(): Int = when (id) {
    GeneratedPlayOptions.QUICK.id -> R.string.quick_screen_title
    GeneratedPlayOptions.CLASSIC.id -> R.string.classic_screen_title
    else -> error("No title is configured for generated play option ${id.value}.")
}

@Composable
internal fun GeneratedPlayOptionConfiguration.localizedTitle(): String = stringResource(titleResourceId())

@Composable
internal fun GeneratedChallenge.localizedTitle(catalog: GeneratedChallengeCatalog): String = stringResource(
    R.string.generated_challenge_title,
    catalog.modeFor(this).localizedTitle(),
    difficulty.localizedTitle()
)

@Composable
internal fun GeneratedPlayRequest.localizedTitle(): String = stringResource(
    R.string.generated_challenge_title,
    GeneratedPlayOptions.resolve(optionId).localizedTitle(),
    difficulty.localizedTitle()
)
