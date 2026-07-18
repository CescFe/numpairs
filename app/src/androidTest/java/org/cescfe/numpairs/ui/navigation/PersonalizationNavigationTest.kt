package org.cescfe.numpairs.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.PersonalizationThemeProvider
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.feature.personalization.ui.PersonalizationScreenTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonalizationNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun personalizationUpdatesPreferencesAndThemeWithoutReplacingGeneratedSession() {
        val snapshot = GeneratedSessionSnapshot(
            sessionId = GeneratedSessionId("personalization-session"),
            modeId = GeneratedModes.FOUR_PAIRS.id.value,
            profileId = GeneratedModes.FOUR_PAIRS.profile.id.value,
            seed = 919,
            initialPuzzle = samplePuzzle,
            currentPuzzle = samplePuzzle
        )
        val generatedSessionRepository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val personalizationRepository = FakePersonalizationPreferencesRepository()
        var latestPrimary = Color.Unspecified

        composeTestRule.setContent {
            PersonalizationThemeProvider(personalizationRepository) {
                val primary = MaterialTheme.colorScheme.primary
                SideEffect {
                    latestPrimary = primary
                }
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = generatedSessionRepository,
                    personalizationPreferencesRepository = personalizationRepository,
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedModeRegistry = GeneratedModes.registry,
                    generatedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory {
                        GeneratedPuzzleGenerationUseCase { request ->
                            GeneratedPuzzleGenerationResult.Generated(
                                request = request,
                                initialPuzzle = samplePuzzle
                            )
                        }
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.PERSONALIZATION_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.SCREEN)
            .assertIsDisplayed()
        PersonalizationTheme.entries.forEach { theme ->
            composeTestRule
                .onNodeWithTag(PersonalizationScreenTestTags.themeOption(theme))
                .performScrollTo()
                .performClick()
            composeTestRule.runOnIdle {
                assertEquals(theme, personalizationRepository.state.value.selectedTheme)
                assertEquals(snapshot, generatedSessionRepository.session.value)
            }
        }
        composeTestRule
            .onNodeWithTag(PersonalizationScreenTestTags.HAPTICS_TOGGLE)
            .performScrollTo()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(PersonalizationTheme.EMBER, personalizationRepository.state.value.selectedTheme)
            assertEquals(false, personalizationRepository.state.value.generatedGameHapticsEnabled)
            assertEquals(Color(0xFFA33A00), latestPrimary)
            assertEquals(snapshot, generatedSessionRepository.session.value)
        }

        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(snapshot, generatedSessionRepository.session.value)
        }
    }
}
