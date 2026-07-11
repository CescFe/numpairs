package org.cescfe.numpairs.domain.generated

import org.cescfe.numpairs.domain.puzzle.assignment.IndexedResolvedTileAssignment
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.assignment.UnorderedStripEntryPair
import org.cescfe.numpairs.domain.puzzle.assignment.resolvedTileAssignments
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
            listOf("4-pairs-low", "8-pairs-medium"),
            GeneratedPuzzleProfiles.ALL.map { profile -> profile.id.value }
        )

        GeneratedPuzzleProfiles.ALL.forEach { profile ->
            CONTRACT_SEEDS.forEach { seed ->
                val generatedPuzzle = GeneratedPairsPuzzleGenerator(
                    profile = profile,
                    seed = seed
                ).generateWithSolution()

                assertContract(
                    generatedPuzzle = generatedPuzzle,
                    profile = profile,
                    seed = seed
                )
                assertEquals(
                    "${profile.id.value} must be deterministic for seed $seed.",
                    generatedPuzzle,
                    GeneratedPairsPuzzleGenerator(profile = profile, seed = seed).generateWithSolution()
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
        val assignments = solvedPuzzle.resolvedTileAssignments()
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
        assertUsageOncePerOperator(
            assignments = assignments,
            stripEntryIds = solvedEntriesById.keys,
            context = context
        )

        val additionPairs = assignments.pairKeysFor(operator = Operator.ADDITION)
        val multiplicationPairs = assignments.pairKeysFor(operator = Operator.MULTIPLICATION)
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

        if (profile.initialStripMaskPolicy.distributionPolicy ==
            StripKnownEntryDistributionPolicy.SPREAD_ACROSS_STRIP_AND_PAIRS_WHEN_POSSIBLE
        ) {
            val additionPairByEntryId = assignments
                .filter { assignment -> assignment.operator == Operator.ADDITION }
                .flatMap { assignment ->
                    val pairKey = assignment.pairKey
                    listOf(
                        assignment.leftOperand.stripEntryId to pairKey,
                        assignment.rightOperand.stripEntryId to pairKey
                    )
                }.toMap()
            assertEquals(
                context,
                knownEntryIds.size,
                knownEntryIds.map { entryId -> additionPairByEntryId.getValue(StripEntryId(entryId)) }.toSet().size
            )
        }

        assertEquals(context, PuzzleCompletionState.INCOMPLETE, initialPuzzle.completionState)
    }

    private fun assertUsageOncePerOperator(
        assignments: List<IndexedResolvedTileAssignment>,
        stripEntryIds: Set<StripEntryId>,
        context: String
    ) {
        listOf(Operator.ADDITION, Operator.MULTIPLICATION).forEach { operator ->
            val usageCounts = assignments
                .filter { assignment -> assignment.operator == operator }
                .flatMap { assignment ->
                    listOf(assignment.leftOperand.stripEntryId, assignment.rightOperand.stripEntryId)
                }.groupingBy { entryId -> entryId }
                .eachCount()

            stripEntryIds.forEach { entryId ->
                assertEquals(context, 1, usageCounts[entryId])
            }
        }
    }

    private companion object {
        val CONTRACT_SEEDS = listOf(1, 2, 7, 42, 99, 2026)
    }
}

private fun List<IndexedResolvedTileAssignment>.pairKeysFor(operator: Operator): Set<UnorderedStripEntryPair> =
    filter { assignment -> assignment.operator == operator }
        .map(IndexedResolvedTileAssignment::pairKey)
        .toSet()

private val IndexedResolvedTileAssignment.pairKey: UnorderedStripEntryPair
    get() = UnorderedStripEntryPair.of(
        firstEntryId = leftOperand.stripEntryId,
        secondEntryId = rightOperand.stripEntryId
    )

private fun GeneratedPuzzleProfile.requiredKnownEntryIdsForContract(): Set<Int> =
    initialStripMaskPolicy.requiredAnchors.map { anchor ->
        when (anchor) {
            RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY -> size.stripEntryCount - 1
        }
    }.toSet()
