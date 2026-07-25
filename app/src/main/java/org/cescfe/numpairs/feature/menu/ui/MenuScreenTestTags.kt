package org.cescfe.numpairs.feature.menu.ui

object MenuScreenTestTags {
    const val SCREEN = "menu_screen"
    const val RESUME_BUTTON = "menu_resume_button"
    const val QUICK_BUTTON = "menu_quick_button"
    const val QUICK_DIFFICULTY_BUTTON = "menu_quick_difficulty_button"
    const val TUTORIAL_BUTTON = "menu_tutorial_button"
    const val SETTINGS_ACTION = "menu_settings_action"
    const val FOUR_PAIRS_SPLIT_CTA = "menu_four_pairs_split_cta"
    const val EIGHT_PAIRS_SPLIT_CTA = "menu_eight_pairs_split_cta"
    const val FOUR_PAIRS_BUTTON = "menu_four_pairs_button"
    const val EIGHT_PAIRS_BUTTON = "menu_eight_pairs_button"
    const val FOUR_PAIRS_DIFFICULTY_BUTTON = "menu_four_pairs_difficulty_button"
    const val EIGHT_PAIRS_DIFFICULTY_BUTTON = "menu_eight_pairs_difficulty_button"
    const val FOUR_PAIRS_DIFFICULTY_MENU = "menu_four_pairs_difficulty_menu"
    const val EIGHT_PAIRS_DIFFICULTY_MENU = "menu_eight_pairs_difficulty_menu"
    const val SESSION_CHOICE_DIALOG = "generated_session_choice_dialog"
    const val SESSION_CHOICE_RESUME_BUTTON = "generated_session_choice_resume_button"
    const val SESSION_CHOICE_NEW_PUZZLE_BUTTON = "generated_session_choice_new_puzzle_button"

    fun difficultyOption(id: GeneratedDifficultyMenuOptionId): String = "menu_generated_difficulty_option_${id.value}"
}
