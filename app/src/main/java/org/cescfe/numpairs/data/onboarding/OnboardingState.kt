package org.cescfe.numpairs.data.onboarding

data class OnboardingState(
    val lastCompletedStage: OnboardingStageCheckpoint = OnboardingStageCheckpoint.NONE,
    val firstRunTutorialOutcome: FirstRunTutorialOutcome = FirstRunTutorialOutcome.UNRESOLVED
)

enum class FirstRunTutorialOutcome(internal val persistedValue: Int) {
    UNRESOLVED(0),
    COMPLETED(1),
    SKIPPED(2);

    val isResolved: Boolean
        get() = this != UNRESOLVED

    internal companion object {
        fun fromPersistedValue(value: Int?): FirstRunTutorialOutcome = entries
            .firstOrNull { outcome -> outcome.persistedValue == value }
            ?: UNRESOLVED
    }
}

enum class OnboardingStageCheckpoint(internal val persistedValue: Int) {
    NONE(0),
    EXPLANATION_COMPLETED(3);

    internal companion object {
        fun fromPersistedValue(value: Int): OnboardingStageCheckpoint = entries
            .firstOrNull { checkpoint -> checkpoint.persistedValue == value }
            ?: NONE
    }
}
