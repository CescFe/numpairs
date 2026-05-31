package org.cescfe.numpairs.feature.game

data class GameCompletionActions(val onNewPuzzleRequested: () -> Unit, val onReturnToMenuRequested: () -> Unit)
