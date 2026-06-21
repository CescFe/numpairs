package org.cescfe.numpairs.data.preferences

import android.content.Context

fun createTopAppBarActionDiscoveryRepository(context: Context): TopAppBarActionDiscoveryRepository =
    DataStoreTopAppBarActionDiscoveryRepository(context.applicationContext.userPreferencesDataStore)
