package org.cescfe.numpairs.data.preferences

import android.content.Context

fun createPersonalizationPreferencesRepository(context: Context): PersonalizationPreferencesRepository =
    DataStorePersonalizationPreferencesRepository(context.applicationContext.userPreferencesDataStore)
