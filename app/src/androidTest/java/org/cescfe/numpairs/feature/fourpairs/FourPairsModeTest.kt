package org.cescfe.numpairs.feature.fourpairs

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.MainActivity
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FourPairsModeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW

    @Test
    fun selectingFourPairsFromMenuShowsGeneratedPlayablePuzzleContent() {
        navigateToFourPairs()

        composeTestRule
            .onNodeWithText(fourPairsScreenTitle())
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP)
            .assertIsDisplayed()

        repeat(profile.size.stripEntryCount) { index ->
            composeTestRule
                .onNodeWithTag(GameScreenTestTags.stripItem(index))
                .fetchSemanticsNode()
        }

        val initialStripMask = currentStripMask()
        assertEquals(3, initialStripMask.knownEntryIds.size)
        assertEquals(5, initialStripMask.hiddenEntryIds.size)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
            .assertIsDisplayed()

        repeat(profile.size.boardTileCount) { tileIndex ->
            composeTestRule
                .onNodeWithTag(GameScreenTestTags.tile(tileIndex))
                .fetchSemanticsNode()
            assertHiddenTileExpression(tileIndex)
        }
    }

    @Test
    fun fourPairsModeSupportsExistingGameInteractions() {
        navigateToFourPairs()
        val initialStripMask = currentStripMask()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(Operator.ADDITION), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.tileOperandOption(initialStripMask.knownEntryIds.first()),
                useUnmergedTree = true
            )
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP)
            .performScrollTo()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(initialStripMask.hiddenEntryIds.first()))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_RANGE)
            .assertIsDisplayed()
    }

    @Test
    fun systemBackFromFourPairsReturnsToTheMenu() {
        navigateToFourPairs()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()

        pressBack()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    @Test
    fun topAppBarBackFromFourPairsReturnsToTheMenu() {
        navigateToFourPairs()

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

    private fun navigateToFourPairs() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .assertIsDisplayed()
            .performClick()
    }

    private fun assertHiddenTileExpression(tileIndex: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_left_operand_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_operator_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(tileIndex), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_right_operand_hidden_content_description))
    }

    private fun currentStripMask(): StripMask {
        val hiddenContentDescription = string(R.string.strip_item_hidden_content_description)
        val knownContentDescriptionPrefix = string(
            R.string.strip_item_known_content_description,
            ""
        )
        val knownEntryIds = mutableListOf<Int>()
        val hiddenEntryIds = mutableListOf<Int>()

        repeat(profile.size.stripEntryCount) { index ->
            val contentDescriptions = composeTestRule
                .onNodeWithTag(GameScreenTestTags.stripItem(index))
                .fetchSemanticsNode()
                .config[SemanticsProperties.ContentDescription]

            when {
                hiddenContentDescription in contentDescriptions -> hiddenEntryIds += index
                contentDescriptions.any { description ->
                    description.startsWith(knownContentDescriptionPrefix)
                } -> knownEntryIds += index
            }
        }

        return StripMask(
            knownEntryIds = knownEntryIds,
            hiddenEntryIds = hiddenEntryIds
        )
    }

    private fun backButtonContentDescription(): String = string(R.string.back_button_content_description)

    private fun fourPairsScreenTitle(): String = string(R.string.four_pairs_screen_title)

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }

    private data class StripMask(val knownEntryIds: List<Int>, val hiddenEntryIds: List<Int>)
}
