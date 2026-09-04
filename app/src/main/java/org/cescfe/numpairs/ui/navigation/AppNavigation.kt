package org.cescfe.numpairs.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.generated.selection.GeneratedDifficultySelectionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.data.preferences.PersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.TopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.feature.daily.DailyChallengeRoute
import org.cescfe.numpairs.feature.daily.DailyCompletedTodayRoute
import org.cescfe.numpairs.feature.daily.DailyFeatureDependencies
import org.cescfe.numpairs.feature.daily.calendar.DailyCalendarRoute
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedChallengeId
import org.cescfe.numpairs.feature.generated.GeneratedLearningRoute
import org.cescfe.numpairs.feature.generated.GeneratedModeLaunchIntent
import org.cescfe.numpairs.feature.generated.GeneratedModeRoute
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPlayChallengeSelector
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptions
import org.cescfe.numpairs.feature.generated.GeneratedPlayRequest
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.localizedTitle
import org.cescfe.numpairs.feature.menu.MenuRoute
import org.cescfe.numpairs.feature.menu.ui.DailyMenuUiState
import org.cescfe.numpairs.feature.menu.ui.GeneratedSessionChoiceDialog
import org.cescfe.numpairs.feature.onboarding.OnboardingLoadingScreen
import org.cescfe.numpairs.feature.onboarding.RequiredOnboardingRoute
import org.cescfe.numpairs.feature.personalization.PersonalizationRoute
import org.cescfe.numpairs.feature.tutorial.TutorialRoute

sealed interface AppDestination {
    data object Menu : AppDestination
    data object Tutorial : AppDestination
    data object Personalization : AppDestination
    data class DailyChallenge(val identity: DailyChallengeId) : AppDestination
    data class DailyCompletedToday(val identity: DailyChallengeId) : AppDestination
    data object DailyCalendar : AppDestination
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
    generatedPlayChallengeSelector: GeneratedPlayChallengeSelector =
        GeneratedPlayChallengeSelector(generatedChallengeCatalog),
    dailyFeatureDependencies: DailyFeatureDependencies? = null,
    startDestination: AppDestination = AppDestination.Menu
) {
    val onboardingState by onboardingRepository.onboardingState.collectAsState(initial = null)
    val readyOnboardingState = onboardingState

    if (readyOnboardingState == null) {
        OnboardingLoadingScreen(modifier = modifier)
    } else {
        ReadyAppNavigation(
            onboardingState = readyOnboardingState,
            onboardingRepository = onboardingRepository,
            generatedSessionRepository = generatedSessionRepository,
            generatedDifficultySelectionRepository = generatedDifficultySelectionRepository,
            personalizationPreferencesRepository = personalizationPreferencesRepository,
            topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
            generatedChallengeCatalog = generatedChallengeCatalog,
            generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory,
            generatedPlayChallengeSelector = generatedPlayChallengeSelector,
            dailyFeatureDependencies = dailyFeatureDependencies,
            modifier = modifier,
            startDestination = startDestination
        )
    }
}

