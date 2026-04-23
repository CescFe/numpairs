package org.cescfe.numpairs.domain.puzzle

data class Strip(val items: List<StripItem>) {
    init {
        require(items.size == NUMBER_COUNT) {
            "Strip must contain exactly $NUMBER_COUNT items."
        }
    }

    fun validEntryRangeFor(index: Int): StripEntryRange {
        require(index in items.indices) {
            "Strip item index must be within the strip bounds."
        }

        require(items[index] !is StripItem.Known) {
            "Known strip items are not editable."
        }

        val minimumValue = items
            .subList(0, index)
            .lastOrNull { it is StripItem.Known }
            ?.let { (it as StripItem.Known).value }
            ?: 1

        val maximumValue = items
            .subList(index + 1, items.size)
            .firstOrNull { it is StripItem.Known }
            ?.let { (it as StripItem.Known).value }

        return StripEntryRange(
            minimumValue = minimumValue,
            maximumValue = maximumValue
        )
    }

    companion object {
        const val NUMBER_COUNT = 8
    }
}
