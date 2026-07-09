package org.cescfe.numpairs.domain.generated

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
        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 42
        ).generateWithSolution()
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
        val firstPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 1234
        ).generateWithSolution()
        val secondPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 1234
        ).generateWithSolution()

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun medium_generation_supports_one_repeated_values_prime_decoys_and_high_value_masking() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val generatedPuzzles = (1..40).map { seed ->
            GeneratedPairsPuzzleGenerator(
                profile = profile,
                seed = seed
            ).generateWithSolution()
        }
        val solvedStripValues = generatedPuzzles.flatMap { puzzle -> puzzle.solvedPuzzle.requireKnownStripValues() }
        val containsOne = solvedStripValues.any { value -> value == 1 }
        val containsRepeatedValue = generatedPuzzles.any { puzzle ->
            puzzle.solvedPuzzle.requireKnownStripValues()
                .groupingBy { value -> value }
                .eachCount()
                .any { (_, occurrenceCount) -> occurrenceCount == 2 }
        }
        val containsPrimeProductDecoy = generatedPuzzles.any { puzzle ->
            puzzle.solvedPuzzle.multiplicationTiles().any(Tile::isPrimeProductDecoy)
        }
        val containsHiddenHighValueTarget = generatedPuzzles.any { puzzle ->
            puzzle.initialPuzzle.strip.entries
                .takeLast(3)
                .any { entry -> entry.item == StripItem.Hidden }
        }

        assertTrue(profile.stripValuePolicy.allowsOne)
        assertTrue(containsOne)
        assertTrue(containsRepeatedValue)
        assertTrue(containsPrimeProductDecoy)
        assertTrue(containsHiddenHighValueTarget)
    }
}
