package org.cescfe.numpairs.domain.generated.profile

import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyAssessmentPolicy

data class GeneratedPuzzleProfileId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated puzzle profile id must not be blank."
        }
    }
}

data class GeneratedPuzzleProfileDefinition(
    val id: GeneratedPuzzleProfileId,
    val difficulty: DifficultyTier,
    val size: GeneratedPuzzleSize,
    val stripValuePolicy: StripValuePolicy,
    val resultConstraints: ResultConstraints,
    val initialStripMaskPolicy: InitialStripMaskPolicy,
    val generationPolicy: GenerationPolicy,
    val varietyPolicy: GeneratedPuzzleVarietyPolicy = GeneratedPuzzleVarietyPolicy(),
    val difficultyAssessmentPolicy: GeneratedPuzzleDifficultyAssessmentPolicy? = null
)

class GeneratedPuzzleProfile private constructor(definition: GeneratedPuzzleProfileDefinition) {
    val id: GeneratedPuzzleProfileId = definition.id
    val difficulty: DifficultyTier = definition.difficulty
    val size: GeneratedPuzzleSize = definition.size
    val stripValuePolicy: StripValuePolicy = definition.stripValuePolicy
    val resultConstraints: ResultConstraints = definition.resultConstraints
    val initialStripMaskPolicy: InitialStripMaskPolicy = definition.initialStripMaskPolicy
    val generationPolicy: GenerationPolicy = definition.generationPolicy
    val varietyPolicy: GeneratedPuzzleVarietyPolicy = definition.varietyPolicy
    val difficultyAssessmentPolicy: GeneratedPuzzleDifficultyAssessmentPolicy? = definition.difficultyAssessmentPolicy

    val hiddenEntryCountRange: IntRange = initialStripMaskPolicy.knownEntryCountRange.let { knownCountRange ->
        (size.stripEntryCount - knownCountRange.last)..(size.stripEntryCount - knownCountRange.first)
    }

    companion object {
        fun create(
            definition: GeneratedPuzzleProfileDefinition,
            validationLimits: GeneratedPuzzleProfileValidationLimits = GeneratedPuzzleProfileValidationLimits()
        ): GeneratedPuzzleProfileCreation {
            val snapshot = definition.snapshot()
            val violations = GeneratedPuzzleProfileSpecification.violationsFor(
                definition = snapshot,
                validationLimits = validationLimits
            )

            return if (violations.isEmpty()) {
                GeneratedPuzzleProfileCreation.Created(profile = GeneratedPuzzleProfile(definition = snapshot))
            } else {
                GeneratedPuzzleProfileCreation.Rejected(violations = violations)
            }
        }
    }
}

private fun GeneratedPuzzleProfileDefinition.snapshot(): GeneratedPuzzleProfileDefinition = copy(
    initialStripMaskPolicy = initialStripMaskPolicy.copy(
        requiredAnchors = initialStripMaskPolicy.requiredAnchors.toSet()
    ),
    varietyPolicy = varietyPolicy.copy(
        highValueMaskTargets = varietyPolicy.highValueMaskTargets.toList()
    )
)

data class GeneratedPuzzleSize(val pairCount: Int) {
    init {
        require(pairCount > 0) {
            "Generated puzzle pair count must be positive."
        }
        require(pairCount <= Int.MAX_VALUE / 2) {
            "Generated puzzle pair count is too large."
        }
    }

    val stripEntryCount: Int = pairCount * 2
    val boardTileCount: Int = pairCount * 2
}

data class StripValuePolicy(
    val valueRange: IntRange,
    val maxOccurrencesPerValue: Int,
    val maxRepeatedValueGroupCount: Int? = null
) {
    init {
        require(!valueRange.isEmpty()) {
            "Strip value range must not be empty."
        }
        require(valueRange.first >= 1) {
            "Strip values must be at least 1."
        }
        require(maxOccurrencesPerValue >= 1) {
            "Maximum occurrences per strip value must be at least 1."
        }
        require(maxRepeatedValueGroupCount == null || maxRepeatedValueGroupCount >= 0) {
            "Maximum repeated strip-value group count must not be negative."
        }
    }

    val allowsOne: Boolean
        get() = 1 in valueRange
}

data class ResultConstraints(
    val maxMultiplicationResult: Int,
    val allowsDuplicateBoardResults: Boolean,
    val productAnchorMix: ProductAnchorMix? = null
) {
    init {
        require(maxMultiplicationResult > 0) {
            "Maximum multiplication result must be positive."
        }
    }
}

data class ProductAnchorMix(val productResultGreaterThan: Int, val countRange: IntRange) {
    init {
        require(productResultGreaterThan >= 0) {
            "Product-anchor threshold must not be negative."
        }
        require(!countRange.isEmpty() && countRange.first >= 0) {
            "Product-anchor count range must contain non-negative counts."
        }
    }
}

data class InitialStripMaskPolicy(
    val knownEntryCountRange: IntRange,
    val requiredAnchors: Set<RequiredKnownStripAnchor>,
    val distributionPolicy: StripKnownEntryDistributionPolicy,
    val maxConsecutiveHiddenEntries: Int
) {
    init {
        require(!knownEntryCountRange.isEmpty() && knownEntryCountRange.first >= 0) {
            "Known strip entry count range must contain non-negative counts."
        }
        require(maxConsecutiveHiddenEntries > 0) {
            "Maximum consecutive hidden strip entries must be positive."
        }
    }
}

enum class RequiredKnownStripAnchor {
    HIGHEST_STRIP_ENTRY
}

internal fun Set<RequiredKnownStripAnchor>.resolveEntryIds(stripEntryCount: Int): Set<Int> = map { anchor ->
    when (anchor) {
        RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY -> stripEntryCount - 1
    }
}.toSet()

sealed interface StripKnownEntryDistributionPolicy {
    data object SpreadAcrossStripAndPairsWhenPossible : StripKnownEntryDistributionPolicy

    data object Unrestricted : StripKnownEntryDistributionPolicy

    data class AtLeastDistinctSolutionPairs(val minimumPairCount: Int) : StripKnownEntryDistributionPolicy {
        init {
            require(minimumPairCount > 0) {
                "Minimum distinct solution-pair count must be positive."
            }
        }
    }
}

data class GeneratedPuzzleVarietyPolicy(
    val highValueMaskTargets: List<HighValueMaskTarget> = emptyList(),
    val primeProductDecoyTarget: PrimeProductDecoyTarget? = null,
    val repeatedValueGroupTarget: RepeatedValueGroupTarget? = null
)

data class HighValueMaskTarget(val rankFromHighest: Int, val targetHiddenProbability: ProbabilityPercent)

data class ProbabilityPercent(val value: Int) {
    init {
        require(value in 0..100) {
            "Probability percent must be between 0 and 100."
        }
    }
}

data class GenerationPolicy(val isBoardTileShufflingEnabled: Boolean)

data class PrimeProductDecoyTarget(
    val targetPuzzlePercent: ProbabilityPercent,
    val targetPairCount: Int,
    val pairPattern: PrimeProductDecoyPairPattern
) {
    init {
        require(targetPairCount > 0) {
            "Prime-product decoy target pair count must be positive."
        }
    }
}

data class RepeatedValueGroupTarget(val targetPuzzlePercent: ProbabilityPercent, val targetGroupCount: Int) {
    init {
        require(targetGroupCount > 0) {
            "Repeated-value target group count must be positive."
        }
    }
}

enum class PrimeProductDecoyPairPattern {
    ONE_AND_PRIME
}
