package org.cescfe.numpairs.data.preferences

import kotlinx.coroutines.flow.Flow

enum class PersonalizationTheme(internal val persistedValue: String) {
    WARM("warm"),
    FROST("frost"),
    OBSIDIAN("obsidian"),
    TERMINAL("terminal"),
    EMBER("ember");

    companion object {
        internal fun fromPersistedValue(value: String?): PersonalizationTheme =
            entries.firstOrNull { theme -> theme.persistedValue == value } ?: WARM
    }
}

data class PersonalizationPreferences(
    val selectedTheme: PersonalizationTheme = PersonalizationTheme.WARM,
    val generatedGameHapticsEnabled: Boolean = true
)

interface PersonalizationPreferencesRepository {
    val preferences: Flow<PersonalizationPreferences>

    suspend fun selectTheme(theme: PersonalizationTheme)

    suspend fun setGeneratedGameHapticsEnabled(enabled: Boolean)
}
