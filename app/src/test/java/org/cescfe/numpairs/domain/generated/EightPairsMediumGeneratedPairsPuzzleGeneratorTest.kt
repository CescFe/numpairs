package org.cescfe.numpairs.domain.generated

import kotlin.math.abs
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EightPairsMediumGeneratedPairsPuzzleGeneratorTest {

    @Test
    fun eight_pairs_medium_profile_generation_satisfies_profile_constraints() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val generatedPuzzle = generatedPuzzle(profile = profile, seed = 42)
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedStripValues = solvedPuzzle.requireKnownStripValues()
        val solvedBoardResults = solvedPuzzle.board.tiles.map(Tile::result)
        val multiplicationTiles = solvedPuzzle.multiplicationTiles()
        val productAnchorMix = requireNotNull(profile.resultConstraints.productAnchorMix)
        val productAnchorCount = multiplicationTiles.count { tile ->
            tile.result > productAnchorMix.productResultGreaterThan
        }

        assertEquals(PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
        assertEquals(profile.size.pairCount, solvedPuzzle.additionTiles().size)
        assertEquals(profile.size.pairCount, multiplicationTiles.size)
        assertEquals(profile.size.boardTileCount, solvedPuzzle.board.tiles.size)
        assertEquals(profile.size.stripEntryCount, solvedPuzzle.strip.entries.size)
        assertEquals(solvedStripValues.sorted(), solvedStripValues)
        assertTrue(solvedStripValues.all { value -> value in profile.stripValuePolicy.valueRange })
        assertTrue(
            solvedStripValues.groupingBy { value -> value }
                .eachCount()
                .all { (_, occurrenceCount) ->
                    occurrenceCount <= profile.stripValuePolicy.maxOccurrencesPerValue
                }
        )
        assertTrue(
            multiplicationTiles.all { tile ->
                tile.result <= profile.resultConstraints.maxMultiplicationResult
            }
        )
        assertEquals(profile.size.boardTileCount, solvedBoardResults.toSet().size)
        assertTrue(productAnchorCount in productAnchorMix.countRange)

        assertEquals(solvedBoardResults, initialPuzzle.board.tiles.map(Tile::result))
        assertGeneratedInitialPuzzleStructure(
            puzzle = initialPuzzle,
            profile = profile
        )
    }

    @Test
    fun eight_pairs_medium_profile_generation_is_deterministic_for_the_same_seed() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val firstPuzzle = generatedPuzzle(profile = profile, seed = 1234)
        val secondPuzzle = generatedPuzzle(profile = profile, seed = 1234)

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun medium_generation_meets_documented_variety_target_frequencies() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val generatedPuzzles = (1..VARIETY_SAMPLE_SIZE).map { seed ->
            generatedPuzzle(profile = profile, seed = seed)
        }
        val primeProductDecoyTarget = requireNotNull(profile.varietyPolicy.primeProductDecoyTarget)
        val primeProductDecoyPuzzleCount = generatedPuzzles.count { puzzle ->
            puzzle.solvedPuzzle.multiplicationTiles().any(Tile::isPrimeProductDecoy)
        }

        assertFrequencyWithinTarget(
            actualCount = primeProductDecoyPuzzleCount,
            sampleSize = generatedPuzzles.size,
            targetPercentage = primeProductDecoyTarget.targetPuzzlePercent
        )
        profile.varietyPolicy.highValueMaskTargets.forEach { target ->
            val targetEntryIndex = profile.size.stripEntryCount - target.rankFromHighest
            val hiddenPuzzleCount = generatedPuzzles.count { puzzle ->
                puzzle.initialPuzzle.strip.entries[targetEntryIndex].item == StripItem.Hidden
            }

            assertFrequencyWithinTarget(
                actualCount = hiddenPuzzleCount,
                sampleSize = generatedPuzzles.size,
                targetPercentage = target.targetHiddenProbability
            )
        }
    }

    @Test
    fun medium_generation_can_produce_repeated_values() {
        val generatedPuzzles = (1..40).map { seed ->
            generatedPuzzle(profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM, seed = seed)
        }
        val containsRepeatedValue = generatedPuzzles.any { puzzle ->
            puzzle.solvedPuzzle.requireKnownStripValues()
                .groupingBy { value -> value }
                .eachCount()
                .any { (_, occurrenceCount) -> occurrenceCount == 2 }
        }

        assertTrue(containsRepeatedValue)
    }

    private fun assertFrequencyWithinTarget(actualCount: Int, sampleSize: Int, targetPercentage: ProbabilityPercent) {
        val actualPercentage = actualCount * 100.0 / sampleSize

        assertTrue(
            "Expected ${targetPercentage.value}% within ±$VARIETY_TOLERANCE_PERCENTAGE_POINTS " +
                "percentage points, " +
                "but observed $actualPercentage% ($actualCount/$sampleSize).",
            abs(actualPercentage - targetPercentage.value) <= VARIETY_TOLERANCE_PERCENTAGE_POINTS
        )
    }

    private companion object {
        const val VARIETY_SAMPLE_SIZE = 500
        const val VARIETY_TOLERANCE_PERCENTAGE_POINTS = 5
    }
}
