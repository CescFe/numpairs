package org.cescfe.numpairs.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        composeTestRule.setContent {
            NumPairsTheme {
                GameScreen(uiState = GameUiState.from(PuzzleSamples.prototype))
            }
        }
    }

    @Test
    fun launchesSuccessfully() {
        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.app_name)
            )
            .assertIsDisplayed()
    }

    @Test
    fun displaysPuzzleBoard() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun displaysStrip() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP)
            .assertIsDisplayed()
    }
}
