package org.cescfe.numpairs.data.generated.session

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory

sealed interface GeneratedAggregateDecodingResult {
    data class Decoded(val aggregate: GeneratedAggregate) : GeneratedAggregateDecodingResult

    data class UnsupportedVersion(val schemaVersion: Int) : GeneratedAggregateDecodingResult

    data object InvalidData : GeneratedAggregateDecodingResult
}

class GeneratedAggregateCodec(
    private val sessionCodec: GeneratedSessionSnapshotCodec = GeneratedSessionSnapshotCodec()
) {
    fun encode(aggregate: GeneratedAggregate): ByteArray {
        require(aggregate.schemaVersion == GENERATED_AGGREGATE_SCHEMA_VERSION) {
            "Only the current generated aggregate schema can be encoded."
        }

        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(FILE_MAGIC)
                output.writeInt(aggregate.schemaVersion)
                output.writeBoolean(aggregate.activeSession != null)
                aggregate.activeSession?.let { snapshot ->
                    val encodedSnapshot = sessionCodec.encode(snapshot)
                    output.writeInt(encodedSnapshot.size)
                    output.write(encodedSnapshot)
                }
                val personalBests = aggregate.personalBests.toSortedMap(compareBy { category -> category.ordinal })
                output.writeInt(personalBests.size)
                personalBests.forEach { (category, elapsedTime) ->
                    output.writeUTF(category.generatedChallengeId)
                    output.writeLong(elapsedTime.milliseconds)
                }
            }
            bytes.toByteArray()
        }
    }

    fun decode(bytes: ByteArray): GeneratedAggregateDecodingResult {
        val aggregateResult = decodeAggregate(bytes)
        if (aggregateResult != GeneratedAggregateDecodingResult.InvalidData) {
            return aggregateResult
        }

        return when (val legacy = sessionCodec.decode(bytes)) {
            is GeneratedSessionSnapshotDecodingResult.Decoded -> GeneratedAggregateDecodingResult.Decoded(
                GeneratedAggregate(activeSession = legacy.snapshot)
            )

            is GeneratedSessionSnapshotDecodingResult.UnsupportedVersion ->
                GeneratedAggregateDecodingResult.UnsupportedVersion(legacy.schemaVersion)

            GeneratedSessionSnapshotDecodingResult.InvalidData -> GeneratedAggregateDecodingResult.InvalidData
        }
    }

    private fun decodeAggregate(bytes: ByteArray): GeneratedAggregateDecodingResult = try {
        DataInputStream(ByteArrayInputStream(bytes)).use { input ->
            if (input.readInt() != FILE_MAGIC) {
                return GeneratedAggregateDecodingResult.InvalidData
            }
            val schemaVersion = input.readInt()
            if (schemaVersion != GENERATED_AGGREGATE_SCHEMA_VERSION) {
                return GeneratedAggregateDecodingResult.UnsupportedVersion(schemaVersion)
            }
            val activeSession = if (input.readBoolean()) {
                val byteCount = input.readInt()
                require(byteCount in 1..input.available()) {
                    "Generated aggregate session size is invalid."
                }
                val encodedSession = ByteArray(byteCount).also { encoded ->
                    input.readFully(encoded)
                }
                when (val decoded = sessionCodec.decode(encodedSession)) {
                    is GeneratedSessionSnapshotDecodingResult.Decoded -> decoded.snapshot

                    is GeneratedSessionSnapshotDecodingResult.UnsupportedVersion,
                    GeneratedSessionSnapshotDecodingResult.InvalidData ->
                        return GeneratedAggregateDecodingResult.InvalidData
                }
            } else {
                null
            }
            val bestCount = input.readInt()
            require(bestCount in 0..GeneratedPersonalBestCategory.entries.size) {
                "Generated aggregate personal-best count is invalid."
            }
            val personalBests = buildMap {
                repeat(bestCount) {
                    val category = requireNotNull(
                        GeneratedPersonalBestCategory.fromGeneratedChallengeIdOrNull(input.readUTF())
                    ) {
                        "Generated aggregate personal-best category is unsupported."
                    }
                    require(put(category, GeneratedElapsedTime(input.readLong())) == null) {
                        "Generated aggregate personal-best categories must be unique."
                    }
                }
            }
            if (input.available() != 0) {
                GeneratedAggregateDecodingResult.InvalidData
            } else {
                GeneratedAggregateDecodingResult.Decoded(
                    GeneratedAggregate(
                        activeSession = activeSession,
                        personalBests = personalBests
                    )
                )
            }
        }
    } catch (_: IOException) {
        GeneratedAggregateDecodingResult.InvalidData
    } catch (_: IllegalArgumentException) {
        GeneratedAggregateDecodingResult.InvalidData
    } catch (_: IllegalStateException) {
        GeneratedAggregateDecodingResult.InvalidData
    }
}

private const val FILE_MAGIC = 0x4E504741
