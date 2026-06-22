package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePresentationStateTest {
    @Test
    fun opening_a_modal_replaces_any_previously_open_modal() {
        val presentationState = GamePresentationState()
            .showStripItemEntry(index = 1)
            .showTileOperatorSelection(tileIndex = 3)

        assertEquals(
            GameModalState.TileOperatorSelection(tileIndex = 3),
            presentationState.modal
        )
    }

    @Test
    fun dismissing_a_modal_only_affects_the_matching_modal_kind() {
        val stripItemEntryState = GamePresentationState()
            .showStripItemEntry(index = 1)
            .dismissTileOperatorSelection()

        val operatorSelectionState = GamePresentationState()
            .showTileOperatorSelection(tileIndex = 2)
            .dismissTileOperatorSelection()

        val operandSelectionState = GamePresentationState()
            .showTileOperandSelection(
                tileIndex = 4,
                slot = OperandSlot.RIGHT
            )
            .dismissTileOperandSelection()

        assertEquals(GameModalState.StripItemEntry(index = 1), stripItemEntryState.modal)
        assertNull(operatorSelectionState.modal)
        assertNull(operandSelectionState.modal)
    }

    @Test
    fun strip_item_entry_input_tracks_index_draft_and_validation_state() {
        val invalidInputState = GamePresentationState()
            .showStripItemEntryInput(index = 1, draftText = "9")
            .markStripItemEntryInputInvalid()

        assertEquals(
            StripItemEntryInputState(
                stripItemIndex = 1,
                draftText = "9",
                isInvalid = true
            ),
            invalidInputState.stripItemEntryInput
        )

        val updatedInputState = invalidInputState.updateStripItemEntryInputDraft(draftText = "3")

        assertEquals(
            StripItemEntryInputState(
                stripItemIndex = 1,
                draftText = "3",
                isInvalid = false
            ),
            updatedInputState.stripItemEntryInput
        )
    }

    @Test
    fun strip_item_entry_input_replaces_modal_state_and_can_be_dismissed() {
        val presentationState = GamePresentationState()
            .showStripItemEntry(index = 1)
            .showStripItemEntryInput(index = 2, draftText = "4")
            .dismissStripItemEntryInput()

        assertNull(presentationState.modal)
        assertNull(presentationState.stripItemEntryInput)
    }

    @Test
    fun opening_a_modal_clears_strip_item_entry_input_state() {
        val presentationState = GamePresentationState()
            .showStripItemEntryInput(index = 1, draftText = "4")
            .showTileOperatorSelection(tileIndex = 0)

        assertEquals(GameModalState.TileOperatorSelection(tileIndex = 0), presentationState.modal)
        assertNull(presentationState.stripItemEntryInput)
    }

    @Test
    fun success_overlay_is_visible_only_for_solved_puzzles_until_it_is_dismissed() {
        val initialState = GamePresentationState()
        val dismissedState = initialState.dismissSuccessOverlay(isPuzzleSolved = true)

        assertFalse(initialState.isSuccessOverlayVisible(isPuzzleSolved = false))
        assertTrue(initialState.isSuccessOverlayVisible(isPuzzleSolved = true))
        assertFalse(dismissedState.isSuccessOverlayVisible(isPuzzleSolved = true))
    }

    @Test
    fun success_overlay_dismissal_is_cleared_after_the_puzzle_becomes_unsolved() {
        val restoredState = GamePresentationState()
            .dismissSuccessOverlay(isPuzzleSolved = true)
            .onPuzzleChanged(isPuzzleSolved = false)

        assertTrue(restoredState.isSuccessOverlayVisible(isPuzzleSolved = true))
    }
}
