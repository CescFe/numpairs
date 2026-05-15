package org.cescfe.numpairs.feature.fourpairs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.feature.fourpairs.ui.FourPairsScreen

@Composable
fun FourPairsRoute(modifier: Modifier = Modifier, onNavigateBack: () -> Unit = {}) {
    FourPairsScreen(
        modifier = modifier,
        onNavigateBack = onNavigateBack
    )
}
