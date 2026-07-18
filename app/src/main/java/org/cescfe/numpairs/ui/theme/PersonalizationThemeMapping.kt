package org.cescfe.numpairs.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import org.cescfe.numpairs.data.preferences.PersonalizationTheme

@Immutable
internal data class NumPairsThemePreviewColors(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val accent: Color,
    val outline: Color
)

internal fun PersonalizationTheme.previewColors(): NumPairsThemePreviewColors {
    val colors = definition().appearanceColors

    return NumPairsThemePreviewColors(
        background = colors.background,
        surface = colors.surfaceContainerHigh,
        primary = colors.primary,
        accent = colors.secondary,
        outline = colors.outline
    )
}
