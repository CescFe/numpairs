package org.cescfe.numpairs.feature.game.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemVisualStyle
import org.cescfe.numpairs.feature.game.presentation.TileOperandOptionUiState
import org.cescfe.numpairs.feature.game.presentation.TileOperandSelectionDialogUiState
import org.cescfe.numpairs.feature.game.presentation.TileUiState
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenStripUsageIndicatorTest : GameScreenTestHost() {
    @Test
    fun stripUsageIndicatorsReflectOperatorUsageState() {
        showUiStateFixture(stripUsageIndicatorUiState())

        screen
            .assertStripUsageIndicatorState(
                index = 0,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_available
            )
            .assertStripUsageIndicatorState(
                index = 0,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_available
            )
            .assertStripUsageIndicatorState(
                index = 1,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertStripUsageIndicatorState(
                index = 1,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_available
            )
            .assertStripUsageIndicatorState(
                index = 2,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_available
            )
            .assertStripUsageIndicatorState(
                index = 2,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertStripUsageIndicatorState(
                index = 3,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertStripUsageIndicatorState(
                index = 3,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertStripUsageIndicatorDoesNotExposeSelectorVisualState(
                index = 3,
                operator = Operator.ADDITION
            )
            .assertStripUsageIndicatorDoesNotExposeSelectorVisualState(
                index = 3,
                operator = Operator.MULTIPLICATION
            )
            .assertStripUsageIndicatorHidden(index = 4, operator = Operator.ADDITION)
            .assertStripUsageIndicatorHidden(index = 4, operator = Operator.MULTIPLICATION)
    }

    @Test
    fun stripIndicatorsAndOperandSelectorBadgesExposeMatchingUsageSemantics() {
        showUiStateFixture(stripAndOperandSelectorUsageUiState())

        screen
            .assertStripUsageIndicatorState(
                index = 1,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertOperandUsageHintState(
                entryId = 1,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertStripUsageIndicatorState(
                index = 2,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertOperandUsageHintState(
                entryId = 2,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertStripUsageIndicatorState(
                index = 3,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertOperandUsageHintState(
                entryId = 3,
                operator = Operator.ADDITION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertStripUsageIndicatorState(
                index = 3,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
            .assertOperandUsageHintState(
                entryId = 3,
                operator = Operator.MULTIPLICATION,
                stateDescriptionResId = R.string.tile_operand_usage_state_used
            )
    }
}

private fun stripUsageIndicatorUiState(): GameUiState = GameUiState(
    stripItems = listOf(
        StripItemUiState(
            label = "1",
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN
        ),
        StripItemUiState(
            label = "2",
            isEntryEnabled = true,
            visualStyle = StripItemVisualStyle.PLAYER_ENTERED,
            additionUsed = true
        ),
        StripItemUiState(
            label = "222",
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN,
            multiplicationUsed = true
        ),
        StripItemUiState(
            label = "4",
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN,
            additionUsed = true,
            multiplicationUsed = true
        ),
        StripItemUiState(
            label = "?",
            isEntryEnabled = true,
            visualStyle = StripItemVisualStyle.HIDDEN
        ),
        StripItemUiState(
            label = "6",
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN
        ),
        StripItemUiState(
            label = "7",
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN
        ),
        StripItemUiState(
            label = "8",
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN
        )
    ),
    tiles = List(8) { index ->
        TileUiState(
            leftOperandLabel = "?",
            operatorLabel = "?",
            rightOperandLabel = "?",
            resultLabel = (index + 1).toString()
        )
    }
)

private fun stripAndOperandSelectorUsageUiState(): GameUiState = stripUsageIndicatorUiState().copy(
    tileOperandSelectionDialog = TileOperandSelectionDialogUiState(
        tileIndex = 0,
        slot = OperandSlot.LEFT,
        availableOperands = listOf(
            TileOperandOptionUiState(
                stripEntryId = 1,
                value = 2,
                additionUsed = true,
                multiplicationUsed = false,
                isSelectable = true
            ),
            TileOperandOptionUiState(
                stripEntryId = 2,
                value = 222,
                additionUsed = false,
                multiplicationUsed = true,
                isSelectable = true
            ),
            TileOperandOptionUiState(
                stripEntryId = 3,
                value = 4,
                additionUsed = true,
                multiplicationUsed = true,
                isSelectable = true
            )
        )
    )
)
