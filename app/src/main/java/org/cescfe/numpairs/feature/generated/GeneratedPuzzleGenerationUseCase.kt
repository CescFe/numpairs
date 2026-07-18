package org.cescfe.numpairs.feature.generated

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationContext
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

fun interface GeneratedPuzzleGenerationUseCase {
    suspend fun generate(request: GeneratedPuzzleGenerationRequest): GeneratedPuzzleGenerationResult
}

sealed interface GeneratedPuzzleGenerationResult {
    val request: GeneratedPuzzleGenerationRequest

    data class Generated(override val request: GeneratedPuzzleGenerationRequest, val initialPuzzle: Puzzle) :
        GeneratedPuzzleGenerationResult

    data class Failed(val failure: GeneratedPairsPuzzleGenerationOutcome.Failed) : GeneratedPuzzleGenerationResult {
        override val request: GeneratedPuzzleGenerationRequest
            get() = failure.request
    }
}

fun interface GeneratedPuzzleGenerationUseCaseFactory {
    fun create(challenge: GeneratedChallenge): GeneratedPuzzleGenerationUseCase
}

class ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
    private val challengeCatalog: GeneratedChallengeCatalog = GeneratedModes.catalog,
    private val generationDispatcher: CoroutineDispatcher = Dispatchers.Default
) : GeneratedPuzzleGenerationUseCaseFactory {
    private val contextByChallengeId: Map<GeneratedChallengeId, GeneratedPuzzleGenerationContext> =
        challengeCatalog.allChallenges.associate { challenge ->
            challenge.id to GeneratedPuzzleGenerationContext.forProfile(profile = challenge.profile)
        }

    override fun create(challenge: GeneratedChallenge): GeneratedPuzzleGenerationUseCase {
        val context = contextFor(challenge = challenge)

        return DispatcherGeneratedPuzzleGenerationUseCase(
            generationDispatcher = generationDispatcher,
            generatorFactory = { request ->
                require(request.profileId == challenge.profile.id) {
                    "Generation request profile ${request.profileId.value} does not match challenge " +
                        "${challenge.id.value}."
                }
                GeneratedPairsPuzzleGenerator(context = context)
            }
        )
    }

    internal fun contextFor(challenge: GeneratedChallenge): GeneratedPuzzleGenerationContext {
        require(challengeCatalog.resolveChallenge(id = challenge.id) == challenge) {
            "Generated challenge ${challenge.id.value} is not configured by this generation use-case factory."
        }

        return contextByChallengeId.getValue(challenge.id)
    }
}

class DispatcherGeneratedPuzzleGenerationUseCase(
    private val generationDispatcher: CoroutineDispatcher,
    private val generatorFactory: (GeneratedPuzzleGenerationRequest) -> GeneratedPairsPuzzleGenerator
) : GeneratedPuzzleGenerationUseCase {
    override suspend fun generate(request: GeneratedPuzzleGenerationRequest): GeneratedPuzzleGenerationResult =
        withContext(generationDispatcher) {
            val coroutineContext = currentCoroutineContext()
            coroutineContext.ensureActive()

            when (
                val outcome = generatorFactory(request).generate(
                    request = request,
                    cancellation = {
                        !coroutineContext.isActive
                    }
                )
            ) {
                is GeneratedPairsPuzzleGenerationOutcome.Generated -> GeneratedPuzzleGenerationResult.Generated(
                    request = outcome.request,
                    initialPuzzle = outcome.puzzle.initialPuzzle
                )

                is GeneratedPairsPuzzleGenerationOutcome.Failed -> GeneratedPuzzleGenerationResult.Failed(outcome)
            }
        }
}
