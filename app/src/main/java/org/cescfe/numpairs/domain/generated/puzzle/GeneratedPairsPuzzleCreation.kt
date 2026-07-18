package org.cescfe.numpairs.domain.generated.puzzle

import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.UnorderedStripEntryPair
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState

sealed interface GeneratedPairsPuzzleCreation {
    data class Created(val puzzle: GeneratedPairsPuzzle) : GeneratedPairsPuzzleCreation

    data class Rejected(val violations: List<GeneratedPairsPuzzleValidationViolation>) : GeneratedPairsPuzzleCreation {
        init {
            require(violations.isNotEmpty()) {
                "A rejected generated puzzle requires at least one violation."
            }
        }
    }
}

sealed interface GeneratedPairsPuzzleValidationViolation {
    val ruleId: GeneratedPairsPuzzleValidationRuleId

    data class SolvedPuzzleNotSolved(val completionState: PuzzleCompletionState) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_PUZZLE_NOT_SOLVED
    }

    data class SolvedBoardTileCountMismatch(val expectedCount: Int, val observedCount: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_BOARD_TILE_COUNT_MISMATCH
    }

    data class SolvedStripEntryCountMismatch(val expectedCount: Int, val observedCount: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_STRIP_ENTRY_COUNT_MISMATCH
    }

    data class InitialBoardTileCountMismatch(val expectedCount: Int, val observedCount: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.INITIAL_BOARD_TILE_COUNT_MISMATCH
    }

    data class InitialStripEntryCountMismatch(val expectedCount: Int, val observedCount: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.INITIAL_STRIP_ENTRY_COUNT_MISMATCH
    }

    data class InitialTileExpressionsNotHidden(val tileIndexes: List<Int>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.INITIAL_TILE_EXPRESSIONS_NOT_HIDDEN
    }

    data class BoardResultsMismatch(val mismatchedTileIndexes: List<Int>) : GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.BOARD_RESULTS_MISMATCH
    }

    data class InitialStripEntryIdentitiesMismatch(
        val solvedEntryIds: List<StripEntryId>,
        val initialEntryIds: List<StripEntryId>
    ) : GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.INITIAL_STRIP_ENTRY_IDENTITIES_MISMATCH
    }

    data class InitialVisibleStripValueMismatch(val entryIds: Set<StripEntryId>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.INITIAL_VISIBLE_STRIP_VALUE_MISMATCH
    }

    data class InitialStripItemsNotMasked(val entryIds: Set<StripEntryId>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.INITIAL_STRIP_ITEMS_NOT_MASKED
    }

    data class SolvedStripValuesNotFullyKnown(val entryIds: Set<StripEntryId>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_STRIP_VALUES_NOT_FULLY_KNOWN
    }

    data class SolvedStripValuesNotSorted(val observedValues: List<Int>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_STRIP_VALUES_NOT_SORTED
    }

    data class SolvedTileAssignmentsIncomplete(val expectedCount: Int, val observedCount: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_TILE_ASSIGNMENTS_INCOMPLETE
    }

    data class SolvedOperandEntryReferenceInvalid(val unknownEntryIdsByTileIndex: Map<Int, Set<StripEntryId>>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_OPERAND_ENTRY_REFERENCE_INVALID
    }

    data class SolvedOperandValueMismatch(val tileIndexes: Set<Int>) : GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_OPERAND_VALUE_MISMATCH
    }

    data class SolvedStripEntryUsageMismatch(
        val additionUsageByEntryId: Map<StripEntryId, Int>,
        val multiplicationUsageByEntryId: Map<StripEntryId, Int>
    ) : GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_STRIP_ENTRY_USAGE_MISMATCH
    }

    data class SolvedSumProductPairingMismatch(
        val additionPairs: Set<UnorderedStripEntryPair>,
        val multiplicationPairs: Set<UnorderedStripEntryPair>
    ) : GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.SOLVED_SUM_PRODUCT_PAIRING_MISMATCH
    }

    data class StripValueOutsideRange(val allowedRange: IntRange, val observedValues: Set<Int>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.STRIP_VALUE_OUTSIDE_RANGE
    }

    data class StripValueOccurrenceExceeded(val maximumAllowed: Int, val observedOccurrencesByValue: Map<Int, Int>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.STRIP_VALUE_OCCURRENCE_EXCEEDED
    }

    data class RepeatedStripValueGroupCountExceeded(val maximumAllowed: Int, val observedRepeatedValues: Set<Int>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.REPEATED_STRIP_VALUE_GROUP_COUNT_EXCEEDED
    }

    data class DuplicateBoardResults(val duplicateResults: Set<Int>) : GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.DUPLICATE_BOARD_RESULTS
    }

    data class MultiplicationResultExceeded(val maximumAllowed: Int, val observedResultsByTileIndex: Map<Int, Int>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.MULTIPLICATION_RESULT_EXCEEDED
    }

    data class ProductAnchorMixOutsideRange(val expectedRange: IntRange, val observedCount: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.PRODUCT_ANCHOR_MIX_OUTSIDE_RANGE
    }

    data class KnownStripEntryCountOutsideRange(val expectedRange: IntRange, val observedCount: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.KNOWN_STRIP_ENTRY_COUNT_OUTSIDE_RANGE
    }

    data class HiddenStripEntryCountOutsideRange(val expectedRange: IntRange, val observedCount: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.HIDDEN_STRIP_ENTRY_COUNT_OUTSIDE_RANGE
    }

    data class RequiredKnownStripAnchorMissing(val missingEntryIds: Set<StripEntryId>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.REQUIRED_KNOWN_STRIP_ANCHOR_MISSING
    }

    data class HiddenStripRunExceeded(val maximumAllowed: Int, val maximumObserved: Int) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.HIDDEN_STRIP_RUN_EXCEEDED
    }

    data class KnownStripEntryDistributionMismatch(val knownEntryIds: Set<StripEntryId>) :
        GeneratedPairsPuzzleValidationViolation {
        override val ruleId = GeneratedPairsPuzzleValidationRuleId.KNOWN_STRIP_ENTRY_DISTRIBUTION_MISMATCH
    }
}

