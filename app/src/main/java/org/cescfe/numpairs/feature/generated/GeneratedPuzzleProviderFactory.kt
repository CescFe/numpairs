package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleGenerationContext
import org.cescfe.numpairs.domain.puzzle.model.Puzzle

fun interface GeneratedPuzzleProviderFactory {
    fun create(mode: GeneratedModeConfiguration): GeneratedPuzzleProvider
}

class ConfiguredGeneratedPuzzleProviderFactory(
    private val modeRegistry: GeneratedModeRegistry = GeneratedModes.registry
) : GeneratedPuzzleProviderFactory {
    private val contextByModeId: Map<GeneratedModeId, GeneratedPuzzleGenerationContext> =
        modeRegistry.all.associate { mode ->
            mode.id to GeneratedPuzzleGenerationContext.forProfile(profile = mode.profile)
        }

    override fun create(mode: GeneratedModeConfiguration): GeneratedPuzzleProvider =
        GeneratorBackedGeneratedPuzzleProvider(
            generator = GeneratedPairsPuzzleGenerator(context = contextFor(mode = mode))
        )

    internal fun create(modeId: GeneratedModeId, seed: Int): GeneratedPuzzleProvider =
        GeneratorBackedGeneratedPuzzleProvider(
            generator = GeneratedPairsPuzzleGenerator(
                context = contextFor(mode = modeRegistry.resolve(id = modeId)),
                seed = seed
            )
        )

    internal fun contextFor(mode: GeneratedModeConfiguration): GeneratedPuzzleGenerationContext {
        require(modeRegistry.resolve(id = mode.id) == mode) {
            "Generated mode ${mode.id.value} is not configured by this provider factory."
        }

        return contextByModeId.getValue(mode.id)
    }
}

private class GeneratorBackedGeneratedPuzzleProvider(private val generator: GeneratedPairsPuzzleGenerator) :
    GeneratedPuzzleProvider {
    override fun nextPuzzle(): Puzzle = generator.generate()
}
