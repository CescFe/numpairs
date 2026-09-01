package org.cescfe.numpairs.domain.daily

import java.time.DayOfWeek
import java.time.LocalDate
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles

sealed interface DailyRecipeProfileSelection {
    val profiles: Set<GeneratedPuzzleProfile>

    fun profileFor(localDate: LocalDate): GeneratedPuzzleProfile

    data class Fixed(val profile: GeneratedPuzzleProfile) : DailyRecipeProfileSelection {
        override val profiles: Set<GeneratedPuzzleProfile> = setOf(profile)

        override fun profileFor(localDate: LocalDate): GeneratedPuzzleProfile = profile
    }

    data class Weekly(val profilesByDayOfWeek: Map<DayOfWeek, GeneratedPuzzleProfile>) :
        DailyRecipeProfileSelection {
        override val profiles: Set<GeneratedPuzzleProfile> = profilesByDayOfWeek.values.toSet()

        init {
            require(profilesByDayOfWeek.keys == DayOfWeek.entries.toSet()) {
                "A weekly Daily recipe must configure exactly one profile for every day of week."
            }
        }

        override fun profileFor(localDate: LocalDate): GeneratedPuzzleProfile =
            profilesByDayOfWeek.getValue(localDate.dayOfWeek)
    }
}

data class DailyRecipeContract(
    val version: DailyRecipeVersion,
    val profileSelection: DailyRecipeProfileSelection,
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

    fun profileFor(identity: DailyChallengeId): GeneratedPuzzleProfile {
        require(identity.recipeVersion == version) {
            "Daily Challenge identity must use recipe ${version.value}."
        }
        return profileSelection.profileFor(identity.localDate)
    }

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
        profileSelection = DailyRecipeProfileSelection.Fixed(GeneratedPuzzleProfiles.FOUR_PAIRS_LOW),
        candidateIndices = List(4, ::DailyCandidateIndex),
        seedSchedule = Fnv1a32DailyCandidateSeedSchedule
    )
    val WEEKLY_SCHEDULE_V2: DailyRecipeContract = DailyRecipeContract(
        version = DailyRecipeVersion("daily-weekly-schedule-v2"),
        profileSelection = DailyRecipeProfileSelection.Weekly(
            profilesByDayOfWeek = mapOf(
                DayOfWeek.MONDAY to GeneratedPuzzleProfiles.THREE_PAIRS_LOW,
                DayOfWeek.TUESDAY to GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
                DayOfWeek.WEDNESDAY to GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM,
                DayOfWeek.THURSDAY to GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM,
                DayOfWeek.FRIDAY to GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
                DayOfWeek.SATURDAY to GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM,
                DayOfWeek.SUNDAY to GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
            )
        ),
        candidateIndices = List(4, ::DailyCandidateIndex),
        seedSchedule = Fnv1a32DailyCandidateSeedSchedule
    )
    val catalog: DailyRecipeContractCatalog = DailyRecipeContractCatalog(
        contracts = listOf(FOUR_PAIRS_LOW_V1, WEEKLY_SCHEDULE_V2)
    )
}
