package org.cescfe.numpairs.domain.generated.generation

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsStripEntryVisibilityDirective
import org.cescfe.numpairs.domain.generated.generation.internal.GeneratedPairsVariationPlanSelector
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileDefinition
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleSize
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleVarietyPolicy
import org.cescfe.numpairs.domain.generated.profile.GenerationPolicy
import org.cescfe.numpairs.domain.generated.profile.HighValueMaskTarget
import org.cescfe.numpairs.domain.generated.profile.InitialStripMaskPolicy
import org.cescfe.numpairs.domain.generated.profile.ProbabilityPercent
import org.cescfe.numpairs.domain.generated.profile.ResultConstraints
import org.cescfe.numpairs.domain.generated.profile.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.profile.StripValuePolicy
import org.cescfe.numpairs.domain.generated.profile.getOrThrow
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedPairsPuzzleGeneratorTest {

    @Test
    fun generate_returns_the_initial_player_facing_puzzle() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val generatedPuzzle = generatedPuzzle(profile = profile, seed = 2026)
        val initialPuzzle = generatedPuzzle.initialPuzzle

        assertEquals(generatedPuzzle.initialPuzzle, initialPuzzle)
        assertGeneratedInitialPuzzleStructure(
            puzzle = initialPuzzle,
            profile = profile
        )
    }

    @Test
    fun generation_execution_policy_rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPuzzleGenerationExecutionPolicy(maxAttempts = 0)
        }
    }

    @Test
    fun generator_reports_exact_search_budget_exhaustion_as_a_typed_failure() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val request = GeneratedPuzzleGenerationRequest(
            profile = profile,
            seed = 2026,
            executionPolicy = GeneratedPuzzleGenerationExecutionPolicy(
                maxAttempts = 10,
                maxSearchWork = 1
            )
        )

        val outcome = GeneratedPairsPuzzleGenerator(profile = profile).generate(request = request)

        val failure = outcome as GeneratedPairsPuzzleGenerationOutcome.Failed
        assertEquals(GeneratedPairsPuzzleGenerationFailureReason.SearchBudgetExhausted, failure.reason)
        assertEquals(profile.id, failure.request.profileId)
        assertEquals(1, failure.attemptsUsed)
        assertEquals(1, failure.searchWorkConsumed)
    }

    @Test
    fun generator_reports_cancellation_without_consuming_search_work() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val request = GeneratedPuzzleGenerationRequest(profile = profile, seed = 2026)

        val outcome = GeneratedPairsPuzzleGenerator(profile = profile).generate(
            request = request,
            cancellation = { true }
        )

        val failure = outcome as GeneratedPairsPuzzleGenerationOutcome.Failed
        assertEquals(GeneratedPairsPuzzleGenerationFailureReason.Cancelled, failure.reason)
        assertEquals(0, failure.attemptsUsed)
        assertEquals(0, failure.searchWorkConsumed)
        assertTrue(failure.candidateRejections.isEmpty())
    }

    @Test
    fun generator_returns_a_hard_valid_fallback_when_the_soft_mask_plan_is_infeasible() {
        val size = GeneratedPuzzleSize(pairCount = 2)
        val profile = GeneratedPuzzleProfile.create(
            definition = GeneratedPuzzleProfileDefinition(
                id = GeneratedPuzzleProfileId("joint-soft-mask-fallback"),
                difficulty = DifficultyTier.LOW,
                size = size,
                stripValuePolicy = StripValuePolicy(
                    valueRange = 2..5,
                    maxOccurrencesPerValue = 1
                ),
                resultConstraints = ResultConstraints(
                    maxMultiplicationResult = 25,
                    allowsDuplicateBoardResults = false
                ),
                initialStripMaskPolicy = InitialStripMaskPolicy(
                    knownEntryCountRange = 1..1,
                    requiredAnchors = emptySet(),
                    distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
                    maxConsecutiveHiddenEntries = 3
                ),
                generationPolicy = GenerationPolicy(
                    isBoardTileShufflingEnabled = true
                ),
                varietyPolicy = GeneratedPuzzleVarietyPolicy(
                    highValueMaskTargets = listOf(
                        HighValueMaskTarget(
                            rankFromHighest = 1,
                            targetHiddenProbability = ProbabilityPercent(0)
                        ),
                        HighValueMaskTarget(
                            rankFromHighest = 2,
                            targetHiddenProbability = ProbabilityPercent(1)
                        )
                    )
                )
            )
        ).getOrThrow()
        val seed = 2026
        val sampledPlan = GeneratedPairsVariationPlanSelector(
            profile = profile,
            random = Random(seed)
        ).select()
        val targetedEntryIds = setOf(
            StripEntryId(size.stripEntryCount - 1),
            StripEntryId(size.stripEntryCount - 2)
        )

        assertEquals(
            targetedEntryIds.associateWith { GeneratedPairsStripEntryVisibilityDirective.KNOWN },
            sampledPlan.stripEntryVisibilityDirectives
        )

        val generatedPuzzle = generatedPuzzle(
            profile = profile,
            seed = seed,
            executionPolicy = GeneratedPuzzleGenerationExecutionPolicy(maxAttempts = 1)
        )

        assertGeneratedInitialPuzzleStructure(
            puzzle = generatedPuzzle.initialPuzzle,
            profile = profile
        )
        assertFalse(
            targetedEntryIds.all { entryId ->
                generatedPuzzle.initialPuzzle.strip.entries.single { entry -> entry.id == entryId.value }.item is
                    StripItem.Known
            }
        )
    }
}
