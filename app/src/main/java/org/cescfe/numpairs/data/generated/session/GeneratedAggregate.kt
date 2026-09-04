package org.cescfe.numpairs.data.generated.session

import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory

const val GENERATED_AGGREGATE_SCHEMA_VERSION: Int = 1

data class GeneratedAggregate(
    val schemaVersion: Int = GENERATED_AGGREGATE_SCHEMA_VERSION,
    val activeSession: GeneratedSessionSnapshot? = null,
    val personalBests: Map<GeneratedPersonalBestCategory, GeneratedElapsedTime> = emptyMap()
) {
    init {
        require(schemaVersion == GENERATED_AGGREGATE_SCHEMA_VERSION) {
            "Generated aggregate schema version is unsupported."
        }
    }
}

data class GeneratedSessionState(
    val activeSession: GeneratedSessionSnapshot? = null,
    val personalBests: Map<GeneratedPersonalBestCategory, GeneratedElapsedTime> = emptyMap()
)
