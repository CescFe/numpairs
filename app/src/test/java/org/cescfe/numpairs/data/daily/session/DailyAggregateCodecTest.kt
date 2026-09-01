package org.cescfe.numpairs.data.daily.session

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.time.LocalDate
import org.cescfe.numpairs.data.puzzle.writePuzzleSnapshot
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyAggregateCodecTest {
    private val codec = DailyAggregateCodec()

    @Test
    fun aggregate_round_trip_preserves_exact_session_progress_timing_movements_and_completions() {
        val fixture = generatedDailyFixture()
        val currentPuzzle = fixture.progressPuzzle()
        val snapshot = fixture.snapshot(
            currentPuzzle = currentPuzzle,
            timingStartInstant = DailyTimingStartInstant(1_798_761_600_123),
            movementCount = DailyMovementCount(17)
        )
        val completions = listOf(
            DailyCompletion(
                identity = dailyChallengeId(date = LocalDate.of(2026, 12, 31)),
                elapsedTime = DailyElapsedTime(91_234),
                movementCount = DailyMovementCount(29)
            ),
            DailyCompletion(
                identity = dailyChallengeId(
                    date = LocalDate.of(2027, 1, 1),
                    recipeVersion = DailyRecipeVersion("retired-daily-recipe")
                ),
                elapsedTime = null,
                movementCount = null
            )
        )
        val aggregate = DailyAggregate(
            activeSession = snapshot,
            completions = completions
        )

        val decoded = codec.decode(codec.encode(aggregate))

        assertEquals(DailyAggregateDecodingResult.Decoded(aggregate), decoded)
        val decodedSnapshot = (decoded as DailyAggregateDecodingResult.Decoded).aggregate.activeSession!!
        assertEquals(currentPuzzle.board.tiles, decodedSnapshot.currentPuzzle.board.tiles)
        assertEquals(currentPuzzle.strip.entries, decodedSnapshot.currentPuzzle.strip.entries)
        assertEquals(snapshot.candidateIndex, decodedSnapshot.candidateIndex)
        assertEquals(snapshot.seed, decodedSnapshot.seed)
        assertEquals(snapshot.timingStartInstant, decodedSnapshot.timingStartInstant)
        assertEquals(snapshot.movementCount, decodedSnapshot.movementCount)
    }

    @Test
    fun aggregate_round_trip_preserves_progress_after_a_player_entered_strip_value_is_cleared() {
        val fixture = generatedDailyFixture()
        val completedPuzzle = fixture.solvedProgressPuzzle()
        val clearIndex = completedPuzzle.strip.items.indexOfFirst { item ->
            item is StripItem.PlayerEntered
        }
        val clearedPuzzle = completedPuzzle.withClearedStripEntry(index = clearIndex)
        val aggregate = DailyAggregate(
            activeSession = fixture.snapshot(currentPuzzle = clearedPuzzle)
        )

        assertEquals(
            DailyAggregateDecodingResult.Decoded(aggregate),
            codec.decode(codec.encode(aggregate))
        )
    }

    @Test
    fun encoding_is_deterministic() {
        val aggregate = DailyAggregate(
            activeSession = generatedDailyFixture().snapshot(),
            completions = listOf(
                DailyCompletion(
                    identity = dailyChallengeId(LocalDate.of(2026, 12, 31)),
                    elapsedTime = DailyElapsedTime(123_456)
                )
            )
        )

        assertTrue(codec.encode(aggregate).contentEquals(codec.encode(aggregate)))
    }

    @Test
    fun malformed_optional_session_is_discarded_while_valid_completions_are_preserved() {
        val completion = DailyCompletion(
            identity = dailyChallengeId(LocalDate.of(2026, 12, 31)),
            elapsedTime = DailyElapsedTime(123_456)
        )
        val encoded = codec.encode(
            DailyAggregate(
                activeSession = generatedDailyFixture().snapshot(),
                completions = listOf(completion)
            )
        )
        encoded[SESSION_PAYLOAD_OFFSET] = 0
        encoded[SESSION_PAYLOAD_OFFSET + 1] = 0

        val decoded = codec.decode(encoded) as DailyAggregateDecodingResult.Decoded

        assertNull(decoded.aggregate.activeSession)
        assertEquals(listOf(completion), decoded.aggregate.completions)
    }

    @Test
    fun version_one_aggregate_migrates_progress_and_history_without_fabricating_timing_or_movements() {
        val fixture = generatedDailyFixture()
        val legacySession = fixture.snapshot(currentPuzzle = fixture.progressPuzzle())
        val legacyCompletionIds = listOf(
            dailyChallengeId(LocalDate.of(2026, 12, 31)),
            dailyChallengeId(
                date = LocalDate.of(2027, 1, 1),
                recipeVersion = DailyRecipeVersion("retired-daily-recipe")
            )
        )

        val decoded = codec.decode(
            encodeInitialAggregate(
                activeSession = legacySession,
                completedChallengeIds = legacyCompletionIds
            )
        )

        assertEquals(
            DailyAggregateDecodingResult.Decoded(
                DailyAggregate(
                    activeSession = legacySession.copy(movementCount = null),
                    completions = legacyCompletionIds.map { identity ->
                        DailyCompletion(
                            identity = identity,
                            elapsedTime = null,
                            movementCount = null
                        )
                    }
                )
            ),
            decoded
        )
    }

    @Test
    fun version_two_aggregate_migrates_exact_timing_without_fabricating_movements() {
        val fixture = generatedDailyFixture()
        val timedSession = fixture.snapshot(
            currentPuzzle = fixture.progressPuzzle(),
            timingStartInstant = DailyTimingStartInstant(1_798_761_600_123)
        )
        val timedCompletion = DailyCompletion(
            identity = dailyChallengeId(LocalDate.of(2026, 12, 31)),
            elapsedTime = DailyElapsedTime(91_234)
        )
        val untimedCompletion = DailyCompletion(
            identity = dailyChallengeId(LocalDate.of(2027, 1, 1)),
            elapsedTime = null
        )

        val decoded = codec.decode(
            encodeTimedAggregate(
                activeSession = timedSession,
                completions = listOf(timedCompletion, untimedCompletion)
            )
        )

        val expected = DailyAggregateDecodingResult.Decoded(
            DailyAggregate(
                activeSession = timedSession.copy(movementCount = null),
                completions = listOf(timedCompletion, untimedCompletion)
            )
        )
        assertEquals(expected, decoded)
        assertEquals(
            expected,
            codec.decode(
                codec.encode((decoded as DailyAggregateDecodingResult.Decoded).aggregate)
            )
        )
    }

    @Test
    fun movement_count_bounds_round_trip_and_negative_persisted_values_are_rejected() {
        val fixture = generatedDailyFixture()
        val aggregate = DailyAggregate(
            activeSession = fixture.snapshot(movementCount = DailyMovementCount(Long.MAX_VALUE)),
            completions = listOf(
                DailyCompletion(
                    identity = dailyChallengeId(LocalDate.of(2026, 12, 31)),
                    elapsedTime = null,
                    movementCount = DailyMovementCount(Long.MAX_VALUE)
                )
            )
        )

        assertEquals(
            DailyAggregateDecodingResult.Decoded(aggregate),
            codec.decode(codec.encode(aggregate))
        )
        assertEquals(
            DailyAggregateDecodingResult.InvalidData,
            codec.decode(
                rawCompletionAggregateWithMovementCount(
                    identity = dailyChallengeId(LocalDate.of(2026, 12, 31)),
                    movementCount = -1
                )
            )
        )
    }

    @Test
    fun codec_reports_unsupported_schema_version() {
        val encoded = codec.encode(DailyAggregate())
        ByteBuffer.wrap(encoded).putInt(Int.SIZE_BYTES, DAILY_AGGREGATE_SCHEMA_VERSION + 1)

        assertEquals(
            DailyAggregateDecodingResult.UnsupportedVersion(DAILY_AGGREGATE_SCHEMA_VERSION + 1),
            codec.decode(encoded)
        )
    }

    @Test
    fun codec_rejects_truncated_trailing_and_impossible_payloads() {
        val encoded = codec.encode(
            DailyAggregate(activeSession = generatedDailyFixture().snapshot())
        )

        assertEquals(
            DailyAggregateDecodingResult.InvalidData,
            codec.decode(byteArrayOf(1, 2, 3))
        )
        assertEquals(
            DailyAggregateDecodingResult.InvalidData,
            codec.decode(encoded.copyOf(encoded.size - 1))
        )
        assertEquals(
            DailyAggregateDecodingResult.InvalidData,
            codec.decode(encoded + 1)
        )
        assertEquals(
            DailyAggregateDecodingResult.InvalidData,
            codec.decode(rawAggregateHeader(hasSession = true, followingValue = 0))
        )
        assertEquals(
            DailyAggregateDecodingResult.InvalidData,
            codec.decode(rawAggregateHeader(hasSession = false, followingValue = MAX_DAILY_COMPLETION_COUNT + 1))
        )
    }

    @Test
    fun codec_rejects_duplicate_and_same_date_completion_records() {
        val date = LocalDate.of(2027, 4, 18)

        assertEquals(
            DailyAggregateDecodingResult.InvalidData,
            codec.decode(
                rawCompletionAggregate(
                    dailyChallengeId(date),
                    dailyChallengeId(date)
                )
            )
        )
        assertEquals(
            DailyAggregateDecodingResult.InvalidData,
            codec.decode(
                rawCompletionAggregate(
                    dailyChallengeId(date),
                    dailyChallengeId(
                        date = date,
                        recipeVersion = DailyRecipeVersion("another-recipe")
                    )
                )
            )
        )
    }

    @Test
    fun aggregate_rejects_duplicate_noncanonical_and_active_date_completion_state() {
        val first = dailyChallengeId(LocalDate.of(2027, 4, 17))
        val second = dailyChallengeId(LocalDate.of(2027, 4, 18))
        val activeSession = generatedDailyFixture(date = second.localDate).snapshot()

        assertThrows(IllegalArgumentException::class.java) {
            DailyAggregate(completions = listOf(first, first).map(::untimedCompletion))
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyAggregate(completions = listOf(second, first).map(::untimedCompletion))
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyAggregate(
                activeSession = activeSession,
                completions = listOf(untimedCompletion(second))
            )
        }
    }

    @Test
    fun snapshot_rejects_recipe_seed_shape_and_progress_mismatches() {
        val fixture = generatedDailyFixture()
        val validSnapshot = fixture.snapshot()
        val threePairsPuzzle = generatedPuzzleFixture(
            profile = GeneratedPuzzleProfiles.THREE_PAIRS_LOW,
            seed = 42
        ).initialPuzzle
        val changedResultsPuzzle = validSnapshot.currentPuzzle.copy(
            board = Board(
                tiles = validSnapshot.currentPuzzle.board.tiles.mapIndexed { index, tile ->
                    if (index == 0) tile.copy(result = tile.result + 1) else tile
                }
            )
        )
        val removedKnownEntryPuzzle = validSnapshot.currentPuzzle.copy(
            strip = Strip.fromEntries(
                validSnapshot.currentPuzzle.strip.entries.map { entry ->
                    if (entry.item is StripItem.Known) {
                        entry.copy(item = StripItem.Hidden)
                    } else {
                        entry
                    }
                }
            )
        )
        val changedEntryIdentityPuzzle = validSnapshot.currentPuzzle.copy(
            strip = Strip.fromEntries(
                validSnapshot.currentPuzzle.strip.entries.mapIndexed { index, entry ->
                    if (index == 0) {
                        entry.copy(id = validSnapshot.currentPuzzle.strip.entries.size)
                    } else {
                        entry
                    }
                }
            )
        )

        assertRejectedByValidation {
            validSnapshot.copy(seed = validSnapshot.seed + 1)
        }
        assertRejectedByValidation {
            validSnapshot.copy(candidateIndex = DailyCandidateIndex(4))
        }
        assertRejectedByValidation {
            validSnapshot.copy(
                dailyChallengeId = validSnapshot.dailyChallengeId.copy(
                    recipeVersion = DailyRecipeVersion("unsupported-recipe")
                )
            )
        }
        assertRejectedByValidation {
            validSnapshot.copy(
                initialPuzzle = threePairsPuzzle,
                currentPuzzle = threePairsPuzzle
            )
        }
        assertRejectedByValidation {
            validSnapshot.copy(currentPuzzle = changedResultsPuzzle)
        }
        assertRejectedByValidation {
            validSnapshot.copy(currentPuzzle = removedKnownEntryPuzzle)
        }
        assertRejectedByValidation {
            validSnapshot.copy(currentPuzzle = changedEntryIdentityPuzzle)
        }
        assertRejectedByValidation {
            validSnapshot.copy(currentPuzzle = fixture.generatedPuzzle.solvedPuzzle)
        }
    }

    @Test
    fun snapshot_rejects_player_entered_initial_state_and_invalid_current_assignments() {
        val fixture = generatedDailyFixture()
        val snapshot = fixture.snapshot()
        val hiddenEntry = snapshot.initialPuzzle.strip.entries.first { entry ->
            entry.item == StripItem.Hidden
        }
        val solvedValue = (
            fixture.generatedPuzzle.solvedPuzzle.strip.entries
                .single { entry -> entry.id == hiddenEntry.id }
                .item as StripItem.Known
            ).value
        val playerEnteredInitial = snapshot.initialPuzzle.copy(
            strip = Strip.fromEntries(
                snapshot.initialPuzzle.strip.entries.map { entry ->
                    if (entry.id == hiddenEntry.id) {
                        entry.copy(item = StripItem.PlayerEntered(solvedValue))
                    } else {
                        entry
                    }
                }
            )
        )
        val invalidAssignment = snapshot.currentPuzzle.copy(
            board = Board(
                tiles = snapshot.currentPuzzle.board.tiles.mapIndexed { index, tile ->
                    if (index == 0) {
                        tile.copy(
                            expression = tile.expression.copy(
                                leftOperand = Expression.Operand.Known(value = solvedValue)
                            )
                        )
                    } else {
                        tile
                    }
                }
            )
        )

        assertRejectedByValidation {
            snapshot.copy(
                initialPuzzle = playerEnteredInitial,
                currentPuzzle = playerEnteredInitial
            )
        }
        assertRejectedByValidation {
            snapshot.copy(currentPuzzle = invalidAssignment)
        }
    }
}

