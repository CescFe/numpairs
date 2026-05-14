package org.cescfe.numpairs.feature.menu.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun menuScreenDisplaysTheTutorialEntryPoint() {
        composeTestRule.setContent {
            NumPairsTheme {
                MenuScreen()
            }
        }

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.menu_tutorial_button))
            .assertIsDisplayed()
    }

    @Test
    fun tappingTutorialInvokesTheCallback() {
        var tutorialSelections = 0

        composeTestRule.setContent {
            NumPairsTheme {
                MenuScreen(
                    onTutorialSelected = {
                        tutorialSelections += 1
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, tutorialSelections)
        }
    }
}
