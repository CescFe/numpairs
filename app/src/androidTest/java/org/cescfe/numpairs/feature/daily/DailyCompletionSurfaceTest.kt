package org.cescfe.numpairs.feature.daily

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.data.daily.session.DailySessionClearResult
import org.cescfe.numpairs.data.daily.session.DailySessionCompletionResult
import org.cescfe.numpairs.data.daily.session.DailySessionId
import org.cescfe.numpairs.data.daily.session.DailySessionProgressUpdateResult
import org.cescfe.numpairs.data.daily.session.DailySessionReplacementResult
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.data.daily.session.DailySessionTimingStartResult
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.data.generated.selection.FakeGeneratedDifficultySelectionRepository
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.preferences.PersonalizationPreferences
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyTimingStartInstant
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.daily.calendar.DailyCalendarScreenTestTags
import org.cescfe.numpairs.feature.daily.share.DailyCompletionShareLaunchResult
import org.cescfe.numpairs.feature.daily.share.DailyCompletionShareLauncher
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.ConfiguredGeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.menu.ui.MenuScreenTestTags
import org.cescfe.numpairs.ui.navigation.AppDestination
import org.cescfe.numpairs.ui.navigation.AppNavigation
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyCompletionSurfaceTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun valid_completed_destination_shares_and_returns_from_calendar_without_mutation() {
        val identity = identity()
        val repository = RecordingDailyRepository(
            DailyState(
                activeSession = null,
                completions = listOf(completion(identity))
            )
        )
        var sharedText: String? = null
        val hapticFeedback = RecordingHapticFeedback()
        val shareLauncher = DailyCompletionShareLauncher { payload ->
            sharedText = payload.text.value
            DailyCompletionShareLaunchResult.Launched
        }

        composeTestRule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides hapticFeedback) {
                NumPairsTheme {
                    DailyCompletedTodayRoute(
                        identity = identity,
                        dailySessionRepository = repository,
                        deviceLocalDateSource = { identity.localDate },
                        shareLauncher = shareLauncher,
                        onNavigateBack = {}
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.COMPLETION_SUMMARY)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.SHARE_RESULT)
            .performClick()
        composeTestRule.runOnIdle {
            assertNotNull(sharedText)
            assertEquals(0, repository.mutationCount)
        }

        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.VIEW_CALENDAR)
            .performClick()
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.BACK_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.COMPLETION_SUMMARY)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(0, repository.mutationCount)
            assertEquals(emptyList<HapticFeedbackType>(), hapticFeedback.requestedTypes)
        }
    }

    @Test
    fun completed_daily_primary_action_uses_bold_button_typography() {
        val identity = identity()
        val repository = RecordingDailyRepository(
            DailyState(
                activeSession = null,
                completions = listOf(completion(identity))
            )
        )

        composeTestRule.setContent {
            NumPairsTheme {
                DailyCompletedTodayRoute(
                    identity = identity,
                    dailySessionRepository = repository,
                    deviceLocalDateSource = { identity.localDate },
                    onNavigateBack = {}
                )
            }
        }

        val layoutResults = mutableListOf<TextLayoutResult>()
        composeTestRule
            .onNodeWithText("Share result", useUnmergedTree = true)
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action ->
                action(layoutResults)
            }

        assertEquals(FontWeight.Bold, layoutResults.single().layoutInput.style.fontWeight)
    }

    @Test
    fun gameplay_route_prepares_the_captured_identity_and_publishes_the_game_surface() {
        val identity = identity()
        val repository = RecordingDailyRepository(
            DailyState(
                activeSession = null,
                completions = emptyList()
            )
        )

        composeTestRule.setContent {
            NumPairsTheme {
                DailyChallengeRoute(
                    identity = identity,
                    dailySessionRepository = repository,
                    deviceLocalDateSource = {
                        identity.localDate.plusDays(1)
                    },
                    generatedPuzzleGenerationUseCaseFactory =
                        ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
                            challengeCatalog = GeneratedModes.catalog
                        ),
                    isGeneratedGameHapticsEnabled = false,
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            composeTestRule
                .onAllNodesWithTag(GameScreenTestTags.SCREEN)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(1, repository.mutationCount)
            assertEquals(identity, repository.lastReplacement?.dailyChallengeId)
        }
    }

    @Test
    fun daily_gameplay_requests_assignment_haptics_only_while_the_preference_is_enabled() {
        val identity = identity()
        val repository = RecordingDailyRepository(
            DailyState(
                activeSession = generatedSnapshot(identity),
                completions = emptyList()
            )
        )
        val preferencesRepository = FakePersonalizationPreferencesRepository(
            initialPreferences = PersonalizationPreferences(
                generatedGameHapticsEnabled = false,
                compactTileSelectorsEnabled = true
            )
        )
        val hapticFeedback = RecordingHapticFeedback()
        val generationFactory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            challengeCatalog = GeneratedModes.catalog
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalHapticFeedback provides hapticFeedback) {
                NumPairsTheme {
                    AppNavigation(
                        onboardingRepository = FakeOnboardingRepository(),
                        generatedSessionRepository = FakeGeneratedSessionRepository(),
                        generatedDifficultySelectionRepository =
                            FakeGeneratedDifficultySelectionRepository(),
                        personalizationPreferencesRepository = preferencesRepository,
                        topAppBarActionDiscoveryRepository =
                            FakeTopAppBarActionDiscoveryRepository(),
                        generatedChallengeCatalog = GeneratedModes.catalog,
                        generatedPuzzleGenerationUseCaseFactory = generationFactory,
                        dailyFeatureDependencies = DailyFeatureDependencies(
                            dailySessionRepository = repository,
                            deviceLocalDateSource = {
                                identity.localDate
                            },
                            generatedPuzzleGenerationUseCaseFactory = generationFactory
                        ),
                        startDestination = AppDestination.DailyChallenge(identity)
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        assignFirstTileOperator(Operator.ADDITION)
        assertEquals(emptyList<HapticFeedbackType>(), hapticFeedback.requestedTypes)
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()

        runBlocking {
            preferencesRepository.setGeneratedGameHapticsEnabled(true)
        }
        composeTestRule.waitForIdle()
        assignFirstTileOperator(Operator.MULTIPLICATION)

        assertEquals(
            listOf(HapticFeedbackType.Confirm),
            hapticFeedback.requestedTypes
        )
    }

    @Test
    fun typed_completed_destination_routes_system_back_to_menu_without_daily_mutation() {
        val identity = identity()
        val repository = RecordingDailyRepository(
            DailyState(
                activeSession = null,
                completions = listOf(completion(identity))
            )
        )
        val generationFactory = ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
            challengeCatalog = GeneratedModes.catalog
        )

        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = FakeGeneratedSessionRepository(),
                    generatedDifficultySelectionRepository =
                        FakeGeneratedDifficultySelectionRepository(),
                    personalizationPreferencesRepository =
                        FakePersonalizationPreferencesRepository(),
                    topAppBarActionDiscoveryRepository =
                        FakeTopAppBarActionDiscoveryRepository(),
                    generatedChallengeCatalog = GeneratedModes.catalog,
                    generatedPuzzleGenerationUseCaseFactory = generationFactory,
                    dailyFeatureDependencies = DailyFeatureDependencies(
                        dailySessionRepository = repository,
                        deviceLocalDateSource = { identity.localDate },
                        generatedPuzzleGenerationUseCaseFactory = generationFactory
                    ),
                    startDestination = AppDestination.DailyCompletedToday(identity)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.COMPLETION_SUMMARY)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            composeTestRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(0, repository.mutationCount)
        }
    }

    @Test
    fun summary_keeps_all_three_actions_available_in_compact_scaled_rtl_content() {
        var shareCount = 0
        var calendarCount = 0
        var backCount = 0

        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = density.density,
                    fontScale = 2f
                ),
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                NumPairsTheme {
                    Box(modifier = Modifier.size(width = 320.dp, height = 420.dp)) {
                        DailyCompletionScreen(
                            presentation = DailyChallengeTitle(
                                visibleText = "Daily · 25 Jul 2026",
                                accessibilityText = "Daily · 25 Jul 2026, 4 pairs · Low"
                            ),
                            onShareResult = { shareCount += 1 },
                            onViewCalendar = { calendarCount += 1 },
                            onNavigateBack = { backCount += 1 }
                        )
                    }
                }
            }
        }

        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.SHARE_RESULT)
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.VIEW_CALENDAR)
            .performScrollTo()
            .performClick()
        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.BACK_TO_MENU)
            .performScrollTo()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, shareCount)
            assertEquals(1, calendarCount)
            assertEquals(1, backCount)
        }
    }

    @Test
    fun wide_completion_summary_caps_and_aligns_all_actions() {
        composeTestRule.setContent {
            NumPairsTheme {
                Box(modifier = Modifier.size(width = 1_000.dp, height = 800.dp)) {
                    DailyCompletionScreen(
                        presentation = DailyChallengeTitle(
                            visibleText = "Daily · Jul 25, 2026",
                            accessibilityText = "Daily · Jul 25, 2026, 4 pairs · Low"
                        ),
                        onShareResult = {},
                        onViewCalendar = {},
                        onNavigateBack = {}
                    )
                }
            }
        }

        val actionNodes = listOf(
            DailyScreenTestTags.SHARE_RESULT,
            DailyScreenTestTags.VIEW_CALENDAR,
            DailyScreenTestTags.BACK_TO_MENU
        ).map { tag ->
            composeTestRule
                .onNodeWithTag(tag)
                .assertIsDisplayed()
                .fetchSemanticsNode()
        }
        val maximumActionWidth = with(composeTestRule.density) {
            432.dp.toPx()
        }
        val minimumTouchTarget = with(composeTestRule.density) {
            48.dp.toPx()
        }
        val firstBounds = actionNodes.first().boundsInRoot

        actionNodes.forEach { node ->
            val bounds = node.boundsInRoot
            assertTrue(bounds.width <= maximumActionWidth)
            assertTrue(node.touchBoundsInRoot.height >= minimumTouchTarget)
            assertEquals(firstBounds.left, bounds.left, 0.5f)
            assertEquals(firstBounds.right, bounds.right, 0.5f)
        }
    }

    @Test
    fun completion_identity_is_visible_without_reconstructing_a_game_board() {
        val presentation = DailyChallengeTitle(
            visibleText = "Daily · Jul 25, 2026",
            accessibilityText = "Daily · Jul 25, 2026, 4 pairs · Low"
        )

        composeTestRule.setContent {
            NumPairsTheme {
                DailyCompletionScreen(
                    presentation = presentation,
                    onShareResult = {},
                    onViewCalendar = {},
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.COMPLETION_IDENTITY)
            .assertIsDisplayed()
            .assertTextEquals(presentation.visibleText)
        composeTestRule
            .onNodeWithTag("game_board")
            .assertDoesNotExist()
    }

    @Test
    fun game_title_keeps_concise_visible_copy_and_complete_accessibility_identity() {
        val visibleTitle = "Daily · Jul 25, 2026"
        val accessibilityTitle = "$visibleTitle, 4 pairs · Low"

        composeTestRule.setContent {
            NumPairsTheme {
                GameRoute(
                    title = visibleTitle,
                    titleContentDescription = accessibilityTitle,
                    initialPuzzle = samplePuzzle,
                    gameSessionKey = "daily-title-accessibility"
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(accessibilityTitle)
            .assertIsDisplayed()
    }

    private fun assignFirstTileOperator(operator: Operator) {
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.BOARD)
            .performScrollTo()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.tileOperator(0), useUnmergedTree = true)
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.TILE_OPERATOR_SELECTOR_TITLE, useUnmergedTree = true)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(
                GameScreenTestTags.tileOperatorOption(operator),
                useUnmergedTree = true
            ).performClick()
        composeTestRule.waitForIdle()
    }

    private fun generatedSnapshot(identity: DailyChallengeId): DailySessionSnapshot {
        val result = runBlocking {
            DailyPuzzleGenerationUseCase(
                currentDailyChallengeResolver = CurrentDailyChallengeResolver(
                    localDateSource = {
                        identity.localDate
                    }
                ),
                generatedPuzzleGenerationUseCaseFactory =
                    ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
                        challengeCatalog = GeneratedModes.catalog
                    )
            ).generate()
        } as DailyPuzzleGenerationResult.Generated

        return DailySessionSnapshot(
            sessionId = DailySessionId("daily-feedback"),
            dailyChallengeId = result.identity,
            candidateIndex = result.candidateIndex,
            seed = result.seed,
            initialPuzzle = result.initialPuzzle,
            currentPuzzle = result.initialPuzzle
        )
    }

    private fun identity(): DailyChallengeId = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
        LocalDate.of(2026, 7, 25)
    )

    private class RecordingHapticFeedback : HapticFeedback {
        val requestedTypes = mutableListOf<HapticFeedbackType>()

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            requestedTypes += hapticFeedbackType
        }
    }
}

