package org.cescfe.numpairs.feature.game.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics

val GameHighlightedKey = SemanticsPropertyKey<Boolean>("GameHighlighted")
var SemanticsPropertyReceiver.gameHighlighted by GameHighlightedKey

val OperandSelectorUsageHintVisualStateKey = SemanticsPropertyKey<String>(
    "OperandSelectorUsageHintVisualState"
)
var SemanticsPropertyReceiver.operandSelectorUsageHintVisualState by OperandSelectorUsageHintVisualStateKey

object OperandSelectorUsageHintVisualStateValues {
    const val AVAILABLE = "available"
    const val USED_WITH_PAIRING_AVAILABLE = "used_with_pairing_available"
    const val USED_EXHAUSTED = "used_exhausted"
    const val RULE_CONFLICT = "rule_conflict"
}

internal fun Modifier.gameHighlightSemantics(isHighlighted: Boolean): Modifier = if (isHighlighted) {
    semantics {
        gameHighlighted = true
    }
} else {
    this
}
