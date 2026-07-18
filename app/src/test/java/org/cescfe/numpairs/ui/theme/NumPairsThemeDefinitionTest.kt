package org.cescfe.numpairs.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.junit.Assert.assertTrue
import org.junit.Test

class NumPairsThemeDefinitionTest {
    @Test
    fun `theme text colors meet WCAG AA contrast`() {
        NumPairsThemeDefinitions.values.forEach { theme ->
            val appearance = theme.appearanceColors
            val semantic = theme.semanticColors

            mapOf(
                "on background" to (appearance.onBackground to appearance.background),
                "on surface" to (appearance.onSurface to appearance.surface),
                "on surface variant" to (appearance.onSurfaceVariant to appearance.surfaceVariant),
                "on primary" to (appearance.onPrimary to appearance.primary),
                "on primary container" to (appearance.onPrimaryContainer to appearance.primaryContainer),
                "on secondary" to (appearance.onSecondary to appearance.secondary),
                "on secondary container" to (appearance.onSecondaryContainer to appearance.secondaryContainer),
                "on tertiary" to (appearance.onTertiary to appearance.tertiary),
                "on tertiary container" to (appearance.onTertiaryContainer to appearance.tertiaryContainer),
                "on selection" to (semantic.onSelection to semantic.selection),
                "on selection container" to (semantic.onSelectionContainer to semantic.selectionContainer),
                "on success" to (semantic.onSuccess to semantic.success),
                "on success container" to (semantic.onSuccessContainer to semantic.successContainer),
                "on error" to (semantic.onError to semantic.error),
                "on error container" to (semantic.onErrorContainer to semantic.errorContainer),
                "on tutorial highlight" to (semantic.onTutorialHighlight to semantic.tutorialHighlight),
                "on hidden container" to (semantic.onHiddenContainer to semantic.hiddenContainer)
            ).forEach { (name, colors) ->
                assertContrastAtLeast(
                    name = "${theme.id.persistedValue}: $name",
                    foreground = colors.first,
                    background = colors.second,
                    minimum = 4.5f
                )
            }
        }
    }

    @Test
    fun `theme meaningful boundaries meet non-text contrast`() {
        NumPairsThemeDefinitions.values.forEach { theme ->
            val appearance = theme.appearanceColors
            val semantic = theme.semanticColors

            mapOf(
                "success boundary" to (semantic.success to semantic.successContainer),
                "error boundary" to (semantic.error to semantic.errorContainer),
                "error on raised surface" to (semantic.error to appearance.surfaceContainerHigh),
                "selection boundary" to (semantic.selection to semantic.selectionContainer),
                "tutorial highlight" to (semantic.tutorialHighlight to appearance.surfaceContainerHigh),
                "hidden boundary" to (semantic.hiddenBorder to semantic.hiddenContainer)
            ).forEach { (name, colors) ->
                assertContrastAtLeast(
                    name = "${theme.id.persistedValue}: $name",
                    foreground = colors.first,
                    background = colors.second,
                    minimum = 3f
                )
            }
        }
    }

    @Test
    fun `every stable theme identity resolves to one definition`() {
        assertTrue(
            "Definitions must cover every stable theme identity",
            NumPairsThemeDefinitions.keys == PersonalizationTheme.entries.toSet()
        )
    }

    @Test
    fun `semantic roles remain visually distinct`() {
        NumPairsThemeDefinitions.values.forEach { theme ->
            val semantic = theme.semanticColors
            val roleColors = setOf(
                semantic.success,
                semantic.error,
                semantic.selection,
                semantic.tutorialHighlight
            )

            assertTrue(
                "${theme.id.persistedValue}: semantic feedback colors must remain distinct",
                roleColors.size == 4
            )
        }

        assertTrue(
            "Frost selection must not reuse its blue brand primary",
            FrostThemeDefinition.semanticColors.selection != FrostThemeDefinition.appearanceColors.primary
        )
        assertTrue(
            "Frost tutorial feedback must not reuse its gold brand accent",
            FrostThemeDefinition.semanticColors.tutorialHighlight != FrostThemeDefinition.appearanceColors.secondary
        )
        assertTrue(
            "Terminal success must remain distinguishable from its green brand primary",
            TerminalThemeDefinition.semanticColors.success != TerminalThemeDefinition.appearanceColors.primary
        )
        assertTrue(
            "Ember error must remain distinguishable from its orange brand primary",
            EmberThemeDefinition.semanticColors.error != EmberThemeDefinition.appearanceColors.primary
        )
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
