package org.cescfe.numpairs.feature.fourpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.fourpairs.FourPairsLowDifficultyPuzzleGenerator
import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.feature.game.ui.GameScreenTestTags
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FourPairsCompletionActionsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun newPuzzleActionGeneratesFreshPuzzleAndClearsPreviousGameState() {
        val firstSolvedPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 2026).generateWithSolution().solvedPuzzle
        val firstPuzzle = firstSolvedPuzzle.withHiddenOperatorAt(tileIndex = 0)
        val secondPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 42).generate()
        assertNotEquals(firstPuzzle.board.tiles[0].result, secondPuzzle.board.tiles[0].result)
        val puzzleProvider = QueueFourPairsPuzzleProvider(firstPuzzle, secondPuzzle)

        setContent(puzzleProvider = puzzleProvider)

        navigateToFourPairs()
        completeFirstTile(operator = firstSolvedPuzzle.board.tiles[0].expression.operator)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
            .assertIsDisplayed()
            .performClick()

        assertEquals(2, puzzleProvider.requestCount)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertDoesNotExist()

        val stripMask = currentStripMask()
        assertEquals(3, stripMask.knownEntryIds.size)
        assertEquals(5, stripMask.hiddenEntryIds.size)
        assertFirstTileExpressionIsHidden()
        assertFirstTileShowsResult(secondPuzzle.board.tiles[0].result)
    }

    @Test
    fun returnToMenuActionNavigatesBackToMenuAfterCompletion() {
        val solvedPuzzle = FourPairsLowDifficultyPuzzleGenerator(seed = 81).generateWithSolution().solvedPuzzle
        val initialPuzzle = solvedPuzzle.withHiddenOperatorAt(tileIndex = 0)
        val puzzleProvider = QueueFourPairsPuzzleProvider(initialPuzzle)

        setContent(puzzleProvider = puzzleProvider)

        navigateToFourPairs()
        completeFirstTile(operator = solvedPuzzle.board.tiles[0].expression.operator)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_RETURN_TO_MENU)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    private fun setContent(puzzleProvider: FourPairsPuzzleProvider) {
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(fourPairsPuzzleProvider = puzzleProvider)
            }
        }
    }

    private fun navigateToFourPairs() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .assertIsDisplayed()
            .performClick()
    }

    private fun completeFirstTile(operator: Operator) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
    }

    private fun assertFirstTileExpressionIsHidden() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_left_operand_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_operator_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_right_operand_hidden_content_description))
    }

    private fun assertFirstTileShowsResult(result: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText(result.toString())))
    }

    private fun currentStripMask(): StripMask {
        val hiddenContentDescription = string(R.string.strip_item_hidden_content_description)
        val knownContentDescriptionPrefix = string(
            R.string.strip_item_known_content_description,
            ""
        )
        val knownEntryIds = mutableListOf<Int>()
        val hiddenEntryIds = mutableListOf<Int>()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP)
            .performScrollTo()

        repeat(Strip.NUMBER_COUNT) { index ->
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

    private fun Puzzle.withHiddenOperatorAt(tileIndex: Int): Puzzle = copy(
        board = Board(
            tiles = board.tiles.toMutableList().apply {
                val tile = get(tileIndex)
                set(
                    tileIndex,
                    tile.copy(
                        expression = tile.expression.copy(operator = Operator.Hidden)
                    )
                )
            }
        )
    )

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }

    private class QueueFourPairsPuzzleProvider(private vararg val puzzles: Puzzle) : FourPairsPuzzleProvider {
        var requestCount = 0
            private set

        override fun nextPuzzle(): Puzzle {
            val puzzle = puzzles.getOrNull(requestCount)
                ?: error("No fake 4 Pairs puzzle configured for request $requestCount.")
            requestCount += 1

            return puzzle
        }
    }

    private data class StripMask(val knownEntryIds: List<Int>, val hiddenEntryIds: List<Int>)
}
