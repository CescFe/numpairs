package org.cescfe.numpairs.feature.generated

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenRobot
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.game.ui.semantics.CorrectTileFeedbackIdKey
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CorrectTileMotionTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun enabledRouteTargetsOneTileOnceAndAllowsANewResponseAfterReset() {
        var recompositionMarker by mutableStateOf(0)
        var puzzleResetKey by mutableStateOf(0)

        composeTestRule.setContent {
            NumPairsTheme {
                Column {
                    Text(text = recompositionMarker.toString())
                    GameRoute(
                        title = "4 pairs",
                        initialPuzzle = oneOperatorAwayPuzzle(),
                        gameSessionKey = "correct-tile-motion",
                        puzzleResetKey = puzzleResetKey,
                        isCorrectTileMotionEnabled = true
                    )
                }
            }
        }
        val game = gameRobot()

        game.scrollToBoard()
            .tapTileOperator(index = 0)
            .tapOperatorOption(Operator.ADDITION)
        assertTileFeedback(tileIndex = 0, feedbackId = 1L)
        assertNoTileFeedback(tileIndex = 1)

        composeTestRule.runOnIdle {
            recompositionMarker += 1
        }
        composeTestRule.waitForIdle()
        assertTileFeedback(tileIndex = 0, feedbackId = 1L)

        game.tapTileReset(index = 0)
        assertNoTileFeedback(tileIndex = 0)
        game.tapTileLeftOperand(index = 0)
            .tapOperandOption(entryId = 0)
            .tapTileOperator(index = 0)
            .tapOperatorOption(Operator.ADDITION)
            .tapTileRightOperand(index = 0)
            .tapOperandOption(entryId = 1)
        assertTileFeedback(tileIndex = 0, feedbackId = 2L)

        composeTestRule.runOnIdle {
            puzzleResetKey += 1
        }
        composeTestRule.waitForIdle()
        assertNoTileFeedback(tileIndex = 0)
    }

    @Test
    fun fourPairsRouteEnablesCorrectTileMotion() {
        assertGeneratedRouteEnablesCorrectTileMotion(
            menuButtonTag = MenuScreenTestTags.FOUR_PAIRS_BUTTON
        )
    }

    @Test
    fun eightPairsRouteEnablesCorrectTileMotion() {
        assertGeneratedRouteEnablesCorrectTileMotion(
            menuButtonTag = MenuScreenTestTags.EIGHT_PAIRS_BUTTON
        )
    }

    private fun assertGeneratedRouteEnablesCorrectTileMotion(menuButtonTag: String) {
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
                    generatedChallengeCatalog = GeneratedModes.catalog,
                    generatedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory {
                        GeneratedPuzzleGenerationUseCase { request ->
                            GeneratedPuzzleGenerationResult.Generated(
                                request = request,
                                initialPuzzle = oneOperatorAwayPuzzle()
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
            .tapTileOperator(index = 0)
            .tapOperatorOption(Operator.ADDITION)

        assertTileFeedback(tileIndex = 0, feedbackId = 1L)
    }

    @Test
    fun genericGameRouteKeepsCorrectTileMotionDisabledByDefault() {
        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = "Tutorial",
                    initialPuzzle = oneOperatorAwayPuzzle(),
                    gameSessionKey = "generic-correct-tile-motion"
                )
            }
        }

        gameRobot()
            .scrollToBoard()
            .tapTileOperator(index = 0)
            .tapOperatorOption(Operator.ADDITION)

        assertNoTileFeedback(tileIndex = 0)
    }

    private fun gameRobot(): GameScreenRobot = GameScreenRobot(
        activity = composeTestRule.activity,
        interactions = composeTestRule
    )

    private fun assertTileFeedback(tileIndex: Int, feedbackId: Long) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(tileIndex), useUnmergedTree = true)
            .assert(SemanticsMatcher.expectValue(CorrectTileFeedbackIdKey, feedbackId))
    }

    private fun assertNoTileFeedback(tileIndex: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(tileIndex), useUnmergedTree = true)
            .assert(SemanticsMatcher.keyNotDefined(CorrectTileFeedbackIdKey))
    }
}

private fun oneOperatorAwayPuzzle(): Puzzle = Puzzle(
    board = Board(
        tiles = listOf(
            Tile(
                expression = Expression(
                    leftOperand = Expression.Operand.Known(value = 1, stripEntryId = 0),
                    operator = Operator.Hidden,
                    rightOperand = Expression.Operand.Known(value = 2, stripEntryId = 1)
                ),
                result = 3
            ),
            Tile(
                expression = Expression(
                    leftOperand = Expression.Operand.Hidden,
                    operator = Operator.Hidden,
                    rightOperand = Expression.Operand.Hidden
                ),
                result = 7
            )
        )
    ),
    strip = Strip.fromItems(
        items = listOf(1, 2, 3, 4).map(StripItem::Known)
    )
)
