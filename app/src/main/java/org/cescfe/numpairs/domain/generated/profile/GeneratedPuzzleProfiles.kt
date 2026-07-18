package org.cescfe.numpairs.domain.generated.profile

import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyAssessmentExecutionPolicy
import org.cescfe.numpairs.domain.generated.assessment.GeneratedPuzzleDifficultyAssessmentPolicy

object GeneratedPuzzleProfiles {
    val FOUR_PAIRS_LOW: GeneratedPuzzleProfile = fourPairsLow()
    val FOUR_PAIRS_MEDIUM: GeneratedPuzzleProfile = fourPairsMedium()
    val EIGHT_PAIRS_MEDIUM: GeneratedPuzzleProfile = eightPairsMedium()
    val EIGHT_PAIRS_HARD: GeneratedPuzzleProfile = eightPairsHard()
    val ALL: List<GeneratedPuzzleProfile> =
        listOf(FOUR_PAIRS_LOW, FOUR_PAIRS_MEDIUM, EIGHT_PAIRS_MEDIUM, EIGHT_PAIRS_HARD)

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
                    distributionPolicy = StripKnownEntryDistributionPolicy.SpreadAcrossStripAndPairsWhenPossible,
                    maxConsecutiveHiddenEntries = 2
                ),
                generationPolicy = GenerationPolicy(
                    isBoardTileShufflingEnabled = true
                )
            )
        ).getOrThrow()
    }

    private fun fourPairsMedium(): GeneratedPuzzleProfile {
        val size = GeneratedPuzzleSize(pairCount = 4)

        return GeneratedPuzzleProfile.create(
            definition = GeneratedPuzzleProfileDefinition(
                id = GeneratedPuzzleProfileId("4-pairs-medium"),
                difficulty = DifficultyTier.MEDIUM,
                size = size,
                stripValuePolicy = StripValuePolicy(
                    valueRange = 1..40,
                    maxOccurrencesPerValue = 2,
                    maxRepeatedValueGroupCount = 1
                ),
                resultConstraints = ResultConstraints(
                    maxMultiplicationResult = 400,
                    allowsDuplicateBoardResults = false,
                    productAnchorMix = ProductAnchorMix(
                        productResultGreaterThan = 80,
                        countRange = 1..2
                    )
                ),
                initialStripMaskPolicy = InitialStripMaskPolicy(
                    knownEntryCountRange = 3..3,
                    requiredAnchors = emptySet(),
                    distributionPolicy = StripKnownEntryDistributionPolicy.AtLeastDistinctSolutionPairs(
                        minimumPairCount = 2
                    ),
                    maxConsecutiveHiddenEntries = 3
                ),
                generationPolicy = GenerationPolicy(
                    isBoardTileShufflingEnabled = true
                ),
                varietyPolicy = GeneratedPuzzleVarietyPolicy(
                    highValueMaskTargets = listOf(
                        HighValueMaskTarget(
                            rankFromHighest = 1,
                            targetHiddenProbability = ProbabilityPercent(25)
                        ),
                        HighValueMaskTarget(
                            rankFromHighest = 2,
                            targetHiddenProbability = ProbabilityPercent(40)
                        )
                    ),
                    primeProductDecoyTarget = PrimeProductDecoyTarget(
                        targetPuzzlePercent = ProbabilityPercent(30),
                        targetPairCount = 1,
                        pairPattern = PrimeProductDecoyPairPattern.ONE_AND_PRIME
                    ),
                    repeatedValueGroupTarget = RepeatedValueGroupTarget(
                        targetPuzzlePercent = ProbabilityPercent(35),
                        targetGroupCount = 1
                    )
                ),
                difficultyAssessmentPolicy = GeneratedPuzzleDifficultyAssessmentPolicy(
                    executionPolicy = GeneratedPuzzleDifficultyAssessmentExecutionPolicy(
                        maxCandidateExpansions = 25_000,
                        validSolutionCountLimit = 20
                    ),
                    minimumInitialPlausibleCandidateCount = 6,
                    minimumInitialForcedDeductionCount = 1,
                    minimumFirstForcedDeductionDepth = 1,
                    minimumPlausibleDecoyCount = 1,
                    minimumValidSolutionCount = 1
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
                    distributionPolicy = StripKnownEntryDistributionPolicy.Unrestricted,
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

    private fun eightPairsHard(): GeneratedPuzzleProfile {
        val size = GeneratedPuzzleSize(pairCount = 8)

        return GeneratedPuzzleProfile.create(
            definition = GeneratedPuzzleProfileDefinition(
                id = GeneratedPuzzleProfileId("8-pairs-hard"),
                difficulty = DifficultyTier.HARD,
                size = size,
                stripValuePolicy = StripValuePolicy(
                    valueRange = 1..99,
                    maxOccurrencesPerValue = 2,
                    minRepeatedValueGroupCount = 1,
                    maxRepeatedValueGroupCount = 2
                ),
                resultConstraints = ResultConstraints(
                    maxMultiplicationResult = 1000,
                    allowsDuplicateBoardResults = false,
                    productAnchorMix = ProductAnchorMix(
                        productResultGreaterThan = 198,
                        countRange = 0..1
                    )
                ),
                initialStripMaskPolicy = InitialStripMaskPolicy(
                    knownEntryCountRange = 4..5,
                    requiredAnchors = emptySet(),
                    distributionPolicy = StripKnownEntryDistributionPolicy.AtLeastDistinctSolutionPairs(
                        minimumPairCount = 3
                    ),
                    maxConsecutiveHiddenEntries = 5
                ),
                generationPolicy = GenerationPolicy(
                    isBoardTileShufflingEnabled = true,
                    maxAttempts = 160,
                    maxSearchWork = 600_000
                ),
                varietyPolicy = GeneratedPuzzleVarietyPolicy(
                    highValueMaskTargets = listOf(
                        HighValueMaskTarget(
                            rankFromHighest = 1,
                            targetHiddenProbability = ProbabilityPercent(60)
                        ),
                        HighValueMaskTarget(
                            rankFromHighest = 2,
                            targetHiddenProbability = ProbabilityPercent(70)
                        ),
                        HighValueMaskTarget(
                            rankFromHighest = 3,
                            targetHiddenProbability = ProbabilityPercent(70)
                        )
                    ),
                    primeProductDecoyTarget = PrimeProductDecoyTarget(
                        targetPuzzlePercent = ProbabilityPercent(60),
                        targetPairCount = 1,
                        pairPattern = PrimeProductDecoyPairPattern.ONE_AND_PRIME
                    )
                ),
                difficultyAssessmentPolicy = GeneratedPuzzleDifficultyAssessmentPolicy(
                    executionPolicy = GeneratedPuzzleDifficultyAssessmentExecutionPolicy(
                        maxCandidateExpansions = 100_000,
                        validSolutionCountLimit = 50
                    ),
                    minimumInitialPlausibleCandidateCount = 11,
                    minimumInitialForcedDeductionCount = 1,
                    maximumInitialForcedDeductionCount = 7,
                    minimumFirstForcedDeductionDepth = 1,
                    minimumPlausibleDecoyCount = 3,
                    minimumMaximumBranchingFactor = 2,
                    minimumExploredAmbiguousStateCount = 1,
                    minimumValidSolutionCount = 1
                )
            )
        ).getOrThrow()
    }
}
