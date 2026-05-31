package org.cescfe.numpairs.feature.game.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle as defaultInitialPuzzle
import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.domain.puzzle.Tile
import org.cescfe.numpairs.domain.puzzle.operandSelectionChoicesFor

class GameViewModel(initialPuzzle: Puzzle = defaultInitialPuzzle) : ViewModel() {
    private var puzzle: Puzzle = initialPuzzle
    private var presentationState = GamePresentationState()

    private val _uiState = MutableStateFlow(
        GameUiState.from(puzzle = puzzle, presentationState = presentationState)
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun reset(initialPuzzle: Puzzle) {
        puzzle = initialPuzzle
        presentationState = GamePresentationState()
        publishUiState()
    }

    fun onStripItemTapped(index: Int) {
        if (!canInteractWithPuzzle()) {
            return
        }

        if (!canEditStripItem(index = index)) {
            return
        }

        commit { showStripItemEntry(index = index) }
    }

    fun onStripItemEntryDismissed() {
        commit { dismissStripItemEntry() }
    }

    fun onTileOperatorTapped(index: Int) {
        if (!canInteractWithPuzzle()) {
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

    fun onStripItemEntryConfirmed(value: Int) {
        val index = (presentationState.modal as? GameModalState.StripItemEntry)?.index ?: return
        val currentStripItem = puzzle.strip.items.getOrNull(index) ?: return

        if (currentStripItem !is StripItem.Hidden && currentStripItem !is StripItem.PlayerEntered) {
            commit { dismissStripItemEntry() }
            return
        }

        if (value !in puzzle.strip.validEntryRangeFor(index)) {
            return
        }

        commit(
            updatedPuzzle = puzzle.copy(
                strip = puzzle.strip.withUpdatedEntry(
                    index = index,
                    value = value
                )
            )
        ) {
            dismissStripItemEntry()
        }
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
        val hasStateChanged = updatedPuzzle != puzzle || nextPresentationState != presentationState

        puzzle = updatedPuzzle
        presentationState = nextPresentationState

        if (hasStateChanged) {
            publishUiState()
        }
    }

    private fun canEditStripItem(index: Int): Boolean {
        val stripItem = puzzle.strip.items.getOrNull(index)

        return stripItem == StripItem.Hidden || stripItem is StripItem.PlayerEntered
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
