package org.cescfe.numpairs.data.daily.session

import kotlinx.coroutines.flow.Flow
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

data class DailyState(val activeSession: DailySessionSnapshot?, val completions: List<DailyCompletion>) {
    val completedChallengeIds: List<DailyChallengeId>
        get() = completions.map(DailyCompletion::identity)
}

sealed interface DailySessionReplacementResult {
    data object Replaced : DailySessionReplacementResult

    data class DateAlreadyCompleted(val completion: DailyCompletion) : DailySessionReplacementResult
}

sealed interface DailySessionTimingStartResult {
    data class Started(val startInstant: DailyTimingStartInstant) : DailySessionTimingStartResult

    data class AlreadyStarted(val startInstant: DailyTimingStartInstant) : DailySessionTimingStartResult

    data object StaleSession : DailySessionTimingStartResult
}

sealed interface DailySessionProgressUpdateResult {
    data object Updated : DailySessionProgressUpdateResult

    data object StaleSession : DailySessionProgressUpdateResult

    data object InvalidPuzzle : DailySessionProgressUpdateResult

    data object InvalidMovement : DailySessionProgressUpdateResult
}

sealed interface DailySessionClearResult {
    data object Cleared : DailySessionClearResult

    data object StaleSession : DailySessionClearResult
}

sealed interface DailySessionCompletionResult {
    data class Completed(val completion: DailyCompletion) : DailySessionCompletionResult

    data class AlreadyCompleted(val completion: DailyCompletion) : DailySessionCompletionResult

    data object StaleSession : DailySessionCompletionResult

    data object InvalidPuzzle : DailySessionCompletionResult

    data object InvalidTiming : DailySessionCompletionResult

    data object InvalidMovement : DailySessionCompletionResult
}

interface DailySessionRepository {
    val state: Flow<DailyState>

    suspend fun replaceSession(snapshot: DailySessionSnapshot): DailySessionReplacementResult

    suspend fun startTiming(
        expectedSessionId: DailySessionId,
        startInstant: DailyTimingStartInstant
    ): DailySessionTimingStartResult

    suspend fun updateCurrentPuzzle(
        expectedSessionId: DailySessionId,
        puzzle: Puzzle,
        movementCount: DailyMovementCount?
    ): DailySessionProgressUpdateResult

    suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult

    suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle,
        movementCount: DailyMovementCount?,
        elapsedTime: DailyElapsedTime? = null
    ): DailySessionCompletionResult
}
