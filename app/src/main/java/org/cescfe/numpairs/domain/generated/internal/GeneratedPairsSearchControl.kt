package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPuzzleGenerationCancellation
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleGenerationExecutionPolicy

internal class GeneratedPairsSearchControl(
    private val executionPolicy: GeneratedPuzzleGenerationExecutionPolicy,
    private val cancellation: GeneratedPuzzleGenerationCancellation
) {
    private var consumedWork = 0

    val searchWorkConsumed: Int
        get() = consumedWork

    fun check(): GeneratedPairsSearchControlResult = when {
        cancellation.isCancellationRequested() -> GeneratedPairsSearchControlResult.Cancelled
        consumedWork >= executionPolicy.maxSearchWork -> GeneratedPairsSearchControlResult.BudgetExhausted
        else -> GeneratedPairsSearchControlResult.Continue
    }

    fun consumeCandidateExpansion(): GeneratedPairsSearchControlResult = when (val result = check()) {
        GeneratedPairsSearchControlResult.Continue -> {
            consumedWork++
            GeneratedPairsSearchControlResult.Continue
        }

        GeneratedPairsSearchControlResult.BudgetExhausted -> GeneratedPairsSearchControlResult.BudgetExhausted
        GeneratedPairsSearchControlResult.Cancelled -> GeneratedPairsSearchControlResult.Cancelled
    }
}

internal sealed interface GeneratedPairsSearchControlResult {
    data object Continue : GeneratedPairsSearchControlResult
    data object BudgetExhausted : GeneratedPairsSearchControlResult
    data object Cancelled : GeneratedPairsSearchControlResult
}

internal sealed interface GeneratedPairsSearchOutcome<out T> {
    data class Found<T>(val value: T) : GeneratedPairsSearchOutcome<T>
    data object NoCandidate : GeneratedPairsSearchOutcome<Nothing>
    data object BudgetExhausted : GeneratedPairsSearchOutcome<Nothing>
    data object Cancelled : GeneratedPairsSearchOutcome<Nothing>
}

internal fun GeneratedPairsSearchControlResult.toSearchOutcome(): GeneratedPairsSearchOutcome<Nothing> = when (this) {
    GeneratedPairsSearchControlResult.Continue -> error("A continuing search control result cannot stop a search.")
    GeneratedPairsSearchControlResult.BudgetExhausted -> GeneratedPairsSearchOutcome.BudgetExhausted
    GeneratedPairsSearchControlResult.Cancelled -> GeneratedPairsSearchOutcome.Cancelled
}
