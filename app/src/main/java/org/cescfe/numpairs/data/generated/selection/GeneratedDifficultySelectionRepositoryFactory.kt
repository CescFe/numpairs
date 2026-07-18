package org.cescfe.numpairs.data.generated.selection

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import org.cescfe.numpairs.feature.generated.GeneratedModes

fun createGeneratedDifficultySelectionRepository(context: Context): GeneratedDifficultySelectionRepository {
    val applicationContext = context.applicationContext

    return DataStoreGeneratedDifficultySelectionRepository(
        dataStore = applicationContext.generatedDifficultySelectionDataStore,
        catalog = GeneratedModes.catalog,
        fallbackDifficultyByMode = mapOf(
            GeneratedModes.FOUR_PAIRS.id to GeneratedModes.FOUR_PAIRS_LOW.difficulty,
            GeneratedModes.EIGHT_PAIRS.id to GeneratedModes.EIGHT_PAIRS_MEDIUM.difficulty
        )
    )
}

private const val GENERATED_DIFFICULTY_SELECTION_DATA_STORE_NAME = "generated_difficulty_selection"

private val Context.generatedDifficultySelectionDataStore by preferencesDataStore(
    name = GENERATED_DIFFICULTY_SELECTION_DATA_STORE_NAME,
    corruptionHandler = ReplaceFileCorruptionHandler {
        emptyPreferences()
    }
)
