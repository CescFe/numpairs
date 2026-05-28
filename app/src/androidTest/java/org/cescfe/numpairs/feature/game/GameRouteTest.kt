package org.cescfe.numpairs.feature.game

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.feature.game.ui.GameScreenRobot
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameRouteTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun displays_the_caller_title_and_initial_puzzle() {
        val routeTitle = "4 pairs"
        val routePuzzle = initialPuzzle.withStripItem(
            index = 0,
            item = StripItem.Known(4)
        )

        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = routeTitle,
                    initialPuzzle = routePuzzle,
                    gameSessionKey = "custom-initial-puzzle"
                )
            }
        }

        GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        ).assertTitleDisplayed(routeTitle)
            .assertStripItemDescription(
                index = 0,
                stringResId = R.string.strip_item_known_content_description,
                "4"
            )
    }

    @Test
    fun game_state_is_isolated_between_route_keys() {
        var gameMode by mutableStateOf(GameRouteMode.TUTORIAL)
        val fourPairsPuzzle = initialPuzzle.withStripItem(
            index = 0,
            item = StripItem.Known(4)
        )

        composeTestRule.setContent {
            NumPairsTheme {
                when (gameMode) {
                    GameRouteMode.TUTORIAL -> GameRoute(
                        title = "Tutorial",
                        initialPuzzle = initialPuzzle,
                        gameSessionKey = "tutorial"
                    )
                    GameRouteMode.FOUR_PAIRS -> GameRoute(
                        title = "4 pairs",
                        initialPuzzle = fourPairsPuzzle,
                        gameSessionKey = "four-pairs"
                    )
                }
            }
        }

        val screen = GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        )

        screen
            .tapStripItem(index = 0)
            .enterStripValue("1")
            .confirmStripEntry()
            .assertStripItemDescription(
                index = 0,
                stringResId = R.string.strip_item_player_entered_content_description,
                "1"
            )

        composeTestRule.runOnIdle {
            gameMode = GameRouteMode.FOUR_PAIRS
        }

        screen
            .assertTitleDisplayed("4 pairs")
            .assertStripItemDescription(
                index = 0,
                stringResId = R.string.strip_item_known_content_description,
                "4"
            )
    }
}

private enum class GameRouteMode {
    TUTORIAL,
    FOUR_PAIRS
}

private fun Puzzle.withStripItem(index: Int, item: StripItem): Puzzle = copy(
    strip = Strip.fromItems(
        items = strip.items.toMutableList().apply {
            set(index, item)
        }
    )
)
