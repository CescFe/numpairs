package org.cescfe.numpairs.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        viewModel = GameViewModel()

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsState()

            NumPairsTheme {
                GameScreen(
                    uiState = uiState,
                    onStripItemTapped = viewModel::onStripItemTapped,
                    onStripItemEntryDismissed = viewModel::onStripItemEntryDismissed,
                    onStripItemEntryConfirmed = viewModel::onStripItemEntryConfirmed
                )
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

    @Test
    fun tappingHiddenStripItemOpensEntryDialog() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_DIALOG)
            .assertIsDisplayed()
    }

    @Test
    fun confirmingEntryDialogCompletesTheHiddenStripItem() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("9")

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CONFIRM)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("9")))
    }

    @Test
    fun cancellingEntryDialogLeavesTheHiddenStripItemUnchanged() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput("9")

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_CANCEL)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("?")))
    }
}
