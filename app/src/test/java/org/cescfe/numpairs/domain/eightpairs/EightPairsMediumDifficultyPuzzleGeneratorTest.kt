package org.cescfe.numpairs.domain.eightpairs

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class EightPairsMediumDifficultyPuzzleGeneratorTest {
    @Test
    fun rules_expose_the_medium_profile_constants() {
        val profile = EightPairsMediumDifficultyRules.profile

        assertSame(GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM, profile)
        assertEquals(EightPairsMediumDifficultyRules.PAIR_COUNT, profile.size.pairCount)
        assertEquals(EightPairsMediumDifficultyRules.STRIP_ENTRY_COUNT, profile.size.stripEntryCount)
        assertEquals(EightPairsMediumDifficultyRules.BOARD_TILE_COUNT, profile.size.boardTileCount)
        assertEquals(EightPairsMediumDifficultyRules.stripValueRange, profile.stripValuePolicy.valueRange)
        assertEquals(
            EightPairsMediumDifficultyRules.MAX_OCCURRENCES_PER_STRIP_VALUE,
            profile.stripValuePolicy.maxOccurrencesPerValue
        )
        assertEquals(
            EightPairsMediumDifficultyRules.MAX_MULTIPLICATION_RESULT,
            profile.resultConstraints.maxMultiplicationResult
        )
        assertFalse(profile.resultConstraints.allowsDuplicateBoardResults)
        assertEquals(
            EightPairsMediumDifficultyRules.productAnchorCountRange,
            requireNotNull(profile.resultConstraints.productAnchorMix).countRange
        )
        assertEquals(
            EightPairsMediumDifficultyRules.PRODUCT_ANCHOR_RESULT_THRESHOLD,
            requireNotNull(profile.resultConstraints.productAnchorMix).productResultGreaterThan
        )
        assertEquals(
            EightPairsMediumDifficultyRules.knownStripEntryCountRange,
            profile.initialStripMaskPolicy.knownEntryCountRange
        )
        assertEquals(
            EightPairsMediumDifficultyRules.hiddenStripEntryCountRange,
            profile.initialStripMaskPolicy.hiddenEntryCountRange
        )
        assertTrue(profile.initialStripMaskPolicy.requiredAnchors.isEmpty())
        assertEquals(
            EightPairsMediumDifficultyRules.MAX_CONSECUTIVE_HIDDEN_ENTRIES,
            profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
        )
        assertEquals(
            listOf(
                1 to EightPairsMediumDifficultyRules.HIGHEST_STRIP_ENTRY_HIDDEN_TARGET_PERCENT,
                2 to EightPairsMediumDifficultyRules.SECOND_HIGHEST_STRIP_ENTRY_HIDDEN_TARGET_PERCENT,
                3 to EightPairsMediumDifficultyRules.THIRD_HIGHEST_STRIP_ENTRY_HIDDEN_TARGET_PERCENT
            ),
            profile.initialStripMaskPolicy.highValueMaskTargets.map { target ->
                target.rankFromHighest to target.targetHiddenProbability.value
            }
        )
        assertEquals(
            EightPairsMediumDifficultyRules.PRIME_PRODUCT_DECOY_TARGET_PERCENT,
            requireNotNull(profile.generationPolicy.primeProductDecoyTarget).targetPuzzlePercent.value
        )
        assertEquals(
            EightPairsMediumDifficultyRules.PRIME_PRODUCT_DECOY_PAIR_COUNT,
            requireNotNull(profile.generationPolicy.primeProductDecoyTarget).targetPairCount
        )
        assertTrue(profile.generationPolicy.isBoardTileShufflingEnabled)
    }

    @Test
    fun generate_returns_the_initial_player_facing_puzzle() {
        val generatedPuzzle = EightPairsMediumDifficultyPuzzleGenerator(seed = 2026).generateWithSolution()
        val initialPuzzle = EightPairsMediumDifficultyPuzzleGenerator(seed = 2026).generate()

        assertEquals(generatedPuzzle.initialPuzzle, initialPuzzle)
        assertInitialPuzzleStructure(initialPuzzle)
    }

    @Test
    fun generated_puzzle_satisfies_medium_constraints() {
        val generatedPuzzle = EightPairsMediumDifficultyPuzzleGenerator(seed = 42).generateWithSolution()
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedStripValues = solvedPuzzle.requireKnownStripValues()
        val solvedBoardResults = solvedPuzzle.board.tiles.map(Tile::result)
        val multiplicationTiles = solvedPuzzle.multiplicationTiles()
        val productAnchorCount = multiplicationTiles.count { tile ->
            tile.result > EightPairsMediumDifficultyRules.PRODUCT_ANCHOR_RESULT_THRESHOLD
        }

        assertEquals(PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
        assertEquals(EightPairsMediumDifficultyRules.PAIR_COUNT, solvedPuzzle.additionTiles().size)
        assertEquals(EightPairsMediumDifficultyRules.PAIR_COUNT, multiplicationTiles.size)
        assertEquals(EightPairsMediumDifficultyRules.BOARD_TILE_COUNT, solvedPuzzle.board.tiles.size)
        assertEquals(EightPairsMediumDifficultyRules.STRIP_ENTRY_COUNT, solvedPuzzle.strip.entries.size)
        assertEquals(solvedStripValues.sorted(), solvedStripValues)
        assertTrue(solvedStripValues.all { value -> value in EightPairsMediumDifficultyRules.stripValueRange })
        assertTrue(
            solvedStripValues.groupingBy { value -> value }
                .eachCount()
                .all { (_, occurrenceCount) ->
                    occurrenceCount <= EightPairsMediumDifficultyRules.MAX_OCCURRENCES_PER_STRIP_VALUE
                }
        )
        assertTrue(
            multiplicationTiles.all { tile ->
                tile.result <= EightPairsMediumDifficultyRules.MAX_MULTIPLICATION_RESULT
            }
        )
        assertEquals(EightPairsMediumDifficultyRules.BOARD_TILE_COUNT, solvedBoardResults.toSet().size)
        assertTrue(productAnchorCount in EightPairsMediumDifficultyRules.productAnchorCountRange)

        assertEquals(solvedBoardResults, initialPuzzle.board.tiles.map(Tile::result))
        assertInitialPuzzleStructure(initialPuzzle)
    }

    @Test
    fun generated_puzzles_are_deterministic_for_the_same_seed() {
        val firstPuzzle = EightPairsMediumDifficultyPuzzleGenerator(seed = 1234).generateWithSolution()
        val secondPuzzle = EightPairsMediumDifficultyPuzzleGenerator(seed = 1234).generateWithSolution()

        assertEquals(firstPuzzle, secondPuzzle)
    }

    @Test
    fun generator_rejects_non_positive_max_attempts() {
        assertThrows(IllegalArgumentException::class.java) {
            EightPairsMediumDifficultyPuzzleGenerator(maxAttempts = 0)
        }
    }

    @Test
    fun generator_fails_after_bounded_attempts_when_profile_cannot_be_satisfied() {
        val impossibleProfile = EightPairsMediumDifficultyRules.profile.copy(
            resultConstraints = EightPairsMediumDifficultyRules.profile.resultConstraints.copy(
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
        val generatedPuzzles = (1..40).map { seed ->
            EightPairsMediumDifficultyPuzzleGenerator(seed = seed).generateWithSolution()
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

        assertTrue(containsOne)
        assertTrue(containsRepeatedValue)
        assertTrue(containsPrimeProductDecoy)
        assertTrue(containsHiddenHighValueTarget)
    }
}

private fun assertInitialPuzzleStructure(puzzle: Puzzle) {
    val knownEntryIds = puzzle.knownEntryIds()

    assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
    assertEquals(EightPairsMediumDifficultyRules.BOARD_TILE_COUNT, puzzle.board.tiles.size)
    assertEquals(EightPairsMediumDifficultyRules.STRIP_ENTRY_COUNT, puzzle.strip.entries.size)
    assertTrue(puzzle.board.tiles.all(Tile::hasHiddenExpression))
    assertTrue(knownEntryIds.size in EightPairsMediumDifficultyRules.knownStripEntryCountRange)
    assertTrue(
        puzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden } in
            EightPairsMediumDifficultyRules.hiddenStripEntryCountRange
    )
    assertTrue(
        knownEntryIds.maxConsecutiveHiddenEntries(
            totalEntryCount = EightPairsMediumDifficultyRules.STRIP_ENTRY_COUNT
        ) <= EightPairsMediumDifficultyRules.MAX_CONSECUTIVE_HIDDEN_ENTRIES
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
            leftOperand.value == 1 && rightOperand.value.isPrime() ||
                rightOperand.value == 1 && leftOperand.value.isPrime()
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
