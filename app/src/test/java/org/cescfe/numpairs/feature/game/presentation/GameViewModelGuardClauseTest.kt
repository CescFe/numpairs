package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.junit.Assert.assertEquals
import org.junit.Test

class GameViewModelGuardClauseTest {
    @Test
    fun invalid_indexes_are_ignored_when_opening_modals_or_resetting_tiles() {
        assertStateUnchanged { onStripItemTapped(index = -1) }
        assertStateUnchanged { onStripItemTapped(index = 999) }
        assertStateUnchanged { onTileOperatorTapped(index = -1) }
        assertStateUnchanged { onTileOperatorTapped(index = 999) }
        assertStateUnchanged { onTileLeftOperandTapped(index = -1) }
        assertStateUnchanged { onTileRightOperandTapped(index = 999) }
        assertStateUnchanged { onTileResetTapped(index = -1) }
        assertStateUnchanged { onTileResetTapped(index = 999) }
    }

    @Test
    fun confirming_without_an_active_modal_leaves_state_unchanged() {
        assertStateUnchanged { onStripItemEntryInputConfirmed() }
        assertStateUnchanged { onTileOperandSelectionConfirmed(stripEntryId = 2) }
        assertStateUnchanged { onTileOperatorSelectionConfirmed(operator = Operator.ADDITION) }
    }

    @Test
    fun confirming_a_hidden_operator_keeps_the_dialog_open_and_does_not_mutate_the_tile() {
        val viewModel = GameViewModel().apply {
            onTileOperatorTapped(index = 0)
        }

        assertStateUnchanged(viewModel) {
            onTileOperatorSelectionConfirmed(operator = Operator.Hidden)
        }
    }

    @Test
    fun resetting_a_pristine_tile_is_ignored() {
        assertStateUnchanged { onTileResetTapped(index = 0) }
    }
}

private fun assertStateUnchanged(viewModel: GameViewModel = GameViewModel(), action: GameViewModel.() -> Unit) {
    val beforeAction = viewModel.uiState.value

    viewModel.action()

    assertEquals(beforeAction, viewModel.uiState.value)
}
