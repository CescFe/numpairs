package org.cescfe.numpairs.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.daily.session.DailySessionClearResult
import org.cescfe.numpairs.data.daily.session.DailySessionCompletionResult
import org.cescfe.numpairs.data.daily.session.DailySessionId
import org.cescfe.numpairs.data.daily.session.DailySessionProgressUpdateResult
import org.cescfe.numpairs.data.daily.session.DailySessionReplacementResult
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.data.generated.selection.FakeGeneratedDifficultySelectionRepository
import org.cescfe.numpairs.data.generated.session.FakeGeneratedSessionRepository
import org.cescfe.numpairs.data.onboarding.FakeOnboardingRepository
import org.cescfe.numpairs.data.preferences.FakePersonalizationPreferencesRepository
import org.cescfe.numpairs.data.preferences.FakeTopAppBarActionDiscoveryRepository
import org.cescfe.numpairs.data.puzzle.seed.samplePuzzle
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.feature.daily.CurrentDailyChallengeResolver
import org.cescfe.numpairs.feature.daily.DailyFeatureDependencies
import org.cescfe.numpairs.feature.daily.DailyPuzzleGenerationResult
import org.cescfe.numpairs.feature.daily.DailyPuzzleGenerationUseCase
import org.cescfe.numpairs.feature.daily.DailyRecipes
import org.cescfe.numpairs.feature.daily.DailyScreenTestTags
import org.cescfe.numpairs.feature.daily.calendar.DailyCalendarScreenTestTags
import org.cescfe.numpairs.feature.game.ui.screen.GameScreenTestTags
import org.cescfe.numpairs.feature.generated.ConfiguredGeneratedPuzzleGenerationUseCaseFactory
import org.cescfe.numpairs.feature.generated.GeneratedChallenge
import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPlayChallengeSelector
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
class DailyMenuNavigationTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun calendar_never_prepares_daily_and_return_recaptures_the_new_local_date() {
        val dateSource = MutableDateSource(LocalDate.of(2026, 7, 25))
        val completedIdentity = identity(dateSource.currentDate)
        val repository = MutableDailyRepository(
            DailyState(
                activeSession = null,
                completedChallengeIds = listOf(completedIdentity)
            )
        )
        val generationCounter = GenerationCounter()
        setContent(
            repository = repository,
            dateSource = dateSource,
            generationCounter = generationCounter
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .assertTextEquals(string(R.string.menu_daily_completed_button))
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_CALENDAR_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.SCREEN)
            .assertIsDisplayed()

