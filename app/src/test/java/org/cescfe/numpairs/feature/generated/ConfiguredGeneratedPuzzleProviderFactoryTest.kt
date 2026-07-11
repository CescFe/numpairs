package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfile
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfileDefinition
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfileId
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleSize
import org.cescfe.numpairs.domain.generated.GeneratedPuzzleVarietyPolicy
import org.cescfe.numpairs.domain.generated.GenerationPolicy
import org.cescfe.numpairs.domain.generated.InitialStripMaskPolicy
import org.cescfe.numpairs.domain.generated.RequiredKnownStripAnchor
import org.cescfe.numpairs.domain.generated.ResultConstraints
import org.cescfe.numpairs.domain.generated.StripKnownEntryDistributionPolicy
import org.cescfe.numpairs.domain.generated.StripValuePolicy
import org.cescfe.numpairs.domain.generated.getOrThrow
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.PuzzleCompletionState
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfiguredGeneratedPuzzleProviderFactoryTest {
    @Test
    fun every_registered_mode_uses_its_configured_profile_and_a_shared_context() {
        val factory = ConfiguredGeneratedPuzzleProviderFactory()

        GeneratedModes.registry.all.forEach { mode ->
            val puzzle = factory.create(modeId = mode.id, seed = 2026).nextPuzzle()

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
    fun seeded_mode_provider_delegates_to_the_domain_generator_for_the_resolved_profile() {
        val expectedPuzzle = GeneratedPairsPuzzleGenerator(
            profile = GeneratedPuzzleProfiles.EIGHT_PAIRS_MEDIUM,
            seed = 1234
        ).generate()

        val providedPuzzle = ConfiguredGeneratedPuzzleProviderFactory()
            .create(modeId = GeneratedModes.EIGHT_PAIRS.id, seed = 1234)
            .nextPuzzle()

        assertEquals(expectedPuzzle, providedPuzzle)
    }

    @Test
    fun a_test_only_third_mode_needs_only_one_configuration_entry() {
        val thirdMode = GeneratedModeConfiguration(
            id = GeneratedModeId("test-two-pairs"),
            profile = testTwoPairsProfile()
        )
        val registry = GeneratedModeRegistry(GeneratedModes.registry.all + thirdMode)
        val factory = ConfiguredGeneratedPuzzleProviderFactory(modeRegistry = registry)

        val puzzle = factory.create(modeId = thirdMode.id, seed = 42).nextPuzzle()

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
