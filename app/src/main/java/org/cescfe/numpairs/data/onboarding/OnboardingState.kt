package org.cescfe.numpairs.data.onboarding

const val REQUIRED_ONBOARDING_VERSION = 1

data class OnboardingState(
    val isInitialized: Boolean = false,
    val completedVersion: Int = 0,
    val lastCompletedStage: OnboardingStageCheckpoint = OnboardingStageCheckpoint.NONE,
    val firstRunTutorialOutcome: FirstRunTutorialOutcome = FirstRunTutorialOutcome.UNRESOLVED
) {
    fun isRequiredVersionComplete(): Boolean = completedVersion >= REQUIRED_ONBOARDING_VERSION
}

enum class FirstRunTutorialOutcome(internal val persistedValue: Int) {
    UNRESOLVED(0),
    COMPLETED(1),
    SKIPPED(2),
    PRE_V6_UPGRADE(3),
    LEGACY_COMPLETED(4);

    val isResolved: Boolean
        get() = this != UNRESOLVED

    internal companion object {
        fun fromPersistedValue(value: Int?, completedVersion: Int): FirstRunTutorialOutcome {
            val persistedOutcome = entries.firstOrNull { outcome -> outcome.persistedValue == value }

            return when {
                completedVersion < REQUIRED_ONBOARDING_VERSION -> UNRESOLVED
                persistedOutcome == null || persistedOutcome == UNRESOLVED -> LEGACY_COMPLETED
                else -> persistedOutcome
            }
        }
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
