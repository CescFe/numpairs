package org.cescfe.numpairs.data.daily.session

import kotlinx.coroutines.flow.Flow
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

data class DailyState(val activeSession: DailySessionSnapshot?, val completedChallengeIds: List<DailyChallengeId>)

sealed interface DailySessionReplacementResult {
    data object Replaced : DailySessionReplacementResult

    data class DateAlreadyCompleted(val completion: DailyChallengeId) : DailySessionReplacementResult
}

sealed interface DailySessionProgressUpdateResult {
    data object Updated : DailySessionProgressUpdateResult

    data object StaleSession : DailySessionProgressUpdateResult

    data object InvalidPuzzle : DailySessionProgressUpdateResult
}

sealed interface DailySessionClearResult {
    data object Cleared : DailySessionClearResult

    data object StaleSession : DailySessionClearResult
}

sealed interface DailySessionCompletionResult {
    data object Completed : DailySessionCompletionResult

    data class AlreadyCompleted(val completion: DailyChallengeId) : DailySessionCompletionResult

    data object StaleSession : DailySessionCompletionResult

    data object InvalidPuzzle : DailySessionCompletionResult
}

interface DailySessionRepository {
    val state: Flow<DailyState>

    suspend fun replaceSession(snapshot: DailySessionSnapshot): DailySessionReplacementResult

    suspend fun updateCurrentPuzzle(expectedSessionId: DailySessionId, puzzle: Puzzle): DailySessionProgressUpdateResult

    suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult

    suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle
    ): DailySessionCompletionResult
}
