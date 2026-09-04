package org.cescfe.numpairs.data.generated.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class FakeGeneratedSessionRepository(initialSession: GeneratedSessionSnapshot? = null) : GeneratedSessionRepository {
    private val mutableSession = MutableStateFlow(initialSession)
    override val session: StateFlow<GeneratedSessionSnapshot?> = mutableSession.asStateFlow()

    override suspend fun replace(snapshot: GeneratedSessionSnapshot) {
        mutableSession.value = snapshot
    }

    override suspend fun startTiming(
        expectedSessionId: GeneratedSessionId,
        startInstant: GeneratedTimingStartInstant
    ): GeneratedSessionTimingStartResult {
        val snapshot = mutableSession.value
        if (snapshot?.sessionId != expectedSessionId) {
            return GeneratedSessionTimingStartResult.StaleSession
        }
        snapshot.timingStartInstant?.let { existing ->
            return GeneratedSessionTimingStartResult.AlreadyStarted(existing)
        }
        mutableSession.value = snapshot.copy(timingStartInstant = startInstant)
        return GeneratedSessionTimingStartResult.Started(startInstant)
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: GeneratedSessionId,
        puzzle: Puzzle,
        correctionCount: PuzzleCorrectionCount?,
        completionElapsedTime: GeneratedElapsedTime?
    ): Boolean {
        val snapshot = mutableSession.value
        if (snapshot?.sessionId != expectedSessionId) {
            return false
        }

        mutableSession.value = snapshot.copy(
            currentPuzzle = puzzle,
            correctionCount = correctionCount,
            completionElapsedTime = completionElapsedTime
        )
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
