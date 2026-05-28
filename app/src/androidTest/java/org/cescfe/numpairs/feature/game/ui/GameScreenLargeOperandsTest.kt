package org.cescfe.numpairs.feature.game.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.math.abs
import org.cescfe.numpairs.R
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
                Box(
                    modifier = Modifier
                        .width(392.dp)
                        .height(800.dp)
                ) {
                    GameScreen(
                        title = "Large operands",
                        uiState = largeOperandBoardUiState(),
                        onTileLeftOperandTapped = { leftOperandClicks += 1 },
                        onTileOperatorTapped = { operatorClicks += 1 },
                        onTileRightOperandTapped = { rightOperandClicks += 1 }
                    )
                    ReferenceOperandSamples(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    )
                }
            }
        }

        val screen = GameScreenRobot(
            activity = composeTestRule.activity,
            interactions = composeTestRule
        )

        screen
            .assertBoardDisplayed()
            .assertLeftOperandDescription(0, R.string.tile_left_operand_content_description, "1")
            .tapTileLeftOperand(0)
            .assertOperatorDescription(
                0,
                R.string.tile_operator_content_description,
                composeTestRule.activity.getString(R.string.tile_operator_option_multiplication)
            )
            .tapTileOperator(0)
            .assertRightOperandDescription(0, R.string.tile_right_operand_content_description, "222")
            .tapTileRightOperand(0)

        composeTestRule.runOnIdle {
            assertEquals(1, leftOperandClicks)
            assertEquals(1, operatorClicks)
            assertEquals(1, rightOperandClicks)
        }

        val referenceDoubleDigitInkWidth = captureReferenceInkWidth(REFERENCE_DOUBLE_DIGIT_TAG)
        val referenceTripleDigitInkWidth = captureReferenceInkWidth(REFERENCE_TRIPLE_DIGIT_TAG)
        val visibleTripleDigitInkWidth = composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
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

private fun GameScreenLargeOperandsTest.captureReferenceInkWidth(testTag: String): Int {
    composeTestRule
        .onNodeWithTag(testTag, useUnmergedTree = true)
        .assertIsDisplayed()

    return composeTestRule
        .onNodeWithTag(testTag, useUnmergedTree = true)
        .captureToImage()
        .inkWidthPx()
}

@Composable
private fun ReferenceOperandSamples(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface
    ) {
        Box {
            ReferenceOperandSample(
                text = "22",
                testTag = REFERENCE_DOUBLE_DIGIT_TAG
            )
            ReferenceOperandSample(
                text = "222",
                testTag = REFERENCE_TRIPLE_DIGIT_TAG,
                modifier = Modifier.padding(top = 44.dp)
            )
        }
    }
}

@Composable
private fun ReferenceOperandSample(text: String, testTag: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.testTag(testTag),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                modifier = Modifier.width(68.dp),
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
