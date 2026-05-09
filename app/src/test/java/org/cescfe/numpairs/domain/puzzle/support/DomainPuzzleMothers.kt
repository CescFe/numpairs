package org.cescfe.numpairs.domain.puzzle.support

import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.domain.puzzle.Strip
import org.cescfe.numpairs.domain.puzzle.StripItem

fun puzzleWithRepeatedSixes(): Puzzle = PuzzleSamples.prototype.copy(
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
