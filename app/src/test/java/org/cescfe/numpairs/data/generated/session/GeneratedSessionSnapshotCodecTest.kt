package org.cescfe.numpairs.data.generated.session

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import org.cescfe.numpairs.data.puzzle.writePuzzleSnapshot
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.puzzle.GeneratedPairsPuzzle
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripEntry
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedSessionSnapshotCodecTest {
    private val codec = GeneratedSessionSnapshotCodec()

    @Test
    fun `round trips representative four pairs session`() {
        val puzzle = generatedInitialPuzzle(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            seed = 207
        )
        val snapshot = snapshot(
            modeId = "four-pairs",
            profileId = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW.id.value,
            seed = 207,
            initialPuzzle = puzzle,
            currentPuzzle = puzzle,
            correctionCount = PuzzleCorrectionCount(17)
        )

        assertEquals(
            GeneratedSessionSnapshotDecodingResult.Decoded(snapshot),
            codec.decode(codec.encode(snapshot))
        )
    }

    @Test
    fun `round trips representative eight pairs session`() {
        val puzzle = generatedInitialPuzzle(
            profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
            seed = 208
        )
        val snapshot = snapshot(
            modeId = "eight-pairs",
            profileId = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM.id.value,
            seed = 208,
            initialPuzzle = puzzle,
            currentPuzzle = puzzle
        )

        assertEquals(
            GeneratedSessionSnapshotDecodingResult.Decoded(snapshot),
            codec.decode(codec.encode(snapshot))
        )
    }

    @Test
    fun `round trips timing start and frozen millisecond completion duration`() {
        val puzzle = generatedPuzzle(
            profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            seed = 211
        )
        val completedPuzzle = puzzle.solvedPuzzle.copy(
            strip = Strip.fromEntries(
                puzzle.solvedPuzzle.strip.entries.mapIndexed { index, entry ->
                    if (puzzle.initialPuzzle.strip.entries[index].item == StripItem.Hidden) {
                        entry.copy(
                            item = StripItem.PlayerEntered((entry.item as StripItem.Known).value)
                        )
                    } else {
                        entry
                    }
                }
            )
        )
        val snapshot = snapshot(
            initialPuzzle = puzzle.initialPuzzle,
            currentPuzzle = completedPuzzle,
            timingStartInstant = GeneratedTimingStartInstant(1_700_000_000_123),
            completionElapsedTime = GeneratedElapsedTime(125_999)
        )

        assertEquals(
            GeneratedSessionSnapshotDecodingResult.Decoded(snapshot),
            codec.decode(codec.encode(snapshot))
        )
    }

    @Test
    fun `preserves repeated values item origins and stable operand identities`() {
        val initialPuzzle = repeatedValuePuzzle(
            strip = Strip.fromEntries(
                entries = listOf(
                    StripEntry(id = 0, item = StripItem.Known(2)),
                    StripEntry(id = 1, item = StripItem.Hidden),
                    StripEntry(id = 2, item = StripItem.Known(3)),
                    StripEntry(id = 3, item = StripItem.Hidden)
                )
            ),
            firstExpression = hiddenExpression()
        )
        val currentPuzzle = repeatedValuePuzzle(
            strip = Strip.fromEntries(
                entries = listOf(
                    StripEntry(id = 0, item = StripItem.Known(2)),
                    StripEntry(id = 1, item = StripItem.PlayerEntered(2)),
                    StripEntry(id = 2, item = StripItem.Known(3)),
                    StripEntry(id = 3, item = StripItem.PlayerEntered(4))
                )
            ),
            firstExpression = Expression(
                leftOperand = Expression.Operand.Known(value = 2, stripEntryId = 0),
                operator = Operator.ADDITION,
                rightOperand = Expression.Operand.Known(value = 2, stripEntryId = 1)
            )
        )
        val snapshot = snapshot(
            initialPuzzle = initialPuzzle,
            currentPuzzle = currentPuzzle
        )

        val decoded = codec.decode(codec.encode(snapshot))

        assertEquals(GeneratedSessionSnapshotDecodingResult.Decoded(snapshot), decoded)
    }

    @Test
    fun `round trips progress after a player-entered strip value is cleared`() {
        val initialPuzzle = repeatedValuePuzzle()
        val enteredPuzzle = repeatedValuePuzzle(
            strip = Strip.fromEntries(
                entries = listOf(
                    StripEntry(id = 0, item = StripItem.Known(2)),
                    StripEntry(id = 1, item = StripItem.PlayerEntered(2)),
                    StripEntry(id = 2, item = StripItem.Known(3)),
                    StripEntry(id = 3, item = StripItem.PlayerEntered(4))
                )
            ),
            firstExpression = Expression(
                leftOperand = Expression.Operand.Known(value = 2, stripEntryId = 0),
                operator = Operator.ADDITION,
                rightOperand = Expression.Operand.Known(value = 2, stripEntryId = 1)
            )
        )
        val clearedPuzzle = enteredPuzzle.withClearedStripEntry(index = 1)
        val snapshot = snapshot(
            initialPuzzle = initialPuzzle,
            currentPuzzle = clearedPuzzle
        )

        assertEquals(
            GeneratedSessionSnapshotDecodingResult.Decoded(snapshot),
            codec.decode(codec.encode(snapshot))
        )
    }

    @Test
    fun `reports unsupported schema version`() {
        val encoded = codec.encode(snapshot())
        ByteBuffer.wrap(encoded).putInt(Int.SIZE_BYTES, GENERATED_SESSION_SCHEMA_VERSION + 1)

        assertEquals(
            GeneratedSessionSnapshotDecodingResult.UnsupportedVersion(
                GENERATED_SESSION_SCHEMA_VERSION + 1
            ),
            codec.decode(encoded)
        )
    }

    @Test
    fun `version one session migrates without fabricating a correction count`() {
        val legacySnapshot = snapshot(correctionCount = null)

        val decoded = codec.decode(encodeInitialSnapshot(legacySnapshot))

        assertEquals(
            GeneratedSessionSnapshotDecodingResult.Decoded(legacySnapshot),
            decoded
        )
        assertEquals(
            GeneratedSessionSnapshotDecodingResult.Decoded(legacySnapshot),
            codec.decode(
                codec.encode(
                    (decoded as GeneratedSessionSnapshotDecodingResult.Decoded).snapshot
                )
            )
        )
    }

    @Test
    fun `version two attempt metrics migrate without fabricating timing`() {
        val legacySnapshot = snapshot(correctionCount = PuzzleCorrectionCount(4))

        val decoded = codec.decode(encodeAttemptMetricsSnapshot(legacySnapshot))

        assertEquals(
            GeneratedSessionSnapshotDecodingResult.Decoded(legacySnapshot),
            decoded
        )
        val migrated = (decoded as GeneratedSessionSnapshotDecodingResult.Decoded).snapshot
        assertEquals(4L, requireNotNull(migrated.correctionCount).value)
        assertEquals(null, migrated.timingStartInstant)
        assertEquals(null, migrated.completionElapsedTime)
    }

    @Test
    fun `reports malformed and invariant breaking data`() {
        assertEquals(
            GeneratedSessionSnapshotDecodingResult.InvalidData,
            codec.decode(byteArrayOf(1, 2, 3))
        )

        val encoded = codec.encode(snapshot())
        val truncated = encoded.copyOf(encoded.size - 1)
        assertEquals(
            GeneratedSessionSnapshotDecodingResult.InvalidData,
            codec.decode(truncated)
        )

        val trailingData = encoded + 1
        assertEquals(
            GeneratedSessionSnapshotDecodingResult.InvalidData,
            codec.decode(trailingData)
        )
    }

    @Test
    fun `encoding is deterministic`() {
        val snapshot = snapshot()

        assertTrue(codec.encode(snapshot).contentEquals(codec.encode(snapshot)))
    }

    private fun snapshot(
        modeId: String = "four-pairs",
        profileId: String = "4-pairs-low",
        seed: Int = 207,
        initialPuzzle: Puzzle = repeatedValuePuzzle(),
        currentPuzzle: Puzzle = initialPuzzle,
        correctionCount: PuzzleCorrectionCount? = PuzzleCorrectionCount.ZERO,
        timingStartInstant: GeneratedTimingStartInstant? = null,
        completionElapsedTime: GeneratedElapsedTime? = null
    ): GeneratedSessionSnapshot = GeneratedSessionSnapshot(
        sessionId = GeneratedSessionId("session-207"),
        modeId = modeId,
        profileId = profileId,
        seed = seed,
        initialPuzzle = initialPuzzle,
        currentPuzzle = currentPuzzle,
        correctionCount = correctionCount,
        timingStartInstant = timingStartInstant,
        completionElapsedTime = completionElapsedTime
    )

    private fun generatedInitialPuzzle(profile: GeneratedPuzzleProfile, seed: Int): Puzzle =
        generatedPuzzle(profile = profile, seed = seed).initialPuzzle

    private fun generatedPuzzle(profile: GeneratedPuzzleProfile, seed: Int): GeneratedPairsPuzzle {
        val outcome = GeneratedPairsPuzzleGenerator(profile = profile).generate(
            request = GeneratedPuzzleGenerationRequest(
                profile = profile,
                seed = seed
            )
        )

        return (outcome as GeneratedPairsPuzzleGenerationOutcome.Generated).puzzle
    }

    private fun repeatedValuePuzzle(
        strip: Strip = Strip.fromEntries(
            entries = listOf(
                StripEntry(id = 0, item = StripItem.Known(2)),
                StripEntry(id = 1, item = StripItem.Hidden),
                StripEntry(id = 2, item = StripItem.Known(3)),
                StripEntry(id = 3, item = StripItem.Hidden)
            )
        ),
        firstExpression: Expression = hiddenExpression()
    ): Puzzle = Puzzle(
        board = Board(
            tiles = listOf(
                Tile(expression = firstExpression, result = 4),
                Tile(expression = hiddenExpression(), result = 4),
                Tile(expression = hiddenExpression(), result = 5),
                Tile(expression = hiddenExpression(), result = 6)
            )
        ),
        strip = strip
    )

    private fun hiddenExpression(): Expression = Expression(
        leftOperand = Expression.Operand.Hidden,
        operator = Operator.Hidden,
        rightOperand = Expression.Operand.Hidden
    )
}

