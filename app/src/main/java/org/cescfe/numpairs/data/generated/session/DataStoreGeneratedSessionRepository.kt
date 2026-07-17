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
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.SNAPSHOT] = codec.encode(snapshot)
        }
    }

    override suspend fun updateCurrentPuzzle(expectedSessionId: GeneratedSessionId, puzzle: Puzzle): Boolean {
        var wasUpdated = false
        dataStore.edit { preferences ->
            val snapshot = preferences.currentSnapshotOrNull()
            if (snapshot?.sessionId == expectedSessionId) {
                preferences[PreferenceKeys.SNAPSHOT] = codec.encode(
                    snapshot.copy(currentPuzzle = puzzle)
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
