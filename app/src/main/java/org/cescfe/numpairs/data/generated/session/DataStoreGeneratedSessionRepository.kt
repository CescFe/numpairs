package org.cescfe.numpairs.data.generated.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestResult
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class DataStoreGeneratedSessionRepository(
    private val dataStore: DataStore<Preferences>,
    private val codec: GeneratedAggregateCodec = GeneratedAggregateCodec()
) : GeneratedSessionRepository {
    override val state: Flow<GeneratedSessionState> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            preferences.currentAggregate().toState()
        }
    override val session: Flow<GeneratedSessionSnapshot?> = state.map { storedState ->
        storedState.activeSession
    }

    override suspend fun replace(snapshot: GeneratedSessionSnapshot) {
        require(snapshot.correctionCount == PuzzleCorrectionCount.ZERO) {
            "A new generated session must start with zero corrections."
        }
        require(snapshot.timingStartInstant == null && snapshot.completionElapsedTime == null) {
            "A new generated session must start without timing state."
        }
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                preferences.currentAggregate().copy(activeSession = snapshot)
            )
        }
    }

    override suspend fun startTiming(
        expectedSessionId: GeneratedSessionId,
        startInstant: GeneratedTimingStartInstant
    ): GeneratedSessionTimingStartResult {
        var result: GeneratedSessionTimingStartResult? = null
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            val snapshot = aggregate.activeSession
            if (snapshot?.sessionId != expectedSessionId) {
                result = GeneratedSessionTimingStartResult.StaleSession
                return@edit
            }
            val existingStart = snapshot.timingStartInstant
            if (existingStart != null) {
                result = GeneratedSessionTimingStartResult.AlreadyStarted(existingStart)
                return@edit
            }
            preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                aggregate.copy(
                    activeSession = snapshot.copy(timingStartInstant = startInstant)
                )
            )
            result = GeneratedSessionTimingStartResult.Started(startInstant)
        }
        return requireNotNull(result)
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: GeneratedSessionId,
        puzzle: Puzzle,
        correctionCount: PuzzleCorrectionCount?,
        completionElapsedTime: GeneratedElapsedTime?
    ): Boolean {
        var wasUpdated = false
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            val snapshot = aggregate.activeSession
            if (
                snapshot?.sessionId == expectedSessionId &&
                correctionCount.canFollow(snapshot.correctionCount) &&
                !puzzle.isSolved &&
                completionElapsedTime == null &&
                snapshot.completionElapsedTime == null
            ) {
                preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                    aggregate.copy(
                        activeSession = snapshot.copy(
                            currentPuzzle = puzzle,
                            correctionCount = correctionCount,
                            completionElapsedTime = completionElapsedTime
                        )
                    )
                )
                wasUpdated = true
            }
        }

        return wasUpdated
    }

    override suspend fun complete(
        expectedSessionId: GeneratedSessionId,
        solvedPuzzle: Puzzle,
        correctionCount: PuzzleCorrectionCount?,
        personalBestResult: GeneratedPersonalBestResult
    ): GeneratedSessionCompletionResult {
        var result: GeneratedSessionCompletionResult? = null
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            val activeSession = aggregate.activeSession
            if (activeSession?.sessionId != expectedSessionId) {
                result = GeneratedSessionCompletionResult.StaleSession
                return@edit
            }
            if (!correctionCount.canFollow(activeSession.correctionCount)) {
                result = GeneratedSessionCompletionResult.InvalidCorrection
                return@edit
            }
            if (!solvedPuzzle.isSolved) {
                result = GeneratedSessionCompletionResult.InvalidPuzzle
                return@edit
            }
            if (
                (activeSession.timingStartInstant == null) !=
                (personalBestResult.currentElapsedTime == null)
            ) {
                result = GeneratedSessionCompletionResult.InvalidTiming
                return@edit
            }
            try {
                requireConsistentGeneratedSessionPuzzle(
                    initialPuzzle = activeSession.initialPuzzle,
                    currentPuzzle = solvedPuzzle
                )
            } catch (_: IllegalArgumentException) {
                result = GeneratedSessionCompletionResult.InvalidPuzzle
                return@edit
            } catch (_: IllegalStateException) {
                result = GeneratedSessionCompletionResult.InvalidPuzzle
                return@edit
            }

            val category = personalBestResult.category
            if (
                category != null &&
                aggregate.personalBests[category] != personalBestResult.previousBestElapsedTime
            ) {
                result = GeneratedSessionCompletionResult.StalePersonalBest
                return@edit
            }
            val resultingBests = if (category != null && personalBestResult.bestElapsedTime != null) {
                aggregate.personalBests + (category to personalBestResult.bestElapsedTime)
            } else {
                aggregate.personalBests
            }
            preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                aggregate.copy(
                    activeSession = null,
                    personalBests = resultingBests
                )
            )
            result = GeneratedSessionCompletionResult.Completed
        }
        return requireNotNull(result)
    }

    override suspend fun clear(expectedSessionId: GeneratedSessionId): Boolean {
        var wasCleared = false
        dataStore.edit { preferences ->
            val aggregate = preferences.currentAggregate()
            if (aggregate.activeSession?.sessionId == expectedSessionId) {
                preferences[PreferenceKeys.AGGREGATE] = codec.encode(
                    aggregate.copy(activeSession = null)
                )
                wasCleared = true
            }
        }

        return wasCleared
    }

    private fun Preferences.currentAggregate(): GeneratedAggregate = this[PreferenceKeys.AGGREGATE]
        ?.let(codec::decode)
        ?.decodedAggregateOrEmpty()
        ?: GeneratedAggregate()

    private object PreferenceKeys {
        val AGGREGATE = byteArrayPreferencesKey(GENERATED_SESSION_SNAPSHOT_PREFERENCE_KEY_NAME)
    }
}

private fun GeneratedAggregate.toState(): GeneratedSessionState = GeneratedSessionState(
    activeSession = activeSession,
    personalBests = personalBests
)

private fun GeneratedAggregateDecodingResult.decodedAggregateOrEmpty(): GeneratedAggregate = when (this) {
    is GeneratedAggregateDecodingResult.Decoded -> aggregate

    is GeneratedAggregateDecodingResult.UnsupportedVersion,
    GeneratedAggregateDecodingResult.InvalidData -> GeneratedAggregate()
}

internal const val GENERATED_SESSION_SNAPSHOT_PREFERENCE_KEY_NAME = "generated_session_snapshot"

private fun PuzzleCorrectionCount?.canFollow(previous: PuzzleCorrectionCount?): Boolean = when {
    previous == null -> this == null
    this == null -> false
    else -> value >= previous.value
}
