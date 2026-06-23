package org.cescfe.numpairs.domain.puzzle.construction

import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem

fun Strip.withKnownEntriesOnly(knownEntryIds: Set<Int>): Strip = Strip.fromEntries(
    entries = entries.map { entry ->
        val visibleValue = requireNotNull(entry.item.maskableValue) {
            "Only visible strip entries can be masked."
        }

        entry.copy(
            item = if (entry.id in knownEntryIds) {
                StripItem.Known(visibleValue)
            } else {
                StripItem.Hidden
            }
        )
    }
)

private val StripItem.maskableValue: Int?
    get() = when (this) {
        StripItem.Hidden -> null
        is StripItem.Known -> value
        is StripItem.PlayerEntered -> value
    }
