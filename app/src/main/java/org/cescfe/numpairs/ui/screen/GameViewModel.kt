package org.cescfe.numpairs.ui.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cescfe.numpairs.domain.puzzle.Board
import org.cescfe.numpairs.domain.puzzle.Operator
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem

class GameViewModel(initialPuzzle: Puzzle = PuzzleSamples.prototype) : ViewModel() {
    private var puzzle: Puzzle = initialPuzzle
    private var stripItemEntryDialogIndex: Int? = null
    private var tileOperatorSelectionDialogIndex: Int? = null

    private val _uiState = MutableStateFlow(
        GameUiState.from(
            puzzle = puzzle,
            stripItemEntryDialogIndex = stripItemEntryDialogIndex,
            tileOperatorSelectionDialogIndex = tileOperatorSelectionDialogIndex
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onStripItemTapped(index: Int) {
        val stripItem = puzzle.strip.items.getOrNull(index)

        if (stripItem != StripItem.Hidden && stripItem !is StripItem.PlayerEntered) {
            return
        }

        tileOperatorSelectionDialogIndex = null
        stripItemEntryDialogIndex = index
        publishUiState()
    }

    fun onStripItemEntryDismissed() {
        if (stripItemEntryDialogIndex == null) {
            return
        }

        stripItemEntryDialogIndex = null
        publishUiState()
    }

    fun onTileOperatorTapped(index: Int) {
        if (puzzle.board.tiles.getOrNull(index) == null) {
            return
        }

        stripItemEntryDialogIndex = null
        tileOperatorSelectionDialogIndex = index
        publishUiState()
    }

    fun onTileOperatorSelectionDismissed() {
        if (tileOperatorSelectionDialogIndex == null) {
            return
        }

        tileOperatorSelectionDialogIndex = null
        publishUiState()
    }

    fun onStripItemEntryConfirmed(value: Int) {
        val index = stripItemEntryDialogIndex ?: return
        val currentStripItem = puzzle.strip.items.getOrNull(index) ?: return

        if (currentStripItem !is StripItem.Hidden && currentStripItem !is StripItem.PlayerEntered) {
            stripItemEntryDialogIndex = null
            publishUiState()
            return
        }

        val validRange = puzzle.strip.validEntryRangeFor(index)

        if (value !in validRange) {
            publishUiState()
            return
        }

        val updatedStripItems = puzzle.strip.items.toMutableList().apply {
            set(index, currentStripItem.completeWith(value))
        }

        puzzle = puzzle.copy(strip = Strip(updatedStripItems))
        stripItemEntryDialogIndex = null
        publishUiState()
    }

    fun onTileOperatorSelectionConfirmed(operator: Operator) {
        val index = tileOperatorSelectionDialogIndex ?: return
        val currentTile = puzzle.board.tiles.getOrNull(index) ?: return

        if (operator == Operator.Hidden) {
            publishUiState()
            return
        }

        val updatedTiles = puzzle.board.tiles.toMutableList().apply {
            set(index, currentTile.withOperator(operator))
        }

        puzzle = puzzle.copy(board = Board(updatedTiles))
        tileOperatorSelectionDialogIndex = null
        publishUiState()
    }

    private fun publishUiState() {
        _uiState.value = GameUiState.from(
            puzzle = puzzle,
            stripItemEntryDialogIndex = stripItemEntryDialogIndex,
            tileOperatorSelectionDialogIndex = tileOperatorSelectionDialogIndex
        )
    }
}
