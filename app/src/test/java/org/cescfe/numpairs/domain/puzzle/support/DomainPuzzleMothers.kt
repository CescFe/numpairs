package org.cescfe.numpairs.domain.puzzle.support

import org.cescfe.numpairs.domain.puzzle.Puzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleSamples
import org.cescfe.numpairs.domain.puzzle.StripItem

fun prototypePuzzleWithRepeatedSixes(): Puzzle = PuzzleSamples.prototype.copy(
    strip = stripOf(
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
