package org.cescfe.numpairs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

enum class AppDestination {
    Splash,
    Game
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.Splash
) {
    var currentDestination by rememberSaveable { mutableStateOf(startDestination) }

    when (currentDestination) {
        AppDestination.Splash -> SplashRoute(
            modifier = modifier,
            onFinished = { currentDestination = AppDestination.Game }
        )
        AppDestination.Game -> GameRoute(modifier = modifier)
    }
}
