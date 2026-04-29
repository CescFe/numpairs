package org.cescfe.numpairs.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.math.abs
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenLargeOperandsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun largeOperandRemainsLegibleAndKeepStableTapTargetsAtThreeColumnWidth() {
        var leftOperandClicks = 0
        var operatorClicks = 0
        var rightOperandClicks = 0

        composeTestRule.setContent {
            NumPairsTheme {
                Box(
                    modifier = Modifier
                        .width(392.dp)
                        .height(800.dp)
                ) {
                    GameScreen(
                        uiState = largeOperandBoardUiState(),
                        onTileLeftOperandTapped = { leftOperandClicks += 1 },
                        onTileOperatorTapped = { operatorClicks += 1 },
                        onTileRightOperandTapped = { rightOperandClicks += 1 }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("1")))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("×")))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText("222")))
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, leftOperandClicks)
            assertEquals(1, operatorClicks)
            assertEquals(1, rightOperandClicks)
        }

        val leftOperandBounds = composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val operatorBounds = composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val rightOperandBounds = composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot
        val firstTileBounds = composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(0))
            .fetchSemanticsNode()
            .boundsInRoot
        val secondTileBounds = composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(1))
            .fetchSemanticsNode()
            .boundsInRoot

        val minimumTapHeightPx = with(composeTestRule.density) { 40.dp.toPx() }
        val minimumOperatorWidthPx = with(composeTestRule.density) { 28.dp.toPx() }

        assertTrue(leftOperandBounds.right <= operatorBounds.left)
        assertTrue(operatorBounds.right <= rightOperandBounds.left)
        assertTrue(leftOperandBounds.height >= minimumTapHeightPx)
        assertTrue(operatorBounds.height >= minimumTapHeightPx)
        assertTrue(rightOperandBounds.height >= minimumTapHeightPx)
        assertTrue(operatorBounds.width >= minimumOperatorWidthPx)
        assertTrue(abs(firstTileBounds.top - secondTileBounds.top) < 1f)
        assertTrue(abs(firstTileBounds.height - secondTileBounds.height) < 1f)
    }
}

private fun largeOperandBoardUiState(): GameUiState = GameUiState(
    stripItems = List(8) { index ->
        StripItemUiState(
            label = (index + 1).toString(),
            isEntryEnabled = false,
            visualStyle = StripItemVisualStyle.KNOWN
        )
    },
    tiles = listOf(
        TileUiState(
            leftOperandLabel = "1",
            operatorLabel = "×",
            rightOperandLabel = "222",
            resultLabel = "222"
        ),
        TileUiState(
            leftOperandLabel = "8",
            operatorLabel = "+",
            rightOperandLabel = "9",
            resultLabel = "17"
        ),
        TileUiState(
            leftOperandLabel = "7",
            operatorLabel = "×",
            rightOperandLabel = "6",
            resultLabel = "42"
        ),
        TileUiState(
            leftOperandLabel = "12",
            operatorLabel = "+",
            rightOperandLabel = "5",
            resultLabel = "17"
        ),
        TileUiState(
            leftOperandLabel = "3",
            operatorLabel = "×",
            rightOperandLabel = "4",
            resultLabel = "12"
        ),
        TileUiState(
            leftOperandLabel = "16",
            operatorLabel = "+",
            rightOperandLabel = "2",
            resultLabel = "18"
        ),
        TileUiState(
            leftOperandLabel = "10",
            operatorLabel = "×",
            rightOperandLabel = "2",
            resultLabel = "20"
        ),
        TileUiState(
            leftOperandLabel = "9",
            operatorLabel = "+",
            rightOperandLabel = "1",
            resultLabel = "10"
        )
    )
)
