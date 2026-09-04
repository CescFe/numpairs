package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.GeneratedPersonalBestCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeneratedChallengePersonalBestCategoryResolverTest {
    private val resolver = GeneratedChallengePersonalBestCategoryResolver()

    @Test
    fun `all six configured challenge identities resolve independently`() {
        val expected = mapOf(
            GeneratedModes.THREE_PAIRS_LOW to GeneratedPersonalBestCategory.THREE_PAIRS_LOW,
            GeneratedModes.FOUR_PAIRS_LOW to GeneratedPersonalBestCategory.FOUR_PAIRS_LOW,
            GeneratedModes.THREE_PAIRS_MEDIUM to GeneratedPersonalBestCategory.THREE_PAIRS_MEDIUM,
            GeneratedModes.FOUR_PAIRS_MEDIUM to GeneratedPersonalBestCategory.FOUR_PAIRS_MEDIUM,
            GeneratedModes.EIGHT_PAIRS_MEDIUM to GeneratedPersonalBestCategory.EIGHT_PAIRS_MEDIUM,
            GeneratedModes.EIGHT_PAIRS_HARD to GeneratedPersonalBestCategory.EIGHT_PAIRS_HARD
        )

        assertEquals(
            expected.values.toSet(),
            expected.map { (challenge, _) ->
                resolver.categoryFor(
                    modeId = challenge.modeId.value,
                    profileId = challenge.profile.id.value
                )
            }.toSet()
        )
        expected.forEach { (challenge, category) ->
            assertEquals(
                category,
                resolver.categoryFor(challenge.modeId.value, challenge.profile.id.value)
            )
        }
    }

    @Test
    fun `mixed and unsupported mode profile identities do not resolve`() {
        assertNull(
            resolver.categoryFor(
                modeId = GeneratedModes.THREE_PAIRS.id.value,
                profileId = GeneratedModes.FOUR_PAIRS_LOW.profile.id.value
            )
        )
        assertNull(resolver.categoryFor(modeId = "quick", profileId = "low"))
    }
}
