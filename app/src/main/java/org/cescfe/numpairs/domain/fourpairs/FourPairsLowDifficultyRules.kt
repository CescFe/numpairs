package org.cescfe.numpairs.domain.fourpairs

object FourPairsLowDifficultyRules {
    const val PAIR_COUNT = 4
    const val KNOWN_STRIP_ENTRY_COUNT = 3
    const val HIDDEN_STRIP_ENTRY_COUNT = 5
    const val MIN_STRIP_VALUE = 2
    const val MAX_STRIP_VALUE = 20
    const val MAX_MULTIPLICATION_RESULT = 150
    const val MAX_CONSECUTIVE_HIDDEN_ENTRIES = 2

    val stripValueRange: IntRange = MIN_STRIP_VALUE..MAX_STRIP_VALUE
}
