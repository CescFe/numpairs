package org.cescfe.numpairs.domain.daily

import java.time.LocalDate
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles

data class DailyRecipeContract(
    val version: DailyRecipeVersion,
    val profile: GeneratedPuzzleProfile,
    val candidateIndices: List<DailyCandidateIndex>,
    private val seedSchedule: DailyCandidateSeedSchedule
) {
    init {
        require(candidateIndices.isNotEmpty()) {
            "A Daily recipe contract must configure at least one candidate."
        }
        require(candidateIndices == List(candidateIndices.size, ::DailyCandidateIndex)) {
            "Daily recipe candidate indexes must be contiguous and start at zero."
        }
    }

    fun identityFor(localDate: LocalDate): DailyChallengeId = DailyChallengeId(
        localDate = localDate,
        recipeVersion = version
    )

    fun seedFor(identity: DailyChallengeId, candidateIndex: DailyCandidateIndex): Int {
        require(identity.recipeVersion == version) {
            "Daily Challenge identity must use recipe ${version.value}."
        }
        require(candidateIndex in candidateIndices) {
            "Daily candidate index ${candidateIndex.value} is not configured by recipe ${version.value}."
        }
        return seedSchedule.seedFor(identity = identity, candidateIndex = candidateIndex)
    }
}

class DailyRecipeContractCatalog(contracts: Collection<DailyRecipeContract>) {
    val all: List<DailyRecipeContract> = contracts.toList()
    private val contractsByVersion: Map<DailyRecipeVersion, DailyRecipeContract> =
        all.associateBy(DailyRecipeContract::version)

    init {
        require(all.isNotEmpty()) {
            "At least one Daily recipe contract must be configured."
        }
        require(contractsByVersion.size == all.size) {
            "Daily recipe contract versions must be unique."
        }
    }

    fun resolve(version: DailyRecipeVersion): DailyRecipeContract = requireNotNull(contractsByVersion[version]) {
        "No Daily recipe contract is configured for version ${version.value}."
    }

    fun resolveOrNull(version: DailyRecipeVersion): DailyRecipeContract? = contractsByVersion[version]
}

object DailyRecipeContracts {
    val FOUR_PAIRS_LOW_V1: DailyRecipeContract = DailyRecipeContract(
        version = DailyRecipeVersion("daily-4-pairs-low-v1"),
        profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
        candidateIndices = List(4, ::DailyCandidateIndex),
        seedSchedule = Fnv1a32DailyCandidateSeedSchedule
    )
    val catalog: DailyRecipeContractCatalog = DailyRecipeContractCatalog(
        contracts = listOf(FOUR_PAIRS_LOW_V1)
    )
}
