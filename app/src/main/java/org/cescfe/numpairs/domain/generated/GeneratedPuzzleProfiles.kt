package org.cescfe.numpairs.domain.generated

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
