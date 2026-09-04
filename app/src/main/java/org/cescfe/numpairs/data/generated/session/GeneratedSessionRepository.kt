package org.cescfe.numpairs.data.generated.session

import kotlinx.coroutines.flow.Flow
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestResult
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

interface GeneratedSessionRepository {
    val state: Flow<GeneratedSessionState>
    val session: Flow<GeneratedSessionSnapshot?>

    suspend fun replace(snapshot: GeneratedSessionSnapshot)

    suspend fun startTiming(
        expectedSessionId: GeneratedSessionId,
        startInstant: GeneratedTimingStartInstant
    ): GeneratedSessionTimingStartResult

    suspend fun updateCurrentPuzzle(
        expectedSessionId: GeneratedSessionId,
        puzzle: Puzzle,
        correctionCount: PuzzleCorrectionCount?,
        completionElapsedTime: GeneratedElapsedTime? = null
    ): Boolean

    suspend fun complete(
        expectedSessionId: GeneratedSessionId,
        solvedPuzzle: Puzzle,
        correctionCount: PuzzleCorrectionCount?,
        personalBestResult: GeneratedPersonalBestResult
    ): GeneratedSessionCompletionResult

    suspend fun clear(expectedSessionId: GeneratedSessionId): Boolean
}

sealed interface GeneratedSessionTimingStartResult {
    data class Started(val startInstant: GeneratedTimingStartInstant) : GeneratedSessionTimingStartResult

    data class AlreadyStarted(val startInstant: GeneratedTimingStartInstant) : GeneratedSessionTimingStartResult

    data object StaleSession : GeneratedSessionTimingStartResult
}

sealed interface GeneratedSessionCompletionResult {
    data object Completed : GeneratedSessionCompletionResult

    data object StaleSession : GeneratedSessionCompletionResult

    data object InvalidPuzzle : GeneratedSessionCompletionResult

    data object InvalidCorrection : GeneratedSessionCompletionResult

    data object InvalidTiming : GeneratedSessionCompletionResult

    data object StalePersonalBest : GeneratedSessionCompletionResult
}
