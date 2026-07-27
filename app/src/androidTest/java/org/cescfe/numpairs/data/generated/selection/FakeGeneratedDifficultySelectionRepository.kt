package org.cescfe.numpairs.data.generated.selection

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptionId
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptions

class FakeGeneratedDifficultySelectionRepository(
    initialSelections: Map<GeneratedPlayOptionId, DifficultyTier> = defaultSelections
) : GeneratedDifficultySelectionRepository {
    private val selectionByOption = initialSelections.mapValues { (_, difficulty) ->
        MutableStateFlow(difficulty)
    }.toMutableMap()

    val explicitSelections = mutableListOf<Pair<GeneratedPlayOptionId, DifficultyTier>>()

    override fun selectedDifficulty(optionId: GeneratedPlayOptionId): Flow<DifficultyTier?> =
        selectionByOption[optionId] ?: flowOf(null)

    override suspend fun selectDifficulty(optionId: GeneratedPlayOptionId, difficulty: DifficultyTier) {
        val selection = requireNotNull(selectionByOption[optionId]) {
            "No fake difficulty selection is configured for option ${optionId.value}."
        }
        require(GeneratedPlayOptions.resolve(optionId).supports(difficulty)) {
            "Difficulty ${difficulty.name} is not supported for fake option ${optionId.value}."
        }

        explicitSelections += optionId to difficulty
        selection.value = difficulty
    }

    fun currentDifficulty(optionId: GeneratedPlayOptionId): DifficultyTier? = selectionByOption[optionId]?.value

    private companion object {
        val defaultSelections = mapOf(
            GeneratedPlayOptions.QUICK.id to DifficultyTier.LOW,
            GeneratedPlayOptions.CLASSIC.id to DifficultyTier.MEDIUM
        )
    }
}
