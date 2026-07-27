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
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptionConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedPlayOptionId

class DataStoreGeneratedDifficultySelectionRepository(
    private val dataStore: DataStore<Preferences>,
    private val catalog: GeneratedChallengeCatalog,
    playOptions: Collection<GeneratedPlayOptionConfiguration>,
    private val fallbackDifficultyByOption: Map<GeneratedPlayOptionId, DifficultyTier>,
    private val legacyModeByOption: Map<GeneratedPlayOptionId, GeneratedModeId>
) : GeneratedDifficultySelectionRepository {
    private val optionsById = playOptions.associateBy(GeneratedPlayOptionConfiguration::id)

    init {
        require(optionsById.size == playOptions.size) {
            "Generated difficulty selection options must have unique ids."
        }
        require(fallbackDifficultyByOption.keys == optionsById.keys) {
            "Every generated play option must have exactly one difficulty fallback."
        }
        require(legacyModeByOption.keys == optionsById.keys) {
            "Every generated play option must have exactly one legacy mode mapping."
        }
        fallbackDifficultyByOption.forEach { (optionId, difficulty) ->
            require(optionsById.getValue(optionId).supports(difficulty)) {
                "The fallback for option ${optionId.value} must be a supported difficulty."
            }
        }
        legacyModeByOption.forEach { (optionId, modeId) ->
            val option = optionsById.getValue(optionId)
            require(option.difficulties.all { difficulty -> catalog.supports(modeId, difficulty) }) {
                "Legacy mode ${modeId.value} must support every difficulty for option ${optionId.value}."
            }
        }
    }

    override fun selectedDifficulty(optionId: GeneratedPlayOptionId): Flow<DifficultyTier?> {
        val option = optionsById[optionId] ?: return flowOf(null)
        val fallback = fallbackDifficultyByOption.getValue(optionId)
        val legacyModeId = legacyModeByOption.getValue(optionId)

        return dataStore.data
            .catch { throwable ->
                if (throwable is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw throwable
                }
            }
            .map { preferences ->
                val storedOptionValue = preferences[difficultyPreferenceKey(optionId)]
                if (storedOptionValue != null) {
                    storedOptionValue.toDifficultyTierOrNull()
                        ?.takeIf(option::supports)
                        ?: fallback
                } else {
                    preferences[legacyDifficultyPreferenceKey(legacyModeId)]
                        .toDifficultyTierOrNull()
                        ?.takeIf(option::supports)
                        ?: fallback
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun selectDifficulty(optionId: GeneratedPlayOptionId, difficulty: DifficultyTier) {
        val option = requireNotNull(optionsById[optionId]) {
            "Generated play option ${optionId.value} does not support remembered difficulty selection."
        }
        require(option.supports(difficulty)) {
            "Difficulty ${difficulty.name} is not supported for generated play option ${optionId.value}."
        }

        dataStore.edit { preferences ->
            preferences[difficultyPreferenceKey(optionId)] = difficulty.persistedValue
        }
    }
}

internal fun difficultyPreferenceKey(optionId: GeneratedPlayOptionId): Preferences.Key<String> =
    stringPreferencesKey("generated_selected_difficulty_${optionId.value}")

internal fun legacyDifficultyPreferenceKey(modeId: GeneratedModeId): Preferences.Key<String> =
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
