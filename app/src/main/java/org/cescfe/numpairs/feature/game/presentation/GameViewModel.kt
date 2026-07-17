package org.cescfe.numpairs.feature.game.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.assignment.operandSelectionChoicesFor
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

class GameViewModel(initialPuzzle: Puzzle = samplePuzzle) : ViewModel() {
    private var puzzle: Puzzle = initialPuzzle
    private var presentationState = GamePresentationState()

    private val _currentPuzzle = MutableStateFlow(puzzle)
    val currentPuzzle: StateFlow<Puzzle> = _currentPuzzle.asStateFlow()

    private val _uiState = MutableStateFlow(
        GameUiState.from(puzzle = puzzle, presentationState = presentationState)
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun reset(initialPuzzle: Puzzle) {
        puzzle = initialPuzzle
        presentationState = GamePresentationState()
        _currentPuzzle.value = puzzle
        publishUiState()
    }

    fun onStripItemTapped(index: Int) {
        if (!canInteractWithPuzzle()) {
            return
        }

        if (!resolveActiveStripItemEntryInput()) {
            return
        }

        if (!canEditStripItem(index = index)) {
            return
        }

        val draftText = when (val stripItem = puzzle.strip.items[index]) {
            StripItem.Hidden -> ""
            is StripItem.PlayerEntered -> stripItem.value.toString()
            is StripItem.Known -> return
        }

        commit {
            showStripItemEntryInput(
                index = index,
                draftText = draftText
            )
        }
    }

    fun onStripItemEntryInputChanged(draftText: String) {
        val input = presentationState.stripItemEntryInput ?: return

        commit {
            updateStripItemEntryInputDraft(
                draftText = draftText,
                isInvalid = draftText.isInvalidStripItemEntryInput(
                    stripItemIndex = input.stripItemIndex
                )
            )
        }
    }

    fun onStripItemEntryInputConfirmed() {
        confirmStripItemEntryInput()
    }

    fun onStripItemEntryInputFocusLost() {
        confirmStripItemEntryInput()
    }

    fun onStripItemEntryInputCancelled() {
        commit { dismissStripItemEntryInput() }
    }

    fun onTileOperatorTapped(index: Int) {
        if (!canInteractWithPuzzle()) {
            return
        }

        if (!resolveActiveStripItemEntryInput()) {
            return
        }

        if (!hasTile(index = index)) {
            return
        }

        commit { showTileOperatorSelection(tileIndex = index) }
    }

    fun onTileOperatorSelectionDismissed() {
        commit { dismissTileOperatorSelection() }
    }

    fun onTileLeftOperandTapped(index: Int) {
        onTileOperandTapped(
            index = index,
            slot = OperandSlot.LEFT
        )
    }

    fun onTileRightOperandTapped(index: Int) {
        onTileOperandTapped(
            index = index,
            slot = OperandSlot.RIGHT
        )
    }

    fun onSuccessOverlayDismissed() {
        commit {
            dismissSuccessOverlay(isPuzzleSolved = puzzle.isSolved)
        }
    }

    fun onTileOperandSelectionDismissed() {
        commit { dismissTileOperandSelection() }
    }

    private fun confirmStripItemEntryInput(): Boolean = resolveActiveStripItemEntryInput()

    private fun resolveActiveStripItemEntryInput(): Boolean {
        val input = presentationState.stripItemEntryInput ?: return true
        val currentStripItem = puzzle.strip.items.getOrNull(input.stripItemIndex) ?: return true

        if (currentStripItem !is StripItem.Hidden && currentStripItem !is StripItem.PlayerEntered) {
            commit { dismissStripItemEntryInput() }
            return true
        }

        val resolvedDraftText = input.draftText
        if (resolvedDraftText.isBlank()) {
            commit { dismissStripItemEntryInput() }
            return true
        }

        val value = resolvedDraftText.toIntOrNull()
        if (value == null || value !in puzzle.strip.validEntryRangeFor(input.stripItemIndex)) {
            commit {
                showStripItemEntryInput(
                    index = input.stripItemIndex,
                    draftText = resolvedDraftText,
                    isInvalid = true
                )
            }
            return false
        }

        commit(
            updatedPuzzle = puzzle.copy(
                strip = puzzle.strip.withUpdatedEntry(
                    index = input.stripItemIndex,
                    value = value
                )
            )
        ) {
            dismissStripItemEntryInput()
        }

        return true
    }

    fun onTileOperandSelectionConfirmed(stripEntryId: Int) {
        val target = (presentationState.modal as? GameModalState.TileOperandSelection)?.target ?: return
        val updatedPuzzle = puzzle.withSelectedOperand(
            target = target,
            stripEntryId = stripEntryId
        ) ?: return

        commit(updatedPuzzle = updatedPuzzle) {
            dismissTileOperandSelection()
        }
    }

    fun onTileOperatorSelectionConfirmed(operator: Operator) {
        val tileIndex = (presentationState.modal as? GameModalState.TileOperatorSelection)?.tileIndex ?: return
        val updatedPuzzle = puzzle.withSelectedOperator(
            tileIndex = tileIndex,
            operator = operator
        ) ?: return

        commit(updatedPuzzle = updatedPuzzle) {
            dismissTileOperatorSelection()
        }
    }

    fun onTileResetTapped(index: Int) {
        if (!canInteractWithPuzzle()) {
            return
        }

        if (!resolveActiveStripItemEntryInput()) {
            return
        }

        val updatedPuzzle = puzzle.withResetTile(tileIndex = index) ?: return

        commit(updatedPuzzle = updatedPuzzle) {
            clearModal()
        }
    }

    private fun publishUiState() {
        _uiState.value = GameUiState.from(
            puzzle = puzzle,
            presentationState = presentationState
        )
    }

    private fun onTileOperandTapped(index: Int, slot: OperandSlot) {
        if (!canInteractWithPuzzle()) {
            return
        }

        if (!resolveActiveStripItemEntryInput()) {
            return
        }

        if (!hasTile(index = index)) {
            return
        }

        commit {
            showTileOperandSelection(
                tileIndex = index,
                slot = slot
            )
        }
    }

    private fun commit(
        updatedPuzzle: Puzzle = puzzle,
        updatePresentation: GamePresentationState.() -> GamePresentationState = { this }
    ) {
        val nextPresentationState = presentationState
            .updatePresentation()
            .onPuzzleChanged(isPuzzleSolved = updatedPuzzle.isSolved)
        val hasPuzzleChanged = updatedPuzzle != puzzle
        val hasStateChanged = hasPuzzleChanged || nextPresentationState != presentationState

        puzzle = updatedPuzzle
        presentationState = nextPresentationState

        if (hasStateChanged) {
            if (hasPuzzleChanged) {
                _currentPuzzle.value = puzzle
            }
            publishUiState()
        }
    }

    private fun canEditStripItem(index: Int): Boolean {
        val stripItem = puzzle.strip.items.getOrNull(index)

        return stripItem == StripItem.Hidden || stripItem is StripItem.PlayerEntered
    }

    private fun String.isInvalidStripItemEntryInput(stripItemIndex: Int): Boolean {
        if (isBlank()) {
            return false
        }

        val value = toIntOrNull() ?: return true

        return value !in puzzle.strip.validEntryRangeFor(stripItemIndex)
    }

    private fun hasTile(index: Int): Boolean = puzzle.board.tiles.getOrNull(index) != null

    private fun canInteractWithPuzzle(): Boolean =
        !presentationState.isSuccessOverlayVisible(isPuzzleSolved = puzzle.isSolved)

    private fun Puzzle.withSelectedOperand(target: TileOperandSelectionTarget, stripEntryId: Int): Puzzle? {
        val currentTile = board.tiles.getOrNull(target.tileIndex) ?: return null
        val selectionChoice = operandSelectionChoicesFor(
            tileIndex = target.tileIndex,
            slot = target.slot
        ).firstOrNull { choice ->
            choice.stripEntryId == stripEntryId
        } ?: return null
        val selectedValue = strip.visibleValueForEntry(stripEntryId) ?: return null

        if (!selectionChoice.canBeSelected) {
            return null
        }

        return withUpdatedTile(index = target.tileIndex) {
            when (target.slot) {
                OperandSlot.LEFT -> currentTile.withLeftOperand(
                    value = selectedValue,
                    stripEntryId = stripEntryId
                )
                OperandSlot.RIGHT -> currentTile.withRightOperand(
                    value = selectedValue,
                    stripEntryId = stripEntryId
                )
            }
        }
    }

    private fun Puzzle.withSelectedOperator(tileIndex: Int, operator: Operator): Puzzle? {
        if (operator == Operator.Hidden) {
            return null
        }

        return withUpdatedTile(index = tileIndex) { tile ->
            tile.withOperator(operator)
        }
    }

    private fun Puzzle.withResetTile(tileIndex: Int): Puzzle? {
        val currentTile = board.tiles.getOrNull(tileIndex) ?: return null

        if (!currentTile.canReset) {
            return null
        }

        return withUpdatedTile(index = tileIndex) { tile ->
            tile.reset()
        }
    }

    private inline fun Puzzle.withUpdatedTile(index: Int, update: (Tile) -> Tile): Puzzle? {
        val currentTile = board.tiles.getOrNull(index) ?: return null

        return copy(
            board = Board(
                tiles = board.tiles.toMutableList().apply {
                    set(index, update(currentTile))
                }
            )
        )
    }
}
