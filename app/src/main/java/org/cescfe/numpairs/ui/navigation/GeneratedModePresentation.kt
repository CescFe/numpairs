package org.cescfe.numpairs.ui.navigation

import androidx.annotation.StringRes
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.generated.GeneratedModeConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedModes

@StringRes
internal fun GeneratedModeConfiguration.titleResourceIdOrNull(): Int? = when (id) {
    GeneratedModes.FOUR_PAIRS.id -> R.string.four_pairs_screen_title
    GeneratedModes.EIGHT_PAIRS.id -> R.string.eight_pairs_screen_title
    else -> null
}
