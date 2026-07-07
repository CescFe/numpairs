package org.cescfe.numpairs.domain.generated

data class GeneratedPuzzleProfileId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated puzzle profile id must not be blank."
        }
    }
}

data class GeneratedPuzzleProfile(
    val id: GeneratedPuzzleProfileId,
    val size: GeneratedPuzzleSize,
    val stripValuePolicy: StripValuePolicy,
    val resultConstraints: ResultConstraints,
    val initialStripMaskPolicy: InitialStripMaskPolicy,
    val generationPolicy: GenerationPolicy
) {
    init {
        require(resultConstraints.pairCount == size.pairCount) {
            "Result constraints must use the generated puzzle pair count."
        }
        require(initialStripMaskPolicy.stripEntryCount == size.stripEntryCount) {
            "Initial strip mask policy must use the generated puzzle strip entry count."
        }
    }
}

data class GeneratedPuzzleSize(val pairCount: Int) {
    init {
        require(pairCount > 0) {
            "Generated puzzle pair count must be positive."
        }
    }

    val stripEntryCount: Int = pairCount * 2
    val boardTileCount: Int = pairCount * 2
}

data class StripValuePolicy(val valueRange: IntRange, val maxOccurrencesPerValue: Int) {
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
    }

    val allowsOne: Boolean
        get() = 1 in valueRange
}

data class ResultConstraints(
    val pairCount: Int,
    val maxMultiplicationResult: Int,
    val allowsDuplicateBoardResults: Boolean,
    val productAnchorMix: ProductAnchorMix? = null
) {
    init {
        require(pairCount > 0) {
            "Result constraint pair count must be positive."
        }
        require(maxMultiplicationResult > 0) {
            "Maximum multiplication result must be positive."
        }

        productAnchorMix?.let { anchorMix ->
            require(!anchorMix.countRange.isEmpty()) {
                "Product-anchor count range must not be empty."
            }
            require(anchorMix.countRange.first >= 0 && anchorMix.countRange.last <= pairCount) {
                "Product-anchor count range must fit within the generated puzzle pair count."
            }
        }
    }
}

data class ProductAnchorMix(val productResultGreaterThan: Int, val countRange: IntRange) {
    init {
        require(productResultGreaterThan >= 0) {
            "Product-anchor threshold must not be negative."
        }
    }
}

data class InitialStripMaskPolicy(
    val stripEntryCount: Int,
    val knownEntryCountRange: IntRange,
    val requiredAnchors: Set<RequiredKnownStripAnchor>,
    val distributionPolicy: StripKnownEntryDistributionPolicy,
    val maxConsecutiveHiddenEntries: Int,
    val highValueMaskTargets: List<HighValueMaskTarget> = emptyList()
) {
    init {
        require(stripEntryCount > 0) {
            "Initial strip mask policy strip entry count must be positive."
        }
        require(!knownEntryCountRange.isEmpty()) {
            "Known strip entry count range must not be empty."
        }
        require(knownEntryCountRange.first >= 0 && knownEntryCountRange.last <= stripEntryCount) {
            "Known strip entry count range must fit within the generated puzzle strip entry count."
        }
        require(maxConsecutiveHiddenEntries > 0) {
            "Maximum consecutive hidden strip entries must be positive."
        }

        highValueMaskTargets.forEach { target ->
            require(target.rankFromHighest in 1..stripEntryCount) {
                "High-value mask target rank must fit within the generated puzzle strip entry count."
            }
        }
    }

    val hiddenEntryCountRange: IntRange =
        (stripEntryCount - knownEntryCountRange.last)..(stripEntryCount - knownEntryCountRange.first)
}

enum class RequiredKnownStripAnchor {
    HIGHEST_STRIP_ENTRY
}

enum class StripKnownEntryDistributionPolicy {
    SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE,
    UNRESTRICTED
}

