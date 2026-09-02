package org.cescfe.numpairs.domain.puzzle

@JvmInline
value class PuzzleCorrectionCount(val value: Long) {
    init {
        require(value >= 0) {
            "Puzzle correction count must not be negative."
        }
    }

    fun incremented(): PuzzleCorrectionCount {
        require(value < Long.MAX_VALUE) {
            "Puzzle correction count cannot overflow."
        }
        return PuzzleCorrectionCount(value + 1)
    }

    companion object {
        val ZERO: PuzzleCorrectionCount = PuzzleCorrectionCount(0)
    }
}
