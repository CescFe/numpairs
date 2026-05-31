package org.cescfe.numpairs.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.puzzle.seed.initialPuzzle
import org.cescfe.numpairs.feature.fourpairs.DefaultFourPairsPuzzleProvider
import org.cescfe.numpairs.feature.fourpairs.FourPairsPuzzleProvider
import org.cescfe.numpairs.feature.fourpairs.FourPairsRoute
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.menu.MenuRoute

sealed interface AppDestination {
    data object Menu : AppDestination
    data object Game : AppDestination
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

    when (currentDestination) {
        AppDestination.Menu -> MenuRoute(
            modifier = modifier,
            onTutorialSelected = {
                currentDestination = AppDestination.Game
            },
            onFourPairsSelected = {
                currentDestination = AppDestination.FourPairs
            }
        )
        AppDestination.Game -> GameRoute(
            title = stringResource(R.string.tutorial_screen_title),
            initialPuzzle = initialPuzzle,
            modifier = modifier,
            gameSessionKey = "tutorial",
            onNavigateBack = navigateToMenu
        )
        AppDestination.FourPairs -> FourPairsRoute(
            modifier = modifier,
            puzzleProvider = fourPairsPuzzleProvider,
            onNavigateBack = navigateToMenu
        )
    }
}
