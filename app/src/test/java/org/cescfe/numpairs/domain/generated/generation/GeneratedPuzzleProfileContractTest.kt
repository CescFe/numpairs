package org.cescfe.numpairs.domain.generated.generation

import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.profile.RequiredKnownStripAnchor
import org.cescfe.numpairs.domain.generated.profile.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.analyzeResolvedPuzzle
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GeneratedPuzzleProfileContractTest {

    @Test
    fun every_registered_profile_satisfies_the_shared_generation_contract() {
        assertEquals(
            listOf("4-pairs-low", "4-pairs-medium", "8-pairs-medium"),
            GeneratedPuzzleProfiles.ALL.map { profile -> profile.id.value }
        )

        GeneratedPuzzleProfiles.ALL.forEach { profile ->
            CONTRACT_SEEDS.forEach { seed ->
                val generatedPuzzle = generatedPuzzle(profile = profile, seed = seed)

                assertContract(
                    generatedPuzzle = generatedPuzzle,
                    profile = profile,
                    seed = seed
                )
                assertEquals(
                    "${profile.id.value} must be deterministic for seed $seed.",
                    generatedPuzzle,
                    generatedPuzzle(profile = profile, seed = seed)
                )
            }
        }
    }

    private fun assertContract(generatedPuzzle: GeneratedPairsPuzzle, profile: GeneratedPuzzleProfile, seed: Int) {
        val solvedPuzzle = generatedPuzzle.solvedPuzzle
        val initialPuzzle = generatedPuzzle.initialPuzzle
        val solvedEntriesById = solvedPuzzle.strip.entries.associate { entry ->
            StripEntryId(entry.id) to (entry.item as StripItem.Known).value
        }
        val solvedAnalysis = solvedPuzzle.analyzeResolvedPuzzle()
        val assignments = solvedAnalysis.resolvedAssignments
        val context = "${profile.id.value}, seed $seed"

        assertEquals(context, PuzzleCompletionState.SOLVED, solvedPuzzle.completionState)
        assertEquals(context, profile.size.boardTileCount, solvedPuzzle.board.tiles.size)
        assertEquals(context, profile.size.stripEntryCount, solvedPuzzle.strip.entries.size)
        assertEquals(context, profile.size.boardTileCount, initialPuzzle.board.tiles.size)
        assertEquals(context, profile.size.stripEntryCount, initialPuzzle.strip.entries.size)
        assertEquals(
            context,
            solvedPuzzle.strip.entries.map { entry -> entry.id },
            initialPuzzle.strip.entries.map { entry -> entry.id }
        )
        assertEquals(context, solvedPuzzle.strip.entries.size, solvedEntriesById.size)

        initialPuzzle.strip.entries.forEach { entry ->
            when (val item = entry.item) {
                StripItem.Hidden -> Unit
                is StripItem.Known ->
                    assertEquals(context, solvedEntriesById.getValue(StripEntryId(entry.id)), item.value)
                is StripItem.PlayerEntered -> fail("$context must not start with player-entered strip values.")
            }
        }

        val solvedValues = solvedPuzzle.requireKnownStripValues()
        assertEquals(context, solvedValues.sorted(), solvedValues)
        assertTrue(context, solvedValues.all { value -> value in profile.stripValuePolicy.valueRange })
        assertTrue(
            context,
            solvedValues.groupingBy { value -> value }.eachCount().values.all { occurrenceCount ->
                occurrenceCount <= profile.stripValuePolicy.maxOccurrencesPerValue
            }
        )
        profile.stripValuePolicy.maxRepeatedValueGroupCount?.let { maximumGroupCount ->
            assertTrue(
                context,
                solvedValues.groupingBy { value -> value }.eachCount().values
                    .count { occurrenceCount -> occurrenceCount > 1 } <= maximumGroupCount
            )
        }

        assertEquals(context, profile.size.boardTileCount, assignments.size)
        assignments.forEach { assignment ->
            assertEquals(
                context,
                solvedEntriesById.getValue(assignment.leftOperand.stripEntryId),
                assignment.leftOperand.value
            )
            assertEquals(
                context,
                solvedEntriesById.getValue(assignment.rightOperand.stripEntryId),
                assignment.rightOperand.value
            )
        }
        listOf(Operator.ADDITION, Operator.MULTIPLICATION).forEach { operator ->
            solvedEntriesById.keys.forEach { entryId ->
                assertEquals(context, 1, solvedAnalysis.usageCountsFor(operator)[entryId])
            }
        }

        val additionPairs = solvedAnalysis.solutionPairsFor(operator = Operator.ADDITION)
        val multiplicationPairs = solvedAnalysis.solutionPairsFor(operator = Operator.MULTIPLICATION)
        assertEquals(context, profile.size.pairCount, additionPairs.size)
        assertEquals(context, additionPairs, multiplicationPairs)

        val multiplicationResults = solvedPuzzle.multiplicationTiles().map(Tile::result)
        assertTrue(
            context,
            multiplicationResults.all { result -> result <= profile.resultConstraints.maxMultiplicationResult }
        )
        val solvedResults = solvedPuzzle.board.tiles.map(Tile::result)
        if (!profile.resultConstraints.allowsDuplicateBoardResults) {
            assertEquals(context, solvedResults.size, solvedResults.toSet().size)
        }
        profile.resultConstraints.productAnchorMix?.let { anchorMix ->
            val anchorCount = multiplicationResults.count { result ->
                result > anchorMix.productResultGreaterThan
            }
            assertTrue(context, anchorCount in anchorMix.countRange)
        }

        assertTrue(context, initialPuzzle.board.tiles.all(Tile::hasHiddenExpression))
        assertEquals(context, solvedResults, initialPuzzle.board.tiles.map(Tile::result))
        val knownEntryIds = initialPuzzle.knownEntryIds()
        assertTrue(context, knownEntryIds.size in profile.initialStripMaskPolicy.knownEntryCountRange)
        assertTrue(
            context,
            initialPuzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden } in
                profile.hiddenEntryCountRange
        )
        assertTrue(context, knownEntryIds.containsAll(profile.requiredKnownEntryIdsForContract()))
        assertTrue(
            context,
            knownEntryIds.maxConsecutiveHiddenEntries(profile.size.stripEntryCount) <=
                profile.initialStripMaskPolicy.maxConsecutiveHiddenEntries
        )

        val distributionPolicy = profile.initialStripMaskPolicy.distributionPolicy
        if (distributionPolicy != StripKnownEntryDistributionPolicy.Unrestricted) {
            val additionPairByEntryId = assignments
                .filter { assignment -> assignment.operator == Operator.ADDITION }
                .flatMap { assignment ->
                    val solutionPair = solvedAnalysis.solutionPairByTileIndex.getValue(assignment.tileIndex)
                    listOf(
                        assignment.leftOperand.stripEntryId to solutionPair,
                        assignment.rightOperand.stripEntryId to solutionPair
                    )
                }.toMap()
            val knownSolutionPairCount = knownEntryIds
                .map { entryId -> additionPairByEntryId.getValue(StripEntryId(entryId)) }
                .toSet()
                .size
            when (distributionPolicy) {
                StripKnownEntryDistributionPolicy.SpreadAcrossStripAndPairsWhenPossible ->
                    assertEquals(context, knownEntryIds.size, knownSolutionPairCount)
                is StripKnownEntryDistributionPolicy.AtLeastDistinctSolutionPairs ->
                    assertTrue(context, knownSolutionPairCount >= distributionPolicy.minimumPairCount)
                StripKnownEntryDistributionPolicy.Unrestricted -> error("Handled before pair-distribution analysis.")
            }
        }

        assertEquals(context, PuzzleCompletionState.INCOMPLETE, initialPuzzle.completionState)
    }

    private companion object {
        val CONTRACT_SEEDS = listOf(1, 2, 7, 42, 99, 2026)
    }
}

private fun GeneratedPuzzleProfile.requiredKnownEntryIdsForContract(): Set<Int> =
    initialStripMaskPolicy.requiredAnchors.map { anchor ->
        when (anchor) {
            RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY -> size.stripEntryCount - 1
        }
    }.toSet()
