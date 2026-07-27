package org.cescfe.numpairs.feature.menu.ui

object MenuScreenTestTags {
    const val SCREEN = "menu_screen"
    const val DAILY_SPLIT_CTA = "menu_daily_split_cta"
    const val DAILY_BUTTON = "menu_daily_button"
    const val DAILY_CALENDAR_BUTTON = "menu_daily_calendar_button"
    const val RESUME_BUTTON = "menu_resume_button"
    const val QUICK_SPLIT_CTA = "menu_quick_split_cta"
    const val QUICK_BUTTON = "menu_quick_button"
    const val QUICK_DIFFICULTY_BUTTON = "menu_quick_difficulty_button"
    const val QUICK_DIFFICULTY_MENU = "menu_quick_difficulty_menu"
    const val CLASSIC_SPLIT_CTA = "menu_classic_split_cta"
    const val CLASSIC_BUTTON = "menu_classic_button"
    const val CLASSIC_DIFFICULTY_BUTTON = "menu_classic_difficulty_button"
    const val CLASSIC_DIFFICULTY_MENU = "menu_classic_difficulty_menu"
    const val TUTORIAL_BUTTON = "menu_tutorial_button"
    const val SETTINGS_ACTION = "menu_settings_action"
    const val SESSION_CHOICE_DIALOG = "generated_session_choice_dialog"
    const val SESSION_CHOICE_RESUME_BUTTON = "generated_session_choice_resume_button"
    const val SESSION_CHOICE_NEW_PUZZLE_BUTTON = "generated_session_choice_new_puzzle_button"

    fun difficultyOption(id: GeneratedDifficultyMenuOptionId): String = "menu_generated_difficulty_option_${id.value}"
}
