package org.cescfe.numpairs.data.generated.session

import kotlinx.coroutines.flow.Flow
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

interface GeneratedSessionRepository {
    val session: Flow<GeneratedSessionSnapshot?>

    suspend fun replace(snapshot: GeneratedSessionSnapshot)

    suspend fun updateCurrentPuzzle(expectedSessionId: GeneratedSessionId, puzzle: Puzzle): Boolean

    suspend fun clear(expectedSessionId: GeneratedSessionId): Boolean
}
