package org.cescfe.numpairs.feature.daily.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import java.time.YearMonth
import java.util.Locale
import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.data.daily.session.DailyState
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource

@Composable
fun DailyCalendarRoute(
    dailySessionRepository: DailySessionRepository,
    deviceLocalDateSource: DeviceLocalDateSource,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capturedCurrentDate = remember(deviceLocalDateSource) {
        deviceLocalDateSource.currentDate()
    }
    var displayedMonth by remember(capturedCurrentDate) {
        mutableStateOf(YearMonth.from(capturedCurrentDate))
    }
    val dailyState by dailySessionRepository.state.collectAsState(
        initial = DailyState(
            activeSession = null,
            completedChallengeIds = emptyList()
        )
    )
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) {
        Locale.forLanguageTag(configuration.locales[0].toLanguageTag())
    }
    val calendarMonth = remember(
        capturedCurrentDate,
        displayedMonth,
        dailyState.completedChallengeIds,
        locale
    ) {
        DailyCalendarMonth.create(
            capturedCurrentDate = capturedCurrentDate,
            displayedMonth = displayedMonth,
            completedChallengeIds = dailyState.completedChallengeIds,
            locale = locale
        )
    }

    DailyCalendarScreen(
        calendarMonth = calendarMonth,
        locale = locale,
        onPreviousMonth = {
            displayedMonth = displayedMonth.minusMonths(1)
        },
        onNextMonth = {
            if (calendarMonth.canNavigateToNextMonth) {
                displayedMonth = displayedMonth.plusMonths(1)
            }
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
