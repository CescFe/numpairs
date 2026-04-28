package org.cescfe.numpairs.domain.puzzle

data class Strip(val items: List<StripItem>) {
    init {
        require(items.size == NUMBER_COUNT) {
            "Strip must contain exactly $NUMBER_COUNT items."
        }
    }

    fun validEntryRangeFor(index: Int): StripEntryRange {
        val editableRun = editableRunContaining(index)
        val minimumValue = items
            .getOrNull(editableRun.first - 1)
            ?.knownValue
            ?: 1
        val maximumValue = items
            .getOrNull(editableRun.last + 1)
            ?.knownValue

        return StripEntryRange(
            minimumValue = minimumValue,
            maximumValue = maximumValue
        )
    }

    fun withUpdatedEntry(index: Int, value: Int): Strip {
        val currentStripItem = requireEditableItemAt(index)
        val updatedItems = items.toMutableList().apply {
            set(index, currentStripItem.completeWith(value))
        }
        val editableRun = editableRunContaining(index)
        val playerEnteredIndexes = editableRun.filter { updatedItems[it] is StripItem.PlayerEntered }
        val sortedPlayerEnteredValues = playerEnteredIndexes
            .map { playerEnteredIndex -> (updatedItems[playerEnteredIndex] as StripItem.PlayerEntered).value }
            .sorted()

        playerEnteredIndexes.zip(sortedPlayerEnteredValues).forEach { (playerEnteredIndex, playerEnteredValue) ->
            updatedItems[playerEnteredIndex] = StripItem.PlayerEntered(playerEnteredValue)
        }

        return copy(items = updatedItems)
    }

    private fun editableRunContaining(index: Int): IntRange {
        requireEditableItemAt(index)

        var firstIndex = index
        while (firstIndex > 0 && items[firstIndex - 1] !is StripItem.Known) {
            firstIndex--
        }

        var lastIndex = index
        while (lastIndex < items.lastIndex && items[lastIndex + 1] !is StripItem.Known) {
            lastIndex++
        }

        return firstIndex..lastIndex
    }

    private fun requireEditableItemAt(index: Int): StripItem {
        require(index in items.indices) {
            "Strip item index must be within the strip bounds."
        }

        return items[index].also { stripItem ->
            require(stripItem !is StripItem.Known) {
                "Known strip items are not editable."
            }
        }
    }

    companion object {
        const val NUMBER_COUNT = 8
    }
}

private val StripItem.knownValue: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> null
    }
