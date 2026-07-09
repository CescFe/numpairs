package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.support.puzzleWithRepeatedSixes
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneratedModeGameSessionFactoryTest {
    @Test
    fun create_assigns_incrementing_session_ids_and_uses_the_next_provider_puzzle() {
        val providedPuzzles = ArrayDeque(
            listOf(
                samplePuzzle,
                puzzleWithRepeatedSixes()
            )
        )
        val factory = GeneratedModeGameSessionFactory(
            puzzleProvider = GeneratedPuzzleProvider {
                providedPuzzles.removeFirst()
            }
        )

        val firstSession = factory.create()
        val secondSession = factory.create()

        assertEquals(0, firstSession.id)
        assertEquals(samplePuzzle, firstSession.initialPuzzle)
        assertEquals(1, secondSession.id)
        assertEquals(puzzleWithRepeatedSixes(), secondSession.initialPuzzle)
    }
}
