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
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.generated.selection.FakeGeneratedDifficultySelectionRepository
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
import org.cescfe.numpairs.feature.time.ElapsedTimeReading
import org.cescfe.numpairs.testing.fourPairsQuickSelector
import org.cescfe.numpairs.testing.threePairsQuickSelector
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.navigation.navigateToSelectedGeneratedChallenge
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val FIRST_COMPLETION_FEEDBACK_ID = 1L

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
                            puzzleResetKey = "completion-celebration",
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

        assertCompletionFeedback()
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
        assertCompletionFeedback()

        composeTestRule.runOnIdle {
            routeCompositionKey += 1
        }
        composeTestRule.waitForIdle()

        assertCompletedUiWithoutFeedback()
    }

    @Test
    fun threePairsQuickUsesAStandardCompletionCelebration() {
        assertGeneratedModeOptsInToCompletionCelebration(
            menuButtonTag = MenuScreenTestTags.QUICK_BUTTON,
            challengeSelector = threePairsQuickSelector()
        )
    }

    @Test
    fun fourPairsQuickUsesAStandardCompletionCelebration() {
        assertGeneratedModeOptsInToCompletionCelebration(
            menuButtonTag = MenuScreenTestTags.QUICK_BUTTON,
            challengeSelector = fourPairsQuickSelector()
        )
    }

    @Test
    fun eightPairsClassicUsesAStandardCompletionCelebration() {
        assertGeneratedModeOptsInToCompletionCelebration(
            menuButtonTag = MenuScreenTestTags.CLASSIC_BUTTON,
            challengeSelector = fourPairsQuickSelector(),
            expectedCopy = R.string.completion_celebration_correction_free_title to
                R.string.completion_celebration_correction_free_supporting_text
        )
    }

    @Test
    fun generated_completion_overlay_uses_the_exact_frozen_chronometer_duration() {
        var reading = ElapsedTimeReading(epochMilliseconds = 1_000, monotonicMilliseconds = 100)
        composeTestRule.setContent {
            NumPairsTheme {
                GeneratedModeRoute(
                    challenge = GeneratedModes.FOUR_PAIRS_LOW,
                    title = "Quick · Low",
                    generationUseCase = { request ->
                        GeneratedPuzzleGenerationResult.Generated(
                            request = request,
                            initialPuzzle = oneOperatorAwayFromSolvedPuzzle()
                        )
                    },
                    generatedSessionRepository = FakeGeneratedSessionRepository(),
                    timeSource = { reading }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(GENERATED_CHRONOMETER_VALUE_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextEquals("00:00")
        composeTestRule.runOnIdle {
            reading = ElapsedTimeReading(
                epochMilliseconds = 126_999,
                monotonicMilliseconds = 126_099
            )
        }
        gameRobot()
            .scrollToBoard()
            .tapTileOperator(index = 1)
            .tapOperatorOption(Operator.MULTIPLICATION)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_HIGHLIGHT)
            .assertIsDisplayed()
            .assertTextEquals("02:05")
            .assertContentDescriptionEquals("Elapsed time: 02:05")
    }

    private fun assertGeneratedModeOptsInToCompletionCelebration(
        menuButtonTag: String,
        challengeSelector: GeneratedPlayChallengeSelector,
        expectedCopy: Pair<Int, Int>? = null
    ) {
        var recompositionMarker by mutableIntStateOf(0)
        composeTestRule.setContent {
            NumPairsTheme {
                Column {
                    Text(text = recompositionMarker.toString())
                    AppNavigation(
                        onboardingRepository = FakeOnboardingRepository(),
                        generatedSessionRepository = FakeGeneratedSessionRepository(),
                        generatedDifficultySelectionRepository = FakeGeneratedDifficultySelectionRepository(),
                        personalizationPreferencesRepository = FakePersonalizationPreferencesRepository(
                            initialPreferences = PersonalizationPreferences(
                                generatedGameHapticsEnabled = false
                            )
                        ),
                        topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                        generatedPlayChallengeSelector = challengeSelector,
                        generatedChallengeCatalog = GeneratedModes.catalog,
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
        }

        composeTestRule.navigateToSelectedGeneratedChallenge(menuButtonTag)
        composeTestRule
            .onNodeWithTag(GENERATED_CHRONOMETER_TAG)
            .assertIsDisplayed()
            .assertHasClickAction()
        gameRobot()
            .scrollToBoard()
            .tapTileOperator(index = 1)
            .tapOperatorOption(Operator.MULTIPLICATION)

        assertCompletionFeedback()
        val selectedCopy = assertStandardCelebrationDisplayed(expectedCopy = expectedCopy)
        composeTestRule.runOnIdle {
            recompositionMarker += 1
        }
        composeTestRule.waitForIdle()
        assertStandardCelebrationDisplayed(expectedCopy = selectedCopy)
    }

    private fun assertStandardCelebrationDisplayed(expectedCopy: Pair<Int, Int>? = null): Pair<Int, Int> {
        composeTestRule
            .onNodeWithContentDescription(string(R.string.success_overlay_badge_content_description))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("OK")
            .assertDoesNotExist()

        val selectedCopy = expectedCopy ?: STANDARD_CELEBRATION_COPY.single { (titleResource, _) ->
            composeTestRule.onAllNodesWithText(string(titleResource)).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule
            .onNodeWithText(string(selectedCopy.first))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(selectedCopy.second))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_RETURN_TO_MENU)
            .assertIsDisplayed()
        return selectedCopy
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

        composeTestRule
            .onNodeWithTag(GENERATED_CHRONOMETER_TAG)
            .assertDoesNotExist()

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

    private fun string(resourceId: Int): String = composeTestRule.activity.getString(resourceId)

    private fun assertCompletionFeedback() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .assert(
                SemanticsMatcher.expectValue(
                    CompletionFeedbackIdKey,
                    FIRST_COMPLETION_FEEDBACK_ID
                )
            )
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.expectValue(
                    CompletionFeedbackIdKey,
                    FIRST_COMPLETION_FEEDBACK_ID
                )
            )
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

private val STANDARD_CELEBRATION_COPY = listOf(
    R.string.completion_celebration_great_work_title to
        R.string.completion_celebration_great_work_supporting_text,
    R.string.completion_celebration_excellent_title to
        R.string.completion_celebration_excellent_supporting_text,
    R.string.completion_celebration_you_rock_title to
        R.string.completion_celebration_you_rock_supporting_text,
    R.string.completion_celebration_nailed_it_title to
        R.string.completion_celebration_nailed_it_supporting_text,
    R.string.completion_celebration_brilliant_title to
        R.string.completion_celebration_brilliant_supporting_text,
    R.string.completion_celebration_correction_free_title to
        R.string.completion_celebration_correction_free_supporting_text,
    R.string.completion_celebration_impressive_title to
        R.string.completion_celebration_impressive_supporting_text,
    R.string.completion_celebration_unstoppable_title to
        R.string.completion_celebration_unstoppable_supporting_text
)

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
