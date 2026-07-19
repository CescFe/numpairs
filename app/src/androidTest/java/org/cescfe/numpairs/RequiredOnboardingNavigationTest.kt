package org.cescfe.numpairs

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cescfe.numpairs.data.generated.selection.FakeGeneratedDifficultySelectionRepository
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.onboarding.FirstRunTutorialOutcome
import org.cescfe.numpairs.data.onboarding.OnboardingStageCheckpoint
import org.cescfe.numpairs.data.onboarding.OnboardingState
import org.cescfe.numpairs.data.onboarding.completedOnboardingState
import org.cescfe.numpairs.data.onboarding.incompleteOnboardingState
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.feature.onboarding.ONBOARDING_LOADING_SCREEN_TEST_TAG
import org.cescfe.numpairs.feature.onboarding.RequiredOnboardingTestTags
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RequiredOnboardingNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun unresolvedFreshStateOpensTutorialBeforeMenu() {
        val repository = FakeOnboardingRepository(initialState = OnboardingState())
        setContent(repository)

        composeTestRule
            .onNodeWithTag(ONBOARDING_LOADING_SCREEN_TEST_TAG)
            .assertIsDisplayed()

        repository.onboardingState.value = incompleteOnboardingState()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_COPY)
            .assert(hasText(string(R.string.tutorial_strip_introduction_copy)))
        assertRequiredSkipActionDisplayed()
        assertRequiredSkipActionPositionedOutsideInstruction()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    @Test
    fun resolvedStateStartsAtTheUnlockedMenu() {
        setContent(FakeOnboardingRepository(completedOnboardingState()))

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    @Test
    fun interruptedFirstRunResumesAfterEachCompletedCheckpoint() {
        val repository = FakeOnboardingRepository(
            incompleteOnboardingState(OnboardingStageCheckpoint.STAGE_ONE)
        )
        setContent(repository)

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_COPY)
            .assert(hasText(string(R.string.tutorial_tiles_introduction_copy)))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(1))
            .assertContentDescriptionEquals(
                string(R.string.strip_item_player_entered_content_description, "3")
            )
        assertRequiredSkipActionDisplayed()

        repository.onboardingState.value = incompleteOnboardingState(OnboardingStageCheckpoint.STAGE_TWO)

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_COPY)
            .assert(hasText(string(R.string.tutorial_repeated_value_practice_copy)))
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(0))
            .assertContentDescriptionEquals(string(R.string.strip_item_hidden_content_description))
        assertRequiredSkipActionDisplayed()
    }

    @Test
    fun completedTutorialStepsPersistMonotonicResumeCheckpoints() {
        val repository = FakeOnboardingRepository(incompleteOnboardingState())
        setContent(repository)

        enterStripValue(index = 1, value = "3")
        waitForTutorialCopy(R.string.tutorial_tiles_introduction_copy)
        assertEquals(OnboardingStageCheckpoint.STAGE_ONE, repository.onboardingState.value.lastCompletedStage)

        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        waitForTutorialCopy(R.string.tutorial_repeated_value_practice_copy)
        assertEquals(OnboardingStageCheckpoint.STAGE_TWO, repository.onboardingState.value.lastCompletedStage)
    }

    @Test
    fun continueAndBackDismissSkipConfirmationWithoutChangingTheStep() {
        val repository = FakeOnboardingRepository(incompleteOnboardingState())
        setContent(repository)

        openSkipConfirmation()
        composeTestRule
            .onNodeWithTag(RequiredOnboardingTestTags.CONTINUE_TUTORIAL_BUTTON)
            .assert(hasText(string(R.string.onboarding_continue_tutorial_button)))
            .performClick()
        assertStillOnFirstStep(repository)

        pressBackUnconditionally()
        composeTestRule
            .onNodeWithTag(RequiredOnboardingTestTags.SKIP_CONFIRMATION_DIALOG)
            .assertIsDisplayed()
        pressBackUnconditionally()
        assertStillOnFirstStep(repository)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BACK_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(RequiredOnboardingTestTags.SKIP_CONFIRMATION_DIALOG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(RequiredOnboardingTestTags.CONTINUE_TUTORIAL_BUTTON)
            .performClick()
        assertStillOnFirstStep(repository)
    }

    @Test
    fun confirmedSkipPersistsSkippedOutcomeAndOpensMenuDirectly() {
        val repository = FakeOnboardingRepository(incompleteOnboardingState())
        setContent(repository)

        openSkipConfirmation()
        composeTestRule
            .onNodeWithTag(RequiredOnboardingTestTags.SKIP_ANYWAY_BUTTON)
            .assert(hasText(string(R.string.onboarding_skip_anyway_button)))
            .performClick()

        waitForMenu()
        assertEquals(FirstRunTutorialOutcome.SKIPPED, repository.onboardingState.value.firstRunTutorialOutcome)
    }

    @Test
    fun completingStepThreePersistsCompletedOutcomeAndOpensMenuDirectly() {
        val repository = FakeOnboardingRepository(
            incompleteOnboardingState(OnboardingStageCheckpoint.STAGE_TWO)
        )
        setContent(repository)

        enterStripValue(index = 0, value = "1")
        enterStripValue(index = 1, value = "2")
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        completeTile(tileIndex = 1, leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
        completeTile(tileIndex = 2, leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)
        completeTile(tileIndex = 3, leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)

        waitForMenu()
        assertEquals(FirstRunTutorialOutcome.COMPLETED, repository.onboardingState.value.firstRunTutorialOutcome)
    }

    @Test
    fun completedFinalCheckpointUnlocksWithoutRestoringObsoleteValidation() {
        val repository = FakeOnboardingRepository(
            incompleteOnboardingState(OnboardingStageCheckpoint.STAGE_THREE)
        )
        setContent(repository)

        waitForMenu()
        assertEquals(FirstRunTutorialOutcome.COMPLETED, repository.onboardingState.value.firstRunTutorialOutcome)
    }

    private fun openSkipConfirmation() {
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.SKIP_ACTION)
            .performClick()
        composeTestRule
            .onNodeWithTag(RequiredOnboardingTestTags.SKIP_CONFIRMATION_DIALOG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.onboarding_skip_tutorial_title))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(string(R.string.onboarding_skip_tutorial_message))
            .assertIsDisplayed()
    }

    private fun assertRequiredSkipActionDisplayed() {
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.SKIP_ACTION)
            .assertIsDisplayed()
            .assert(hasText(string(R.string.onboarding_skip_tutorial_action)))
    }

    private fun assertRequiredSkipActionPositionedOutsideInstruction() {
        val screenBounds = composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .fetchSemanticsNode()
            .boundsInRoot
        val instructionBounds = composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.INSTRUCTION_SURFACE)
            .fetchSemanticsNode()
            .boundsInRoot
        val skipBounds = composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.SKIP_ACTION)
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(skipBounds.top >= instructionBounds.bottom)
        assertTrue(skipBounds.left >= screenBounds.center.x)
        assertTrue(skipBounds.right <= screenBounds.right)
        assertTrue(skipBounds.bottom < screenBounds.bottom)
    }

    private fun assertStillOnFirstStep(repository: FakeOnboardingRepository) {
        composeTestRule
            .onNodeWithTag(RequiredOnboardingTestTags.SKIP_CONFIRMATION_DIALOG)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_COPY)
            .assert(hasText(string(R.string.tutorial_strip_introduction_copy)))
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
        assertEquals(FirstRunTutorialOutcome.UNRESOLVED, repository.onboardingState.value.firstRunTutorialOutcome)
    }

    private fun enterStripValue(index: Int, value: String) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(index))
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performTextInput(value)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.STRIP_ENTRY_INPUT)
            .performImeAction()
    }

    private fun completeTile(tileIndex: Int, leftStripEntryId: Int, operator: Operator, rightStripEntryId: Int) {
        chooseTileOperand(tileIndex = tileIndex, isLeftOperand = true, stripEntryId = leftStripEntryId)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(leftStripEntryId), useUnmergedTree = true)
            .performClick()
        chooseTileOperand(tileIndex = tileIndex, isLeftOperand = false, stripEntryId = rightStripEntryId)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(rightStripEntryId), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(tileIndex), useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperatorOption(operator), useUnmergedTree = true)
            .performClick()
    }

    private fun chooseTileOperand(tileIndex: Int, isLeftOperand: Boolean, stripEntryId: Int) {
        composeTestRule
            .onNodeWithTag(
                if (isLeftOperand) {
                    GameScreenTestTags.tileLeftOperand(tileIndex)
                } else {
                    GameScreenTestTags.tileRightOperand(tileIndex)
                },
                useUnmergedTree = true
            )
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperandOption(stripEntryId), useUnmergedTree = true)
            .assertIsEnabled()
    }

    private fun waitForMenu() {
        composeTestRule.waitUntil(timeoutMillis = ONBOARDING_WAIT_TIMEOUT_MILLIS) {
            composeTestRule
                .onAllNodes(hasText(string(R.string.menu_four_pairs_button)))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun waitForTutorialCopy(copyResId: Int) {
        composeTestRule.waitUntil(timeoutMillis = ONBOARDING_WAIT_TIMEOUT_MILLIS) {
            composeTestRule
                .onAllNodes(hasText(string(copyResId)))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun setContent(repository: FakeOnboardingRepository) {
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = repository,
                    generatedSessionRepository = FakeGeneratedSessionRepository(),
                    generatedDifficultySelectionRepository = FakeGeneratedDifficultySelectionRepository(),
                    personalizationPreferencesRepository = FakePersonalizationPreferencesRepository(),
                    topAppBarActionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository(),
                    generatedChallengeCatalog = GeneratedModes.catalog,
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

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }

    private companion object {
        const val ONBOARDING_WAIT_TIMEOUT_MILLIS = 5_000L
    }
}
