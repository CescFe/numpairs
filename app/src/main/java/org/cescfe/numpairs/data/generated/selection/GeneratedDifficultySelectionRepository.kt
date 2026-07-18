package org.cescfe.numpairs.data.generated.selection

import kotlinx.coroutines.flow.Flow
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.feature.generated.GeneratedModeId

interface GeneratedDifficultySelectionRepository {
    fun selectedDifficulty(modeId: GeneratedModeId): Flow<DifficultyTier?>

    suspend fun selectDifficulty(modeId: GeneratedModeId, difficulty: DifficultyTier)
}
