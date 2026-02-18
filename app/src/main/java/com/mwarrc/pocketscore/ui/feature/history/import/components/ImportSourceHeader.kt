package com.mwarrc.pocketscore.ui.feature.history.import_.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Header card for the import screen that shows the source device, share date,
 * and quick stat chips for player and match counts.
 *
 * @param shareData The structured data package being imported.
 */
@Composable
fun ImportSourceHeader(shareData: PocketScoreShare) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }
    val formattedDate = remember(shareData.shareDate) {
        dateFormat.format(Date(shareData.shareDate))
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device icon
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Devices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(13.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shareData.sourceDevice ?: "Unknown Device",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Stats chips
            Column(horizontalAlignment = Alignment.End) {
                ImportStatChip(
                    icon = Icons.Default.Person,
                    label = "${shareData.friends.size} players",
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(4.dp))
                ImportStatChip(
                    icon = Icons.Default.History,
                    label = "${shareData.games.size} matches",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

/**
 * Small pill chip showing an icon + label in a tinted surface.
 */
@Composable
fun ImportStatChip(icon: ImageVector, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(12.dp), tint = color)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
