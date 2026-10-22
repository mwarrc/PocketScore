package com.mwarrc.pocketscore.ui.feature.history.import_.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A 4-cell breakdown grid shown at the top of the import screen.
 *
 * Displays at a glance:
 * - New matches being added
 * - Duplicate matches that will be skipped
 * - New players being created
 * - Existing players being merged
 *
 * @param newGamesCount     Number of matches not already in the local database.
 * @param duplicateCount    Number of matches that already exist locally.
 * @param newPlayersCount   Number of imported players with no local match.
 * @param mergingPlayersCount Number of imported players mapped to an existing local player.
 */
@Composable
fun ImportBreakdownCard(
    newGamesCount: Int,
    duplicateCount: Int,
    newPlayersCount: Int,
    mergingPlayersCount: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Import Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BreakdownItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AddCircle,
                    value = newGamesCount.toString(),
                    label = "New Matches",
                    color = MaterialTheme.colorScheme.primary
                )
                BreakdownItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ContentCopy,
                    value = duplicateCount.toString(),
                    label = "Duplicates",
                    color = if (duplicateCount > 0)
                        MaterialTheme.colorScheme.outline
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
                BreakdownItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.PersonAdd,
                    value = newPlayersCount.toString(),
                    label = "New Players",
                    color = MaterialTheme.colorScheme.secondary
                )
                BreakdownItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.MergeType,
                    value = mergingPlayersCount.toString(),
                    label = "Merging",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun BreakdownItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                fontSize = 9.sp
            )
        }
    }
}
