package org.cescfe.numpairs.data.generated.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class FakeGeneratedSessionRepository(initialSession: GeneratedSessionSnapshot? = null) : GeneratedSessionRepository {
    private val mutableSession = MutableStateFlow(initialSession)
    override val session: StateFlow<GeneratedSessionSnapshot?> = mutableSession.asStateFlow()

    override suspend fun replace(snapshot: GeneratedSessionSnapshot) {
        mutableSession.value = snapshot
    }

    override suspend fun updateCurrentPuzzle(expectedSessionId: GeneratedSessionId, puzzle: Puzzle): Boolean {
        val snapshot = mutableSession.value
        if (snapshot?.sessionId != expectedSessionId) {
            return false
        }

        mutableSession.value = snapshot.copy(currentPuzzle = puzzle)
        return true
    }

    override suspend fun clear(expectedSessionId: GeneratedSessionId): Boolean {
        if (mutableSession.value?.sessionId != expectedSessionId) {
            return false
        }

        mutableSession.value = null
        return true
    }
}
