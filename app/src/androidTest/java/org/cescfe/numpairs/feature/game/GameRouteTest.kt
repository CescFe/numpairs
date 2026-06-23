package org.cescfe.numpairs.feature.game

import androidx.activity.ComponentActivity
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicReference
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.assignment.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.construction.resolvedTile
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenRobot
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameRouteTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun renders_caller_top_bar_actions() {
        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "Tutorial",
                    initialPuzzle = samplePuzzle,
                    gameSessionKey = "top-bar-action",
                    topBarActions = {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.testTag(TOP_BAR_ACTION_TAG)
                        ) {
                            Text(text = "A")
                        }
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TOP_BAR_ACTION_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun displays_the_caller_title_and_initial_puzzle() {
        val routeTitle = "4 pairs"
        val routePuzzle = samplePuzzle.withStripItem(
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
    fun exposes_game_ui_state_changes_to_the_caller() {
        val observedUiState = AtomicReference<GameUiState?>()

        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "Tutorial",
                    initialPuzzle = samplePuzzle,
                    gameSessionKey = "observed-ui-state",
                    onGameUiStateChanged = observedUiState::set
                )
            }
        }

        composeTestRule.waitUntil {
            observedUiState.get() != null
        }

        GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        ).tapStripItem(index = 0)

        composeTestRule.waitUntil {
            observedUiState.get()?.stripItemEntryInput?.stripItemIndex == 0
        }

        assertEquals(0, observedUiState.get()?.stripItemEntryInput?.stripItemIndex)
    }

    @Test
    fun solved_puzzle_shows_the_success_overlay_by_default() {
        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "Tutorial",
                    initialPuzzle = solvedOnePairPuzzle(),
                    gameSessionKey = "success-overlay-default"
                )
            }
        }

        GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        ).assertSuccessOverlayVisible()
    }

    @Test
    fun caller_can_disable_the_success_overlay() {
        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "Tutorial",
                    initialPuzzle = solvedOnePairPuzzle(),
                    gameSessionKey = "success-overlay-disabled",
                    isSuccessOverlayEnabled = false
                )
            }
        }

        GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        ).assertSuccessOverlayHidden()
    }

    @Test
    fun game_state_is_isolated_between_route_keys() {
        var gameMode by mutableStateOf(GameRouteMode.TUTORIAL)
        val fourPairsPuzzle = samplePuzzle.withStripItem(
            index = 0,
            item = StripItem.Known(4)
        )

        composeTestRule.setContent {
            NumPairsTheme {
                when (gameMode) {
                    GameRouteMode.TUTORIAL -> GameRoute(
                        title = "Tutorial",
                        initialPuzzle = samplePuzzle,
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
            .submitStripEntryInput()
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

private const val TOP_BAR_ACTION_TAG = "game_route_top_bar_action"

private fun solvedOnePairPuzzle(): Puzzle {
    val firstOperand = ResolvedOperandAssignment(value = 1, stripEntryId = 0)
    val secondOperand = ResolvedOperandAssignment(value = 2, stripEntryId = 1)

    return Puzzle(
        board = Board(
            tiles = listOf(
                resolvedTile(
                    leftOperand = firstOperand,
                    operator = Operator.ADDITION,
                    rightOperand = secondOperand
                ),
                resolvedTile(
                    leftOperand = firstOperand,
                    operator = Operator.MULTIPLICATION,
                    rightOperand = secondOperand
                )
            )
        ),
        strip = Strip.fromItems(
            items = listOf(
                StripItem.Known(1),
                StripItem.Known(2)
            )
        )
    )
}

private fun Puzzle.withStripItem(index: Int, item: StripItem): Puzzle = copy(
    strip = Strip.fromItems(
        items = strip.items.toMutableList().apply {
            set(index, item)
        }
    )
)
