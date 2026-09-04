package org.cescfe.numpairs.domain.generated

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeneratedPersonalBestTest {
    private val category = GeneratedPersonalBestCategory.FOUR_PAIRS_LOW

    @Test
    fun `the six categories are exact generated challenges`() {
        assertEquals(
            listOf(
                "three-pairs-low",
                "four-pairs-low",
                "three-pairs-medium",
                "four-pairs-medium",
                "eight-pairs-medium",
                "eight-pairs-hard"
            ),
            GeneratedPersonalBestCategory.entries.map(GeneratedPersonalBestCategory::generatedChallengeId)
        )
    }

    @Test
    fun `first timed result establishes a baseline`() {
        assertEquals(
            GeneratedPersonalBestResult(
                category = category,
                currentElapsedTime = GeneratedElapsedTime(65_432),
                previousBestElapsedTime = null,
                bestElapsedTime = GeneratedElapsedTime(65_432),
                outcome = GeneratedPersonalBestOutcome.BASELINE
            ),
            GeneratedPersonalBestResult.classify(
                category = category,
                currentElapsedTime = GeneratedElapsedTime(65_432),
                previousBestElapsedTime = null
            )
        )
    }

    @Test
    fun `only a strictly faster millisecond duration is a personal record`() {
        assertEquals(
            GeneratedPersonalBestOutcome.PERSONAL_RECORD,
            classify(current = 59_999, previous = 60_000).outcome
        )
        assertEquals(GeneratedElapsedTime(59_999), classify(current = 59_999, previous = 60_000).bestElapsedTime)

        assertEquals(
            GeneratedPersonalBestOutcome.NOT_RECORD,
            classify(current = 60_000, previous = 60_000).outcome
        )
        assertEquals(GeneratedElapsedTime(60_000), classify(current = 60_000, previous = 60_000).bestElapsedTime)

        assertEquals(
            GeneratedPersonalBestOutcome.NOT_RECORD,
            classify(current = 60_001, previous = 60_000).outcome
        )
        assertEquals(GeneratedElapsedTime(60_000), classify(current = 60_001, previous = 60_000).bestElapsedTime)
    }

    @Test
    fun `untimed and unresolved results cannot establish or improve a best`() {
        val untimedBaseline = GeneratedPersonalBestResult.classify(
            category = category,
            currentElapsedTime = null,
            previousBestElapsedTime = null
        )
        assertEquals(GeneratedPersonalBestOutcome.NOT_RECORD, untimedBaseline.outcome)
        assertNull(untimedBaseline.bestElapsedTime)

        val untimedWithBest = GeneratedPersonalBestResult.classify(
            category = category,
            currentElapsedTime = null,
            previousBestElapsedTime = GeneratedElapsedTime(42_000)
        )
        assertEquals(GeneratedPersonalBestOutcome.NOT_RECORD, untimedWithBest.outcome)
        assertEquals(GeneratedElapsedTime(42_000), untimedWithBest.bestElapsedTime)

        val unresolved = GeneratedPersonalBestResult.classify(
            category = null,
            currentElapsedTime = GeneratedElapsedTime(1),
            previousBestElapsedTime = null
        )
        assertEquals(GeneratedPersonalBestOutcome.NOT_RECORD, unresolved.outcome)
        assertNull(unresolved.previousBestElapsedTime)
        assertNull(unresolved.bestElapsedTime)
    }

    private fun classify(current: Long, previous: Long): GeneratedPersonalBestResult =
        GeneratedPersonalBestResult.classify(
            category = category,
            currentElapsedTime = GeneratedElapsedTime(current),
            previousBestElapsedTime = GeneratedElapsedTime(previous)
        )
}
