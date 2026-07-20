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
import org.cescfe.numpairs.data.generated.selection.createGeneratedDifficultySelectionRepository
import org.cescfe.numpairs.data.generated.session.createGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.createOnboardingRepository
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.preferences.PersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.createPersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.createTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.feature.generated.ConfiguredGeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.onboarding.OnboardingStartupCoordinator
import org.cescfe.numpairs.feature.onboarding.OnboardingStartupFailureScreen
import org.cescfe.numpairs.feature.onboarding.OnboardingStartupState
import org.cescfe.numpairs.ui.navigation.ReadyAppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val onboardingRepository = createOnboardingRepository(applicationContext)
        val onboardingStartupCoordinator = OnboardingStartupCoordinator(
            repository = onboardingRepository,
            coroutineScope = lifecycleScope
        )
        splashScreen.setKeepOnScreenCondition {
            onboardingStartupCoordinator.state.value is OnboardingStartupState.Loading
        }
        val generatedSessionRepository = createGeneratedSessionRepository(applicationContext)
        val generatedDifficultySelectionRepository = createGeneratedDifficultySelectionRepository(applicationContext)
        val personalizationPreferencesRepository = createPersonalizationPreferencesRepository(applicationContext)
        val topAppBarActionDiscoveryRepository = createTopAppBarActionDiscoveryRepository(applicationContext)
        val generatedChallengeCatalog = GeneratedModes.catalog
        val generatedPuzzleGenerationUseCaseFactory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            challengeCatalog = generatedChallengeCatalog
        )
        setContent {
            PersonalizationThemeProvider(personalizationPreferencesRepository) {
                val onboardingStartupState by onboardingStartupCoordinator.state.collectAsState()
                val useDarkSystemBarIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = useDarkSystemBarIcons
                        isAppearanceLightNavigationBars = useDarkSystemBarIcons
                    }
                }
                when (val state = onboardingStartupState) {
                    OnboardingStartupState.Loading -> Unit
                    is OnboardingStartupState.Failure -> OnboardingStartupFailureScreen(
                        isRetrying = state.isRetrying,
                        onRetry = onboardingStartupCoordinator::retry
                    )
                    is OnboardingStartupState.Ready -> ReadyAppNavigation(
                        onboardingState = state.onboardingState,
                        onboardingRepository = onboardingRepository,
                        generatedSessionRepository = generatedSessionRepository,
                        generatedDifficultySelectionRepository = generatedDifficultySelectionRepository,
                        personalizationPreferencesRepository = personalizationPreferencesRepository,
                        topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
                        generatedChallengeCatalog = generatedChallengeCatalog,
                        generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory
                    )
                }
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
        theme = preferences.selectedTheme,
        content = content
    )
}
