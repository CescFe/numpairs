package org.cescfe.numpairs.data.generated.selection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.feature.generated.GeneratedModeId
import org.cescfe.numpairs.feature.generated.GeneratedModes

class FakeGeneratedDifficultySelectionRepository(
    initialSelections: Map<GeneratedModeId, DifficultyTier> = defaultSelections
) : GeneratedDifficultySelectionRepository {
    private val selectionByMode = initialSelections.mapValues { (_, difficulty) ->
        MutableStateFlow(difficulty)
    }.toMutableMap()

    val explicitSelections = mutableListOf<Pair<GeneratedModeId, DifficultyTier>>()

    override fun selectedDifficulty(modeId: GeneratedModeId): Flow<DifficultyTier?> =
        selectionByMode[modeId] ?: flowOf(null)

    override suspend fun selectDifficulty(modeId: GeneratedModeId, difficulty: DifficultyTier) {
        val selection = requireNotNull(selectionByMode[modeId]) {
            "No fake difficulty selection is configured for mode ${modeId.value}."
        }
        require(
            GeneratedModes.catalog.allChallenges.any { challenge ->
                challenge.modeId == modeId && challenge.difficulty == difficulty
            }
        ) {
            "Difficulty ${difficulty.name} is not supported for fake mode ${modeId.value}."
        }

        explicitSelections += modeId to difficulty
        selection.value = difficulty
    }

    fun currentDifficulty(modeId: GeneratedModeId): DifficultyTier? = selectionByMode[modeId]?.value

    private companion object {
        val defaultSelections = mapOf(
            GeneratedModes.FOUR_PAIRS.id to DifficultyTier.LOW,
            GeneratedModes.EIGHT_PAIRS.id to DifficultyTier.MEDIUM
        )
    }
}
