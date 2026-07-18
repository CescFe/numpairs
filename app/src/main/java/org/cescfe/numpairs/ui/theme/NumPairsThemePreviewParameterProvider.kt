package org.cescfe.numpairs.ui.theme

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.cescfe.numpairs.data.preferences.PersonalizationTheme

internal class NumPairsThemePreviewParameterProvider : PreviewParameterProvider<PersonalizationTheme> {
    override val values: Sequence<PersonalizationTheme>
        get() = PersonalizationTheme.entries.asSequence()
}
