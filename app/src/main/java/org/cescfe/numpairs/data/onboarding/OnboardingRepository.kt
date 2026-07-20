package org.cescfe.numpairs.data.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val onboardingState: Flow<OnboardingState>

    suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint)

    suspend fun markTutorialCompleted()

    suspend fun markTutorialSkipped()
}
