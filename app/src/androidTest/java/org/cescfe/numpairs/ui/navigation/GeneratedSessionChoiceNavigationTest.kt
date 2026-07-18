package org.cescfe.numpairs.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
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
import org.cescfe.numpairs.data.generated.selection.FakeGeneratedDifficultySelectionRepository
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.generated.session.GeneratedSessionId
import org.cescfe.numpairs.data.generated.session.GeneratedSessionSnapshot
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
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
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.selector.ui.GeneratedDifficultyOptionId
import org.cescfe.numpairs.feature.generated.selector.ui.GeneratedDifficultySelectorTestTags
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

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.FOUR_PAIRS_BUTTON)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.FOUR_PAIRS_LOW), recorder.generatedChallenges)
        }
    }

    @Test
    fun same_mode_choice_keeps_resume_primary_and_opens_the_stored_session() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.FOUR_PAIRS_BUTTON)

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_challenge_message,
                    challengeName(R.string.four_pairs_screen_title, R.string.generated_difficulty_low)
                )
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_RESUME_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_new_challenge_button,
                    challengeName(R.string.four_pairs_screen_title, R.string.generated_difficulty_low)
                )
            )
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(recorder.generatedChallenges.isEmpty())
            assertEquals(snapshot, repository.session.value)
        }

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_RESUME_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(recorder.generatedChallenges.isEmpty())
            assertEquals(snapshot, repository.session.value)
        }
    }

    @Test
    fun same_mode_different_difficulty_copy_and_replacement_identify_both_challenges() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val difficultyRepository = FakeGeneratedDifficultySelectionRepository()
        val recorder = setContent(
            repository = repository,
            difficultyRepository = difficultyRepository
        )

        composeTestRule.onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON).performClick()
        composeTestRule
            .onNodeWithTag(
                GeneratedDifficultySelectorTestTags.option(
                    GeneratedDifficultyOptionId(GeneratedModes.FOUR_PAIRS_MEDIUM.id.value)
                )
            )
            .performClick()
            .assertIsSelected()
        composeTestRule.onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON).performClick()

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_challenge_message,
                    challengeName(R.string.four_pairs_screen_title, R.string.generated_difficulty_low)
                )
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_new_challenge_button,
                    challengeName(R.string.four_pairs_screen_title, R.string.generated_difficulty_medium)
                )
            )
            .performClick()

        composeTestRule.onNodeWithTag(GameScreenTestTags.SCREEN).assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.FOUR_PAIRS_MEDIUM), recorder.generatedChallenges)
            assertEquals(
                GeneratedModes.FOUR_PAIRS_MEDIUM.profile.id.value,
                repository.session.value?.profileId
            )
        }
    }

    @Test
    fun different_mode_secondary_action_replaces_with_the_selected_mode() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_challenge_message,
                    challengeName(R.string.four_pairs_screen_title, R.string.generated_difficulty_low)
                )
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_RESUME_BUTTON)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_new_challenge_button,
                    challengeName(R.string.eight_pairs_screen_title, R.string.generated_difficulty_medium)
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
            assertEquals(listOf(GeneratedModes.EIGHT_PAIRS_MEDIUM), recorder.generatedChallenges)
            assertEquals(GeneratedModes.EIGHT_PAIRS.id.value, repository.session.value?.modeId)
        }
    }

    @Test
    fun system_back_and_outside_tap_dismiss_without_side_effects() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)
        assertChoiceDialogVisible()
        pressBackUnconditionally()
        assertDismissedWithoutSideEffects(snapshot, repository, recorder)
        pressBackUnconditionally()

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
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
            .onNodeWithTag(GeneratedDifficultySelectorTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(recorder.generatedChallenges.isEmpty())
            assertEquals(snapshot, repository.session.value)
        }
    }

    private fun setContent(
        repository: FakeGeneratedSessionRepository,
        difficultyRepository: FakeGeneratedDifficultySelectionRepository =
            FakeGeneratedDifficultySelectionRepository()
    ): GenerationRecorder {
        val recorder = GenerationRecorder()
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = repository,
                    generatedDifficultySelectionRepository = difficultyRepository,
                    personalizationPreferencesRepository = FakePersonalizationPreferencesRepository(),
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedChallengeCatalog = GeneratedModes.catalog,
                    generatedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory { challenge ->
                        GeneratedPuzzleGenerationUseCase { request ->
                            recorder.generatedChallenges += challenge
                            GeneratedPuzzleGenerationResult.Generated(
                                request = request,
                                initialPuzzle = initialPuzzleFor(challenge)
                            )
                        }
                    }
                )
            }
        }
        return recorder
    }

    private fun initialPuzzleFor(challenge: GeneratedChallenge): Puzzle = when (challenge.modeId) {
        GeneratedModes.FOUR_PAIRS.id -> samplePuzzle
        GeneratedModes.EIGHT_PAIRS.id -> eightPairsPuzzle()
        else -> error("Unsupported test mode ${challenge.modeId.value}.")
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

    private fun challengeName(modeNameResource: Int, difficultyNameResource: Int): String = string(
        R.string.generated_challenge_title,
        string(modeNameResource),
        string(difficultyNameResource)
    )

    private class GenerationRecorder {
        val generatedChallenges = mutableListOf<GeneratedChallenge>()
    }

    private companion object {
        const val EIGHT_PAIRS_ENTRY_COUNT = 16
    }
}

private fun resumableFourPairsSnapshot(): GeneratedSessionSnapshot = GeneratedSessionSnapshot(
    sessionId = GeneratedSessionId("session-choice"),
    modeId = GeneratedModes.FOUR_PAIRS.id.value,
    profileId = GeneratedModes.FOUR_PAIRS_LOW.profile.id.value,
    seed = 214,
    initialPuzzle = samplePuzzle,
    currentPuzzle = samplePuzzle
)
