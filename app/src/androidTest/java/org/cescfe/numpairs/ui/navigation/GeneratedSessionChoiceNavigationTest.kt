package org.cescfe.numpairs.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.GeneratedModeConfiguration
import org.cescfe.numpairs.feature.generated.GeneratedModeId
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeneratedSessionChoiceNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun generated_mode_starts_directly_when_no_session_is_resumable() {
        val repository = FakeGeneratedSessionRepository()
        val recorder = setContent(repository)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.FOUR_PAIRS.id), recorder.generatedModes)
        }
    }

    @Test
    fun same_mode_choice_keeps_resume_primary_and_opens_the_stored_session() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .performClick()

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_mode_message,
                    string(R.string.four_pairs_screen_title)
                )
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_RESUME_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_new_mode_button,
                    string(R.string.four_pairs_screen_title)
                )
            )
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(recorder.generatedModes.isEmpty())
            assertEquals(snapshot, repository.session.value)
        }

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_RESUME_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(recorder.generatedModes.isEmpty())
            assertEquals(snapshot, repository.session.value)
        }
    }

    @Test
    fun different_mode_secondary_action_replaces_with_the_selected_mode() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)
            .performClick()

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_mode_message,
                    string(R.string.four_pairs_screen_title)
                )
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_RESUME_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_new_mode_button,
                    string(R.string.eight_pairs_screen_title)
                )
            )
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_NEW_PUZZLE_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.EIGHT_PAIRS.id), recorder.generatedModes)
            assertEquals(GeneratedModes.EIGHT_PAIRS.id.value, repository.session.value?.modeId)
        }
    }

    @Test
    fun system_back_and_outside_tap_dismiss_without_side_effects() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)
            .performClick()
        assertChoiceDialogVisible()
        pressBackUnconditionally()
        assertDismissedWithoutSideEffects(snapshot, repository, recorder)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .performClick()
        assertChoiceDialogVisible()
        composeTestRule
            .onRoot()
            .performTouchInput {
                click(Offset(1f, 1f))
            }
        assertDismissedWithoutSideEffects(snapshot, repository, recorder)
    }

    private fun assertChoiceDialogVisible() {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_DIALOG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.generated_session_choice_title))
            .assertIsDisplayed()
    }

    private fun assertDismissedWithoutSideEffects(
        snapshot: GeneratedSessionSnapshot,
        repository: FakeGeneratedSessionRepository,
        recorder: GenerationRecorder
    ) {
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(recorder.generatedModes.isEmpty())
            assertEquals(snapshot, repository.session.value)
        }
    }

    private fun setContent(repository: FakeGeneratedSessionRepository): GenerationRecorder {
        val recorder = GenerationRecorder()
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = repository,
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedModeRegistry = GeneratedModes.registry,
                    generatedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory { mode ->
                        GeneratedPuzzleGenerationUseCase { request ->
                            recorder.generatedModes += mode.id
                            GeneratedPuzzleGenerationResult.Generated(
                                request = request,
                                initialPuzzle = initialPuzzleFor(mode)
                            )
                        }
                    }
                )
            }
        }
        return recorder
    }

    private fun initialPuzzleFor(mode: GeneratedModeConfiguration): Puzzle = when (mode.id) {
        GeneratedModes.FOUR_PAIRS.id -> samplePuzzle
        GeneratedModes.EIGHT_PAIRS.id -> eightPairsPuzzle()
        else -> error("Unsupported test mode ${mode.id.value}.")
    }

    private fun eightPairsPuzzle(): Puzzle = Puzzle(
        board = Board(
            tiles = List(EIGHT_PAIRS_ENTRY_COUNT) { index ->
                Tile(
                    expression = Expression(
                        leftOperand = Expression.Operand.Hidden,
                        operator = Operator.Hidden,
                        rightOperand = Expression.Operand.Hidden
                    ),
                    result = index + 2
                )
            }
        ),
        strip = Strip.fromItems(
            items = List(EIGHT_PAIRS_ENTRY_COUNT) {
                StripItem.Hidden
            }
        )
    )

    private fun string(stringResId: Int, vararg formatArgs: Any): String =
        composeTestRule.activity.getString(stringResId, *formatArgs)

    private class GenerationRecorder {
        val generatedModes = mutableListOf<GeneratedModeId>()
    }

    private companion object {
        const val EIGHT_PAIRS_ENTRY_COUNT = 16
    }
}

private fun resumableFourPairsSnapshot(): GeneratedSessionSnapshot = GeneratedSessionSnapshot(
    sessionId = GeneratedSessionId("session-choice"),
    modeId = GeneratedModes.FOUR_PAIRS.id.value,
    profileId = GeneratedModes.FOUR_PAIRS.profile.id.value,
    seed = 214,
    initialPuzzle = samplePuzzle,
    currentPuzzle = samplePuzzle
)
