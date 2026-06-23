package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.assignment.OperandSelectionAvailability
import org.cescfe.numpairs.domain.puzzle.assignment.operandSelectionChoicesFor
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.support.hiddenTile
import org.cescfe.numpairs.domain.puzzle.support.withTile
import org.cescfe.numpairs.domain.puzzle.validation.LivePuzzleRuleConflict
import org.cescfe.numpairs.domain.puzzle.validation.LiveStripEntryOperatorUsage
import org.cescfe.numpairs.domain.puzzle.validation.liveRuleConflictsByStripEntryOperator
import org.cescfe.numpairs.domain.puzzle.validation.liveRuleConflictsByTile
import org.cescfe.numpairs.domain.puzzle.validation.liveRuleConflictsForCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LivePuzzleRuleConflictTest {
    @Test
    fun detects_duplicate_same_operator_usage_before_completion() {
        val puzzle = liveRulePuzzle()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 3)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.MULTIPLICATION)
                    .withRightOperand(value = 3, stripEntryId = 2)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 6)
                    .withLeftOperand(value = 2, stripEntryId = 1)
                    .withOperator(Operator.MULTIPLICATION)
                    .withRightOperand(value = 3, stripEntryId = 2)
            )

        assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
        assertEquals(
            mapOf(
                0 to setOf(LivePuzzleRuleConflict.DUPLICATE_OPERATOR_USAGE),
                1 to setOf(LivePuzzleRuleConflict.DUPLICATE_OPERATOR_USAGE)
            ),
            puzzle.liveRuleConflictsByTile
        )
    }

    @Test
    fun duplicate_same_operator_usage_marks_only_the_duplicated_strip_entry_operator_usage() {
        val puzzle = liveRulePuzzle()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 4)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
                    .withRightOperand(value = 3, stripEntryId = 2)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 5)
                    .withLeftOperand(value = 2, stripEntryId = 1)
                    .withOperator(Operator.ADDITION)
                    .withRightOperand(value = 3, stripEntryId = 2)
            )

        assertEquals(
            mapOf(
                LiveStripEntryOperatorUsage(
                    stripEntryId = 2,
                    operator = Operator.ADDITION
                ) to setOf(LivePuzzleRuleConflict.DUPLICATE_OPERATOR_USAGE)
            ),
            puzzle.liveRuleConflictsByStripEntryOperator
        )
    }

    @Test
    fun detects_mismatched_pairing_before_completion() {
        val puzzle = liveRulePuzzle()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 3)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
                    .withRightOperand(value = 2, stripEntryId = 1)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 3)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.MULTIPLICATION)
                    .withRightOperand(value = 3, stripEntryId = 2)
            )

        assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
        assertEquals(
            mapOf(
                0 to setOf(LivePuzzleRuleConflict.MISMATCHED_PAIRING),
                1 to setOf(LivePuzzleRuleConflict.MISMATCHED_PAIRING)
            ),
            puzzle.liveRuleConflictsByTile
        )
    }

    @Test
    fun mismatched_pairing_marks_only_the_entry_with_different_addition_and_multiplication_partners() {
        val puzzle = liveRulePuzzle()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 3)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
                    .withRightOperand(value = 2, stripEntryId = 1)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 3)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.MULTIPLICATION)
                    .withRightOperand(value = 3, stripEntryId = 2)
            )

        assertEquals(
            mapOf(
                LiveStripEntryOperatorUsage(
                    stripEntryId = 0,
                    operator = Operator.ADDITION
                ) to setOf(LivePuzzleRuleConflict.MISMATCHED_PAIRING),
                LiveStripEntryOperatorUsage(
                    stripEntryId = 0,
                    operator = Operator.MULTIPLICATION
                ) to setOf(LivePuzzleRuleConflict.MISMATCHED_PAIRING)
            ),
            puzzle.liveRuleConflictsByStripEntryOperator
        )
    }

    @Test
    fun candidate_and_current_two_times_three_reports_duplicate_usage_and_mismatched_pairing() {
        val puzzle = liveRulePuzzle()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 3)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
                    .withRightOperand(value = 2, stripEntryId = 1)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 3)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.MULTIPLICATION)
                    .withRightOperand(value = 3, stripEntryId = 2)
            )
            .withTile(
                index = 2,
                tile = hiddenTile(result = 6)
                    .withLeftOperand(value = 2, stripEntryId = 1)
                    .withOperator(Operator.MULTIPLICATION)
            )

        val candidateConflicts = puzzle.liveRuleConflictsForCandidate(
            tileIndex = 2,
            slot = OperandSlot.RIGHT,
            stripEntryId = 2,
            operator = Operator.MULTIPLICATION
        )
        val candidateChoice = puzzle.operandSelectionChoicesFor(tileIndex = 2, slot = OperandSlot.RIGHT)
            .first { choice -> choice.stripEntryId == 2 }
        val currentPuzzle = puzzle.withTile(
            index = 2,
            tile = puzzle.board.tiles[2].withRightOperand(value = 3, stripEntryId = 2)
        )

        assertEquals(
            setOf(
                LivePuzzleRuleConflict.DUPLICATE_OPERATOR_USAGE,
                LivePuzzleRuleConflict.MISMATCHED_PAIRING
            ),
            candidateConflicts
        )
        assertEquals(OperandSelectionAvailability.AVAILABLE, candidateChoice.availability)
        assertTrue(candidateChoice.canBeSelected)
        assertEquals(
            setOf(
                LivePuzzleRuleConflict.DUPLICATE_OPERATOR_USAGE,
                LivePuzzleRuleConflict.MISMATCHED_PAIRING
            ),
            currentPuzzle.liveRuleConflictsByTile.getValue(2)
        )
    }

    @Test
    fun matching_sum_and_product_pairs_do_not_report_live_conflicts() {
        val puzzle = liveRulePuzzle()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 3)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
                    .withRightOperand(value = 2, stripEntryId = 1)
            )
            .withTile(
                index = 1,
                tile = hiddenTile(result = 2)
                    .withLeftOperand(value = 1, stripEntryId = 0)
                    .withOperator(Operator.MULTIPLICATION)
                    .withRightOperand(value = 2, stripEntryId = 1)
            )

        assertEquals(emptyMap<Int, Set<LivePuzzleRuleConflict>>(), puzzle.liveRuleConflictsByTile)
    }
}

private fun liveRulePuzzle(): Puzzle = Puzzle(
    board = Board(
        tiles = listOf(
            hiddenTile(result = 3),
            hiddenTile(result = 3),
            hiddenTile(result = 6),
            hiddenTile(result = 4)
        )
    ),
    strip = Strip.fromItems(
        items = listOf(
            StripItem.Known(1),
            StripItem.Known(2),
            StripItem.Known(3),
            StripItem.Known(4)
        )
    )
)
