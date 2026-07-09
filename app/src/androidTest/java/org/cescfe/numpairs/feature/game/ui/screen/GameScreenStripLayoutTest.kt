package org.cescfe.numpairs.feature.game.ui.screen

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.math.roundToInt
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemVisualStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenStripLayoutTest : GameScreenTestHost() {

    @Test
    fun sixteen_strip_entries_are_rendered_in_uniform_non_overlapping_rows() {
        showUiStateFixture(
            GameUiState(
                stripItems = List(16) { index ->
                    StripItemUiState(
                        label = (index + 10).toString(),
                        isEntryEnabled = true,
                        visualStyle = StripItemVisualStyle.HIDDEN
                    )
                },
                tiles = emptyList()
            )
        )

        val entryBounds = (0 until 16).map { index ->
            composeTestRule
                .onNodeWithTag(GameScreenTestTags.stripItem(index))
                .fetchSemanticsNode()
                .boundsInRoot
        }
        val rows = entryBounds.groupBy { bounds -> bounds.top.roundToInt() }.values

        assertEquals(16, entryBounds.size)
        assertTrue(rows.size >= 2)
        assertUniformWidths(entryBounds)
        rows.forEach(::assertEntriesDoNotOverlap)
    }

    private fun assertUniformWidths(entryBounds: List<Rect>) {
        val expectedWidth = entryBounds.first().width

        entryBounds.forEach { bounds ->
            assertEquals(expectedWidth, bounds.width, WIDTH_TOLERANCE_PX)
        }
    }

    private fun assertEntriesDoNotOverlap(rowBounds: List<Rect>) {
        rowBounds
            .sortedBy(Rect::left)
            .zipWithNext()
            .forEach { (left, right) ->
                assertTrue(left.right <= right.left)
            }
    }

    private companion object {
        const val WIDTH_TOLERANCE_PX = 0.1f
    }
}
