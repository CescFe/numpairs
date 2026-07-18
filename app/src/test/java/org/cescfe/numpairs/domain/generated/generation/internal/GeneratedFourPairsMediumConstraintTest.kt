package org.cescfe.numpairs.domain.generated.generation.internal

import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileCreation
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileDefinition
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileViolation
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleSize
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleVarietyPolicy
import org.cescfe.numpairs.domain.generated.profile.GenerationPolicy
import org.cescfe.numpairs.domain.generated.profile.InitialStripMaskPolicy
import org.cescfe.numpairs.domain.generated.profile.ProbabilityPercent
import org.cescfe.numpairs.domain.generated.profile.RepeatedValueGroupTarget
import org.cescfe.numpairs.domain.generated.profile.ResultConstraints
import org.cescfe.numpairs.domain.generated.profile.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.profile.StripValuePolicy
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzleValidationViolation
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedFourPairsMediumConstraintTest {
    @Test
    fun solved_values_reject_more_repeated_groups_than_the_profile_allows() {
        val violations = GeneratedStripValueConstraint(
            policy = StripValuePolicy(
                valueRange = 1..40,
                maxOccurrencesPerValue = 2,
                maxRepeatedValueGroupCount = 1
            )
        ).violationsFor(values = listOf(1, 1, 2, 2, 3, 4, 5, 6))

        assertTrue(
            violations.any { violation ->
                violation is GeneratedPairsPuzzleValidationViolation.RepeatedStripValueGroupCountExceeded &&
                    violation.maximumAllowed == 1 && violation.observedRepeatedValues == setOf(1, 2)
            }
        )
    }

    @Test
    fun profile_rejects_an_unreachable_repetition_target() {
        val creation = profileCreation(
            stripValuePolicy = StripValuePolicy(
                valueRange = 1..40,
                maxOccurrencesPerValue = 1
            ),
            varietyPolicy = GeneratedPuzzleVarietyPolicy(
                repeatedValueGroupTarget = RepeatedValueGroupTarget(
                    targetPuzzlePercent = ProbabilityPercent(35),
                    targetGroupCount = 1
                )
            )
        )

        assertTrue(
            (creation as GeneratedPuzzleProfileCreation.Rejected).violations.any { violation ->
                violation is GeneratedPuzzleProfileViolation.RepeatedValueGroupTargetUnreachable
            }
        )
    }

    @Test
    fun profile_rejects_a_distinct_pair_distribution_that_known_entries_cannot_cover() {
        val creation = profileCreation(
            initialStripMaskPolicy = InitialStripMaskPolicy(
                knownEntryCountRange = 2..2,
                requiredAnchors = emptySet(),
                distributionPolicy = StripKnownEntryDistributionPolicy.AtLeastDistinctSolutionPairs(
                    minimumPairCount = 3
                ),
                maxConsecutiveHiddenEntries = 3
            )
        )

        assertTrue(
            (creation as GeneratedPuzzleProfileCreation.Rejected).violations.any { violation ->
                violation is GeneratedPuzzleProfileViolation.DistinctSolutionPairDistributionInfeasible
            }
        )
    }
}

private fun profileCreation(
    stripValuePolicy: StripValuePolicy = StripValuePolicy(
        valueRange = 1..40,
        maxOccurrencesPerValue = 2,
        maxRepeatedValueGroupCount = 1
    ),
    initialStripMaskPolicy: InitialStripMaskPolicy = InitialStripMaskPolicy(
        knownEntryCountRange = 3..3,
        requiredAnchors = emptySet(),
        distributionPolicy = StripKnownEntryDistributionPolicy.AtLeastDistinctSolutionPairs(minimumPairCount = 2),
        maxConsecutiveHiddenEntries = 3
    ),
    varietyPolicy: GeneratedPuzzleVarietyPolicy = GeneratedPuzzleVarietyPolicy()
): GeneratedPuzzleProfileCreation = GeneratedPuzzleProfile.create(
    definition = GeneratedPuzzleProfileDefinition(
        id = GeneratedPuzzleProfileId("medium-constraint-test"),
        difficulty = DifficultyTier.MEDIUM,
        size = GeneratedPuzzleSize(pairCount = 4),
        stripValuePolicy = stripValuePolicy,
        resultConstraints = ResultConstraints(
            maxMultiplicationResult = 400,
            allowsDuplicateBoardResults = false
        ),
        initialStripMaskPolicy = initialStripMaskPolicy,
        generationPolicy = GenerationPolicy(isBoardTileShufflingEnabled = true),
        varietyPolicy = varietyPolicy
    )
)
