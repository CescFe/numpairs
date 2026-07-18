package org.cescfe.numpairs.ui.theme

import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class NumPairsThemePreviewParameterProviderTest {
    @Test
    fun `preview parameters contain every personalization theme exactly once`() {
        val themes = NumPairsThemePreviewParameterProvider().values.toList()

        assertEquals(PersonalizationTheme.entries, themes)
        assertEquals(themes.size, themes.distinct().size)
    }
}
