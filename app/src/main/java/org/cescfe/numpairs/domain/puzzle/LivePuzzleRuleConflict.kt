package org.cescfe.numpairs.domain.puzzle

enum class LivePuzzleRuleConflict {
    DUPLICATE_OPERATOR_USAGE,
    MISMATCHED_PAIRING
}

val Puzzle.liveRuleConflictsByTile: Map<Int, Set<LivePuzzleRuleConflict>>
    get() = board.liveRuleConflictsByTile()

fun Puzzle.liveRuleConflictsForCandidate(
    tileIndex: Int,
    slot: OperandSlot,
    stripEntryId: Int,
    operator: Operator
): Set<LivePuzzleRuleConflict> {
    if (
        operator == Operator.Hidden ||
        tileIndex !in board.tiles.indices ||
        stripEntryId !in visibleStripEntryIds
    ) {
        return emptySet()
    }

    return board.liveRuleConflictsByTile(
        candidate = CandidateTileOperandAssignment(
            tileIndex = tileIndex,
            slot = slot,
            stripEntryId = stripEntryId,
            operator = operator
        )
    )[tileIndex].orEmpty()
}

private fun Board.liveRuleConflictsByTile(
    candidate: CandidateTileOperandAssignment? = null
): Map<Int, Set<LivePuzzleRuleConflict>> {
    val assignments = operandAssignments(candidate = candidate)
    val pairAssignments = pairAssignments(candidate = candidate)
    val conflicts = mutableMapOf<Int, MutableSet<LivePuzzleRuleConflict>>()

    assignments
        .groupBy { assignment -> assignment.stripEntryId to assignment.operator }
        .values
        .filter { sameEntryOperatorAssignments -> sameEntryOperatorAssignments.size > 1 }
        .forEach { sameEntryOperatorAssignments ->
            sameEntryOperatorAssignments.forEach { assignment ->
                conflicts.add(
                    tileIndex = assignment.tileIndex,
                    conflict = LivePuzzleRuleConflict.DUPLICATE_OPERATOR_USAGE
                )
            }
        }

    pairAssignments.mismatchedPairingTileIndexes().forEach { tileIndex ->
        conflicts.add(
            tileIndex = tileIndex,
            conflict = LivePuzzleRuleConflict.MISMATCHED_PAIRING
        )
    }

    return conflicts
}

private data class CandidateTileOperandAssignment(
    val tileIndex: Int,
    val slot: OperandSlot,
    val stripEntryId: Int,
    val operator: Operator
)

private data class LiveOperandAssignment(
    val tileIndex: Int,
    val slot: OperandSlot,
    val stripEntryId: Int,
    val operator: Operator
)

private data class LivePairAssignment(
    val tileIndex: Int,
    val leftStripEntryId: Int,
    val rightStripEntryId: Int,
    val operator: Operator
)

private data class PartnerOnTile(val partnerStripEntryId: Int, val tileIndex: Int)

private fun MutableMap<Int, MutableSet<LivePuzzleRuleConflict>>.add(tileIndex: Int, conflict: LivePuzzleRuleConflict) {
    getOrPut(tileIndex) { mutableSetOf() }.add(conflict)
}

private fun Board.operandAssignments(candidate: CandidateTileOperandAssignment?): List<LiveOperandAssignment> =
    tiles.flatMapIndexed { tileIndex, tile ->
        tile.operandAssignments(
            tileIndex = tileIndex,
            operatorOverride = candidate?.operatorFor(tileIndex)
        )
    }.filterNot { assignment ->
        candidate?.replaces(assignment) == true
    } + listOfNotNull(candidate?.asOperandAssignment())

private fun Tile.operandAssignments(tileIndex: Int, operatorOverride: Operator?): List<LiveOperandAssignment> {
    val resolvedOperator = operatorOverride ?: expression.operator
    if (resolvedOperator == Operator.Hidden) {
        return emptyList()
    }

    return buildList {
        expression.leftOperand.stripEntryId?.let { stripEntryId ->
            add(
                LiveOperandAssignment(
                    tileIndex = tileIndex,
                    slot = OperandSlot.LEFT,
                    stripEntryId = stripEntryId,
                    operator = resolvedOperator
                )
            )
        }
        expression.rightOperand.stripEntryId?.let { stripEntryId ->
            add(
                LiveOperandAssignment(
                    tileIndex = tileIndex,
                    slot = OperandSlot.RIGHT,
                    stripEntryId = stripEntryId,
                    operator = resolvedOperator
                )
            )
        }
    }
}

private fun Board.pairAssignments(candidate: CandidateTileOperandAssignment?): List<LivePairAssignment> =
    tiles.mapIndexedNotNull { tileIndex, tile ->
        tile.pairAssignment(
            tileIndex = tileIndex,
            candidate = candidate
        )
    }

