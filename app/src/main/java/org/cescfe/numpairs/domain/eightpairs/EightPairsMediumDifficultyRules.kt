package org.cescfe.numpairs.domain.eightpairs

import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles

object EightPairsMediumDifficultyRules {
    const val PAIR_COUNT = 8
    const val STRIP_ENTRY_COUNT = PAIR_COUNT * 2
    const val BOARD_TILE_COUNT = PAIR_COUNT * 2
    const val MIN_KNOWN_STRIP_ENTRY_COUNT = 6
    const val MAX_KNOWN_STRIP_ENTRY_COUNT = 7
    const val MIN_HIDDEN_STRIP_ENTRY_COUNT = 9
    const val MAX_HIDDEN_STRIP_ENTRY_COUNT = 10
    const val MIN_STRIP_VALUE = 1
    const val MAX_STRIP_VALUE = 99
    const val MAX_OCCURRENCES_PER_STRIP_VALUE = 2
    const val MAX_MULTIPLICATION_RESULT = 1000
    const val PRODUCT_ANCHOR_RESULT_THRESHOLD = 198
    const val MIN_PRODUCT_ANCHOR_COUNT = 2
    const val MAX_PRODUCT_ANCHOR_COUNT = 4
    const val MAX_CONSECUTIVE_HIDDEN_ENTRIES = 4
    const val HIGHEST_STRIP_ENTRY_HIDDEN_TARGET_PERCENT = 20
    const val SECOND_HIGHEST_STRIP_ENTRY_HIDDEN_TARGET_PERCENT = 40
    const val THIRD_HIGHEST_STRIP_ENTRY_HIDDEN_TARGET_PERCENT = 40
    const val PRIME_PRODUCT_DECOY_TARGET_PERCENT = 30
    const val PRIME_PRODUCT_DECOY_PAIR_COUNT = 1

    val profile: GeneratedPuzzleProfile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
    val stripValueRange: IntRange = MIN_STRIP_VALUE..MAX_STRIP_VALUE
    val knownStripEntryCountRange: IntRange = MIN_KNOWN_STRIP_ENTRY_COUNT..MAX_KNOWN_STRIP_ENTRY_COUNT
    val hiddenStripEntryCountRange: IntRange = MIN_HIDDEN_STRIP_ENTRY_COUNT..MAX_HIDDEN_STRIP_ENTRY_COUNT
    val productAnchorCountRange: IntRange = MIN_PRODUCT_ANCHOR_COUNT..MAX_PRODUCT_ANCHOR_COUNT
}
