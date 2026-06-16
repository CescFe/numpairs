package org.cescfe.numpairs.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NumPairsColorScheme = darkColorScheme(
    primary = NumPairsJade,
    onPrimary = NumPairsOnJade,
    primaryContainer = NumPairsJadeSoft,
    onPrimaryContainer = NumPairsOnJadeSoft,
    secondary = NumPairsJade,
    onSecondary = NumPairsOnJade,
    secondaryContainer = NumPairsSurfaceSubtle,
    onSecondaryContainer = NumPairsOnSurface,
    tertiary = NumPairsJade,
    onTertiary = NumPairsOnJade,
    tertiaryContainer = NumPairsJadeSoft,
    onTertiaryContainer = NumPairsOnJadeSoft,
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
    scrim = NumPairsBackground
)

@Composable
fun NumPairsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NumPairsColorScheme,
        typography = Typography,
        content = content
    )
}
