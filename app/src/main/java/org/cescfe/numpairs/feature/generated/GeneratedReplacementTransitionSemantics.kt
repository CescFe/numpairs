package org.cescfe.numpairs.feature.generated

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics

internal val GeneratedReplacementTransitionKey =
    SemanticsPropertyKey<GeneratedPuzzleReplacementTransition>("GeneratedReplacementTransition")
internal var SemanticsPropertyReceiver.generatedReplacementTransition by GeneratedReplacementTransitionKey

internal fun Modifier.generatedReplacementTransitionSemantics(
    transition: GeneratedPuzzleReplacementTransition?
): Modifier = if (transition != null) {
    semantics {
        generatedReplacementTransition = transition
    }
} else {
    this
}
