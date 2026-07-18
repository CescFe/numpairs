package org.cescfe.numpairs.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class NumPairsThemeDefinitionTest {
    @Test
    fun `Warm text colors meet WCAG AA contrast`() {
        val appearance = WarmThemeDefinition.appearanceColors
        val semantic = WarmThemeDefinition.semanticColors

        mapOf(
            "on background" to (appearance.onBackground to appearance.background),
            "on surface" to (appearance.onSurface to appearance.surface),
            "on surface variant" to (appearance.onSurfaceVariant to appearance.surfaceVariant),
            "on primary" to (appearance.onPrimary to appearance.primary),
            "on primary container" to (appearance.onPrimaryContainer to appearance.primaryContainer),
            "on selection" to (semantic.onSelection to semantic.selection),
            "on selection container" to (semantic.onSelectionContainer to semantic.selectionContainer),
            "on success" to (semantic.onSuccess to semantic.success),
            "on success container" to (semantic.onSuccessContainer to semantic.successContainer),
            "on error" to (semantic.onError to semantic.error),
            "on error container" to (semantic.onErrorContainer to semantic.errorContainer),
            "on tutorial highlight" to (semantic.onTutorialHighlight to semantic.tutorialHighlight),
            "on hidden container" to (semantic.onHiddenContainer to semantic.hiddenContainer)
        ).forEach { (name, colors) ->
            assertContrastAtLeast(name, colors.first, colors.second, minimum = 4.5f)
        }
    }

    @Test
    fun `Warm meaningful boundaries meet non-text contrast`() {
        val appearance = WarmThemeDefinition.appearanceColors
        val semantic = WarmThemeDefinition.semanticColors

        mapOf(
            "success boundary" to (semantic.success to semantic.successContainer),
            "error boundary" to (semantic.error to semantic.errorContainer),
            "selection boundary" to (semantic.selection to semantic.selectionContainer),
            "tutorial highlight" to (semantic.tutorialHighlight to appearance.surfaceContainerHigh),
            "hidden boundary" to (semantic.hiddenBorder to semantic.hiddenContainer)
        ).forEach { (name, colors) ->
            assertContrastAtLeast(name, colors.first, colors.second, minimum = 3f)
        }
    }

    private fun assertContrastAtLeast(name: String, foreground: Color, background: Color, minimum: Float) {
        val ratio = contrastRatio(foreground, background)

        assertTrue(
            "$name contrast was $ratio:1, expected at least $minimum:1",
            ratio >= minimum
        )
    }

    private fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())

        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
