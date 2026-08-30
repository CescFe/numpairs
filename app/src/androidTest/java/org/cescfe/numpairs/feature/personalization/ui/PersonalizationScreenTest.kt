package org.cescfe.numpairs.feature.personalization.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.cescfe.numpairs.feature.personalization.ExternalUriLaunchResult
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
    fun themeAndToggleControlsEmitTheirRequestedPreferences() {
        var selectedTheme: PersonalizationTheme? = null
        var compactSelectorsEnabled: Boolean? = null
        var hapticsEnabled: Boolean? = null
        setContent(
            onThemeSelected = { theme -> selectedTheme = theme },
            onCompactSelectorsChanged = { enabled -> compactSelectorsEnabled = enabled },
            onHapticsChanged = { enabled -> hapticsEnabled = enabled }
        )

        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.themeOption(PersonalizationTheme.FROST))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.COMPACT_SELECTORS_TOGGLE)
            .performScrollTo()
            .assertIsOff()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    composeTestRule.activity.getString(
                        R.string.personalization_compact_selectors_disabled
                    )
                )
            ).performClick()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.HAPTICS_TOGGLE)
            .performScrollTo()
            .assertIsOn()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(PersonalizationTheme.FROST, selectedTheme)
            assertEquals(true, compactSelectorsEnabled)
            assertEquals(false, hapticsEnabled)
        }
    }

    @Test
    fun compactSelectorsPreferenceDisplaysItsLocalizedExplanationAndEnabledState() {
        setContent(
            preferences = PersonalizationPreferences(
                compactTileSelectorsEnabled = true
            )
        )

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.personalization_compact_selectors_title)
            ).performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.personalization_compact_selectors_supporting_text)
            ).assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.COMPACT_SELECTORS_TOGGLE)
            .assertIsOn()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    composeTestRule.activity.getString(
                        R.string.personalization_compact_selectors_enabled
                    )
                )
            )
    }

    @Test
    fun openSourceActionIsAccessibleAndEmitsTheExternalRequest() {
        var activationCount = 0
        setContent(
            onOpenSourceRepositorySelected = {
                activationCount += 1
                ExternalUriLaunchResult.Launched
            }
        )

        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.OPEN_SOURCE_ACTION)
            .performScrollTo()
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertContentDescriptionEquals(
                composeTestRule.activity.getString(
                    R.string.personalization_open_source_content_description
                )
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Button
                )
            ).performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, activationCount)
        }
    }

    @Test
    fun privacyPolicyActionIsAccessibleAndEmitsTheExternalRequest() {
        var activationCount = 0
        setContent(
            onPrivacyPolicySelected = {
                activationCount += 1
                ExternalUriLaunchResult.Launched
            }
        )

        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.PRIVACY_POLICY_ACTION)
            .performScrollTo()
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertContentDescriptionEquals(
                composeTestRule.activity.getString(
                    R.string.personalization_privacy_policy_content_description
                )
            ).assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Button
                )
            ).performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, activationCount)
        }
    }

    @Test
    fun unavailableOpenSourceHandlerShowsFeedbackAndKeepsSettingsUsable() {
        var hapticsEnabled: Boolean? = null
        setContent(
            onHapticsChanged = { enabled -> hapticsEnabled = enabled },
            onOpenSourceRepositorySelected = { ExternalUriLaunchResult.Unavailable }
        )

        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.OPEN_SOURCE_ACTION)
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(
                    R.string.personalization_open_source_unavailable
                )
            ).assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.HAPTICS_TOGGLE)
            .performScrollTo()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(false, hapticsEnabled)
        }
    }

    @Test
    fun unavailablePrivacyPolicyHandlerShowsFeedbackAndKeepsSettingsUsable() {
        var hapticsEnabled: Boolean? = null
        setContent(
            onHapticsChanged = { enabled -> hapticsEnabled = enabled },
            onPrivacyPolicySelected = { ExternalUriLaunchResult.Unavailable }
        )

        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.PRIVACY_POLICY_ACTION)
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(
                    R.string.personalization_privacy_policy_unavailable
                )
            ).assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.HAPTICS_TOGGLE)
            .performScrollTo()
            .performClick()

        composeTestRule.runOnIdle {
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

        val compactSelectorsBounds = composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.COMPACT_SELECTORS_TOGGLE)
            .performScrollTo()
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(compactSelectorsBounds.height >= minimumTouchTargetPx)
        assertTrue(compactSelectorsBounds.width >= minimumTouchTargetPx)

        val openSourceBounds = composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.OPEN_SOURCE_ACTION)
            .performScrollTo()
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(openSourceBounds.height >= minimumTouchTargetPx)
        assertTrue(openSourceBounds.width >= minimumTouchTargetPx)

        val privacyPolicyBounds = composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.PRIVACY_POLICY_ACTION)
            .performScrollTo()
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(privacyPolicyBounds.height >= minimumTouchTargetPx)
        assertTrue(privacyPolicyBounds.width >= minimumTouchTargetPx)
    }

    private fun setContent(
        preferences: PersonalizationPreferences = PersonalizationPreferences(),
        onThemeSelected: (PersonalizationTheme) -> Unit = {},
        onCompactSelectorsChanged: (Boolean) -> Unit = {},
        onHapticsChanged: (Boolean) -> Unit = {},
        onOpenSourceRepositorySelected: () -> ExternalUriLaunchResult = {
            ExternalUriLaunchResult.Launched
        },
        onPrivacyPolicySelected: () -> ExternalUriLaunchResult = {
            ExternalUriLaunchResult.Launched
        }
    ) {
        composeTestRule.setContent {
            NumPairsTheme {
                PersonalizationScreen(
                    preferences = preferences,
                    onThemeSelected = onThemeSelected,
                    onCompactTileSelectorsEnabledChanged = onCompactSelectorsChanged,
                    onGeneratedGameHapticsEnabledChanged = onHapticsChanged,
                    onOpenSourceRepositorySelected = onOpenSourceRepositorySelected,
                    onPrivacyPolicySelected = onPrivacyPolicySelected,
                    onNavigateBack = {}
                )
            }
        }
    }
}
