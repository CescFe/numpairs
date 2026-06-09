package org.cescfe.numpairs.feature.tutorial

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.game.ui.GameScreenTestTags
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TutorialRouteTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectingTutorialFromMenuOpensTheWalkthroughOnStepOne() {
        setContent()

        navigateToTutorial()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.INSTRUCTION_SURFACE)
            .assertIsDisplayed()
        assertStepDisplayed(stepIndex = 0)
        assertOnePairOrientationScenarioDisplayed()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_BUTTON)
            .assertIsNotEnabled()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_BUTTON)
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertDoesNotExist()
    }

    @Test
    fun playerCanMoveForwardAndBackwardManuallyBetweenTutorialSteps() {
        setContent()
        navigateToTutorial()

        tapNextStep()
        assertStepDisplayed(stepIndex = 1)
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_BUTTON)
            .assertIsEnabled()

        tapPreviousStep()
        assertStepDisplayed(stepIndex = 0)
    }

    @Test
    fun changingStepsSwitchesPuzzleScenariosAndResetsScenarioGameState() {
        setContent()
        navigateToTutorial()

        enterFirstScenarioHiddenStripValue()
        tapNextStep()
        tapNextStep()

        assertStepDisplayed(stepIndex = 2)
        assertTwoPairPracticeScenarioDisplayed()

        repeat(3) {
            tapNextStep()
        }
        assertStepDisplayed(stepIndex = 5)
        assertFinalEasyFourPairsScenarioDisplayed()

        repeat(5) {
            tapPreviousStep()
        }
        assertStepDisplayed(stepIndex = 0)
        assertOnePairOrientationScenarioDisplayed()
    }

    @Test
    fun routeLevelBackFromTutorialReturnsToTheMenu() {
        setContent()
        navigateToTutorial()

        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    private fun setContent() {
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation()
            }
        }
    }

    private fun navigateToTutorial() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun tapNextStep() {
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_BUTTON)
            .performScrollTo()
            .performClick()
    }

    private fun tapPreviousStep() {
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_BUTTON)
            .performScrollTo()
            .performClick()
    }

    private fun enterFirstScenarioHiddenStripValue() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("3")
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_player_entered_content_description,
                    "3"
                )
            )
    }

    private fun assertStepDisplayed(stepIndex: Int) {
        val step = TutorialMvpContent.steps[stepIndex]

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_INDICATOR)
            .performScrollTo()
            .assert(
                hasText(
                    string(
                        R.string.tutorial_step_indicator,
                        step.order,
                        TutorialMvpContent.steps.size
                    )
                )
            )
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_COPY)
            .performScrollTo()
            .assert(hasText(step.playerFacingCopy))
    }

    private fun assertOnePairOrientationScenarioDisplayed() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .performScrollTo()
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_known_content_description,
                    "2"
                )
            )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(string(R.string.strip_item_hidden_content_description))
        assertTileResult(tileIndex = 0, result = 5)
        assertTileResult(tileIndex = 1, result = 6)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(2), useUnmergedTree = true)
            .assertDoesNotExist()
    }

    private fun assertTwoPairPracticeScenarioDisplayed() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .performScrollTo()
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_known_content_description,
                    "1"
                )
            )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(string(R.string.strip_item_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(3))
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_known_content_description,
                    "4"
                )
            )
        assertTileResult(tileIndex = 2, result = 7)
        assertTileResult(tileIndex = 3, result = 12)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(4), useUnmergedTree = true)
            .assertDoesNotExist()
    }

    private fun assertFinalEasyFourPairsScenarioDisplayed() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(7))
            .performScrollTo()
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_known_content_description,
                    "8"
                )
            )
        assertTileResult(tileIndex = 6, result = 15)
        assertTileResult(tileIndex = 7, result = 56)
    }

    private fun assertTileResult(tileIndex: Int, result: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(tileIndex), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText(result.toString())))
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }
}
