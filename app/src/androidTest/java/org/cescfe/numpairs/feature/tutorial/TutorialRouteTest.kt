package org.cescfe.numpairs.feature.tutorial

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasNoClickAction
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
import org.cescfe.numpairs.domain.puzzle.Operator
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

        tapNextStep()
        tapNextStep()

        assertStepDisplayed(stepIndex = 2)
        assertTwoPairPracticeScenarioDisplayed()
        enterStripValue(index = 1, value = "2")

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

        repeat(2) {
            tapNextStep()
        }
        assertStepDisplayed(stepIndex = 2)
        assertTwoPairPracticeScenarioDisplayed()
    }

    @Test
    fun stepThreeOnlyAllowsTheRequiredStripEntryAndValue() {
        setContent()
        navigateToTutorial()
        navigateToStep(stepIndex = 2)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performScrollTo()
            .assertHasNoClickAction()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("1")
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .assertIsNotEnabled()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CANCEL)
            .performClick()

        enterStripValue(index = 1, value = "2")
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_player_entered_content_description,
                    "2"
                )
            )
    }

    @Test
    fun stepFourOnlyAllowsCompletingTheFirstAdditionExpression() {
        setContent()
        navigateToTutorial()
        completeStepThree()
        tapNextStep()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(1), useUnmergedTree = true)
            .performScrollTo()
            .assertHasNoClickAction()

        chooseTileOperand(tileIndex = 0, isLeftOperand = true, stripEntryId = 0)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(1), useUnmergedTree = true)
            .assertIsNotEnabled()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(0), useUnmergedTree = true)
            .performClick()

        chooseTileOperand(tileIndex = 0, isLeftOperand = false, stripEntryId = 1)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(0), useUnmergedTree = true)
            .assertIsNotEnabled()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(1), useUnmergedTree = true)
            .performClick()

        openTileOperatorMenu(tileIndex = 0)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.MULTIPLICATION), useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .performClick()

        assertTileExpression(
            tileIndex = 0,
            operator = Operator.ADDITION
        )
    }

    @Test
    fun stepFiveOnlyAllowsCompletingTheComplementaryMultiplicationExpression() {
        setContent()
        navigateToTutorial()
        completeStepThree()
        tapNextStep()
        completeFirstAdditionTile()
        tapNextStep()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileReset(0), useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(0), useUnmergedTree = true)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .assertHasNoClickAction()

        chooseTileOperand(tileIndex = 1, isLeftOperand = true, stripEntryId = 0)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(0), useUnmergedTree = true)
            .performClick()
        chooseTileOperand(tileIndex = 1, isLeftOperand = false, stripEntryId = 1)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(1), useUnmergedTree = true)
            .performClick()
        openTileOperatorMenu(tileIndex = 1)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.MULTIPLICATION), useUnmergedTree = true)
            .performClick()

        assertTileExpression(
            tileIndex = 1,
            operator = Operator.MULTIPLICATION
        )
    }

    @Test
    fun finalTutorialScenarioCanBeCompletedWithNormalGameplay() {
        setContent()
        navigateToTutorial()
        navigateToStep(stepIndex = 5)

        enterStripValue(index = 1, value = "2")
        enterStripValue(index = 4, value = "5")
        enterStripValue(index = 6, value = "7")
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        completeTile(tileIndex = 1, leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
        completeTile(tileIndex = 2, leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)
        completeTile(tileIndex = 3, leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
        completeTile(tileIndex = 4, leftStripEntryId = 4, operator = Operator.ADDITION, rightStripEntryId = 5)
        completeTile(tileIndex = 5, leftStripEntryId = 4, operator = Operator.MULTIPLICATION, rightStripEntryId = 5)
        completeTile(tileIndex = 6, leftStripEntryId = 6, operator = Operator.ADDITION, rightStripEntryId = 7)
        completeTile(tileIndex = 7, leftStripEntryId = 6, operator = Operator.MULTIPLICATION, rightStripEntryId = 7)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
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

    private fun navigateToStep(stepIndex: Int) {
        repeat(stepIndex) {
            tapNextStep()
        }
    }

    private fun completeStepThree() {
        navigateToStep(stepIndex = 2)
        enterStripValue(index = 1, value = "2")
    }

    private fun completeFirstAdditionTile() {
        completeTile(
            tileIndex = 0,
            leftStripEntryId = 0,
            operator = Operator.ADDITION,
            rightStripEntryId = 1
        )
    }

    private fun enterStripValue(index: Int, value: String) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(index))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput(value)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(index))
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_player_entered_content_description,
                    value
                )
            )
    }

    private fun completeTile(tileIndex: Int, leftStripEntryId: Int, operator: Operator, rightStripEntryId: Int) {
        chooseTileOperand(
            tileIndex = tileIndex,
            isLeftOperand = true,
            stripEntryId = leftStripEntryId
        )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(leftStripEntryId), useUnmergedTree = true)
            .performClick()
        chooseTileOperand(
            tileIndex = tileIndex,
            isLeftOperand = false,
            stripEntryId = rightStripEntryId
        )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(rightStripEntryId), useUnmergedTree = true)
            .performClick()
        openTileOperatorMenu(tileIndex = tileIndex)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .performClick()
    }

    private fun chooseTileOperand(tileIndex: Int, isLeftOperand: Boolean, stripEntryId: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(tileIndex), useUnmergedTree = true)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(
                if (isLeftOperand) {
                    GameScreenTestTags.tileLeftOperand(tileIndex)
                } else {
                    GameScreenTestTags.tileRightOperand(tileIndex)
                },
                useUnmergedTree = true
            )
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(stripEntryId), useUnmergedTree = true)
            .assertIsEnabled()
    }

    private fun openTileOperatorMenu(tileIndex: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(tileIndex), useUnmergedTree = true)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    private fun assertTileExpression(tileIndex: Int, operator: Operator) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_left_operand_content_description, "1"))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_operator_content_description, operator.symbol))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_right_operand_content_description, "2"))
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
            .performScrollTo()
            .assert(hasAnyDescendant(hasText(result.toString())))
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }
}