private fun <T> assertRejectedByValidation(block: () -> T) {
    assertThrows(IllegalArgumentException::class.java) {
        val unexpectedResult = block()
        throw AssertionError("Expected validation to reject $unexpectedResult")
    }
}

private fun rawAggregateHeader(hasSession: Boolean, followingValue: Int): ByteArray =
    ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(DAILY_AGGREGATE_FILE_MAGIC)
            output.writeInt(DAILY_AGGREGATE_SCHEMA_VERSION)
            output.writeBoolean(hasSession)
            output.writeInt(followingValue)
        }
        bytes.toByteArray()
    }

private fun rawCompletionAggregate(vararg completions: DailyChallengeId): ByteArray =
    ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(DAILY_AGGREGATE_FILE_MAGIC)
            output.writeInt(DAILY_AGGREGATE_SCHEMA_VERSION)
            output.writeBoolean(false)
            output.writeInt(completions.size)
            completions.forEach { identity ->
                output.writeUTF(identity.canonicalLocalDate)
                output.writeUTF(identity.recipeVersion.value)
                output.writeBoolean(false)
                output.writeBoolean(false)
            }
        }
        bytes.toByteArray()
    }

private fun encodeInitialAggregate(
    activeSession: DailySessionSnapshot?,
    completedChallengeIds: List<DailyChallengeId>
): ByteArray = ByteArrayOutputStream().use { bytes ->
    DataOutputStream(bytes).use { output ->
        output.writeInt(DAILY_AGGREGATE_FILE_MAGIC)
        output.writeInt(INITIAL_DAILY_AGGREGATE_SCHEMA_VERSION)
        output.writeBoolean(activeSession != null)
        activeSession?.let { snapshot ->
            val sessionPayload = encodeInitialSession(snapshot)
            output.writeInt(sessionPayload.size)
            output.write(sessionPayload)
        }
        output.writeInt(completedChallengeIds.size)
        completedChallengeIds.forEach { identity ->
            output.writeUTF(identity.canonicalLocalDate)
            output.writeUTF(identity.recipeVersion.value)
        }
    }
    bytes.toByteArray()
}

