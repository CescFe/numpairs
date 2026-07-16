package org.cescfe.numpairs.feature.tutorial

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenRobot
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinalValidationRouteTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shows_the_authored_puzzle_without_tutorial_guidance_or_highlights() {
        setRoute()

        val screen = GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        )
        screen
            .assertTitleDisplayed(composeTestRule.activity.getString(R.string.final_validation_screen_title))
            .assertStripDisplayed()
            .assertBoardDisplayed()
            .assertStripItemDescription(
                index = 1,
                stringResId = R.string.strip_item_hidden_content_description
            )
            .assertStripItemNotHighlighted(index = 1)
            .assertLeftOperandNotHighlighted(tileIndex = 0)
            .assertOperatorNotHighlighted(tileIndex = 0)
            .assertRightOperandNotHighlighted(tileIndex = 0)

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.INSTRUCTION_SURFACE)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_INDICATOR)
            .assertDoesNotExist()
    }

    @Test
    fun permits_standard_interactions_and_opens_the_static_rules_helper() {
        setRoute()

        val screen = GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        )
        screen
            .tapStripItem(index = 1)
            .assertStripEntryInputDisplayed()
            .replaceStripValue("3")
            .submitStripEntryInput()
            .tapTileLeftOperand(index = 0)
            .assertOperandSelectorDisplayed()

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertIsDisplayed()
    }

    private fun setRoute() {
        composeTestRule.setContent {
            NumPairsTheme {
                FinalValidationRoute()
            }
        }
    }
}
