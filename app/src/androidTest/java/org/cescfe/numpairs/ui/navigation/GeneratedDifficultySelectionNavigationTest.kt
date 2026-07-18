package org.cescfe.numpairs.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import org.cescfe.numpairs.domain.generated.profile.DifficultyTier
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
class GeneratedDifficultySelectionNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun menu_routes_to_independent_read_only_selector_defaults() {
        val fixture = setContent()

        composeTestRule.onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON).performClick()
        composeTestRule.onNodeWithTag(optionTag(GeneratedModes.FOUR_PAIRS_LOW)).assertIsSelected()
        composeTestRule.onNodeWithTag(optionTag(GeneratedModes.FOUR_PAIRS_MEDIUM)).assertIsNotSelected()
        composeTestRule.onNodeWithTag(GeneratedDifficultySelectorTestTags.BACK_BUTTON).performClick()

        composeTestRule.onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_BUTTON).performClick()
        composeTestRule.onNodeWithTag(optionTag(GeneratedModes.EIGHT_PAIRS_MEDIUM)).assertIsSelected()
        composeTestRule.onNodeWithTag(optionTag(GeneratedModes.EIGHT_PAIRS_HARD)).assertIsNotSelected()

        composeTestRule.runOnIdle {
            assertTrue(fixture.difficultyRepository.explicitSelections.isEmpty())
            assertTrue(fixture.generatedChallenges.isEmpty())
            assertEquals(
                DifficultyTier.LOW,
                fixture.difficultyRepository.currentDifficulty(GeneratedModes.FOUR_PAIRS.id)
            )
            assertEquals(
                DifficultyTier.MEDIUM,
                fixture.difficultyRepository.currentDifficulty(GeneratedModes.EIGHT_PAIRS.id)
            )
        }
    }

    @Test
    fun explicit_four_pairs_medium_selection_persists_and_launches_that_exact_challenge() {
        val fixture = setContent()

        composeTestRule.onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON).performClick()
        composeTestRule
            .onNodeWithTag(optionTag(GeneratedModes.FOUR_PAIRS_MEDIUM))
            .performClick()
            .assertIsSelected()
        composeTestRule.onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON).performClick()

        composeTestRule.onNodeWithTag(GameScreenTestTags.SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithText(challengeName(GeneratedModes.FOUR_PAIRS_MEDIUM)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(GameScreenTestTags.HINT_ACTION).assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.FOUR_PAIRS_MEDIUM), fixture.generatedChallenges)
            assertEquals(
                GeneratedModes.FOUR_PAIRS_MEDIUM.profile.id.value,
                fixture.sessionRepository.session.value?.profileId
            )
            assertEquals(
                listOf(GeneratedModes.FOUR_PAIRS.id to DifficultyTier.MEDIUM),
                fixture.difficultyRepository.explicitSelections
            )
        }

        composeTestRule.onNodeWithTag(GameScreenTestTags.BACK_BUTTON).performClick()
        composeTestRule.onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON).performClick()
        composeTestRule.onNodeWithTag(optionTag(GeneratedModes.FOUR_PAIRS_MEDIUM)).assertIsSelected()
        composeTestRule.runOnIdle {
            assertEquals(1, fixture.difficultyRepository.explicitSelections.size)
        }
    }

    @Test
    fun eight_pairs_hard_selection_launches_and_stores_the_hard_profile() {
        val fixture = setContent()

        composeTestRule.onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_BUTTON).performClick()
        composeTestRule
            .onNodeWithTag(optionTag(GeneratedModes.EIGHT_PAIRS_HARD))
            .performClick()
            .assertIsSelected()
        composeTestRule.onNodeWithTag(GeneratedDifficultySelectorTestTags.PLAY_BUTTON).performClick()

        composeTestRule.onNodeWithTag(GameScreenTestTags.SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithText(challengeName(GeneratedModes.EIGHT_PAIRS_HARD)).assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(listOf(GeneratedModes.EIGHT_PAIRS_HARD), fixture.generatedChallenges)
            assertEquals(GeneratedModes.EIGHT_PAIRS.id.value, fixture.sessionRepository.session.value?.modeId)
            assertEquals(
                GeneratedModes.EIGHT_PAIRS_HARD.profile.id.value,
                fixture.sessionRepository.session.value?.profileId
            )
        }
    }

    @Test
    fun resume_restores_the_exact_hard_challenge_without_reading_or_writing_the_selector_choice() {
        val snapshot = GeneratedSessionSnapshot(
            sessionId = GeneratedSessionId("resume-hard"),
            modeId = GeneratedModes.EIGHT_PAIRS.id.value,
            profileId = GeneratedModes.EIGHT_PAIRS_HARD.profile.id.value,
            seed = 499,
            initialPuzzle = eightPairsPuzzle(),
            currentPuzzle = eightPairsPuzzle()
        )
        val fixture = setContent(initialSession = snapshot)

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.RESUME_BUTTON)
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_resume_content_description,
                    challengeName(GeneratedModes.EIGHT_PAIRS_HARD)
                )
            )
            .performClick()

        composeTestRule.onNodeWithTag(GameScreenTestTags.SCREEN).assertIsDisplayed()
        composeTestRule.onNodeWithText(challengeName(GeneratedModes.EIGHT_PAIRS_HARD)).assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertTrue(fixture.generatedChallenges.isEmpty())
            assertTrue(fixture.difficultyRepository.explicitSelections.isEmpty())
            assertEquals(
                DifficultyTier.MEDIUM,
                fixture.difficultyRepository.currentDifficulty(GeneratedModes.EIGHT_PAIRS.id)
            )
            assertEquals(snapshot, fixture.sessionRepository.session.value)
        }
    }

    private fun setContent(initialSession: GeneratedSessionSnapshot? = null): NavigationFixture {
        val fixture = NavigationFixture(
            sessionRepository = FakeGeneratedSessionRepository(initialSession = initialSession),
            difficultyRepository = FakeGeneratedDifficultySelectionRepository()
        )
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = fixture.sessionRepository,
                    generatedDifficultySelectionRepository = fixture.difficultyRepository,
                    personalizationPreferencesRepository = FakePersonalizationPreferencesRepository(),
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedChallengeCatalog = GeneratedModes.catalog,
                    generatedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory { challenge ->
                        GeneratedPuzzleGenerationUseCase { request ->
                            fixture.generatedChallenges += challenge
                            GeneratedPuzzleGenerationResult.Generated(
                                request = request,
                                initialPuzzle = if (challenge.modeId == GeneratedModes.FOUR_PAIRS.id) {
                                    samplePuzzle
                                } else {
                                    eightPairsPuzzle()
                                }
                            )
                        }
                    }
                )
            }
        }
        return fixture
    }

    private fun optionTag(challenge: GeneratedChallenge): String = GeneratedDifficultySelectorTestTags.option(
        GeneratedDifficultyOptionId(challenge.id.value)
    )

    private fun challengeName(challenge: GeneratedChallenge): String = string(
        R.string.generated_challenge_title,
        string(
            if (challenge.modeId == GeneratedModes.FOUR_PAIRS.id) {
                R.string.four_pairs_screen_title
            } else {
                R.string.eight_pairs_screen_title
            }
        ),
        string(
            when (challenge.difficulty) {
                DifficultyTier.LOW -> R.string.generated_difficulty_low
                DifficultyTier.MEDIUM -> R.string.generated_difficulty_medium
                DifficultyTier.HARD -> R.string.generated_difficulty_hard
            }
        )
    )

    private fun string(stringResId: Int, vararg formatArgs: Any): String =
        composeTestRule.activity.getString(stringResId, *formatArgs)

    private data class NavigationFixture(
        val sessionRepository: FakeGeneratedSessionRepository,
        val difficultyRepository: FakeGeneratedDifficultySelectionRepository,
        val generatedChallenges: MutableList<GeneratedChallenge> = mutableListOf()
    )
}

private fun eightPairsPuzzle(): Puzzle = Puzzle(
    board = Board(
        tiles = List(16) { index ->
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
        items = List(16) { StripItem.Hidden }
    )
)
