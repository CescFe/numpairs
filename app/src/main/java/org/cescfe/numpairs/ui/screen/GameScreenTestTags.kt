package org.cescfe.numpairs.ui.screen

object GameScreenTestTags {
    const val SCREEN = "game_screen"
    const val BOARD = "game_board"
    const val STRIP = "game_strip"
    const val STRIP_ENTRY_DIALOG = "strip_entry_dialog"
    const val STRIP_ENTRY_INPUT = "strip_entry_input"
    const val STRIP_ENTRY_RANGE = "strip_entry_range"
    const val STRIP_ENTRY_CONFIRM = "strip_entry_confirm"
    const val STRIP_ENTRY_CANCEL = "strip_entry_cancel"

    fun stripItem(index: Int): String = "strip_item_$index"
}
