package org.cescfe.numpairs.feature.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.feature.menu.ui.MenuScreen

@Composable
fun MenuRoute(
    modifier: Modifier = Modifier,
    onLearnBasicsTutorialSelected: () -> Unit = {},
    onPracticeFullPuzzleTutorialSelected: () -> Unit = {},
    onFourPairsSelected: () -> Unit = {}
) {
    MenuScreen(
        modifier = modifier,
        onLearnBasicsTutorialSelected = onLearnBasicsTutorialSelected,
        onPracticeFullPuzzleTutorialSelected = onPracticeFullPuzzleTutorialSelected,
        onFourPairsSelected = onFourPairsSelected
    )
}
