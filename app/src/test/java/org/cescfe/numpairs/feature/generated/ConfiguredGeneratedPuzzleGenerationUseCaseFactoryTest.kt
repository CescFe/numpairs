package org.cescfe.numpairs.feature.generated

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
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
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfiguredGeneratedPuzzleGenerationUseCaseFactoryTest {
    @Test
    fun every_registered_challenge_uses_its_profile_and_a_shared_context() = runBlocking {
        val factory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            generationDispatcher = Dispatchers.Unconfined
        )

        GeneratedModes.catalog.allChallenges.forEach { challenge ->
            val outcome = factory.create(challenge = challenge).generate(
                GeneratedPuzzleGenerationRequest(profile = challenge.profile, seed = 2026)
            )
            val puzzle = (outcome as GeneratedPuzzleGenerationResult.Generated).initialPuzzle

            assertEquals(challenge.profile.size.boardTileCount, puzzle.board.tiles.size)
            assertEquals(challenge.profile.size.stripEntryCount, puzzle.strip.entries.size)
            assertEquals(PuzzleCompletionState.INCOMPLETE, puzzle.completionState)
            assertTrue(puzzle.board.tiles.all(Tile::hasHiddenExpression))
            assertTrue(
                puzzle.strip.entries.count { entry -> entry.item is StripItem.Known } in
                    challenge.profile.initialStripMaskPolicy.knownEntryCountRange
            )
            assertTrue(
                puzzle.strip.entries.count { entry -> entry.item == StripItem.Hidden } in
                    challenge.profile.hiddenEntryCountRange
            )
            assertSame(
                factory.contextFor(challenge = challenge),
                factory.contextFor(challenge = challenge)
            )
        }
    }

    @Test
    fun identical_requests_produce_identical_puzzles_without_sharing_a_generator_random_stream() = runBlocking {
        val challenge = GeneratedModes.EIGHT_PAIRS_MEDIUM
        val factory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            generationDispatcher = Dispatchers.Unconfined
        )
        val request = GeneratedPuzzleGenerationRequest(profile = challenge.profile, seed = 1234)

        val first = factory.create(challenge = challenge).generate(request)
        val second = factory.create(challenge = challenge).generate(request)

        assertEquals(first, second)
    }

    @Test
    fun a_test_only_third_mode_needs_only_one_challenge_entry() = runBlocking {
        val profile = testTwoPairsProfile()
        val modeId = GeneratedModeId("test-two-pairs")
        val challenge = GeneratedChallenge(
            id = GeneratedChallengeId("test-two-pairs-low"),
            modeId = modeId,
            difficulty = DifficultyTier.LOW,
            profile = profile
        )
        val thirdMode = GeneratedModeConfiguration(
            id = modeId,
            size = profile.size,
            challenges = listOf(challenge)
        )
        val catalog = GeneratedChallengeCatalog(GeneratedModes.catalog.all + thirdMode)
        val factory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            challengeCatalog = catalog,
            generationDispatcher = Dispatchers.Unconfined
        )

        val outcome = factory.create(challenge = challenge).generate(
            GeneratedPuzzleGenerationRequest(profile = profile, seed = 42)
        )
        val puzzle = (outcome as GeneratedPuzzleGenerationResult.Generated).initialPuzzle

        assertEquals(thirdMode.size.boardTileCount, puzzle.board.tiles.size)
        assertEquals(thirdMode.size.stripEntryCount, puzzle.strip.entries.size)
    }

    @Test
    fun an_unconfigured_challenge_cannot_create_generation_state() {
        val unconfigured = GeneratedModes.FOUR_PAIRS_LOW.copy(
            id = GeneratedChallengeId("unconfigured-four-pairs-low")
        )
        val factory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            generationDispatcher = Dispatchers.Unconfined
        )

        assertThrows(IllegalArgumentException::class.java) {
            factory.contextFor(challenge = unconfigured)
        }
    }
}

private fun Tile.hasHiddenExpression(): Boolean = expression.leftOperand == Expression.Operand.Hidden &&
    expression.operator == Operator.Hidden &&
    expression.rightOperand == Expression.Operand.Hidden

private fun testTwoPairsProfile(): GeneratedPuzzleProfile = GeneratedPuzzleProfile.create(
    definition = GeneratedPuzzleProfileDefinition(
        id = GeneratedPuzzleProfileId("test-two-pairs"),
        difficulty = DifficultyTier.LOW,
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
