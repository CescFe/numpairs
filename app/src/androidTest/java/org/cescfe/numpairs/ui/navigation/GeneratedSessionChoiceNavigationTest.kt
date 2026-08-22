package org.cescfe.numpairs.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import org.cescfe.numpairs.feature.generated.GeneratedPlayChallengeSelector
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.ui.GeneratedDifficultyMenuOptionId
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
    fun quick_starts_directly_when_no_session_is_resumable() {
        val repository = FakeGeneratedSessionRepository()
        val recorder = setContent(repository)

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.QUICK_BUTTON)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.FOUR_PAIRS_LOW), recorder.generatedChallenges)
            assertEquals(1, recorder.quickSelectionCount)
        }
    }

    @Test
    fun quick_can_select_three_pairs_directly_without_writing_difficulty() {
        val repository = FakeGeneratedSessionRepository()
        val difficultyRepository = FakeGeneratedDifficultySelectionRepository()
        val recorder = setContent(
            repository = repository,
            difficultyRepository = difficultyRepository,
            quickBucket = 0
        )

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.QUICK_BUTTON)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                challengeName(R.string.quick_screen_title, R.string.generated_difficulty_low)
            )
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.THREE_PAIRS_LOW), recorder.generatedChallenges)
            assertEquals(GeneratedModes.THREE_PAIRS.id.value, repository.session.value?.modeId)
            assertEquals(
                GeneratedModes.THREE_PAIRS_LOW.profile.id.value,
                repository.session.value?.profileId
            )
            assertTrue(difficultyRepository.explicitSelections.isEmpty())
        }
    }

    @Test
    fun same_option_choice_keeps_resume_primary_and_opens_the_stored_session() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.QUICK_BUTTON)

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_challenge_message,
                    challengeName(R.string.quick_screen_title, R.string.generated_difficulty_low)
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
                    challengeName(R.string.quick_screen_title, R.string.generated_difficulty_low)
                )
            )
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(recorder.generatedChallenges.isEmpty())
            assertEquals(0, recorder.quickSelectionCount)
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

        composeTestRule.onNodeWithTag(MenuScreenTestTags.QUICK_DIFFICULTY_BUTTON).performClick()
        composeTestRule
            .onNodeWithTag(
                MenuScreenTestTags.difficultyOption(
                    GeneratedDifficultyMenuOptionId("medium")
                )
            )
            .performClick()
        composeTestRule.onNodeWithTag(MenuScreenTestTags.QUICK_BUTTON).performClick()

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_challenge_message,
                    challengeName(R.string.quick_screen_title, R.string.generated_difficulty_low)
                )
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_new_challenge_button,
                    challengeName(R.string.quick_screen_title, R.string.generated_difficulty_medium)
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
    fun different_option_secondary_action_replaces_with_classic() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.CLASSIC_BUTTON)

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_challenge_message,
                    challengeName(R.string.quick_screen_title, R.string.generated_difficulty_low)
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
                    challengeName(R.string.classic_screen_title, R.string.generated_difficulty_medium)
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
    fun quick_uses_the_shared_choice_dialog_and_a_new_quick_replacement_action() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val difficultyRepository = FakeGeneratedDifficultySelectionRepository()
        val recorder = setContent(
            repository = repository,
            difficultyRepository = difficultyRepository,
            quickBucket = 0
        )

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.QUICK_BUTTON)

        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_challenge_message,
                    challengeName(R.string.quick_screen_title, R.string.generated_difficulty_low)
                )
            )
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                string(
                    R.string.generated_session_choice_new_challenge_button,
                    challengeName(R.string.quick_screen_title, R.string.generated_difficulty_low)
                )
            )
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(0, recorder.quickSelectionCount)
        }
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_NEW_PUZZLE_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.THREE_PAIRS_LOW), recorder.generatedChallenges)
            assertEquals(1, recorder.quickSelectionCount)
            assertEquals(GeneratedModes.THREE_PAIRS.id.value, repository.session.value?.modeId)
            assertTrue(difficultyRepository.explicitSelections.isEmpty())
        }
    }

    @Test
    fun system_back_and_outside_tap_dismiss_without_side_effects() {
        val snapshot = resumableFourPairsSnapshot()
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val recorder = setContent(repository)

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.CLASSIC_BUTTON)
        assertChoiceDialogVisible()
        pressBackUnconditionally()
        assertDismissedWithoutSideEffects(snapshot, repository, recorder)

        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.QUICK_BUTTON)
        assertChoiceDialogVisible()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SESSION_CHOICE_DIALOG)
            .performTouchInput {
                click(Offset(-1f, -1f))
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
            assertTrue(recorder.generatedChallenges.isEmpty())
            assertEquals(snapshot, repository.session.value)
        }
    }

    private fun setContent(
        repository: FakeGeneratedSessionRepository,
        difficultyRepository: FakeGeneratedDifficultySelectionRepository =
            FakeGeneratedDifficultySelectionRepository(),
        quickBucket: Int = 99
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
                    generatedPlayChallengeSelector = GeneratedPlayChallengeSelector(
                        challengeCatalog = GeneratedModes.catalog,
                        quickBucketSource = {
                            recorder.quickSelectionCount += 1
                            quickBucket
                        }
                    ),
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
        GeneratedModes.THREE_PAIRS.id -> quickPuzzle()
        GeneratedModes.FOUR_PAIRS.id -> samplePuzzle
        GeneratedModes.EIGHT_PAIRS.id -> eightPairsPuzzle()
        else -> error("Unsupported test mode ${challenge.modeId.value}.")
    }

    private fun quickPuzzle(): Puzzle = Puzzle(
        board = Board(tiles = samplePuzzle.board.tiles.take(6)),
        strip = Strip.fromItems(
            items = listOf(
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(5),
                StripItem.Hidden,
                StripItem.Hidden,
                StripItem.Known(15)
            )
        )
    )

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
        var quickSelectionCount = 0
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
