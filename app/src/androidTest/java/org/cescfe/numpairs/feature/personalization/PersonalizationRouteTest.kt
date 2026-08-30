package org.cescfe.numpairs.feature.personalization

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.feature.personalization.ui.PersonalizationScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalizationRouteTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun compactSelectorsControlUpdatesThePersistedPreference() {
        val repository = FakePersonalizationPreferencesRepository()

        composeTestRule.setContent {
            NumPairsTheme {
                PersonalizationRoute(
                    repository = repository,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.COMPACT_SELECTORS_TOGGLE)
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil {
            repository.state.value.compactTileSelectorsEnabled
        }
    }

    @Test
    fun externalActionsLaunchTheirCanonicalUrlsWithoutChangingPreferences() {
        val initialPreferences = PersonalizationPreferences()
        val repository = FakePersonalizationPreferencesRepository(initialPreferences)
        val launchedUris = mutableListOf<String>()

        composeTestRule.setContent {
            NumPairsTheme {
                PersonalizationRoute(
                    repository = repository,
                    onNavigateBack = {},
                    externalUriLauncher = ExternalUriLauncher { uri ->
                        launchedUris += uri
                        ExternalUriLaunchResult.Launched
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.OPEN_SOURCE_ACTION)
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.PRIVACY_POLICY_ACTION)
            .performScrollTo()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                listOf(OPEN_SOURCE_REPOSITORY_URL, PRIVACY_POLICY_URL),
                launchedUris
            )
            assertEquals(initialPreferences, repository.state.value)
        }
    }

    @Test
    fun unavailableRepositoryHandlerCommunicatesFailureWithoutChangingPreferences() {
        val initialPreferences = PersonalizationPreferences()
        val repository = FakePersonalizationPreferencesRepository(initialPreferences)

        composeTestRule.setContent {
            NumPairsTheme {
                PersonalizationRoute(
                    repository = repository,
                    onNavigateBack = {},
                    externalUriLauncher = ExternalUriLauncher {
                        ExternalUriLaunchResult.Unavailable
                    }
                )
            }
        }

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

        composeTestRule.runOnIdle {
            assertEquals(initialPreferences, repository.state.value)
        }
    }
}