private fun encodeInitialSnapshot(snapshot: GeneratedSessionSnapshot): ByteArray =
    ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(GENERATED_SESSION_FILE_MAGIC)
            output.writeInt(INITIAL_GENERATED_SESSION_SCHEMA_VERSION)
            output.writeUTF(snapshot.sessionId.value)
            output.writeUTF(snapshot.modeId)
            output.writeUTF(snapshot.profileId)
            output.writeInt(snapshot.seed)
            output.writePuzzleSnapshot(snapshot.initialPuzzle)
            output.writePuzzleSnapshot(snapshot.currentPuzzle)
        }
        bytes.toByteArray()
    }

private fun encodeAttemptMetricsSnapshot(snapshot: GeneratedSessionSnapshot): ByteArray =
    ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(GENERATED_SESSION_FILE_MAGIC)
            output.writeInt(GENERATED_SESSION_ATTEMPT_METRICS_SCHEMA_VERSION)
            output.writeUTF(snapshot.sessionId.value)
            output.writeUTF(snapshot.modeId)
            output.writeUTF(snapshot.profileId)
            output.writeInt(snapshot.seed)
            output.writePuzzleSnapshot(snapshot.initialPuzzle)
            output.writePuzzleSnapshot(snapshot.currentPuzzle)
            output.writeBoolean(snapshot.correctionCount != null)
            snapshot.correctionCount?.let { correctionCount ->
                output.writeLong(correctionCount.value)
            }
        }
        bytes.toByteArray()
    }

private const val GENERATED_SESSION_FILE_MAGIC = 0x4E505331
