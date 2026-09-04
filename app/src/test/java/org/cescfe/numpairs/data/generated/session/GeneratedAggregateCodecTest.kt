package org.cescfe.numpairs.data.generated.session

import java.nio.ByteBuffer
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedAggregateCodecTest {
    private val codec = GeneratedAggregateCodec()

    @Test
    fun `round trips an active timed session and all six independent bests`() {
        val aggregate = GeneratedAggregate(
            activeSession = snapshot(),
            personalBests = GeneratedPersonalBestCategory.entries.associateWith { category ->
                GeneratedElapsedTime(40_000L + category.ordinal)
            }
        )

        assertEquals(
            GeneratedAggregateDecodingResult.Decoded(aggregate),
            codec.decode(codec.encode(aggregate))
        )
    }

    @Test
    fun `legacy snapshot migrates without losing session identity progress timing corrections or seed`() {
        val legacySnapshot = snapshot()

        assertEquals(
            GeneratedAggregateDecodingResult.Decoded(
                GeneratedAggregate(activeSession = legacySnapshot)
            ),
            codec.decode(GeneratedSessionSnapshotCodec().encode(legacySnapshot))
        )
    }

    @Test
    fun `reports unsupported malformed and trailing aggregate data`() {
        val encoded = codec.encode(GeneratedAggregate())
        ByteBuffer.wrap(encoded).putInt(Int.SIZE_BYTES, GENERATED_AGGREGATE_SCHEMA_VERSION + 1)
        assertEquals(
            GeneratedAggregateDecodingResult.UnsupportedVersion(GENERATED_AGGREGATE_SCHEMA_VERSION + 1),
            codec.decode(encoded)
        )
        assertEquals(
            GeneratedAggregateDecodingResult.InvalidData,
            codec.decode(byteArrayOf(1, 2, 3))
        )
        assertEquals(
            GeneratedAggregateDecodingResult.InvalidData,
            codec.decode(codec.encode(GeneratedAggregate()) + 1)
        )
    }

    @Test
    fun `encoding is deterministic regardless of best map insertion order`() {
        val forward = GeneratedAggregate(
            personalBests = GeneratedPersonalBestCategory.entries.associateWith { category ->
                GeneratedElapsedTime(category.ordinal.toLong())
            }
        )
        val reverse = GeneratedAggregate(
            personalBests = GeneratedPersonalBestCategory.entries.reversed().associateWith { category ->
                GeneratedElapsedTime(category.ordinal.toLong())
            }
        )

        assertTrue(codec.encode(forward).contentEquals(codec.encode(reverse)))
    }

    private fun snapshot(): GeneratedSessionSnapshot = GeneratedSessionSnapshot(
        sessionId = GeneratedSessionId("legacy-session"),
        modeId = "four-pairs",
        profileId = "4-pairs-low",
        seed = 717,
        initialPuzzle = samplePuzzle,
        currentPuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 1)
        ),
        correctionCount = PuzzleCorrectionCount(3),
        timingStartInstant = GeneratedTimingStartInstant(1_700_000_000_000)
    )
}
