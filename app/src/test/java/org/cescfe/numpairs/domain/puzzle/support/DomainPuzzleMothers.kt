package org.cescfe.numpairs.domain.puzzle.support

import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem

fun puzzleWithRepeatedSixes(): Puzzle = samplePuzzle.copy(
    strip = Strip.fromItems(
        items = listOf(
            StripItem.Known(6),
            StripItem.Known(6),
            StripItem.Hidden,
            StripItem.Hidden,
            StripItem.Known(25),
            StripItem.Hidden,
            StripItem.Hidden,
            StripItem.Known(222)
        )
    )
)
