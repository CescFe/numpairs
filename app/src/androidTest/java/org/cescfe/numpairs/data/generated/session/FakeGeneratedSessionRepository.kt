package org.cescfe.numpairs.data.generated.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestResult
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class FakeGeneratedSessionRepository(
    initialSession: GeneratedSessionSnapshot? = null,
    initialPersonalBests: Map<GeneratedPersonalBestCategory, GeneratedElapsedTime> = emptyMap()
) : GeneratedSessionRepository {
    private val mutableSession = MutableStateFlow(initialSession)
    private val mutableState = MutableStateFlow(
        GeneratedSessionState(
            activeSession = initialSession,
            personalBests = initialPersonalBests
        )
    )
    override val state: StateFlow<GeneratedSessionState> = mutableState.asStateFlow()
    override val session: StateFlow<GeneratedSessionSnapshot?> = mutableSession.asStateFlow()

    override suspend fun replace(snapshot: GeneratedSessionSnapshot) {
        updateSession(snapshot)
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
        updateSession(snapshot.copy(timingStartInstant = startInstant))
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

        updateSession(
            snapshot.copy(
                currentPuzzle = puzzle,
                correctionCount = correctionCount,
                completionElapsedTime = completionElapsedTime
            )
        )
        return true
    }

    override suspend fun complete(
        expectedSessionId: GeneratedSessionId,
        solvedPuzzle: Puzzle,
        correctionCount: PuzzleCorrectionCount?,
        personalBestResult: GeneratedPersonalBestResult
    ): GeneratedSessionCompletionResult {
        if (mutableSession.value?.sessionId != expectedSessionId) {
            return GeneratedSessionCompletionResult.StaleSession
        }
        val category = personalBestResult.category
        if (
            category != null &&
            mutableState.value.personalBests[category] != personalBestResult.previousBestElapsedTime
        ) {
            return GeneratedSessionCompletionResult.StalePersonalBest
        }
        val resultingBests = if (category != null && personalBestResult.bestElapsedTime != null) {
            mutableState.value.personalBests + (category to personalBestResult.bestElapsedTime)
        } else {
            mutableState.value.personalBests
        }
        mutableSession.value = null
        mutableState.value = GeneratedSessionState(
            activeSession = null,
            personalBests = resultingBests
        )
        return GeneratedSessionCompletionResult.Completed
    }

    override suspend fun clear(expectedSessionId: GeneratedSessionId): Boolean {
        if (mutableSession.value?.sessionId != expectedSessionId) {
            return false
        }

        updateSession(null)
        return true
    }

    private fun updateSession(snapshot: GeneratedSessionSnapshot?) {
        mutableSession.value = snapshot
        mutableState.value = mutableState.value.copy(activeSession = snapshot)
    }
}
