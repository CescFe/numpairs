package org.cescfe.numpairs.feature.personalization.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalizationScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysFivePreviewsAndCommunicatesSelectionBeyondColor() {
        setContent(
            preferences = PersonalizationPreferences(
                selectedTheme = PersonalizationTheme.OBSIDIAN
            )
        )

        composeTestRule
            .onAllNodesWithTag(
                testTag = PersonalizationScreenTestTags.THEME_PREVIEW,
                useUnmergedTree = true
            )
            .fetchSemanticsNodes()
            .let { previews -> assertEquals(5, previews.size) }
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.themeOption(PersonalizationTheme.OBSIDIAN))
            .performScrollTo()
            .assertIsSelected()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.themeOption(PersonalizationTheme.WARM))
            .performScrollTo()
            .assertIsNotSelected()
    }

    @Test
    fun themeAndHapticsControlsEmitTheirRequestedPreferences() {
        var selectedTheme: PersonalizationTheme? = null
        var hapticsEnabled: Boolean? = null
        setContent(
            onThemeSelected = { theme -> selectedTheme = theme },
            onHapticsChanged = { enabled -> hapticsEnabled = enabled }
        )

        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.themeOption(PersonalizationTheme.FROST))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.HAPTICS_TOGGLE)
            .performScrollTo()
            .assertIsOn()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(PersonalizationTheme.FROST, selectedTheme)
            assertEquals(false, hapticsEnabled)
        }
    }

    @Test
    fun interactivePreferencesMeetMinimumTouchTarget() {
        setContent()
        val minimumTouchTargetPx = 48 * composeTestRule.activity.resources.displayMetrics.density

        PersonalizationTheme.entries.forEach { theme ->
            val bounds = composeTestRule
                .onNodeWithTag(PersonalizationScreenTestTags.themeOption(theme))
                .performScrollTo()
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot
            assertTrue("${theme.name} touch target was too short", bounds.height >= minimumTouchTargetPx)
            assertTrue("${theme.name} touch target was too narrow", bounds.width >= minimumTouchTargetPx)
        }

        val hapticsBounds = composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.HAPTICS_TOGGLE)
            .performScrollTo()
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(hapticsBounds.height >= minimumTouchTargetPx)
        assertTrue(hapticsBounds.width >= minimumTouchTargetPx)
    }

    private fun setContent(
        preferences: PersonalizationPreferences = PersonalizationPreferences(),
        onThemeSelected: (PersonalizationTheme) -> Unit = {},
        onHapticsChanged: (Boolean) -> Unit = {}
    ) {
        composeTestRule.setContent {
            NumPairsTheme {
                PersonalizationScreen(
                    preferences = preferences,
                    onThemeSelected = onThemeSelected,
                    onGeneratedGameHapticsEnabledChanged = onHapticsChanged,
                    onNavigateBack = {}
                )
            }
        }
    }
}
