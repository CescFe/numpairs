package org.cescfe.numpairs.data.onboarding

const val REQUIRED_ONBOARDING_VERSION = 1

data class OnboardingState(
    val isInitialized: Boolean = false,
    val completedVersion: Int = 0,
    val lastCompletedStage: OnboardingStageCheckpoint = OnboardingStageCheckpoint.NONE,
    val postCorePath: OnboardingPostCorePath = OnboardingPostCorePath.UNDECIDED,
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
