package org.cescfe.numpairs.feature.daily

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.daily.DailyElapsedTime

@Composable
internal fun DailyChronometer(elapsedTime: DailyElapsedTime, modifier: Modifier = Modifier) {
    val formattedElapsedTime = DailyElapsedTimeFormatter.format(elapsedTime)
    val accessibilityDescription = stringResource(
        R.string.daily_elapsed_time_content_description,
        formattedElapsedTime
    )
    Text(
        text = formattedElapsedTime,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .testTag(DailyScreenTestTags.CHRONOMETER)
            .semantics {
                contentDescription = accessibilityDescription
            },
        style = MaterialTheme.typography.titleMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontFeatureSettings = "tnum"
        ),
        maxLines = 1
    )
}
