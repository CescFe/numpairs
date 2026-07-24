package org.cescfe.numpairs.feature.menu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import java.io.IOException
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.generated.selection.GeneratedDifficultySelectionRepository
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedModeConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.localizedTitle
import org.cescfe.numpairs.feature.menu.ui.GeneratedDifficultyMenuOptionId
import org.cescfe.numpairs.feature.menu.ui.GeneratedDifficultyMenuOptionUiState
import org.cescfe.numpairs.feature.menu.ui.GeneratedModeMenuUiState
import org.cescfe.numpairs.feature.menu.ui.MenuScreen

@Composable
fun MenuRoute(
    generatedDifficultySelectionRepository: GeneratedDifficultySelectionRepository,
    generatedChallengeCatalog: GeneratedChallengeCatalog,
    modifier: Modifier = Modifier,
    resumeChallengeName: String? = null,
    onResumeSelected: () -> Unit = {},
    onTutorialSelected: () -> Unit = {},
    onPersonalizationSelected: () -> Unit = {},
    onGeneratedChallengeSelected: (GeneratedChallenge) -> Unit = {}
) {
    val fourPairsMode = generatedChallengeCatalog.resolve(GeneratedModes.FOUR_PAIRS.id)
    val eightPairsMode = generatedChallengeCatalog.resolve(GeneratedModes.EIGHT_PAIRS.id)
    val fourPairsChallenge = fourPairsMode.selectedChallenge(
        repository = generatedDifficultySelectionRepository
    ) ?: return
    val eightPairsChallenge = eightPairsMode.selectedChallenge(
        repository = generatedDifficultySelectionRepository
    ) ?: return
    val coroutineScope = rememberCoroutineScope()
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
        resumeChallengeName = resumeChallengeName,
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
