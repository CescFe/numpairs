package org.cescfe.numpairs.domain.daily

import java.time.DayOfWeek
import java.time.LocalDate
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class DailyRecipeContractTest {
    @Test
    fun weekly_contract_resolves_the_authoritative_profile_from_its_identity_date() {
        val contract = DailyRecipeContracts.WEEKLY_SCHEDULE_V2
        val expectedProfiles = mapOf(
            DayOfWeek.MONDAY to GeneratedPuzzleProfiles.THREE_PAIRS_LOW,
            DayOfWeek.TUESDAY to GeneratedPuzzleProfiles.FOUR_PAIRS_LOW,
            DayOfWeek.WEDNESDAY to GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM,
            DayOfWeek.THURSDAY to GeneratedPuzzleProfiles.FOUR_PAIRS_MEDIUM,
            DayOfWeek.FRIDAY to GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
            DayOfWeek.SATURDAY to GeneratedPuzzleProfiles.THREE_PAIRS_MEDIUM,
            DayOfWeek.SUNDAY to GeneratedPuzzleProfiles.FOUR_PAIRS_LOW
        )

        expectedProfiles.forEach { (dayOfWeek, expectedProfile) ->
            val identity = contract.identityFor(dateFor(dayOfWeek))

            assertSame(expectedProfile, contract.profileFor(identity))
            assertEquals(contract.version, identity.recipeVersion)
        }
    }

    @Test
    fun contract_requires_a_complete_week_and_rejects_another_recipe_identity() {
        assertThrows(IllegalArgumentException::class.java) {
            DailyRecipeContract(
                version = DailyRecipeVersion("incomplete-week"),
                profileSelection = DailyRecipeProfileSelection.Weekly(
                    profilesByDayOfWeek = mapOf(
                        DayOfWeek.MONDAY to GeneratedPuzzleProfiles.THREE_PAIRS_LOW
                    )
                ),
                candidateIndices = listOf(DailyCandidateIndex(0)),
                seedSchedule = Fnv1a32DailyCandidateSeedSchedule
            )
        }

        val contract = DailyRecipeContracts.WEEKLY_SCHEDULE_V2
        val identity = DailyRecipeContracts.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 9, 2)
        )
        assertThrows(IllegalArgumentException::class.java) {
            contract.profileFor(identity)
        }
    }
}

private fun dateFor(dayOfWeek: DayOfWeek): LocalDate = LocalDate.of(2026, 8, 31).plusDays(
    (dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()
)
