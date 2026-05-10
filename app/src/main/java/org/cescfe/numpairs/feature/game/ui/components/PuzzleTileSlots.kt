package org.cescfe.numpairs.feature.game.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
internal fun TileResetAction(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val resetContentDescription = stringResource(R.string.tile_reset_content_description)

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
    leftOperandContentDescription: String? = null,
    onLeftOperandClick: (() -> Unit)? = null,
    operatorModifier: Modifier = Modifier,
    operatorContentDescription: String? = null,
    onOperatorClick: (() -> Unit)? = null,
    operatorOverlay: @Composable BoxScope.() -> Unit = {},
    rightOperandModifier: Modifier = Modifier,
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
    onClick: (() -> Unit)? = null,
    textColor: Color = Color.Unspecified,
    isOperand: Boolean = false,
    horizontalTextPadding: Dp = 0.dp,
    overlayContent: @Composable BoxScope.() -> Unit = {}
) {
    val slotModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics(mergeDescendants = true) {
            this.contentDescription = contentDescription
        }
    }

    Box(
        modifier = slotModifier
            .widthIn(min = TILE_EXPRESSION_ITEM_MIN_WIDTH)
            .defaultMinSize(minHeight = TILE_EXPRESSION_ITEM_MIN_HEIGHT)
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
                .padding(horizontal = horizontalTextPadding),
            style = expressionTextStyle(text = text, isOperand = isOperand),
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        overlayContent()
    }
}

@Composable
private fun expressionTextStyle(text: String, isOperand: Boolean) = when {
    isOperand && text.length >= LARGE_OPERAND_CHARACTER_COUNT -> MaterialTheme.typography.titleSmall
    else -> MaterialTheme.typography.titleMedium
}
