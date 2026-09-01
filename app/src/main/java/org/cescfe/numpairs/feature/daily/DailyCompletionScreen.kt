package org.cescfe.numpairs.feature.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.data.preferences.PersonalizationTheme
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.ui.theme.NumPairsComponents
import org.cescfe.numpairs.ui.theme.NumPairsTheme
import org.cescfe.numpairs.ui.theme.NumPairsThemePreviewParameterProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DailyCompletionScreen(
    presentation: DailyChallengeTitle,
    onShareResult: () -> Unit,
    onViewCalendar: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    elapsedTime: DailyElapsedTime? = null,
    movementCount: DailyMovementCount? = null,
    bestElapsedTime: DailyElapsedTime? = null
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(DailyScreenTestTags.COMPLETION_SUMMARY),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                colors = NumPairsComponents.topAppBarColors(),
                title = {
                    Text(text = stringResource(R.string.daily_completion_screen_title))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = DAILY_COMPLETION_MAX_WIDTH),
                shape = NumPairsComponents.LargeShape,
                color = NumPairsComponents.successContainerColor(),
                contentColor = NumPairsComponents.successContentColor(),
                border = NumPairsComponents.successBorder()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.daily_completion_message),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = presentation.visibleText,
                        modifier = Modifier.testTag(DailyScreenTestTags.COMPLETION_IDENTITY),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = presentation.challengeText,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    elapsedTime?.let { completedElapsedTime ->
                        val formattedElapsedTime = DailyElapsedTimeFormatter.format(completedElapsedTime)
                        val accessibilityDescription = stringResource(
                            R.string.daily_elapsed_time_content_description,
                            formattedElapsedTime
                        )
                        Text(
                            text = stringResource(R.string.daily_completion_duration_label),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = formattedElapsedTime,
                            modifier = Modifier
                                .testTag(DailyScreenTestTags.COMPLETION_DURATION)
                                .semantics {
                                    contentDescription = accessibilityDescription
                                },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    bestElapsedTime?.let { personalBestElapsedTime ->
                        val formattedBestElapsedTime = DailyElapsedTimeFormatter.format(personalBestElapsedTime)
                        val accessibilityDescription = stringResource(
                            R.string.daily_personal_best_content_description,
                            formattedBestElapsedTime
                        )
                        Text(
                            text = stringResource(R.string.daily_personal_best_label),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = formattedBestElapsedTime,
                            modifier = Modifier
                                .testTag(DailyScreenTestTags.PERSONAL_BEST_DURATION)
                                .semantics {
                                    contentDescription = accessibilityDescription
                                },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    movementCount?.let { completedMovementCount ->
                        val formattedMovementCount = DailyMovementCountFormatter.format(completedMovementCount)
                        val accessibilityDescription = pluralStringResource(
                            R.plurals.daily_movement_count_content_description,
                            completedMovementCount.pluralQuantity(),
                            formattedMovementCount
                        )
                        Text(
                            text = stringResource(R.string.daily_completion_movements_label),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = formattedMovementCount,
                            modifier = Modifier
                                .testTag(DailyScreenTestTags.COMPLETION_MOVEMENTS)
                                .semantics {
                                    contentDescription = accessibilityDescription
                                },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontFeatureSettings = "tnum",
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    NumPairsComponents.PrimaryCtaButton(
                        onClick = onShareResult,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(DailyScreenTestTags.SHARE_RESULT)
                    ) {
                        Text(
                            text = stringResource(R.string.daily_share_result_action),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    OutlinedButton(
                        onClick = onViewCalendar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(DailyScreenTestTags.VIEW_CALENDAR),
                        shape = NumPairsComponents.MediumShape,
                        colors = NumPairsComponents.secondaryButtonColors(),
                        border = NumPairsComponents.secondaryButtonBorder()
                    ) {
                        Text(text = stringResource(R.string.daily_view_calendar_action))
                    }
                    TextButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(DailyScreenTestTags.BACK_TO_MENU)
                    ) {
                        Text(text = stringResource(R.string.daily_back_to_menu_action))
                    }
                }
            }
        }
    }
}

object DailyScreenTestTags {
    const val LOADING = "daily_loading"
    const val FAILURE = "daily_failure"
    const val RETRY = "daily_retry"
    const val COMPLETION_SUMMARY = "daily_completion_summary"
    const val COMPLETION_IDENTITY = "daily_completion_identity"
    const val SHARE_RESULT = "daily_share_result"
    const val VIEW_CALENDAR = "daily_view_calendar"
    const val BACK_TO_MENU = "daily_back_to_menu"
    const val PERSISTENCE_FAILURE = "daily_persistence_failure"
    const val CHRONOMETER = "daily_chronometer"
    const val COMPLETION_DURATION = "daily_completion_duration"
    const val PERSONAL_BEST_DURATION = "daily_personal_best_duration"
    const val COMPLETION_MOVEMENTS = "daily_completion_movements"
}

private val DAILY_COMPLETION_MAX_WIDTH = 480.dp

@Preview(showBackground = true)
@Composable
private fun DailyCompletionScreenPreview(
    @PreviewParameter(NumPairsThemePreviewParameterProvider::class) theme: PersonalizationTheme
) {
    NumPairsTheme(theme = theme) {
        DailyCompletionScreen(
            presentation = DailyChallengeTitle(
                visibleText = "Daily · Jul 25, 2026",
                accessibilityText = "Daily · Jul 25, 2026, 4 pairs · Low",
                challengeText = "4 pairs · Low"
            ),
            elapsedTime = DailyElapsedTime(83_456),
            movementCount = DailyMovementCount(23),
            bestElapsedTime = DailyElapsedTime(74_321),
            onShareResult = {},
            onViewCalendar = {},
            onNavigateBack = {}
        )
    }
}
