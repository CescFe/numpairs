package org.cescfe.numpairs.feature.fourpairs

import org.cescfe.numpairs.domain.puzzle.model.Puzzle

fun interface FourPairsPuzzleProvider {
    fun nextPuzzle(): Puzzle
}
