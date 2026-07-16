package org.cescfe.numpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.feature.tutorial.TutorialContent
import org.cescfe.numpairs.feature.tutorial.TutorialMode
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationLearningFlowTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun menuTutorialLaunchesTheCombinedIntroductionAndBackReturnsToMenu() {
        setContent(puzzleProvider = QueueGeneratedPuzzleProvider(samplePuzzle))

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.INSTRUCTION_SURFACE)
            .assertIsDisplayed()

        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    @Test
    fun inGameLearningActionsOpenTutorialOverlaysAndReturnToCurrentFourPairsScreen() {
        val puzzleProvider = QueueGeneratedPuzzleProvider(samplePuzzle)
        setContent(puzzleProvider = puzzleProvider)

        navigateToFourPairs()
        enterPreservedStripValue()
        assertPreservedStripItemPlayerEntered()
        assertEquals(1, puzzleProvider.requestCount)

        openLearnBasicsFromRulesHelper()
        closeOverlayAndAssertCurrentFourPairsScreen()

        openSolvingTipsPracticeFromHintAction()
        closeOverlayAndAssertCurrentFourPairsScreen()
        assertEquals(1, puzzleProvider.requestCount)
    }

    private fun setContent(puzzleProvider: QueueGeneratedPuzzleProvider) {
        val actionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository()

        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    topAppBarActionDiscoveryRepository = actionDiscoveryRepository,
                    generatedModeRegistry = GeneratedModes.registry,
                    generatedPuzzleGenerationUseCaseFactory = fourPairsProviderFactory(puzzleProvider = puzzleProvider)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun navigateToFourPairs() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    private fun openLearnBasicsFromRulesHelper() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_PLAY_TUTORIAL_BUTTON)
            .assertIsDisplayed()
            .performClick()

        assertTutorialOverlayMode(TutorialMode.LEARN_BASICS)
    }

    private fun openSolvingTipsPracticeFromHintAction() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.HINT_ACTION)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_PRACTICE_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertDoesNotExist()

        assertTutorialOverlayMode(TutorialMode.SOLVING_TIPS_PRACTICE)
    }

    private fun assertTutorialOverlayMode(mode: TutorialMode) {
        val steps = TutorialContent.stepsFor(mode)

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_INDICATOR)
            .performScrollTo()
            .assert(
                hasText(
                    string(
                        R.string.tutorial_step_indicator,
                        1,
                        steps.size
                    )
                )
            )
    }

    private fun closeOverlayAndAssertCurrentFourPairsScreen() {
        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
        assertPreservedStripItemPlayerEntered()
    }

    private fun enterPreservedStripValue() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(PRESERVED_STRIP_ITEM_INDEX))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput(PRESERVED_STRIP_ITEM_VALUE)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performImeAction()
    }

    private fun assertPreservedStripItemPlayerEntered() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(PRESERVED_STRIP_ITEM_INDEX))
            .performScrollTo()
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_player_entered_content_description,
                    PRESERVED_STRIP_ITEM_VALUE
                )
            )
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }

    private fun fourPairsProviderFactory(
        puzzleProvider: QueueGeneratedPuzzleProvider
    ): GeneratedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory { mode ->
        require(mode == GeneratedModes.FOUR_PAIRS)
        GeneratedPuzzleGenerationUseCase { request ->
            GeneratedPuzzleGenerationResult.Generated(
                request = request,
                initialPuzzle = puzzleProvider.nextPuzzle()
            )
        }
    }

    private class QueueGeneratedPuzzleProvider(private val puzzle: Puzzle) {
        var requestCount = 0
            private set

        fun nextPuzzle(): Puzzle {
            requestCount += 1
            return puzzle
        }
    }

    private companion object {
        const val PRESERVED_STRIP_ITEM_INDEX = 1
        const val PRESERVED_STRIP_ITEM_VALUE = "2"
    }
}
