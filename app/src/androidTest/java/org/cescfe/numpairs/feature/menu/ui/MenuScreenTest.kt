package org.cescfe.numpairs.feature.menu.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun generated_mode_rows_identify_the_selected_challenge_and_emit_distinct_actions() {
        var playCount = 0
        var difficultyCount = 0
        setContent(
            onFourPairsSelected = { playCount += 1 },
            onFourPairsDifficultySelected = { difficultyCount += 1 }
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .assertIsDisplayed()
            .assertTextEquals(fourPairsState.challengeName)
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_play_generated_challenge_content_description,
                    fourPairsState.challengeName
                )
            )
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_choose_generated_difficulty_content_description,
                    fourPairsState.modeName
                )
            )
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, playCount)
            assertEquals(1, difficultyCount)
        }
    }

    @Test
    fun difficulty_actions_are_square_and_match_the_primary_action_height() {
        setContent()

        listOf(
            MenuScreenTestTags.FOUR_PAIRS_BUTTON to MenuScreenTestTags.FOUR_PAIRS_DIFFICULTY_BUTTON,
            MenuScreenTestTags.EIGHT_PAIRS_BUTTON to MenuScreenTestTags.EIGHT_PAIRS_DIFFICULTY_BUTTON
        ).forEach { (playTag, difficultyTag) ->
            val playBounds = composeTestRule
                .onNodeWithTag(playTag)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot
            val difficultyBounds = composeTestRule
                .onNodeWithTag(difficultyTag)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .boundsInRoot

            assertEquals(playBounds.height, difficultyBounds.height, 0.5f)
            assertEquals(difficultyBounds.width, difficultyBounds.height, 0.5f)
            assertTrue(playBounds.right < difficultyBounds.left)
        }
    }

    private fun setContent(onFourPairsSelected: () -> Unit = {}, onFourPairsDifficultySelected: () -> Unit = {}) {
        composeTestRule.setContent {
            NumPairsTheme {
                MenuScreen(
                    fourPairsMode = fourPairsState,
                    eightPairsMode = eightPairsState,
                    onFourPairsSelected = onFourPairsSelected,
                    onFourPairsDifficultySelected = onFourPairsDifficultySelected
                )
            }
        }
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String =
        composeTestRule.activity.getString(stringResId, *formatArgs)

    private companion object {
        val fourPairsState = GeneratedModeMenuUiState(
            modeName = "4 pairs",
            challengeName = "4 pairs · Low"
        )
        val eightPairsState = GeneratedModeMenuUiState(
            modeName = "8 pairs",
            challengeName = "8 pairs · Medium"
        )
    }
}
