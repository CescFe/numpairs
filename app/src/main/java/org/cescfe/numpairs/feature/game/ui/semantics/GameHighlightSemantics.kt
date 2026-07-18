package org.cescfe.numpairs.feature.game.ui.semantics

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

val StripEntryInputInvalidKey = SemanticsPropertyKey<Boolean>("StripEntryInputInvalid")
var SemanticsPropertyReceiver.stripEntryInputInvalid by StripEntryInputInvalidKey

val CorrectTileFeedbackIdKey = SemanticsPropertyKey<Long>("CorrectTileFeedbackId")
var SemanticsPropertyReceiver.correctTileFeedbackId by CorrectTileFeedbackIdKey

val CompletionFeedbackIdKey = SemanticsPropertyKey<Long>("CompletionFeedbackId")
var SemanticsPropertyReceiver.completionFeedbackId by CompletionFeedbackIdKey

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

internal fun Modifier.correctTileFeedbackSemantics(feedbackId: Long?): Modifier = if (feedbackId != null) {
    semantics {
        correctTileFeedbackId = feedbackId
    }
} else {
    this
}

internal fun Modifier.completionFeedbackSemantics(feedbackId: Long?): Modifier = if (feedbackId != null) {
    semantics {
        completionFeedbackId = feedbackId
    }
} else {
    this
}
