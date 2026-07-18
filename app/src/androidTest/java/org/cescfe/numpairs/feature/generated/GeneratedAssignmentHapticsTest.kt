package org.cescfe.numpairs.feature.generated

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.data.generated.selection.FakeGeneratedDifficultySelectionRepository
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.navigation.navigateToSelectedGeneratedChallenge
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneratedAssignmentHapticsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun generatedAssignmentsRequestConfirmationOnlyWhileThePreferenceIsEnabled() {
        val hapticFeedback = RecordingHapticFeedback()
        val preferencesRepository = FakePersonalizationPreferencesRepository()
        setAppContent(
            hapticFeedback = hapticFeedback,
            preferencesRepository = preferencesRepository
        )
        navigateToGeneratedMode(MenuScreenTestTags.FOUR_PAIRS_BUTTON)

        assignFirstTileOperator(Operator.ADDITION)
        assertEquals(listOf(HapticFeedbackType.Confirm), hapticFeedback.requestedTypes)

        runBlocking {
            preferencesRepository.setGeneratedGameHapticsEnabled(false)
        }
        composeTestRule.waitForIdle()
        assignFirstTileOperator(Operator.MULTIPLICATION)
        assertEquals(1, hapticFeedback.requestedTypes.size)

        runBlocking {
            preferencesRepository.setGeneratedGameHapticsEnabled(true)
        }
        composeTestRule.waitForIdle()
        assignFirstTileOperator(Operator.ADDITION)
        assertEquals(
            listOf(HapticFeedbackType.Confirm, HapticFeedbackType.Confirm),
            hapticFeedback.requestedTypes
        )

        assignFirstTileOperator(Operator.ADDITION)
        enterFirstStripValue()
        assertEquals(2, hapticFeedback.requestedTypes.size)
    }

    @Test
    fun eightPairsUsesTheSameGeneratedAssignmentHaptic() {
        val hapticFeedback = RecordingHapticFeedback()
        setAppContent(hapticFeedback = hapticFeedback)
        navigateToGeneratedMode(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)

        assignFirstTileOperator(Operator.ADDITION)

        assertEquals(listOf(HapticFeedbackType.Confirm), hapticFeedback.requestedTypes)
    }

    @Test
    fun genericGameRouteDoesNotRequestGeneratedAssignmentHaptics() {
        val hapticFeedback = RecordingHapticFeedback()

        composeTestRule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides hapticFeedback) {
                NumPairsTheme {
                    GameRoute(
                        title = "Tutorial",
                        initialPuzzle = samplePuzzle,
                        gameSessionKey = "generic-route-haptics"
                    )
                }
            }
        }

        assignFirstTileOperator(Operator.ADDITION)

        assertEquals(emptyList<HapticFeedbackType>(), hapticFeedback.requestedTypes)
    }

    private fun setAppContent(
        hapticFeedback: RecordingHapticFeedback,
        preferencesRepository: FakePersonalizationPreferencesRepository =
            FakePersonalizationPreferencesRepository()
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides hapticFeedback) {
                NumPairsTheme {
                    AppNavigation(
                        onboardingRepository = FakeOnboardingRepository(),
                        generatedSessionRepository = FakeGeneratedSessionRepository(),
                        generatedDifficultySelectionRepository = FakeGeneratedDifficultySelectionRepository(),
                        personalizationPreferencesRepository = preferencesRepository,
                        topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                        generatedChallengeCatalog = GeneratedModes.catalog,
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
        }
    }

    private fun navigateToGeneratedMode(menuButtonTag: String) {
        composeTestRule.navigateToSelectedGeneratedChallenge(menuButtonTag)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun assignFirstTileOperator(operator: Operator) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun enterFirstStripValue() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("1")
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performImeAction()
        composeTestRule.waitForIdle()
    }

    private class RecordingHapticFeedback : HapticFeedback {
        val requestedTypes = mutableListOf<HapticFeedbackType>()

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            requestedTypes += hapticFeedbackType
        }
    }
}
