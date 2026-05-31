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
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RulesHelperDialogTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displaysStaticRulesHelperContent() {
        composeTestRule.setContent {
            NumPairsTheme {
                RulesHelperDialog(onDismiss = {})
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription(string(R.string.rules_helper_close_content_description))
            .assertIsDisplayed()

        assertRulesHelperTextExists(R.string.rules_helper_title)
        assertRulesHelperTextExists(R.string.rules_helper_strip_title)
        assertRulesHelperTextExists(R.string.rules_helper_strip_body)
        assertRulesHelperTextExists(R.string.rules_helper_board_title)
        assertRulesHelperTextExists(R.string.rules_helper_board_body)
        assertRulesHelperTextExists(R.string.rules_helper_expression_title)
        assertRulesHelperTextExists(R.string.rules_helper_expression_body)
        assertRulesHelperTextExists(R.string.rules_helper_pairs_title)
        assertRulesHelperTextExists(R.string.rules_helper_pairs_body)
        assertRulesHelperTextExists(R.string.rules_helper_completion_title)
        assertRulesHelperTextExists(R.string.rules_helper_completion_body)
    }

    @Test
    fun closeIconDismissesRulesHelperDialog() {
        var isDialogVisible by mutableStateOf(true)
        var dismissCount = 0

        composeTestRule.setContent {
            NumPairsTheme {
                if (isDialogVisible) {
                    RulesHelperDialog(
                        onDismiss = {
                            dismissCount += 1
                            isDialogVisible = false
                        }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_CLOSE_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertEquals(1, dismissCount)
        }
    }

    @Test
    fun systemBackDismissesRulesHelperDialog() {
        var isDialogVisible by mutableStateOf(true)
        var dismissCount = 0

        composeTestRule.setContent {
            NumPairsTheme {
                if (isDialogVisible) {
                    RulesHelperDialog(
                        onDismiss = {
                            dismissCount += 1
                            isDialogVisible = false
                        }
                    )
                }
            }
        }

        pressBack()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertEquals(1, dismissCount)
        }
    }

    private fun assertRulesHelperTextExists(stringResId: Int) {
        composeTestRule
            .onNodeWithText(string(stringResId))
            .assert(hasText(string(stringResId)))
    }

    private fun string(stringResId: Int): String = composeTestRule.activity.getString(stringResId)
}
