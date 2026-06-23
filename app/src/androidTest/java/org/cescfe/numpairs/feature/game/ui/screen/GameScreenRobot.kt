package org.cescfe.numpairs.feature.game.ui.screen

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import androidx.test.espresso.Espresso.pressBack as espressoPressBack
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.feature.game.ui.semantics.GameHighlightedKey
import org.cescfe.numpairs.feature.game.ui.semantics.OperandSelectorUsageHintVisualStateKey
import org.cescfe.numpairs.feature.game.ui.semantics.StripEntryInputInvalidKey

class GameScreenRobot(
    private val activity: ComponentActivity,
    private val interactions: SemanticsNodeInteractionsProvider
) {
    fun assertTitleDisplayed(): GameScreenRobot = assertTitleDisplayed(string(R.string.tutorial_screen_title))

    fun assertTitleDisplayed(title: String): GameScreenRobot = apply {
        interactions
            .onNodeWithText(title)
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

    fun replaceStripValue(value: String): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextReplacement(value)
    }

    fun submitStripEntryInput(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performImeAction()
    }

    fun pressBack(): GameScreenRobot = apply {
        espressoPressBack()
    }

    fun assertStripEntryInputDisplayed(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assertIsDisplayed()
    }

    fun assertNoDialogDisplayed(): GameScreenRobot = apply {
        interactions
            .onAllNodes(
                SemanticsMatcher.expectValue(SemanticsProperties.IsDialog, Unit),
                useUnmergedTree = true
            )
            .assertCountEquals(0)
    }

    fun assertStripEntryInputHidden(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assertDoesNotExist()
    }

    fun assertStripEntryInputFocused(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assertIsFocused()
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

    fun assertStripEntryValidRange(minimum: Int, maximum: Int? = null): GameScreenRobot = apply {
        assertStripEntryFeedback(
            message = stripEntryValidRangeText(minimum = minimum, maximum = maximum),
            isError = false
        )
    }

    fun assertStripEntryInvalidRange(minimum: Int, maximum: Int? = null): GameScreenRobot = apply {
        assertStripEntryFeedback(
            message = stripEntryInvalidRangeText(minimum = minimum, maximum = maximum),
            isError = true
        )
    }

    fun assertStripEntryInputInvalid(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assert(
                SemanticsMatcher.expectValue(
                    StripEntryInputInvalidKey,
                    true
                )
            )
    }

    fun assertStripEntryInputNotInvalid(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assert(
                SemanticsMatcher.keyNotDefined(StripEntryInputInvalidKey)
            )
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
        assertUsageIndicator(
            testTag = GameScreenTestTags.tileOperandUsageHint(entryId = entryId, operator = operator),
            operator = operator,
            stateDescriptionResId = stateDescriptionResId
        )
    }

    fun assertOperandUsageHintVisualState(entryId: Int, operator: Operator, visualState: String): GameScreenRobot =
        apply {
            interactions
                .onNodeWithTag(
                    GameScreenTestTags.tileOperandUsageHint(entryId = entryId, operator = operator),
                    useUnmergedTree = true
                )
                .assert(
                    SemanticsMatcher.expectValue(
                        OperandSelectorUsageHintVisualStateKey,
                        visualState
                    )
                )
        }

    fun assertStripUsageIndicatorState(
        index: Int,
        operator: Operator,
        @StringRes stateDescriptionResId: Int
    ): GameScreenRobot = apply {
        assertUsageIndicator(
            testTag = GameScreenTestTags.stripUsageIndicator(index = index, operator = operator),
            operator = operator,
            stateDescriptionResId = stateDescriptionResId
        )
    }

    fun assertStripUsageIndicatorHidden(index: Int, operator: Operator): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(
                GameScreenTestTags.stripUsageIndicator(index = index, operator = operator),
                useUnmergedTree = true
            )
            .assertDoesNotExist()
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

    fun assertStripItemHighlighted(index: Int): GameScreenRobot = apply {
        assertHighlighted(testTag = GameScreenTestTags.stripItem(index))
    }

    fun assertStripItemNotHighlighted(index: Int): GameScreenRobot = apply {
        assertNotHighlighted(testTag = GameScreenTestTags.stripItem(index))
    }

    fun assertStripItemHasNoStateDescription(index: Int): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.stripItem(index))
            .assert(
                SemanticsMatcher.keyNotDefined(SemanticsProperties.StateDescription)
            )
    }

    fun assertTileHighlighted(index: Int): GameScreenRobot = apply {
        assertHighlighted(testTag = GameScreenTestTags.tile(index))
    }

    fun assertTileNotHighlighted(index: Int): GameScreenRobot = apply {
        assertNotHighlighted(testTag = GameScreenTestTags.tile(index))
    }

    fun assertLeftOperandHighlighted(tileIndex: Int): GameScreenRobot = apply {
        assertHighlighted(
            testTag = GameScreenTestTags.tileLeftOperand(tileIndex),
            useUnmergedTree = true
        )
    }

    fun assertLeftOperandNotHighlighted(tileIndex: Int): GameScreenRobot = apply {
        assertNotHighlighted(
            testTag = GameScreenTestTags.tileLeftOperand(tileIndex),
            useUnmergedTree = true
        )
    }

    fun assertOperatorHighlighted(tileIndex: Int): GameScreenRobot = apply {
        assertHighlighted(
            testTag = GameScreenTestTags.tileOperator(tileIndex),
            useUnmergedTree = true
        )
    }

    fun assertOperatorNotHighlighted(tileIndex: Int): GameScreenRobot = apply {
        assertNotHighlighted(
            testTag = GameScreenTestTags.tileOperator(tileIndex),
            useUnmergedTree = true
        )
    }

    fun assertRightOperandHighlighted(tileIndex: Int): GameScreenRobot = apply {
        assertHighlighted(
            testTag = GameScreenTestTags.tileRightOperand(tileIndex),
            useUnmergedTree = true
        )
    }

    fun assertRightOperandNotHighlighted(tileIndex: Int): GameScreenRobot = apply {
        assertNotHighlighted(
            testTag = GameScreenTestTags.tileRightOperand(tileIndex),
            useUnmergedTree = true
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

    fun assertPuzzleOutcomeHidden(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.PUZZLE_OUTCOME)
            .assertDoesNotExist()
    }

    fun assertLocalRuleConflictMessageDisplayed(@StringRes stringResId: Int): GameScreenRobot = apply {
        assertContentDescription(
            testTag = GameScreenTestTags.LOCAL_RULE_CONFLICT,
            contentDescription = string(stringResId)
        )
        interactions
            .onNodeWithTag(GameScreenTestTags.LOCAL_RULE_CONFLICT_MESSAGE, useUnmergedTree = true)
            .assert(hasText(string(stringResId)))
    }

    fun assertLocalRuleConflictHidden(): GameScreenRobot = apply {
        interactions
            .onNodeWithTag(GameScreenTestTags.LOCAL_RULE_CONFLICT)
            .assertDoesNotExist()
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

    private fun assertStripEntryFeedback(message: String, isError: Boolean) {
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_RANGE)
            .assertIsDisplayed()
            .assert(hasText(message))
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ContentDescription,
                    listOf(message)
                )
            )
            .assert(
                if (isError) {
                    SemanticsMatcher.expectValue(SemanticsProperties.Error, message)
                } else {
                    SemanticsMatcher.keyNotDefined(SemanticsProperties.Error)
                }
            )
        interactions
            .onNodeWithTag(GameScreenTestTags.STRIP)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    message
                )
            )
    }

    private fun assertUsageIndicator(testTag: String, operator: Operator, @StringRes stateDescriptionResId: Int) {
        assertContentDescription(
            testTag = testTag,
            contentDescription = usageIndicatorContentDescription(operator),
            useUnmergedTree = true
        )
        interactions
            .onNodeWithTag(testTag, useUnmergedTree = true)
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.StateDescription,
                    string(stateDescriptionResId)
                )
            )
        interactions
            .onNode(
                hasTestTag(testTag).and(hasAnyDescendant(hasText(operator.symbol))),
                useUnmergedTree = true
            )
            .assertIsDisplayed()
    }

    private fun assertHighlighted(testTag: String, useUnmergedTree: Boolean = false) {
        interactions
            .onNodeWithTag(testTag, useUnmergedTree = useUnmergedTree)
            .assert(
                SemanticsMatcher.expectValue(
                    GameHighlightedKey,
                    true
                )
            )
    }

    private fun assertNotHighlighted(testTag: String, useUnmergedTree: Boolean = false) {
        interactions
            .onNodeWithTag(testTag, useUnmergedTree = useUnmergedTree)
            .assert(
                SemanticsMatcher.keyNotDefined(GameHighlightedKey)
            )
    }

    private fun usageIndicatorContentDescription(operator: Operator): String = string(
        when (operator) {
            Operator.Addition -> R.string.tile_operand_usage_addition_hint
            Operator.Multiplication -> R.string.tile_operand_usage_multiplication_hint
            Operator.Hidden -> error("Hidden operator does not expose operand usage indicators.")
        }
    )

    private fun stripEntryValidRangeText(minimum: Int, maximum: Int?): String = maximum?.let { maximumValue ->
        string(R.string.strip_entry_valid_range_bounded, minimum, maximumValue)
    } ?: string(R.string.strip_entry_valid_range_unbounded, minimum)

    private fun stripEntryInvalidRangeText(minimum: Int, maximum: Int?): String = maximum?.let { maximumValue ->
        string(R.string.strip_entry_invalid_range_bounded, minimum, maximumValue)
    } ?: string(R.string.strip_entry_invalid_range_unbounded, minimum)

    private fun string(@StringRes stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        activity.getString(stringResId)
    } else {
        activity.getString(stringResId, *formatArgs)
    }
}