private fun Tile.pairAssignment(tileIndex: Int, candidate: CandidateTileOperandAssignment?): LivePairAssignment? {
    val resolvedOperator = candidate?.operatorFor(tileIndex) ?: expression.operator
    if (resolvedOperator == Operator.Hidden) {
        return null
    }

    val leftStripEntryId = candidate
        ?.takeIf { candidateAssignment ->
            candidateAssignment.tileIndex == tileIndex && candidateAssignment.slot == OperandSlot.LEFT
        }
        ?.stripEntryId
        ?: expression.leftOperand.stripEntryId
    val rightStripEntryId = candidate
        ?.takeIf { candidateAssignment ->
            candidateAssignment.tileIndex == tileIndex && candidateAssignment.slot == OperandSlot.RIGHT
        }
        ?.stripEntryId
        ?: expression.rightOperand.stripEntryId

    return if (leftStripEntryId != null && rightStripEntryId != null) {
        LivePairAssignment(
            tileIndex = tileIndex,
            leftStripEntryId = leftStripEntryId,
            rightStripEntryId = rightStripEntryId,
            operator = resolvedOperator
        )
    } else {
        null
    }
}

private fun List<LivePairAssignment>.mismatchedPairingTileIndexes(): Set<Int> {
    val partnersByEntryIdAndOperator = mutableMapOf<Pair<Int, Operator>, MutableSet<PartnerOnTile>>()

    forEach { assignment ->
        partnersByEntryIdAndOperator.addPartner(
            stripEntryId = assignment.leftStripEntryId,
            operator = assignment.operator,
            partner = PartnerOnTile(
                partnerStripEntryId = assignment.rightStripEntryId,
                tileIndex = assignment.tileIndex
            )
        )
        partnersByEntryIdAndOperator.addPartner(
            stripEntryId = assignment.rightStripEntryId,
            operator = assignment.operator,
            partner = PartnerOnTile(
                partnerStripEntryId = assignment.leftStripEntryId,
                tileIndex = assignment.tileIndex
            )
        )
    }

    return partnersByEntryIdAndOperator.keys
        .map(Pair<Int, Operator>::first)
        .toSet()
        .flatMap { stripEntryId ->
            val additionPartners = partnersByEntryIdAndOperator.getValueOrEmpty(stripEntryId, Operator.ADDITION)
            val multiplicationPartners = partnersByEntryIdAndOperator.getValueOrEmpty(
                stripEntryId = stripEntryId,
                operator = Operator.MULTIPLICATION
            )

            if (
                additionPartners.isNotEmpty() &&
                multiplicationPartners.isNotEmpty() &&
                additionPartners.partnerIds != multiplicationPartners.partnerIds
            ) {
                additionPartners.map(PartnerOnTile::tileIndex) + multiplicationPartners.map(PartnerOnTile::tileIndex)
            } else {
                emptyList()
            }
        }
        .toSet()
}

private fun MutableMap<Pair<Int, Operator>, MutableSet<PartnerOnTile>>.addPartner(
    stripEntryId: Int,
    operator: Operator,
    partner: PartnerOnTile
) {
    getOrPut(stripEntryId to operator) { mutableSetOf() }.add(partner)
}

private fun Map<Pair<Int, Operator>, Set<PartnerOnTile>>.getValueOrEmpty(
    stripEntryId: Int,
    operator: Operator
): Set<PartnerOnTile> = get(stripEntryId to operator).orEmpty()

private val Set<PartnerOnTile>.partnerIds: Set<Int>
    get() = map(PartnerOnTile::partnerStripEntryId).toSet()

private fun CandidateTileOperandAssignment.operatorFor(tileIndex: Int): Operator? =
    operator.takeIf { this.tileIndex == tileIndex }

private fun CandidateTileOperandAssignment.replaces(assignment: LiveOperandAssignment): Boolean =
    tileIndex == assignment.tileIndex && slot == assignment.slot

private fun CandidateTileOperandAssignment.asOperandAssignment(): LiveOperandAssignment = LiveOperandAssignment(
    tileIndex = tileIndex,
    slot = slot,
    stripEntryId = stripEntryId,
    operator = operator
)

private val Puzzle.visibleStripEntryIds: Set<Int>
    get() = strip.entries.mapNotNull { entry ->
        entry.id.takeIf { entry.item.visibleValue != null }
    }.toSet()

private val StripItem.visibleValue: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> value
    }

private val Expression.Operand.stripEntryId: Int?
    get() = when (this) {
        Expression.Operand.Hidden -> null
        is Expression.Operand.Known -> stripEntryId
    }
