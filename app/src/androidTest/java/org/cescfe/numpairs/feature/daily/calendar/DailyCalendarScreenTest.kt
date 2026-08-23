package org.cescfe.numpairs.feature.daily.calendar

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.daily.session.DailySessionClearResult
import org.cescfe.numpairs.data.daily.session.DailySessionCompletionResult
import org.cescfe.numpairs.data.daily.session.DailySessionId
import org.cescfe.numpairs.data.daily.session.DailySessionProgressUpdateResult
import org.cescfe.numpairs.data.daily.session.DailySessionReplacementResult
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailySessionSnapshot
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.domain.daily.DailyChallengeId
import org.cescfe.numpairs.domain.daily.DailyRecipeVersion
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyCalendarScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun date_cells_expose_non_interactive_today_completion_and_future_semantics() {
        val currentDate = LocalDate.of(2026, 7, 25)
        val pastCompletion = LocalDate.of(2026, 7, 3)
        val futureDate = LocalDate.of(2026, 7, 27)
        setScreen(
            DailyCalendarMonth.create(
                capturedCurrentDate = currentDate,
                completedChallengeIds = listOf(
                    identity(pastCompletion),
                    identity(currentDate)
                ),
                locale = Locale.US
            )
        )

        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.date(currentDate))
            .assertIsDisplayed()
            .assertHasNoClickAction()
            .assertContentDescriptionEquals(
                string(
                    R.string.daily_calendar_today_completed_date_description,
                    localizedDate(currentDate)
                )
            )
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.date(pastCompletion))
            .assertHasNoClickAction()
            .assertContentDescriptionEquals(
                string(
                    R.string.daily_calendar_completed_date_description,
                    localizedDate(pastCompletion)
                )
            )
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.date(futureDate))
            .assertHasNoClickAction()
            .assertIsNotEnabled()
            .assertContentDescriptionEquals(
                string(
                    R.string.daily_calendar_future_date_description,
                    localizedDate(futureDate)
                )
            )
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.NEXT_MONTH_BUTTON)
            .assertIsNotEnabled()
    }

    @Test
    fun month_and_back_actions_are_distinct_and_next_is_enabled_only_before_current_month() {
        var previousCount = 0
        var nextCount = 0
        var backCount = 0
        setScreen(
            month = DailyCalendarMonth.create(
                capturedCurrentDate = LocalDate.of(2026, 7, 25),
                displayedMonth = YearMonth.of(2026, 6),
                locale = Locale.US
            ),
            onPreviousMonth = { previousCount += 1 },
            onNextMonth = { nextCount += 1 },
            onNavigateBack = { backCount += 1 }
        )

        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.PREVIOUS_MONTH_BUTTON)
            .assertHasClickAction()
            .performClick()
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.NEXT_MONTH_BUTTON)
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.BACK_BUTTON)
            .assertHasClickAction()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, previousCount)
            assertEquals(1, nextCount)
            assertEquals(1, backCount)
        }
    }

    @Test
    fun route_captures_date_once_reads_history_and_never_mutates_the_repository() {
        val currentDate = LocalDate.of(2026, 7, 25)
        val completion = identity(LocalDate.of(2026, 7, 3))
        val repository = ReadOnlyRecordingDailyRepository(
            DailyState(
                activeSession = null,
                completedChallengeIds = listOf(completion)
            )
        )
        var dateReadCount = 0

        composeTestRule.setContent {
            NumPairsTheme {
                DailyCalendarRoute(
                    dailySessionRepository = repository,
                    deviceLocalDateSource = {
                        dateReadCount += 1
                        currentDate
                    },
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.date(completion.localDate))
            .assertContentDescriptionEquals(
                string(
                    R.string.daily_calendar_completed_date_description,
                    localizedDate(completion.localDate)
                )
            )
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.PREVIOUS_MONTH_BUTTON)
            .performClick()
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.NEXT_MONTH_BUTTON)
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, dateReadCount)
            assertEquals(0, repository.mutationCount)
        }
    }

    @Test
    fun compact_scaled_rtl_layout_keeps_late_month_dates_available_by_scrolling() {
        val currentDate = LocalDate.of(2028, 2, 29)
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
                        DailyCalendarScreen(
                            calendarMonth = DailyCalendarMonth.create(
                                capturedCurrentDate = currentDate,
                                locale = Locale.forLanguageTag("ar")
                            ),
                            locale = Locale.forLanguageTag("ar"),
                            onPreviousMonth = {},
                            onNextMonth = {},
                            onNavigateBack = {}
                        )
                    }
                }
            }
        }

        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.date(currentDate))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.MONTH_HEADING)
            .assertTextEquals(
                currentDate.format(
                    DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ar"))
                )
            )
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.PREVIOUS_MONTH_BUTTON)
            .assertContentDescriptionEquals(
                string(R.string.daily_calendar_previous_month_content_description)
            )
        composeTestRule
            .onNodeWithTag(DailyCalendarScreenTestTags.NEXT_MONTH_BUTTON)
            .assertContentDescriptionEquals(
                string(R.string.daily_calendar_next_month_content_description)
            )
    }

    @Test
    fun wide_layout_caps_calendar_content_and_preserves_navigation_touch_targets() {
        val currentDate = LocalDate.of(2026, 7, 25)
        composeTestRule.setContent {
            NumPairsTheme {
                Box(modifier = Modifier.size(width = 1_000.dp, height = 800.dp)) {
                    DailyCalendarScreen(
                        calendarMonth = DailyCalendarMonth.create(
                            capturedCurrentDate = currentDate,
                            displayedMonth = YearMonth.of(2026, 6),
                            locale = Locale.US
                        ),
                        locale = Locale.US,
                        onPreviousMonth = {},
                        onNextMonth = {},
                        onNavigateBack = {}
                    )
                }
            }
        }

        val navigationNodes = listOf(
            DailyCalendarScreenTestTags.BACK_BUTTON,
            DailyCalendarScreenTestTags.PREVIOUS_MONTH_BUTTON,
            DailyCalendarScreenTestTags.NEXT_MONTH_BUTTON
        ).associateWith { tag ->
            composeTestRule
                .onNodeWithTag(tag)
                .assertIsDisplayed()
                .fetchSemanticsNode()
        }
        val minimumTouchTarget = with(composeTestRule.density) {
            48.dp.toPx()
        }
        navigationNodes.values.forEach { node ->
            assertTrue(node.touchBoundsInRoot.width >= minimumTouchTarget)
            assertTrue(node.touchBoundsInRoot.height >= minimumTouchTarget)
        }

        val previousBounds = navigationNodes.getValue(
            DailyCalendarScreenTestTags.PREVIOUS_MONTH_BUTTON
        ).boundsInRoot
        val nextBounds = navigationNodes.getValue(
            DailyCalendarScreenTestTags.NEXT_MONTH_BUTTON
        ).boundsInRoot
        val maximumContentWidth = with(composeTestRule.density) {
            480.dp.toPx()
        }
        assertTrue(nextBounds.right - previousBounds.left <= maximumContentWidth)
    }

    private fun setScreen(
        month: DailyCalendarMonth,
        onPreviousMonth: () -> Unit = {},
        onNextMonth: () -> Unit = {},
        onNavigateBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            NumPairsTheme {
                DailyCalendarScreen(
                    calendarMonth = month,
                    locale = Locale.US,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onNavigateBack = onNavigateBack
                )
            }
        }
    }

    private fun localizedDate(date: LocalDate): String = date.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.US)
    )

    private fun string(stringResId: Int, vararg args: Any): String =
        composeTestRule.activity.getString(stringResId, *args)
}