private class RecordingDailyRepository(initialState: DailyState) : DailySessionRepository {
    override val state = MutableStateFlow(initialState)

    var mutationCount: Int = 0
    var lastReplacement: DailySessionSnapshot? = null

    override suspend fun replaceSession(snapshot: DailySessionSnapshot): DailySessionReplacementResult {
        mutationCount += 1
        lastReplacement = snapshot
        return DailySessionReplacementResult.Replaced
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: DailySessionId,
        puzzle: Puzzle
    ): DailySessionProgressUpdateResult {
        mutationCount += 1
        return DailySessionProgressUpdateResult.Updated
    }

    override suspend fun startTiming(
        expectedSessionId: DailySessionId,
        startInstant: DailyTimingStartInstant
    ): DailySessionTimingStartResult = DailySessionTimingStartResult.StaleSession

    override suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult {
        mutationCount += 1
        return DailySessionClearResult.Cleared
    }

    override suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle,
        elapsedTime: DailyElapsedTime?
    ): DailySessionCompletionResult {
        mutationCount += 1
        return DailySessionCompletionResult.Completed(
            DailyCompletion(
                identity = expectedDailyChallengeId,
                elapsedTime = elapsedTime
            )
        )
    }
}

private fun completion(identity: DailyChallengeId): DailyCompletion = DailyCompletion(
    identity = identity,
    elapsedTime = null
)
