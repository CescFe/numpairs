package org.cescfe.numpairs.feature.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import java.io.IOException
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.generated.selection.GeneratedDifficultySelectionRepository
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.feature.daily.CurrentDailyAvailability
import org.cescfe.numpairs.feature.daily.CurrentDailyAvailabilityResolver
import org.cescfe.numpairs.feature.daily.CurrentDailyChallengeResolver
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedModeConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.localizedTitle
import org.cescfe.numpairs.feature.menu.ui.DailyMenuUiState
import org.cescfe.numpairs.feature.menu.ui.GeneratedDifficultyMenuOptionId
import org.cescfe.numpairs.feature.menu.ui.GeneratedDifficultyMenuOptionUiState
import org.cescfe.numpairs.feature.menu.ui.GeneratedModeMenuUiState
import org.cescfe.numpairs.feature.menu.ui.MenuScreen

@Composable
fun MenuRoute(
    generatedDifficultySelectionRepository: GeneratedDifficultySelectionRepository,
    generatedChallengeCatalog: GeneratedChallengeCatalog,
    dailySessionRepository: DailySessionRepository? = null,
    deviceLocalDateSource: DeviceLocalDateSource? = null,
    modifier: Modifier = Modifier,
    resumeChallengeName: String? = null,
    onResumeSelected: () -> Unit = {},
    onTutorialSelected: () -> Unit = {},
    onPersonalizationSelected: () -> Unit = {},
    onDailySelected: (DailyMenuUiState) -> Unit = {},
    onDailyCalendarSelected: () -> Unit = {},
    onGeneratedChallengeSelected: (GeneratedChallenge) -> Unit = {}
) {
    require((dailySessionRepository == null) == (deviceLocalDateSource == null)) {
        "Daily Menu composition requires both the repository and local-date source."
    }
    val dailyMenuState = dailySessionRepository?.let { repository ->
        requireNotNull(deviceLocalDateSource)
        currentDailyMenuState(
            dailySessionRepository = repository,
            deviceLocalDateSource = deviceLocalDateSource
        )
    }
    if (dailySessionRepository != null && dailyMenuState == null) {
        return
    }
    val quickChallenge = generatedChallengeCatalog.resolveChallenge(GeneratedModes.THREE_PAIRS_LOW.id)
    val fourPairsMode = generatedChallengeCatalog.resolve(GeneratedModes.FOUR_PAIRS.id)
    val eightPairsMode = generatedChallengeCatalog.resolve(GeneratedModes.EIGHT_PAIRS.id)
    val fourPairsChallenge = fourPairsMode.selectedChallenge(
        repository = generatedDifficultySelectionRepository
    ) ?: return
    val eightPairsChallenge = eightPairsMode.selectedChallenge(
        repository = generatedDifficultySelectionRepository
    ) ?: return
    val coroutineScope = rememberCoroutineScope()
    val dailyActionGuard = remember(dailyMenuState) {
        DailyMenuActionGuard()
    }
    val selectDifficulty: (GeneratedModeConfiguration, GeneratedDifficultyMenuOptionId) -> Unit =
        { mode, optionId ->
            val challenge = mode.challengeFor(optionId)
            coroutineScope.launch {
                try {
                    generatedDifficultySelectionRepository.selectDifficulty(
                        modeId = mode.id,
                        difficulty = challenge.difficulty
                    )
                } catch (_: IOException) {
                    // Keep the last observable selection when local preference storage is unavailable.
                }
            }
        }

    MenuScreen(
        modifier = modifier,
        dailyChallenge = dailyMenuState,
        resumeChallengeName = resumeChallengeName,
        onDailySelected = {
            dailyMenuState?.let { currentState ->
                dailyActionGuard.handle {
                    onDailySelected(currentState)
                }
            }
        },
        onDailyCalendarSelected = onDailyCalendarSelected,
        onQuickSelected = {
            onGeneratedChallengeSelected(quickChallenge)
        },
        fourPairsMode = fourPairsMode.menuUiState(
            selectedChallenge = fourPairsChallenge,
            catalog = generatedChallengeCatalog
        ),
        eightPairsMode = eightPairsMode.menuUiState(
            selectedChallenge = eightPairsChallenge,
            catalog = generatedChallengeCatalog
        ),
        onResumeSelected = onResumeSelected,
        onTutorialSelected = onTutorialSelected,
        onPersonalizationSelected = onPersonalizationSelected,
        onFourPairsSelected = {
            onGeneratedChallengeSelected(fourPairsChallenge)
        },
        onEightPairsSelected = {
            onGeneratedChallengeSelected(eightPairsChallenge)
        },
        onFourPairsDifficultySelected = { optionId ->
            selectDifficulty(fourPairsMode, optionId)
        },
        onEightPairsDifficultySelected = { optionId ->
            selectDifficulty(eightPairsMode, optionId)
        }
    )
}

