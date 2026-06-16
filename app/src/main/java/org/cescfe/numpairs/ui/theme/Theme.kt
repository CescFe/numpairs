package org.cescfe.numpairs.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NumPairsColorScheme = lightColorScheme(
    primary = NumPairsGreen,
    onPrimary = NumPairsOnGreen,
    primaryContainer = NumPairsGreenSoft,
    onPrimaryContainer = NumPairsOnGreenSoft,

    secondary = NumPairsSand,
    onSecondary = NumPairsOnSand,
    secondaryContainer = NumPairsSurfaceSubtle,
    onSecondaryContainer = NumPairsOnSurface,

    tertiary = NumPairsGreenSoft,
    onTertiary = NumPairsOnGreenSoft,
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
    onError = Color.White,
    errorContainer = NumPairsErrorSoft,
    onErrorContainer = NumPairsOnErrorSoft,
)

@Composable
fun NumPairsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NumPairsColorScheme,
        typography = Typography,
        content = content
    )
}
