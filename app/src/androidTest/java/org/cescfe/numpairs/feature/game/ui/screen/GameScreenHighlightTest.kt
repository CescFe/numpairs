package org.cescfe.numpairs.feature.game.ui.screen

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.feature.game.GameHighlightState
import org.cescfe.numpairs.feature.game.GameTileExpressionSlot
import org.cescfe.numpairs.feature.game.GameTileExpressionSlotHighlight
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenHighlightTest : GameScreenTestHost() {
    @Test
    fun puzzleElementsAreNotHighlightedByDefault() {
        screen
            .assertStripNotHighlighted()
            .assertStripItemNotHighlighted(index = 1)
            .scrollToBoard()
            .assertTileNotHighlighted(index = 0)
            .assertLeftOperandNotHighlighted(tileIndex = 0)
            .assertOperatorNotHighlighted(tileIndex = 0)
            .assertRightOperandNotHighlighted(tileIndex = 0)
    }

    @Test
    fun rendersStaticHighlightsForStripSurfaceEntriesTilesAndExpressionSlots() {
        showHighlightState(
            GameHighlightState(
                isStripHighlighted = true,
                stripEntryIndexes = setOf(1),
                tileIndexes = setOf(0),
                tileExpressionSlots = setOf(
                    GameTileExpressionSlotHighlight(
                        tileIndex = 0,
                        slot = GameTileExpressionSlot.LEFT_OPERAND
                    ),
                    GameTileExpressionSlotHighlight(
                        tileIndex = 0,
                        slot = GameTileExpressionSlot.OPERATOR
                    ),
                    GameTileExpressionSlotHighlight(
                        tileIndex = 0,
                        slot = GameTileExpressionSlot.RIGHT_OPERAND
                    )
                )
            )
        )

        screen
            .assertStripHighlighted()
            .assertStripItemHighlighted(index = 1)
            .assertStripItemNotHighlighted(index = 2)
            .scrollToBoard()
            .assertTileHighlighted(index = 0)
            .assertTileNotHighlighted(index = 1)
            .assertLeftOperandHighlighted(tileIndex = 0)
            .assertOperatorHighlighted(tileIndex = 0)
            .assertRightOperandHighlighted(tileIndex = 0)
            .assertLeftOperandNotHighlighted(tileIndex = 1)
            .assertOperatorNotHighlighted(tileIndex = 1)
            .assertRightOperandNotHighlighted(tileIndex = 1)
    }
}