        composeTestRule.runOnIdle {
            dateSource.currentDate = dateSource.currentDate.plusDays(1)
        }
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.BACK_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .assertTextEquals(string(R.string.menu_daily_start_button))
        composeTestRule.runOnIdle {
            assertEquals(0, generationCounter.count)
            assertEquals(0, repository.mutationCount)
        }
    }

    @Test
    fun completed_primary_opens_the_lightweight_summary_and_back_returns_to_menu() {
        val dateSource = MutableDateSource(LocalDate.of(2026, 7, 25))
        val repository = MutableDailyRepository(
            DailyState(
                activeSession = null,
                completedChallengeIds = listOf(identity(dateSource.currentDate))
            )
        )
        val generationCounter = GenerationCounter()
        setContent(
            repository = repository,
            dateSource = dateSource,
            generationCounter = generationCounter
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .performClick()
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
            assertEquals(0, generationCounter.count)
            assertEquals(0, repository.mutationCount)
        }
    }

    @Test
    fun exact_current_snapshot_exposes_continue_and_restores_without_generation() {
        val dateSource = MutableDateSource(LocalDate.of(2026, 7, 25))
        val snapshot = generatedSnapshot(dateSource.currentDate)
        val repository = MutableDailyRepository(
            DailyState(
                activeSession = snapshot,
                completedChallengeIds = emptyList()
            )
        )
        val generationCounter = GenerationCounter()
        setContent(
            repository = repository,
            dateSource = dateSource,
            generationCounter = generationCounter
        )

        composeTestRule
            .onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
            .assertTextEquals(string(R.string.menu_daily_continue_button))
            .performClick()
        composeTestRule
            .onNodeWithTag(GameScreenTestTags.SCREEN)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(0, generationCounter.count)
            assertEquals(0, repository.mutationCount)
        }
    }

    @Test
    fun start_primary_routes_to_one_daily_preparation_attempt() {
        val dateSource = MutableDateSource(LocalDate.of(2026, 7, 25))
        val repository = MutableDailyRepository(
            DailyState(
                activeSession = null,
                completedChallengeIds = emptyList()
            )
        )
        val generationCounter = GenerationCounter()
        val difficultyRepository = setContent(
            repository = repository,
            dateSource = dateSource,
            generationCounter = generationCounter
        )

        val dailyAction = composeTestRule.onNodeWithTag(MenuScreenTestTags.DAILY_BUTTON)
        dailyAction.performClick()
        composeTestRule
            .onNodeWithTag(DailyScreenTestTags.FAILURE)
            .assertIsDisplayed()
        composeTestRule.runOnIdle {
            assertEquals(1, generationCounter.count)
            assertEquals(listOf(GeneratedModes.FOUR_PAIRS_LOW), generationCounter.createdChallenges)
            assertTrue(difficultyRepository.explicitSelections.isEmpty())
            assertEquals(0, repository.mutationCount)
        }
    }

    private fun setContent(
        repository: MutableDailyRepository,
        dateSource: MutableDateSource,
        generationCounter: GenerationCounter
    ): FakeGeneratedDifficultySelectionRepository {
        val difficultyRepository = FakeGeneratedDifficultySelectionRepository()
        val generationFactory = GeneratedPuzzleGenerationUseCaseFactory { challenge ->
            generationCounter.createdChallenges += challenge
            GeneratedPuzzleGenerationUseCase { request ->
                generationCounter.count += 1
                GeneratedPuzzleGenerationResult.Generated(
                    request = request,
                    initialPuzzle = samplePuzzle
                )
            }
        }
        composeTestRule.setContent {
            NumPairsTheme {
                AppNavigation(
                    onboardingRepository = FakeOnboardingRepository(),
                    generatedSessionRepository = FakeGeneratedSessionRepository(),
                    generatedDifficultySelectionRepository = difficultyRepository,
                    personalizationPreferencesRepository =
                        FakePersonalizationPreferencesRepository(),
                    topAppBarActionDiscoveryRepository =
                        FakeTopAppBarActionDiscoveryRepository(),
                    generatedChallengeCatalog = GeneratedModes.catalog,
                    generatedPlayChallengeSelector = GeneratedPlayChallengeSelector(
                        challengeCatalog = GeneratedModes.catalog,
                        quickBucketSource = {
                            error("Daily navigation must not consume Quick selection randomness.")
                        }
                    ),
                    generatedPuzzleGenerationUseCaseFactory = generationFactory,
                    dailyFeatureDependencies = DailyFeatureDependencies(
                        dailySessionRepository = repository,
                        deviceLocalDateSource = dateSource,
                        generatedPuzzleGenerationUseCaseFactory = generationFactory
                    )
                )
            }
        }
        return difficultyRepository
    }

    private fun generatedSnapshot(date: LocalDate): DailySessionSnapshot {
        val dateSource = DeviceLocalDateSource { date }
        val currentResolver = CurrentDailyChallengeResolver(
            localDateSource = dateSource
        )
        val result = runBlocking {
            DailyPuzzleGenerationUseCase(
                currentDailyChallengeResolver = currentResolver,
                generatedPuzzleGenerationUseCaseFactory =
                    ConfiguredGeneratedPuzzleGenerationUseCaseFactory(
                        challengeCatalog = GeneratedModes.catalog
                    )
            ).generate()
        } as DailyPuzzleGenerationResult.Generated
        return DailySessionSnapshot(
            sessionId = DailySessionId("daily-menu-continue"),
            dailyChallengeId = result.identity,
            candidateIndex = result.candidateIndex,
            seed = result.seed,
            initialPuzzle = result.initialPuzzle,
            currentPuzzle = result.initialPuzzle
        )
    }

    private fun identity(date: LocalDate): DailyChallengeId = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(date)

    private fun string(resourceId: Int): String = composeTestRule.activity.getString(resourceId)
}

private class MutableDateSource(var currentDate: LocalDate) : DeviceLocalDateSource {
    override fun currentDate(): LocalDate = currentDate
}

private class MutableDailyRepository(initialState: DailyState) : DailySessionRepository {
    override val state = MutableStateFlow(initialState)

    var mutationCount: Int = 0

    override suspend fun replaceSession(snapshot: DailySessionSnapshot): DailySessionReplacementResult {
        mutationCount += 1
        state.value = state.value.copy(activeSession = snapshot)
        return DailySessionReplacementResult.Replaced
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: DailySessionId,
        puzzle: Puzzle
    ): DailySessionProgressUpdateResult {
        mutationCount += 1
        return DailySessionProgressUpdateResult.Updated
    }

    override suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult {
        mutationCount += 1
        return DailySessionClearResult.Cleared
    }

    override suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle
    ): DailySessionCompletionResult {
        mutationCount += 1
        return DailySessionCompletionResult.Completed
    }
}

private class GenerationCounter {
    var count: Int = 0
    val createdChallenges = mutableListOf<GeneratedChallenge>()
}
