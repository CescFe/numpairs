package org.cescfe.numpairs.feature.generated

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationFailureReason
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneratedPuzzleGenerationRouteTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun initial_failure_renders_retry_then_shows_the_ready_game_without_blocking_the_ui_thread() {
        val firstAttempt = CompletableDeferred<Boolean>()
        val retryAttempt = CompletableDeferred<Boolean>()
        val useCase = RetryingGeneratedPuzzleUseCase(
            firstAttempt = firstAttempt,
            retryAttempt = retryAttempt
        )

        composeTestRule.setContent {
            NumPairsTheme {
                GeneratedModeRoute(
                    mode = GeneratedModes.FOUR_PAIRS,
                    title = "4 pairs",
                    generationUseCase = useCase,
                    generatedSessionRepository = FakeGeneratedSessionRepository()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GENERATED_PUZZLE_LOADING_TAG)
            .assertIsDisplayed()

        composeTestRule.runOnIdle {
            firstAttempt.complete(true)
        }
        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodesWithTag(GENERATED_PUZZLE_FAILURE_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(GENERATED_PUZZLE_FAILURE_TAG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.generated_puzzle_retry_button))
            .performClick()

        composeTestRule
            .onNodeWithTag(GENERATED_PUZZLE_LOADING_TAG)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            retryAttempt.complete(true)
        }
        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodesWithTag(GameScreenTestTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    @Test
    fun resume_shows_the_stored_current_puzzle_without_generating_a_replacement() {
        val currentPuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 1)
        )
        val snapshot = generatedSessionSnapshot(currentPuzzle = currentPuzzle)
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val useCase = CountingGeneratedPuzzleUseCase()

        composeTestRule.setContent {
            NumPairsTheme {
                GeneratedModeRoute(
                    mode = GeneratedModes.FOUR_PAIRS,
                    launchIntent = GeneratedModeLaunchIntent.ResumeSession(snapshot.sessionId),
                    title = "4 pairs",
                    generationUseCase = useCase,
                    generatedSessionRepository = repository
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(0, useCase.requestCount)
            assertEquals(snapshot, repository.session.value)
        }
    }

    @Test
    fun unavailable_resume_offers_a_safe_route_back() {
        var didNavigateBack = false
        val useCase = CountingGeneratedPuzzleUseCase()

        composeTestRule.setContent {
            NumPairsTheme {
                GeneratedModeRoute(
                    mode = GeneratedModes.FOUR_PAIRS,
                    launchIntent = GeneratedModeLaunchIntent.ResumeSession(
                        expectedSessionId = GeneratedSessionId("missing")
                    ),
                    title = "4 pairs",
                    generationUseCase = useCase,
                    generatedSessionRepository = FakeGeneratedSessionRepository(),
                    onNavigateBack = {
                        didNavigateBack = true
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GENERATED_SESSION_RESUME_UNAVAILABLE_TAG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.generated_puzzle_back_to_menu_button))
            .performClick()
        composeTestRule.runOnIdle {
            assertTrue(didNavigateBack)
            assertEquals(0, useCase.requestCount)
        }
    }

    private fun string(stringResId: Int): String = composeTestRule.activity.getString(stringResId)
}

private class RetryingGeneratedPuzzleUseCase(
    private val firstAttempt: CompletableDeferred<Boolean>,
    private val retryAttempt: CompletableDeferred<Boolean>
) : GeneratedPuzzleGenerationUseCase {
    private var attempts = 0

    override suspend fun generate(request: GeneratedPuzzleGenerationRequest): GeneratedPuzzleGenerationResult {
        attempts++

        return when (attempts) {
            1 -> {
                firstAttempt.await()
                GeneratedPuzzleGenerationResult.Failed(
                    GeneratedPairsPuzzleGenerationOutcome.Failed(
                        request = request,
                        attemptsUsed = 1,
                        searchWorkConsumed = 1,
                        reason = GeneratedPairsPuzzleGenerationFailureReason.AttemptsExhausted,
                        candidateRejections = emptyList()
                    )
                )
            }

            2 -> {
                retryAttempt.await()
                GeneratedPuzzleGenerationResult.Generated(
                    request = request,
                    initialPuzzle = samplePuzzle
                )
            }

            else -> error("Unexpected generated puzzle attempt.")
        }
    }
}

private class CountingGeneratedPuzzleUseCase : GeneratedPuzzleGenerationUseCase {
    var requestCount: Int = 0

    override suspend fun generate(request: GeneratedPuzzleGenerationRequest): GeneratedPuzzleGenerationResult {
        requestCount++
        return GeneratedPuzzleGenerationResult.Generated(
            request = request,
            initialPuzzle = samplePuzzle
        )
    }
}

private fun generatedSessionSnapshot(currentPuzzle: Puzzle) = GeneratedSessionSnapshot(
    sessionId = GeneratedSessionId("resume-session"),
    modeId = GeneratedModes.FOUR_PAIRS.id.value,
    profileId = GeneratedModes.FOUR_PAIRS.profile.id.value,
    seed = 211,
    initialPuzzle = samplePuzzle,
    currentPuzzle = currentPuzzle
)
