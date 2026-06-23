package org.cescfe.numpairs.feature.game.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.ui.theme.NumPairsComponents

internal enum class OperandUsageIndicatorState(@get:StringRes val stateDescriptionResId: Int) {
    AVAILABLE(R.string.tile_operand_usage_state_available),
    USED(R.string.tile_operand_usage_state_used)
}

@get:StringRes
internal val Operator.usageIndicatorContentDescriptionResId: Int
    get() = when (this) {
        Operator.Addition -> R.string.tile_operand_usage_addition_hint
        Operator.Multiplication -> R.string.tile_operand_usage_multiplication_hint
        Operator.Hidden -> error("Hidden operator does not expose operand usage indicators.")
    }

internal val Operator.usageIndicatorSymbol: String
    get() = when (this) {
        Operator.Addition,
        Operator.Multiplication -> symbol

        Operator.Hidden -> error("Hidden operator does not expose operand usage indicators.")
    }

internal data class OperandUsageIndicatorColors(val container: Color, val content: Color, val border: BorderStroke)

@Composable
internal fun operandUsageIndicatorColors(state: OperandUsageIndicatorState): OperandUsageIndicatorColors =
    when (state) {
        OperandUsageIndicatorState.AVAILABLE -> OperandUsageIndicatorColors(
            container = NumPairsComponents.subtleSurfaceColor(),
            content = MaterialTheme.colorScheme.onSurfaceVariant,
            border = NumPairsComponents.subtleBorder()
        )

        OperandUsageIndicatorState.USED -> OperandUsageIndicatorColors(
            container = MaterialTheme.colorScheme.secondary,
            content = MaterialTheme.colorScheme.onSecondary,
            border = BorderStroke(
                width = NumPairsComponents.ThinBorderWidth,
                color = MaterialTheme.colorScheme.secondary
            )
        )
    }
