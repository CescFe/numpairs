package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialContentTest {
    @Test
    fun exposes_authored_content_for_each_learning_flow() {
        assertEquals(
            listOf(
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.SOLVING_TIPS_PRACTICE
            ),
            TutorialContent.scenarios.map(TutorialScenario::id)
        )
        assertEquals(
            TutorialContent.learnBasicsSteps,
            TutorialContent.stepsFor(TutorialMode.LEARN_BASICS)
        )
        assertEquals(
            TutorialContent.solvingTipsPracticeSteps,
            TutorialContent.stepsFor(TutorialMode.SOLVING_TIPS_PRACTICE)
        )
        assertEquals(
            setOf(TutorialScenarioId.TWO_PAIR_PRACTICE),
            TutorialContent.stepsFor(TutorialMode.LEARN_BASICS).map(TutorialStep::scenarioId).toSet()
        )
        assertEquals(
            setOf(TutorialScenarioId.SOLVING_TIPS_PRACTICE),
            TutorialContent.stepsFor(TutorialMode.SOLVING_TIPS_PRACTICE).map(TutorialStep::scenarioId).toSet()
        )
    }

    @Test
    fun authored_scenarios_are_valid_puzzles_with_hidden_work_for_the_player() {
        TutorialContent.scenarios.forEach { scenario ->
            assertEquals(PuzzleCompletionState.INCOMPLETE, scenario.initialPuzzle.completionState)
            assertEquals(PuzzleCompletionState.SOLVED, scenario.solvedPuzzle.completionState)
            assertEquals(scenario.stripValues.size, scenario.initialPuzzle.strip.entries.size)
            assertEquals(scenario.stripValues.size, scenario.initialPuzzle.board.tiles.size)
            assertEquals(scenario.stripValues.map(StripItem::Known), scenario.solvedPuzzle.strip.items)
            assertEquals(
                scenario.initialPuzzle.board.tiles.map(Tile::result),
                scenario.solvedPuzzle.board.tiles.map(Tile::result)
            )
            assertTrue(scenario.initialPuzzle.strip.items.any { stripItem -> stripItem == StripItem.Hidden })
            assertAllTilesHaveHiddenExpressions(scenario.initialPuzzle)
        }
    }

    @Test
    fun learn_basics_guides_the_player_through_core_rules_before_normal_completion() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.TWO_PAIR_PRACTICE)
        val steps = TutorialContent.stepsFor(TutorialMode.LEARN_BASICS)

        assertEquals(listOf(1, 2, 3, 4), steps.map(TutorialStep::order))

        steps[0].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(1))
            ),
            expectedAction = TutorialRequiredAction.EnterStripValue(stripEntryIndex = 1, value = 2),
            incompletePuzzle = scenario.initialPuzzle,
            completePuzzle = scenario.initialPuzzle.withStripValue(index = 1, value = 2)
        )

        steps[1].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0)
            ),
            expectedAction = TutorialRequiredAction.CompleteTileExpression(
                tileIndex = 0,
                leftStripEntryId = 0,
                operator = Operator.ADDITION,
                rightStripEntryId = 1
            ),
            incompletePuzzle = scenario.initialPuzzle,
            completePuzzle = scenario.initialPuzzle.withSolvedScenarioTiles(scenario, 0)
        )

        steps[2].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 1)
            ),
            expectedAction = TutorialRequiredAction.CompleteTileExpression(
                tileIndex = 1,
                leftStripEntryId = 0,
                operator = Operator.MULTIPLICATION,
                rightStripEntryId = 1
            ),
            incompletePuzzle = scenario.initialPuzzle,
            completePuzzle = scenario.initialPuzzle.withSolvedScenarioTiles(scenario, 1)
        )

        steps[3].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 2),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 3)
            ),
            expectedAction = TutorialRequiredAction.CompleteScenario,
            incompletePuzzle = scenario.initialPuzzle,
            completePuzzle = scenario.solvedPuzzle
        )
    }

    @Test
    fun solving_tips_practice_requires_the_first_pair_before_the_remaining_pair() {
        val scenario = TutorialContent.scenario(TutorialScenarioId.SOLVING_TIPS_PRACTICE)
        val steps = TutorialContent.stepsFor(TutorialMode.SOLVING_TIPS_PRACTICE)

        assertEquals(listOf(5, 6), steps.map(TutorialStep::order))

        steps[0].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(1)),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0)
            ),
            expectedAction = TutorialRequiredAction.CompleteTileExpressionsInOrder(
                expressions = listOf(
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
                    )
                )
            ),
            incompletePuzzle = scenario.initialPuzzle,
            completePuzzle = scenario.initialPuzzle.withSolvedScenarioTiles(scenario, 0, 1)
        )
        assertFalse(
            steps[0].isComplete(
                GameUiState.from(scenario.initialPuzzle.withSolvedScenarioTiles(scenario, 0))
            )
        )

        steps[1].assertGuidedAction(
            expectedHighlights = listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(2)),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 2),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 3)
            ),
            expectedAction = TutorialRequiredAction.CompleteTileExpressions(
                expressions = listOf(
                    TutorialRequiredAction.CompleteTileExpression(
                        tileIndex = 2,
                        leftStripEntryId = 2,
                        operator = Operator.MULTIPLICATION,
                        rightStripEntryId = 3
                    ),
                    TutorialRequiredAction.CompleteTileExpression(
                        tileIndex = 3,
                        leftStripEntryId = 2,
                        operator = Operator.ADDITION,
                        rightStripEntryId = 3
                    )
                )
            ),
            incompletePuzzle = scenario.initialPuzzle,
            completePuzzle = scenario.solvedPuzzle
        )
    }
}

private fun assertAllTilesHaveHiddenExpressions(puzzle: Puzzle) {
    assertTrue(
        puzzle.board.tiles.all { tile ->
            tile.hasHiddenExpression()
        }
    )
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

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

private fun TutorialStep.assertGuidedAction(
    expectedHighlights: List<TutorialHighlightTarget>,
    expectedAction: TutorialRequiredAction,
    incompletePuzzle: Puzzle,
    completePuzzle: Puzzle
) {
    assertEquals(expectedHighlights, highlightedTargets)
    assertEquals(expectedAction, requiredAction)

    assertFalse(isComplete(GameUiState.from(incompletePuzzle)))
    assertTrue(isComplete(GameUiState.from(completePuzzle)))
}
