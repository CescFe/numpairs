package org.cescfe.numpairs.feature.onboarding

import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import org.cescfe.numpairs.data.onboarding.OnboardingInitializer
import org.cescfe.numpairs.data.onboarding.OnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingState

internal sealed interface OnboardingStartupState {
    data object Loading : OnboardingStartupState

    data class Ready(val onboardingState: OnboardingState) : OnboardingStartupState

    data class Failure(val cause: Throwable, val isRetrying: Boolean = false) : OnboardingStartupState
}

internal class OnboardingStartupCoordinator(
    private val initializer: OnboardingInitializer,
    private val repository: OnboardingRepository,
    private val coroutineScope: CoroutineScope
) {
    private val mutableState = MutableStateFlow<OnboardingStartupState>(OnboardingStartupState.Loading)
    private var startupJob: Job? = null

    val state = mutableState.asStateFlow()

    init {
        start()
    }

    fun retry() {
        val failure = state.value as? OnboardingStartupState.Failure ?: return
        mutableState.value = failure.copy(isRetrying = true)
        start()
    }

    private fun start() {
        startupJob?.cancel()
        startupJob = coroutineScope.launch {
            onboardingStartupStates(
                initializer = initializer,
                repository = repository
            ).collect(mutableState::emit)
        }
    }
}

internal fun onboardingStartupStates(
    initializer: OnboardingInitializer,
    repository: OnboardingRepository
): Flow<OnboardingStartupState> = flow<OnboardingStartupState> {
    initializer.initialize()
    emitAll(
        repository.onboardingState.map { onboardingState ->
            check(onboardingState.isInitialized) {
                "Onboarding initialization completed without publishing initialized state."
            }
            OnboardingStartupState.Ready(onboardingState)
        }
    )
}.retryWhen { cause, attempt ->
    cause is IOException && attempt < AUTOMATIC_STARTUP_RETRY_COUNT
}.catch { cause ->
    emit(OnboardingStartupState.Failure(cause))
}

private const val AUTOMATIC_STARTUP_RETRY_COUNT = 2L
