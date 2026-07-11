package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.GeneratedPuzzlePairValues
import org.cescfe.numpairs.domain.generated.PrimeProductDecoyPairPattern
import org.cescfe.numpairs.domain.generated.matches

internal data class GeneratedPairsStripEntry(val id: Int, val value: Int)

internal data class GeneratedPairsSolvedCandidate(
    val entries: List<GeneratedPairsStripEntry>,
    val pairs: List<GeneratedPairsEntryPair>
)

internal data class GeneratedPairsVariationPlan(
    val primeProductDecoyDirective: GeneratedPairsPrimeProductDecoyDirective,
    val stripEntryVisibilityDirectives: Map<Int, GeneratedPairsStripEntryVisibilityDirective>
)

internal sealed interface GeneratedPairsPrimeProductDecoyDirective {
    data object Unrestricted : GeneratedPairsPrimeProductDecoyDirective

    data object Exclude : GeneratedPairsPrimeProductDecoyDirective

    data class Include(val pairCount: Int) : GeneratedPairsPrimeProductDecoyDirective {
        init {
            require(pairCount > 0) {
                "A prime-product decoy inclusion directive requires a positive pair count."
            }
        }
    }
}

internal enum class GeneratedPairsStripEntryVisibilityDirective {
    KNOWN,
    HIDDEN
}

internal data class GeneratedPairsStripMaskSelection(
    val knownEntryIds: Set<Int>,
    val variationPlanOutcome: GeneratedPairsVariationPlanOutcome
)

internal enum class GeneratedPairsVariationPlanOutcome {
    HONORED,
    FALLBACK
}

internal interface GeneratedPairsPairValues {
    val firstValue: Int
    val secondValue: Int

    val sum: Int
        get() = firstValue + secondValue

    val product: Int
        get() = firstValue * secondValue

    val resultValues: Set<Int>
        get() = setOf(sum, product)

    fun requiredOccurrencesByValue(): Map<Int, Int> = listOf(firstValue, secondValue)
        .groupingBy { value -> value }
        .eachCount()

    fun primeProductDecoyIncrement(): Int = if (isPrimeProductDecoy()) 1 else 0

    fun isPrimeProductDecoy(): Boolean = PrimeProductDecoyPairPattern.ONE_AND_PRIME.matches(
        pair = GeneratedPuzzlePairValues(
            firstValue = firstValue,
            secondValue = secondValue
        )
    )
}

internal data class GeneratedPairsValuePair(override val firstValue: Int, override val secondValue: Int) :
    GeneratedPairsPairValues

internal data class GeneratedPairsEntryPair(
    val firstEntry: GeneratedPairsStripEntry,
    val secondEntry: GeneratedPairsStripEntry
) : GeneratedPairsPairValues {
    override val firstValue: Int = firstEntry.value
    override val secondValue: Int = secondEntry.value
    val key: GeneratedPairsEntryPairKey = GeneratedPairsEntryPairKey(
        firstEntryId = minOf(firstEntry.id, secondEntry.id),
        secondEntryId = maxOf(firstEntry.id, secondEntry.id)
    )
}

internal data class GeneratedPairsEntryPairKey(val firstEntryId: Int, val secondEntryId: Int)
