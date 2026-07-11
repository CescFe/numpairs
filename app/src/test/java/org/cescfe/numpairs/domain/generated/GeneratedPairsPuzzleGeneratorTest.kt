package org.cescfe.numpairs.domain.generated

import kotlin.random.Random
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsStripEntryVisibilityDirective
import org.cescfe.numpairs.domain.generated.internal.GeneratedPairsVariationPlanSelector
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class GeneratedPairsPuzzleGeneratorTest {

    @Test
    fun generate_returns_the_initial_player_facing_puzzle() {
        val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 2026
        ).generateWithSolution()
        val initialPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 2026
        ).generate()

        assertEquals(generatedPuzzle.initialPuzzle, initialPuzzle)
        assertGeneratedInitialPuzzleStructure(
            puzzle = initialPuzzle,
            profile = profile
        )
    }

    @Test
    fun generator_rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPairsPuzzleGenerator(
                profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
                maxAttempts = 0
            )
        }
    }

    @Test
    fun generator_returns_a_hard_valid_fallback_when_the_soft_mask_plan_is_infeasible() {
        val size = GeneratedPuzzleSize(pairCount = 2)
        val profile = GeneratedPuzzleProfile.create(
            definition = GeneratedPuzzleProfileDefinition(
                id = GeneratedPuzzleProfileId("joint-soft-mask-fallback"),
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

        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = seed,
            maxAttempts = 1
        ).generateWithSolution()

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
