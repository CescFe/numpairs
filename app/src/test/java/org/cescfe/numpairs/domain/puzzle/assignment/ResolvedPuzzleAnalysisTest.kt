package org.cescfe.numpairs.domain.puzzle.assignment

import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.support.TileAssignment
import org.cescfe.numpairs.domain.puzzle.support.assignedTile
import org.cescfe.numpairs.domain.puzzle.support.defaultKnownStripValues
import org.cescfe.numpairs.domain.puzzle.support.knownPuzzleWithAssignments
import org.cescfe.numpairs.domain.puzzle.support.tileWithoutStripIdentity
import org.cescfe.numpairs.domain.puzzle.support.withTile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolvedPuzzleAnalysisTest {
    @Test
    fun complete_assignments_expose_references_usage_and_canonical_solution_pairs() {
        val analysis = matchingPuzzle().analyzeResolvedPuzzle()

        assertEquals(4, analysis.resolvedAssignments.size)
        assertTrue(analysis.unresolvedAssignments.isEmpty())
        assertEquals((0..3).map(::StripEntryId).toSet(), analysis.referencedEntryIds)
        assertEquals(setOf(1), analysis.referencedValuesByEntryId.getValue(StripEntryId(0)))
        val expectedUsageCounts = mapOf(
            StripEntryId(0) to 1,
            StripEntryId(1) to 1,
            StripEntryId(2) to 1,
            StripEntryId(3) to 1
        )
        assertEquals(expectedUsageCounts, analysis.usageCountsFor(operator = Operator.ADDITION))
        assertEquals(expectedUsageCounts, analysis.usageCountsFor(operator = Operator.MULTIPLICATION))
        assertEquals(
            analysis.solutionPairsFor(operator = Operator.ADDITION),
            analysis.solutionPairsFor(operator = Operator.MULTIPLICATION)
        )
    }

    @Test
    fun missing_and_unknown_identities_are_represented_without_throwing() {
        val puzzle = matchingPuzzle()
            .withTile(
                index = 0,
                tile = tileWithoutStripIdentity(
                    leftOperand = 1,
                    operator = Operator.ADDITION,
                    rightOperand = 2
                )
            ).withTile(
                index = 1,
                tile = assignedTile(
                    leftEntryId = 99,
                    leftValue = 1,
                    operator = Operator.MULTIPLICATION,
                    rightEntryId = 0,
                    rightValue = 1
                )
            )

        val analysis = puzzle.analyzeResolvedPuzzle()

        assertEquals(listOf(0), analysis.unresolvedAssignments.map(IndexedUnresolvedTileAssignment::tileIndex))
        assertEquals(null, analysis.unresolvedAssignments.single().leftOperand?.stripEntryId)
        assertEquals(
            mapOf(1 to setOf(StripEntryId(99))),
            analysis.unknownEntryIdsByTileIndex
        )
        assertEquals(setOf(1), analysis.referencedValuesByEntryId.getValue(StripEntryId(99)))
    }

    @Test
    fun operand_values_are_compared_with_the_authoritative_strip_entry() {
        val puzzle = matchingPuzzle().withTile(
            index = 0,
            tile = assignedTile(
                leftEntryId = 0,
                leftValue = 99,
                operator = Operator.ADDITION,
                rightEntryId = 1,
                rightValue = 2
            )
        )

        val analysis = puzzle.analyzeResolvedPuzzle()

        assertEquals(setOf(0), analysis.operandValueMismatchTileIndexes)
        assertTrue(analysis.unknownEntryIdsByTileIndex.isEmpty())
    }

    @Test
    fun under_use_over_use_and_mismatched_pairs_are_derived_from_the_same_assignments() {
        val puzzle = knownPuzzleWithAssignments(
            stripValues = defaultKnownStripValues(pairCount = 2),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 0, operator = Operator.MULTIPLICATION, rightEntryId = 2),
            TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
            TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 3)
        )

        val analysis = puzzle.analyzeResolvedPuzzle()

        assertEquals(2, analysis.usageCountsFor(Operator.ADDITION).getValue(StripEntryId(0)))
        assertEquals(null, analysis.usageCountsFor(Operator.ADDITION)[StripEntryId(2)])
        assertTrue(
            analysis.solutionPairsFor(Operator.ADDITION) !=
                analysis.solutionPairsFor(Operator.MULTIPLICATION)
        )
        assertTrue(analysis.mismatchedSolutionPairs.isNotEmpty())
    }
}

private fun matchingPuzzle() = knownPuzzleWithAssignments(
    stripValues = defaultKnownStripValues(pairCount = 2),
    TileAssignment(leftEntryId = 0, operator = Operator.ADDITION, rightEntryId = 1),
    TileAssignment(leftEntryId = 1, operator = Operator.MULTIPLICATION, rightEntryId = 0),
    TileAssignment(leftEntryId = 2, operator = Operator.ADDITION, rightEntryId = 3),
    TileAssignment(leftEntryId = 3, operator = Operator.MULTIPLICATION, rightEntryId = 2)
)
