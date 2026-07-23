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
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationResult
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.feature.tutorial.TutorialContent
import org.cescfe.numpairs.feature.tutorial.TutorialMode
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.navigation.navigateToSelectedGeneratedChallenge
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationLearningFlowTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun howToPlayReplaysFromStepOneAndBackPreservesEveryResolvedOutcome() {
        val initialState = resolvedOnboardingState(FirstRunTutorialOutcome.COMPLETED)
        val onboardingRepository = FakeOnboardingRepository(initialState = initialState)
        setContent(
            puzzleProvider = QueueGeneratedPuzzleProvider(samplePuzzle),
            onboardingRepository = onboardingRepository
        )

        FirstRunTutorialOutcome.entries
            .filter(FirstRunTutorialOutcome::isResolved)
            .forEach { outcome ->
                val resolvedState = resolvedOnboardingState(outcome)
                onboardingRepository.onboardingState.value = resolvedState

                composeTestRule
                    .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
                    .assert(hasText(string(R.string.menu_tutorial_button)))
                    .performClick()
                composeTestRule
                    .onNodeWithText(string(R.string.tutorial_objective_explanation_copy))
                    .assertIsDisplayed()

                pressBackUnconditionally()

                composeTestRule
                    .onNodeWithTag(MenuScreenTestTags.SCREEN)
                    .assertIsDisplayed()
                assertEquals(resolvedState, onboardingRepository.onboardingState.value)
            }
    }

    @Test
    fun continuingFromCompletedHowToPlayReturnsToMenuWithoutChangingOnboarding() {
        val initialState = resolvedOnboardingState(FirstRunTutorialOutcome.COMPLETED)
        val onboardingRepository = FakeOnboardingRepository(initialState = initialState)
        setContent(
            puzzleProvider = QueueGeneratedPuzzleProvider(samplePuzzle),
            onboardingRepository = onboardingRepository
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.TUTORIAL_BUTTON)
            .performClick()
        completeLearnBasicsTutorial()
        assertTutorialSuccessOverlayDisplayed()
        assertEquals(initialState, onboardingRepository.onboardingState.value)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_PRIMARY_ACTION)
            .performClick()

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        assertEquals(initialState, onboardingRepository.onboardingState.value)
    }

    private fun resolvedOnboardingState(outcome: FirstRunTutorialOutcome): OnboardingState = OnboardingState(
        lastCompletedStage = OnboardingStageCheckpoint.EXPLANATION_COMPLETED,
        firstRunTutorialOutcome = outcome
    )

    @Test
    fun inGameLearningActionsOpenTutorialOverlaysAndReturnToCurrentFourPairsScreen() {
        val puzzleProvider = QueueGeneratedPuzzleProvider(samplePuzzle)
        setContent(puzzleProvider = puzzleProvider)

        navigateToFourPairs()
        enterPreservedStripValue()
        assertPreservedStripItemPlayerEntered()
        assertEquals(1, puzzleProvider.requestCount)

        openLearnBasicsFromRulesHelper()
        closeOverlayAndAssertCurrentFourPairsScreen()

        openSolvingTipsPracticeFromHintAction()
        closeOverlayAndAssertCurrentFourPairsScreen()
        assertEquals(1, puzzleProvider.requestCount)
    }

    @Test
    fun continuingFromCompletedRulesHelperTutorialRestoresCurrentFourPairsPuzzle() {
        val puzzleProvider = QueueGeneratedPuzzleProvider(samplePuzzle)
        setContent(puzzleProvider = puzzleProvider)

        navigateToFourPairs()
        enterPreservedStripValue()
        openLearnBasicsFromRulesHelper()
        completeLearnBasicsTutorial()
        assertTutorialSuccessOverlayDisplayed()

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_PRIMARY_ACTION)
            .performClick()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        assertPreservedStripItemPlayerEntered()
        assertEquals(1, puzzleProvider.requestCount)
    }

    private fun setContent(
        puzzleProvider: QueueGeneratedPuzzleProvider,
        onboardingRepository: FakeOnboardingRepository = FakeOnboardingRepository()
    ) {
        val actionDiscoveryRepository = FakeTopAppBarActionDiscoveryRepository()

        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = onboardingRepository,
                    generatedSessionRepository = FakeGeneratedSessionRepository(),
                    generatedDifficultySelectionRepository = FakeGeneratedDifficultySelectionRepository(),
                    personalizationPreferencesRepository = FakePersonalizationPreferencesRepository(),
                    topAppBarActionDiscoveryRepository = actionDiscoveryRepository,
                    generatedChallengeCatalog = GeneratedModes.catalog,
                    generatedPuzzleGenerationUseCaseFactory = fourPairsProviderFactory(puzzleProvider = puzzleProvider)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
    }

    private fun navigateToFourPairs() {
        composeTestRule.navigateToSelectedGeneratedChallenge(MenuScreenTestTags.FOUR_PAIRS_BUTTON)

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
    }

    private fun openLearnBasicsFromRulesHelper() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_ACTION)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.RULES_HELPER_PLAY_TUTORIAL_BUTTON)
            .assertIsDisplayed()
            .performClick()

        assertTutorialOverlayMode(TutorialMode.LEARN_BASICS)
    }

    private fun openSolvingTipsPracticeFromHintAction() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.HINT_ACTION)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_PRACTICE_BUTTON)
            .assertIsDisplayed()
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SOLVING_TIPS_DIALOG)
            .assertDoesNotExist()

        assertTutorialOverlayMode(TutorialMode.SOLVING_TIPS_PRACTICE)
    }

    private fun assertTutorialOverlayMode(mode: TutorialMode) {
        val steps = TutorialContent.stepsFor(mode)

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.STEP_INDICATOR)
            .performScrollTo()
            .assert(
                hasText(
                    string(
                        R.string.tutorial_step_indicator,
                        1,
                        steps.size
                    )
                )
            )
    }

    private fun closeOverlayAndAssertCurrentFourPairsScreen() {
        pressBackUnconditionally()

        composeTestRule
            .onNodeWithTag(TutorialScreenTestTags.FULL_SCREEN_OVERLAY)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertDoesNotExist()
        assertPreservedStripItemPlayerEntered()
    }

    private fun completeLearnBasicsTutorial() {
        repeat(TutorialContent.learnBasicsSteps.lastIndex) {
            composeTestRule
                .onNodeWithTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
                .performScrollTo()
                .performClick()
        }

        enterStripValue(index = 0, value = "1")
        enterStripValue(index = 1, value = "2")
        completeTile(tileIndex = 0, leftStripEntryId = 0, operator = Operator.ADDITION, rightStripEntryId = 1)
        completeTile(tileIndex = 1, leftStripEntryId = 0, operator = Operator.MULTIPLICATION, rightStripEntryId = 1)
        completeTile(tileIndex = 2, leftStripEntryId = 2, operator = Operator.ADDITION, rightStripEntryId = 3)
        completeTile(tileIndex = 3, leftStripEntryId = 2, operator = Operator.MULTIPLICATION, rightStripEntryId = 3)
    }

    private fun assertTutorialSuccessOverlayDisplayed() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SUCCESS_OVERLAY_PRIMARY_ACTION)
            .assert(hasText(string(R.string.tutorial_success_overlay_continue_button)))
    }

    private fun enterPreservedStripValue() {
        enterStripValue(
            index = PRESERVED_STRIP_ITEM_INDEX,
            value = PRESERVED_STRIP_ITEM_VALUE
        )
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

    private fun assertPreservedStripItemPlayerEntered() {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.stripItem(PRESERVED_STRIP_ITEM_INDEX))
            .performScrollTo()
            .assertContentDescriptionEquals(
                string(
                    R.string.strip_item_player_entered_content_description,
                    PRESERVED_STRIP_ITEM_VALUE
                )
            )
    }

    private fun string(stringResId: Int, vararg formatArgs: Any): String = if (formatArgs.isEmpty()) {
        composeTestRule.activity.getString(stringResId)
    } else {
        composeTestRule.activity.getString(stringResId, *formatArgs)
    }

    private fun fourPairsProviderFactory(
        puzzleProvider: QueueGeneratedPuzzleProvider
    ): GeneratedPuzzleGenerationUseCaseFactory = GeneratedPuzzleGenerationUseCaseFactory { challenge ->
        require(challenge == GeneratedModes.FOUR_PAIRS_LOW)
        GeneratedPuzzleGenerationUseCase { request ->
            GeneratedPuzzleGenerationResult.Generated(
                request = request,
                initialPuzzle = puzzleProvider.nextPuzzle()
            )
        }
    }

    private class QueueGeneratedPuzzleProvider(private val puzzle: Puzzle) {
        var requestCount = 0
            private set

        fun nextPuzzle(): Puzzle {
            requestCount += 1
            return puzzle
        }
    }

    private companion object {
        const val PRESERVED_STRIP_ITEM_INDEX = 1
        const val PRESERVED_STRIP_ITEM_VALUE = "2"
    }
}
