package org.cescfe.numpairs.domain.generated.internal

internal class GeneratedPairsSolvedCandidateGenerator(private val valuePairSelector: GeneratedPairsValuePairSelector) {
    fun generate(variationPlan: GeneratedPairsVariationPlan): GeneratedPairsSolvedCandidate? {
        val selectedValuePairs = valuePairSelector.selectValuePairs(
            variationPlan = variationPlan
        ) ?: return null
        val entries = selectedValuePairs
            .flatMap { pair -> listOf(pair.firstValue, pair.secondValue) }
            .sorted()
            .mapIndexed { index, value ->
                GeneratedPairsStripEntry(
                    id = index,
                    value = value
                )
            }

        return GeneratedPairsSolvedCandidate(
            entries = entries,
            pairs = selectedValuePairs.toEntryPairs(entries = entries)
        )
    }
}

private fun List<GeneratedPairsValuePair>.toEntryPairs(
    entries: List<GeneratedPairsStripEntry>
): List<GeneratedPairsEntryPair> {
    val unusedEntriesByValue = entries
        .groupBy(GeneratedPairsStripEntry::value)
        .mapValues { (_, entriesForValue) -> entriesForValue.toMutableList() }

    return map { pair ->
        GeneratedPairsEntryPair(
            firstEntry = unusedEntriesByValue.takeEntry(value = pair.firstValue),
            secondEntry = unusedEntriesByValue.takeEntry(value = pair.secondValue)
        )
    }
}

private fun Map<Int, MutableList<GeneratedPairsStripEntry>>.takeEntry(value: Int): GeneratedPairsStripEntry {
    val entries = requireNotNull(get(value)) {
        "Generated value pair references a missing strip value."
    }

    return entries.removeAt(0)
}
