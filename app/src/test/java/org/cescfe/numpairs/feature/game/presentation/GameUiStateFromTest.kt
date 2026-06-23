package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.support.TileAssignment
import org.cescfe.numpairs.domain.puzzle.support.assignedTile
import org.cescfe.numpairs.domain.puzzle.support.defaultKnownStripValues
import org.cescfe.numpairs.domain.puzzle.support.hiddenTile
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.cescfe.numpairs.domain.puzzle.support.puzzleWithRepeatedSixes
import org.cescfe.numpairs.domain.puzzle.support.withTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameUiStateFromTest {
    @Test
    fun maps_strip_items_to_labels_entry_enablement_and_visual_style() {
        val puzzle = initialPuzzle.copy(
            strip = Strip.fromItems(
                items = listOf(
                    StripItem.Hidden,
                    StripItem.PlayerEntered(2),
                    StripItem.Known(6),
                    StripItem.Hidden,
                    StripItem.Known(25),
                    StripItem.Hidden,
                    StripItem.Hidden,
                    StripItem.Known(222)
                )
            )
        )

        assertEquals(
            listOf(
                StripItemUiState(label = "?", isEntryEnabled = true, visualStyle = StripItemVisualStyle.HIDDEN),
                StripItemUiState(label = "2", isEntryEnabled = true, visualStyle = StripItemVisualStyle.PLAYER_ENTERED),
                StripItemUiState(label = "6", isEntryEnabled = false, visualStyle = StripItemVisualStyle.KNOWN),
                StripItemUiState(label = "?", isEntryEnabled = true, visualStyle = StripItemVisualStyle.HIDDEN),
                StripItemUiState(label = "25", isEntryEnabled = false, visualStyle = StripItemVisualStyle.KNOWN),
                StripItemUiState(label = "?", isEntryEnabled = true, visualStyle = StripItemVisualStyle.HIDDEN),
                StripItemUiState(label = "?", isEntryEnabled = true, visualStyle = StripItemVisualStyle.HIDDEN),
                StripItemUiState(label = "222", isEntryEnabled = false, visualStyle = StripItemVisualStyle.KNOWN)
            ),
            GameUiState.from(puzzle).stripItems
        )
    }

    @Test
    fun maps_strip_items_to_operator_usage_state() {
        val puzzle = Puzzle(
            strip = Strip.fromItems(items = defaultKnownStripValues().map(StripItem::Known)),
            board = Board(
                tiles = listOf(
                    assignedTile(
                        leftEntryId = 1,
                        leftValue = 2,
                        operator = Operator.ADDITION,
                        rightEntryId = 4,
                        rightValue = 5
                    ),
                    assignedTile(
                        leftEntryId = 5,
                        leftValue = 6,
                        operator = Operator.MULTIPLICATION,
                        rightEntryId = 2,
                        rightValue = 3
                    ),
                    assignedTile(
                        leftEntryId = 3,
                        leftValue = 4,
                        operator = Operator.ADDITION,
                        rightEntryId = 6,
                        rightValue = 7
                    ),
                    assignedTile(
                        leftEntryId = 7,
                        leftValue = 8,
                        operator = Operator.MULTIPLICATION,
                        rightEntryId = 3,
                        rightValue = 4
                    ),
                    hiddenTile(result = 1),
                    hiddenTile(result = 2),
                    hiddenTile(result = 3),
                    hiddenTile(result = 4)
                )
            )
        )

        val stripItems = GameUiState.from(puzzle).stripItems

        assertStripUsage(stripItems[0], additionUsed = false, multiplicationUsed = false)
        assertStripUsage(stripItems[1], additionUsed = true, multiplicationUsed = false)
        assertStripUsage(stripItems[2], additionUsed = false, multiplicationUsed = true)
        assertStripUsage(stripItems[3], additionUsed = true, multiplicationUsed = true)
    }

    @Test
    fun builds_strip_item_entry_input_state_for_editable_entries() {
        val uiState = GameUiState.from(
            puzzle = initialPuzzle,
            presentationState = GamePresentationState().showStripItemEntryInput(
                index = 1,
                draftText = "9",
                isInvalid = true
            )
        )

        assertEquals(
            StripItemEntryInputUiState(
                stripItemIndex = 1,
                draftText = "9",
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                isInvalid = true
            ),
            uiState.stripItemEntryInput
        )
    }

    @Test
    fun does_not_build_strip_item_entry_input_state_for_known_entries() {
        val uiState = GameUiState.from(
            puzzle = initialPuzzle,
            presentationState = GamePresentationState().showStripItemEntryInput(
                index = 2,
                draftText = "6"
            )
        )

        assertNull(uiState.stripItemEntryInput)
    }

    @Test
    fun maps_an_incorrect_tile_to_invalid_visual_state_and_invalid_outcome() {
        val incorrectAssignments = solvedTileAssignments().toMutableList().apply {
            this[0] = TileAssignment(
                leftEntryId = 0,
                operator = Operator.ADDITION,
                rightEntryId = 1,
                result = 999
            )
        }
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(),
            *incorrectAssignments.toTypedArray()
        )

        val uiState = GameUiState.from(puzzle)

        assertEquals(TileVisualState.INCORRECT, uiState.tiles.first().visualState)
        assertEquals(
            PuzzleOutcomeUiState.Invalid(PuzzleCompletionState.INCORRECT_TILES),
            uiState.puzzleOutcome
        )
    }

    @Test
    fun highlights_mismatched_pairing_tiles_without_marking_them_incorrect() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 0, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 3),
            TileAssignment(leftEntryId = 4, operator = Operator.ADDITION, rightEntryId = 5),
            TileAssignment(leftEntryId = 5, operator = Operator.MULTIPLICATION, rightEntryId = 4),
            TileAssignment(leftEntryId = 6, operator = Operator.ADDITION, rightEntryId = 7),
            TileAssignment(leftEntryId = 7, operator = Operator.MULTIPLICATION, rightEntryId = 6)
        )

        val uiState = GameUiState.from(puzzle)
        val highlightedTileIndexes = uiState.tiles.mapIndexedNotNull { index, tile ->
            index.takeIf { tile.isPairingMismatchHighlighted }
        }

        assertEquals(
            PuzzleOutcomeUiState.Invalid(PuzzleCompletionState.MISMATCHED_SUM_PRODUCT_PAIRINGS),
            uiState.puzzleOutcome
        )
        assertEquals(listOf(0, 1, 2, 3), highlightedTileIndexes)
        assertTrue(highlightedTileIndexes.none { uiState.tiles[it].isInvalid })
    }

    @Test
    fun maps_live_duplicate_operator_usage_conflicts_to_tiles() {
        val puzzle = liveRulePresentationPuzzle()
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

        val uiState = GameUiState.from(puzzle)
        val selectorUiState = GameUiState.from(
            puzzle = puzzle,
            presentationState = GamePresentationState().showTileOperandSelection(
                tileIndex = 2,
                slot = OperandSlot.LEFT
            )
        )
        val selectorDialog = selectorUiState.tileOperandSelectionDialog!!
        val optionForThree = selectorDialog.availableOperands
            .first { operand -> operand.stripEntryId == 2 }
        val optionForOne = selectorDialog.availableOperands
            .first { operand -> operand.stripEntryId == 0 }
        val optionForTwo = selectorDialog.availableOperands
            .first { operand -> operand.stripEntryId == 1 }

        assertEquals(
            setOf(RuleConflictUiState.DUPLICATE_OPERATOR_USAGE),
            uiState.tiles[0].liveRuleConflicts
        )
        assertEquals(TileVisualState.LIVE_RULE_CONFLICT, uiState.tiles[0].visualState)
        assertEquals(
            setOf(RuleConflictUiState.DUPLICATE_OPERATOR_USAGE),
            uiState.tiles[1].liveRuleConflicts
        )
        assertEquals(TileVisualState.LIVE_RULE_CONFLICT, uiState.tiles[1].visualState)
        assertEquals(
            setOf(RuleConflictUiState.DUPLICATE_OPERATOR_USAGE),
            optionForThree.multiplicationRuleConflicts
        )
        assertTrue(optionForOne.multiplicationRuleConflicts.isEmpty())
        assertTrue(optionForTwo.multiplicationRuleConflicts.isEmpty())
        assertNull(uiState.puzzleOutcome)
    }

    @Test
    fun maps_live_mismatched_pairing_conflicts_to_tiles_before_completion() {
        val puzzle = liveRulePresentationPuzzle()
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

        val uiState = GameUiState.from(puzzle)

        assertEquals(
            setOf(RuleConflictUiState.MISMATCHED_PAIRING),
            uiState.tiles[0].liveRuleConflicts
        )
        assertEquals(TileVisualState.LIVE_RULE_CONFLICT, uiState.tiles[0].visualState)
        assertEquals(
            setOf(RuleConflictUiState.MISMATCHED_PAIRING),
            uiState.tiles[1].liveRuleConflicts
        )
        assertEquals(TileVisualState.LIVE_RULE_CONFLICT, uiState.tiles[1].visualState)
        assertNull(uiState.puzzleOutcome)
    }

    @Test
    fun maps_reactive_mismatched_pairing_conflicts_to_operand_options() {
        val puzzle = liveRulePresentationPuzzle()
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

        val uiState = GameUiState.from(
            puzzle = puzzle,
            presentationState = GamePresentationState().showTileOperandSelection(
                tileIndex = 2,
                slot = OperandSlot.RIGHT
            )
        )
        val selectorDialog = uiState.tileOperandSelectionDialog!!
        val optionForOne = selectorDialog.availableOperands
            .first { operand -> operand.stripEntryId == 0 }
        val optionForThree = selectorDialog.availableOperands
            .first { operand -> operand.stripEntryId == 2 }

        assertEquals(
            setOf(RuleConflictUiState.MISMATCHED_PAIRING),
            optionForOne.additionRuleConflicts
        )
        assertEquals(
            setOf(RuleConflictUiState.MISMATCHED_PAIRING),
            optionForOne.multiplicationRuleConflicts
        )
        assertTrue(optionForThree.multiplicationRuleConflicts.isEmpty())
    }

    @Test
    fun maps_a_solved_puzzle_to_a_success_outcome() {
        val uiState = GameUiState.from(
            puzzle = solvedPuzzleWithKnownStripAndAssignments()
        )

        assertEquals(PuzzleOutcomeUiState.Solved, uiState.puzzleOutcome)
        assertTrue(uiState.isSuccessOverlayVisible)
    }

    @Test
    fun builds_an_operator_dialog_with_the_current_operator_selection() {
        val puzzle = initialPuzzle.withTile(
            index = 0,
            tile = hiddenTile(result = 223).withOperator(Operator.MULTIPLICATION)
        )

        val uiState = GameUiState.from(
            puzzle = puzzle,
            presentationState = GamePresentationState().showTileOperatorSelection(tileIndex = 0)
        )

        assertEquals(
            TileOperatorSelectionDialogUiState(
                tileIndex = 0,
                availableOperators = listOf(
                    Operator.ADDITION,
                    Operator.MULTIPLICATION
                ),
                initialOperator = Operator.MULTIPLICATION
            ),
            uiState.tileOperatorSelectionDialog
        )
    }

    @Test
    fun builds_an_operand_selection_dialog_without_collapsing_repeated_values() {
        val puzzle = puzzleWithRepeatedSixes()
            .withTile(
                index = 0,
                tile = hiddenTile(result = 12)
                    .withLeftOperand(value = 6, stripEntryId = 0)
                    .withOperator(Operator.ADDITION)
            )

        val uiState = GameUiState.from(
            puzzle = puzzle,
            presentationState = GamePresentationState().showTileOperandSelection(
                tileIndex = 1,
                slot = OperandSlot.LEFT
            )
        )
        val dialog = uiState.tileOperandSelectionDialog!!
        val repeatedSixOptions = dialog.availableOperands.filter { operand -> operand.value == 6 }
        val usedRepeatedSixOption = repeatedSixOptions.first { operand -> operand.stripEntryId == 0 }
        val unusedRepeatedSixOption = repeatedSixOptions.first { operand -> operand.stripEntryId == 1 }

        assertEquals(1, dialog.tileIndex)
        assertEquals(OperandSlot.LEFT, dialog.slot)
        assertEquals(
            listOf(0, 1),
            repeatedSixOptions.map(TileOperandOptionUiState::stripEntryId)
        )
        assertTrue(usedRepeatedSixOption.additionUsed)
        assertTrue(usedRepeatedSixOption.isSelectable)
        assertEquals(false, unusedRepeatedSixOption.additionUsed)
        assertTrue(unusedRepeatedSixOption.isSelectable)
    }

    @Test
    fun does_not_build_a_dialog_for_an_invalid_operator_selection_index() {
        val uiState = GameUiState.from(
            puzzle = initialPuzzle,
            presentationState = GamePresentationState().showTileOperatorSelection(tileIndex = 999)
        )

        assertNull(uiState.tileOperatorSelectionDialog)
    }

    @Test
    fun does_not_build_a_dialog_for_invalid_operand_selection_indexes() {
        listOf(-1, 999).forEach { invalidTileIndex ->
            val uiState = GameUiState.from(
                puzzle = initialPuzzle,
                presentationState = GamePresentationState().showTileOperandSelection(
                    tileIndex = invalidTileIndex,
                    slot = OperandSlot.LEFT
                )
            )

            assertNull(uiState.tileOperandSelectionDialog)
        }
    }
}

private fun assertStripUsage(stripItem: StripItemUiState, additionUsed: Boolean, multiplicationUsed: Boolean) {
    assertEquals(additionUsed, stripItem.additionUsed)
    assertEquals(multiplicationUsed, stripItem.multiplicationUsed)
}

private fun liveRulePresentationPuzzle(): Puzzle = Puzzle(
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
