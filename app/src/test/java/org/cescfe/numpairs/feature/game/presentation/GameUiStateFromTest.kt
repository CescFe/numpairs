package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripEntryRange
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.support.TileAssignment
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
    fun builds_create_mode_strip_item_dialog_for_hidden_entries() {
        val uiState = GameUiState.from(
            puzzle = initialPuzzle,
            presentationState = GamePresentationState().showStripItemEntry(index = 1)
        )

        assertEquals(
            StripItemEntryDialogUiState(
                stripItemIndex = 1,
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                mode = StripItemEntryDialogMode.CREATE,
                initialValue = ""
            ),
            uiState.stripItemEntryDialog
        )
    }

    @Test
    fun builds_edit_mode_strip_item_dialog_for_player_entered_entries() {
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

        val uiState = GameUiState.from(
            puzzle = puzzle,
            presentationState = GamePresentationState().showStripItemEntry(index = 1)
        )

        assertEquals(
            StripItemEntryDialogUiState(
                stripItemIndex = 1,
                validRange = StripEntryRange(minimumValue = 1, maximumValue = 6),
                mode = StripItemEntryDialogMode.EDIT,
                initialValue = "2"
            ),
            uiState.stripItemEntryDialog
        )
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
    fun builds_an_operand_selection_dialog_from_operand_selection_hints() {
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

        assertEquals(
            TileOperandSelectionDialogUiState(
                tileIndex = 1,
                slot = OperandSlot.LEFT,
                availableOperands = listOf(
                    TileOperandOptionUiState(
                        stripEntryId = 0,
                        value = 6,
                        additionUsed = true,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        stripEntryId = 1,
                        value = 6,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        stripEntryId = 4,
                        value = 25,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    ),
                    TileOperandOptionUiState(
                        stripEntryId = 7,
                        value = 222,
                        additionUsed = false,
                        multiplicationUsed = false,
                        isSelectable = true
                    )
                )
            ),
            uiState.tileOperandSelectionDialog
        )
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