private class ReadOnlyRecordingDailyRepository(initialState: DailyState) : DailySessionRepository {
    override val state = MutableStateFlow(initialState)

    var mutationCount: Int = 0
        private set

    override suspend fun replaceSession(snapshot: DailySessionSnapshot): DailySessionReplacementResult {
        mutationCount += 1
        return DailySessionReplacementResult.Replaced
    }

    override suspend fun updateCurrentPuzzle(
        expectedSessionId: DailySessionId,
        puzzle: Puzzle
    ): DailySessionProgressUpdateResult {
        mutationCount += 1
        return DailySessionProgressUpdateResult.StaleSession
    }

    override suspend fun clearSession(expectedSessionId: DailySessionId): DailySessionClearResult {
        mutationCount += 1
        return DailySessionClearResult.StaleSession
    }

    override suspend fun complete(
        expectedSessionId: DailySessionId,
        expectedDailyChallengeId: DailyChallengeId,
        solvedPuzzle: Puzzle
    ): DailySessionCompletionResult {
        mutationCount += 1
        return DailySessionCompletionResult.StaleSession
    }
}

private fun identity(date: LocalDate): DailyChallengeId = DailyChallengeId(
    localDate = date,
    recipeVersion = DailyRecipeVersion("daily-calendar-test")
)
