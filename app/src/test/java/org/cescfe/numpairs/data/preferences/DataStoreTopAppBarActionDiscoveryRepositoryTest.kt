package org.cescfe.numpairs.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreTopAppBarActionDiscoveryRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreJobs = mutableListOf<Job>()

    @After
    fun tearDown() {
        dataStoreJobs.forEach(Job::cancel)
    }

    @Test
    fun discoveryStateDefaultsToNotSeenForFreshPreferences() = runBlocking {
        val fixture = createRepository()

        assertEquals(
            TopAppBarActionDiscoveryState(
                hasSeenHelpAction = false,
                hasSeenHintAction = false
            ),
            fixture.repository.discoveryState.first()
        )
    }

    @Test
    fun canMarkHelpAndHintActionsAsSeenIndependently() = runBlocking {
        val fixture = createRepository()

        fixture.repository.markHelpActionSeen()

        assertEquals(
            TopAppBarActionDiscoveryState(
                hasSeenHelpAction = true,
                hasSeenHintAction = false
            ),
            fixture.repository.discoveryState.first()
        )

        fixture.repository.markHintActionSeen()

        assertEquals(
            TopAppBarActionDiscoveryState(
                hasSeenHelpAction = true,
                hasSeenHintAction = true
            ),
            fixture.repository.discoveryState.first()
        )
    }

    @Test
    fun discoveryStatePersistsAcrossRepositoryInstances() = runBlocking {
        val dataStoreFile = createDataStoreFile()
        val firstFixture = createRepository(dataStoreFile = dataStoreFile)

        firstFixture.repository.markHelpActionSeen()
        firstFixture.repository.markHintActionSeen()
        firstFixture.close()

        val secondFixture = createRepository(dataStoreFile = dataStoreFile)

        assertEquals(
            TopAppBarActionDiscoveryState(
                hasSeenHelpAction = true,
                hasSeenHintAction = true
            ),
            secondFixture.repository.discoveryState.first()
        )
    }

    private fun createRepository(dataStoreFile: File = createDataStoreFile()): RepositoryFixture {
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.IO)
        dataStoreJobs += job

        return RepositoryFixture(
            repository = DataStoreTopAppBarActionDiscoveryRepository(
                dataStore = PreferenceDataStoreFactory.create(
                    scope = scope,
                    produceFile = { dataStoreFile }
                )
            ),
            job = job
        )
    }

    private fun createDataStoreFile(): File = File(
        temporaryFolder.root,
        "${UUID.randomUUID()}.preferences_pb"
    )

    private data class RepositoryFixture(val repository: TopAppBarActionDiscoveryRepository, private val job: Job) {
        suspend fun close() {
            job.cancelAndJoin()
        }
    }
}
