package org.cescfe.numpairs

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.feature.game.ui.GameScreenTestTags
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityStartupTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun coldStartShowsMenuAfterSplashAndSystemBackFromTutorialReturnsToTheMenu() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()

        pressBack()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    @Test
    fun gameScreenTopAppBarBackButtonReturnsToTheMenu() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BACK_BUTTON)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(backButtonContentDescription())
            .performClick()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    @Test
    fun tutorialRulesHelperOpensAndDismissesWithoutChangingPuzzleState() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("1")
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(rulesHelperActionContentDescription())
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_CLOSE_BUTTON)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(closeRulesHelperContentDescription())
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_player_entered_content_description,
                    "1"
                )
            )

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .performClick()
        pressBack()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    private fun backButtonContentDescription(): String = string(R.string.back_button_content_description)

    private fun rulesHelperActionContentDescription(): String = string(R.string.rules_helper_action_content_description)

    private fun closeRulesHelperContentDescription(): String = string(R.string.rules_helper_close_content_description)

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }
}
