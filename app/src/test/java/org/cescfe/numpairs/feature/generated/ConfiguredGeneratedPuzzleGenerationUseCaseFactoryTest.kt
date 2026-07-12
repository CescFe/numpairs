package org.cescfe.numpairs.feature.generated

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileDefinition
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleSize
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleVarietyPolicy
import org.cescfe.numpairs.domain.generated.profile.GenerationPolicy
import org.cescfe.numpairs.domain.generated.profile.InitialStripMaskPolicy
import org.cescfe.numpairs.domain.generated.profile.RequiredKnownStripAnchor
import org.cescfe.numpairs.domain.generated.profile.ResultConstraints
import org.cescfe.numpairs.domain.generated.profile.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.profile.StripValuePolicy
import org.cescfe.numpairs.domain.generated.profile.getOrThrow
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfiguredGeneratedPuzzleGenerationUseCaseFactoryTest {
    @Test
    fun every_registered_mode_uses_its_configured_profile_and_a_shared_context() = runBlocking {
        val factory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            generationDispatcher = Dispatchers.Unconfined
        )

        GeneratedModes.registry.all.forEach { mode ->
            val outcome = factory.create(mode = mode).generate(
                GeneratedPuzzleGenerationRequest(profile = mode.profile, seed = 2026)
            )
            val puzzle = (outcome as GeneratedPuzzleGenerationResult.Generated).initialPuzzle

            assertEquals(mode.profile.size.boardTileCount, puzzle.board.tiles.size)
            assertEquals(mode.profile.size.stripEntryCount, puzzle.strip.entries.size)
            assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
            assertTrue(puzzle.board.tiles.all(Tile::hasHiddenExpression))
            assertTrue(
                puzzle.strip.entries.count { entry -> entry.item is StripItem.Known } in
                    mode.profile.initialStripMaskPolicy.knownEntryCountRange
            )
            assertTrue(
                puzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden } in
                    mode.profile.hiddenEntryCountRange
            )
            assertSame(
                factory.contextFor(mode = mode),
                factory.contextFor(mode = mode)
            )
        }
    }

    @Test
    fun identical_requests_produce_identical_puzzles_without_sharing_a_generator_random_stream() = runBlocking {
        val mode = GeneratedModes.EIGHT_PAIRS
        val factory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            generationDispatcher = Dispatchers.Unconfined
        )
        val request = GeneratedPuzzleGenerationRequest(profile = mode.profile, seed = 1234)

        val first = factory.create(mode = mode).generate(request)
        val second = factory.create(mode = mode).generate(request)

        assertEquals(first, second)
    }

    @Test
    fun a_test_only_third_mode_needs_only_one_configuration_entry() = runBlocking {
        val thirdMode = GeneratedModeConfiguration(
            id = GeneratedModeId("test-two-pairs"),
            profile = testTwoPairsProfile()
        )
        val registry = GeneratedModeRegistry(GeneratedModes.registry.all + thirdMode)
        val factory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            modeRegistry = registry,
            generationDispatcher = Dispatchers.Unconfined
        )

        val outcome = factory.create(mode = thirdMode).generate(
            GeneratedPuzzleGenerationRequest(profile = thirdMode.profile, seed = 42)
        )
        val puzzle = (outcome as GeneratedPuzzleGenerationResult.Generated).initialPuzzle

        assertEquals(thirdMode.profile.size.boardTileCount, puzzle.board.tiles.size)
        assertEquals(thirdMode.profile.size.stripEntryCount, puzzle.strip.entries.size)
    }
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

private fun testTwoPairsProfile(): GeneratedPuzzleProfile = GeneratedPuzzleProfile.create(
    definition = GeneratedPuzzleProfileDefinition(
        id = GeneratedPuzzleProfileId("test-two-pairs"),
        size = GeneratedPuzzleSize(pairCount = 2),
        stripValuePolicy = StripValuePolicy(
            valueRange = 2..12,
            maxOccurrencesPerValue = 1
        ),
        resultConstraints = ResultConstraints(
            maxMultiplicationResult = 100,
            allowsDuplicateBoardResults = false
        ),
        initialStripMaskPolicy = InitialStripMaskPolicy(
            knownEntryCountRange = 1..1,
            requiredAnchors = setOf(RequiredKnownStripAnchor.HIGHEST_STRIP_ENTRY),
            distributionPolicy = StripKnownEntryDistributionPolicy.UNRESTRICTED,
            maxConsecutiveHiddenEntries = 3
        ),
        generationPolicy = GenerationPolicy(
            isBoardTileShufflingEnabled = true
        ),
        varietyPolicy = GeneratedPuzzleVarietyPolicy()
    )
).getOrThrow()
