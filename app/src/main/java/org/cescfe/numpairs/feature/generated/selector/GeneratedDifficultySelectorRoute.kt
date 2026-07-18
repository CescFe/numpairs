package org.cescfe.numpairs.feature.generated.selector

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
import org.cescfe.numpairs.feature.generated.GeneratedModeConfiguration
import org.cescfe.numpairs.feature.generated.localizedTitle
import org.cescfe.numpairs.feature.generated.selector.ui.GeneratedDifficultyOptionId
import org.cescfe.numpairs.feature.generated.selector.ui.GeneratedDifficultyOptionUiState
import org.cescfe.numpairs.feature.generated.selector.ui.GeneratedDifficultySelectorScreen
import org.cescfe.numpairs.feature.generated.selector.ui.GeneratedDifficultySelectorUiState

@Composable
fun GeneratedDifficultySelectorRoute(
    mode: GeneratedModeConfiguration,
    repository: GeneratedDifficultySelectionRepository,
    onPlay: (GeneratedChallenge) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDifficultyFlow = remember(repository, mode.id) {
        repository.selectedDifficulty(modeId = mode.id)
    }
    val selectedDifficulty by selectedDifficultyFlow.collectAsState(initial = null)
    val selectedChallenge = selectedDifficulty?.let { difficulty ->
        mode.challenges.singleOrNull { challenge -> challenge.difficulty == difficulty }
    } ?: return
    val coroutineScope = rememberCoroutineScope()
    val state = GeneratedDifficultySelectorUiState(
        modeName = mode.localizedTitle(),
        options = mode.challenges.map { challenge ->
            GeneratedDifficultyOptionUiState(
                id = challenge.selectorOptionId,
                label = challenge.difficulty.localizedTitle()
            )
        },
        selectedOptionId = selectedChallenge.selectorOptionId
    )

    GeneratedDifficultySelectorScreen(
        state = state,
        onDifficultySelected = { optionId ->
            val challenge = mode.challengeFor(optionId)
            coroutineScope.launch {
                try {
                    repository.selectDifficulty(
                        modeId = mode.id,
                        difficulty = challenge.difficulty
                    )
                } catch (_: IOException) {
                    // Keep the last observable selection when local preference storage is unavailable.
                }
            }
        },
        onPlay = { optionId -> onPlay(mode.challengeFor(optionId)) },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

private val GeneratedChallenge.selectorOptionId: GeneratedDifficultyOptionId
    get() = GeneratedDifficultyOptionId(id.value)

private fun GeneratedModeConfiguration.challengeFor(optionId: GeneratedDifficultyOptionId): GeneratedChallenge =
    challenges.single { challenge -> challenge.id.value == optionId.value }
