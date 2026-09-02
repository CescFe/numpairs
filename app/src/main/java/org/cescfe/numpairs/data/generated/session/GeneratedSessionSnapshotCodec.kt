package org.cescfe.numpairs.data.generated.session

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import org.cescfe.numpairs.data.puzzle.readPuzzleSnapshot
import org.cescfe.numpairs.data.puzzle.writePuzzleSnapshot
import org.cescfe.numpairs.domain.puzzle.PuzzleCorrectionCount
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedTimingStartInstant

sealed interface GeneratedSessionSnapshotDecodingResult {
    data class Decoded(val snapshot: GeneratedSessionSnapshot) : GeneratedSessionSnapshotDecodingResult

    data class UnsupportedVersion(val schemaVersion: Int) : GeneratedSessionSnapshotDecodingResult

    data object InvalidData : GeneratedSessionSnapshotDecodingResult
}

class GeneratedSessionSnapshotCodec {
    fun encode(snapshot: GeneratedSessionSnapshot): ByteArray {
        require(snapshot.schemaVersion == GENERATED_SESSION_SCHEMA_VERSION) {
            "Only the current generated session schema can be encoded."
        }

        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(FILE_MAGIC)
                output.writeInt(snapshot.schemaVersion)
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
                output.writeBoolean(snapshot.timingStartInstant != null)
                snapshot.timingStartInstant?.let { startInstant ->
                    output.writeLong(startInstant.epochMilliseconds)
                }
                output.writeBoolean(snapshot.completionElapsedTime != null)
                snapshot.completionElapsedTime?.let { elapsedTime ->
                    output.writeLong(elapsedTime.milliseconds)
                }
            }
            bytes.toByteArray()
        }
    }

    fun decode(bytes: ByteArray): GeneratedSessionSnapshotDecodingResult = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            if (input.readInt() != FILE_MAGIC) {
                return GeneratedSessionSnapshotDecodingResult.InvalidData
            }

            val schemaVersion = input.readInt()
            val snapshot = when (schemaVersion) {
                INITIAL_GENERATED_SESSION_SCHEMA_VERSION -> input.readSnapshotWith(
                    readCorrectionCount = { null }
                )

                GENERATED_SESSION_ATTEMPT_METRICS_SCHEMA_VERSION -> input.readSnapshotWith(
                    readCorrectionCount = { readCorrectionCount() }
                )

                GENERATED_SESSION_SCHEMA_VERSION -> input.readSnapshotWith(
                    readCorrectionCount = { readCorrectionCount() },
                    readTimingStart = {
                        if (readBoolean()) GeneratedTimingStartInstant(readLong()) else null
                    },
                    readCompletionElapsedTime = {
                        if (readBoolean()) GeneratedElapsedTime(readLong()) else null
                    }
                )

                else -> return GeneratedSessionSnapshotDecodingResult.UnsupportedVersion(schemaVersion)
            }

            if (input.available() != 0) {
                GeneratedSessionSnapshotDecodingResult.InvalidData
            } else {
                GeneratedSessionSnapshotDecodingResult.Decoded(snapshot)
            }
        }
    } catch (_: IOException) {
        GeneratedSessionSnapshotDecodingResult.InvalidData
    } catch (_: IllegalArgumentException) {
        GeneratedSessionSnapshotDecodingResult.InvalidData
    } catch (_: IllegalStateException) {
        GeneratedSessionSnapshotDecodingResult.InvalidData
    }
}

private fun DataInputStream.readSnapshotWith(
    readCorrectionCount: DataInputStream.() -> PuzzleCorrectionCount?,
    readTimingStart: DataInputStream.() -> GeneratedTimingStartInstant? = { null },
    readCompletionElapsedTime: DataInputStream.() -> GeneratedElapsedTime? = { null }
): GeneratedSessionSnapshot {
    val sessionId = GeneratedSessionId(readUTF())
    val modeId = readUTF()
    val profileId = readUTF()
    val seed = readInt()
    val initialPuzzle = readPuzzleSnapshot()
    val currentPuzzle = readPuzzleSnapshot()
    return GeneratedSessionSnapshot(
        sessionId = sessionId,
        modeId = modeId,
        profileId = profileId,
        seed = seed,
        initialPuzzle = initialPuzzle,
        currentPuzzle = currentPuzzle,
        correctionCount = readCorrectionCount(),
        timingStartInstant = readTimingStart(),
        completionElapsedTime = readCompletionElapsedTime()
    )
}

private fun DataInputStream.readCorrectionCount(): PuzzleCorrectionCount? =
    if (readBoolean()) PuzzleCorrectionCount(readLong()) else null

private const val FILE_MAGIC = 0x4E505331
