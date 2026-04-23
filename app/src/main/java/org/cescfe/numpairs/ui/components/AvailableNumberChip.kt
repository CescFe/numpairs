package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.ui.theme.NumPairsTheme

private val CHIP_MIN_HEIGHT = 48.dp

@Composable
fun AvailableNumberChip(label: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            AvailableNumberChipLabel(label = label)
        }
    } else {
        Surface(
            modifier = modifier,
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            AvailableNumberChipLabel(label = label)
        }
    }
}

@Composable
private fun AvailableNumberChipLabel(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = CHIP_MIN_HEIGHT)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvailableNumberChipPreview() {
    NumPairsTheme {
        AvailableNumberChip(label = "4")
    }
}
