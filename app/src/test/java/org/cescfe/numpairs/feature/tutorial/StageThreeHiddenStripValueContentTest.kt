package org.cescfe.numpairs.feature.tutorial

import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StageThreeHiddenStripValueContentTest {
    private val scenario = StageThreeHiddenStripValueContent.scenario
    private val step = StageThreeHiddenStripValueContent.step

    @Test
    fun authors_one_hidden_strip_value_revealed_by_a_resolved_expression() {
        val clueTile = scenario.initialPuzzle.board.tiles[0]

        assertEquals(TutorialScenarioId.HIDDEN_STRIP_VALUE, scenario.id)
        assertEquals(listOf(2, 3), scenario.stripValues)
        assertEquals(listOf(StripItem.Known(2), StripItem.Hidden), scenario.initialPuzzle.strip.items)
        assertEquals(1, scenario.initialPuzzle.strip.items.count { item -> item == StripItem.Hidden })
        assertEquals(Expression.Operand.Known(value = 2, stripEntryId = 0), clueTile.expression.leftOperand)
        assertEquals(Operator.ADDITION, clueTile.expression.operator)
        assertEquals(Expression.Operand.Known(value = 3, stripEntryId = 1), clueTile.expression.rightOperand)
        assertEquals(5, clueTile.result)
        assertEquals(PuzzleCompletionState.INCOMPLETE, scenario.initialPuzzle.completionState)
        assertEquals(PuzzleCompletionState.SOLVED, scenario.solvedPuzzle.completionState)
        assertEquals(
            listOf(TutorialIntendedPair(firstStripEntryId = 0, secondStripEntryId = 1)),
            scenario.intendedPairs
        )
    }

    @Test
    fun focuses_the_hidden_entry_and_relevant_clue() {
        assertEquals(4, step.order)
        assertEquals(TutorialScenarioId.HIDDEN_STRIP_VALUE, step.scenarioId)
        assertEquals(
            listOf(
                TutorialHighlightTarget.StripEntries(indexes = listOf(1)),
                TutorialHighlightTarget.Tiles(indexes = listOf(0))
            ),
            step.highlightedTargets
        )

        val highlightState = step.toHighlightState(scenario = scenario, uiState = null)

        assertEquals(setOf(1), highlightState.stripEntryIndexes)
        assertEquals(setOf(0), highlightState.tileIndexes)
        assertTrue(highlightState.tileExpressionSlots.isEmpty())
    }

    @Test
    fun allows_only_the_correct_hidden_strip_entry_value() {
        val policy = step.toInteractionPolicy(scenario = scenario, uiState = null)

        assertTrue(policy.canTapStripItem(1))
        assertFalse(policy.canTapStripItem(0))
        assertTrue(policy.canConfirmStripItemEntry(1, 3))
        assertFalse(policy.canConfirmStripItemEntry(1, 4))
        assertFalse(policy.canConfirmStripItemEntry(0, 2))
        assertFalse(policy.canTapTileLeftOperand(0))
        assertFalse(policy.canTapTileRightOperand(0))
        assertFalse(policy.canTapTileOperator(0))
        assertFalse(policy.canTapTileReset(0))
        assertFalse(policy.canConfirmTileOperand(0, OperandSlot.LEFT, 0))
        assertFalse(policy.canConfirmTileOperator(0, Operator.ADDITION))
    }

    @Test
    fun completes_only_after_three_is_player_entered() {
        val incorrectPuzzle = scenario.initialPuzzle.copy(
            strip = scenario.initialPuzzle.strip.withUpdatedEntry(index = 1, value = 4)
        )
        val completedPuzzle = scenario.initialPuzzle.copy(
            strip = scenario.initialPuzzle.strip.withUpdatedEntry(index = 1, value = 3)
        )

        assertFalse(step.isComplete(GameUiState.from(scenario.initialPuzzle)))
        assertFalse(step.isComplete(GameUiState.from(incorrectPuzzle)))
        assertTrue(step.isComplete(GameUiState.from(completedPuzzle)))
        assertEquals(PuzzleCompletionState.SOLVED, completedPuzzle.completionState)
    }

    @Test
    fun exposes_stage_three_as_the_final_active_learn_basics_step() {
        assertEquals(scenario, TutorialContent.scenario(TutorialScenarioId.HIDDEN_STRIP_VALUE))
        assertEquals(step, TutorialContent.learnBasicsSteps.last())
    }
}
