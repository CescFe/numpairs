package org.cescfe.numpairs.domain.generated

sealed interface GeneratedPuzzleProfileCreation {
    data class Created(val profile: GeneratedPuzzleProfile) : GeneratedPuzzleProfileCreation

    data class Rejected(val violations: List<GeneratedPuzzleProfileViolation>) : GeneratedPuzzleProfileCreation {
        init {
            require(violations.isNotEmpty()) {
                "A rejected generated puzzle profile requires at least one violation."
            }
        }
    }
}

fun GeneratedPuzzleProfileCreation.getOrThrow(): GeneratedPuzzleProfile = when (this) {
    is GeneratedPuzzleProfileCreation.Created -> profile
    is GeneratedPuzzleProfileCreation.Rejected -> {
        val ruleIds = violations.joinToString { violation -> violation.ruleId.code }
        throw IllegalArgumentException("Generated puzzle profile is invalid: $ruleIds.")
    }
}

enum class GeneratedPuzzleProfileRuleId(val code: String) {
    STRIP_VALUE_CAPACITY("profile.strip-value-capacity"),
    KNOWN_ENTRY_RANGE("profile.known-entry-range"),
    REQUIRED_ANCHOR_CAPACITY("profile.required-anchor-capacity"),
    HIDDEN_RUN_FEASIBILITY("profile.hidden-run-feasibility"),
    SPREAD_DISTRIBUTION_CAPACITY("profile.spread-distribution-capacity"),
    HIGH_VALUE_TARGET_RANK("profile.high-value-target-rank"),
    DUPLICATE_HIGH_VALUE_TARGET_RANK("profile.duplicate-high-value-target-rank"),
    HIGH_VALUE_TARGET_ANCHOR_CONFLICT("profile.high-value-target-anchor-conflict"),
    HIGH_VALUE_TARGET_MASK_FEASIBILITY("profile.high-value-target-mask-feasibility"),
    PRODUCT_ANCHOR_COUNT_RANGE("profile.product-anchor-count-range"),
    PRODUCT_ANCHOR_THRESHOLD("profile.product-anchor-threshold"),
    PRODUCT_ANCHOR_CAPACITY("profile.product-anchor-capacity"),
    PRODUCT_ANCHOR_SELECTION_FEASIBILITY("profile.product-anchor-selection-feasibility"),
    PRIME_DECOY_TARGET_COUNT("profile.prime-decoy-target-count"),
    PRIME_DECOY_TARGET_FEASIBILITY("profile.prime-decoy-target-feasibility"),
    ELIGIBLE_VALUE_PAIR_CATALOG("profile.eligible-value-pair-catalog"),
    ARITHMETIC_RESULT_RANGE("profile.arithmetic-result-range"),
    VALIDATION_WORK_LIMIT("profile.validation-work-limit")
}

sealed interface GeneratedPuzzleProfileViolation {
    val ruleId: GeneratedPuzzleProfileRuleId

    data class InsufficientStripValueCapacity(val requiredEntryCount: Int, val availableEntryCount: Long) :
        GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.STRIP_VALUE_CAPACITY
    }

    data class KnownEntryRangeOutsidePuzzle(val configuredRange: IntRange, val stripEntryCount: Int) :
        GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.KNOWN_ENTRY_RANGE
    }

    data class RequiredAnchorsExceedKnownCount(val requiredAnchorCount: Int, val maximumKnownEntryCount: Int) :
        GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.REQUIRED_ANCHOR_CAPACITY
    }

    data class HiddenRunLimitInfeasible(
        val minimumKnownEntryCount: Int,
        val configuredRange: IntRange,
        val maxConsecutiveHiddenEntries: Int
    ) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.HIDDEN_RUN_FEASIBILITY
    }

    data class SpreadDistributionExceedsPairCount(val minimumKnownEntryCount: Int, val pairCount: Int) :
        GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId =
            GeneratedPuzzleProfileRuleId.SPREAD_DISTRIBUTION_CAPACITY
    }

    data class HighValueTargetRankOutsidePuzzle(val rankFromHighest: Int, val stripEntryCount: Int) :
        GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.HIGH_VALUE_TARGET_RANK
    }

    data class DuplicateHighValueTargetRank(val rankFromHighest: Int) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId =
            GeneratedPuzzleProfileRuleId.DUPLICATE_HIGH_VALUE_TARGET_RANK
    }

    data class HighValueTargetConflictsWithRequiredAnchor(
        val rankFromHighest: Int,
        val targetHiddenProbability: ProbabilityPercent
    ) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId =
            GeneratedPuzzleProfileRuleId.HIGH_VALUE_TARGET_ANCHOR_CONFLICT
    }

    data class HighValueTargetMaskInfeasible(
        val forcedKnownEntryIds: Set<Int>,
        val forcedHiddenEntryIds: Set<Int>,
        val knownEntryCountRange: IntRange,
        val maxConsecutiveHiddenEntries: Int
    ) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId =
            GeneratedPuzzleProfileRuleId.HIGH_VALUE_TARGET_MASK_FEASIBILITY
    }

    data class ProductAnchorCountOutsidePuzzle(val configuredRange: IntRange, val pairCount: Int) :
        GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_COUNT_RANGE
    }

    data class ProductAnchorThresholdUnreachable(
        val productResultGreaterThan: Int,
        val maximumReachableProduct: Int?,
        val minimumRequiredAnchorCount: Int
    ) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_THRESHOLD
    }

    data class InsufficientProductAnchorCapacity(
        val minimumRequiredAnchorCount: Int,
        val maximumStructurallyAvailableCount: Int
    ) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_CAPACITY
    }

    data class ProductAnchorSelectionInfeasible(val requiredPairCount: Int, val productAnchorCountRange: IntRange) :
        GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId =
            GeneratedPuzzleProfileRuleId.PRODUCT_ANCHOR_SELECTION_FEASIBILITY
    }

    data class PrimeDecoyTargetCountOutsidePuzzle(val targetPairCount: Int, val pairCount: Int) :
        GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.PRIME_DECOY_TARGET_COUNT
    }

    data class PrimeDecoyTargetUnreachable(
        val pairPattern: PrimeProductDecoyPairPattern,
        val targetPairCount: Int,
        val maximumStructurallyAvailableCount: Int
    ) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId =
            GeneratedPuzzleProfileRuleId.PRIME_DECOY_TARGET_FEASIBILITY
    }

    data class InsufficientEligibleValuePairCatalog(
        val requiredPairCount: Int,
        val maximumStructurallyAvailableCount: Int,
        val valueRange: IntRange,
        val maxMultiplicationResult: Int
    ) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.ELIGIBLE_VALUE_PAIR_CATALOG
    }

    data class ArithmeticResultMayOverflow(val maximumStripValue: Int) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.ARITHMETIC_RESULT_RANGE
    }

    data class ValidationWorkLimitExceeded(
        val workKind: GeneratedPuzzleProfileValidationWorkKind,
        val configuredLimit: Int,
        val consumedWork: Int
    ) : GeneratedPuzzleProfileViolation {
        override val ruleId: GeneratedPuzzleProfileRuleId = GeneratedPuzzleProfileRuleId.VALIDATION_WORK_LIMIT
    }
}
