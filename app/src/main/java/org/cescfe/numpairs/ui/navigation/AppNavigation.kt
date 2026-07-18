package org.cescfe.numpairs.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.cescfe.numpairs.data.generated.selection.GeneratedDifficultySelectionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.data.preferences.PersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.feature.fourpairs.FourPairsRoute
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedChallengeId
import org.cescfe.numpairs.feature.generated.GeneratedModeId
import org.cescfe.numpairs.feature.generated.GeneratedModeLaunchIntent
import org.cescfe.numpairs.feature.generated.GeneratedModeRoute
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.localizedTitle
import org.cescfe.numpairs.feature.generated.selector.GeneratedDifficultySelectorRoute
import org.cescfe.numpairs.feature.menu.MenuRoute
import org.cescfe.numpairs.feature.menu.ui.GeneratedSessionChoiceDialog
import org.cescfe.numpairs.feature.onboarding.OnboardingLoadingScreen
import org.cescfe.numpairs.feature.onboarding.RequiredOnboardingRoute
import org.cescfe.numpairs.feature.personalization.PersonalizationRoute
import org.cescfe.numpairs.feature.tutorial.GuidedIntroductionRoute

sealed interface AppDestination {
    data object Menu : AppDestination
    data object Tutorial : AppDestination
    data object Personalization : AppDestination
    data class GeneratedDifficultySelector(val modeId: GeneratedModeId) : AppDestination
    data class GeneratedChallenge(
        val challengeId: GeneratedChallengeId,
        val launchIntent: GeneratedModeLaunchIntent = GeneratedModeLaunchIntent.newPuzzle()
    ) : AppDestination
}

@Composable
fun AppNavigation(
    onboardingRepository: OnboardingRepository,
    generatedSessionRepository: GeneratedSessionRepository,
    generatedDifficultySelectionRepository: GeneratedDifficultySelectionRepository,
    personalizationPreferencesRepository: PersonalizationPreferencesRepository,
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    generatedChallengeCatalog: GeneratedChallengeCatalog,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory,
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.Menu
) {
    val onboardingState by onboardingRepository.onboardingState.collectAsState(initial = OnboardingState())

    when {
        !onboardingState.isInitialized -> OnboardingLoadingScreen(modifier = modifier)
        !onboardingState.isRequiredVersionComplete() -> RequiredOnboardingRoute(
            onboardingState = onboardingState,
            onboardingRepository = onboardingRepository,
            modifier = modifier
        )
        else -> UnlockedAppNavigation(
            generatedSessionRepository = generatedSessionRepository,
            generatedDifficultySelectionRepository = generatedDifficultySelectionRepository,
            personalizationPreferencesRepository = personalizationPreferencesRepository,
            topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
            generatedChallengeCatalog = generatedChallengeCatalog,
            generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory,
            modifier = modifier,
            startDestination = startDestination
        )
    }
}

