package org.cescfe.numpairs.feature.game.ui

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.espresso.Espresso.pressBack as espressoPressBack
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Operator

class GameScreenRobot(
    private val activity: ComponentActivity,
    private val interactions: SemanticsNodeInteractionsProvider
) {
    fun assertTitleDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithText(string(R.string.app_name))
            .assertIsDisplayed()
    }

    fun scrollToBoard(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
    }

    fun assertBoardDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun assertStripDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP)
            .assertIsDisplayed()
    }

    fun tapStripItem(index: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.stripItem(index))
            .performClick()
    }

    fun tapTileLeftOperand(index: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(index), useUnmergedTree = true)
            .performClick()
    }

    fun tapTileRightOperand(index: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(index), useUnmergedTree = true)
            .performClick()
    }

    fun tapTileOperator(index: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileOperator(index), useUnmergedTree = true)
            .performClick()
    }

    fun tapTileReset(index: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileReset(index), useUnmergedTree = true)
            .performClick()
    }

    fun tapOperandOption(entryId: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(entryId), useUnmergedTree = true)
            .performClick()
    }

    fun tapOperatorOption(operator: Operator): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .performClick()
    }

    fun tapSuccessOverlay(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .performClick()
    }

    fun enterStripValue(value: String): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput(value)
    }

    fun confirmStripEntry(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()
    }

    fun cancelStripEntry(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CANCEL)
            .performClick()
    }

    fun pressBack(): GameScreenRobot = apply {
        espressoPressBack()
    }

    fun assertStripEntryDialogDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_DIALOG)
            .assertIsDisplayed()
    }

    fun assertStripEntryValidRange(minimum: Int, maximum: Int? = null): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_RANGE, useUnmergedTree = true)
            .assert(
                hasText(
                    if (maximum == null) {
                        string(
                            R.string.strip_entry_valid_range_unbounded,
                            minimum
                        )
                    } else {
                        string(
                            R.string.strip_entry_valid_range_bounded,
                            minimum,
                            maximum
                        )
                    }
                )
            )
    }

    fun assertStripEntryInputValue(value: String): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.EditableText,
                    AnnotatedString(value)
                )
            )
    }

    fun assertStripEntryConfirmDisabled(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .assertIsNotEnabled()
    }

    fun assertStripItemDescription(index: Int, @StringRes stringResId: Int, vararg formatArgs: Any): GameScreenRobot =
        apply {
            assertContentDescription(
                testTag = GameScreenTestTags.stripItem(index),
                contentDescription = string(stringResId, *formatArgs)
            )
        }

    fun assertLeftOperandDescription(
        tileIndex: Int,
        @StringRes stringResId: Int,
        vararg formatArgs: Any
    ): GameScreenRobot = apply {
        assertContentDescription(
            testTag = GameScreenTestTags.tileLeftOperand(tileIndex),
            contentDescription = string(stringResId, *formatArgs),
            useUnmergedTree = true
        )
    }

    fun assertRightOperandDescription(
        tileIndex: Int,
        @StringRes stringResId: Int,
        vararg formatArgs: Any
    ): GameScreenRobot = apply {
        assertContentDescription(
            testTag = GameScreenTestTags.tileRightOperand(tileIndex),
            contentDescription = string(stringResId, *formatArgs),
            useUnmergedTree = true
        )
    }

    fun assertOperatorDescription(
        tileIndex: Int,
        @StringRes stringResId: Int,
        vararg formatArgs: Any
    ): GameScreenRobot = apply {
        assertContentDescription(
            testTag = GameScreenTestTags.tileOperator(tileIndex),
            contentDescription = string(stringResId, *formatArgs),
            useUnmergedTree = true
        )
    }

    fun assertOperandSelectorDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun assertOperandSelectorHidden(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    fun assertOperatorSelectorDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun assertOperatorSelectorHidden(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    fun assertOperandOptionDisplayed(entryId: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(entryId), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun assertOperandOptionDescription(entryId: Int, value: String): GameScreenRobot = apply {
        assertContentDescription(
            testTag = GameScreenTestTags.tileOperandOption(entryId),
            contentDescription = value,
            useUnmergedTree = true
        )
    }

    fun assertOperandOptionEnabled(entryId: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(entryId), useUnmergedTree = true)
            .assertIsEnabled()
    }

    fun assertOperandOptionDisabled(entryId: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(entryId), useUnmergedTree = true)
            .assertIsNotEnabled()
    }

    fun assertOperatorOptionDisplayed(operator: Operator): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun assertOperatorOptionSelected(operator: Operator): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .assertIsSelected()
    }

    fun assertNoSelectedNodes(): GameScreenRobot = apply {
        interactions
            .onAllNodes(
                SemanticsMatcher.expectValue(SemanticsProperties.Selected, true),
                useUnmergedTree = true
            )
            .assertCountEquals(0)
    }

    fun assertOperandUsageHintState(
        entryId: Int,
        operator: Operator,
        @StringRes stateDescriptionResId: Int
    ): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(
                GameScreenTestTags.tileOperandUsageHint(entryId = entryId, operator = operator),
                useUnmergedTree = true
            )
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(stateDescriptionResId)
                )
            )
    }

    fun assertResetHidden(tileIndex: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileReset(tileIndex), useUnmergedTree = true)
            .assertDoesNotExist()
    }

    fun assertResetVisible(tileIndex: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tileReset(tileIndex), useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun assertTileStateDescription(tileIndex: Int, @StringRes stringResId: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tile(tileIndex))
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(stringResId)
                )
            )
    }

    fun assertTileHasNoStateDescription(tileIndex: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.tile(tileIndex))
            .assert(
                SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription)
            )
    }

    fun assertPuzzleOutcomeVisible(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.PUZZLE_OUTCOME)
            .assertIsDisplayed()
    }

    fun assertPuzzleOutcomeTitleDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.PUZZLE_OUTCOME_TITLE, useUnmergedTree = true)
            .assert(hasText(string(R.string.puzzle_outcome_invalid_title)))
    }

    fun assertPuzzleOutcomeMessageDisplayed(@StringRes stringResId: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.PUZZLE_OUTCOME_MESSAGE, useUnmergedTree = true)
            .assert(hasText(string(stringResId)))
    }

    fun assertSuccessOverlayVisible(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
    }

    fun assertSuccessOverlayHidden(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
    }

    fun assertSuccessOverlayMessageDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_MESSAGE, useUnmergedTree = true)
            .assert(hasText(string(R.string.success_overlay_message)))
    }

    private fun assertContentDescription(
        testTag: String,
        contentDescription: String,
        useUnmergedTree: Boolean = false
    ) {
        interactions
            .onNodeWithTag(testTag, useUnmergedTree = useUnmergedTree)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ContentDescription,
                    listOf(contentDescription)
                )
            )
    }

    private fun string(@StringRes stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        activity.getString(stringResId)
    } else {
        activity.getString(stringResId, *formatArgs)
    }
}
