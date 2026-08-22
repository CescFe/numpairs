package org.cescfe.numpairs.feature.game.ui.components.tile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.game.presentation.TileUiState
import org.cescfe.numpairs.feature.game.ui.semantics.gameHighlightSemantics
import org.cescfe.numpairs.feature.game.ui.semantics.tileInputActive
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTextStyles
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors

@Composable
internal fun TileResetAction(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val resetContentDescription = stringResource(R.string.tile_reset_content_description)

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = NumPairsComponents.subtleSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.primary,
        border = NumPairsComponents.subtleBorder()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    stateDescription = resetContentDescription
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_replay),
                contentDescription = resetContentDescription,
                modifier = Modifier.size(TILE_RESET_ACTION_ICON_SIZE),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
internal fun TileExpressionRow(
    tile: TileUiState,
    modifier: Modifier = Modifier,
    leftOperandModifier: Modifier = Modifier,
    isLeftOperandHighlighted: Boolean = false,
    isLeftOperandInputActive: Boolean = false,
    leftOperandContentDescription: String? = null,
    onLeftOperandClick: (() -> Unit)? = null,
    operatorModifier: Modifier = Modifier,
    isOperatorHighlighted: Boolean = false,
    isOperatorInputActive: Boolean = false,
    operatorContentDescription: String? = null,
    onOperatorClick: (() -> Unit)? = null,
    operatorOverlay: @Composable BoxScope.() -> Unit = {},
    rightOperandModifier: Modifier = Modifier,
    isRightOperandHighlighted: Boolean = false,
    isRightOperandInputActive: Boolean = false,
    rightOperandContentDescription: String? = null,
    onRightOperandClick: (() -> Unit)? = null,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TILE_EXPRESSION_ITEM_SPACING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TileExpressionItem(
            text = tile.leftOperandLabel,
            modifier = Modifier
                .weight(1f)
                .then(leftOperandModifier),
            contentDescription = leftOperandContentDescription,
            isHighlighted = isLeftOperandHighlighted,
            isInputActive = isLeftOperandInputActive,
            onClick = onLeftOperandClick,
            textColor = textColor,
            isOperand = true,
            horizontalTextPadding = TILE_OPERAND_TEXT_PADDING
        )
        TileExpressionItem(
            text = tile.operatorLabel,
            modifier = Modifier
                .width(TILE_OPERATOR_SLOT_WIDTH)
                .then(operatorModifier),
            contentDescription = operatorContentDescription,
            isHighlighted = isOperatorHighlighted,
            isInputActive = isOperatorInputActive,
            onClick = onOperatorClick,
            textColor = textColor,
            overlayContent = operatorOverlay
        )
        TileExpressionItem(
            text = tile.rightOperandLabel,
            modifier = Modifier
                .weight(1f)
                .then(rightOperandModifier),
            contentDescription = rightOperandContentDescription,
            isHighlighted = isRightOperandHighlighted,
            isInputActive = isRightOperandInputActive,
            onClick = onRightOperandClick,
            textColor = textColor,
            isOperand = true,
            horizontalTextPadding = TILE_OPERAND_TEXT_PADDING
        )
    }
}

@Composable
private fun TileExpressionItem(
    text: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isHighlighted: Boolean = false,
    isInputActive: Boolean = false,
    onClick: (() -> Unit)? = null,
    textColor: Color = Color.Unspecified,
    isOperand: Boolean = false,
    horizontalTextPadding: Dp = 0.dp,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    val activeInputStateDescription = stringResource(R.string.tile_expression_active_input_state)
    val confirmationScale = remember { Animatable(1f) }
    var previousText by remember { mutableStateOf(text) }
    val inputShape = RoundedCornerShape(HIGHLIGHTED_TILE_EXPRESSION_SLOT_CORNER_RADIUS)
    val resolvedTextColor = if (isInputActive) {
        MaterialTheme.numPairsSemanticColors.onSelectionContainer
    } else {
        textColor
    }
    val slotModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics(mergeDescendants = true) {
            this.contentDescription = contentDescription
        }
    }

    LaunchedEffect(text) {
        val shouldConfirmSelection = text != previousText && text != "?"
        previousText = text
        confirmationScale.snapTo(1f)

        if (shouldConfirmSelection) {
            confirmationScale.animateTo(
                targetValue = TILE_INPUT_CONFIRMATION_SCALE,
                animationSpec = tween(
                    durationMillis = TILE_INPUT_CONFIRMATION_SCALE_UP_DURATION_MILLIS,
                    easing = FastOutSlowInEasing
                )
            )
            confirmationScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = TILE_INPUT_CONFIRMATION_SCALE_DOWN_DURATION_MILLIS,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Box(
        modifier = slotModifier
            .widthIn(min = TILE_EXPRESSION_ITEM_MIN_WIDTH)
            .defaultMinSize(minHeight = TILE_EXPRESSION_ITEM_MIN_HEIGHT)
            .background(
                color = if (isInputActive) {
                    MaterialTheme.numPairsSemanticColors.selectionContainer
                } else {
                    Color.Transparent
                },
                shape = inputShape
            )
            .expressionSlotBorder(
                isHighlighted = isHighlighted,
                isInputActive = isInputActive
            )
            .gameHighlightSemantics(isHighlighted)
            .semantics {
                if (isInputActive) {
                    stateDescription = activeInputStateDescription
                    tileInputActive = true
                }
            }
            .let { currentModifier ->
                if (onClick == null) {
                    currentModifier
                } else {
                    currentModifier.clickable(onClick = onClick)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalTextPadding)
                .graphicsLayer {
                    scaleX = confirmationScale.value
                    scaleY = confirmationScale.value
                },
            style = expressionTextStyle(text = text, isOperand = isOperand),
            color = resolvedTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        overlayContent()
    }
}

@Composable
private fun Modifier.expressionSlotBorder(isHighlighted: Boolean, isInputActive: Boolean): Modifier = when {
    isHighlighted -> border(
        border = BorderStroke(
            width = HIGHLIGHTED_TILE_EXPRESSION_SLOT_BORDER_WIDTH,
            color = MaterialTheme.numPairsSemanticColors.tutorialHighlight
        ),
        shape = RoundedCornerShape(HIGHLIGHTED_TILE_EXPRESSION_SLOT_CORNER_RADIUS)
    )

    isInputActive -> border(
        border = BorderStroke(
            width = NumPairsComponents.FocusBorderWidth,
            color = MaterialTheme.numPairsSemanticColors.selection
        ),
        shape = RoundedCornerShape(HIGHLIGHTED_TILE_EXPRESSION_SLOT_CORNER_RADIUS)
    )

    else -> this
}

private fun expressionTextStyle(text: String, isOperand: Boolean) = when {
    isOperand && text.length >= LARGE_OPERAND_CHARACTER_COUNT -> NumPairsTextStyles.TileExpressionCompact
    else -> NumPairsTextStyles.TileExpression
}
