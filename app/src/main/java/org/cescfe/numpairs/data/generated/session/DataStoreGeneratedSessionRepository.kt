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
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

class DataStoreGeneratedSessionRepository(
    private val dataStore: DataStore<Preferences>,
    private val codec: GeneratedSessionSnapshotCodec = GeneratedSessionSnapshotCodec()
) : GeneratedSessionRepository {
    override val session: Flow<GeneratedSessionSnapshot?> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            preferences[PreferenceKeys.SNAPSHOT]
                ?.let(codec::decode)
                ?.decodedSnapshotOrNull()
        }

    override suspend fun replace(snapshot: GeneratedSessionSnapshot) {
        require(snapshot.correctionCount == PuzzleCorrectionCount.ZERO) {
            "A new generated session must start with zero corrections."
        }
        require(snapshot.timingStartInstant == null && snapshot.completionElapsedTime == null) {
            "A new generated session must start without timing state."
        }
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SNAPSHOT] = codec.encode(snapshot)
        }
    }

    override suspend fun startTiming(
        expectedSessionId: GeneratedSessionId,
        startInstant: GeneratedTimingStartInstant
    ): GeneratedSessionTimingStartResult {
        var result: GeneratedSessionTimingStartResult? = null
        dataStore.edit { preferences ->
            val snapshot = preferences.currentSnapshotOrNull()
            if (snapshot?.sessionId != expectedSessionId) {
                result = GeneratedSessionTimingStartResult.StaleSession
                return@edit
            }
            val existingStart = snapshot.timingStartInstant
            if (existingStart != null) {
                result = GeneratedSessionTimingStartResult.AlreadyStarted(existingStart)
                return@edit
            }
            preferences[PreferenceKeys.SNAPSHOT] = codec.encode(
                snapshot.copy(timingStartInstant = startInstant)
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
            val snapshot = preferences.currentSnapshotOrNull()
            if (
                snapshot?.sessionId == expectedSessionId &&
                correctionCount.canFollow(snapshot.correctionCount) &&
                completionElapsedTime.canFollow(snapshot.completionElapsedTime, puzzle.isSolved)
            ) {
                preferences[PreferenceKeys.SNAPSHOT] = codec.encode(
                    snapshot.copy(
                        currentPuzzle = puzzle,
                        correctionCount = correctionCount,
                        completionElapsedTime = completionElapsedTime
                    )
                )
                wasUpdated = true
            }
        }

        return wasUpdated
    }

    override suspend fun clear(expectedSessionId: GeneratedSessionId): Boolean {
        var wasCleared = false
        dataStore.edit { preferences ->
            val snapshot = preferences.currentSnapshotOrNull()
            if (snapshot?.sessionId == expectedSessionId) {
                preferences.remove(PreferenceKeys.SNAPSHOT)
                wasCleared = true
            }
        }

        return wasCleared
    }

    private fun Preferences.currentSnapshotOrNull(): GeneratedSessionSnapshot? = this[PreferenceKeys.SNAPSHOT]
        ?.let(codec::decode)
        ?.decodedSnapshotOrNull()

    private object PreferenceKeys {
        val SNAPSHOT = byteArrayPreferencesKey(GENERATED_SESSION_SNAPSHOT_PREFERENCE_KEY_NAME)
    }
}

private fun GeneratedSessionSnapshotDecodingResult.decodedSnapshotOrNull(): GeneratedSessionSnapshot? = when (this) {
    is GeneratedSessionSnapshotDecodingResult.Decoded -> snapshot

    is GeneratedSessionSnapshotDecodingResult.UnsupportedVersion,
    GeneratedSessionSnapshotDecodingResult.InvalidData -> null
}

internal const val GENERATED_SESSION_SNAPSHOT_PREFERENCE_KEY_NAME = "generated_session_snapshot"

private fun PuzzleCorrectionCount?.canFollow(previous: PuzzleCorrectionCount?): Boolean = when {
    previous == null -> this == null
    this == null -> false
    else -> value >= previous.value
}

private fun GeneratedElapsedTime?.canFollow(previous: GeneratedElapsedTime?, isSolved: Boolean): Boolean = when {
    !isSolved -> this == null && previous == null
    previous == null -> true
    else -> this == previous
}