enum class GeneratedPairsPuzzleValidationRuleId(val code: String) {
    SOLVED_PUZZLE_NOT_SOLVED("generated.solved-puzzle-not-solved"),
    SOLVED_BOARD_TILE_COUNT_MISMATCH("generated.solved-board-tile-count-mismatch"),
    SOLVED_STRIP_ENTRY_COUNT_MISMATCH("generated.solved-strip-entry-count-mismatch"),
    INITIAL_BOARD_TILE_COUNT_MISMATCH("generated.initial-board-tile-count-mismatch"),
    INITIAL_STRIP_ENTRY_COUNT_MISMATCH("generated.initial-strip-entry-count-mismatch"),
    INITIAL_TILE_EXPRESSIONS_NOT_HIDDEN("generated.initial-tile-expressions-not-hidden"),
    BOARD_RESULTS_MISMATCH("generated.board-results-mismatch"),
    INITIAL_STRIP_ENTRY_IDENTITIES_MISMATCH("generated.initial-strip-entry-identities-mismatch"),
    INITIAL_VISIBLE_STRIP_VALUE_MISMATCH("generated.initial-visible-strip-value-mismatch"),
    INITIAL_STRIP_ITEMS_NOT_MASKED("generated.initial-strip-items-not-masked"),
    SOLVED_STRIP_VALUES_NOT_FULLY_KNOWN("generated.solved-strip-values-not-fully-known"),
    SOLVED_STRIP_VALUES_NOT_SORTED("generated.solved-strip-values-not-sorted"),
    SOLVED_TILE_ASSIGNMENTS_INCOMPLETE("generated.solved-tile-assignments-incomplete"),
    SOLVED_OPERAND_ENTRY_REFERENCE_INVALID("generated.solved-operand-entry-reference-invalid"),
    SOLVED_OPERAND_VALUE_MISMATCH("generated.solved-operand-value-mismatch"),
    SOLVED_STRIP_ENTRY_USAGE_MISMATCH("generated.solved-strip-entry-usage-mismatch"),
    SOLVED_SUM_PRODUCT_PAIRING_MISMATCH("generated.solved-sum-product-pairing-mismatch"),
    STRIP_VALUE_OUTSIDE_RANGE("generated.strip-value-outside-range"),
    STRIP_VALUE_OCCURRENCE_EXCEEDED("generated.strip-value-occurrence-exceeded"),
    REPEATED_STRIP_VALUE_GROUP_COUNT_EXCEEDED("generated.repeated-strip-value-group-count-exceeded"),
    DUPLICATE_BOARD_RESULTS("generated.duplicate-board-results"),
    MULTIPLICATION_RESULT_EXCEEDED("generated.multiplication-result-exceeded"),
    PRODUCT_ANCHOR_MIX_OUTSIDE_RANGE("generated.product-anchor-mix-outside-range"),
    KNOWN_STRIP_ENTRY_COUNT_OUTSIDE_RANGE("generated.known-strip-entry-count-outside-range"),
    HIDDEN_STRIP_ENTRY_COUNT_OUTSIDE_RANGE("generated.hidden-strip-entry-count-outside-range"),
    REQUIRED_KNOWN_STRIP_ANCHOR_MISSING("generated.required-known-strip-anchor-missing"),
    HIDDEN_STRIP_RUN_EXCEEDED("generated.hidden-strip-run-exceeded"),
    KNOWN_STRIP_ENTRY_DISTRIBUTION_MISMATCH("generated.known-strip-entry-distribution-mismatch")
}
