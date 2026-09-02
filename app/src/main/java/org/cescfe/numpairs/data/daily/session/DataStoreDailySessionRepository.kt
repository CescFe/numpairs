package org.cescfe.numpairs.data.daily.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class DataStoreDailySessionRepository(
    private val dataStore: DataStore<Preferences>,
    private val codec: DailyAggregateCodec = DailyAggregateCodec()
) : DailySessionRepository {
    override val state: Flow<DailyState> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }.map { preferences ->
            preferences.currentAggregate().toDailyState()
        }

    override suspend fun replaceSession(snapshot: DailySessionSnapshot): DailySessionReplacementResult {
        require(snapshot.movementCount == DailyMovementCount.ZERO) {
            "A new Daily Session must start with zero movements."
        }
        require(snapshot.correctionCount == PuzzleCorrectionCount.ZERO) {
            "A new Daily Session must start with zero corrections."
        }
        var result: DailySessionReplacementResult? = null
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            val completion = aggregate.completions.singleOrNull { completed ->
                completed.identity.localDate == snapshot.dailyChallengeId.localDate
            }
            if (completion != null) {
                result = DailySessionReplacementResult.DateAlreadyCompleted(completion)
            } else {
                preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                    aggregate.copy(activeSession = snapshot)
                )
                result = DailySessionReplacementResult.Replaced
            }
        }
        return requireNotNull(result)
    }

    override suspend fun startTiming(
        expectedSessionId: DailySessionId,
        startInstant: DailyTimingStartInstant
    ): DailySessionTimingStartResult {
        var result: DailySessionTimingStartResult? = null
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            val activeSession = aggregate.activeSession
            if (activeSession?.sessionId != expectedSessionId) {
                result = DailySessionTimingStartResult.StaleSession
                return@edit
            }

            val existingStartInstant = activeSession.timingStartInstant
            if (existingStartInstant != null) {
                result = DailySessionTimingStartResult.AlreadyStarted(existingStartInstant)
                return@edit
            }

            preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                aggregate.copy(
                    activeSession = activeSession.copy(
                        timingStartInstant = startInstant
                    )
                )
            )
            result = DailySessionTimingStartResult.Started(startInstant)
        }
        return requireNotNull(result)
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: DailySessionId,
        puzzle: Puzzle,
        movementCount: DailyMovementCount?,
        correctionCount: PuzzleCorrectionCount?
    ): DailySessionProgressUpdateResult {
        var result: DailySessionProgressUpdateResult? = null
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            val activeSession = aggregate.activeSession
            if (activeSession?.sessionId != expectedSessionId) {
                result = DailySessionProgressUpdateResult.StaleSession
                return@edit
            }

            if (!movementCount.canFollow(activeSession.movementCount)) {
                result = DailySessionProgressUpdateResult.InvalidMovement
                return@edit
            }
            if (!correctionCount.canFollow(activeSession.correctionCount)) {
                result = DailySessionProgressUpdateResult.InvalidCorrection
                return@edit
            }

            val updatedSession = try {
                activeSession.copy(
                    currentPuzzle = puzzle,
                    movementCount = movementCount,
                    correctionCount = correctionCount
                )
            } catch (_: IllegalArgumentException) {
                result = DailySessionProgressUpdateResult.InvalidPuzzle
                return@edit
            } catch (_: IllegalStateException) {
                result = DailySessionProgressUpdateResult.InvalidPuzzle
                return@edit
            }
            preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                aggregate.copy(activeSession = updatedSession)
            )
            result = DailySessionProgressUpdateResult.Updated
        }
        return requireNotNull(result)
    }

    override suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult {
        var result: DailySessionClearResult? = null
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            if (aggregate.activeSession?.sessionId != expectedSessionId) {
                result = DailySessionClearResult.StaleSession
            } else {
                preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                    aggregate.copy(activeSession = null)
                )
                result = DailySessionClearResult.Cleared
            }
        }
        return requireNotNull(result)
    }

    override suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle,
        movementCount: DailyMovementCount?,
        correctionCount: PuzzleCorrectionCount?,
        elapsedTime: DailyElapsedTime?
    ): DailySessionCompletionResult {
        var result: DailySessionCompletionResult? = null
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            val existingCompletion = aggregate.completions.singleOrNull { completion ->
                completion.identity.localDate == expectedDailyChallengeId.localDate
            }
            if (existingCompletion != null) {
                result = DailySessionCompletionResult.AlreadyCompleted(existingCompletion)
                return@edit
            }

            val activeSession = aggregate.activeSession
            if (
                activeSession?.sessionId != expectedSessionId ||
                activeSession.dailyChallengeId != expectedDailyChallengeId
            ) {
                result = DailySessionCompletionResult.StaleSession
                return@edit
            }
            try {
                activeSession.requireValidSolvedPuzzle(solvedPuzzle)
            } catch (_: IllegalArgumentException) {
                result = DailySessionCompletionResult.InvalidPuzzle
                return@edit
            } catch (_: IllegalStateException) {
                result = DailySessionCompletionResult.InvalidPuzzle
                return@edit
            }

            if ((activeSession.timingStartInstant == null) != (elapsedTime == null)) {
                result = DailySessionCompletionResult.InvalidTiming
                return@edit
            }
            if (!movementCount.canFollow(activeSession.movementCount)) {
                result = DailySessionCompletionResult.InvalidMovement
                return@edit
            }
            if (!correctionCount.canFollow(activeSession.correctionCount)) {
                result = DailySessionCompletionResult.InvalidCorrection
                return@edit
            }

            val completion = DailyCompletion(
                identity = expectedDailyChallengeId,
                elapsedTime = elapsedTime,
                movementCount = movementCount,
                correctionCount = correctionCount
            )
            val completions = (aggregate.completions + completion)
                .sortedWith(DAILY_COMPLETION_COMPARATOR)
            preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                aggregate.copy(
                    activeSession = null,
                    completions = completions
                )
            )
            result = DailySessionCompletionResult.Completed(completion)
        }
        return requireNotNull(result)
    }

    private fun Preferences.currentAggregate(): DailyAggregate = this[PreferenceKeys.AGGREGATE]
        ?.let(codec::decode)
        ?.decodedAggregateOrEmpty()
        ?: DailyAggregate()

    private object PreferenceKeys {
        val AGGREGATE = byteArrayPreferencesKey(DAILY_AGGREGATE_PREFERENCE_KEY_NAME)
    }
}

private fun DailyAggregate.toDailyState(): DailyState = DailyState(
    activeSession = activeSession,
    completions = completions
)

private fun DailyAggregateDecodingResult.decodedAggregateOrEmpty(): DailyAggregate = when (this) {
    is DailyAggregateDecodingResult.Decoded -> aggregate

    is DailyAggregateDecodingResult.UnsupportedVersion,
    DailyAggregateDecodingResult.InvalidData -> DailyAggregate()
}

internal const val DAILY_AGGREGATE_PREFERENCE_KEY_NAME = "daily_aggregate"

private fun DailyMovementCount?.canFollow(previous: DailyMovementCount?): Boolean = when {
    previous == null -> this == null
    this == null -> false
    else -> value >= previous.value
}

private fun PuzzleCorrectionCount?.canFollow(previous: PuzzleCorrectionCount?): Boolean = when {
    previous == null -> this == null
    this == null -> false
    else -> value >= previous.value
}
