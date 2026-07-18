package org.cescfe.numpairs.domain.generated.profile

object GeneratedPuzzleProfiles {
    val FOUR_PAIRS_LOW: GeneratedPuzzleProfile = fourPairsLow()
    val EIGHT_PAIRS_MEDIUM: GeneratedPuzzleProfile = eightPairsMedium()
    val ALL: List<GeneratedPuzzleProfile> = listOf(FOUR_PAIRS_LOW, EIGHT_PAIRS_MEDIUM)

    private fun fourPairsLow(): GeneratedPuzzleProfile {
        val size = GeneratedPuzzleSize(pairCount = 4)

        return GeneratedPuzzleProfile.create(
            definition = GeneratedPuzzleProfileDefinition(
                id = GeneratedPuzzleProfileId("4-pairs-low"),
                difficulty = DifficultyTier.LOW,
                size = size,
                stripValuePolicy = StripValuePolicy(
                    valueRange = 2..20,
                    maxOccurrencesPerValue = 1
                ),
                resultConstraints = ResultConstraints(
                    maxMultiplicationResult = 150,
                    allowsDuplicateBoardResults = false
                ),
                initialStripMaskPolicy = InitialStripMaskPolicy(
                    knownEntryCountRange = 3..3,
                    requiredAnchors = setOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY),
                    distributionPolicy = StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE,
                    maxConsecutiveHiddenEntries = 2
                ),
                generationPolicy = GenerationPolicy(
                    isBoardTileShufflingEnabled = true
                )
            )
        ).getOrThrow()
    }

    private fun eightPairsMedium(): GeneratedPuzzleProfile {
        val size = GeneratedPuzzleSize(pairCount = 8)

        return GeneratedPuzzleProfile.create(
            definition = GeneratedPuzzleProfileDefinition(
                id = GeneratedPuzzleProfileId("8-pairs-medium"),
                difficulty = DifficultyTier.MEDIUM,
                size = size,
                stripValuePolicy = StripValuePolicy(
                    valueRange = 1..99,
                    maxOccurrencesPerValue = 2
                ),
                resultConstraints = ResultConstraints(
                    maxMultiplicationResult = 1000,
                    allowsDuplicateBoardResults = false,
                    productAnchorMix = ProductAnchorMix(
                        productResultGreaterThan = 198,
                        countRange = 2..4
                    )
                ),
                initialStripMaskPolicy = InitialStripMaskPolicy(
                    knownEntryCountRange = 6..7,
                    requiredAnchors = emptySet(),
                    distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
                    maxConsecutiveHiddenEntries = 4
                ),
                generationPolicy = GenerationPolicy(
                    isBoardTileShufflingEnabled = true
                ),
                varietyPolicy = GeneratedPuzzleVarietyPolicy(
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
                    ),
                    primeProductDecoyTarget = PrimeProductDecoyTarget(
                        targetPuzzlePercent = ProbabilityPercent(30),
                        targetPairCount = 1,
                        pairPattern = PrimeProductDecoyPairPattern.ONE_AND_PRIME
                    )
                )
            )
        ).getOrThrow()
    }
}