@Composable
internal fun ReadyAppNavigation(
    onboardingState: OnboardingState,
    onboardingRepository: OnboardingRepository,
    generatedSessionRepository: GeneratedSessionRepository,
    generatedDifficultySelectionRepository: GeneratedDifficultySelectionRepository,
    personalizationPreferencesRepository: PersonalizationPreferencesRepository,
    topAppBarActionDiscoveryRepository: TopAppBarActionDiscoveryRepository,
    generatedChallengeCatalog: GeneratedChallengeCatalog,
    generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory,
    modifier: Modifier = Modifier,
    generatedPlayChallengeSelector: GeneratedPlayChallengeSelector =
        GeneratedPlayChallengeSelector(generatedChallengeCatalog),
    dailyFeatureDependencies: DailyFeatureDependencies? = null,
    startDestination: AppDestination = AppDestination.Menu
) {
    if (!onboardingState.firstRunTutorialOutcome.isResolved) {
        RequiredOnboardingRoute(
            onboardingState = onboardingState,
            onboardingRepository = onboardingRepository,
            modifier = modifier
        )
    } else {
        UnlockedAppNavigation(
            generatedSessionRepository = generatedSessionRepository,
            generatedDifficultySelectionRepository = generatedDifficultySelectionRepository,
            personalizationPreferencesRepository = personalizationPreferencesRepository,
            topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
            generatedChallengeCatalog = generatedChallengeCatalog,
            generatedPuzzleGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory,
            generatedPlayChallengeSelector = generatedPlayChallengeSelector,
            dailyFeatureDependencies = dailyFeatureDependencies,
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
    generatedPlayChallengeSelector: GeneratedPlayChallengeSelector,
    dailyFeatureDependencies: DailyFeatureDependencies?,
    modifier: Modifier,
    startDestination: AppDestination
) {
    val generatedSessionSnapshot by generatedSessionRepository.session.collectAsState(initial = null)
    val personalizationPreferences by personalizationPreferencesRepository.preferences.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    val resumableSession = generatedSessionSnapshot.toResumableGeneratedSessionOrNull(
        challengeCatalog = generatedChallengeCatalog
    )
    var pendingGeneratedPlayRequest by remember {
        mutableStateOf<GeneratedPlayRequest?>(null)
    }
    var currentDestination by remember(startDestination) {
        mutableStateOf(startDestination)
    }
    val navigateToMenu: () -> Unit = {
        pendingGeneratedPlayRequest = null
        currentDestination = AppDestination.Menu
    }
    val navigateToNewGeneratedPuzzle: (GeneratedPlayRequest) -> Unit = { request ->
        val challenge = generatedPlayChallengeSelector.select(
            optionId = request.optionId,
            difficulty = request.difficulty
        )
        currentDestination = AppDestination.GeneratedChallenge(
            challengeId = challenge.id,
            launchIntent = GeneratedModeLaunchIntent.newPuzzle()
        )
    }
    val onGeneratedPlayRequested: (GeneratedPlayRequest) -> Unit = { request ->
        if (resumableSession == null) {
            navigateToNewGeneratedPuzzle(request)
        } else {
            pendingGeneratedPlayRequest = request
        }
    }

    BackHandler(enabled = currentDestination != AppDestination.Menu) {
        navigateToMenu()
    }

    when (val destination = currentDestination) {
        AppDestination.Menu -> {
            MenuRoute(
                generatedDifficultySelectionRepository = generatedDifficultySelectionRepository,
                dailySessionRepository = dailyFeatureDependencies?.dailySessionRepository,
                deviceLocalDateSource = dailyFeatureDependencies?.deviceLocalDateSource,
                modifier = modifier,
                resumeChallengeName = resumableSession?.challenge?.localizedTitle(generatedChallengeCatalog),
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
                onDailySelected = { dailyState ->
                    currentDestination = when (dailyState) {
                        is DailyMenuUiState.StartToday,
                        is DailyMenuUiState.ContinueToday -> AppDestination.DailyChallenge(
                            identity = dailyState.identity
                        )

                        is DailyMenuUiState.CompletedToday -> AppDestination.DailyCompletedToday(
                            identity = dailyState.identity
                        )
                    }
                },
                onDailyCalendarSelected = {
                    currentDestination = AppDestination.DailyCalendar
                },
                onGeneratedPlayRequested = onGeneratedPlayRequested
            )
        }

        AppDestination.Tutorial -> TutorialRoute(
            modifier = modifier,
            onNavigateBack = navigateToMenu
        )

        AppDestination.Personalization -> PersonalizationRoute(
            repository = personalizationPreferencesRepository,
            onNavigateBack = navigateToMenu,
            modifier = modifier
        )

        is AppDestination.DailyChallenge -> {
            val dependencies = requireNotNull(dailyFeatureDependencies) {
                "Daily feature dependencies are required for Daily gameplay."
            }
            DailyChallengeRoute(
                identity = destination.identity,
                dailySessionRepository = dependencies.dailySessionRepository,
                deviceLocalDateSource = dependencies.deviceLocalDateSource,
                generatedPuzzleGenerationUseCaseFactory =
                    dependencies.generatedPuzzleGenerationUseCaseFactory,
                timeSource = dependencies.timeSource,
                isGeneratedGameHapticsEnabled =
                    personalizationPreferences?.generatedGameHapticsEnabled == true,
                compactTileSelectorsEnabled =
                    personalizationPreferences?.compactTileSelectorsEnabled == true,
                onNavigateBack = navigateToMenu,
                modifier = modifier
            )
        }

        is AppDestination.DailyCompletedToday -> {
            val dependencies = requireNotNull(dailyFeatureDependencies) {
                "Daily feature dependencies are required for Daily completion."
            }
            DailyCompletedTodayRoute(
                identity = destination.identity,
                dailySessionRepository = dependencies.dailySessionRepository,
                deviceLocalDateSource = dependencies.deviceLocalDateSource,
                onNavigateBack = navigateToMenu,
                modifier = modifier
            )
        }

        AppDestination.DailyCalendar -> {
            val dependencies = requireNotNull(dailyFeatureDependencies) {
                "Daily feature dependencies are required for the Daily calendar."
            }
            DailyCalendarRoute(
                dailySessionRepository = dependencies.dailySessionRepository,
                deviceLocalDateSource = dependencies.deviceLocalDateSource,
                onNavigateBack = navigateToMenu,
                modifier = modifier
            )
        }

        is AppDestination.GeneratedChallenge -> {
            val challenge = generatedChallengeCatalog.resolveChallenge(id = destination.challengeId)
            val mode = generatedChallengeCatalog.modeFor(challenge)
            val challengeTitle = challenge.localizedTitle(generatedChallengeCatalog)
            val generationUseCase = remember(generatedPuzzleGenerationUseCaseFactory, challenge.id) {
                generatedPuzzleGenerationUseCaseFactory.create(challenge = challenge)
            }

            when (mode.id) {
                GeneratedModes.THREE_PAIRS.id,
                GeneratedModes.FOUR_PAIRS.id -> GeneratedLearningRoute(
                    modifier = modifier,
                    title = challengeTitle,
                    challenge = challenge,
                    launchIntent = destination.launchIntent,
                    generationUseCase = generationUseCase,
                    generatedSessionRepository = generatedSessionRepository,
                    topAppBarActionDiscoveryRepository = topAppBarActionDiscoveryRepository,
                    isGeneratedGameHapticsEnabled =
                        personalizationPreferences?.generatedGameHapticsEnabled == true,
                    compactTileSelectorsEnabled =
                        personalizationPreferences?.compactTileSelectorsEnabled == true,
                    isChronometerExpanded =
                        personalizationPreferences?.generatedChronometerExpanded != false,
                    onChronometerExpandedChange = { expanded ->
                        coroutineScope.launch {
                            personalizationPreferencesRepository.setGeneratedChronometerExpanded(expanded)
                        }
                    },
                    newPuzzleChallengeProvider = {
                        generatedPlayChallengeSelector.select(
                            optionId = GeneratedPlayOptions.QUICK.id,
                            difficulty = challenge.difficulty
                        )
                    },
                    replacementGenerationUseCaseFactory = generatedPuzzleGenerationUseCaseFactory,
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
                    compactTileSelectorsEnabled =
                        personalizationPreferences?.compactTileSelectorsEnabled == true,
                    isChronometerExpanded =
                        personalizationPreferences?.generatedChronometerExpanded != false,
                    onChronometerExpandedChange = { expanded ->
                        coroutineScope.launch {
                            personalizationPreferencesRepository.setGeneratedChronometerExpanded(expanded)
                        }
                    },
                    modifier = modifier,
                    onNavigateBack = navigateToMenu
                )
            }
        }
    }

    val selectedRequest = pendingGeneratedPlayRequest
    if (selectedRequest != null && resumableSession != null) {
        val actionGuard = remember(
            selectedRequest.optionId,
            selectedRequest.difficulty,
            resumableSession.sessionId
        ) {
            GeneratedSessionChoiceActionGuard()
        }
        GeneratedSessionChoiceDialog(
            savedChallengeName = resumableSession.challenge.localizedTitle(generatedChallengeCatalog),
            selectedChallengeName = selectedRequest.localizedTitle(),
            onResume = {
                actionGuard.handle {
                    pendingGeneratedPlayRequest = null
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
                    pendingGeneratedPlayRequest = null
                    navigateToNewGeneratedPuzzle(selectedRequest)
                }
            },
            onDismiss = {
                if (!actionGuard.isHandled) {
                    pendingGeneratedPlayRequest = null
                }
            }
        )
    }
}
