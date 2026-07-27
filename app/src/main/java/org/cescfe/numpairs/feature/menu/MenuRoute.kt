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
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.feature.daily.CurrentDailyAvailability
import org.cescfe.numpairs.feature.daily.CurrentDailyAvailabilityResolver
import org.cescfe.numpairs.feature.daily.CurrentDailyChallengeResolver
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptionConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptions
import org.cescfe.numpairs.feature.generated.GeneratedPlayRequest
import org.cescfe.numpairs.feature.generated.localizedTitle
import org.cescfe.numpairs.feature.menu.ui.DailyMenuUiState
import org.cescfe.numpairs.feature.menu.ui.GeneratedDifficultyMenuOptionId
import org.cescfe.numpairs.feature.menu.ui.GeneratedDifficultyMenuOptionUiState
import org.cescfe.numpairs.feature.menu.ui.GeneratedPlayOptionMenuUiState
import org.cescfe.numpairs.feature.menu.ui.MenuScreen

@Composable
fun MenuRoute(
    generatedDifficultySelectionRepository: GeneratedDifficultySelectionRepository,
    dailySessionRepository: DailySessionRepository? = null,
    deviceLocalDateSource: DeviceLocalDateSource? = null,
    modifier: Modifier = Modifier,
    resumeChallengeName: String? = null,
    onResumeSelected: () -> Unit = {},
    onTutorialSelected: () -> Unit = {},
    onPersonalizationSelected: () -> Unit = {},
    onDailySelected: (DailyMenuUiState) -> Unit = {},
    onDailyCalendarSelected: () -> Unit = {},
    onGeneratedPlayRequested: (GeneratedPlayRequest) -> Unit = {}
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
    val quickDifficulty = GeneratedPlayOptions.QUICK.selectedDifficulty(
        repository = generatedDifficultySelectionRepository
    ) ?: return
    val classicDifficulty = GeneratedPlayOptions.CLASSIC.selectedDifficulty(
        repository = generatedDifficultySelectionRepository
    ) ?: return
    val coroutineScope = rememberCoroutineScope()
    val dailyActionGuard = remember(dailyMenuState) {
        DailyMenuActionGuard()
    }
    val selectDifficulty: (GeneratedPlayOptionConfiguration, GeneratedDifficultyMenuOptionId) -> Unit =
        { playOption, optionId ->
            val difficulty = playOption.difficultyFor(optionId)
            coroutineScope.launch {
                try {
                    generatedDifficultySelectionRepository.selectDifficulty(
                        optionId = playOption.id,
                        difficulty = difficulty
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
        quickOption = GeneratedPlayOptions.QUICK.menuUiState(selectedDifficulty = quickDifficulty),
        classicOption = GeneratedPlayOptions.CLASSIC.menuUiState(selectedDifficulty = classicDifficulty),
        onResumeSelected = onResumeSelected,
        onTutorialSelected = onTutorialSelected,
        onPersonalizationSelected = onPersonalizationSelected,
        onQuickSelected = {
            onGeneratedPlayRequested(
                GeneratedPlayRequest(
                    optionId = GeneratedPlayOptions.QUICK.id,
                    difficulty = quickDifficulty
                )
            )
        },
        onClassicSelected = {
            onGeneratedPlayRequested(
                GeneratedPlayRequest(
                    optionId = GeneratedPlayOptions.CLASSIC.id,
                    difficulty = classicDifficulty
                )
            )
        },
        onQuickDifficultySelected = { optionId ->
            selectDifficulty(GeneratedPlayOptions.QUICK, optionId)
        },
        onClassicDifficultySelected = { optionId ->
            selectDifficulty(GeneratedPlayOptions.CLASSIC, optionId)
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
private fun GeneratedPlayOptionConfiguration.selectedDifficulty(
    repository: GeneratedDifficultySelectionRepository
): DifficultyTier? {
    val selectedDifficultyFlow = remember(repository, id) {
        repository.selectedDifficulty(optionId = id)
    }
    val selectedDifficulty by selectedDifficultyFlow.collectAsState(initial = null)
    return selectedDifficulty?.takeIf(::supports)
}

@Composable
private fun GeneratedPlayOptionConfiguration.menuUiState(
    selectedDifficulty: DifficultyTier
): GeneratedPlayOptionMenuUiState = GeneratedPlayOptionMenuUiState(
    optionName = localizedTitle(),
    selectionName = GeneratedPlayRequest(id, selectedDifficulty).localizedTitle(),
    difficultyOptions = difficulties.map { difficulty ->
        GeneratedDifficultyMenuOptionUiState(
            id = difficulty.menuOptionId,
            label = difficulty.localizedTitle()
        )
    },
    selectedDifficultyOptionId = selectedDifficulty.menuOptionId
)

private fun GeneratedPlayOptionConfiguration.difficultyFor(optionId: GeneratedDifficultyMenuOptionId): DifficultyTier =
    difficulties.single { difficulty ->
        difficulty.menuOptionId == optionId
    }

private val DifficultyTier.menuOptionId: GeneratedDifficultyMenuOptionId
    get() = GeneratedDifficultyMenuOptionId(
        when (this) {
            DifficultyTier.LOW -> "low"
            DifficultyTier.MEDIUM -> "medium"
            DifficultyTier.HARD -> "hard"
        }
    )
