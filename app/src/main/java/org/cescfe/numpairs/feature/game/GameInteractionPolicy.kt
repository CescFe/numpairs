package org.cescfe.numpairs.feature.game

import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.Operator

data class GameInteractionPolicy(
    val canTapStripItem: (Int) -> Boolean = { true },
    val canConfirmStripItemEntry: (Int, Int) -> Boolean = { _, _ -> true },
    val canTapTileLeftOperand: (Int) -> Boolean = { true },
    val canTapTileRightOperand: (Int) -> Boolean = { true },
    val canTapTileOperator: (Int) -> Boolean = { true },
    val canTapTileReset: (Int) -> Boolean = { true },
    val canConfirmTileOperand: (Int, OperandSlot, Int) -> Boolean = { _, _, _ -> true },
    val canConfirmTileOperator: (Int, Operator) -> Boolean = { _, _ -> true }
) {
    companion object {
        val AllowAll = GameInteractionPolicy()
    }
}
