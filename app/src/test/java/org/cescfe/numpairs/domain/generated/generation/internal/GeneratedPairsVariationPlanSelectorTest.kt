package org.cescfe.numpairs.domain.generated.generation.internal

import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileDefinition
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.HighValueMaskTarget
import org.cescfe.numpairs.domain.generated.profile.ProbabilityPercent
import org.cescfe.numpairs.domain.generated.profile.getOrThrow
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedPairsVariationPlanSelectorTest {

    @Test
    fun missing_targets_are_unrestricted_and_consume_no_probability_rolls() {
        val plan = GeneratedPairsVariationPlanSelector(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            nextProbabilityRoll = {
                error("Profiles without variety targets must not consume a probability roll.")
            }
        ).select()

        assertEquals(
            GeneratedPairsPrimeProductDecoyDirective.Unrestricted,
            plan.primeProductDecoyDirective
        )
        assertTrue(plan.stripEntryVisibilityDirectives.isEmpty())
    }

    @Test
    fun zero_percent_excludes_decoys_and_keeps_targeted_entries_known() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.withTargetProbabilities(percentage = 0)
        val plan = GeneratedPairsVariationPlanSelector(
            profile = profile,
            nextProbabilityRoll = { 0 }
        ).select()

        assertEquals(GeneratedPairsPrimeProductDecoyDirective.Exclude, plan.primeProductDecoyDirective)
        assertEquals(profile.targetedHighValueEntryIds(), plan.stripEntryVisibilityDirectives.keys)
        assertTrue(
            plan.stripEntryVisibilityDirectives.values.all { directive ->
                directive == GeneratedPairsStripEntryVisibilityDirective.KNOWN
            }
        )
    }

    @Test
    fun one_hundred_percent_includes_decoys_and_hides_targeted_entries() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.withTargetProbabilities(percentage = 100)
        val plan = GeneratedPairsVariationPlanSelector(
            profile = profile,
            nextProbabilityRoll = { 99 }
        ).select()

        assertEquals(
            GeneratedPairsPrimeProductDecoyDirective.Include(pairCount = 1),
            plan.primeProductDecoyDirective
        )
        assertEquals(profile.targetedHighValueEntryIds(), plan.stripEntryVisibilityDirectives.keys)
        assertTrue(
            plan.stripEntryVisibilityDirectives.values.all { directive ->
                directive == GeneratedPairsStripEntryVisibilityDirective.HIDDEN
            }
        )
    }

    @Test
    fun a_probability_roll_must_be_strictly_below_the_target() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val belowTargetRolls = ArrayDeque(listOf(29, 19, 39, 39))
        val belowTargetPlan = GeneratedPairsVariationPlanSelector(
            profile = profile,
            nextProbabilityRoll = belowTargetRolls::removeFirst
        ).select()

        assertEquals(
            GeneratedPairsPrimeProductDecoyDirective.Include(pairCount = 1),
            belowTargetPlan.primeProductDecoyDirective
        )
        assertTrue(
            belowTargetPlan.stripEntryVisibilityDirectives.values.all { directive ->
                directive == GeneratedPairsStripEntryVisibilityDirective.HIDDEN
            }
        )
        assertTrue(belowTargetRolls.isEmpty())

        val thresholdRolls = ArrayDeque(listOf(30, 20, 40, 39))
        val thresholdPlan = GeneratedPairsVariationPlanSelector(
            profile = profile,
            nextProbabilityRoll = thresholdRolls::removeFirst
        ).select()

        assertEquals(GeneratedPairsPrimeProductDecoyDirective.Exclude, thresholdPlan.primeProductDecoyDirective)
        assertEquals(
            GeneratedPairsStripEntryVisibilityDirective.KNOWN,
            thresholdPlan.visibilityDirectiveForRank(profile = profile, rankFromHighest = 1)
        )
        assertEquals(
            GeneratedPairsStripEntryVisibilityDirective.KNOWN,
            thresholdPlan.visibilityDirectiveForRank(profile = profile, rankFromHighest = 2)
        )
        assertEquals(
            GeneratedPairsStripEntryVisibilityDirective.HIDDEN,
            thresholdPlan.visibilityDirectiveForRank(profile = profile, rankFromHighest = 3)
        )
        assertTrue(thresholdRolls.isEmpty())
    }
}

private fun GeneratedPuzzleProfile.withTargetProbabilities(percentage: Int): GeneratedPuzzleProfile =
    GeneratedPuzzleProfile.create(
        definition = GeneratedPuzzleProfileDefinition(
            id = id,
            difficulty = difficulty,
            size = size,
            stripValuePolicy = stripValuePolicy,
            resultConstraints = resultConstraints,
            initialStripMaskPolicy = initialStripMaskPolicy,
            generationPolicy = generationPolicy,
            varietyPolicy = varietyPolicy.copy(
                highValueMaskTargets = varietyPolicy.highValueMaskTargets.map { target ->
                    HighValueMaskTarget(
                        rankFromHighest = target.rankFromHighest,
                        targetHiddenProbability = ProbabilityPercent(percentage)
                    )
                },
                primeProductDecoyTarget = requireNotNull(varietyPolicy.primeProductDecoyTarget).copy(
                    targetPuzzlePercent = ProbabilityPercent(percentage)
                )
            )
        )
    ).getOrThrow()

private fun GeneratedPairsVariationPlan.visibilityDirectiveForRank(
    profile: GeneratedPuzzleProfile,
    rankFromHighest: Int
): GeneratedPairsStripEntryVisibilityDirective? =
    stripEntryVisibilityDirectives[StripEntryId(profile.size.stripEntryCount - rankFromHighest)]

private fun GeneratedPuzzleProfile.targetedHighValueEntryIds(): Set<StripEntryId> =
    varietyPolicy.highValueMaskTargets.map { target ->
        StripEntryId(size.stripEntryCount - target.rankFromHighest)
    }.toSet()
