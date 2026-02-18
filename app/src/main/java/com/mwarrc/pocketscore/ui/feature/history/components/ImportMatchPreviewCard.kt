package com.mwarrc.pocketscore.ui.feature.history.import_.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Redesigned match preview card for the import flow.
 *
 * Shows player scores, winner, date, and a duplicate indicator when the
 * match already exists in the local database.
 *
 * @param game The game state to preview.
 * @param mappings Map of original player names to their newly mapped counterparts.
 * @param isDuplicate Whether this game already exists locally and will be skipped.
 * @param modifier Modifier for the card container.
 */
@Composable
fun ImportMatchPreviewCard(
    game: GameState,
    mappings: Map<String, String> = emptyMap(),
    isDuplicate: Boolean = false,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val sortedPlayers = remember(game, mappings) {
        game.players.sortedByDescending { it.score }
    }
    val winner = sortedPlayers.firstOrNull()
    val winnerDisplayName = winner?.name?.let { mappings[it] ?: it } ?: "N/A"

    val containerColor = when {
        isDuplicate -> MaterialTheme.colorScheme.surfaceContainerLowest
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val borderColor = when {
        isDuplicate -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDuplicate) 0.55f else 1f),
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // ── Header row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date + player count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday,
                        null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = dateFormat.format(Date(game.startTime)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${game.players.size}P",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status badge
                if (isDuplicate) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Already exists",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                "New",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Player score rows ──
            sortedPlayers.take(4).forEachIndexed { index, player ->
                val displayName = mappings[player.name] ?: player.name
                val isWinner = index == 0 && !isDuplicate
                val maxScore = sortedPlayers.firstOrNull()?.score?.takeIf { it > 0 } ?: 1

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank indicator
                    Surface(
                        shape = CircleShape,
                        color = when (index) {
                            0 -> if (!isDuplicate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (index == 0 && !isDuplicate) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))

                    // Player name
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                        color = if (isWinner) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Score bar + value
                    val fraction = if (maxScore > 0) player.score.toFloat() / maxScore else 0f
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(2.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {}
                        Surface(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction.coerceIn(0f, 1f)),
                            shape = RoundedCornerShape(2.dp),
                            color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        ) {}
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = player.score.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isWinner) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(28.dp)
                    )
                }
            }

            if (sortedPlayers.size > 4) {
                Text(
                    text = "+ ${sortedPlayers.size - 4} more",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 28.dp)
                )
            }
        }
    }
}
