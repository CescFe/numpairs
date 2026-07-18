package org.cescfe.numpairs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.luminance
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.generated.session.createGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.createOnboardingRuntime
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.preferences.PersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.cescfe.numpairs.data.preferences.createPersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.createTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.feature.generated.ConfiguredGeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.cescfe.numpairs.ui.theme.NumPairsThemeId

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val onboardingRuntime = createOnboardingRuntime(applicationContext)
        val generatedSessionRepository = createGeneratedSessionRepository(applicationContext)
        val personalizationPreferencesRepository = createPersonalizationPreferencesRepository(applicationContext)
        val topAppBarActionDiscoveryRepository = createTopAppBarActionDiscoveryRepository(applicationContext)
        val generatedModeRegistry = GeneratedModes.registry
        val generatedPuzzleGenerationUseCaseFactory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            modeRegistry = generatedModeRegistry
        )
        lifecycleScope.launch {
            onboardingRuntime.initializer.initialize()
        }

        setContent {
            PersonalizationThemeProvider(personalizationPreferencesRepository) {
                val useDarkSystemBarIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = useDarkSystemBarIcons
                        isAppearanceLightNavigationBars = useDarkSystemBarIcons
                    }
                }
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

@Composable
internal fun PersonalizationThemeProvider(
    repository: PersonalizationPreferencesRepository,
    content: @Composable () -> Unit
) {
    val preferences by repository.preferences.collectAsState(initial = PersonalizationPreferences())

    NumPairsTheme(
        themeId = preferences.selectedTheme.toNumPairsThemeId(),
        content = content
    )
}

private fun PersonalizationTheme.toNumPairsThemeId(): NumPairsThemeId = when (this) {
    PersonalizationTheme.WARM -> NumPairsThemeId.WARM
    PersonalizationTheme.FROST -> NumPairsThemeId.FROST
    PersonalizationTheme.OBSIDIAN -> NumPairsThemeId.OBSIDIAN
    PersonalizationTheme.TERMINAL -> NumPairsThemeId.TERMINAL
    PersonalizationTheme.EMBER -> NumPairsThemeId.EMBER
}
