package org.cescfe.numpairs.feature.fourpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerationOutcome
import org.cescfe.numpairs.domain.generated.generation.GeneratedPairsPuzzleGenerator
import org.cescfe.numpairs.domain.generated.generation.GeneratedPuzzleGenerationRequest
import org.cescfe.numpairs.domain.generated.profile.GeneratedPuzzleProfiles
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FourPairsCompletionActionsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val profile = GeneratedPuzzleProfiles.FOUR_PAIRS_LOW

    @Test
    fun newPuzzleActionGeneratesFreshPuzzleAndClearsPreviousGameState() {
        val firstSolvedPuzzle = generatedPuzzle(seed = 2026).solvedPuzzle
        val firstPuzzle = firstSolvedPuzzle.withHiddenOperatorAt(tileIndex = 0)
        val secondPuzzle = generatedPuzzle(seed = 42).initialPuzzle
        assertNotEquals(firstPuzzle.board.tiles[0].result, secondPuzzle.board.tiles[0].result)
        val puzzleProvider = QueueGeneratedPuzzleProvider(firstPuzzle, secondPuzzle)
        val generatedSessionRepository = FakeGeneratedSessionRepository()

        setContent(
            puzzleProvider = puzzleProvider,
            generatedSessionRepository = generatedSessionRepository
        )

        navigateToFourPairs()
        completeFirstTile(operator = firstSolvedPuzzle.board.tiles[0].expression.operator)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.success_overlay_new_puzzle_button))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_RETURN_TO_MENU)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.success_overlay_return_to_menu_button))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_NEW_PUZZLE)
            .performClick()

        assertEquals(2, puzzleProvider.requestCount)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR, useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERAND_SELECTOR, useUnmergedTree = true)
            .assertDoesNotExist()

        val stripMask = currentStripMask()
        assertEquals(3, stripMask.knownEntryIds.size)
        assertEquals(5, stripMask.hiddenEntryIds.size)
        assertFirstTileExpressionIsHidden()
        assertFirstTileShowsResult(secondPuzzle.board.tiles[0].result)
        composeTestRule.runOnIdle {
            assertEquals(secondPuzzle, generatedSessionRepository.session.value?.currentPuzzle)
            assertEquals(GeneratedModes.FOUR_PAIRS.id.value, generatedSessionRepository.session.value?.modeId)
        }
    }

    @Test
    fun returnToMenuActionNavigatesBackToMenuAfterCompletion() {
        val solvedPuzzle = generatedPuzzle(seed = 81).solvedPuzzle
        val initialPuzzle = solvedPuzzle.withHiddenOperatorAt(tileIndex = 0)
        val puzzleProvider = QueueGeneratedPuzzleProvider(initialPuzzle)
        val generatedSessionRepository = FakeGeneratedSessionRepository()

        setContent(
            puzzleProvider = puzzleProvider,
            generatedSessionRepository = generatedSessionRepository
        )

        navigateToFourPairs()
        completeFirstTile(operator = solvedPuzzle.board.tiles[0].expression.operator)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_RETURN_TO_MENU)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.success_overlay_return_to_menu_button))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_RETURN_TO_MENU)
            .performClick()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.RESUME_BUTTON)
            .assertDoesNotExist()
        composeTestRule.runOnIdle {
            assertNull(generatedSessionRepository.session.value)
        }
    }

    @Test
    fun rulesHelperOpensAndDismissesInFourPairsWithoutRegeneratingPuzzle() {
        val initialPuzzle = generatedPuzzle(seed = 1234).initialPuzzle
        val puzzleProvider = QueueGeneratedPuzzleProvider(initialPuzzle)

        setContent(puzzleProvider = puzzleProvider)

        navigateToFourPairs()
        val initialStripMask = currentStripMask()
        assertEquals(1, puzzleProvider.requestCount)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(string(R.string.rules_helper_action_content_description))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_CLOSE_BUTTON)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(string(R.string.rules_helper_close_content_description))
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertDoesNotExist()
        assertEquals(1, puzzleProvider.requestCount)
        assertEquals(initialStripMask, currentStripMask())

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertIsDisplayed()

        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
        assertEquals(1, puzzleProvider.requestCount)
        assertEquals(initialStripMask, currentStripMask())
    }

    private fun setContent(
        puzzleProvider: QueueGeneratedPuzzleProvider,
        generatedSessionRepository: FakeGeneratedSessionRepository = FakeGeneratedSessionRepository()
    ) {
        val actionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository()

        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = generatedSessionRepository,
                    topAppBarActionDiscoveryRepository = actionDiscoveryRepository,
                    generatedModeRegistry = GeneratedModes.registry,
                    generatedPuzzleGenerationUseCaseFactory = fourPairsProviderFactory(puzzleProvider = puzzleProvider)
                )
            }
        }
    }

    private fun navigateToFourPairs() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .assertIsDisplayed()
            .performClick()
    }

    private fun completeFirstTile(operator: Operator) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
    }

    private fun assertFirstTileExpressionIsHidden() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileLeftOperand(0), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_left_operand_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_operator_hidden_content_description))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileRightOperand(0), useUnmergedTree = true)
            .assertContentDescriptionEquals(string(R.string.tile_right_operand_hidden_content_description))
    }

    private fun assertFirstTileShowsResult(result: Int) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tile(0), useUnmergedTree = true)
            .assert(hasAnyDescendant(hasText(result.toString())))
    }

    private fun currentStripMask(): StripMask {
        val hiddenContentDescription = string(R.string.strip_item_hidden_content_description)
        val knownContentDescriptionPrefix = string(
            R.string.strip_item_known_content_description,
            ""
        )
        val knownEntryIds = mutableListOf<Int>()
        val hiddenEntryIds = mutableListOf<Int>()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP)
            .performScrollTo()

        repeat(profile.size.stripEntryCount) { index ->
            val contentDescriptions = composeTestRule
                .onNodeWithTag(GameScreenTestTags.stripItem(index))
                .fetchSemanticsNode()
                .config[SemanticsProperties.ContentDescription]

            when {
                hiddenContentDescription in contentDescriptions -> hiddenEntryIds += index
                contentDescriptions.any { description ->
                    description.startsWith(knownContentDescriptionPrefix)
                } -> knownEntryIds += index
            }
        }

        return StripMask(
            knownEntryIds = knownEntryIds,
            hiddenEntryIds = hiddenEntryIds
        )
    }

    private fun Puzzle.withHiddenOperatorAt(tileIndex: Int): Puzzle = copy(
        board = Board(
            tiles = board.tiles.toMutableList().apply {
                val tile = get(tileIndex)
                set(
                    tileIndex,
                    tile.copy(
                        expression = tile.expression.copy(operator = Operator.Hidden)
                    )
                )
            }
        )
    )

    private fun generatedPuzzle(seed: Int) = when (
        val outcome = GeneratedPairsPuzzleGenerator(profile = profile).generate(
            request = GeneratedPuzzleGenerationRequest(profile = profile, seed = seed)
        )
    ) {
        is GeneratedPairsPuzzleGenerationOutcome.Generated -> outcome.puzzle
        is GeneratedPairsPuzzleGenerationOutcome.Failed -> error("Expected a generated 4 Pairs puzzle.")
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }

    private fun fourPairsProviderFactory(
        puzzleProvider: QueueGeneratedPuzzleProvider
    ): GeneratedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory { mode ->
        require(mode == GeneratedModes.FOUR_PAIRS)
        GeneratedPuzzleGenerationUseCase { request ->
            GeneratedPuzzleGenerationResult.Generated(
                request = request,
                initialPuzzle = puzzleProvider.nextPuzzle()
            )
        }
    }

    private class QueueGeneratedPuzzleProvider(private vararg val puzzles: Puzzle) {
        var requestCount = 0
            private set

        fun nextPuzzle(): Puzzle {
            val puzzle = puzzles.getOrNull(requestCount)
                ?: error("No fake 4 Pairs puzzle configured for request $requestCount.")
            requestCount += 1

            return puzzle
        }
    }

    private data class StripMask(val knownEntryIds: List<Int>, val hiddenEntryIds: List<Int>)
}
