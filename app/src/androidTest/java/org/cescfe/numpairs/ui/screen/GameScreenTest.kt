package org.cescfe.numpairs.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        viewModel = GameViewModel()

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsState()

            NumPairsTheme {
                GameScreen(
                    uiState = uiState,
                    onStripItemTapped = viewModel::onStripItemTapped,
                    onStripItemEntryDismissed = viewModel::onStripItemEntryDismissed,
                    onStripItemEntryConfirmed = viewModel::onStripItemEntryConfirmed,
                    onTileLeftOperandTapped = viewModel::onTileLeftOperandTapped,
                    onTileRightOperandTapped = viewModel::onTileRightOperandTapped,
                    onTileOperandSelectionDismissed = viewModel::onTileOperandSelectionDismissed,
                    onTileOperandSelectionConfirmed = viewModel::onTileOperandSelectionConfirmed,
                    onTileOperatorTapped = viewModel::onTileOperatorTapped,
                    onTileOperatorSelectionDismissed = viewModel::onTileOperatorSelectionDismissed,
                    onTileOperatorSelectionConfirmed = viewModel::onTileOperatorSelectionConfirmed
                )
            }
        }
    }

    @Test
    fun launchesSuccessfully() {
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.app_name)
            )
            .assertIsDisplayed()
    }

    @Test
    fun displaysPuzzleBoard() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun displaysStrip() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP)
            .assertIsDisplayed()
    }

    @Test
    fun tappingHiddenStripItemOpensEntryDialog() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_DIALOG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_RANGE, useUnmergedTree = true)
            .assert(
                hasText(
                    composeTestRule.activity.getString(
                        R.string.strip_entry_valid_range_bounded,
                        1,
                        6
                    )
                )
            )
    }

    @Test
    fun confirmingEntryDialogCompletesTheHiddenStripItem() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("2")

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("2")))
    }

    @Test
    fun tappingPlayerEnteredStripItemReopensEntryDialogWithPrefilledValue() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("2")

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.EditableText,
                    AnnotatedString("2")
                )
            )
    }

    @Test
    fun confirmingEntryDialogIsDisabledForOutOfRangeValues() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("9")

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .assertIsNotEnabled()
    }

    @Test
    fun cancellingEntryDialogLeavesTheHiddenStripItemUnchanged() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("9")

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CANCEL)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("?")))
    }

    @Test
    fun tappingHiddenTileOperatorOpensContextualSelector() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.MULTIPLICATION), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun tappingHiddenLeftTileOperandOpensBottomSheetSelector() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 6), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 1, value = 25), useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 2, value = 222), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun tappingHiddenRightTileOperandOpensBottomSheetSelector() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun playerEnteredStripItemAppearsAsSelectableOperandValue() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("2")

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 2), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun selectingAnOperandOptionCompletesTheHiddenTileOperandImmediately() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 6), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("6")))

        composeTestRule
            .onAllNodesWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun tappingFilledLeftTileOperandReopensBottomSheetSelectorWithTheCurrentOptionSelected() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 6), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 6), useUnmergedTree = true)
            .assertIsSelected()
    }

    @Test
    fun tappingFilledRightTileOperandReopensBottomSheetSelector() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 6), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun selectingAnOperandOptionReplacesTheFilledTileOperandImmediately() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 6), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 1, value = 25), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("25")))
    }

    @Test
    fun dismissingTheOperandSelectorLeavesTheHiddenTileOperandUnchanged() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        pressBack()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("?")))

        composeTestRule
            .onAllNodesWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun dismissingTheOperandSelectorLeavesTheFilledTileOperandUnchanged() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 6), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        pressBack()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("6")))
    }

    @Test
    fun selectingAnOperatorOptionCompletesTheHiddenTileOperatorImmediately() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("+")))

        composeTestRule
            .onAllNodesWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun dismissingTheOperatorSelectorLeavesTheHiddenTileOperatorUnchanged() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        pressBack()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("?")))

        composeTestRule
            .onAllNodesWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun tappingFilledTileOperatorReopensTheSelectorWithTheCurrentOptionSelectedAndAllowsReassignment() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .assertIsSelected()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.MULTIPLICATION), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("×")))
    }

    @Test
    fun fullyKnownIncorrectTilesAreMarkedInvalidAndRemainEditable() {
        buildIncorrectFirstTile()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(0))
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    composeTestRule.activity.getString(R.string.tile_state_incorrect)
                )
            )

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun correctingAnIncorrectTileClearsItsInvalidState() {
        buildIncorrectFirstTile()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(0))
            .assert(
                SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription)
            )
    }

    private fun buildIncorrectFirstTile() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("1")

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 0, value = 1), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.MULTIPLICATION), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(index = 3, value = 222), useUnmergedTree = true)
            .performClick()
    }
}
