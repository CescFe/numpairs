package org.cescfe.numpairs.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStoreTopAppBarActionDiscoveryRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var dataStoreScope: CoroutineScope? = null

    @After
    fun tearDown() {
        dataStoreScope?.cancel()
    }

    @Test
    fun discoveryStateDefaultsToNotSeenForFreshPreferences() = runBlocking {
        val repository = createRepository()

        assertEquals(
            TopAppBarActionDiscoveryState(
                hasSeenHelpAction = false,
                hasSeenHintAction = false
            ),
            repository.discoveryState.first()
        )
    }

    @Test
    fun canMarkHelpAndHintActionsAsSeenIndependently() = runBlocking {
        val repository = createRepository()

        repository.markHelpActionSeen()

        assertEquals(
            TopAppBarActionDiscoveryState(
                hasSeenHelpAction = true,
                hasSeenHintAction = false
            ),
            repository.discoveryState.first()
        )

        repository.markHintActionSeen()

        assertEquals(
            TopAppBarActionDiscoveryState(
                hasSeenHelpAction = true,
                hasSeenHintAction = true
            ),
            repository.discoveryState.first()
        )
    }

    private fun createRepository(): TopAppBarActionDiscoveryRepository {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStoreScope = scope
        val dataStoreFile = File(
            temporaryFolder.root,
            "${context.packageName}-${UUID.randomUUID()}.preferences_pb"
        )

        return DataStoreTopAppBarActionDiscoveryRepository(
            dataStore = PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { dataStoreFile }
            )
        )
    }
}