private fun encodeInitialSession(snapshot: DailySessionSnapshot): ByteArray = ByteArrayOutputStream().use { bytes ->
    DataOutputStream(bytes).use { output ->
        output.writeUTF(snapshot.sessionId.value)
        output.writeUTF(snapshot.dailyChallengeId.canonicalLocalDate)
        output.writeUTF(snapshot.dailyChallengeId.recipeVersion.value)
        output.writeInt(snapshot.candidateIndex.value)
        output.writeInt(snapshot.seed)
        output.writePuzzleSnapshot(snapshot.initialPuzzle)
        output.writePuzzleSnapshot(snapshot.currentPuzzle)
    }
    bytes.toByteArray()
}

private fun encodeTimedAggregate(activeSession: DailySessionSnapshot?, completions: List<DailyCompletion>): ByteArray =
    ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(DAILY_AGGREGATE_FILE_MAGIC)
            output.writeInt(TIMED_DAILY_AGGREGATE_SCHEMA_VERSION)
            output.writeBoolean(activeSession != null)
            activeSession?.let { snapshot ->
                val sessionPayload = encodeTimedSession(snapshot)
                output.writeInt(sessionPayload.size)
                output.write(sessionPayload)
            }
            output.writeInt(completions.size)
            completions.forEach { completion ->
                output.writeUTF(completion.identity.canonicalLocalDate)
                output.writeUTF(completion.identity.recipeVersion.value)
                output.writeBoolean(completion.elapsedTime != null)
                completion.elapsedTime?.let { elapsedTime ->
                    output.writeLong(elapsedTime.milliseconds)
                }
            }
        }
        bytes.toByteArray()
    }

