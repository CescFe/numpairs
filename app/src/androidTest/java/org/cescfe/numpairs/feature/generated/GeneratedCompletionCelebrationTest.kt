package org.cescfe.numpairs.feature.generated

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.cescfe.numpairs.feature.game.GameCompletionActions
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenRobot
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.game.ui.semantics.CompletionFeedbackIdKey
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneratedCompletionCelebrationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun enabledRouteCoordinatesOneTransientResponseAndKeepsCompletionActionsAvailable() {
        var initialPuzzle by mutableStateOf(oneOperatorAwayFromSolvedPuzzle())
        var recompositionMarker by mutableIntStateOf(0)
        var routeCompositionKey by mutableIntStateOf(0)
        var newPuzzleRequests = 0
        var returnToMenuRequests = 0

        composeTestRule.setContent {
            NumPairsTheme {
                Column {
                    Text(text = recompositionMarker.toString())
                    key(routeCompositionKey) {
                        GameRoute(
                            title = "4 pairs",
                            initialPuzzle = initialPuzzle,
                            gameSessionKey = "completion-celebration",
                            completionActions = GameCompletionActions(
                                onNewPuzzleRequested = { newPuzzleRequests += 1 },
                                onReturnToMenuRequested = { returnToMenuRequests += 1 }
                            ),
                            isCompletionCelebrationEnabled = true,
                            onPuzzleChanged = { puzzle -> initialPuzzle = puzzle }
                        )
                    }
                }
            }
        }

        gameRobot()
            .scrollToBoard()
            .tapTileOperator(index = 1)
            .tapOperatorOption(Operator.MULTIPLICATION)

        assertCompletionFeedback(feedbackId = 1L)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_RETURN_TO_MENU)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, newPuzzleRequests)
            assertEquals(1, returnToMenuRequests)
            recompositionMarker += 1
        }
        composeTestRule.waitForIdle()
        assertCompletionFeedback(feedbackId = 1L)

        composeTestRule.runOnIdle {
            routeCompositionKey += 1
        }
        composeTestRule.waitForIdle()

        assertCompletedUiWithoutFeedback()
    }

    @Test
    fun fourPairsOptsInToCompletionCelebration() {
        assertGeneratedModeOptsInToCompletionCelebration(
            menuButtonTag = MenuScreenTestTags.FOUR_PAIRS_BUTTON
        )
    }

    @Test
    fun eightPairsOptsInToCompletionCelebration() {
        assertGeneratedModeOptsInToCompletionCelebration(
            menuButtonTag = MenuScreenTestTags.EIGHT_PAIRS_BUTTON
        )
    }

    private fun assertGeneratedModeOptsInToCompletionCelebration(menuButtonTag: String) {
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = FakeGeneratedSessionRepository(),
                    personalizationPreferencesRepository = FakePersonalizationPreferencesRepository(
                        initialPreferences = PersonalizationPreferences(
                            generatedGameHapticsEnabled = false
                        )
                    ),
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedModeRegistry = GeneratedModes.registry,
                    generatedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory {
                        GeneratedPuzzleGenerationUseCase { request ->
                            GeneratedPuzzleGenerationResult.Generated(
                                request = request,
                                initialPuzzle = oneOperatorAwayFromSolvedPuzzle()
                            )
                        }
                    }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(menuButtonTag)
            .performClick()
        gameRobot()
            .scrollToBoard()
            .tapTileOperator(index = 1)
            .tapOperatorOption(Operator.MULTIPLICATION)

        assertCompletionFeedback(feedbackId = 1L)
    }

    @Test
    fun genericRouteShowsTheCompletedUiWithoutCelebration() {
        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "Tutorial",
                    initialPuzzle = oneOperatorAwayFromSolvedPuzzle(),
                    gameSessionKey = "generic-completion"
                )
            }
        }

        gameRobot()
            .scrollToBoard()
            .tapTileOperator(index = 1)
            .tapOperatorOption(Operator.MULTIPLICATION)

        assertCompletedUiWithoutFeedback()
    }

    @Test
    fun initiallySolvedStateShowsTheFinalOverlayWithoutCelebration() {
        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "Restored",
                    initialPuzzle = solvedPuzzle(),
                    gameSessionKey = "restored-completion",
                    isCompletionCelebrationEnabled = true
                )
            }
        }

        assertCompletedUiWithoutFeedback()
    }

    private fun gameRobot(): GameScreenRobot = GameScreenRobot(
        activity = composeTestRule.activity,
        interactions = composeTestRule
    )

    private fun assertCompletionFeedback(feedbackId: Long) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .assert(SemanticsMatcher.expectValue(CompletionFeedbackIdKey, feedbackId))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(CompletionFeedbackIdKey, feedbackId))
    }

    private fun assertCompletedUiWithoutFeedback() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .assert(SemanticsMatcher.keyNotDefined(CompletionFeedbackIdKey))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyNotDefined(CompletionFeedbackIdKey))
    }
}

private fun solvedPuzzle(): Puzzle {
    val firstOperand = ResolvedOperandAssignment(value = 1, stripEntryId = StripEntryId(0))
    val secondOperand = ResolvedOperandAssignment(value = 2, stripEntryId = StripEntryId(1))

    return Puzzle(
        board = Board(
            tiles = listOf(
                resolvedTile(
                    leftOperand = firstOperand,
                    operator = Operator.ADDITION,
                    rightOperand = secondOperand
                ),
                resolvedTile(
                    leftOperand = firstOperand,
                    operator = Operator.MULTIPLICATION,
                    rightOperand = secondOperand
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

private fun oneOperatorAwayFromSolvedPuzzle(): Puzzle {
    val solvedPuzzle = solvedPuzzle()
    val multiplicationTile = solvedPuzzle.board.tiles[1]

    return solvedPuzzle.copy(
        board = Board(
            tiles = solvedPuzzle.board.tiles.toMutableList().apply {
                set(
                    1,
                    multiplicationTile.copy(
                        expression = multiplicationTile.expression.copy(
                            operator = Operator.Hidden
                        )
                    )
                )
            }
        )
    )
}
