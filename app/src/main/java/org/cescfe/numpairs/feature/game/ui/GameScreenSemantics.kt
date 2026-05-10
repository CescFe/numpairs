package org.cescfe.numpairs.feature.game.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.R
import org.cescfe.numpairs.feature.game.presentation.StripItemUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemVisualStyle
import org.cescfe.numpairs.feature.game.presentation.TileUiState

@Composable
internal fun stripItemContentDescription(stripItem: StripItemUiState): String = when (stripItem.visualStyle) {
    StripItemVisualStyle.KNOWN -> stringResource(
        R.string.strip_item_known_content_description,
        stripItem.label
    )

    StripItemVisualStyle.HIDDEN -> stringResource(
        R.string.strip_item_hidden_content_description
    )

    StripItemVisualStyle.PLAYER_ENTERED -> stringResource(
        R.string.strip_item_player_entered_content_description,
        stripItem.label
    )
}

@Composable
internal fun tileLeftOperandContentDescription(tile: TileUiState): String = if (tile.leftOperandLabel == "?") {
    stringResource(R.string.tile_left_operand_hidden_content_description)
} else {
    stringResource(R.string.tile_left_operand_content_description, tile.leftOperandLabel)
}

@Composable
internal fun tileRightOperandContentDescription(tile: TileUiState): String = if (tile.rightOperandLabel == "?") {
    stringResource(R.string.tile_right_operand_hidden_content_description)
} else {
    stringResource(R.string.tile_right_operand_content_description, tile.rightOperandLabel)
}

@Composable
internal fun tileOperatorContentDescription(tile: TileUiState): String = if (tile.operatorLabel == "?") {
    stringResource(R.string.tile_operator_hidden_content_description)
} else {
    stringResource(
        R.string.tile_operator_content_description,
        tile.operatorAccessibilityLabel()
    )
}

@Composable
internal fun TileUiState.operatorAccessibilityLabel(): String = when (operatorLabel) {
    "+" -> stringResource(R.string.tile_operator_option_addition)
    "×" -> stringResource(R.string.tile_operator_option_multiplication)
    else -> operatorLabel
}
