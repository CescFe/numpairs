package org.cescfe.numpairs.feature.game.ui

import org.cescfe.numpairs.domain.puzzle.Operator

object GameScreenTestTags {
    const val SCREEN = "game_screen"
    const val BACK_BUTTON = "game_back_button"
    const val SUCCESS_OVERLAY = "success_overlay"
    const val SUCCESS_OVERLAY_MESSAGE = "success_overlay_message"
    const val SUCCESS_OVERLAY_NEW_PUZZLE = "success_overlay_new_puzzle"
    const val SUCCESS_OVERLAY_RETURN_TO_MENU = "success_overlay_return_to_menu"
    const val PUZZLE_OUTCOME = "puzzle_outcome"
    const val PUZZLE_OUTCOME_TITLE = "puzzle_outcome_title"
    const val PUZZLE_OUTCOME_MESSAGE = "puzzle_outcome_message"
    const val LOCAL_RULE_CONFLICT = "local_rule_conflict"
    const val LOCAL_RULE_CONFLICT_MESSAGE = "local_rule_conflict_message"
    const val BOARD = "game_board"
    const val STRIP = "game_strip"
    const val STRIP_ENTRY_INPUT = "strip_entry_input"
    const val STRIP_ENTRY_RANGE = "strip_entry_range"
    const val TILE_OPERAND_SELECTOR = "tile_operand_selector"
    const val TILE_OPERATOR_SELECTOR = "tile_operator_selector"
    const val HINT_ACTION = "hint_action"
    const val HINT_ACTION_DISCOVERY_DOT = "hint_action_discovery_dot"
    const val RULES_HELPER_ACTION = "rules_helper_action"
    const val RULES_HELPER_ACTION_DISCOVERY_DOT = "rules_helper_action_discovery_dot"
    const val RULES_HELPER_DIALOG = "rules_helper_dialog"
    const val RULES_HELPER_CLOSE_BUTTON = "rules_helper_close_button"
    const val RULES_HELPER_PLAY_TUTORIAL_BUTTON = "rules_helper_play_tutorial_button"
    const val RULES_HELPER_CONTENT = "rules_helper_content"
    const val SOLVING_TIPS_DIALOG = "solving_tips_dialog"
    const val SOLVING_TIPS_CLOSE_BUTTON = "solving_tips_close_button"
    const val SOLVING_TIPS_PRACTICE_BUTTON = "solving_tips_practice_button"
    const val SOLVING_TIPS_CONTENT = "solving_tips_content"

    fun stripItem(index: Int): String = "strip_item_$index"

    fun stripUsageIndicator(index: Int, operator: Operator): String = when (operator) {
        Operator.Addition -> "strip_usage_indicator_${index}_addition"
        Operator.Multiplication -> "strip_usage_indicator_${index}_multiplication"
        Operator.Hidden -> error("Hidden operator does not have a strip usage indicator.")
    }

    fun tile(index: Int): String = "tile_$index"

    fun tileLeftOperand(index: Int): String = "tile_left_operand_$index"

    fun tileRightOperand(index: Int): String = "tile_right_operand_$index"

    fun tileOperator(index: Int): String = "tile_operator_$index"

    fun tileReset(index: Int): String = "tile_reset_$index"

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
