package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.OperandSlot

data class GamePresentationState(
    val modal: GameModalState? = null,
    val stripItemEntryInput: StripItemEntryInputState? = null,
    private val isSuccessOverlayDismissed: Boolean = false
) {
    fun showStripItemEntry(index: Int): GamePresentationState = copy(
        modal = GameModalState.StripItemEntry(index = index),
        stripItemEntryInput = null
    )

    fun dismissStripItemEntry(): GamePresentationState = when (modal) {
        is GameModalState.StripItemEntry -> copy(modal = null)
        else -> this
    }

    fun showStripItemEntryInput(index: Int, draftText: String, isInvalid: Boolean = false): GamePresentationState =
        copy(
            modal = null,
            stripItemEntryInput = StripItemEntryInputState(
                stripItemIndex = index,
                draftText = draftText,
                isInvalid = isInvalid
            )
        )

    fun updateStripItemEntryInputDraft(draftText: String, isInvalid: Boolean = false): GamePresentationState =
        stripItemEntryInput?.let { input ->
            copy(
                stripItemEntryInput = input.copy(
                    draftText = draftText,
                    isInvalid = isInvalid
                )
            )
        } ?: this

    fun markStripItemEntryInputInvalid(): GamePresentationState = stripItemEntryInput?.let { input ->
        copy(stripItemEntryInput = input.copy(isInvalid = true))
    } ?: this

    fun dismissStripItemEntryInput(): GamePresentationState = if (stripItemEntryInput == null) {
        this
    } else {
        copy(stripItemEntryInput = null)
    }

    fun showTileOperatorSelection(tileIndex: Int): GamePresentationState = copy(
        modal = GameModalState.TileOperatorSelection(tileIndex = tileIndex),
        stripItemEntryInput = null
    )

    fun dismissTileOperatorSelection(): GamePresentationState = when (modal) {
        is GameModalState.TileOperatorSelection -> copy(modal = null)
        else -> this
    }

    fun showTileOperandSelection(tileIndex: Int, slot: OperandSlot): GamePresentationState = copy(
        modal = GameModalState.TileOperandSelection(
            target = TileOperandSelectionTarget(
                tileIndex = tileIndex,
                slot = slot
            )
        ),
        stripItemEntryInput = null
    )

    fun dismissTileOperandSelection(): GamePresentationState = when (modal) {
        is GameModalState.TileOperandSelection -> copy(modal = null)
        else -> this
    }

    fun clearModal(): GamePresentationState = if (modal == null) {
        this
    } else {
        copy(modal = null)
    }

    fun dismissSuccessOverlay(isPuzzleSolved: Boolean): GamePresentationState =
        if (isSuccessOverlayVisible(isPuzzleSolved = isPuzzleSolved)) {
            copy(isSuccessOverlayDismissed = true)
        } else {
            this
        }

    fun onPuzzleChanged(isPuzzleSolved: Boolean): GamePresentationState =
        if (isPuzzleSolved || !isSuccessOverlayDismissed) {
            this
        } else {
            copy(isSuccessOverlayDismissed = false)
        }

    fun isSuccessOverlayVisible(isPuzzleSolved: Boolean): Boolean = isPuzzleSolved && !isSuccessOverlayDismissed
}

data class StripItemEntryInputState(val stripItemIndex: Int, val draftText: String, val isInvalid: Boolean = false)

sealed interface GameModalState {
    data class StripItemEntry(val index: Int) : GameModalState

    data class TileOperatorSelection(val tileIndex: Int) : GameModalState

    data class TileOperandSelection(val target: TileOperandSelectionTarget) : GameModalState
}

data class TileOperandSelectionTarget(val tileIndex: Int, val slot: OperandSlot)
