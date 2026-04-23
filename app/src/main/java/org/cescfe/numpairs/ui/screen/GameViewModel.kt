package org.cescfe.numpairs.ui.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem

class GameViewModel(initialPuzzle: Puzzle = PuzzleSamples.prototype) : ViewModel() {
    private var puzzle: Puzzle = initialPuzzle
    private var stripItemEntryDialogIndex: Int? = null

    private val _uiState = MutableStateFlow(
        GameUiState.from(
            puzzle = puzzle,
            stripItemEntryDialogIndex = stripItemEntryDialogIndex
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun onStripItemTapped(index: Int) {
        if (puzzle.strip.items.getOrNull(index) != StripItem.Hidden) {
            return
        }

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

    fun onStripItemEntryConfirmed(value: Int) {
        val index = stripItemEntryDialogIndex ?: return
        val currentStripItem = puzzle.strip.items.getOrNull(index) ?: return

        if (currentStripItem !is StripItem.Hidden) {
            stripItemEntryDialogIndex = null
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

    private fun publishUiState() {
        _uiState.value = GameUiState.from(
            puzzle = puzzle,
            stripItemEntryDialogIndex = stripItemEntryDialogIndex
        )
    }
}
