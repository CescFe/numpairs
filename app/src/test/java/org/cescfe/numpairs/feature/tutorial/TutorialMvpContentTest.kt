package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialMvpContentTest {
    @Test
    fun defines_the_authored_tutorial_scenarios() {
        assertEquals(
            listOf(
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.FINAL_EASY_FOUR_PAIRS
            ),
            TutorialMvpContent.scenarios.map(TutorialScenario::id)
        )
    }

    @Test
    fun defines_the_two_pair_practice_scenario() {
        val scenario = TutorialMvpContent.scenario(TutorialScenarioId.TWO_PAIR_PRACTICE)

        assertEquals(listOf(1, 2, 3, 4), scenario.stripValues)
        assertEquals(
            listOf(
                StripItem.Known(1),
                StripItem.Hidden,
                StripItem.Known(3),
                StripItem.Known(4)
            ),
            scenario.initialPuzzle.strip.items
        )
        assertEquals(listOf(3, 2, 7, 12), scenario.initialPuzzle.board.tiles.map(Tile::result))
        assertAllTilesHaveHiddenExpressions(scenario.initialPuzzle)
        assertEquals(
            listOf(
                TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1),
                TutorialIntendedPair(firstStripEntryId = 2, secondStripEntryId = 3)
            ),
            scenario.intendedPairs
        )
        scenario.assertSolvedTiles(
            ExpectedSolvedTile(
                leftValue = 1,
                leftStripEntryId = 0,
                operator = Operator.ADDITION,
                rightValue = 2,
                rightStripEntryId = 1,
                result = 3
            ),
            ExpectedSolvedTile(
                leftValue = 1,
                leftStripEntryId = 0,
                operator = Operator.MULTIPLICATION,
                rightValue = 2,
                rightStripEntryId = 1,
                result = 2
            ),
            ExpectedSolvedTile(
                leftValue = 3,
                leftStripEntryId = 2,
                operator = Operator.ADDITION,
                rightValue = 4,
                rightStripEntryId = 3,
                result = 7
            ),
            ExpectedSolvedTile(
                leftValue = 3,
                leftStripEntryId = 2,
                operator = Operator.MULTIPLICATION,
                rightValue = 4,
                rightStripEntryId = 3,
                result = 12
            )
        )
    }

    @Test
    fun defines_the_final_easy_four_pairs_tutorial_scenario() {
        val scenario = TutorialMvpContent.scenario(TutorialScenarioId.FINAL_EASY_FOUR_PAIRS)

        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8), scenario.stripValues)
        assertEquals(
            listOf(
                StripItem.Known(1),
                StripItem.Hidden,
                StripItem.Known(3),
                StripItem.Known(4),
                StripItem.Hidden,
                StripItem.Known(6),
                StripItem.Hidden,
                StripItem.Known(8)
            ),
            scenario.initialPuzzle.strip.items
        )
        assertEquals(listOf(3, 2, 7, 12, 11, 30, 15, 56), scenario.initialPuzzle.board.tiles.map(Tile::result))
        assertAllTilesHaveHiddenExpressions(scenario.initialPuzzle)
        assertEquals(
            listOf(
                TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1),
                TutorialIntendedPair(firstStripEntryId = 2, secondStripEntryId = 3),
                TutorialIntendedPair(firstStripEntryId = 4, secondStripEntryId = 5),
                TutorialIntendedPair(firstStripEntryId = 6, secondStripEntryId = 7)
            ),
            scenario.intendedPairs
        )
        scenario.assertSolvedTiles(
            ExpectedSolvedTile(
                leftValue = 1,
                leftStripEntryId = 0,
                operator = Operator.ADDITION,
                rightValue = 2,
                rightStripEntryId = 1,
                result = 3
            ),
            ExpectedSolvedTile(
                leftValue = 1,
                leftStripEntryId = 0,
                operator = Operator.MULTIPLICATION,
                rightValue = 2,
                rightStripEntryId = 1,
                result = 2
            ),
            ExpectedSolvedTile(
                leftValue = 3,
                leftStripEntryId = 2,
                operator = Operator.ADDITION,
                rightValue = 4,
                rightStripEntryId = 3,
                result = 7
            ),
            ExpectedSolvedTile(
                leftValue = 3,
                leftStripEntryId = 2,
                operator = Operator.MULTIPLICATION,
                rightValue = 4,
                rightStripEntryId = 3,
                result = 12
            ),
            ExpectedSolvedTile(
                leftValue = 5,
                leftStripEntryId = 4,
                operator = Operator.ADDITION,
                rightValue = 6,
                rightStripEntryId = 5,
                result = 11
            ),
            ExpectedSolvedTile(
                leftValue = 5,
                leftStripEntryId = 4,
                operator = Operator.MULTIPLICATION,
                rightValue = 6,
                rightStripEntryId = 5,
                result = 30
            ),
            ExpectedSolvedTile(
                leftValue = 7,
                leftStripEntryId = 6,
                operator = Operator.ADDITION,
                rightValue = 8,
                rightStripEntryId = 7,
                result = 15
            ),
            ExpectedSolvedTile(
                leftValue = 7,
                leftStripEntryId = 6,
                operator = Operator.MULTIPLICATION,
                rightValue = 8,
                rightStripEntryId = 7,
                result = 56
            )
        )
    }

    @Test
    fun tutorial_scenarios_are_valid_under_the_domain_rules() {
        TutorialMvpContent.scenarios.forEach { scenario ->
            assertEquals(PuzzleCompletionState.INCOMPLETE, scenario.initialPuzzle.completionState)
            assertEquals(PuzzleCompletionState.SOLVED, scenario.solvedPuzzle.completionState)
            assertEquals(scenario.stripValues.size, scenario.initialPuzzle.strip.entries.size)
            assertEquals(scenario.stripValues.size, scenario.initialPuzzle.board.tiles.size)
        }
    }

    @Test
    fun maps_steps_one_to_five_to_the_authored_tutorial_scenarios() {
        val steps = TutorialMvpContent.steps

        assertEquals(listOf(1, 2, 3, 4, 5), steps.map(TutorialStep::order))
        assertEquals(
            listOf(
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.FINAL_EASY_FOUR_PAIRS
            ),
            steps.map(TutorialStep::scenarioId)
        )
        assertEquals(
            listOf(
                R.string.tutorial_step_one_copy,
                R.string.tutorial_step_two_copy,
                R.string.tutorial_step_three_copy,
                R.string.tutorial_step_four_copy,
                R.string.tutorial_step_five_copy
            ),
            steps.map(TutorialStep::playerFacingCopyResId)
        )
    }

    @Test
    fun exposes_mode_specific_tutorial_step_lists() {
        assertEquals(
            listOf(
                TutorialMode.LEARN_BASICS,
                TutorialMode.PRACTICE_FULL_PUZZLE
            ),
            TutorialMode.entries.toList()
        )
        assertEquals(
            listOf(1, 2, 3, 4),
            TutorialMvpContent.stepsFor(TutorialMode.LEARN_BASICS).map(TutorialStep::order)
        )
        assertEquals(
            listOf(5),
            TutorialMvpContent.stepsFor(TutorialMode.PRACTICE_FULL_PUZZLE).map(TutorialStep::order)
        )
        assertEquals(
            TutorialMvpContent.learnBasicsSteps,
            TutorialMvpContent.stepsFor(TutorialMode.LEARN_BASICS)
        )
        assertEquals(
            TutorialMvpContent.practiceFullPuzzleSteps,
            TutorialMvpContent.stepsFor(TutorialMode.PRACTICE_FULL_PUZZLE)
        )
    }

    @Test
    fun defines_step_highlights_and_required_actions_for_the_guided_ui() {
        val steps = TutorialMvpContent.steps

        assertEquals(
            listOf(
                listOf(
                    TutorialHighlightTarget.StripEntries(indexes = listOf(1))
                ),
                listOf(
                    TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0)
                ),
                listOf(
                    TutorialHighlightTarget.TileExpressionSlots(tileIndex = 1)
                ),
                listOf(
                    TutorialHighlightTarget.TileExpressionSlots(tileIndex = 2),
                    TutorialHighlightTarget.TileExpressionSlots(tileIndex = 3)
                ),
                listOf(
                    TutorialHighlightTarget.HiddenStripEntries,
                    TutorialHighlightTarget.HiddenTileExpressions
                )
            ),
            steps.map(TutorialStep::highlightedTargets)
        )
        assertEquals(
            listOf(
                TutorialRequiredAction.EnterStripValue(stripEntryIndex = 1, value = 2),
                TutorialRequiredAction.CompleteTileExpression(
                    tileIndex = 0,
                    leftStripEntryId = 0,
                    operator = Operator.ADDITION,
                    rightStripEntryId = 1
                ),
                TutorialRequiredAction.CompleteTileExpression(
                    tileIndex = 1,
                    leftStripEntryId = 0,
                    operator = Operator.MULTIPLICATION,
                    rightStripEntryId = 1
                ),
                TutorialRequiredAction.CompleteScenario,
                TutorialRequiredAction.CompleteScenario
            ),
            steps.map(TutorialStep::requiredAction)
        )
    }

    @Test
    fun defines_completion_predicates_for_auto_advancing_steps() {
        val steps = TutorialMvpContent.steps

        assertEquals(
            listOf(
                TutorialStepCompletionPredicate.StripValueEntered(stripEntryIndex = 1, value = 2),
                TutorialStepCompletionPredicate.TileExpressionCompleted(
                    tileIndex = 0,
                    leftValue = 1,
                    operator = Operator.ADDITION,
                    rightValue = 2
                ),
                TutorialStepCompletionPredicate.TileExpressionCompleted(
                    tileIndex = 1,
                    leftValue = 1,
                    operator = Operator.MULTIPLICATION,
                    rightValue = 2
                ),
                TutorialStepCompletionPredicate.ScenarioSolved,
                TutorialStepCompletionPredicate.ScenarioSolved
            ),
            steps.map(TutorialStep::completionPredicate)
        )
    }

    @Test
    fun completion_predicates_match_the_expected_game_ui_state() {
        val steps = TutorialMvpContent.steps
        val twoPairScenario = TutorialMvpContent.scenario(TutorialScenarioId.TWO_PAIR_PRACTICE)
        val finalScenario = TutorialMvpContent.scenario(TutorialScenarioId.FINAL_EASY_FOUR_PAIRS)

        assertFalse(steps[0].isComplete(GameUiState.from(twoPairScenario.initialPuzzle)))
        assertTrue(
            steps[0].isComplete(
                GameUiState.from(twoPairScenario.initialPuzzle.withStripValue(index = 1, value = 2))
            )
        )

        assertFalse(steps[1].isComplete(GameUiState.from(twoPairScenario.initialPuzzle)))
        assertTrue(
            steps[1].isComplete(
                GameUiState.from(twoPairScenario.initialPuzzle.withSolvedScenarioTiles(twoPairScenario, 0))
            )
        )

        assertFalse(steps[2].isComplete(GameUiState.from(twoPairScenario.initialPuzzle)))
        assertTrue(
            steps[2].isComplete(
                GameUiState.from(twoPairScenario.initialPuzzle.withSolvedScenarioTiles(twoPairScenario, 1))
            )
        )

        assertFalse(steps[3].isComplete(GameUiState.from(twoPairScenario.initialPuzzle)))
        assertTrue(steps[3].isComplete(GameUiState.from(twoPairScenario.solvedPuzzle)))

        assertFalse(steps[4].isComplete(GameUiState.from(finalScenario.initialPuzzle)))
        assertTrue(steps[4].isComplete(GameUiState.from(finalScenario.solvedPuzzle)))
    }
}

