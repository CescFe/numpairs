package org.cescfe.numpairs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.cescfe.numpairs.data.preferences.createTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val topAppBarActionDiscoveryRepository = createTopAppBarActionDiscoveryRepository(applicationContext)

        setContent {
            NumPairsTheme {
                AppNavigation(
                    topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository
                )
            }
        }
    }
}
