package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles

@JvmInline
value class GeneratedModeId(val value: String) {
    init {
        require(value.isNotBlank()) {
            "Generated mode id must not be blank."
        }
    }
}

data class GeneratedModeConfiguration(val id: GeneratedModeId, val profile: GeneratedPuzzleProfile)

class GeneratedModeRegistry(configurations: Collection<GeneratedModeConfiguration>) {
    val all: List<GeneratedModeConfiguration> = configurations.toList()
    private val configurationsById: Map<GeneratedModeId, GeneratedModeConfiguration> = all.associateBy(
        GeneratedModeConfiguration::id
    )

    init {
        require(all.isNotEmpty()) {
            "At least one generated mode must be configured."
        }
        require(configurationsById.size == all.size) {
            "Generated mode ids must be unique."
        }
        require(all.map(GeneratedModeConfiguration::profile).distinct().size == all.size) {
            "A generated profile can only belong to one generated mode."
        }
    }

    fun resolve(id: GeneratedModeId): GeneratedModeConfiguration = requireNotNull(configurationsById[id]) {
        "No generated mode is configured for id ${id.value}."
    }
}

object GeneratedModes {
    val FOUR_PAIRS: GeneratedModeConfiguration = GeneratedModeConfiguration(
        id = GeneratedModeId("four-pairs"),
        profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
    )
    val EIGHT_PAIRS: GeneratedModeConfiguration = GeneratedModeConfiguration(
        id = GeneratedModeId("eight-pairs"),
        profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM
    )
    val registry: GeneratedModeRegistry = GeneratedModeRegistry(
        configurations = listOf(FOUR_PAIRS, EIGHT_PAIRS)
    )
}
