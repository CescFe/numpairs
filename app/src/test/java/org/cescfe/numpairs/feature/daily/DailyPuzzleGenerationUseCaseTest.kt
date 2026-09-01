package org.cescfe.numpairs.feature.daily

import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.feature.generated.ConfiguredGeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyPuzzleGenerationUseCaseTest {
    @Test
    fun generation_captures_current_identity_once_and_returns_the_first_successful_candidate() = runBlocking {
        val date = LocalDate.of(2026, 7, 25)
        var dateReadCount = 0
        val requests = mutableListOf<GeneratedPuzzleGenerationRequest>()
        val createdChallenges = mutableListOf<GeneratedChallenge>()
        val useCase = dailyGenerationUseCase(
            dateSource = {
                dateReadCount += 1
                date
            },
            generatedFactory = { challenge ->
                createdChallenges += challenge
                GeneratedPuzzleGenerationUseCase { request ->
                    requests += request
                    if (requests.size < 3) {
                        failedResult(
                            request = request,
                            reason = GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted
                        )
                    } else {
                        GeneratedPuzzleGenerationResult.Generated(
                            request = request,
                            initialPuzzle = samplePuzzle
                        )
                    }
                }
            }
        )

        val result = useCase.generate() as DailyPuzzleGenerationResult.Generated
        val expectedIdentity = DailyRecipes.WEEKLY_SCHEDULE_V2.identityFor(date)
        val expectedSeeds = (0..2).map { candidateIndex ->
            DailyRecipes.WEEKLY_SCHEDULE_V2.seedFor(
                identity = expectedIdentity,
                candidateIndex = DailyCandidateIndex(candidateIndex)
            )
        }

        assertEquals(1, dateReadCount)
        assertEquals(expectedIdentity, result.identity)
        assertSame(GeneratedModes.THREE_PAIRS_MEDIUM, result.challenge)
        assertEquals(DailyCandidateIndex(2), result.candidateIndex)
        assertEquals(expectedSeeds[2], result.seed)
        assertSame(samplePuzzle, result.initialPuzzle)
        assertEquals(expectedSeeds, requests.map(GeneratedPuzzleGenerationRequest::seed))
        assertTrue(requests.all { request -> request.profile === GeneratedModes.THREE_PAIRS_MEDIUM.profile })
        assertEquals(listOf(GeneratedModes.THREE_PAIRS_MEDIUM), createdChallenges)
    }

    @Test
    fun generation_uses_the_scheduled_challenge_for_every_weekday() = runBlocking {
        val expectedChallenges = listOf(
            GeneratedModes.THREE_PAIRS_LOW,
            GeneratedModes.FOUR_PAIRS_LOW,
            GeneratedModes.THREE_PAIRS_MEDIUM,
            GeneratedModes.FOUR_PAIRS_MEDIUM,
            GeneratedModes.EIGHT_PAIRS_MEDIUM,
            GeneratedModes.THREE_PAIRS_MEDIUM,
            GeneratedModes.FOUR_PAIRS_LOW
        )

        expectedChallenges.forEachIndexed { dayOffset, expectedChallenge ->
            val requests = mutableListOf<GeneratedPuzzleGenerationRequest>()
            val createdChallenges = mutableListOf<GeneratedChallenge>()
            val useCase = dailyGenerationUseCase(
                dateSource = {
                    LocalDate.of(2026, 8, 31).plusDays(dayOffset.toLong())
                },
                generatedFactory = { challenge ->
                    createdChallenges += challenge
                    GeneratedPuzzleGenerationUseCase { request ->
                        requests += request
                        GeneratedPuzzleGenerationResult.Generated(
                            request = request,
                            initialPuzzle = samplePuzzle
                        )
                    }
                }
            )

            val result = useCase.generate() as DailyPuzzleGenerationResult.Generated

            assertSame(expectedChallenge, result.challenge)
            assertEquals(listOf(expectedChallenge), createdChallenges)
            assertEquals(listOf(expectedChallenge.profile), requests.map(GeneratedPuzzleGenerationRequest::profile))
        }
    }

    @Test
    fun non_cancellation_failures_are_preserved_in_order_when_every_candidate_is_exhausted() = runBlocking {
        val reasons = listOf(
            GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted,
            GeneratedPairsPuzzleGenerationFailureReason.SearchBudgetExhausted,
            GeneratedPairsPuzzleGenerationFailureReason.DifficultyAssessmentWorkLimitReached,
            GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted
        )
        val useCase = dailyGenerationUseCase(
            dateSource = { LocalDate.of(2026, 12, 31) },
            generatedFactory = generatedFactory { request, attempt ->
                failedResult(request = request, reason = reasons[attempt])
            }
        )

        val result = useCase.generate() as DailyPuzzleGenerationResult.Exhausted

        assertEquals(listOf(0, 1, 2, 3), result.attemptedFailures.map { it.candidateIndex.value })
        assertEquals(reasons, result.attemptedFailures.map { it.failure.reason })
        assertEquals(
            result.currentDailyChallenge.recipe.candidateIndices.map { candidateIndex ->
                result.currentDailyChallenge.recipe.seedFor(result.identity, candidateIndex)
            },
            result.attemptedFailures.map(DailyCandidateGenerationFailure::seed)
        )
    }

    @Test
    fun typed_cancellation_is_terminal_and_does_not_attempt_later_candidates() = runBlocking {
        val requests = mutableListOf<GeneratedPuzzleGenerationRequest>()
        val useCase = dailyGenerationUseCase(
            dateSource = { LocalDate.of(2028, 2, 29) },
            generatedFactory = generatedFactory { request, attempt ->
                requests += request
                failedResult(
                    request = request,
                    reason = if (attempt == 1) {
                        GeneratedPairsPuzzleGenerationFailureReason.Cancelled
                    } else {
                        GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted
                    }
                )
            }
        )

        val result = useCase.generate() as DailyPuzzleGenerationResult.Cancelled

        assertEquals(2, requests.size)
        assertEquals(listOf(0, 1), result.attemptedFailures.map { it.candidateIndex.value })
        assertEquals(
            GeneratedPairsPuzzleGenerationFailureReason.Cancelled,
            result.attemptedFailures.last().failure.reason
        )
    }

    @Test
    fun coroutine_cancellation_is_not_converted_into_exhaustion() {
        val useCase = dailyGenerationUseCase(
            dateSource = { LocalDate.of(2026, 7, 25) },
            generatedFactory = {
                GeneratedPuzzleGenerationUseCase {
                    throw CancellationException("cancel test")
                }
            }
        )

        assertThrows(CancellationException::class.java) {
            runBlocking {
                useCase.generate()
            }
        }
    }

    @Test
    fun configured_generation_is_repeatable_for_the_same_daily_identity() = runBlocking {
        val useCase = configuredDailyGenerationUseCase(LocalDate.of(2027, 4, 18))

        val first = useCase.generate() as DailyPuzzleGenerationResult.Generated
        val second = useCase.generate() as DailyPuzzleGenerationResult.Generated

        assertEquals(first, second)
    }

    @Test
    fun every_date_in_2027_generates_within_the_configured_four_candidates() = runBlocking {
        var currentDate = LocalDate.of(2027, 1, 1)
        val useCase = configuredDailyGenerationUseCase(
            dateSource = { currentDate }
        )
        val successfulCandidateCounts = IntArray(4)

        repeat(currentDate.lengthOfYear()) {
            val result = useCase.generate() as DailyPuzzleGenerationResult.Generated
            assertEquals(currentDate, result.identity.localDate)
            successfulCandidateCounts[result.candidateIndex.value] += 1
            currentDate = currentDate.plusDays(1)
        }

        assertEquals(365, successfulCandidateCounts.sum())
    }
}

