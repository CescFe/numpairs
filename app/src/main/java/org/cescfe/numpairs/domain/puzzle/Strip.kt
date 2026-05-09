package org.cescfe.numpairs.domain.puzzle

@ConsistentCopyVisibility
data class Strip private constructor(val entries: List<StripEntry>) {
    init {
        require(entries.size == NUMBER_COUNT) {
            "Strip must contain exactly $NUMBER_COUNT items."
        }
        require(entries.map(StripEntry::id).toSet().size == entries.size) {
            "Strip entry ids must be unique."
        }
        require(entries.visibleValuesAreNonDecreasing()) {
            "Visible strip values must be in non-decreasing order."
        }
    }

    val items: List<StripItem>
        get() = entries.map(StripEntry::item)

    val hasHiddenEntries: Boolean
        get() = entries.any { stripEntry -> stripEntry.item == StripItem.Hidden }

    fun validEntryRangeFor(index: Int): StripEntryRange = entryRangeFor(editableRunContaining(index))

    fun withUpdatedEntry(index: Int, value: Int): Strip {
        val currentStripEntry = requireEditableEntryAt(index)
        val editableRun = editableRunContaining(index)
        val validRange = entryRangeFor(editableRun)

        require(value in validRange) {
            "Updated strip values must stay within the valid entry range."
        }

        val updatedEntries = entries.toMutableList().apply {
            set(
                index,
                currentStripEntry.copy(
                    item = currentStripEntry.item.completeWith(value)
                )
            )
        }
        val playerEnteredIndexes = editableRun.filter { updatedEntries[it].item is StripItem.PlayerEntered }
        val sortedPlayerEnteredEntries = playerEnteredIndexes
            .map { playerEnteredIndex -> updatedEntries[playerEnteredIndex] }
            .sortedBy { playerEnteredEntry -> (playerEnteredEntry.item as StripItem.PlayerEntered).value }

        playerEnteredIndexes.zip(sortedPlayerEnteredEntries).forEach { (playerEnteredIndex, playerEnteredEntry) ->
            updatedEntries[playerEnteredIndex] = playerEnteredEntry
        }

        return Strip(entries = updatedEntries)
    }

    private fun entryRangeFor(editableRun: IntRange): StripEntryRange {
        val minimumValue = entries
            .getOrNull(editableRun.first - 1)
            ?.item
            ?.knownValue
            ?: 1
        val maximumValue = entries
            .getOrNull(editableRun.last + 1)
            ?.item
            ?.knownValue

        return StripEntryRange(
            minimumValue = minimumValue,
            maximumValue = maximumValue
        )
    }

    fun visibleValueForEntry(entryId: Int): Int? = entries
        .firstOrNull { stripEntry -> stripEntry.id == entryId }
        ?.item
        ?.visibleValue

    private fun editableRunContaining(index: Int): IntRange {
        requireEditableEntryAt(index)

        var firstIndex = index
        while (firstIndex > 0 && entries[firstIndex - 1].item !is StripItem.Known) {
            firstIndex--
        }

        var lastIndex = index
        while (lastIndex < entries.lastIndex && entries[lastIndex + 1].item !is StripItem.Known) {
            lastIndex++
        }

        return firstIndex..lastIndex
    }

    private fun requireEditableEntryAt(index: Int): StripEntry {
        require(index in entries.indices) {
            "Strip item index must be within the strip bounds."
        }

        return entries[index].also { stripEntry ->
            require(stripEntry.item !is StripItem.Known) {
                "Known strip items are not editable."
            }
        }
    }

    companion object {
        const val NUMBER_COUNT = 8

        fun fromEntries(entries: List<StripEntry>): Strip = Strip(entries = entries)

        fun fromItems(items: List<StripItem>): Strip = Strip(
            entries = items.mapIndexed { index, item ->
                StripEntry(
                    id = index,
                    item = item
                )
            }
        )
    }
}

private fun List<StripEntry>.visibleValuesAreNonDecreasing(): Boolean = mapNotNull { stripEntry ->
    stripEntry.item.visibleValue
}.zipWithNext().all { (leftValue, rightValue) ->
    leftValue <= rightValue
}

private val StripItem.knownValue: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> null
    }

private val StripItem.visibleValue: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> value
    }
