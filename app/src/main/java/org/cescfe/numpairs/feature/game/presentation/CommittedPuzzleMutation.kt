package org.cescfe.numpairs.feature.game.presentation

import org.cescfe.numpairs.domain.puzzle.model.Puzzle

data class CommittedPuzzleMutation(val puzzle: Puzzle, val isCorrection: Boolean)
