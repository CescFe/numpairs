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
    fun create(mode: GeneratedModeConfiguration): GeneratedPuzzleGenerationUseCase
}

class ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
    private val modeRegistry: GeneratedModeRegistry = GeneratedModes.registry,
    private val generationDispatcher: CoroutineDispatcher = Dispatchers.Default
) : GeneratedPuzzleGenerationUseCaseFactory {
    private val contextByModeId: Map<GeneratedModeId, GeneratedPuzzleGenerationContext> =
        modeRegistry.all.associate { mode ->
            mode.id to GeneratedPuzzleGenerationContext.forProfile(profile = mode.profile)
        }

    override fun create(mode: GeneratedModeConfiguration): GeneratedPuzzleGenerationUseCase {
        val context = contextFor(mode = mode)

        return DispatcherGeneratedPuzzleGenerationUseCase(
            generationDispatcher = generationDispatcher,
            generatorFactory = { request ->
                require(request.profileId == mode.profile.id) {
                    "Generation request profile ${request.profileId.value} does not match mode ${mode.id.value}."
                }
                GeneratedPairsPuzzleGenerator(context = context)
            }
        )
    }

    internal fun contextFor(mode: GeneratedModeConfiguration): GeneratedPuzzleGenerationContext {
        require(modeRegistry.resolve(id = mode.id) == mode) {
            "Generated mode ${mode.id.value} is not configured by this generation use-case factory."
        }

        return contextByModeId.getValue(mode.id)
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