@Composable
private fun currentDailyMenuState(
    dailySessionRepository: DailySessionRepository,
    deviceLocalDateSource: DeviceLocalDateSource
): DailyMenuUiState? {
    val currentDailyChallengeResolver = remember(deviceLocalDateSource) {
        CurrentDailyChallengeResolver(localDateSource = deviceLocalDateSource)
    }
    val availabilityResolver = remember(
        currentDailyChallengeResolver,
        dailySessionRepository
    ) {
        CurrentDailyAvailabilityResolver(
            currentDailyChallengeResolver = currentDailyChallengeResolver,
            dailySessionRepository = dailySessionRepository
        )
    }
    val capturedCurrentDailyChallenge = remember(currentDailyChallengeResolver) {
        currentDailyChallengeResolver.resolve()
    }
    val dailyState by dailySessionRepository.state.collectAsState(initial = null)

    return remember(
        availabilityResolver,
        capturedCurrentDailyChallenge,
        dailyState
    ) {
        dailyState?.let { currentState ->
            availabilityResolver.resolve(
                currentDailyChallenge = capturedCurrentDailyChallenge,
                dailyState = currentState
            ).toMenuUiState()
        }
    }
}

private fun CurrentDailyAvailability.toMenuUiState(): DailyMenuUiState = when (this) {
    is CurrentDailyAvailability.StartToday -> DailyMenuUiState.StartToday(
        identity = currentDailyChallenge.identity
    )

    is CurrentDailyAvailability.ContinueToday -> DailyMenuUiState.ContinueToday(
        identity = currentDailyChallenge.identity
    )

    is CurrentDailyAvailability.CompletedToday -> DailyMenuUiState.CompletedToday(
        identity = completion
    )
}

@Composable
private fun GeneratedModeConfiguration.selectedChallenge(
    repository: GeneratedDifficultySelectionRepository
): GeneratedChallenge? {
    val selectedDifficultyFlow = remember(repository, id) {
        repository.selectedDifficulty(modeId = id)
    }
    val selectedDifficulty by selectedDifficultyFlow.collectAsState(initial = null)

    return selectedDifficulty?.let { difficulty ->
        challenges.singleOrNull { challenge -> challenge.difficulty == difficulty }
    }
}

@Composable
private fun GeneratedModeConfiguration.menuUiState(
    selectedChallenge: GeneratedChallenge,
    catalog: GeneratedChallengeCatalog
): GeneratedModeMenuUiState = GeneratedModeMenuUiState(
    modeName = localizedTitle(),
    challengeName = selectedChallenge.localizedTitle(catalog),
    difficultyOptions = challenges.map { challenge ->
        GeneratedDifficultyMenuOptionUiState(
            id = challenge.menuOptionId,
            label = challenge.difficulty.localizedTitle()
        )
    },
    selectedDifficultyOptionId = selectedChallenge.menuOptionId
)

private fun GeneratedModeConfiguration.challengeFor(optionId: GeneratedDifficultyMenuOptionId): GeneratedChallenge =
    challenges.single { challenge ->
        challenge.id.value == optionId.value
    }

private val GeneratedChallenge.menuOptionId: GeneratedDifficultyMenuOptionId
    get() = GeneratedDifficultyMenuOptionId(id.value)
