package org.cescfe.numpairs.feature.generated

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime

@Composable
internal fun GeneratedChronometer(
    elapsedTime: GeneratedElapsedTime,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedElapsedTime = GeneratedElapsedTimeFormatter.format(elapsedTime)
    val elapsedDescription = stringResource(
        R.string.generated_elapsed_time_content_description,
        formattedElapsedTime
    )
    val toggleDescription = stringResource(
        if (isExpanded) {
            R.string.generated_hide_elapsed_time_action
        } else {
            R.string.generated_show_elapsed_time_action
        }
    )
    val toggle = { onExpandedChange(!isExpanded) }

    Surface(
        onClick = toggle,
        modifier = modifier
            .testTag(GENERATED_CHRONOMETER_TAG)
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = if (isExpanded) elapsedDescription else toggleDescription
                onClick(label = toggleDescription) {
                    toggle()
                    true
                }
            },
        color = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isExpanded) 8.dp else 12.dp,
                vertical = 12.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_stopwatch),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            if (isExpanded) {
                Text(
                    text = formattedElapsedTime,
                    modifier = Modifier.testTag(GENERATED_CHRONOMETER_VALUE_TAG),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontFeatureSettings = "tnum"
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

internal const val GENERATED_CHRONOMETER_TAG = "generatedChronometer"
internal const val GENERATED_CHRONOMETER_VALUE_TAG = "generatedChronometerValue"
