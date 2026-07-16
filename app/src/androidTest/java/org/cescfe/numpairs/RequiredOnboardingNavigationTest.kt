package org.cescfe.numpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.data.onboarding.completedOnboardingState
import org.cescfe.numpairs.data.onboarding.incompleteOnboardingState
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.feature.onboarding.ONBOARDING_LOADING_SCREEN_TEST_TAG
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequiredOnboardingNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun uninitializedStateWaitsBeforeRoutingAndFreshStateStartsStageOne() {
        val repository = FakeOnboardingRepository(initialState = OnboardingState())
        setContent(repository)

        composeTestRule
            .onNodeWithTag(ONBOARDING_LOADING_SCREEN_TEST_TAG)
            .assertIsDisplayed()

        repository.onboardingState.value = incompleteOnboardingState()

        composeTestRule
            .onNodeWithText(string(R.string.tutorial_stage_one_place_number_copy))
            .assertIsDisplayed()
    }

    @Test
    fun completedStateStartsAtTheUnlockedMenu() {
        setContent(FakeOnboardingRepository(completedOnboardingState()))

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    @Test
    fun completedCheckpointsResumeAtTheNextRequiredStageOrValidation() {
        val repository = FakeOnboardingRepository(
            incompleteOnboardingState(OnboardingStageCheckpoint.STAGE_ONE)
        )
        setContent(repository)
        composeTestRule
            .onNodeWithText(string(R.string.tutorial_stage_two_complete_sum_copy))
            .assertIsDisplayed()

        repository.onboardingState.value = incompleteOnboardingState(OnboardingStageCheckpoint.STAGE_TWO)
        composeTestRule
            .onNodeWithText(string(R.string.tutorial_stage_three_hidden_strip_value_copy))
            .assertIsDisplayed()

        repository.onboardingState.value = incompleteOnboardingState(OnboardingStageCheckpoint.STAGE_THREE)
        composeTestRule
            .onNodeWithText(string(R.string.final_validation_screen_title))
            .assertIsDisplayed()
    }

    @Test
    fun systemBackCannotExposeTheMenuWhileOnboardingIsIncomplete() {
        setContent(FakeOnboardingRepository(incompleteOnboardingState()))

        pressBackUnconditionally()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BACK_BUTTON)
            .performClick()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.INSTRUCTION_SURFACE)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    @Test
    fun persistedValidationCompletionUnlocksTheMenu() {
        val repository = FakeOnboardingRepository(
            incompleteOnboardingState(OnboardingStageCheckpoint.STAGE_THREE)
        )
        setContent(repository)

        repository.onboardingState.value = completedOnboardingState()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun setContent(repository: FakeOnboardingRepository) {
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = repository,
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedModeRegistry = GeneratedModes.registry,
                    generatedPuzzleGenerationUseCaseFactory = generatedPuzzleFactory()
                )
            }
        }
    }

    private fun generatedPuzzleFactory(): GeneratedPuzzleGenerationUseCaseFactory =
        GeneratedPuzzleGenerationUseCaseFactory {
            GeneratedPuzzleGenerationUseCase { request ->
                GeneratedPuzzleGenerationResult.Generated(request, samplePuzzle)
            }
        }

    private fun string(stringResId: Int): String = composeTestRule.activity.getString(stringResId)
}
