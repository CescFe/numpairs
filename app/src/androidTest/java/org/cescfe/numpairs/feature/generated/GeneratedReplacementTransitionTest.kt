package org.cescfe.numpairs.feature.generated

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CompletableDeferred
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.domain.puzzle.assignment.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.assignment.StripEntryId
import org.cescfe.numpairs.domain.puzzle.construction.resolvedTile
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenRobot
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneratedReplacementTransitionTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun fourPairsKeepsCompletionUntilTheAdoptedSuccessorTransitionsOnce() {
        assertSafeReplacementTransition(
            challenge = GeneratedModes.FOUR_PAIRS_LOW,
            menuButtonTag = MenuScreenTestTags.FOUR_PAIRS_BUTTON
        )
    }

    @Test
    fun eightPairsKeepsCompletionUntilTheAdoptedSuccessorTransitionsOnce() {
        assertSafeReplacementTransition(
            challenge = GeneratedModes.EIGHT_PAIRS_MEDIUM,
            menuButtonTag = MenuScreenTestTags.EIGHT_PAIRS_BUTTON
        )
    }

    private fun assertSafeReplacementTransition(challenge: GeneratedChallenge, menuButtonTag: String) {
        val successor = CompletableDeferred<Puzzle>()
        val puzzleProvider = ControlledReplacementPuzzleProvider(
            initialPuzzle = oneOperatorAwayFromSolvedReplacementPuzzle(),
            successor = successor
        )
        val useCaseFactory = GeneratedPuzzleGenerationUseCaseFactory { requestedChallenge ->
            require(requestedChallenge == challenge)
            GeneratedPuzzleGenerationUseCase { request ->
                GeneratedPuzzleGenerationResult.Generated(
                    request = request,
                    initialPuzzle = puzzleProvider.nextPuzzle()
                )
            }
        }
        var recompositionMarker by mutableIntStateOf(0)

        composeTestRule.setContent {
            NumPairsTheme {
                Box {
                    AppNavigation(
                        onboardingRepository = FakeOnboardingRepository(),
                        generatedSessionRepository = FakeGeneratedSessionRepository(),
                        personalizationPreferencesRepository = FakePersonalizationPreferencesRepository(
                            initialPreferences = PersonalizationPreferences(
                                generatedGameHapticsEnabled = false
                            )
                        ),
                        topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                        generatedChallengeCatalog = GeneratedModes.catalog,
                        generatedPuzzleGenerationUseCaseFactory = useCaseFactory
                    )
                    Text(text = recompositionMarker.toString())
                }
            }
        }

        composeTestRule
            .onNodeWithTag(menuButtonTag)
            .performClick()
        gameRobot()
            .scrollToBoard()
            .tapTileOperator(index = 1)
            .tapOperatorOption(Operator.MULTIPLICATION)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
            .performClick()

        composeTestRule
            .onNodeWithTag(GENERATED_PUZZLE_LOADING_TAG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(2, puzzleProvider.requestCount)
        }

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.runOnIdle {
            successor.complete(oneOperatorAwayFromSolvedReplacementPuzzle())
        }
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(GENERATED_PUZZLE_CONTENT_TAG)
                .fetchSemanticsNodes()
                .singleOrNull()
                ?.config
                ?.contains(GeneratedReplacementTransitionKey) == true
        }

        composeTestRule
            .onNodeWithTag(GENERATED_PUZZLE_LOADING_TAG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
        val transition = composeTestRule
            .onNodeWithTag(GENERATED_PUZZLE_CONTENT_TAG)
            .fetchSemanticsNode()
            .config[GeneratedReplacementTransitionKey]
        assertNotEquals(transition.predecessorSessionId, transition.successorSessionId)

        composeTestRule.mainClock.advanceTimeBy(500)
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(GENERATED_PUZZLE_CONTENT_TAG)
            .assert(SemanticsMatcher.keyNotDefined(GeneratedReplacementTransitionKey))

        composeTestRule.runOnIdle {
            recompositionMarker += 1
        }
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(GENERATED_PUZZLE_CONTENT_TAG)
            .assert(SemanticsMatcher.keyNotDefined(GeneratedReplacementTransitionKey))
    }

    private fun gameRobot(): GameScreenRobot = GameScreenRobot(
        activity = composeTestRule.activity,
        interactions = composeTestRule
    )
}

private class ControlledReplacementPuzzleProvider(
    private val initialPuzzle: Puzzle,
    private val successor: CompletableDeferred<Puzzle>
) {
    var requestCount = 0
        private set

    suspend fun nextPuzzle(): Puzzle {
        requestCount += 1

        return when (requestCount) {
            1 -> initialPuzzle
            2 -> successor.await()
            else -> error("Duplicate replacement request was not deduplicated.")
        }
    }
}

private fun oneOperatorAwayFromSolvedReplacementPuzzle(): Puzzle {
    val firstOperand = ResolvedOperandAssignment(value = 1, stripEntryId = StripEntryId(0))
    val secondOperand = ResolvedOperandAssignment(value = 2, stripEntryId = StripEntryId(1))
    val additionTile = resolvedTile(
        leftOperand = firstOperand,
        operator = Operator.ADDITION,
        rightOperand = secondOperand
    )
    val multiplicationTile = resolvedTile(
        leftOperand = firstOperand,
        operator = Operator.MULTIPLICATION,
        rightOperand = secondOperand
    )

    return Puzzle(
        board = Board(
            tiles = listOf(
                additionTile,
                multiplicationTile.copy(
                    expression = multiplicationTile.expression.copy(
                        operator = Operator.Hidden
                    )
                )
            )
        ),
        strip = Strip.fromItems(
            items = listOf(
                StripItem.Known(1),
                StripItem.Known(2)
            )
        )
    )
}
