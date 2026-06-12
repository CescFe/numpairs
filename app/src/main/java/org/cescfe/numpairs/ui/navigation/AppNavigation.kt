package org.cescfe.numpairs.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.feature.fourpairs.DefaultFourPairsPuzzleProvider
import org.cescfe.numpairs.feature.fourpairs.FourPairsPuzzleProvider
import org.cescfe.numpairs.feature.fourpairs.FourPairsRoute
import org.cescfe.numpairs.feature.menu.MenuRoute
import org.cescfe.numpairs.feature.tutorial.TutorialMode
import org.cescfe.numpairs.feature.tutorial.TutorialRoute

sealed interface AppDestination {
    data object Menu : AppDestination
    data class Tutorial(val mode: TutorialMode) : AppDestination
    data object FourPairs : AppDestination
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.Menu,
    fourPairsPuzzleProvider: FourPairsPuzzleProvider = DefaultFourPairsPuzzleProvider
) {
    var currentDestination by remember(startDestination) {
        mutableStateOf(startDestination)
    }
    val navigateToMenu: () -> Unit = {
        currentDestination = AppDestination.Menu
    }

    BackHandler(enabled = currentDestination != AppDestination.Menu) {
        navigateToMenu()
    }

    when (val destination = currentDestination) {
        AppDestination.Menu -> MenuRoute(
            modifier = modifier,
            onLearnBasicsTutorialSelected = {
                currentDestination = AppDestination.Tutorial(mode = TutorialMode.LEARN_BASICS)
            },
            onPracticeFullPuzzleTutorialSelected = {
                currentDestination = AppDestination.Tutorial(mode = TutorialMode.PRACTICE_FULL_PUZZLE)
            },
            onFourPairsSelected = {
                currentDestination = AppDestination.FourPairs
            }
        )
        is AppDestination.Tutorial -> TutorialRoute(
            modifier = modifier,
            mode = destination.mode,
            onNavigateBack = navigateToMenu
        )
        AppDestination.FourPairs -> FourPairsRoute(
            modifier = modifier,
            puzzleProvider = fourPairsPuzzleProvider,
            onNavigateBack = navigateToMenu
        )
    }
}
