package org.cescfe.numpairs.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NumPairsColorScheme = darkColorScheme(
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
)

@Composable
fun NumPairsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NumPairsColorScheme,
        typography = Typography,
        content = content
    )
}
