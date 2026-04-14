package org.cescfe.numpairs.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.ui.theme.NumPairsTheme

@Composable
fun AvailableNumberChip(
    number: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Text(
            text = number.toString(),
            modifier = Modifier
                .widthIn(min = 44.dp)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvailableNumberChipPreview() {
    NumPairsTheme {
        AvailableNumberChip(number = 4)
    }
}
