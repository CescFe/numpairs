package org.cescfe.numpairs.data.preferences

import kotlinx.coroutines.flow.Flow

data class TopAppBarActionDiscoveryState(val hasSeenHelpAction: Boolean = false, val hasSeenHintAction: Boolean = false)

interface TopAppBarActionDiscoveryRepository {
    val discoveryState: Flow<TopAppBarActionDiscoveryState>

    suspend fun markHelpActionSeen()

    suspend fun markHintActionSeen()
}
