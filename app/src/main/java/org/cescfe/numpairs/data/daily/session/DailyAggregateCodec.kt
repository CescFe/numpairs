package org.cescfe.numpairs.data.daily.session

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.time.DateTimeException
import java.time.LocalDate
import org.cescfe.numpairs.data.puzzle.readPuzzleSnapshot
import org.cescfe.numpairs.data.puzzle.writePuzzleSnapshot
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant

sealed interface DailyAggregateDecodingResult {
    data class Decoded(val aggregate: DailyAggregate) : DailyAggregateDecodingResult

    data class UnsupportedVersion(val schemaVersion: Int) : DailyAggregateDecodingResult

    data object InvalidData : DailyAggregateDecodingResult
}

class DailyAggregateCodec {
    fun encode(aggregate: DailyAggregate): ByteArray {
        require(aggregate.schemaVersion == DAILY_AGGREGATE_SCHEMA_VERSION) {
            "Only the current Daily aggregate schema can be encoded."
        }

        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(FILE_MAGIC)
                output.writeInt(aggregate.schemaVersion)
                output.writeBoolean(aggregate.activeSession != null)
                aggregate.activeSession?.let { activeSession ->
                    val encodedSession = encodeSession(activeSession)
                    require(encodedSession.size <= MAX_SESSION_PAYLOAD_SIZE) {
                        "Daily Session payload exceeds the supported bound."
                    }
                    output.writeInt(encodedSession.size)
                    output.write(encodedSession)
                }
                output.writeInt(aggregate.completions.size)
                aggregate.completions.forEach(output::writeDailyCompletion)
            }
            bytes.toByteArray()
        }
    }

    fun decode(bytes: ByteArray): DailyAggregateDecodingResult = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            if (input.readInt() != FILE_MAGIC) {
                return DailyAggregateDecodingResult.InvalidData
            }

            val schemaVersion = input.readInt()
            when (schemaVersion) {
                LEGACY_DAILY_AGGREGATE_SCHEMA_VERSION -> decodeLegacyAggregate(input)
                DAILY_AGGREGATE_SCHEMA_VERSION -> decodeCurrentAggregate(input)
                else -> DailyAggregateDecodingResult.UnsupportedVersion(schemaVersion)
            }
        }
    } catch (_: IOException) {
        DailyAggregateDecodingResult.InvalidData
    } catch (_: DateTimeException) {
        DailyAggregateDecodingResult.InvalidData
    } catch (_: IllegalArgumentException) {
        DailyAggregateDecodingResult.InvalidData
    } catch (_: IllegalStateException) {
        DailyAggregateDecodingResult.InvalidData
    }

    private fun encodeSession(snapshot: DailySessionSnapshot): ByteArray = ByteArrayOutputStream().use { bytes ->
        DataOutputStream(bytes).use { output ->
            output.writeUTF(snapshot.sessionId.value)
            output.writeDailyChallengeId(snapshot.dailyChallengeId)
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

    private fun decodeLegacyAggregate(input: DataInputStream): DailyAggregateDecodingResult = decodeAggregate(
        input = input,
        decodeSession = ::decodeLegacySessionOrNull,
        readCompletion = {
            DailyCompletion(
                identity = readDailyChallengeId(),
                elapsedTime = null
            )
        }
    )

    private fun decodeCurrentAggregate(input: DataInputStream): DailyAggregateDecodingResult = decodeAggregate(
        input = input,
        decodeSession = ::decodeCurrentSessionOrNull,
        readCompletion = DataInputStream::readDailyCompletion
    )

    private fun decodeAggregate(
        input: DataInputStream,
        decodeSession: (ByteArray) -> DailySessionSnapshot?,
        readCompletion: DataInputStream.() -> DailyCompletion
    ): DailyAggregateDecodingResult {
        val activeSession = if (input.readBoolean()) {
            val sessionPayloadSize = input.readInt()
            if (sessionPayloadSize !in 1..MAX_SESSION_PAYLOAD_SIZE) {
                return DailyAggregateDecodingResult.InvalidData
            }
            val sessionPayload = ByteArray(sessionPayloadSize)
            input.readFully(sessionPayload)
            decodeSession(sessionPayload)
        } else {
            null
        }
        val completionCount = input.readInt()
        if (completionCount !in 0..MAX_DAILY_COMPLETION_COUNT) {
            return DailyAggregateDecodingResult.InvalidData
        }
        val completions = List(completionCount) {
            input.readCompletion()
        }

        return if (input.available() != 0) {
            DailyAggregateDecodingResult.InvalidData
        } else {
            DailyAggregateDecodingResult.Decoded(
                DailyAggregate(
                    activeSession = activeSession,
                    completions = completions
                )
            )
        }
    }

    private fun decodeLegacySessionOrNull(bytes: ByteArray): DailySessionSnapshot? = decodeSessionOrNull(
        bytes = bytes,
        readTimingStartInstant = { null }
    )

    private fun decodeCurrentSessionOrNull(bytes: ByteArray): DailySessionSnapshot? = decodeSessionOrNull(
        bytes = bytes,
        readTimingStartInstant = {
            if (readBoolean()) {
                DailyTimingStartInstant(readLong())
            } else {
                null
            }
        }
    )

    private fun decodeSessionOrNull(
        bytes: ByteArray,
        readTimingStartInstant: DataInputStream.() -> DailyTimingStartInstant?
    ): DailySessionSnapshot? = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val snapshot = DailySessionSnapshot(
                sessionId = DailySessionId(input.readUTF()),
                dailyChallengeId = input.readDailyChallengeId(),
                candidateIndex = DailyCandidateIndex(input.readInt()),
                seed = input.readInt(),
                initialPuzzle = input.readPuzzleSnapshot(),
                currentPuzzle = input.readPuzzleSnapshot(),
                timingStartInstant = input.readTimingStartInstant()
            )
            snapshot.takeIf { input.available() == 0 }
        }
    } catch (_: IOException) {
        null
    } catch (_: DateTimeException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IllegalStateException) {
        null
    }
}

private fun DataOutputStream.writeDailyChallengeId(identity: DailyChallengeId) {
    writeUTF(identity.canonicalLocalDate)
    writeUTF(identity.recipeVersion.value)
}

private fun DataInputStream.readDailyChallengeId(): DailyChallengeId = DailyChallengeId(
    localDate = LocalDate.parse(readUTF()),
    recipeVersion = DailyRecipeVersion(readUTF())
)

private fun DataOutputStream.writeDailyCompletion(completion: DailyCompletion) {
    writeDailyChallengeId(completion.identity)
    writeBoolean(completion.elapsedTime != null)
    completion.elapsedTime?.let { elapsedTime ->
        writeLong(elapsedTime.milliseconds)
    }
}

private fun DataInputStream.readDailyCompletion(): DailyCompletion = DailyCompletion(
    identity = readDailyChallengeId(),
    elapsedTime = if (readBoolean()) {
        DailyElapsedTime(readLong())
    } else {
        null
    }
)

private const val FILE_MAGIC = 0x4E504441
private const val MAX_SESSION_PAYLOAD_SIZE = 1_000_000
