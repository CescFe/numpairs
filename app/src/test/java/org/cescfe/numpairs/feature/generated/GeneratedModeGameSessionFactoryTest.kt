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
            puzzleProvider = {
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

    @Test
    fun every_built_in_mode_resets_sessions_through_the_shared_configuration() {
        val providerFactory = ConfiguredGeneratedPuzzleProviderFactory()

        GeneratedModes.registry.all.forEach { mode ->
            val sessionFactory = GeneratedModeGameSessionFactory(
                puzzleProvider = providerFactory.create(modeId = mode.id, seed = 42)
            )

            val firstSession = sessionFactory.create()
            val secondSession = sessionFactory.create()

            assertEquals(0, firstSession.id)
            assertEquals(1, secondSession.id)
            assertEquals(mode.profile.size.boardTileCount, secondSession.initialPuzzle.board.tiles.size)
            assertEquals(mode.profile.size.stripEntryCount, secondSession.initialPuzzle.strip.entries.size)
        }
    }
}
