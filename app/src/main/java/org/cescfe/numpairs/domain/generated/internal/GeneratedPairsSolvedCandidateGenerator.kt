package org.cescfe.numpairs.domain.generated.internal

import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId

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
                    id = StripEntryId(index),
                    value = value
                )
            }

        return GeneratedPairsSolvedCandidate(
            entries = entries,
            pairs = selectedValuePairs.toEntryPairs(entries = entries)
        )
    }

    fun generate(
        variationPlan: GeneratedPairsVariationPlan,
        searchControl: GeneratedPairsSearchControl
    ): GeneratedPairsSearchOutcome<GeneratedPairsSolvedCandidate> = when (
        val selection = valuePairSelector.selectValuePairs(
            variationPlan = variationPlan,
            searchControl = searchControl
        )
    ) {
        is GeneratedPairsSearchOutcome.Found -> {
            val selectedValuePairs = selection.value
            val entries = selectedValuePairs
                .flatMap { pair -> listOf(pair.firstValue, pair.secondValue) }
                .sorted()
                .mapIndexed { index, value ->
                    GeneratedPairsStripEntry(
                        id = StripEntryId(index),
                        value = value
                    )
                }

            GeneratedPairsSearchOutcome.Found(
                GeneratedPairsSolvedCandidate(
                    entries = entries,
                    pairs = selectedValuePairs.toEntryPairs(entries = entries)
                )
            )
        }

        GeneratedPairsSearchOutcome.NoCandidate -> GeneratedPairsSearchOutcome.NoCandidate
        GeneratedPairsSearchOutcome.BudgetExhausted -> GeneratedPairsSearchOutcome.BudgetExhausted
        GeneratedPairsSearchOutcome.Cancelled -> GeneratedPairsSearchOutcome.Cancelled
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
