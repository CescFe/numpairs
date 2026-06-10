package org.cescfe.numpairs.feature.game

data class GameHighlightState(
    val stripEntryIndexes: Set<Int> = emptySet(),
    val tileIndexes: Set<Int> = emptySet(),
    val tileExpressionSlots: Set<GameTileExpressionSlotHighlight> = emptySet()
) {
    init {
        require(stripEntryIndexes.all { index -> index >= 0 }) {
            "Highlighted strip entry indexes must be non-negative."
        }
        require(tileIndexes.all { index -> index >= 0 }) {
            "Highlighted tile indexes must be non-negative."
        }
    }

    fun isStripEntryHighlighted(index: Int): Boolean = index in stripEntryIndexes

    fun isTileHighlighted(index: Int): Boolean = index in tileIndexes

    fun isTileExpressionSlotHighlighted(tileIndex: Int, slot: GameTileExpressionSlot): Boolean =
        GameTileExpressionSlotHighlight(tileIndex = tileIndex, slot = slot) in tileExpressionSlots

    companion object {
        val None = GameHighlightState()
    }
}

data class GameTileExpressionSlotHighlight(val tileIndex: Int, val slot: GameTileExpressionSlot) {
    init {
        require(tileIndex >= 0) {
            "Highlighted tile expression slot index must be non-negative."
        }
    }
}

enum class GameTileExpressionSlot {
    LEFT_OPERAND,
    OPERATOR,
    RIGHT_OPERAND
}
