package org.cescfe.numpairs.feature.tutorial

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.feature.fourpairs.FourPairsPuzzleProvider
import org.cescfe.numpairs.feature.fourpairs.FourPairsRoute
import org.cescfe.numpairs.feature.game.ui.GameScreenTestTags
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TutorialOverlayHostTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tutorialOverlayHostsSelectedModeAndClosesWithoutResettingUnderlyingFourPairsPuzzle() {
        val puzzleProvider = QueueFourPairsPuzzleProvider(initialPuzzle)
        var tutorialOverlayMode by mutableStateOf<TutorialMode?>(null)

        composeTestRule.setContent {
            NumPairsTheme {
                FourPairsRoute(
                    puzzleProvider = puzzleProvider,
                    tutorialOverlayMode = tutorialOverlayMode,
                    onTutorialOverlayClosed = {
                        tutorialOverlayMode = null
                    }
                )
            }
        }

        enterPreservedStripValue()
        assertPreservedStripItemPlayerEntered()
        assertEquals(1, puzzleProvider.requestCount)

        composeTestRule.runOnIdle {
            tutorialOverlayMode = TutorialMode.PRACTICE_FULL_PUZZLE
        }

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertIsDisplayed()
        assertStepIndicatorDisplayed(mode = TutorialMode.PRACTICE_FULL_PUZZLE)

        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        assertPreservedStripItemPlayerEntered()
        assertEquals(1, puzzleProvider.requestCount)
    }

    @Test
    fun playTutorialFromRulesHelperDismissesDialogOpensLearnBasicsOverlayAndReturnsToGame() {
        val puzzleProvider = QueueFourPairsPuzzleProvider(initialPuzzle)

        composeTestRule.setContent {
            NumPairsTheme {
                FourPairsRoute(puzzleProvider = puzzleProvider)
            }
        }

        enterPreservedStripValue()
        assertPreservedStripItemPlayerEntered()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_PLAY_TUTORIAL_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertIsDisplayed()
        assertStepIndicatorDisplayed(mode = TutorialMode.LEARN_BASICS)
        completeLearnBasicsTutorial()
        assertTutorialSuccessOverlayDisplayed()
        assertEquals(1, puzzleProvider.requestCount)

        dismissTutorialSuccessOverlayAndAssertOverlayRemains()
        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        assertPreservedStripItemPlayerEntered()
        assertEquals(1, puzzleProvider.requestCount)
    }

    @Test
    fun hintActionOpensSolvingTipsDialogPracticeOverlayAndReturnsToGame() {
        val puzzleProvider = QueueFourPairsPuzzleProvider(initialPuzzle)

        composeTestRule.setContent {
            NumPairsTheme {
                FourPairsRoute(puzzleProvider = puzzleProvider)
            }
        }

        enterPreservedStripValue()
        assertPreservedStripItemPlayerEntered()
        assertHintActionIsLeftOfRulesHelpAction()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.HINT_ACTION)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(string(R.string.hint_action_content_description))
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
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertIsDisplayed()
        assertStepIndicatorDisplayed(mode = TutorialMode.SOLVING_TIPS_PRACTICE)
        completeSolvingTipsPracticeTutorial()
        assertTutorialSuccessOverlayDisplayed()
        assertEquals(1, puzzleProvider.requestCount)

        dismissTutorialSuccessOverlayAndAssertOverlayRemains()
        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        assertPreservedStripItemPlayerEntered()
        assertEquals(1, puzzleProvider.requestCount)
    }

    private fun completeLearnBasicsTutorial() {
        enterTutorialStripValue(index = 1, value = "2")
        waitForLearnBasicsStep(stepIndex = 1)
        completeTutorialTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        waitForLearnBasicsStep(stepIndex = 2)
        completeTutorialTile(
            tileIndex = 1,
            leftStripEntryId = 0,
            operator = Operator.MULTIPLICATION,
            rightStripEntryId = 1
        )
        waitForLearnBasicsStep(stepIndex = 3)
        completeTutorialTile(tileIndex = 2, leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)
        completeTutorialTile(
            tileIndex = 3,
            leftStripEntryId = 2,
            operator = Operator.MULTIPLICATION,
            rightStripEntryId = 3
        )
    }

    private fun completeSolvingTipsPracticeTutorial() {
        enterTutorialStripValue(index = 1, value = "3")
        completeTutorialTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        completeTutorialTile(
            tileIndex = 1,
            leftStripEntryId = 0,
            operator = Operator.MULTIPLICATION,
            rightStripEntryId = 1
        )
        waitForSecondSolvingTipsPracticeStep()
        enterTutorialStripValue(index = 2, value = "4")
        completeTutorialTile(
            tileIndex = 2,
            leftStripEntryId = 2,
            operator = Operator.MULTIPLICATION,
            rightStripEntryId = 3
        )
        completeTutorialTile(tileIndex = 3, leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)
    }

    private fun enterTutorialStripValue(index: Int, value: String) {
        overlayNodeWithTag(GameScreenTestTags.stripItem(index))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput(value)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()
        overlayNodeWithTag(GameScreenTestTags.stripItem(index))
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_player_entered_content_description,
                    value
                )
            )
    }

    private fun completeTutorialTile(
        tileIndex: Int,
        leftStripEntryId: Int,
        operator: Operator,
        rightStripEntryId: Int
    ) {
        chooseTutorialTileOperand(
            tileIndex = tileIndex,
            isLeftOperand = true,
            stripEntryId = leftStripEntryId
        )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(leftStripEntryId), useUnmergedTree = true)
            .performClick()
        chooseTutorialTileOperand(
            tileIndex = tileIndex,
            isLeftOperand = false,
            stripEntryId = rightStripEntryId
        )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(rightStripEntryId), useUnmergedTree = true)
            .performClick()
        openTutorialTileOperatorMenu(tileIndex = tileIndex)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .performClick()
    }

    private fun chooseTutorialTileOperand(tileIndex: Int, isLeftOperand: Boolean, stripEntryId: Int) {
        overlayNodeWithTag(GameScreenTestTags.tile(tileIndex), useUnmergedTree = true)
            .performScrollTo()
        overlayNodeWithTag(
            if (isLeftOperand) {
                GameScreenTestTags.tileLeftOperand(tileIndex)
            } else {
                GameScreenTestTags.tileRightOperand(tileIndex)
            },
            useUnmergedTree = true
        ).performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(stripEntryId), useUnmergedTree = true)
            .assertIsEnabled()
    }

    private fun openTutorialTileOperatorMenu(tileIndex: Int) {
        overlayNodeWithTag(GameScreenTestTags.tile(tileIndex), useUnmergedTree = true)
            .performScrollTo()
        overlayNodeWithTag(GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    private fun assertTutorialSuccessOverlayDisplayed() {
        overlayNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
    }

    private fun dismissTutorialSuccessOverlayAndAssertOverlayRemains() {
        pressBackUnconditionally()
        overlayNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertIsDisplayed()
    }

    private fun waitForLearnBasicsStep(stepIndex: Int) {
        val step = TutorialMvpContent.stepsFor(TutorialMode.LEARN_BASICS)[stepIndex]

        composeTestRule.waitUntil(timeoutMillis = TUTORIAL_STEP_WAIT_TIMEOUT_MS) {
            composeTestRule
                .onAllNodes(hasText(string(step.playerFacingCopyResId)))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitForSecondSolvingTipsPracticeStep() {
        val step = TutorialMvpContent.stepsFor(TutorialMode.SOLVING_TIPS_PRACTICE)[1]

        composeTestRule.waitUntil(timeoutMillis = TUTORIAL_STEP_WAIT_TIMEOUT_MS) {
            composeTestRule
                .onAllNodes(hasText(string(step.playerFacingCopyResId)))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun overlayNodeWithTag(testTag: String, useUnmergedTree: Boolean = false) = composeTestRule.onNode(
        hasTestTag(testTag) and hasAnyAncestor(hasTestTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)),
        useUnmergedTree = useUnmergedTree
    )

    private fun assertHintActionIsLeftOfRulesHelpAction() {
        val hintActionBounds = composeTestRule
            .onNodeWithTag(GameScreenTestTags.HINT_ACTION)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        val rulesHelperActionBounds = composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(
            "Hint action should appear to the left of rules help action.",
            hintActionBounds.right <= rulesHelperActionBounds.left
        )
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

    private fun assertStepIndicatorDisplayed(mode: TutorialMode) {
        val steps = TutorialMvpContent.stepsFor(mode)

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
        const val TUTORIAL_STEP_WAIT_TIMEOUT_MS = 5_000L
    }
}
