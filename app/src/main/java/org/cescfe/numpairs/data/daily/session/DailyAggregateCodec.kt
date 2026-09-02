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
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount

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
                INITIAL_DAILY_AGGREGATE_SCHEMA_VERSION -> decodeInitialAggregate(input)
                TIMED_DAILY_AGGREGATE_SCHEMA_VERSION -> decodeTimedAggregate(input)
                MOVEMENT_DAILY_AGGREGATE_SCHEMA_VERSION -> decodeMovementAggregate(input)
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
            output.writeBoolean(snapshot.movementCount != null)
            snapshot.movementCount?.let { movementCount ->
                output.writeLong(movementCount.value)
            }
            output.writeBoolean(snapshot.correctionCount != null)
            snapshot.correctionCount?.let { correctionCount ->
                output.writeLong(correctionCount.value)
            }
        }
        bytes.toByteArray()
    }

    private fun decodeInitialAggregate(input: DataInputStream): DailyAggregateDecodingResult = decodeAggregate(
        input = input,
        decodeSession = ::decodeInitialSessionOrNull,
        readCompletion = {
            DailyCompletion(
                identity = readDailyChallengeId(),
                elapsedTime = null,
                movementCount = null,
                correctionCount = null
            )
        }
    )

    private fun decodeTimedAggregate(input: DataInputStream): DailyAggregateDecodingResult = decodeAggregate(
        input = input,
        decodeSession = ::decodeTimedSessionOrNull,
        readCompletion = DataInputStream::readTimedDailyCompletion
    )

    private fun decodeMovementAggregate(input: DataInputStream): DailyAggregateDecodingResult = decodeAggregate(
        input = input,
        decodeSession = ::decodeMovementSessionOrNull,
        readCompletion = DataInputStream::readMovementDailyCompletion
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

    private fun decodeInitialSessionOrNull(bytes: ByteArray): DailySessionSnapshot? = decodeSessionOrNull(
        bytes = bytes,
        readTimingStartInstant = { null },
        readMovementCount = { null },
        readCorrectionCount = { null }
    )

    private fun decodeTimedSessionOrNull(bytes: ByteArray): DailySessionSnapshot? = decodeSessionOrNull(
        bytes = bytes,
        readTimingStartInstant = DataInputStream::readDailyTimingStartInstant,
        readMovementCount = { null },
        readCorrectionCount = { null }
    )

    private fun decodeMovementSessionOrNull(bytes: ByteArray): DailySessionSnapshot? = decodeSessionOrNull(
        bytes = bytes,
        readTimingStartInstant = DataInputStream::readDailyTimingStartInstant,
        readMovementCount = DataInputStream::readDailyMovementCount,
        readCorrectionCount = { null }
    )

    private fun decodeCurrentSessionOrNull(bytes: ByteArray): DailySessionSnapshot? = decodeSessionOrNull(
        bytes = bytes,
        readTimingStartInstant = DataInputStream::readDailyTimingStartInstant,
        readMovementCount = DataInputStream::readDailyMovementCount,
        readCorrectionCount = DataInputStream::readPuzzleCorrectionCount
    )

    private fun decodeSessionOrNull(
        bytes: ByteArray,
        readTimingStartInstant: DataInputStream.() -> DailyTimingStartInstant?,
        readMovementCount: DataInputStream.() -> DailyMovementCount?,
        readCorrectionCount: DataInputStream.() -> PuzzleCorrectionCount?
    ): DailySessionSnapshot? = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val snapshot = DailySessionSnapshot(
                sessionId = DailySessionId(input.readUTF()),
                dailyChallengeId = input.readDailyChallengeId(),
                candidateIndex = DailyCandidateIndex(input.readInt()),
                seed = input.readInt(),
                initialPuzzle = input.readPuzzleSnapshot(),
                currentPuzzle = input.readPuzzleSnapshot(),
                timingStartInstant = input.readTimingStartInstant(),
                movementCount = input.readMovementCount(),
                correctionCount = input.readCorrectionCount()
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
    writeBoolean(completion.movementCount != null)
    completion.movementCount?.let { movementCount ->
        writeLong(movementCount.value)
    }
    writeBoolean(completion.correctionCount != null)
    completion.correctionCount?.let { correctionCount ->
        writeLong(correctionCount.value)
    }
}

private fun DataInputStream.readDailyCompletion(): DailyCompletion = DailyCompletion(
    identity = readDailyChallengeId(),
    elapsedTime = readDailyElapsedTime(),
    movementCount = readDailyMovementCount(),
    correctionCount = readPuzzleCorrectionCount()
)

private fun DataInputStream.readMovementDailyCompletion(): DailyCompletion = DailyCompletion(
    identity = readDailyChallengeId(),
    elapsedTime = readDailyElapsedTime(),
    movementCount = readDailyMovementCount(),
    correctionCount = null
)

private fun DataInputStream.readTimedDailyCompletion(): DailyCompletion = DailyCompletion(
    identity = readDailyChallengeId(),
    elapsedTime = readDailyElapsedTime(),
    movementCount = null,
    correctionCount = null
)

private fun DataInputStream.readDailyElapsedTime(): DailyElapsedTime? = if (readBoolean()) {
    DailyElapsedTime(readLong())
} else {
    null
}

private fun DataInputStream.readDailyTimingStartInstant(): DailyTimingStartInstant? = if (readBoolean()) {
    DailyTimingStartInstant(readLong())
} else {
    null
}

private fun DataInputStream.readDailyMovementCount(): DailyMovementCount? = if (readBoolean()) {
    DailyMovementCount(readLong())
} else {
    null
}

private fun DataInputStream.readPuzzleCorrectionCount(): PuzzleCorrectionCount? = if (readBoolean()) {
    PuzzleCorrectionCount(readLong())
} else {
    null
}

private const val FILE_MAGIC = 0x4E504441
private const val MAX_SESSION_PAYLOAD_SIZE = 1_000_000
