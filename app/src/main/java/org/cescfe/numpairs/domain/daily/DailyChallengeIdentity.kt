package org.cescfe.numpairs.domain.daily

import java.time.LocalDate

@JvmInline
value class DailyRecipeVersion(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Daily recipe version must not be blank."
        }
        require('|' !in value) {
            "Daily recipe version must not contain the seed-payload delimiter."
        }
    }
}

@JvmInline
value class DailyCandidateIndex(val value: Int) {
    init {
        require(value >= 0) {
            "Daily candidate index must be zero or greater."
        }
    }
}

data class DailyChallengeId(val localDate: LocalDate, val recipeVersion: DailyRecipeVersion) {
    val canonicalLocalDate: String
        get() = localDate.toString()
}

fun interface DailyCandidateSeedSchedule {
    fun seedFor(identity: DailyChallengeId, candidateIndex: DailyCandidateIndex): Int
}

object Fnv1a32DailyCandidateSeedSchedule : DailyCandidateSeedSchedule {
    override fun seedFor(identity: DailyChallengeId, candidateIndex: DailyCandidateIndex): Int {
        val payload = buildString {
            append(identity.recipeVersion.value)
            append(PAYLOAD_DELIMITER)
            append(identity.canonicalLocalDate)
            append(PAYLOAD_DELIMITER)
            append(candidateIndex.value)
        }
        var hash = FNV_OFFSET_BASIS
        payload.encodeToByteArray().forEach { byte ->
            hash = (hash xor byte.toUByte().toUInt()) * FNV_PRIME
        }
        return hash.toInt()
    }

    private const val PAYLOAD_DELIMITER: Char = '|'
    private const val FNV_OFFSET_BASIS: UInt = 0x811C9DC5u
    private const val FNV_PRIME: UInt = 0x01000193u
}
