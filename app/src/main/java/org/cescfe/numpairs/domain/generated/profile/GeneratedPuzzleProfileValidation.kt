package org.cescfe.numpairs.domain.generated.profile

data class GeneratedPuzzleProfileValidationLimits(
    val maxCatalogExpansions: Int = DEFAULT_MAX_CATALOG_EXPANSIONS,
    val maxPairSelectionStates: Int = DEFAULT_MAX_PAIR_SELECTION_STATES,
    val maxMaskStates: Int = DEFAULT_MAX_MASK_STATES
) {
    init {
        require(maxCatalogExpansions > 0) {
            "Maximum profile catalog expansions must be positive."
        }
        require(maxPairSelectionStates > 0) {
            "Maximum profile pair-selection states must be positive."
        }
        require(maxMaskStates > 0) {
            "Maximum profile mask states must be positive."
        }
    }

    private companion object {
        const val DEFAULT_MAX_CATALOG_EXPANSIONS = 10_000
        const val DEFAULT_MAX_PAIR_SELECTION_STATES = 100_000
        const val DEFAULT_MAX_MASK_STATES = 100_000
    }
}

enum class GeneratedPuzzleProfileValidationWorkKind {
    CATALOG_EXPANSION,
    PAIR_SELECTION_STATE,
    MASK_STATE
}

internal class GeneratedPuzzleProfileValidationWorkTracker(private val limits: GeneratedPuzzleProfileValidationLimits) {
    private val consumedWorkByKind = mutableMapOf<GeneratedPuzzleProfileValidationWorkKind, Int>()

    fun consume(
        kind: GeneratedPuzzleProfileValidationWorkKind
    ): GeneratedPuzzleProfileViolation.ValidationWorkLimitExceeded? {
        val consumedWork = consumedWorkByKind.getOrDefault(kind, 0)
        val configuredLimit = limits.limitFor(kind = kind)
        if (consumedWork >= configuredLimit) {
            return GeneratedPuzzleProfileViolation.ValidationWorkLimitExceeded(
                workKind = kind,
                configuredLimit = configuredLimit,
                consumedWork = consumedWork
            )
        }

        consumedWorkByKind[kind] = consumedWork + 1
        return null
    }
}

internal sealed interface GeneratedPuzzleProfileBoundedEvaluation<out T> {
    data class Completed<T>(val value: T) : GeneratedPuzzleProfileBoundedEvaluation<T>

    data class LimitExceeded(val violation: GeneratedPuzzleProfileViolation.ValidationWorkLimitExceeded) :
        GeneratedPuzzleProfileBoundedEvaluation<Nothing>
}

private fun GeneratedPuzzleProfileValidationLimits.limitFor(kind: GeneratedPuzzleProfileValidationWorkKind): Int =
    when (kind) {
        GeneratedPuzzleProfileValidationWorkKind.CATALOG_EXPANSION -> maxCatalogExpansions
        GeneratedPuzzleProfileValidationWorkKind.PAIR_SELECTION_STATE -> maxPairSelectionStates
        GeneratedPuzzleProfileValidationWorkKind.MASK_STATE -> maxMaskStates
    }
