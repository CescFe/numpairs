package org.cescfe.numpairs.data.daily.session

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.time.LocalDate
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
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
    fun aggregate_round_trip_preserves_exact_session_progress_and_completion_identity() {
        val fixture = generatedDailyFixture()
        val currentPuzzle = fixture.progressPuzzle()
        val snapshot = fixture.snapshot(currentPuzzle = currentPuzzle)
        val completions = listOf(
            dailyChallengeId(date = LocalDate.of(2026, 12, 31)),
            dailyChallengeId(
                date = LocalDate.of(2027, 1, 1),
                recipeVersion = DailyRecipeVersion("retired-daily-recipe")
            )
        )
        val aggregate = DailyAggregate(
            activeSession = snapshot,
            completedChallengeIds = completions
        )

        val decoded = codec.decode(codec.encode(aggregate))

        assertEquals(DailyAggregateDecodingResult.Decoded(aggregate), decoded)
        val decodedSnapshot = (decoded as DailyAggregateDecodingResult.Decoded).aggregate.activeSession!!
        assertEquals(currentPuzzle.board.tiles, decodedSnapshot.currentPuzzle.board.tiles)
        assertEquals(currentPuzzle.strip.entries, decodedSnapshot.currentPuzzle.strip.entries)
        assertEquals(snapshot.candidateIndex, decodedSnapshot.candidateIndex)
        assertEquals(snapshot.seed, decodedSnapshot.seed)
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
            completedChallengeIds = listOf(dailyChallengeId(LocalDate.of(2026, 12, 31)))
        )

        assertTrue(codec.encode(aggregate).contentEquals(codec.encode(aggregate)))
    }

    @Test
    fun malformed_optional_session_is_discarded_while_valid_completions_are_preserved() {
        val completion = dailyChallengeId(LocalDate.of(2026, 12, 31))
        val encoded = codec.encode(
            DailyAggregate(
                activeSession = generatedDailyFixture().snapshot(),
                completedChallengeIds = listOf(completion)
            )
        )
        encoded[SESSION_PAYLOAD_OFFSET] = 0
        encoded[SESSION_PAYLOAD_OFFSET + 1] = 0

        val decoded = codec.decode(encoded) as DailyAggregateDecodingResult.Decoded

        assertNull(decoded.aggregate.activeSession)
        assertEquals(listOf(completion), decoded.aggregate.completedChallengeIds)
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
            DailyAggregate(completedChallengeIds = listOf(first, first))
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyAggregate(completedChallengeIds = listOf(second, first))
        }
        assertThrows(IllegalArgumentException::class.java) {
            DailyAggregate(
                activeSession = activeSession,
                completedChallengeIds = listOf(second)
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

        assertThrows(IllegalArgumentException::class.java) {
            validSnapshot.copy(seed = validSnapshot.seed + 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            validSnapshot.copy(candidateIndex = DailyCandidateIndex(4))
        }
        assertThrows(IllegalArgumentException::class.java) {
            validSnapshot.copy(
                dailyChallengeId = validSnapshot.dailyChallengeId.copy(
                    recipeVersion = DailyRecipeVersion("unsupported-recipe")
                )
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            validSnapshot.copy(
                initialPuzzle = threePairsPuzzle,
                currentPuzzle = threePairsPuzzle
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            validSnapshot.copy(currentPuzzle = changedResultsPuzzle)
        }
        assertThrows(IllegalArgumentException::class.java) {
            validSnapshot.copy(currentPuzzle = removedKnownEntryPuzzle)
        }
        assertThrows(IllegalArgumentException::class.java) {
            validSnapshot.copy(currentPuzzle = changedEntryIdentityPuzzle)
        }
        assertThrows(IllegalArgumentException::class.java) {
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

        assertThrows(IllegalArgumentException::class.java) {
            snapshot.copy(
                initialPuzzle = playerEnteredInitial,
                currentPuzzle = playerEnteredInitial
            )
        }
        assertThrows(IllegalArgumentException::class.java) {
            snapshot.copy(currentPuzzle = invalidAssignment)
        }
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
            }
        }
        bytes.toByteArray()
    }

private const val DAILY_AGGREGATE_FILE_MAGIC = 0x4E504441
private const val SESSION_PAYLOAD_OFFSET = 13
