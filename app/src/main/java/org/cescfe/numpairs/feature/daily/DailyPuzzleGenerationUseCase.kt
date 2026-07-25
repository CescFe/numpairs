package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.domain.daily.DailyCandidateIndex
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory

data class DailyCandidateGenerationFailure(
    val candidateIndex: DailyCandidateIndex,
    val seed: Int,
    val failure: GeneratedPairsPuzzleGenerationOutcome.Failed
) {
    init {
        require(failure.request.seed == seed) {
            "Daily candidate failure seed must match its generation request."
        }
    }
}

sealed interface DailyPuzzleGenerationResult {
    val currentDailyChallenge: CurrentDailyChallenge

    val identity: DailyChallengeId
        get() = currentDailyChallenge.identity

    val challenge: GeneratedChallenge
        get() = currentDailyChallenge.recipe.challenge

    data class Generated(
        override val currentDailyChallenge: CurrentDailyChallenge,
        val candidateIndex: DailyCandidateIndex,
        val seed: Int,
        val initialPuzzle: Puzzle
    ) : DailyPuzzleGenerationResult {
        init {
            require(candidateIndex in currentDailyChallenge.recipe.candidateIndices) {
                "Generated Daily candidate index must belong to its recipe."
            }
            require(
                seed == currentDailyChallenge.recipe.seedFor(
                    identity = currentDailyChallenge.identity,
                    candidateIndex = candidateIndex
                )
            ) {
                "Generated Daily seed must match its recipe candidate."
            }
        }
    }

    data class Exhausted(
        override val currentDailyChallenge: CurrentDailyChallenge,
        val attemptedFailures: List<DailyCandidateGenerationFailure>
    ) : DailyPuzzleGenerationResult {
        init {
            require(attemptedFailures.isNotEmpty()) {
                "Exhausted Daily generation requires at least one attempted failure."
            }
            require(
                attemptedFailures.map(DailyCandidateGenerationFailure::candidateIndex) ==
                    currentDailyChallenge.recipe.candidateIndices
            ) {
                "Exhausted Daily generation must preserve every configured candidate in order."
            }
            require(
                attemptedFailures.none { attemptedFailure ->
                    attemptedFailure.failure.reason == GeneratedPairsPuzzleGenerationFailureReason.Cancelled
                }
            ) {
                "Cancelled Daily generation must not be reported as exhausted."
            }
            currentDailyChallenge.requireFailuresMatchRecipe(attemptedFailures)
        }
    }

    data class Cancelled(
        override val currentDailyChallenge: CurrentDailyChallenge,
        val attemptedFailures: List<DailyCandidateGenerationFailure>
    ) : DailyPuzzleGenerationResult {
        init {
            require(
                attemptedFailures.lastOrNull()?.failure?.reason ==
                    GeneratedPairsPuzzleGenerationFailureReason.Cancelled
            ) {
                "Cancelled Daily generation requires a terminal cancelled candidate."
            }
            require(
                attemptedFailures.map(DailyCandidateGenerationFailure::candidateIndex) ==
                    currentDailyChallenge.recipe.candidateIndices.take(attemptedFailures.size)
            ) {
                "Cancelled Daily generation must preserve the attempted candidate prefix in order."
            }
            currentDailyChallenge.requireFailuresMatchRecipe(attemptedFailures)
        }
    }
}

private fun CurrentDailyChallenge.requireFailuresMatchRecipe(attemptedFailures: List<DailyCandidateGenerationFailure>) {
    attemptedFailures.forEach { attemptedFailure ->
        require(
            attemptedFailure.seed == recipe.seedFor(
                identity = identity,
                candidateIndex = attemptedFailure.candidateIndex
            )
        ) {
            "Daily candidate failure seed must match its recipe candidate."
        }
        require(attemptedFailure.failure.request.profile == recipe.challenge.profile) {
            "Daily candidate failure profile must match its recipe challenge."
        }
    }
}

fun interface DailyPuzzleGenerator {
    suspend fun generate(currentDailyChallenge: CurrentDailyChallenge): DailyPuzzleGenerationResult
}

class DailyPuzzleGenerationUseCase(
    private val currentDailyChallengeResolver: CurrentDailyChallengeResolver,
    private val generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory
) : DailyPuzzleGenerator {
    suspend fun generate(): DailyPuzzleGenerationResult = generate(
        currentDailyChallenge = currentDailyChallengeResolver.resolve()
    )

    override suspend fun generate(currentDailyChallenge: CurrentDailyChallenge): DailyPuzzleGenerationResult {
        val recipe = currentDailyChallenge.recipe
        val challenge = recipe.challenge
        val generationUseCase = generatedPuzzleGenerationUseCaseFactory.create(challenge)
        val attemptedFailures = mutableListOf<DailyCandidateGenerationFailure>()

        recipe.candidateIndices.forEach { candidateIndex ->
            val seed = recipe.seedFor(
                identity = currentDailyChallenge.identity,
                candidateIndex = candidateIndex
            )
            val request = GeneratedPuzzleGenerationRequest(
                profile = challenge.profile,
                seed = seed
            )

            when (val result = generationUseCase.generate(request)) {
                is GeneratedPuzzleGenerationResult.Generated -> {
                    require(result.request == request) {
                        "Generated Daily candidate must correspond to its requested seed and profile."
                    }
                    return DailyPuzzleGenerationResult.Generated(
                        currentDailyChallenge = currentDailyChallenge,
                        candidateIndex = candidateIndex,
                        seed = seed,
                        initialPuzzle = result.initialPuzzle
                    )
                }

                is GeneratedPuzzleGenerationResult.Failed -> {
                    require(result.request == request) {
                        "Failed Daily candidate must correspond to its requested seed and profile."
                    }
                    attemptedFailures += DailyCandidateGenerationFailure(
                        candidateIndex = candidateIndex,
                        seed = seed,
                        failure = result.failure
                    )
                    if (result.failure.reason == GeneratedPairsPuzzleGenerationFailureReason.Cancelled) {
                        return DailyPuzzleGenerationResult.Cancelled(
                            currentDailyChallenge = currentDailyChallenge,
                            attemptedFailures = attemptedFailures
                        )
                    }
                }
            }
        }

        return DailyPuzzleGenerationResult.Exhausted(
            currentDailyChallenge = currentDailyChallenge,
            attemptedFailures = attemptedFailures
        )
    }
}
