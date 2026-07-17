package org.cescfe.numpairs.data.generated.session

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore

fun createGeneratedSessionRepository(context: Context): GeneratedSessionRepository {
    val applicationContext = context.applicationContext

    return DataStoreGeneratedSessionRepository(applicationContext.generatedSessionDataStore)
}

private const val GENERATED_SESSION_DATA_STORE_NAME = "generated_session"

private val Context.generatedSessionDataStore by preferencesDataStore(
    name = GENERATED_SESSION_DATA_STORE_NAME,
    corruptionHandler = ReplaceFileCorruptionHandler {
        emptyPreferences()
    }
)
