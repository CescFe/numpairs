package org.cescfe.numpairs.feature.tutorial

enum class GuidedOnboardingStage {
    NUMBER_PLACEMENT,
    COMPLEMENTARY_PAIR,
    HIDDEN_STRIP_VALUE;

    internal fun nextOrNull(): GuidedOnboardingStage? = entries.getOrNull(ordinal + 1)
}
