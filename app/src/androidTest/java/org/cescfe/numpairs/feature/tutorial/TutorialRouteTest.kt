package org.cescfe.numpairs.feature.tutorial

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicInteger
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.game.ui.semantics.GameHighlightedKey
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
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
        assertFocusedIntroductionStripDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .assertDoesNotExist()
        assertNoRequiredSkipAction()
        assertHighlighted(testTag = GameScreenTestTags.stripItem(1))
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(0))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    @Test
    fun tutorialDoesNotAdvanceBeforeTheRequiredActionIsCompleted() {
        setContent()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .assertHasNoClickAction()
        composeTestRule.mainClock.advanceTimeBy(TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS)

        assertStepDisplayed(stepIndex = 0)
    }

    @Test
    fun stepOneShowsTutorialGuidanceAndKeepsInvalidRangeFeedbackTruthful() {
        setContent()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_RANGE)
            .assert(hasText(string(R.string.tutorial_strip_entry_guidance)))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("5")
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_RANGE)
            .assert(hasText(string(R.string.strip_entry_invalid_range_bounded, 2, 4)))

        assertStepDisplayed(stepIndex = 0)
    }

    @Test
    fun stepOnePreservesTheEnteredStripWhenStepTwoRevealsTheAuthoredTiles() {
        setContent()

        completeFocusedStepOne()

        waitForStep(stepIndex = 1)
        assertStepDisplayed(stepIndex = 1)
        assertFocusedIntroductionStripDisplayed(expectedSecondValue = "3", isSecondValuePlayerEntered = true)
        assertTileResult(tileIndex = 0, result = 5)
        assertTileExpressionHidden(tileIndex = 0)
        assertTileExpression(tileIndex = 1, operator = Operator.MULTIPLICATION, leftValue = "2", rightValue = "3")
        assertTileExpression(tileIndex = 2, operator = Operator.ADDITION, leftValue = "4", rightValue = "5")
        assertTileExpression(tileIndex = 3, operator = Operator.MULTIPLICATION, leftValue = "4", rightValue = "5")
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    @Test
    fun completingStepTwoStartsTheCleanRepeatedValuePuzzle() {
        setContent()

        completeFocusedStepOne()
        waitForStep(stepIndex = 1)
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        waitForStep(stepIndex = 2)

        assertStepDisplayed(stepIndex = 2)
        assertRepeatedValuePracticeScenarioDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    @Test
    fun stepTwoOffersNormalChoicesAndAcceptsReversedOperands() {
        setContent(startStepIndex = 1)

        openTileOperatorMenu(tileIndex = 0)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.MULTIPLICATION), useUnmergedTree = true)
            .assertIsEnabled()
            .performClick()

        chooseTileOperand(tileIndex = 0, isLeftOperand = true, stripEntryId = 1)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(0), useUnmergedTree = true)
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(1), useUnmergedTree = true)
            .assertIsEnabled()
            .performClick()
        chooseTileOperand(tileIndex = 0, isLeftOperand = false, stripEntryId = 0)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(0), useUnmergedTree = true)
            .performClick()

        composeTestRule.mainClock.advanceTimeBy(TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS)
        assertStepDisplayed(stepIndex = 1)
        assertTileExpression(
            tileIndex = 0,
            operator = Operator.MULTIPLICATION,
            leftValue = "3",
            rightValue = "2"
        )

        openTileOperatorMenu(tileIndex = 0)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .performClick()

        waitForStep(stepIndex = 2)
    }

    @Test
    fun stepThreeUsesNormalPuzzleInteractionsAndCompletesLearnBasicsWithoutShowingSuccess() {
        setContent()

        completeFocusedTutorial()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    @Test
    fun focusedTutorialReportsGuidedCompletionExactlyOnceWithoutShowingSuccess() {
        val completionCount = AtomicInteger(0)
        setContent(onTutorialCompleted = { completionCount.incrementAndGet() })

        completeFocusedTutorial()

        composeTestRule.waitUntil(timeoutMillis = TUTORIAL_STEP_WAIT_TIMEOUT_MS) {
            completionCount.get() == 1
        }
        composeTestRule.mainClock.advanceTimeBy(TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS)

        assertEquals(1, completionCount.get())
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    @Test
    fun uninterruptedProgressCheckpointIsReportedExactlyOnce() {
        val stepOneCompletionCount = AtomicInteger(0)
        setContent(
            onProgressCheckpointReached = { progressCheckpoint ->
                if (progressCheckpoint == TutorialProgressCheckpoint.STRIP_INTRODUCTION_COMPLETED) {
                    stepOneCompletionCount.incrementAndGet()
                }
            }
        )

        completeFocusedStepOne()
        waitForStep(stepIndex = 1)
        composeTestRule.mainClock.advanceTimeBy(TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS)

        assertEquals(1, stepOneCompletionCount.get())
    }

    @Test
    fun reopeningAfterClosingOnStepTwoAllowsStepOneToAdvanceAgain() {
        val restartTutorial = setRestartableContent()
        completeFocusedStepOne()
        waitForStep(stepIndex = 1)

        restartTutorial()
        assertStepDisplayed(stepIndex = 0)
        assertFocusedIntroductionStripDisplayed()
        completeFocusedStepOne()

        waitForStep(stepIndex = 1)
        assertStepDisplayed(stepIndex = 1)
    }

    @Test
    fun reopeningAfterCompletingAllStepsAllowsStepOneToAdvanceAgain() {
        val restartTutorial = setRestartableContent()
        completeFocusedTutorial()
        composeTestRule.mainClock.advanceTimeBy(TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS)

        restartTutorial()
        assertStepDisplayed(stepIndex = 0)
        assertFocusedIntroductionStripDisplayed()
        completeFocusedStepOne()

        waitForStep(stepIndex = 1)
        assertStepDisplayed(stepIndex = 1)
    }

    @Test
    fun solvingTipsPracticeHighlightsExpectedActionsAndCompletesWithSuccessOverlay() {
        setSolvingTipsPracticeTutorialContent()

        assertStepDisplayed(stepIndex = 0, mode = TutorialMode.SOLVING_TIPS_PRACTICE)
        assertSolvingTipsPracticeScenarioDisplayed()
        assertHighlighted(testTag = GameScreenTestTags.stripItem(1))
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(0))
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(2))
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(0))
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(1))
        assertTileExpressionSlotsHighlighted(tileIndex = 0)
        assertTileExpressionSlotsNotHighlighted(tileIndex = 1)

        enterStripValue(index = 1, value = "3")
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        assertStepDisplayed(stepIndex = 0, mode = TutorialMode.SOLVING_TIPS_PRACTICE)
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(1))
        assertTileExpressionSlotsNotHighlighted(tileIndex = 0)
        assertTileExpressionSlotsHighlighted(tileIndex = 1)

        completeTile(tileIndex = 1, leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
        waitForStep(stepIndex = 1, mode = TutorialMode.SOLVING_TIPS_PRACTICE)
        assertStepDisplayed(stepIndex = 1, mode = TutorialMode.SOLVING_TIPS_PRACTICE)
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(0))
        assertHighlighted(testTag = GameScreenTestTags.stripItem(2))
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(3))
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(0))
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(2))
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(3))
        assertTileExpressionSlotsNotHighlighted(tileIndex = 0)
        assertTileExpressionSlotsHighlighted(tileIndex = 2)
        assertTileExpressionSlotsHighlighted(tileIndex = 3)

        enterStripValue(index = 2, value = "4")
        completeTile(tileIndex = 2, leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
        completeTile(tileIndex = 3, leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
    }

    private fun setContent(
        startStepIndex: Int = 0,
        onProgressCheckpointReached: suspend (TutorialProgressCheckpoint) -> Unit = {},
        onTutorialCompleted: (() -> Unit)? = null
    ) {
        composeTestRule.setContent {
            NumPairsTheme {
                TutorialRoute(
                    startStepIndex = startStepIndex,
                    onProgressCheckpointReached = onProgressCheckpointReached,
                    onTutorialCompleted = onTutorialCompleted
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun setSolvingTipsPracticeTutorialContent() {
        composeTestRule.setContent {
            NumPairsTheme {
                TutorialRoute(mode = TutorialMode.SOLVING_TIPS_PRACTICE)
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun setRestartableContent(): () -> Unit {
        val isTutorialVisible = mutableStateOf(true)
        composeTestRule.setContent {
            NumPairsTheme {
                if (isTutorialVisible.value) {
                    TutorialRoute()
                }
            }
        }
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()

        return {
            composeTestRule.runOnUiThread {
                isTutorialVisible.value = false
            }
            composeTestRule
                .onNodeWithTag(GameScreenTestTags.SCREEN)
                .assertDoesNotExist()
            composeTestRule.runOnUiThread {
                isTutorialVisible.value = true
            }
            composeTestRule
                .onNodeWithTag(GameScreenTestTags.SCREEN)
                .assertIsDisplayed()
        }
    }

    private fun completeFocusedStepOne() {
        enterStripValue(index = 1, value = "3")
    }

    private fun completeFocusedTutorial() {
        completeFocusedStepOne()
        waitForStep(stepIndex = 1)
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        waitForStep(stepIndex = 2)

        enterStripValue(index = 0, value = "1")
        enterStripValue(index = 1, value = "2")
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        completeTile(tileIndex = 1, leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
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
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performImeAction()
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

    private fun assertTileExpression(
        tileIndex: Int,
        operator: Operator,
        leftValue: String = "2",
        rightValue: String = "3"
    ) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_left_operand_content_description, leftValue))
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
            .assertContentDescriptionEquals(string(R.string.tile_right_operand_content_description, rightValue))
    }

    private fun assertSecondSolvingTipsTileExpressionHidden() {
        assertTileExpressionHidden(tileIndex = 1)
    }

    private fun assertTileExpressionHidden(tileIndex: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_left_operand_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_operator_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_right_operand_hidden_content_description))
    }

    private fun assertStepDisplayed(stepIndex: Int, mode: TutorialMode = TutorialMode.LEARN_BASICS) {
        val steps = TutorialContent.stepsFor(mode)
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

    private fun assertNoRequiredSkipAction() {
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.SKIP_ACTION)
            .assertDoesNotExist()
    }

    private fun assertFocusedIntroductionStripDisplayed(
        expectedSecondValue: String? = null,
        isSecondValuePlayerEntered: Boolean = false
    ) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .performScrollTo()
            .assertContentDescriptionEquals(string(R.string.strip_item_known_content_description, "2"))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(
                if (isSecondValuePlayerEntered) {
                    string(
                        R.string.strip_item_player_entered_content_description,
                        requireNotNull(expectedSecondValue)
                    )
                } else {
                    string(R.string.strip_item_hidden_content_description)
                }
            )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(2))
            .assertContentDescriptionEquals(string(R.string.strip_item_known_content_description, "4"))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(3))
            .assertContentDescriptionEquals(string(R.string.strip_item_known_content_description, "5"))
    }

    private fun assertRepeatedValuePracticeScenarioDisplayed() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .performScrollTo()
            .assertContentDescriptionEquals(string(R.string.strip_item_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(string(R.string.strip_item_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(2))
            .assertContentDescriptionEquals(string(R.string.strip_item_known_content_description, "2"))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(3))
            .assertContentDescriptionEquals(string(R.string.strip_item_known_content_description, "3"))
        assertTileResult(tileIndex = 0, result = 3)
        assertTileResult(tileIndex = 1, result = 2)
        assertTileResult(tileIndex = 2, result = 5)
        assertTileResult(tileIndex = 3, result = 6)
    }

    private fun assertSolvingTipsPracticeScenarioDisplayed() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .performScrollTo()
            .assertContentDescriptionEquals(string(R.string.strip_item_known_content_description, "2"))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(string(R.string.strip_item_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(2))
            .assertContentDescriptionEquals(string(R.string.strip_item_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(3))
            .assertContentDescriptionEquals(string(R.string.strip_item_known_content_description, "8"))
        assertTileResult(tileIndex = 0, result = 5)
        assertTileResult(tileIndex = 1, result = 6)
        assertSecondSolvingTipsTileExpressionHidden()
        assertTileResult(tileIndex = 2, result = 32)
        assertTileResult(tileIndex = 3, result = 12)
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
        assertNodeNotHighlighted(testTag = testTag, useUnmergedTree = true)
    }

    private fun assertTileExpressionSlotsHighlighted(tileIndex: Int) {
        assertHighlighted(testTag = GameScreenTestTags.tileLeftOperand(tileIndex), useUnmergedTree = true)
        assertHighlighted(testTag = GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
        assertHighlighted(testTag = GameScreenTestTags.tileRightOperand(tileIndex), useUnmergedTree = true)
    }

    private fun assertTileExpressionSlotsNotHighlighted(tileIndex: Int) {
        assertNodeNotHighlighted(testTag = GameScreenTestTags.tileLeftOperand(tileIndex), useUnmergedTree = true)
        assertNodeNotHighlighted(testTag = GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
        assertNodeNotHighlighted(testTag = GameScreenTestTags.tileRightOperand(tileIndex), useUnmergedTree = true)
    }

    private fun assertNodeNotHighlighted(testTag: String, useUnmergedTree: Boolean = false) {
        composeTestRule
            .onNodeWithTag(testTag, useUnmergedTree = useUnmergedTree)
            .assert(
                SemanticsMatcher.keyNotDefined(GameHighlightedKey)
            )
    }

    private fun waitForStep(stepIndex: Int, mode: TutorialMode = TutorialMode.LEARN_BASICS) {
        val step = TutorialContent.stepsFor(mode)[stepIndex]

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
