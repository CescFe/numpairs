package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.generated.ProductAnchorMix

internal data class GeneratedPairsStripEntry(val id: Int, val value: Int)

internal data class GeneratedPairsSolvedCandidate(
    val entries: List<GeneratedPairsStripEntry>,
    val pairs: List<GeneratedPairsEntryPair>
)

internal data class GeneratedPairsGenerationTargets(val primeProductDecoyPairTargetCount: Int) {
    fun shouldPreferPrimeProductDecoy(currentPrimeProductDecoyCount: Int): Boolean =
        currentPrimeProductDecoyCount < primeProductDecoyPairTargetCount
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
