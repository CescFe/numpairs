package org.cescfe.numpairs.data.preferences

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeTopAppBarActionDiscoveryRepository(
    initialState: TopAppBarActionDiscoveryState = TopAppBarActionDiscoveryState()
) : TopAppBarActionDiscoveryRepository {
    private val mutableState = MutableStateFlow(initialState)

    val state: StateFlow<TopAppBarActionDiscoveryState> = mutableState.asStateFlow()

    override val discoveryState = state

    override suspend fun markHelpActionSeen() {
        mutableState.update { state ->
            state.copy(hasSeenHelpAction = true)
        }
    }

    override suspend fun markHintActionSeen() {
        mutableState.update { state ->
            state.copy(hasSeenHintAction = true)
        }
    }
}
