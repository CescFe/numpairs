package org.cescfe.numpairs.data.onboarding

const val REQUIRED_ONBOARDING_VERSION = 1

data class OnboardingState(
    val isInitialized: Boolean = false,
    val completedVersion: Int = 0,
    val lastCompletedStage: OnboardingStageCheckpoint = OnboardingStageCheckpoint.NONE
) {
    fun isRequiredVersionComplete(): Boolean = completedVersion >= REQUIRED_ONBOARDING_VERSION
}

enum class OnboardingStageCheckpoint(internal val persistedValue: Int) {
    NONE(0),
    STAGE_ONE(1),
    STAGE_TWO(2),
    STAGE_THREE(3);

    internal companion object {
        fun fromPersistedValue(value: Int): OnboardingStageCheckpoint = entries
            .firstOrNull { checkpoint -> checkpoint.persistedValue == value }
            ?: NONE
    }
}
