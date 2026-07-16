package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StageTwoComplementaryPairContentTest {
    private val scenario = StageTwoComplementaryPairContent.scenario
    private val steps = StageTwoComplementaryPairContent.steps

    @Test
    fun authors_one_obvious_sum_product_pair_with_hidden_expressions() {
        assertEquals(TutorialScenarioId.COMPLEMENTARY_PAIR, scenario.id)
        assertEquals(listOf(2, 3), scenario.stripValues)
        assertEquals(listOf(StripItem.Known(2), StripItem.Known(3)), scenario.initialPuzzle.strip.items)
        assertEquals(listOf(5, 6), scenario.initialPuzzle.board.tiles.map { tile -> tile.result })
        assertTrue(
            scenario.initialPuzzle.board.tiles.all { tile ->
                tile.expression.leftOperand == Expression.Operand.Hidden &&
                    tile.expression.operator == Operator.Hidden &&
                    tile.expression.rightOperand == Expression.Operand.Hidden
            }
        )
        assertEquals(PuzzleCompletionState.INCOMPLETE, scenario.initialPuzzle.completionState)
        assertEquals(PuzzleCompletionState.SOLVED, scenario.solvedPuzzle.completionState)
        assertEquals(
            listOf(TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1)),
            scenario.intendedPairs
        )
    }

    @Test
    fun guides_the_sum_before_the_complementary_product() {
        assertEquals(listOf(2, 3), steps.map(TutorialStep::order))
        assertEquals(
            listOf(
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 0),
                TutorialHighlightTarget.TileExpressionSlots(tileIndex = 1)
            ),
            steps.flatMap(TutorialStep::highlightedTargets)
        )
        assertEquals(
            completeExpression(tileIndex = 0, operator = Operator.ADDITION),
            steps[0].requiredAction
        )
        assertEquals(
            completeExpression(tileIndex = 1, operator = Operator.MULTIPLICATION),
            steps[1].requiredAction
        )
    }

    @Test
    fun requires_both_intended_expressions_before_stage_completion() {
        val additionCompleted = scenario.initialPuzzle.withSolvedTiles(0)
        val multiplicationCompleted = scenario.initialPuzzle.withSolvedTiles(1)

        assertFalse(steps[1].isComplete(GameUiState.from(scenario.initialPuzzle)))
        assertFalse(steps[1].isComplete(GameUiState.from(additionCompleted)))
        assertFalse(steps[1].isComplete(GameUiState.from(multiplicationCompleted)))
        assertTrue(steps[1].isComplete(GameUiState.from(scenario.solvedPuzzle)))
    }

    @Test
    fun each_step_accepts_only_its_intended_tile_entries_and_operator() {
        steps.forEachIndexed { index, step ->
            val expectedOperator = if (index == 0) Operator.ADDITION else Operator.MULTIPLICATION
            val policy = step.toInteractionPolicy(scenario = scenario, uiState = null)

            assertTrue(policy.canTapTileLeftOperand(index))
            assertTrue(policy.canTapTileRightOperand(index))
            assertTrue(policy.canTapTileOperator(index))
            assertFalse(policy.canTapTileLeftOperand(1 - index))
            assertFalse(policy.canTapStripItem(0))
            assertFalse(policy.canTapTileReset(index))
            assertTrue(policy.canConfirmTileOperand(index, OperandSlot.LEFT, 0))
            assertTrue(policy.canConfirmTileOperand(index, OperandSlot.RIGHT, 1))
            assertFalse(policy.canConfirmTileOperand(index, OperandSlot.LEFT, 1))
            assertFalse(policy.canConfirmTileOperand(index, OperandSlot.RIGHT, 0))
            assertTrue(policy.canConfirmTileOperator(index, expectedOperator))
            assertFalse(policy.canConfirmTileOperator(index, otherOperator(expectedOperator)))
        }
    }

    @Test
    fun registers_stage_two_without_activating_it() {
        assertEquals(scenario, TutorialContent.scenario(TutorialScenarioId.COMPLEMENTARY_PAIR))
        assertFalse(
            TutorialContent.learnBasicsSteps.any { step ->
                step.scenarioId == TutorialScenarioId.COMPLEMENTARY_PAIR
            }
        )
    }

    private fun completeExpression(tileIndex: Int, operator: Operator): TutorialRequiredAction.CompleteTileExpression =
        TutorialRequiredAction.CompleteTileExpression(
            tileIndex = tileIndex,
            leftStripEntryId = 0,
            operator = operator,
            rightStripEntryId = 1
        )

    private fun Puzzle.withSolvedTiles(vararg tileIndexes: Int): Puzzle = copy(
        board = Board(
            tiles = board.tiles.toMutableList().apply {
                tileIndexes.forEach { tileIndex ->
                    set(tileIndex, scenario.solvedPuzzle.board.tiles[tileIndex])
                }
            }
        )
    )

    private fun otherOperator(operator: Operator): Operator = when (operator) {
        Operator.Addition -> Operator.MULTIPLICATION
        Operator.Multiplication -> Operator.ADDITION
        Operator.Hidden -> error("Stage 2 never uses the hidden operator.")
    }
}
