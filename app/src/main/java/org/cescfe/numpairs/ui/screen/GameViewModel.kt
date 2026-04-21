package org.cescfe.numpairs.ui.screen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState.from(PuzzleSamples.prototype))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
}