private fun encodeTimedSession(snapshot: DailySessionSnapshot): ByteArray = ByteArrayOutputStream().use { bytes ->
    DataOutputStream(bytes).use { output ->
        output.writeUTF(snapshot.sessionId.value)
        output.writeUTF(snapshot.dailyChallengeId.canonicalLocalDate)
        output.writeUTF(snapshot.dailyChallengeId.recipeVersion.value)
        output.writeInt(snapshot.candidateIndex.value)
        output.writeInt(snapshot.seed)
        output.writePuzzleSnapshot(snapshot.initialPuzzle)
        output.writePuzzleSnapshot(snapshot.currentPuzzle)
        output.writeBoolean(snapshot.timingStartInstant != null)
        snapshot.timingStartInstant?.let { startInstant ->
            output.writeLong(startInstant.epochMilliseconds)
        }
    }
    bytes.toByteArray()
}

private fun rawCompletionAggregateWithMovementCount(identity: DailyChallengeId, movementCount: Long): ByteArray =
    ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeInt(DAILY_AGGREGATE_FILE_MAGIC)
            output.writeInt(DAILY_AGGREGATE_SCHEMA_VERSION)
            output.writeBoolean(false)
            output.writeInt(1)
            output.writeUTF(identity.canonicalLocalDate)
            output.writeUTF(identity.recipeVersion.value)
            output.writeBoolean(false)
            output.writeBoolean(true)
            output.writeLong(movementCount)
        }
        bytes.toByteArray()
    }

private fun untimedCompletion(identity: DailyChallengeId): DailyCompletion = DailyCompletion(
    identity = identity,
    elapsedTime = null
)

private const val DAILY_AGGREGATE_FILE_MAGIC = 0x4E504441
private const val SESSION_PAYLOAD_OFFSET = 13