private fun dailyGenerationUseCase(
    dateSource: DeviceLocalDateSource,
    generatedFactory: GeneratedPuzzleGenerationUseCaseFactory
): DailyPuzzleGenerationUseCase = DailyPuzzleGenerationUseCase(
    currentDailyChallengeResolver = CurrentDailyChallengeResolver(localDateSource = dateSource),
    generatedPuzzleGenerationUseCaseFactory = generatedFactory
)

private fun configuredDailyGenerationUseCase(date: LocalDate): DailyPuzzleGenerationUseCase =
    configuredDailyGenerationUseCase(
        dateSource = { date }
    )

private fun configuredDailyGenerationUseCase(dateSource: DeviceLocalDateSource): DailyPuzzleGenerationUseCase =
    dailyGenerationUseCase(
        dateSource = dateSource,
        generatedFactory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            generationDispatcher = Dispatchers.Unconfined
        )
    )

private fun generatedFactory(
    resultForAttempt: (GeneratedPuzzleGenerationRequest, Int) -> GeneratedPuzzleGenerationResult
): GeneratedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory {
    var attempt = 0
    GeneratedPuzzleGenerationUseCase { request ->
        resultForAttempt(request, attempt++)
    }
}

private fun failedResult(
    request: GeneratedPuzzleGenerationRequest,
    reason: GeneratedPairsPuzzleGenerationFailureReason
): GeneratedPuzzleGenerationResult.Failed = GeneratedPuzzleGenerationResult.Failed(
    failure = GeneratedPairsPuzzleGenerationOutcome.Failed(
        request = request,
        attemptsUsed = 1,
        searchWorkConsumed = 1,
        reason = reason,
        candidateRejections = emptyList()
    )
)