@Composable
private fun UnlockedAppNavigation(
    generatedSessionRepository: GeneratedSessionRepository,
    generatedDifficultySelectionRepository: GeneratedDifficultySelectionRepository,
    personalizationPreferencesRepository: PersonalizationPreferencesRepository,
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    generatedChallengeCatalog: GeneratedChallengeCatalog,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory,
    modifier: Modifier,
    startDestination: AppDestination
) {
    val generatedSessionSnapshot by generatedSessionRepository.session.collectAsState(initial = null)
    val personalizationPreferences by personalizationPreferencesRepository.preferences.collectAsState(initial = null)
    val resumableSession = generatedSessionSnapshot.toResumableGeneratedSessionOrNull(
        challengeCatalog = generatedChallengeCatalog
    )
    var pendingGeneratedChallengeChoice by remember {
        mutableStateOf<GeneratedChallenge?>(null)
    }
    var currentDestination by remember(startDestination) {
        mutableStateOf(startDestination)
    }
    val navigateToMenu: () -> Unit = {
        pendingGeneratedChallengeChoice = null
        currentDestination = AppDestination.Menu
    }
    val navigateToNewGeneratedPuzzle: (GeneratedChallenge) -> Unit = { challenge ->
        currentDestination = AppDestination.GeneratedChallenge(
            challengeId = challenge.id,
            launchIntent = GeneratedModeLaunchIntent.newPuzzle()
        )
    }
    val onGeneratedChallengeSelected: (GeneratedChallenge) -> Unit = { selectedChallenge ->
        if (resumableSession == null) {
            navigateToNewGeneratedPuzzle(selectedChallenge)
        } else {
            pendingGeneratedChallengeChoice = selectedChallenge
        }
    }

    BackHandler(enabled = currentDestination != AppDestination.Menu) {
        navigateToMenu()
    }

    when (val destination = currentDestination) {
        AppDestination.Menu -> {
            MenuRoute(
                modifier = modifier,
                resumeChallengeName = resumableSession?.challenge?.let { challenge ->
                    challenge.localizedTitle(generatedChallengeCatalog)
                },
                onResumeSelected = {
                    resumableSession?.let { session ->
                        currentDestination = AppDestination.GeneratedChallenge(
                            challengeId = session.challenge.id,
                            launchIntent = GeneratedModeLaunchIntent.ResumeSession(
                                expectedSessionId = session.sessionId
                            )
                        )
                    }
                },
                onTutorialSelected = {
                    currentDestination = AppDestination.Tutorial
                },
                onPersonalizationSelected = {
                    currentDestination = AppDestination.Personalization
                },
                onFourPairsSelected = {
                    currentDestination = AppDestination.GeneratedDifficultySelector(
                        modeId = GeneratedModes.FOUR_PAIRS.id
                    )
                },
                onEightPairsSelected = {
                    currentDestination = AppDestination.GeneratedDifficultySelector(
                        modeId = GeneratedModes.EIGHT_PAIRS.id
                    )
                }
            )
        }
        AppDestination.Tutorial -> GuidedIntroductionRoute(
            modifier = modifier,
            onNavigateBack = navigateToMenu
        )
        AppDestination.Personalization -> PersonalizationRoute(
            repository = personalizationPreferencesRepository,
            onNavigateBack = navigateToMenu,
            modifier = modifier
        )
        is AppDestination.GeneratedDifficultySelector -> GeneratedDifficultySelectorRoute(
            mode = generatedChallengeCatalog.resolve(id = destination.modeId),
            repository = generatedDifficultySelectionRepository,
            onPlay = onGeneratedChallengeSelected,
            onNavigateBack = navigateToMenu,
            modifier = modifier
        )
        is AppDestination.GeneratedChallenge -> {
            val challenge = generatedChallengeCatalog.resolveChallenge(id = destination.challengeId)
            val mode = generatedChallengeCatalog.modeFor(challenge)
            val challengeTitle = challenge.localizedTitle(generatedChallengeCatalog)
            val generationUseCase = remember(generatedPuzzleGenerationUseCaseFactory, challenge.id) {
                generatedPuzzleGenerationUseCaseFactory.create(challenge = challenge)
            }

            when (mode.id) {
                GeneratedModes.FOUR_PAIRS.id -> FourPairsRoute(
                    modifier = modifier,
                    title = challengeTitle,
                    challenge = challenge,
                    launchIntent = destination.launchIntent,
                    generationUseCase = generationUseCase,
                    generatedSessionRepository = generatedSessionRepository,
                    topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
                    isGeneratedGameHapticsEnabled =
                    personalizationPreferences?.generatedGameHapticsEnabled == true,
                    onNavigateBack = navigateToMenu
                )

                else -> GeneratedModeRoute(
                    challenge = challenge,
                    launchIntent = destination.launchIntent,
                    title = challengeTitle,
                    generationUseCase = generationUseCase,
                    generatedSessionRepository = generatedSessionRepository,
                    isGeneratedGameHapticsEnabled =
                    personalizationPreferences?.generatedGameHapticsEnabled == true,
                    modifier = modifier,
                    onNavigateBack = navigateToMenu
                )
            }
        }
    }

    val selectedChallenge = pendingGeneratedChallengeChoice
    if (selectedChallenge != null && resumableSession != null) {
        val actionGuard = remember(selectedChallenge.id, resumableSession.sessionId) {
            GeneratedSessionChoiceActionGuard()
        }
        GeneratedSessionChoiceDialog(
            savedChallengeName = resumableSession.challenge.localizedTitle(generatedChallengeCatalog),
            selectedChallengeName = selectedChallenge.localizedTitle(generatedChallengeCatalog),
            onResume = {
                actionGuard.handle {
                    pendingGeneratedChallengeChoice = null
                    currentDestination = AppDestination.GeneratedChallenge(
                        challengeId = resumableSession.challenge.id,
                        launchIntent = GeneratedModeLaunchIntent.ResumeSession(
                            expectedSessionId = resumableSession.sessionId
                        )
                    )
                }
            },
            onNewPuzzle = {
                actionGuard.handle {
                    pendingGeneratedChallengeChoice = null
                    navigateToNewGeneratedPuzzle(selectedChallenge)
                }
            },
            onDismiss = {
                if (!actionGuard.isHandled) {
                    pendingGeneratedChallengeChoice = null
                }
            }
        )
    }
}
