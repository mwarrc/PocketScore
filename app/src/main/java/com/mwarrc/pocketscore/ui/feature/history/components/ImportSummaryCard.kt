package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A summary card displaying high-level statistics about an incoming data package.
 *
 * Used during the import flow to give users an immediate overview of what is
 * about to be added to their local database, including the source device name
 * and totals for players (friends) and matches (games).
 *
 * @param shareData The structured data package being imported.
 * @param modifier Modifier for the card container.
 */
@Composable
fun ImportSummaryCard(
    shareData: PocketScoreShare,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }
    val formattedDate = remember(shareData.shareDate) {
        dateFormat.format(Date(shareData.shareDate))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Source Device Identity Header
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = "Source",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = shareData.sourceDevice ?: "Unknown Device",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary Quick Stats (Players & Matches)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ImportStatBox(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Person,
                label = "Players",
                value = "${shareData.friends.size}",
                color = MaterialTheme.colorScheme.secondary
            )
            ImportStatBox(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.History,
                label = "Matches",
                value = "${shareData.games.size}",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

/**
 * Individual statistic box used within the summary card.
 */
@Composable
private fun ImportStatBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}
