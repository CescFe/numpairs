package org.cescfe.numpairs.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
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

private const val REFERENCE_DOUBLE_DIGIT_TAG = "reference_double_digit_operand"
private const val REFERENCE_TRIPLE_DIGIT_TAG = "reference_triple_digit_operand"

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
                Row {
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
                    ReferenceOperandSamples()
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

        val visibleTripleDigitInkWidth = composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .captureToImage()
            .inkWidthPx()
        val referenceDoubleDigitInkWidth = composeTestRule
            .onNodeWithTag(REFERENCE_DOUBLE_DIGIT_TAG, useUnmergedTree = true)
            .captureToImage()
            .inkWidthPx()
        val referenceTripleDigitInkWidth = composeTestRule
            .onNodeWithTag(REFERENCE_TRIPLE_DIGIT_TAG, useUnmergedTree = true)
            .captureToImage()
            .inkWidthPx()

        assertTrue(
            "Rendered operand looks closer to '22' than '222' " +
                "(actual=$visibleTripleDigitInkWidth, ref22=$referenceDoubleDigitInkWidth, ref222=$referenceTripleDigitInkWidth)",
            abs(visibleTripleDigitInkWidth - referenceTripleDigitInkWidth) <
                abs(visibleTripleDigitInkWidth - referenceDoubleDigitInkWidth)
        )

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

@Composable
private fun ReferenceOperandSamples() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReferenceOperandSample(
                text = "22",
                testTag = REFERENCE_DOUBLE_DIGIT_TAG
            )
            ReferenceOperandSample(
                text = "222",
                testTag = REFERENCE_TRIPLE_DIGIT_TAG
            )
        }
    }
}

@Composable
private fun ReferenceOperandSample(text: String, testTag: String) {
    Surface(
        modifier = Modifier.testTag(testTag),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(40.dp)
                .padding(horizontal = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

private fun ImageBitmap.inkWidthPx(): Int {
    val pixels = toPixelMap()
    val backgroundColor = pixels[0, 0]
    var minimumX = pixels.width
    var maximumX = -1

    for (y in 0 until pixels.height) {
        for (x in 0 until pixels.width) {
            if (pixels[x, y].isInkAgainst(backgroundColor)) {
                minimumX = minOf(minimumX, x)
                maximumX = maxOf(maximumX, x)
            }
        }
    }

    return if (maximumX < minimumX) {
        0
    } else {
        maximumX - minimumX + 1
    }
}

private fun Color.isInkAgainst(backgroundColor: Color): Boolean {
    val channelDistance = abs(red - backgroundColor.red) +
        abs(green - backgroundColor.green) +
        abs(blue - backgroundColor.blue) +
        abs(alpha - backgroundColor.alpha)

    return channelDistance > 0.20f
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
