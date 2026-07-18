package org.cescfe.numpairs.feature.generated

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier

@StringRes
internal fun GeneratedModeConfiguration.titleResourceIdOrNull(): Int? = when (id) {
    GeneratedModes.FOUR_PAIRS.id -> R.string.four_pairs_screen_title
    GeneratedModes.EIGHT_PAIRS.id -> R.string.eight_pairs_screen_title
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

@Composable
internal fun GeneratedChallenge.localizedTitle(catalog: GeneratedChallengeCatalog): String = stringResource(
    R.string.generated_challenge_title,
    catalog.modeFor(this).localizedTitle(),
    difficulty.localizedTitle()
)
