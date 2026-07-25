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
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion

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
                output.writeInt(aggregate.completedChallengeIds.size)
                aggregate.completedChallengeIds.forEach(output::writeDailyChallengeId)
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
            if (schemaVersion != DAILY_AGGREGATE_SCHEMA_VERSION) {
                return DailyAggregateDecodingResult.UnsupportedVersion(schemaVersion)
            }

            val activeSession = if (input.readBoolean()) {
                val sessionPayloadSize = input.readInt()
                if (sessionPayloadSize !in 1..MAX_SESSION_PAYLOAD_SIZE) {
                    return DailyAggregateDecodingResult.InvalidData
                }
                val sessionPayload = ByteArray(sessionPayloadSize)
                input.readFully(sessionPayload)
                decodeSessionOrNull(sessionPayload)
            } else {
                null
            }
            val completionCount = input.readInt()
            if (completionCount !in 0..MAX_DAILY_COMPLETION_COUNT) {
                return DailyAggregateDecodingResult.InvalidData
            }
            val completedChallengeIds = List(completionCount) {
                input.readDailyChallengeId()
            }

            if (input.available() != 0) {
                DailyAggregateDecodingResult.InvalidData
            } else {
                DailyAggregateDecodingResult.Decoded(
                    DailyAggregate(
                        schemaVersion = schemaVersion,
                        activeSession = activeSession,
                        completedChallengeIds = completedChallengeIds
                    )
                )
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
        }
        bytes.toByteArray()
    }

    private fun decodeSessionOrNull(bytes: ByteArray): DailySessionSnapshot? = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            val snapshot = DailySessionSnapshot(
                sessionId = DailySessionId(input.readUTF()),
                dailyChallengeId = input.readDailyChallengeId(),
                candidateIndex = DailyCandidateIndex(input.readInt()),
                seed = input.readInt(),
                initialPuzzle = input.readPuzzleSnapshot(),
                currentPuzzle = input.readPuzzleSnapshot()
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

private const val FILE_MAGIC = 0x4E504441
private const val MAX_SESSION_PAYLOAD_SIZE = 1_000_000
