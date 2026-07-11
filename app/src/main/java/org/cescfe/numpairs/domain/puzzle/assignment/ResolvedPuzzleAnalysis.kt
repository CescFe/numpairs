package org.cescfe.numpairs.domain.puzzle.assignment

import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.StripItem

internal class ResolvedPuzzleAnalysis internal constructor(
    val stripEntryIds: Set<StripEntryId>,
    val knownStripValuesByEntryId: Map<StripEntryId, Int>,
    val operandReferences: List<IndexedOperandReference>,
    val resolvedAssignments: List<IndexedResolvedTileAssignment>,
    val unresolvedAssignments: List<IndexedUnresolvedTileAssignment>,
    val unknownEntryIdsByTileIndex: Map<Int, Set<StripEntryId>>,
    val operandValueMismatchTileIndexes: Set<Int>,
    private val usageCountsByOperator: Map<Operator, Map<StripEntryId, Int>>,
    private val solutionPairsByOperator: Map<Operator, Set<UnorderedStripEntryPair>>,
    val solutionPairByTileIndex: Map<Int, UnorderedStripEntryPair>
) {
    val referencedEntryIds: Set<StripEntryId> = operandReferences
        .mapNotNullTo(mutableSetOf()) { reference -> reference.operand.stripEntryId }

    val referencedValuesByEntryId: Map<StripEntryId, Set<Int>> = operandReferences
        .mapNotNull { reference ->
            reference.operand.stripEntryId?.let { entryId -> entryId to reference.operand.value }
        }.groupBy(
            keySelector = { (entryId, _) -> entryId },
            valueTransform = { (_, value) -> value }
        ).mapValues { (_, values) -> values.toSet() }

    val mismatchedSolutionPairs: Set<UnorderedStripEntryPair> = run {
        val additionPairs = solutionPairsFor(operator = Operator.ADDITION)
        val multiplicationPairs = solutionPairsFor(operator = Operator.MULTIPLICATION)
        (additionPairs - multiplicationPairs) + (multiplicationPairs - additionPairs)
    }

    fun usageCountsFor(operator: Operator): Map<StripEntryId, Int> = usageCountsByOperator[operator].orEmpty()

    fun solutionPairsFor(operator: Operator): Set<UnorderedStripEntryPair> = solutionPairsByOperator[operator].orEmpty()
}

internal data class OperandReference(val stripEntryId: StripEntryId?, val value: Int)

internal data class IndexedOperandReference(val tileIndex: Int, val slot: OperandSlot, val operand: OperandReference)

internal data class IndexedUnresolvedTileAssignment(
    val tileIndex: Int,
    val leftOperand: OperandReference?,
    val operator: Operator?,
    val rightOperand: OperandReference?
)

internal fun Puzzle.analyzeResolvedPuzzle(): ResolvedPuzzleAnalysis {
    val stripEntryIds = strip.entries.mapTo(linkedSetOf()) { entry -> StripEntryId(entry.id) }
    val knownStripValuesByEntryId = strip.entries.mapNotNull { entry ->
        (entry.item as? StripItem.Known)?.let { item -> StripEntryId(entry.id) to item.value }
    }.toMap()
    val operandReferences = board.tiles.flatMapIndexed { tileIndex, tile ->
        buildList {
            tile.expression.leftOperand.toReference()?.let { operand ->
                add(IndexedOperandReference(tileIndex = tileIndex, slot = OperandSlot.LEFT, operand = operand))
            }
            tile.expression.rightOperand.toReference()?.let { operand ->
                add(IndexedOperandReference(tileIndex = tileIndex, slot = OperandSlot.RIGHT, operand = operand))
            }
        }
    }
    val resolvedAssignments = mutableListOf<IndexedResolvedTileAssignment>()
    val unresolvedAssignments = mutableListOf<IndexedUnresolvedTileAssignment>()

    board.tiles.forEachIndexed { tileIndex, tile ->
        val resolvedAssignment = tile.resolvedStripEntryAssignment()
        if (resolvedAssignment == null) {
            unresolvedAssignments += IndexedUnresolvedTileAssignment(
                tileIndex = tileIndex,
                leftOperand = tile.expression.leftOperand.toReference(),
                operator = tile.expression.operator.takeUnless { operator -> operator == Operator.Hidden },
                rightOperand = tile.expression.rightOperand.toReference()
            )
        } else {
            resolvedAssignments += IndexedResolvedTileAssignment(
                tileIndex = tileIndex,
                assignment = resolvedAssignment
            )
        }
    }

    val unknownEntryIdsByTileIndex = operandReferences
        .mapNotNull { reference ->
            reference.operand.stripEntryId
                ?.takeUnless { entryId -> entryId in stripEntryIds }
                ?.let { entryId -> reference.tileIndex to entryId }
        }.groupBy(
            keySelector = { (tileIndex, _) -> tileIndex },
            valueTransform = { (_, entryId) -> entryId }
        ).mapValues { (_, entryIds) -> entryIds.toSet() }
    val operandValueMismatchTileIndexes = operandReferences.mapNotNullTo(linkedSetOf()) { reference ->
        val entryId = reference.operand.stripEntryId ?: return@mapNotNullTo null
        reference.tileIndex.takeUnless {
            knownStripValuesByEntryId[entryId] == reference.operand.value
        }
    }
    val usageCountsByOperator = CONCRETE_OPERATORS.associateWith { operator ->
        resolvedAssignments
            .filter { assignment -> assignment.operator == operator }
            .flatMap { assignment ->
                listOf(assignment.leftOperand.stripEntryId, assignment.rightOperand.stripEntryId)
            }.groupingBy { entryId -> entryId }
            .eachCount()
    }
    val solutionPairByTileIndex = resolvedAssignments.associate { assignment ->
        assignment.tileIndex to assignment.solutionPair
    }
    val solutionPairsByOperator = CONCRETE_OPERATORS.associateWith { operator ->
        resolvedAssignments
            .filter { assignment -> assignment.operator == operator }
            .mapTo(linkedSetOf()) { assignment -> solutionPairByTileIndex.getValue(assignment.tileIndex) }
    }

    return ResolvedPuzzleAnalysis(
        stripEntryIds = stripEntryIds,
        knownStripValuesByEntryId = knownStripValuesByEntryId,
        operandReferences = operandReferences,
        resolvedAssignments = resolvedAssignments,
        unresolvedAssignments = unresolvedAssignments,
        unknownEntryIdsByTileIndex = unknownEntryIdsByTileIndex,
        operandValueMismatchTileIndexes = operandValueMismatchTileIndexes,
        usageCountsByOperator = usageCountsByOperator,
        solutionPairsByOperator = solutionPairsByOperator,
        solutionPairByTileIndex = solutionPairByTileIndex
    )
}

private fun Expression.Operand.toReference(): OperandReference? = when (this) {
    Expression.Operand.Hidden -> null
    is Expression.Operand.Known -> OperandReference(
        stripEntryId = stripEntryId?.let(::StripEntryId),
        value = value
    )
}

private val IndexedResolvedTileAssignment.solutionPair: UnorderedStripEntryPair
    get() = UnorderedStripEntryPair.of(
        firstEntryId = leftOperand.stripEntryId,
        secondEntryId = rightOperand.stripEntryId
    )

private val CONCRETE_OPERATORS = listOf(Operator.ADDITION, Operator.MULTIPLICATION)
