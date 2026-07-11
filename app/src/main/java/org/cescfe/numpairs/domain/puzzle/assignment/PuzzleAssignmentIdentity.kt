package org.cescfe.numpairs.domain.puzzle.assignment

@JvmInline
value class StripEntryId(val value: Int) : Comparable<StripEntryId> {
    init {
        require(value >= 0) {
            "Strip entry id must be non-negative."
        }
    }

    override fun compareTo(other: StripEntryId): Int = value.compareTo(other.value)
}

@ConsistentCopyVisibility
data class UnorderedStripEntryPair private constructor(
    val firstEntryId: StripEntryId,
    val secondEntryId: StripEntryId
) {
    companion object {
        fun of(firstEntryId: StripEntryId, secondEntryId: StripEntryId): UnorderedStripEntryPair =
            UnorderedStripEntryPair(
                firstEntryId = minOf(firstEntryId, secondEntryId),
                secondEntryId = maxOf(firstEntryId, secondEntryId)
            )
    }
}
