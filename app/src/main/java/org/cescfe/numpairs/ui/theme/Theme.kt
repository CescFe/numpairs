package org.cescfe.numpairs.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import org.cescfe.numpairs.data.preferences.PersonalizationTheme

private val LocalNumPairsSemanticColors = staticCompositionLocalOf {
    WarmThemeDefinition.semanticColors
}

internal val MaterialTheme.numPairsSemanticColors: NumPairsSemanticColors
    @Composable
    @ReadOnlyComposable
    get() = LocalNumPairsSemanticColors.current

@Composable
fun NumPairsTheme(content: @Composable () -> Unit) {
    NumPairsTheme(
        theme = PersonalizationTheme.WARM,
        content = content
    )
}

@Composable
internal fun NumPairsTheme(theme: PersonalizationTheme, content: @Composable () -> Unit) {
    val themeDefinition = theme.definition()

    CompositionLocalProvider(
        LocalNumPairsSemanticColors provides themeDefinition.semanticColors
    ) {
        MaterialTheme(
            colorScheme = themeDefinition.appearanceColors,
            typography = Typography,
            content = content
        )
    }
}
