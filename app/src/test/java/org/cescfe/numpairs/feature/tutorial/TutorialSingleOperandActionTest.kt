package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.feature.game.GameTileExpressionSlot
import org.cescfe.numpairs.feature.game.GameTileExpressionSlotHighlight
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialSingleOperandActionTest {
    private val scenario = TutorialContent.scenario(TutorialScenarioId.TWO_PAIR_PRACTICE)
    private val step = TutorialStep(
        order = 1,
        scenarioId = scenario.id,
        playerFacingCopyResId = R.string.tutorial_step_one_copy,
        highlightedTargets = listOf(
            TutorialHighlightTarget.StripEntries(indexes = listOf(REQUIRED_STRIP_ENTRY_ID)),
            TutorialHighlightTarget.TileOperandSlot(
                tileIndex = TARGET_TILE_INDEX,
                slot = OperandSlot.LEFT
            )
        ),
        requiredAction = TutorialRequiredAction.PlaceTileOperand(
            tileIndex = TARGET_TILE_INDEX,
            slot = OperandSlot.LEFT,
            stripEntryId = REQUIRED_STRIP_ENTRY_ID
        ),
        completionPredicate = TutorialStepCompletionPredicate.TileOperandPlaced(
            tileIndex = TARGET_TILE_INDEX,
            slot = OperandSlot.LEFT,
            value = REQUIRED_VALUE
        )
    )

    @Test
    fun single_operand_models_reject_invalid_indexes_and_values() {
        assertThrows(IllegalArgumentException::class.java) {
            TutorialRequiredAction.PlaceTileOperand(
                tileIndex = -1,
                slot = OperandSlot.LEFT,
                stripEntryId = REQUIRED_STRIP_ENTRY_ID
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            TutorialRequiredAction.PlaceTileOperand(
                tileIndex = TARGET_TILE_INDEX,
                slot = OperandSlot.LEFT,
                stripEntryId = -1
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            TutorialStepCompletionPredicate.TileOperandPlaced(
                tileIndex = -1,
                slot = OperandSlot.LEFT,
                value = REQUIRED_VALUE
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            TutorialStepCompletionPredicate.TileOperandPlaced(
                tileIndex = TARGET_TILE_INDEX,
                slot = OperandSlot.LEFT,
                value = 0
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            TutorialHighlightTarget.TileOperandSlot(
                tileIndex = -1,
                slot = OperandSlot.LEFT
            )
        }
    }

    @Test
    fun single_operand_step_completes_only_when_the_target_slot_contains_the_required_value() {
        val initialState = GameUiState.from(scenario.initialPuzzle)
        val wrongValueState = initialState.withOperandLabel(
            tileIndex = TARGET_TILE_INDEX,
            slot = OperandSlot.LEFT,
            label = "2"
        )
        val wrongSlotState = initialState.withOperandLabel(
            tileIndex = TARGET_TILE_INDEX,
            slot = OperandSlot.RIGHT,
            label = REQUIRED_VALUE.toString()
        )
        val completedState = initialState.withOperandLabel(
            tileIndex = TARGET_TILE_INDEX,
            slot = OperandSlot.LEFT,
            label = REQUIRED_VALUE.toString()
        )

        assertFalse(step.isComplete(initialState))
        assertFalse(step.isComplete(wrongValueState))
        assertFalse(step.isComplete(wrongSlotState))
        assertTrue(step.isComplete(completedState))
    }

    @Test
    fun single_operand_policy_allows_only_the_target_slot_and_required_strip_entry() {
        val policy = step.toInteractionPolicy(
            scenario = scenario,
            uiState = GameUiState.from(scenario.initialPuzzle)
        )

        assertFalse(policy.canTapStripItem(REQUIRED_STRIP_ENTRY_ID))
        assertFalse(policy.canConfirmStripItemEntry(REQUIRED_STRIP_ENTRY_ID, REQUIRED_VALUE))
        assertTrue(policy.canTapTileLeftOperand(TARGET_TILE_INDEX))
        assertFalse(policy.canTapTileRightOperand(TARGET_TILE_INDEX))
        assertFalse(policy.canTapTileLeftOperand(1))
        assertFalse(policy.canTapTileOperator(TARGET_TILE_INDEX))
        assertFalse(policy.canTapTileReset(TARGET_TILE_INDEX))
        assertTrue(
            policy.canConfirmTileOperand(
                TARGET_TILE_INDEX,
                OperandSlot.LEFT,
                REQUIRED_STRIP_ENTRY_ID
            )
        )
        assertFalse(policy.canConfirmTileOperand(TARGET_TILE_INDEX, OperandSlot.RIGHT, REQUIRED_STRIP_ENTRY_ID))
        assertFalse(policy.canConfirmTileOperand(TARGET_TILE_INDEX, OperandSlot.LEFT, 1))
        assertFalse(policy.canConfirmTileOperand(1, OperandSlot.LEFT, REQUIRED_STRIP_ENTRY_ID))
    }

    @Test
    fun single_operand_highlights_focus_the_required_number_and_exact_operand_slot() {
        val highlightState = step.toHighlightState(
            scenario = scenario,
            uiState = GameUiState.from(scenario.initialPuzzle)
        )

        assertEquals(setOf(REQUIRED_STRIP_ENTRY_ID), highlightState.stripEntryIndexes)
        assertEquals(emptySet<Int>(), highlightState.tileIndexes)
        assertEquals(
            setOf(
                GameTileExpressionSlotHighlight(
                    tileIndex = TARGET_TILE_INDEX,
                    slot = GameTileExpressionSlot.LEFT_OPERAND
                )
            ),
            highlightState.tileExpressionSlots
        )
        assertFalse(
            highlightState.isTileExpressionSlotHighlighted(
                tileIndex = TARGET_TILE_INDEX,
                slot = GameTileExpressionSlot.OPERATOR
            )
        )
        assertFalse(
            highlightState.isTileExpressionSlotHighlighted(
                tileIndex = TARGET_TILE_INDEX,
                slot = GameTileExpressionSlot.RIGHT_OPERAND
            )
        )
    }
}

private fun GameUiState.withOperandLabel(tileIndex: Int, slot: OperandSlot, label: String): GameUiState = copy(
    tiles = tiles.toMutableList().apply {
        val tile = get(tileIndex)
        set(
            tileIndex,
            when (slot) {
                OperandSlot.LEFT -> tile.copy(leftOperandLabel = label)
                OperandSlot.RIGHT -> tile.copy(rightOperandLabel = label)
            }
        )
    }
)

private const val TARGET_TILE_INDEX = 0
private const val REQUIRED_STRIP_ENTRY_ID = 0
private const val REQUIRED_VALUE = 1
