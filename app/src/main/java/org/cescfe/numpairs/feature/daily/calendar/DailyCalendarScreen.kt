package org.cescfe.numpairs.feature.daily.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import org.cescfe.numpairs.R
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCalendarScreen(
    calendarMonth: DailyCalendarMonth,
    locale: Locale,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(DailyCalendarScreenTestTags.SCREEN),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                colors = NumPairsComponents.topAppBarColors(),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag(DailyCalendarScreenTestTags.BACK_BUTTON)
                    ) {
                        LogicalChevronIcon(
                            pointsToStart = true,
                            contentDescription = stringResource(R.string.back_button_content_description)
                        )
                    }
                },
                title = {
                    Text(text = stringResource(R.string.daily_calendar_screen_title))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = DAILY_CALENDAR_MAX_WIDTH),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonthNavigation(
                    calendarMonth = calendarMonth,
                    locale = locale,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth
                )
                WeekdayHeadings(
                    weekdays = calendarMonth.weekdayOrder,
                    locale = locale
                )
                CalendarGrid(
                    positions = calendarMonth.positions,
                    locale = locale
                )
            }
        }
    }
}

@Composable
private fun MonthNavigation(
    calendarMonth: DailyCalendarMonth,
    locale: Locale,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.testTag(DailyCalendarScreenTestTags.PREVIOUS_MONTH_BUTTON),
            colors = NumPairsComponents.iconButtonColors()
        ) {
            LogicalChevronIcon(
                pointsToStart = true,
                contentDescription = stringResource(
                    R.string.daily_calendar_previous_month_content_description
                )
            )
        }
        Text(
            text = calendarMonth.displayedMonth.atDay(1).format(
                DateTimeFormatter.ofPattern("LLLL yyyy", locale)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .testTag(DailyCalendarScreenTestTags.MONTH_HEADING),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = onNextMonth,
            enabled = calendarMonth.canNavigateToNextMonth,
            modifier = Modifier.testTag(DailyCalendarScreenTestTags.NEXT_MONTH_BUTTON),
            colors = NumPairsComponents.iconButtonColors()
        ) {
            LogicalChevronIcon(
                pointsToStart = false,
                contentDescription = stringResource(
                    R.string.daily_calendar_next_month_content_description
                )
            )
        }
    }
}

@Composable
private fun WeekdayHeadings(weekdays: List<DayOfWeek>, locale: Locale) {
    Row(modifier = Modifier.fillMaxWidth()) {
        weekdays.forEach { weekday ->
            Text(
                text = weekday.getDisplayName(TextStyle.SHORT_STANDALONE, locale),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalendarGrid(positions: List<DailyCalendarPosition>, locale: Locale) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        positions.chunked(DAYS_PER_WEEK).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { position ->
                    Box(modifier = Modifier.weight(1f)) {
                        when (position) {
                            DailyCalendarPosition.OutsideMonth -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                )
                            }

                            is DailyCalendarPosition.InMonth -> {
                                DailyCalendarDateCell(
                                    calendarDate = position.calendarDate,
                                    locale = locale
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyCalendarDateCell(calendarDate: DailyCalendarDate, locale: Locale) {
    val description = calendarDate.localizedContentDescription(locale)
    val border = when {
        calendarDate.isToday -> NumPairsComponents.defaultBorder().copy(
            width = NumPairsComponents.StrongBorderWidth
        )

        calendarDate.isCompleted -> NumPairsComponents.successBorder()

        else -> BorderStroke(
            width = NumPairsComponents.ThinBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = NEUTRAL_CELL_BORDER_ALPHA)
        )
    }
    val background = if (calendarDate.isCompleted) {
        NumPairsComponents.successContainerColor()
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (calendarDate.isCompleted) {
        NumPairsComponents.successContentColor()
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .alpha(if (calendarDate.isFuture) FUTURE_DATE_ALPHA else 1f)
            .background(color = background, shape = CircleShape)
            .border(border = border, shape = CircleShape)
            .clearAndSetSemantics {
                contentDescription = description
                if (calendarDate.isFuture) {
                    disabled()
                }
            }.testTag(DailyCalendarScreenTestTags.date(calendarDate.date)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = calendarDate.date.dayOfMonth.toString(),
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (calendarDate.isToday || calendarDate.isCompleted) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
            if (calendarDate.isCompleted) {
                Text(
                    text = CHECK_MARK,
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun DailyCalendarDate.localizedContentDescription(locale: Locale): String {
    val localizedDate = date.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    )
    return stringResource(
        when {
            isToday && isCompleted -> R.string.daily_calendar_today_completed_date_description
            isToday -> R.string.daily_calendar_today_date_description
            isCompleted && isFuture -> R.string.daily_calendar_future_completed_date_description
            isCompleted -> R.string.daily_calendar_completed_date_description
            isFuture -> R.string.daily_calendar_future_date_description
            else -> R.string.daily_calendar_date_description
        },
        localizedDate
    )
}

@Composable
private fun LogicalChevronIcon(pointsToStart: Boolean, contentDescription: String) {
    val layoutDirection = LocalLayoutDirection.current
    val pointsLeft = pointsToStart == (layoutDirection == LayoutDirection.Ltr)
    Icon(
        painter = painterResource(R.drawable.ic_chevron_left),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer {
                rotationZ = if (pointsLeft) 0f else 180f
            }
    )
}

private const val DAYS_PER_WEEK = 7
private const val CHECK_MARK = "✓"
private const val FUTURE_DATE_ALPHA = 0.5f
private const val NEUTRAL_CELL_BORDER_ALPHA = 0.35f
private val DAILY_CALENDAR_MAX_WIDTH = 480.dp
