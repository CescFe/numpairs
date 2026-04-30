package org.cescfe.numpairs.ui.screen

import org.cescfe.numpairs.domain.puzzle.Operator

object GameScreenTestTags {
    const val SCREEN = "game_screen"
    const val SUCCESS_OVERLAY = "success_overlay"
    const val SUCCESS_OVERLAY_MESSAGE = "success_overlay_message"
    const val PUZZLE_OUTCOME = "puzzle_outcome"
    const val PUZZLE_OUTCOME_TITLE = "puzzle_outcome_title"
    const val PUZZLE_OUTCOME_MESSAGE = "puzzle_outcome_message"
    const val BOARD = "game_board"
    const val STRIP = "game_strip"
    const val STRIP_ENTRY_DIALOG = "strip_entry_dialog"
    const val STRIP_ENTRY_INPUT = "strip_entry_input"
    const val STRIP_ENTRY_RANGE = "strip_entry_range"
    const val STRIP_ENTRY_CONFIRM = "strip_entry_confirm"
    const val STRIP_ENTRY_CANCEL = "strip_entry_cancel"
    const val TILE_OPERAND_SELECTOR = "tile_operand_selector"
    const val TILE_OPERATOR_SELECTOR = "tile_operator_selector"

    fun stripItem(index: Int): String = "strip_item_$index"

    fun tile(index: Int): String = "tile_$index"

    fun tileLeftOperand(index: Int): String = "tile_left_operand_$index"

    fun tileRightOperand(index: Int): String = "tile_right_operand_$index"

    fun tileOperator(index: Int): String = "tile_operator_$index"

    fun tileOperandOption(entryId: Int): String = "tile_operand_option_$entryId"

    fun tileOperandUsageHint(entryId: Int, operator: Operator): String = when (operator) {
        Operator.Addition -> "tile_operand_usage_hint_${entryId}_addition"
        Operator.Multiplication -> "tile_operand_usage_hint_${entryId}_multiplication"
        Operator.Hidden -> error("Hidden operator does not have an operand usage hint.")
    }

    fun tileOperatorOption(operator: Operator): String = when (operator) {
        Operator.Addition -> "tile_operator_option_addition"
        Operator.Multiplication -> "tile_operator_option_multiplication"
        Operator.Hidden -> error("Hidden operator is not a selectable option.")
    }
}
