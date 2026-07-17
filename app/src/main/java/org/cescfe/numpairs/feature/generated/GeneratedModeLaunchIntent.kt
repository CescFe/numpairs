package org.cescfe.numpairs.feature.generated

import java.util.UUID
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId

@JvmInline
value class GeneratedModeLaunchId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated mode launch id must not be blank."
        }
    }
}

sealed interface GeneratedModeLaunchIntent {
    data class NewPuzzle(val launchId: GeneratedModeLaunchId) : GeneratedModeLaunchIntent

    data class ResumeSession(val expectedSessionId: GeneratedSessionId) : GeneratedModeLaunchIntent

    companion object {
        val DefaultNewPuzzle: NewPuzzle = NewPuzzle(
            launchId = GeneratedModeLaunchId("default-new-puzzle")
        )

        fun newPuzzle(): NewPuzzle = NewPuzzle(
            launchId = GeneratedModeLaunchId(UUID.randomUUID().toString())
        )
    }
}
