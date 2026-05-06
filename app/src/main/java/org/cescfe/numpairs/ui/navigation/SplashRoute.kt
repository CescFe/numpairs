package org.cescfe.numpairs.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.ui.screen.SplashScreen

@Composable
fun SplashRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onFinished()
    }

    SplashScreen(modifier = modifier)
}
