package org.cescfe.numpairs.feature.game.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.game.ui.help.SolvingTipsDialog
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SolvingTipsDialogTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysStaticSolvingTipsContent() {
        composeTestRule.setContent {
            NumPairsTheme {
                SolvingTipsDialog(onDismiss = {})
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_CONTENT)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(string(R.string.solving_tips_close_content_description))
            .assertIsDisplayed()

        assertSolvingTipsTextExists(R.string.solving_tips_title)
        assertSolvingTipsTextExists(R.string.solving_tips_strip_title)
        assertSolvingTipsTextExists(R.string.solving_tips_strip_hidden_range)
        assertSolvingTipsTextExists(R.string.solving_tips_strip_highest_anchor)
        assertSolvingTipsTextExists(R.string.solving_tips_products_title)
        assertSolvingTipsTextExists(R.string.solving_tips_products_large_results)
        assertSolvingTipsTextExists(R.string.solving_tips_products_factors)
        assertSolvingTipsTextExists(R.string.solving_tips_sums_title)
        assertSolvingTipsTextExists(R.string.solving_tips_sums_prime_results)
        assertSolvingTipsTextExists(R.string.solving_tips_ui_clues_title)
        assertSolvingTipsTextExists(R.string.solving_tips_ui_clues_operand_usage)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_PRACTICE_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.solving_tips_practice_button))
            .assertIsDisplayed()
    }

    @Test
    fun closeIconDismissesSolvingTipsDialog() {
        var isDialogVisible by mutableStateOf(true)
        var dismissCount = 0

        composeTestRule.setContent {
            NumPairsTheme {
                if (isDialogVisible) {
                    SolvingTipsDialog(
                        onDismiss = {
                            dismissCount += 1
                            isDialogVisible = false
                        }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_CLOSE_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertEquals(1, dismissCount)
        }
    }

    @Test
    fun practiceTipsActionDismissesSolvingTipsDialogAndInvokesCallback() {
        var isDialogVisible by mutableStateOf(true)
        var dismissCount = 0
        var practiceTipsRequestCount = 0

        composeTestRule.setContent {
            NumPairsTheme {
                if (isDialogVisible) {
                    SolvingTipsDialog(
                        onDismiss = {
                            dismissCount += 1
                            isDialogVisible = false
                        },
                        onPracticeTipsRequested = {
                            practiceTipsRequestCount += 1
                        }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_PRACTICE_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertEquals(1, dismissCount)
            assertEquals(1, practiceTipsRequestCount)
        }
    }

    @Test
    fun systemBackDismissesSolvingTipsDialog() {
        var isDialogVisible by mutableStateOf(true)
        var dismissCount = 0

        composeTestRule.setContent {
            NumPairsTheme {
                if (isDialogVisible) {
                    SolvingTipsDialog(
                        onDismiss = {
                            dismissCount += 1
                            isDialogVisible = false
                        }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertIsDisplayed()

        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertEquals(1, dismissCount)
        }
    }

    private fun assertSolvingTipsTextExists(stringResId: Int) {
        composeTestRule
            .onNodeWithText(string(stringResId))
            .assert(hasText(string(stringResId)))
    }

    private fun string(stringResId: Int): String = composeTestRule.activity.getString(stringResId)
}
