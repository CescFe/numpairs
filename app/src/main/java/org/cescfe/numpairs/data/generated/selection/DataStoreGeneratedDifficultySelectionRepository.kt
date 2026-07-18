package org.cescfe.numpairs.data.generated.selection

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
import org.cescfe.numpairs.feature.generated.GeneratedChallengeCatalog
import org.cescfe.numpairs.feature.generated.GeneratedModeId

class DataStoreGeneratedDifficultySelectionRepository(
    private val dataStore: DataStore<Preferences>,
    private val catalog: GeneratedChallengeCatalog,
    private val fallbackDifficultyByMode: Map<GeneratedModeId, DifficultyTier>
) : GeneratedDifficultySelectionRepository {
    init {
        val configuredModeIds = catalog.all.map { mode -> mode.id }.toSet()
        require(fallbackDifficultyByMode.keys == configuredModeIds) {
            "Every configured generated mode must have exactly one difficulty fallback."
        }
        fallbackDifficultyByMode.forEach { (modeId, difficulty) ->
            require(catalog.supports(modeId = modeId, difficulty = difficulty)) {
                "The fallback for mode ${modeId.value} must be a supported difficulty."
            }
        }
    }

    override fun selectedDifficulty(modeId: GeneratedModeId): Flow<DifficultyTier?> {
        val fallback = fallbackDifficultyByMode[modeId] ?: return flowOf(null)

        return dataStore.data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }
            .map { preferences ->
                preferences[difficultyPreferenceKey(modeId)]
                    .toDifficultyTierOrNull()
                    ?.takeIf { difficulty -> catalog.supports(modeId = modeId, difficulty = difficulty) }
                    ?: fallback
            }
            .distinctUntilChanged()
    }

    override suspend fun selectDifficulty(modeId: GeneratedModeId, difficulty: DifficultyTier) {
        require(catalog.supports(modeId = modeId, difficulty = difficulty)) {
            "Difficulty ${difficulty.name} is not supported for generated mode ${modeId.value}."
        }

        dataStore.edit { preferences ->
            preferences[difficultyPreferenceKey(modeId)] = difficulty.persistedValue
        }
    }
}

internal fun difficultyPreferenceKey(modeId: GeneratedModeId): Preferences.Key<String> =
    stringPreferencesKey("generated_selected_difficulty_${modeId.value}")

private fun GeneratedChallengeCatalog.supports(modeId: GeneratedModeId, difficulty: DifficultyTier): Boolean =
    allChallenges.any { challenge -> challenge.modeId == modeId && challenge.difficulty == difficulty }

private val DifficultyTier.persistedValue: String
    get() = when (this) {
        DifficultyTier.LOW -> "low"
        DifficultyTier.MEDIUM -> "medium"
        DifficultyTier.HARD -> "hard"
    }

private fun String?.toDifficultyTierOrNull(): DifficultyTier? = when (this) {
    "low" -> DifficultyTier.LOW
    "medium" -> DifficultyTier.MEDIUM
    "hard" -> DifficultyTier.HARD
    else -> null
}
