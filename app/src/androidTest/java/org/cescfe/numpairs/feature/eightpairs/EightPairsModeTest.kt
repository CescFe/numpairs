package org.cescfe.numpairs.feature.eightpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleProvider
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleProviderFactory
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EightPairsModeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun menuShowsTutorialFourPairsAndEightPairs() {
        setContent()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.menu_tutorial_button))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.menu_four_pairs_button))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.menu_eight_pairs_button))
            .assertIsDisplayed()
    }

    @Test
    fun selectingEightPairsFromMenuShowsTheEightPairsScreen() {
        val puzzleProvider = setContent()

        navigateToEightPairs()

        composeTestRule
            .onNodeWithText(string(R.string.eight_pairs_screen_title))
            .assertIsDisplayed()
        assertEquals(1, puzzleProvider.requestCount)
    }

    @Test
    fun systemBackFromEightPairsReturnsToTheMenu() {
        setContent()
        navigateToEightPairs()

        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    @Test
    fun topAppBarBackFromEightPairsReturnsToTheMenu() {
        setContent()
        navigateToEightPairs()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BACK_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    private fun setContent(): RecordingGeneratedPuzzleProvider {
        val puzzleProvider = RecordingGeneratedPuzzleProvider(initialEightPairsPuzzle())

        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedModeRegistry = GeneratedModes.registry,
                    generatedPuzzleProviderFactory = eightPairsProviderFactory(puzzleProvider = puzzleProvider)
                )
            }
        }

        return puzzleProvider
    }

    private fun navigateToEightPairs() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)
            .assertIsDisplayed()
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun initialEightPairsPuzzle(): Puzzle = Puzzle(
        board = Board(
            tiles = List(EIGHT_PAIRS_ENTRY_COUNT) { index ->
                Tile(
                    expression = Expression(
                        leftOperand = Expression.Operand.Hidden,
                        operator = Operator.Hidden,
                        rightOperand = Expression.Operand.Hidden
                    ),
                    result = index + 2
                )
            }
        ),
        strip = Strip.fromItems(
            items = List(EIGHT_PAIRS_ENTRY_COUNT) { StripItem.Hidden }
        )
    )

    private fun string(stringResId: Int): String = composeTestRule.activity.getString(stringResId)

    private fun eightPairsProviderFactory(puzzleProvider: GeneratedPuzzleProvider): GeneratedPuzzleProviderFactory =
        GeneratedPuzzleProviderFactory { mode ->
            require(mode == GeneratedModes.EIGHT_PAIRS)
            puzzleProvider
        }

    private class RecordingGeneratedPuzzleProvider(private val puzzle: Puzzle) : GeneratedPuzzleProvider {
        var requestCount = 0
            private set

        override fun nextPuzzle(): Puzzle {
            requestCount += 1
            return puzzle
        }
    }

    private companion object {
        const val EIGHT_PAIRS_ENTRY_COUNT = 16
    }
}
