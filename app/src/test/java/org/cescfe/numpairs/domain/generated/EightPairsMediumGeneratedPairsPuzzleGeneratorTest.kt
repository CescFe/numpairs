package org.cescfe.numpairs.domain.generated

import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class EightPairsMediumGeneratedPairsPuzzleGeneratorTest {

    @Test
    fun generate_returns_the_initial_player_facing_puzzle() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val generatedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 2026
        ).generateWithSolution()
        val initialPuzzle = GeneratedPairsPuzzleGenerator(
            profile = profile,
            seed = 2026
        ).generate()

        assertEquals(generatedPuzzle.initialPuzzle, initialPuzzle)
        assertInitialPuzzleStructure(
            puzzle = initialPuzzle,
            profile = profile
        )
    }

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
        assertInitialPuzzleStructure(
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
    fun generator_rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException::class.java) {
            GeneratedPairsPuzzleGenerator(
                profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
                maxAttempts = 0
            )
        }
    }

    @Test
    fun generator_fails_after_bounded_attempts_when_profile_cannot_be_satisfied() {
        val profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
        val impossibleProfile = profile.copy(
            resultConstraints = profile.resultConstraints.copy(
                maxMultiplicationResult = 1
            )
        )

        assertThrows(IllegalStateException::class.java) {
            GeneratedPairsPuzzleGenerator(
                profile = impossibleProfile,
                seed = 2026,
                maxAttempts = 1
            ).generateWithSolution()
        }
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

private fun assertInitialPuzzleStructure(puzzle: Puzzle, profile: GeneratedPuzzleProfile) {
    val knownEntryIds = puzzle.knownEntryIds()

    assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
    assertEquals(profile.size.boardTileCount, puzzle.board.tiles.size)
    assertEquals(profile.size.stripEntryCount, puzzle.strip.entries.size)
    assertTrue(puzzle.board.tiles.all(Tile::hasHiddenExpression))
    assertTrue(knownEntryIds.size in profile.initialStripMaskPolicy.knownEntryCountRange)
    assertTrue(
        puzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden } in
            profile.initialStripMaskPolicy.hiddenEntryCountRange
    )
    assertTrue(
        knownEntryIds.maxConsecutiveHiddenEntries(
            totalEntryCount = profile.size.stripEntryCount
        ) <= profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
    )
}

private fun Puzzle.requireKnownStripValues(): List<Int> = strip.entries.map { entry ->
    (entry.item as StripItem.Known).value
}

private fun Puzzle.additionTiles(): List<Tile> = tilesFor(operator = Operator.ADDITION)

private fun Puzzle.multiplicationTiles(): List<Tile> = tilesFor(operator = Operator.MULTIPLICATION)

private fun Puzzle.tilesFor(operator: Operator): List<Tile> = board.tiles.filter { tile ->
    tile.expression.operator == operator
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

private fun Tile.isPrimeProductDecoy(): Boolean {
    val leftOperand = expression.leftOperand as? Expression.Operand.Known ?: return false
    val rightOperand = expression.rightOperand as? Expression.Operand.Known ?: return false

    return expression.operator == Operator.MULTIPLICATION &&
        (
            leftOperand.value == 1 &&
                rightOperand.value.isPrime() ||
                rightOperand.value == 1 &&
                leftOperand.value.isPrime()
            )
}

private fun Puzzle.knownEntryIds(): Set<Int> = strip.entries
    .filter { entry -> entry.item is StripItem.Known }
    .map { entry -> entry.id }
    .toSet()

private fun Set<Int>.maxConsecutiveHiddenEntries(totalEntryCount: Int): Int {
    var currentHiddenCount = 0
    var maxHiddenCount = 0

    repeat(totalEntryCount) { entryId ->
        if (entryId in this) {
            currentHiddenCount = 0
        } else {
            currentHiddenCount++
            maxHiddenCount = maxOf(maxHiddenCount, currentHiddenCount)
        }
    }

    return maxHiddenCount
}

private fun Int.isPrime(): Boolean {
    if (this < 2) {
        return false
    }

    for (candidateDivisor in 2..this / 2) {
        if (this % candidateDivisor == 0) {
            return false
        }
    }

    return true
}
