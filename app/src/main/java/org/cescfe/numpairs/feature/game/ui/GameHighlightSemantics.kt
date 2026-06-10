package org.cescfe.numpairs.feature.game.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics

val GameHighlightedKey = SemanticsPropertyKey<Boolean>("GameHighlighted")
var SemanticsPropertyReceiver.gameHighlighted by GameHighlightedKey

internal fun Modifier.gameHighlightSemantics(isHighlighted: Boolean): Modifier = if (isHighlighted) {
    semantics {
        gameHighlighted = true
    }
} else {
    this
}
