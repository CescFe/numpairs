package org.cescfe.numpairs.data.generated.session

import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.StripItem

const val GENERATED_SESSION_SCHEMA_VERSION: Int = 1

@JvmInline
value class GeneratedSessionId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated session id must not be blank."
        }
    }
}

data class GeneratedSessionSnapshot(
    val schemaVersion: Int = GENERATED_SESSION_SCHEMA_VERSION,
    val sessionId: GeneratedSessionId,
    val modeId: String,
    val profileId: String,
    val seed: Int,
    val initialPuzzle: Puzzle,
    val currentPuzzle: Puzzle
) {
    init {
        require(schemaVersion == GENERATED_SESSION_SCHEMA_VERSION) {
            "Generated session snapshot schema version is unsupported."
        }
        require(modeId.isNotBlank()) {
            "Generated session mode id must not be blank."
        }
        require(profileId.isNotBlank()) {
            "Generated session profile id must not be blank."
        }
        require(
            initialPuzzle.board.tiles.map { tile -> tile.result } ==
                currentPuzzle.board.tiles.map { tile -> tile.result }
        ) {
            "Generated session puzzle results must remain unchanged."
        }
        require(
            initialPuzzle.strip.entries.map { entry -> entry.id }.toSet() ==
                currentPuzzle.strip.entries.map { entry -> entry.id }.toSet()
        ) {
            "Generated session strip entry identities must remain unchanged."
        }

        val currentItemsByEntryId = currentPuzzle.strip.entries.associate { entry ->
            entry.id to entry.item
        }
        initialPuzzle.strip.entries.forEach { initialEntry ->
            val initialItem = initialEntry.item
            val currentItem = currentItemsByEntryId.getValue(initialEntry.id)
            when (initialItem) {
                StripItem.Hidden -> require(currentItem !is StripItem.Known) {
                    "Hidden generated session strip entries cannot become known entries."
                }

                is StripItem.Known -> require(currentItem == initialItem) {
                    "Known generated session strip entries must remain unchanged."
                }

                is StripItem.PlayerEntered -> error(
                    "Initial generated session strip entries cannot be player-entered."
                )
            }
        }
    }
}
