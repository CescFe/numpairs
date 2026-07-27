package org.cescfe.numpairs.data.generated.selection

import kotlinx.coroutines.flow.Flow
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptionId

interface GeneratedDifficultySelectionRepository {
    fun selectedDifficulty(optionId: GeneratedPlayOptionId): Flow<DifficultyTier?>

    suspend fun selectDifficulty(optionId: GeneratedPlayOptionId, difficulty: DifficultyTier)
}
