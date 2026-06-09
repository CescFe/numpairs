package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.Expression
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialMvpContentTest {
    @Test
    fun defines_the_authored_tutorial_scenarios() {
        assertEquals(
            listOf(
                TutorialScenarioId.ONE_PAIR_ORIENTATION,
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.FINAL_EASY_FOUR_PAIRS
            ),
            TutorialMvpContent.scenarios.map(TutorialScenario::id)
        )
    }

    @Test
    fun defines_the_one_pair_orientation_scenario() {
        val scenario = TutorialMvpContent.scenario(TutorialScenarioId.ONE_PAIR_ORIENTATION)

        assertEquals(listOf(2, 3), scenario.stripValues)
        assertEquals(
            listOf(
                StripItem.Known(2),
                StripItem.Hidden
            ),
            scenario.initialPuzzle.strip.items
        )
        assertEquals(listOf(5, 6), scenario.initialPuzzle.board.tiles.map(Tile::result))
        assertAllTilesHaveHiddenExpressions(scenario.initialPuzzle)
        assertEquals(
            listOf(TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1)),
            scenario.intendedPairs
        )
        scenario.assertSolvedTiles(
            ExpectedSolvedTile(
                leftValue = 2,
                leftStripEntryId = 0,
                operator = Operator.ADDITION,
                rightValue = 3,
                rightStripEntryId = 1,
                result = 5
            ),
            ExpectedSolvedTile(
                leftValue = 2,
                leftStripEntryId = 0,
                operator = Operator.MULTIPLICATION,
                rightValue = 3,
                rightStripEntryId = 1,
                result = 6
            )
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
    fun maps_steps_one_to_six_to_the_documented_tutorial_scenarios() {
        val steps = TutorialMvpContent.steps

        assertEquals(listOf(1, 2, 3, 4, 5, 6), steps.map(TutorialStep::order))
        assertEquals(
            listOf(
                TutorialScenarioId.ONE_PAIR_ORIENTATION,
                TutorialScenarioId.ONE_PAIR_ORIENTATION,
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.TWO_PAIR_PRACTICE,
                TutorialScenarioId.FINAL_EASY_FOUR_PAIRS
            ),
            steps.map(TutorialStep::scenarioId)
        )
        assertEquals(
            listOf(
                "Guess all the unknown elements.",
                "Strip: numbers available to solve the puzzle. " +
                    "Grid: tiles with a visible result and an unknown expression.",
                "Guess hidden values to complete an ascending list of positive integers.",
                "Fill each tile with two operands and one operator.",
                "Pair strip numbers so each pair creates one sum and one product " +
                    "that match two grid results.",
                "Now finish the remaining unknowns."
            ),
            steps.map(TutorialStep::playerFacingCopy)
        )
    }

    @Test
    fun defines_step_highlights_and_required_actions_for_the_guided_ui() {
        val steps = TutorialMvpContent.steps

        assertEquals(
            listOf(
                listOf(
                    TutorialHighlightTarget.HiddenStripEntries,
                    TutorialHighlightTarget.HiddenTileExpressions
                ),
                listOf(
                    TutorialHighlightTarget.StripArea,
                    TutorialHighlightTarget.GridArea
                ),
                listOf(
                    TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1, 2))
                ),
                listOf(
                    TutorialHighlightTarget.Tiles(indexes = listOf(0)),
                    TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0),
                    TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1))
                ),
                listOf(
                    TutorialHighlightTarget.StripEntries(indexes = listOf(0, 1)),
                    TutorialHighlightTarget.Tiles(indexes = listOf(0, 1))
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
                null,
                null,
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
                TutorialRequiredAction.CompleteScenario
            ),
            steps.map(TutorialStep::requiredAction)
        )
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
