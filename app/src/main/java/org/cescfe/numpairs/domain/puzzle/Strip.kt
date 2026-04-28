package org.cescfe.numpairs.domain.puzzle

data class Strip(val entries: List<StripEntry>) {
    init {
        require(entries.size == NUMBER_COUNT) {
            "Strip must contain exactly $NUMBER_COUNT items."
        }
        require(entries.map(StripEntry::id).toSet().size == entries.size) {
            "Strip entry ids must be unique."
        }
    }

    val items: List<StripItem>
        get() = entries.map(StripEntry::item)

    fun validEntryRangeFor(index: Int): StripEntryRange {
        val editableRun = editableRunContaining(index)
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

    fun withUpdatedEntry(index: Int, value: Int): Strip {
        val currentStripEntry = requireEditableEntryAt(index)
        val updatedEntries = entries.toMutableList().apply {
            set(
                index,
                currentStripEntry.copy(
                    item = currentStripEntry.item.completeWith(value)
                )
            )
        }
        val editableRun = editableRunContaining(index)
        val playerEnteredIndexes = editableRun.filter { updatedEntries[it].item is StripItem.PlayerEntered }
        val sortedPlayerEnteredEntries = playerEnteredIndexes
            .map { playerEnteredIndex -> updatedEntries[playerEnteredIndex] }
            .sortedBy { playerEnteredEntry -> (playerEnteredEntry.item as StripItem.PlayerEntered).value }

        playerEnteredIndexes.zip(sortedPlayerEnteredEntries).forEach { (playerEnteredIndex, playerEnteredEntry) ->
            updatedEntries[playerEnteredIndex] = playerEnteredEntry
        }

        return copy(entries = updatedEntries)
    }

    fun visibleEntries(): List<VisibleStripEntry> = entries.mapIndexedNotNull { index, stripEntry ->
        stripEntry.item.visibleValue?.let { visibleValue ->
            VisibleStripEntry(
                entryId = stripEntry.id,
                stripIndex = index,
                value = visibleValue
            )
        }
    }

    fun visibleEntryWithId(entryId: Int): VisibleStripEntry? = entries
        .withIndex()
        .firstOrNull { indexedEntry -> indexedEntry.value.id == entryId && indexedEntry.value.item.visibleValue != null }
        ?.let { indexedEntry ->
            VisibleStripEntry(
                entryId = indexedEntry.value.id,
                stripIndex = indexedEntry.index,
                value = indexedEntry.value.item.visibleValue ?: error("Visible strip entry must expose a value.")
            )
        }

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
