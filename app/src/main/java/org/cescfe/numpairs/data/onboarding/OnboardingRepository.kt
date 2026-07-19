package org.cescfe.numpairs.data.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val onboardingState: Flow<OnboardingState>

    suspend fun initialize(installationKind: OnboardingInstallationKind)

    suspend fun recordStageCompleted(stage: OnboardingStageCheckpoint)

    suspend fun selectPostCorePath(path: OnboardingPostCorePath)

    suspend fun markTutorialCompleted()

    suspend fun markTutorialSkipped()
}

enum class OnboardingInstallationKind {
    FRESH_INSTALL,
    PRE_V6_UPGRADE
}