private data class ExpectedSolvedTile(
    val leftValue: Int,
    val leftStripEntryId: Int,
    val operator: Operator,
    val rightValue: Int,
    val rightStripEntryId: Int,
    val result: Int
)

private fun assertAllTilesHaveHiddenExpressions(puzzle: Puzzle) {
    assertTrue(
        puzzle.board.tiles.all { tile ->
            tile.expression.leftOperand == Expression.Operand.Hidden &&
                tile.expression.operator == Operator.Hidden &&
                tile.expression.rightOperand == Expression.Operand.Hidden
        }
    )
}

private fun Puzzle.withStripValue(index: Int, value: Int): Puzzle = copy(
    strip = strip.withUpdatedEntry(index = index, value = value)
)

private fun Puzzle.withSolvedScenarioTiles(scenario: TutorialScenario, vararg tileIndexes: Int): Puzzle = copy(
    board = Board(
        tiles = board.tiles.toMutableList().apply {
            tileIndexes.forEach { tileIndex ->
                set(tileIndex, scenario.solvedPuzzle.board.tiles[tileIndex])
            }
        }
    )
)

private fun TutorialScenario.assertSolvedTiles(vararg expectedTiles: ExpectedSolvedTile) {
    assertEquals(expectedTiles.size, solvedPuzzle.board.tiles.size)

    expectedTiles.zip(solvedPuzzle.board.tiles).forEach { (expectedTile, actualTile) ->
        assertEquals(
            Expression(
                leftOperand = Expression.Operand.Known(
                    value = expectedTile.leftValue,
                    stripEntryId = expectedTile.leftStripEntryId
                ),
                operator = expectedTile.operator,
                rightOperand = Expression.Operand.Known(
                    value = expectedTile.rightValue,
                    stripEntryId = expectedTile.rightStripEntryId
                )
            ),
            actualTile.expression
        )
        assertEquals(expectedTile.result, actualTile.result)
    }
}
