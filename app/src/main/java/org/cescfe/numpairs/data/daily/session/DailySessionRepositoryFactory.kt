package org.cescfe.numpairs.data.daily.session

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore

fun createDailySessionRepository(context: Context): DailySessionRepository {
    val applicationContext = context.applicationContext

    return DataStoreDailySessionRepository(applicationContext.dailySessionDataStore)
}

private const val DAILY_SESSION_DATA_STORE_NAME = "daily_challenge"

private val Context.dailySessionDataStore by preferencesDataStore(
    name = DAILY_SESSION_DATA_STORE_NAME,
    corruptionHandler = ReplaceFileCorruptionHandler {
        emptyPreferences()
    }
)
