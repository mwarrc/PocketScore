package com.mwarrc.pocketscore.ui.feature.about.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/**
 * A minimal, elegant summary of app usage statistics.
 * Displayed at the very bottom of the About screen.
 */
@Composable
fun AboutStatsView(
    totalGames: Int,
    totalPlayers: Int,
    totalTimeSummary: String,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subtle divider
        HorizontalDivider(
            modifier = Modifier
                .width(40.dp)
                .padding(bottom = 24.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Matches Played",
                value = numberFormat.format(totalGames)
            )
            StatItem(
                label = "Saved Roster",
                value = numberFormat.format(totalPlayers)
            )
            StatItem(
                label = "Play Time",
                value = totalTimeSummary
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontSize = 9.sp,
            letterSpacing = 1.sp
        )
    }
}
