package org.cescfe.numpairs.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

internal enum class NumPairsThemeId(val persistedValue: String) {
    WARM("warm")
}

@Immutable
internal data class NumPairsSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val selection: Color,
    val onSelection: Color,
    val selectionContainer: Color,
    val onSelectionContainer: Color,
    val tutorialHighlight: Color,
    val onTutorialHighlight: Color,
    val hiddenContainer: Color,
    val onHiddenContainer: Color,
    val hiddenBorder: Color
)

@Immutable
internal data class NumPairsThemeDefinition(
    val id: NumPairsThemeId,
    val appearanceColors: ColorScheme,
    val semanticColors: NumPairsSemanticColors
)

internal val WarmThemeDefinition = NumPairsThemeDefinition(
    id = NumPairsThemeId.WARM,
    appearanceColors = darkColorScheme(
        primary = NumPairsGreen,
        onPrimary = NumPairsOnGreen,
        primaryContainer = NumPairsGreenSoft,
        onPrimaryContainer = NumPairsOnGreenSoft,
        secondary = NumPairsFocus,
        onSecondary = NumPairsOnFocus,
        secondaryContainer = NumPairsSurfaceSubtle,
        onSecondaryContainer = NumPairsOnSurface,
        tertiary = NumPairsSand,
        onTertiary = NumPairsOnSand,
        tertiaryContainer = NumPairsSurfaceSubtle,
        onTertiaryContainer = NumPairsOnSurface,
        background = NumPairsBackground,
        onBackground = NumPairsOnSurface,
        surface = NumPairsSurface,
        onSurface = NumPairsOnSurface,
        surfaceVariant = NumPairsSurfaceSubtle,
        onSurfaceVariant = NumPairsOnSurfaceVariant,
        surfaceContainer = NumPairsSurface,
        surfaceContainerHigh = NumPairsSurfaceRaised,
        surfaceContainerHighest = NumPairsSurfaceRaised,
        outline = NumPairsOutline,
        outlineVariant = NumPairsOutlineVariant,
        error = NumPairsError,
        onError = NumPairsBackground,
        errorContainer = NumPairsErrorSoft,
        onErrorContainer = NumPairsOnErrorSoft,
        scrim = NumPairsScrim
    ),
    semanticColors = NumPairsSemanticColors(
        success = NumPairsGreen,
        onSuccess = NumPairsOnGreen,
        successContainer = NumPairsGreenSoft,
        onSuccessContainer = NumPairsOnGreenSoft,
        error = NumPairsError,
        onError = NumPairsBackground,
        errorContainer = NumPairsErrorSoft,
        onErrorContainer = NumPairsOnErrorSoft,
        selection = NumPairsFocus,
        onSelection = NumPairsOnFocus,
        selectionContainer = NumPairsSurfaceSubtle,
        onSelectionContainer = NumPairsOnSurface,
        tutorialHighlight = NumPairsSand,
        onTutorialHighlight = NumPairsOnSand,
        hiddenContainer = NumPairsSurfaceSubtle,
        onHiddenContainer = NumPairsOnSurfaceVariant,
        hiddenBorder = NumPairsOnSurfaceVariant
    )
)
