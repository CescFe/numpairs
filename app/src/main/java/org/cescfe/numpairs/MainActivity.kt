package org.cescfe.numpairs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.generated.session.createGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.createOnboardingRuntime
import org.cescfe.numpairs.data.preferences.createTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.feature.generated.ConfiguredGeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val onboardingRuntime = createOnboardingRuntime(applicationContext)
        val generatedSessionRepository = createGeneratedSessionRepository(applicationContext)
        val topAppBarActionDiscoveryRepository = createTopAppBarActionDiscoveryRepository(applicationContext)
        val generatedModeRegistry = GeneratedModes.registry
        val generatedPuzzleGenerationUseCaseFactory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            modeRegistry = generatedModeRegistry
        )
        lifecycleScope.launch {
            onboardingRuntime.initializer.initialize()
        }

        setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = onboardingRuntime.repository,
                    generatedSessionRepository = generatedSessionRepository,
                    topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
                    generatedModeRegistry = generatedModeRegistry,
                    generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory
                )
            }
        }
    }
}
