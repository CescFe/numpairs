package org.cescfe.numpairs.feature.game

import org.cescfe.numpairs.domain.generated.profile.DifficultyTier

internal enum class StandardCompletionCelebration {
    GREAT_WORK,
    EXCELLENT,
    YOU_ROCK,
    NAILED_IT,
    BRILLIANT,
    KEEP_IT_UP,
    MEDIUM_HARD_IMPRESSIVE,
    HARD_UNSTOPPABLE
}

internal data class StandardCompletionCelebrationContext(
    val generatedChallengeId: String,
    val completionId: String,
    val difficulty: DifficultyTier
) {
    init {
        require(generatedChallengeId.isNotBlank()) {
            "A standard completion celebration requires a generated challenge id."
        }
        require(completionId.isNotBlank()) {
            "A standard completion celebration requires a stable completion id."
        }
    }
}

internal object StandardCompletionCelebrationSelector {
    fun select(context: StandardCompletionCelebrationContext): StandardCompletionCelebration {
        val eligibleVariants = eligibleVariants(context.difficulty)
        val stableSelectionKey = buildString {
            append(context.generatedChallengeId)
            append('|')
            append(context.completionId)
            append('|')
            append(context.difficulty.name)
        }
        return eligibleVariants[Math.floorMod(stableSelectionKey.hashCode(), eligibleVariants.size)]
    }

    internal fun eligibleVariants(difficulty: DifficultyTier): List<StandardCompletionCelebration> = buildList {
        addAll(GENERAL_VARIANTS)
        if (difficulty == DifficultyTier.MEDIUM || difficulty == DifficultyTier.HARD) {
            add(StandardCompletionCelebration.MEDIUM_HARD_IMPRESSIVE)
        }
        if (difficulty == DifficultyTier.HARD) {
            add(StandardCompletionCelebration.HARD_UNSTOPPABLE)
        }
    }

    private val GENERAL_VARIANTS = listOf(
        StandardCompletionCelebration.GREAT_WORK,
        StandardCompletionCelebration.EXCELLENT,
        StandardCompletionCelebration.YOU_ROCK,
        StandardCompletionCelebration.NAILED_IT,
        StandardCompletionCelebration.BRILLIANT
    )
}
