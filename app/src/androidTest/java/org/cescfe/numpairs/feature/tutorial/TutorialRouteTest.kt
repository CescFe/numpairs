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
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicInteger
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.game.ui.semantics.GameHighlightedKey
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        assertStepDisplayed(stepIndex = OBJECTIVE_EXPLANATION_STEP_INDEX)
        assertFocusedIntroductionStripDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .assertIsDisplayed()
        assertNoRequiredSkipAction()
        repeat(4) { index ->
            assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(index))
            assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(index))
            assertTileExpressionSlotsNotHighlighted(tileIndex = index)
            assertTileExpressionHidden(tileIndex = index)
        }
        assertNodeNotHighlighted(testTag = GameScreenTestTags.STRIP)
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_ACTION)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .assertIsDisplayed()
            .assertIsEnabled()
        val minimumTouchTargetHeight = with(composeTestRule.density) { 48.dp.toPx() }
        val instructionBounds = composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.INSTRUCTION_SURFACE)
            .fetchSemanticsNode()
            .boundsInRoot
        val nextBounds = composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(
            nextBounds.height >= minimumTouchTargetHeight
        )
        assertTrue(nextBounds.center.x > instructionBounds.center.x)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    @Test
    fun explanationUsesManualForwardAndBackwardNavigation() {
        setContent()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .performClick()
        assertStepDisplayed(stepIndex = STRIP_EXPLANATION_STEP_INDEX)
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_ACTION)
            .assertIsEnabled()
            .performClick()
        assertStepDisplayed(stepIndex = OBJECTIVE_EXPLANATION_STEP_INDEX)
        composeTestRule.mainClock.advanceTimeBy(TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS)

        assertStepDisplayed(stepIndex = OBJECTIVE_EXPLANATION_STEP_INDEX)
    }

    @Test
    fun explanationFreezesPuzzleInteractionsAndFocusesOneConceptAtATime() {
        setContent()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertHasNoClickAction()
        assertTileExpressionSlotsHaveNoClickAction(tileIndex = 0)

        navigateToExplanationStep(STRIP_EXPLANATION_STEP_INDEX)

        assertHighlighted(testTag = GameScreenTestTags.STRIP)
        repeat(4) { index ->
            assertHighlighted(testTag = GameScreenTestTags.stripItem(index))
            assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(index))
            assertTileExpressionSlotsNotHighlighted(tileIndex = index)
        }

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .performClick()
        assertStepDisplayed(stepIndex = TILE_EXPLANATION_STEP_INDEX)

        assertNodeNotHighlighted(testTag = GameScreenTestTags.STRIP)
        repeat(4) { index ->
            assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(index))
        }
        assertHighlighted(testTag = GameScreenTestTags.tile(0), useUnmergedTree = true)
        assertTileExpressionSlotsHighlighted(tileIndex = 0)
        repeat(3) { offset ->
            val tileIndex = offset + 1
            assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(tileIndex))
            assertTileExpressionSlotsNotHighlighted(tileIndex = tileIndex)
        }

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .performClick()
        assertStepDisplayed(stepIndex = PAIR_EXPLANATION_STEP_INDEX)

        assertNodeNotHighlighted(testTag = GameScreenTestTags.STRIP)
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(0))
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(1))
        assertHighlighted(testTag = GameScreenTestTags.stripItem(2))
        assertHighlighted(testTag = GameScreenTestTags.stripItem(3))
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(0))
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(1))
        assertHighlighted(testTag = GameScreenTestTags.tile(2), useUnmergedTree = true)
        assertHighlighted(testTag = GameScreenTestTags.tile(3), useUnmergedTree = true)
    }

    @Test
    fun workedExampleAdvancesManuallyInOrderWhilePuzzleInteractionsStayLocked() {
        setContent()
        advanceThroughExplanation()

        assertStepDisplayed(stepIndex = WORKED_EXAMPLE_INTRODUCTION_STEP_INDEX)
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_ACTION)
            .assertIsDisplayed()
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .assertIsDisplayed()
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertHasNoClickAction()
        assertTileExpressionSlotsHaveNoClickAction(tileIndex = 3)
        repeat(4, ::assertTileExpressionHidden)
        assertNodeNotHighlighted(testTag = GameScreenTestTags.STRIP)
        repeat(4) { index ->
            assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(index))
            assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(index))
            assertTileExpressionSlotsNotHighlighted(tileIndex = index)
        }

        advanceWorkedExampleStep(PRODUCT_FOUR_FIVE_STEP_INDEX)
        assertTileExpression(tileIndex = 3, operator = Operator.MULTIPLICATION, leftValue = "4", rightValue = "5")
        advanceWorkedExampleStep(SUM_FOUR_FIVE_STEP_INDEX)
        assertTileExpression(tileIndex = 2, operator = Operator.ADDITION, leftValue = "4", rightValue = "5")
        advanceWorkedExampleStep(REVEAL_THREE_STEP_INDEX)
        assertFocusedIntroductionStripDisplayed(expectedSecondValue = "3", isSecondValuePlayerEntered = true)
        assertNodeNotHighlighted(testTag = GameScreenTestTags.STRIP)
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(0))
        assertHighlighted(testTag = GameScreenTestTags.stripItem(1))
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(2))
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(3))
        repeat(4) { index ->
            assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(index))
            assertTileExpressionSlotsNotHighlighted(tileIndex = index)
        }
        advanceWorkedExampleStep(PRODUCT_TWO_THREE_STEP_INDEX)
        assertTileExpression(tileIndex = 1, operator = Operator.MULTIPLICATION)
        advanceWorkedExampleStep(SUM_TWO_THREE_STEP_INDEX)
        assertTileExpression(tileIndex = 0, operator = Operator.ADDITION)

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_ACTION)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        assertStepDisplayed(stepIndex = PRODUCT_TWO_THREE_STEP_INDEX)
        assertTileExpressionHidden(tileIndex = 0)
        assertTileExpression(tileIndex = 1, operator = Operator.MULTIPLICATION)
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .performClick()
        assertStepDisplayed(stepIndex = SUM_TWO_THREE_STEP_INDEX)
        assertTileExpression(tileIndex = 0, operator = Operator.ADDITION)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    @Test
    fun independentPracticeCueAllowsAnyFirstActionThenStaysDismissed() {
        setContent(startStepIndex = PRACTICE_STEP_INDEX)

        assertRepeatedValuePracticeScenarioDisplayed()
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(0))
        assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(1))
        assertHighlighted(testTag = GameScreenTestTags.stripItem(2))
        assertHighlighted(testTag = GameScreenTestTags.stripItem(3))
        assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(0))
        assertHighlighted(testTag = GameScreenTestTags.tile(3), useUnmergedTree = true)

        openTileOperatorMenu(tileIndex = 0)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.MULTIPLICATION), useUnmergedTree = true)
            .assertIsEnabled()
            .performClick()
        assertPracticeCueDismissed()

        openTileOperatorMenu(tileIndex = 0)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .assertIsEnabled()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileReset(0), useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        assertPracticeCueDismissed()
    }

    @Test
    fun independentPracticeCanReviewPreviousStepsWithoutLosingProgressOrRepeatingCheckpoint() {
        val checkpointCount = AtomicInteger(0)
        setContent(
            onProgressCheckpointReached = { progressCheckpoint ->
                if (progressCheckpoint == TutorialProgressCheckpoint.WORKED_EXAMPLE_COMPLETED) {
                    checkpointCount.incrementAndGet()
                }
            }
        )
        advanceThroughExplanation()
        advanceThroughWorkedExample()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_ACTION)
            .assertIsDisplayed()
            .assertIsEnabled()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .assertDoesNotExist()
        val minimumTouchTargetHeight = with(composeTestRule.density) { 48.dp.toPx() }
        val instructionBounds = composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.INSTRUCTION_SURFACE)
            .fetchSemanticsNode()
            .boundsInRoot
        val previousBounds = composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_ACTION)
            .fetchSemanticsNode()
            .boundsInRoot
        assertTrue(previousBounds.height >= minimumTouchTargetHeight)
        assertTrue(previousBounds.center.x < instructionBounds.center.x)

        enterStripValue(index = 0, value = "1")
        assertPracticeCueDismissed()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.PREVIOUS_STEP_ACTION)
            .performScrollTo()
            .performClick()
        assertStepDisplayed(stepIndex = SUM_TWO_THREE_STEP_INDEX)
        assertTileExpression(tileIndex = 0, operator = Operator.ADDITION)

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .performScrollTo()
            .performClick()
        waitForStep(stepIndex = PRACTICE_STEP_INDEX)

        assertEquals(1, checkpointCount.get())
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .performScrollTo()
            .assertContentDescriptionEquals(
                string(R.string.strip_item_player_entered_content_description, "1")
            )
        assertPracticeCueDismissed()
    }

    @Test
    fun independentPracticeUsesNormalInteractionsAndCompletesLearnBasicsWithoutShowingSuccess() {
        setContent()

        completeFocusedTutorial()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    @Test
    fun focusedTutorialReportsCompletionExactlyOnceWithoutShowingSuccess() {
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
    fun workedExampleCheckpointIsReportedExactlyOnce() {
        val checkpointCount = AtomicInteger(0)
        setContent(
            onProgressCheckpointReached = { progressCheckpoint ->
                if (progressCheckpoint == TutorialProgressCheckpoint.WORKED_EXAMPLE_COMPLETED) {
                    checkpointCount.incrementAndGet()
                }
            }
        )

        advanceThroughExplanation()
        advanceThroughWorkedExample()

        assertEquals(1, checkpointCount.get())
    }

    @Test
    fun reopeningAfterTheWorkedExampleStartsTheTutorialFromTheBeginning() {
        val restartTutorial = setRestartableContent()
        advanceThroughExplanation()
        advanceThroughWorkedExample()

        restartTutorial()
        assertStepDisplayed(stepIndex = OBJECTIVE_EXPLANATION_STEP_INDEX)
        assertFocusedIntroductionStripDisplayed()
        advanceThroughExplanation()
        assertStepDisplayed(stepIndex = WORKED_EXAMPLE_INTRODUCTION_STEP_INDEX)
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

    private fun completeFocusedTutorial() {
        advanceThroughExplanation()
        advanceThroughWorkedExample()

        enterStripValue(index = 0, value = "1")
        enterStripValue(index = 1, value = "2")
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        completeTile(tileIndex = 1, leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
        completeTile(tileIndex = 2, leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)
        completeTile(tileIndex = 3, leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
    }

    private fun advanceThroughExplanation() {
        repeat(WORKED_EXAMPLE_INTRODUCTION_STEP_INDEX) {
            composeTestRule
                .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
                .performScrollTo()
                .performClick()
        }
        assertStepDisplayed(stepIndex = WORKED_EXAMPLE_INTRODUCTION_STEP_INDEX)
    }

    private fun advanceWorkedExampleStep(expectedStepIndex: Int) {
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .performScrollTo()
            .performClick()
        assertStepDisplayed(stepIndex = expectedStepIndex)
    }

    private fun advanceThroughWorkedExample() {
        repeat(SUM_TWO_THREE_STEP_INDEX - WORKED_EXAMPLE_INTRODUCTION_STEP_INDEX) {
            composeTestRule
                .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
                .performScrollTo()
                .performClick()
        }
        assertStepDisplayed(stepIndex = SUM_TWO_THREE_STEP_INDEX)
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            .performScrollTo()
            .performClick()
        waitForStep(stepIndex = PRACTICE_STEP_INDEX)
    }

    private fun navigateToExplanationStep(stepIndex: Int) {
        require(stepIndex in OBJECTIVE_EXPLANATION_STEP_INDEX..PAIR_EXPLANATION_STEP_INDEX)

        repeat(stepIndex) {
            composeTestRule
                .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
                .performScrollTo()
                .performClick()
        }
        assertStepDisplayed(stepIndex = stepIndex)
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

        assertStepIndicator(stepIndex = stepIndex, mode = mode)
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_COPY)
            .performScrollTo()
            .assert(hasText(string(step.playerFacingCopyResId)))
    }

    private fun assertStepIndicator(stepIndex: Int, mode: TutorialMode = TutorialMode.LEARN_BASICS) {
        val steps = TutorialContent.stepsFor(mode)

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

    private fun assertTileExpressionSlotsHaveNoClickAction(tileIndex: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(tileIndex), useUnmergedTree = true)
            .assertHasNoClickAction()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
            .assertHasNoClickAction()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(tileIndex), useUnmergedTree = true)
            .assertHasNoClickAction()
    }

    private fun assertPracticeCueDismissed() {
        repeat(4) { index ->
            assertNodeNotHighlighted(testTag = GameScreenTestTags.stripItem(index))
            assertUnmergedNodeNotHighlighted(testTag = GameScreenTestTags.tile(index))
        }
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
        const val OBJECTIVE_EXPLANATION_STEP_INDEX = 0
        const val STRIP_EXPLANATION_STEP_INDEX = 1
        const val TILE_EXPLANATION_STEP_INDEX = 2
        const val PAIR_EXPLANATION_STEP_INDEX = 3
        const val WORKED_EXAMPLE_INTRODUCTION_STEP_INDEX = 4
        const val PRODUCT_FOUR_FIVE_STEP_INDEX = 5
        const val SUM_FOUR_FIVE_STEP_INDEX = 6
        const val REVEAL_THREE_STEP_INDEX = 7
        const val PRODUCT_TWO_THREE_STEP_INDEX = 8
        const val SUM_TWO_THREE_STEP_INDEX = 9
        const val PRACTICE_STEP_INDEX = 10
        const val TUTORIAL_AUTO_ADVANCE_TEST_WAIT_MS = 1_000L
        const val TUTORIAL_STEP_WAIT_TIMEOUT_MS = 5_000L
    }
}
