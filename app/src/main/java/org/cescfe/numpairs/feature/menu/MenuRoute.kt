package org.cescfe.numpairs.feature.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.feature.menu.ui.MenuScreen

@Composable
fun MenuRoute(
    modifier: Modifier = Modifier,
    onTutorialSelected: () -> Unit = {},
    onFourPairsSelected: () -> Unit = {},
    onEightPairsSelected: () -> Unit = {}
) {
    MenuScreen(
        modifier = modifier,
        onTutorialSelected = onTutorialSelected,
        onFourPairsSelected = onFourPairsSelected,
        onEightPairsSelected = onEightPairsSelected
    )
}
