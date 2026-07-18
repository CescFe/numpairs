package org.cescfe.numpairs.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
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
class GeneratedSessionResumeNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun menu_hides_resume_without_a_resumable_session() {
        setContent(repository = FakeGeneratedSessionRepository())

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.RESUME_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun menu_orders_resume_first_and_opens_the_exact_session_without_generation() {
        val currentPuzzle = samplePuzzle.copy(
            strip = samplePuzzle.strip.withUpdatedEntry(index = 1, value = 2)
        )
        val snapshot = GeneratedSessionSnapshot(
            sessionId = GeneratedSessionId("resume-from-menu"),
            modeId = GeneratedModes.FOUR_PAIRS.id.value,
            profileId = GeneratedModes.FOUR_PAIRS_LOW.profile.id.value,
            seed = 213,
            initialPuzzle = samplePuzzle,
            currentPuzzle = currentPuzzle
        )
        val repository = FakeGeneratedSessionRepository(initialSession = snapshot)
        val generationCounter = GenerationCounter()
        setContent(repository = repository, generationCounter = generationCounter)

        val resumeNode = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.RESUME_BUTTON)
            .assertIsDisplayed()
            .assertContentDescriptionEquals(
                string(
                    R.string.menu_resume_content_description,
                    challengeName(R.string.four_pairs_screen_title, R.string.generated_difficulty_low)
                )
            )
        val resumeTop = resumeNode.fetchSemanticsNode().boundsInRoot.top
        val fourPairsTop = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.FOUR_PAIRS_BUTTON)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val eightPairsTop = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.EIGHT_PAIRS_BUTTON)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val tutorialTop = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val personalizationTop = composeTestRule
            .onNodeWithTag(MenuScreenTestTags.PERSONALIZATION_BUTTON)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        assertTrue(resumeTop < fourPairsTop)
        assertTrue(fourPairsTop < eightPairsTop)
        assertTrue(eightPairsTop < tutorialTop)
        assertTrue(tutorialTop < personalizationTop)

        resumeNode.performClick()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .performScrollTo()
            .assertContentDescriptionEquals(
                string(R.string.strip_item_player_entered_content_description, "2")
            )
        composeTestRule.runOnIdle {
            assertEquals(0, generationCounter.count)
            assertEquals(snapshot, repository.session.value)
        }
    }

    private fun setContent(
        repository: FakeGeneratedSessionRepository,
        generationCounter: GenerationCounter = GenerationCounter()
    ) {
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = repository,
                    generatedDifficultySelectionRepository = FakeGeneratedDifficultySelectionRepository(),
                    personalizationPreferencesRepository = FakePersonalizationPreferencesRepository(),
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedChallengeCatalog = GeneratedModes.catalog,
                    generatedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory {
                        GeneratedPuzzleGenerationUseCase { request ->
                            generationCounter.count++
                            GeneratedPuzzleGenerationResult.Generated(
                                request = request,
                                initialPuzzle = samplePuzzle
                            )
                        }
                    }
                )
            }
        }
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String =
        composeTestRule.activity.getString(stringResId, *formatArgs)

    private fun challengeName(modeNameResource: Int, difficultyNameResource: Int): String = string(
        R.string.generated_challenge_title,
        string(modeNameResource),
        string(difficultyNameResource)
    )

    private class GenerationCounter {
        var count: Int = 0
    }
}
