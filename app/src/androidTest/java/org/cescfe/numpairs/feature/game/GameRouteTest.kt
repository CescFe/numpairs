package org.cescfe.numpairs.feature.game

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
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
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.construction.resolvedTile
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.feature.game.presentation.CommittedPuzzleMutation
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.TileAssignmentCommit
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenRobot
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun exposes_initial_and_committed_puzzle_changes_to_the_caller() {
        val observedPuzzle = AtomicReference<Puzzle?>()

        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "4 pairs",
                    initialPuzzle = samplePuzzle,
                    gameSessionKey = "observed-puzzle",
                    onPuzzleChanged = observedPuzzle::set
                )
            }
        }

        composeTestRule.waitUntil {
            observedPuzzle.get() == samplePuzzle
        }

        GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        ).tapStripItem(index = 0)
            .enterStripValue("1")
            .submitStripEntryInput()

        composeTestRule.waitUntil {
            observedPuzzle.get()?.strip?.items?.get(0) == StripItem.PlayerEntered(1)
        }

        assertEquals(
            StripItem.PlayerEntered(1),
            observedPuzzle.get()?.strip?.items?.get(0)
        )
    }

    @Test
    fun exposes_each_effective_puzzle_mutation_synchronously_without_conflating_rapid_commits() {
        val committedMutations = mutableListOf<CommittedPuzzleMutation>()

        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "4 pairs",
                    initialPuzzle = samplePuzzle,
                    gameSessionKey = "committed-puzzle-mutations",
                    onPuzzleMutationCommitted = committedMutations::add
                )
            }
        }

        GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        ).tapStripItem(index = 5)
            .enterStripValue("100")
            .submitStripEntryInput()
            .tapStripItem(index = 5)
            .replaceStripValue("101")
            .submitStripEntryInput()
            .tapStripItem(index = 5)
            .tapStripEntryClearAction()
            .scrollToBoard()
            .tapTileLeftOperand(index = 0)
            .tapOperandOption(entryId = 2)
            .tapOperatorOption(Operator.ADDITION)
            .tapOperandOption(entryId = 4)
            .tapTileReset(index = 0)

        composeTestRule.runOnIdle {
            assertEquals(7, committedMutations.size)
            assertEquals(StripItem.PlayerEntered(100), committedMutations[0].puzzle.strip.items[5])
            assertEquals(StripItem.PlayerEntered(101), committedMutations[1].puzzle.strip.items[5])
            assertEquals(StripItem.Hidden, committedMutations[2].puzzle.strip.items[5])
            assertEquals(Operator.ADDITION, committedMutations[4].puzzle.board.tiles[0].expression.operator)
            assertEquals(samplePuzzle.board.tiles[0], committedMutations.last().puzzle.board.tiles[0])
            assertEquals(
                listOf(false, true, true, false, false, false, true),
                committedMutations.map(CommittedPuzzleMutation::isCorrection)
            )
        }
    }

    @Test
    fun transient_and_no_op_interactions_do_not_report_puzzle_mutations() {
        val committedMutations = mutableListOf<CommittedPuzzleMutation>()
        val initialPuzzle = puzzleWithEnteredStripAndNonPristineTile()

        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "4 pairs",
                    initialPuzzle = initialPuzzle,
                    gameSessionKey = "ignored-puzzle-interactions",
                    onPuzzleMutationCommitted = committedMutations::add
                )
            }
        }

        val screen = GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        )
        screen.tapStripItem(index = 5)
            .submitStripEntryInput()
            .scrollToBoard()
            .tapTileOperator(index = 1)
            .pressBack()
            .tapStripItem(index = 0)
            .enterStripValue("999")
            .submitStripEntryInput()

        composeTestRule.runOnIdle {
            assertEquals(emptyList<CommittedPuzzleMutation>(), committedMutations)
        }
    }

    @Test
    fun one_action_that_commits_a_pending_strip_edit_and_resets_a_tile_reports_both_mutations() {
        val committedMutations = mutableListOf<CommittedPuzzleMutation>()
        val initialPuzzle = puzzleWithEnteredStripAndNonPristineTile()

        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "4 pairs",
                    initialPuzzle = initialPuzzle,
                    gameSessionKey = "compound-puzzle-mutation",
                    onPuzzleMutationCommitted = committedMutations::add
                )
            }
        }

        GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        ).tapStripItem(index = 5)
            .replaceStripValue("101")
            .tapTileReset(index = 0)

        composeTestRule.runOnIdle {
            assertEquals(2, committedMutations.size)
            assertEquals(StripItem.PlayerEntered(101), committedMutations[0].puzzle.strip.items[5])
            assertEquals(Operator.ADDITION, committedMutations[0].puzzle.board.tiles[0].expression.operator)
            assertEquals(StripItem.PlayerEntered(101), committedMutations[1].puzzle.strip.items[5])
            assertEquals(samplePuzzle.board.tiles[0], committedMutations[1].puzzle.board.tiles[0])
            assertTrue(committedMutations.all(CommittedPuzzleMutation::isCorrection))
        }
    }

    @Test
    fun exposes_each_tile_assignment_commit_once_without_replaying_it() {
        val commits = mutableListOf<TileAssignmentCommit>()
        val committedMutations = mutableListOf<CommittedPuzzleMutation>()
        val callbackOrder = mutableListOf<String>()
        var recompositionMarker by mutableStateOf(0)
        var puzzleResetKey by mutableStateOf(0)

        composeTestRule.setContent {
            NumPairsTheme {
                Column {
                    Text(text = recompositionMarker.toString())
                    GameRoute(
                        title = "4 pairs",
                        initialPuzzle = oneOperatorAwayFromSolvedOnePairPuzzle(),
                        gameSessionKey = "assignment-commit",
                        puzzleResetKey = puzzleResetKey,
                        onPuzzleMutationCommitted = { mutation ->
                            committedMutations += mutation
                            callbackOrder += "mutation"
                        },
                        onTileAssignmentCommitted = { commit ->
                            commits += commit
                            callbackOrder += "assignment"
                        }
                    )
                }
            }
        }

        GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        ).scrollToBoard()
            .tapTileOperator(index = 1)
            .tapOperatorOption(Operator.MULTIPLICATION)

        composeTestRule.waitUntil {
            commits.size == 1
        }
        composeTestRule.runOnIdle {
            assertEquals(
                TileAssignmentCommit(
                    tileIndex = 1,
                    madeTileCorrect = true,
                    madePuzzleSolved = true
                ),
                commits.single()
            )
            assertEquals(listOf("mutation", "assignment"), callbackOrder)
            assertEquals(1, committedMutations.size)
            assertTrue(committedMutations.single().puzzle.isSolved)
            recompositionMarker += 1
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertEquals(1, commits.size)
            assertEquals(1, committedMutations.size)
            puzzleResetKey += 1
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertEquals(1, commits.size)
            assertEquals(1, committedMutations.size)
        }
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
                        gameSessionKey = GeneratedModes.FOUR_PAIRS.id.value
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
    val firstOperand = ResolvedOperandAssignment(value = 1, stripEntryId = StripEntryId(0))
    val secondOperand = ResolvedOperandAssignment(value = 2, stripEntryId = StripEntryId(1))

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

private fun oneOperatorAwayFromSolvedOnePairPuzzle(): Puzzle {
    val solvedPuzzle = solvedOnePairPuzzle()
    val multiplicationTile = solvedPuzzle.board.tiles[1]

    return solvedPuzzle.copy(
        board = Board(
            tiles = solvedPuzzle.board.tiles.toMutableList().apply {
                set(
                    1,
                    multiplicationTile.copy(
                        expression = multiplicationTile.expression.copy(
                            operator = Operator.Hidden
                        )
                    )
                )
            }
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

private fun puzzleWithEnteredStripAndNonPristineTile(): Puzzle = samplePuzzle
    .withStripItem(index = 5, item = StripItem.PlayerEntered(100))
    .copy(
        board = Board(
            tiles = samplePuzzle.board.tiles.toMutableList().apply {
                val tile = get(0)
                set(
                    0,
                    tile.copy(
                        expression = tile.expression.copy(operator = Operator.ADDITION)
                    )
                )
            }
        )
    )
