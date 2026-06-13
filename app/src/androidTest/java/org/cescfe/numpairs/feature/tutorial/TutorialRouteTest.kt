package org.cescfe.numpairs.feature.tutorial

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.feature.game.ui.GameHighlightedKey
import org.cescfe.numpairs.feature.game.ui.GameScreenTestTags
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TutorialRouteTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun learnBasicsTutorialOpensOnStepOne() {
        setContent()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.INSTRUCTION_SURFACE)
            .assertIsDisplayed()
        assertStepDisplayed(stepIndex = 0)
        assertTwoPairPracticeScenarioDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(string(R.string.strip_item_hidden_content_description))
        assertNoInternalStepButtons()
        assertHighlighted(testTag = GameScreenTestTags.stripItem(1))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertDoesNotExist()
    }

    @Test
    fun tutorialDoesNotAdvanceBeforeTheRequiredActionIsCompleted() {
        setContent()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performScrollTo()
            .assertHasNoClickAction()
        composeTestRule.mainClock.advanceTimeBy(TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS)

        assertStepDisplayed(stepIndex = 0)
    }

    @Test
    fun guidedActionsAdvanceAutomaticallyAndPreserveTheTwoPairPracticeState() {
        setContent()

        enterStripValue(index = 1, value = "2")
        waitForStep(stepIndex = 1)
        assertStepDisplayed(stepIndex = 1)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_player_entered_content_description,
                    "2"
                )
            )
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(0))
        assertHighlighted(testTag = GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
        assertHighlighted(testTag = GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
        assertHighlighted(testTag = GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(1), useUnmergedTree = true)
            .performScrollTo()
            .assertHasNoClickAction()
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        waitForStep(stepIndex = 2)
        assertStepDisplayed(stepIndex = 2)
        assertTileExpression(tileIndex = 0, operator = Operator.ADDITION)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileReset(0), useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .assertHasNoClickAction()
        completeTile(tileIndex = 1, leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
        waitForStep(stepIndex = 3)
        assertStepDisplayed(stepIndex = 3)
        assertTwoPairPracticeScenarioDisplayed()
        assertTileExpression(tileIndex = 0, operator = Operator.ADDITION)
        assertTileExpression(tileIndex = 1, operator = Operator.MULTIPLICATION)
    }

    @Test
    fun finishingTheTwoPairPracticePuzzleShowsSuccessOverlayOnTheLearnBasicsCompletionStep() {
        setContent()

        completeGuidedTwoPairSteps()
        completeTwoPairPracticeRemainder()
        composeTestRule.mainClock.advanceTimeBy(TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS)

        assertStepDisplayed(stepIndex = 3)
        assertTwoPairPracticeScenarioDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
    }

    @Test
    fun practiceFullPuzzleTutorialScenarioCanBeCompletedWithNormalGameplay() {
        setPracticeFullPuzzleTutorialContent()

        assertStepDisplayed(stepIndex = 0, mode = TutorialMode.PRACTICE_FULL_PUZZLE)
        assertFinalEasyFourPairsScenarioDisplayed()

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

    private fun setContent() {
        composeTestRule.setContent {
            NumPairsTheme {
                TutorialRoute()
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun setPracticeFullPuzzleTutorialContent() {
        composeTestRule.setContent {
            NumPairsTheme {
                TutorialRoute(mode = TutorialMode.PRACTICE_FULL_PUZZLE)
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun completeGuidedTwoPairSteps() {
        enterStripValue(index = 1, value = "2")
        waitForStep(stepIndex = 1)
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        waitForStep(stepIndex = 2)
        completeTile(tileIndex = 1, leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
        waitForStep(stepIndex = 3)
    }

    private fun completeTwoPairPracticeRemainder() {
        completeTile(tileIndex = 2, leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)
        completeTile(tileIndex = 3, leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
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
            .assertContentDescriptionEquals(
                string(
                    R.string.tile_operator_content_description,
                    operator.accessibilityLabel()
                )
            )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_right_operand_content_description, "2"))
    }

    private fun assertStepDisplayed(stepIndex: Int, mode: TutorialMode = TutorialMode.LEARN_BASICS) {
        val steps = TutorialMvpContent.stepsFor(mode)
        val step = steps[stepIndex]

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_INDICATOR)
            .performScrollTo()
            .assert(
                hasText(
                    string(
                        R.string.tutorial_step_indicator,
                        stepIndex + 1,
                        steps.size
                    )
                )
            )
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_COPY)
            .performScrollTo()
            .assert(hasText(string(step.playerFacingCopyResId)))
    }

    private fun assertNoInternalStepButtons() {
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_BUTTON)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_BUTTON)
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

    private fun assertHighlighted(testTag: String, useUnmergedTree: Boolean = false) {
        composeTestRule
            .onNodeWithTag(testTag, useUnmergedTree = useUnmergedTree)
            .assert(
                SemanticsMatcher.expectValue(
                    GameHighlightedKey,
                    true
                )
            )
    }

    private fun assertUnmergedNodeNotHighlighted(testTag: String) {
        composeTestRule
            .onNodeWithTag(testTag, useUnmergedTree = true)
            .assert(
                SemanticsMatcher.expectValue(
                    GameHighlightedKey,
                    false
                )
            )
    }

    private fun waitForStep(stepIndex: Int, mode: TutorialMode = TutorialMode.LEARN_BASICS) {
        val step = TutorialMvpContent.stepsFor(mode)[stepIndex]

        composeTestRule.waitUntil(timeoutMillis = TUTORIAL_STEP_WAIT_TIMEOUT_MS) {
            composeTestRule
                .onAllNodes(hasText(string(step.playerFacingCopyResId)))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }

    private fun Operator.accessibilityLabel(): String = when (this) {
        Operator.Addition -> string(R.string.tile_operator_option_addition)
        Operator.Multiplication -> string(R.string.tile_operator_option_multiplication)
        Operator.Hidden -> symbol
    }

    private companion object {
        const val TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS = 1_000L
        const val TUTORIAL_STEP_WAIT_TIMEOUT_MS = 5_000L
    }
}
