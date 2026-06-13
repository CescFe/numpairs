package org.cescfe.numpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.feature.fourpairs.FourPairsPuzzleProvider
import org.cescfe.numpairs.feature.game.ui.GameScreenTestTags
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.feature.tutorial.TutorialMode
import org.cescfe.numpairs.feature.tutorial.TutorialMvpContent
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
    fun inGameLearningActionsOpenTutorialOverlaysAndReturnToCurrentFourPairsScreen() {
        val puzzleProvider = QueueFourPairsPuzzleProvider(initialPuzzle)
        setContent(puzzleProvider = puzzleProvider)

        navigateToFourPairs()
        enterPreservedStripValue()
        assertPreservedStripItemPlayerEntered()
        assertEquals(1, puzzleProvider.requestCount)

        openLearnBasicsFromRulesHelper()
        closeOverlayAndAssertCurrentFourPairsScreen()

        openPracticeFullPuzzleFromHintAction()
        closeOverlayAndAssertCurrentFourPairsScreen()
        assertEquals(1, puzzleProvider.requestCount)
    }

    private fun setContent(puzzleProvider: FourPairsPuzzleProvider) {
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(fourPairsPuzzleProvider = puzzleProvider)
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

    private fun openPracticeFullPuzzleFromHintAction() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.HINT_ACTION)
            .assertIsDisplayed()
            .performClick()

        assertTutorialOverlayMode(TutorialMode.PRACTICE_FULL_PUZZLE)
    }

    private fun assertTutorialOverlayMode(mode: TutorialMode) {
        val steps = TutorialMvpContent.stepsFor(mode)

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
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()
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

    private class QueueFourPairsPuzzleProvider(private val puzzle: Puzzle) : FourPairsPuzzleProvider {
        var requestCount = 0
            private set

        override fun nextPuzzle(): Puzzle {
            requestCount += 1
            return puzzle
        }
    }

    private companion object {
        const val PRESERVED_STRIP_ITEM_INDEX = 1
        const val PRESERVED_STRIP_ITEM_VALUE = "2"
    }
}