data class HighValueMaskTarget(val rankFromHighest: Int, val targetHiddenProbability: ProbabilityPercent)

data class ProbabilityPercent(val value: Int) {
    init {
        require(value in 0..100) {
            "Probability percent must be between 0 and 100."
        }
    }
}

data class GenerationPolicy(
    val isBoardTileShufflingEnabled: Boolean,
    val isBoundedGenerationExpected: Boolean,
    val isDeterministicGenerationExpected: Boolean,
    val primeProductDecoyTarget: PrimeProductDecoyTarget? = null
)

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

enum class PrimeProductDecoyPairPattern {
    ONE_AND_PRIME
}

object GeneratedPuzzleProfiles {
    val FOUR_PAIRS_LOW: GeneratedPuzzleProfile = fourPairsLow()
    val EIGHT_PAIRS_MEDIUM: GeneratedPuzzleProfile = eightPairsMedium()

    private fun fourPairsLow(): GeneratedPuzzleProfile {
        val size = GeneratedPuzzleSize(pairCount = 4)

        return GeneratedPuzzleProfile(
            id = GeneratedPuzzleProfileId("4-pairs-low"),
            size = size,
            stripValuePolicy = StripValuePolicy(
                valueRange = 2..20,
                maxOccurrencesPerValue = 1
            ),
            resultConstraints = ResultConstraints(
                pairCount = size.pairCount,
                maxMultiplicationResult = 150,
                allowsDuplicateBoardResults = false
            ),
            initialStripMaskPolicy = InitialStripMaskPolicy(
                stripEntryCount = size.stripEntryCount,
                knownEntryCountRange = 3..3,
                requiredAnchors = setOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY),
                distributionPolicy = StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE,
                maxConsecutiveHiddenEntries = 2
            ),
            generationPolicy = GenerationPolicy(
                isBoardTileShufflingEnabled = true,
                isBoundedGenerationExpected = true,
                isDeterministicGenerationExpected = true
            )
        )
    }

    private fun eightPairsMedium(): GeneratedPuzzleProfile {
        val size = GeneratedPuzzleSize(pairCount = 8)

        return GeneratedPuzzleProfile(
            id = GeneratedPuzzleProfileId("8-pairs-medium"),
            size = size,
            stripValuePolicy = StripValuePolicy(
                valueRange = 1..99,
                maxOccurrencesPerValue = 2
            ),
            resultConstraints = ResultConstraints(
                pairCount = size.pairCount,
                maxMultiplicationResult = 1000,
                allowsDuplicateBoardResults = false,
                productAnchorMix = ProductAnchorMix(
                    productResultGreaterThan = 198,
                    countRange = 2..4
                )
            ),
            initialStripMaskPolicy = InitialStripMaskPolicy(
                stripEntryCount = size.stripEntryCount,
                knownEntryCountRange = 6..7,
                requiredAnchors = emptySet(),
                distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
                maxConsecutiveHiddenEntries = 4,
                highValueMaskTargets = listOf(
                    HighValueMaskTarget(
                        rankFromHighest = 1,
                        targetHiddenProbability = ProbabilityPercent(20)
                    ),
                    HighValueMaskTarget(
                        rankFromHighest = 2,
                        targetHiddenProbability = ProbabilityPercent(40)
                    ),
                    HighValueMaskTarget(
                        rankFromHighest = 3,
                        targetHiddenProbability = ProbabilityPercent(40)
                    )
                )
            ),
            generationPolicy = GenerationPolicy(
                isBoardTileShufflingEnabled = true,
                isBoundedGenerationExpected = true,
                isDeterministicGenerationExpected = true,
                primeProductDecoyTarget = PrimeProductDecoyTarget(
                    targetPuzzlePercent = ProbabilityPercent(30),
                    targetPairCount = 1,
                    pairPattern = PrimeProductDecoyPairPattern.ONE_AND_PRIME
                )
            )
        )
    }
}
