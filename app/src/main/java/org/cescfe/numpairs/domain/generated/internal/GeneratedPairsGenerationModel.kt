package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.ProductAnchorMix

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

internal data class GeneratedPairsValuePair(val firstValue: Int, val secondValue: Int) {
    val sum: Int = firstValue + secondValue
    val product: Int = firstValue * secondValue
    val resultValues: Set<Int> = setOf(sum, product)

    fun requiredOccurrencesByValue(): Map<Int, Int> = listOf(firstValue, secondValue)
        .groupingBy { value -> value }
        .eachCount()

    fun productAnchorIncrement(productAnchorMix: ProductAnchorMix?): Int =
        if (productAnchorMix != null && product > productAnchorMix.productResultGreaterThan) 1 else 0

    fun primeProductDecoyIncrement(): Int = if (isPrimeProductDecoy()) 1 else 0

    fun isPrimeProductDecoy(): Boolean = firstValue == 1 &&
        secondValue.isPrime() ||
        secondValue == 1 &&
        firstValue.isPrime()
}

internal data class GeneratedPairsEntryPair(
    val firstEntry: GeneratedPairsStripEntry,
    val secondEntry: GeneratedPairsStripEntry
) {
    val product: Int = firstEntry.value * secondEntry.value
    val entryIds: Set<Int> = setOf(firstEntry.id, secondEntry.id)
    val key: GeneratedPairsEntryPairKey = GeneratedPairsEntryPairKey(
        firstEntryId = minOf(firstEntry.id, secondEntry.id),
        secondEntryId = maxOf(firstEntry.id, secondEntry.id)
    )
}

internal data class GeneratedPairsEntryPairKey(val firstEntryId: Int, val secondEntryId: Int)

private fun Int.isPrime(): Boolean {
    if (this < 2) {
        return false
    }

    var candidateDivisor = 2
    while (candidateDivisor * candidateDivisor <= this) {
        if (this % candidateDivisor == 0) {
            return false
        }
        candidateDivisor++
    }

    return true
}
