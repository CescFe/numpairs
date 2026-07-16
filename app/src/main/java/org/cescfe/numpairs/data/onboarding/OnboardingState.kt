package org.cescfe.numpairs.data.onboarding

const val REQUIRED_ONBOARDING_VERSION = 1

data class OnboardingState(
    val isInitialized: Boolean = false,
    val completedVersion: Int = 0,
    val lastCompletedStage: OnboardingStageCheckpoint = OnboardingStageCheckpoint.NONE,
    val postCorePath: OnboardingPostCorePath = OnboardingPostCorePath.UNDECIDED
) {
    fun isRequiredVersionComplete(): Boolean = completedVersion >= REQUIRED_ONBOARDING_VERSION
}

enum class OnboardingPostCorePath(internal val persistedValue: Int) {
    UNDECIDED(0),
    CONTINUE_GUIDED(1),
    EARLY_VALIDATION(2);

    internal companion object {
        fun fromPersistedValue(value: Int): OnboardingPostCorePath = entries
            .firstOrNull { path -> path.persistedValue == value }
            ?: UNDECIDED
    }
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
