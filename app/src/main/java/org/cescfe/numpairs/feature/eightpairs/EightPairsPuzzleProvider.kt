package org.cescfe.numpairs.feature.eightpairs

import org.cescfe.numpairs.domain.puzzle.model.Puzzle

fun interface EightPairsPuzzleProvider {
    fun nextPuzzle(): Puzzle
}
