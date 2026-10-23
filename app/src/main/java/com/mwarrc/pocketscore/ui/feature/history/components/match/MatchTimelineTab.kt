package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tab component displaying a reverse-chronological timeline of all match events.
 * Redesigned according to modern Material 3 specifications.
 * 
 * Features:
 * - **Player Filtering**: Clean M3 filter chips with player color dots.
 * - **Event Cards**: Rounded cards with leading event category icons and timestamp badges.
 * - **Points Badges**: High-contrast tonal pill chips for positive, negative, and zero scores.
 * - **Polished Empty State**: Informative placeholder when no events match the filter.
 * 
 * @param game The game state containing the event history to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTimelineTab(game: GameState) {
    // UI state for event filtering
    var selectedPlayerFilter by remember { mutableStateOf<String?>(null) }
    
    // Unique list of player names for filtering chips
    val players = remember(game) { game.players.map { it.name } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Player filter selection row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All Events" selector
            item {
                FilterChip(
                    selected = selectedPlayerFilter == null,
                    onClick = { selectedPlayerFilter = null },
                    label = { 
                        Text(
                            text = "All Events",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedPlayerFilter == null,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = CircleShape
                )
            }
            
            // Individual player selectors
            items(players, key = { it }) { playerName ->
                val color = getPlayerColor(playerName)
                val isSelected = selectedPlayerFilter == playerName

                FilterChip(
                    selected = isSelected,
                    onClick = { 
                        selectedPlayerFilter = if (isSelected) null else playerName 
                    },
                    label = { 
                        Text(
                            text = playerName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) color else color.copy(alpha = 0.4f))
                        )
                    },
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        selectedBorderColor = color
                    ),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        selectedContainerColor = color.copy(alpha = 0.14f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLabelColor = color
                    ),
                    shape = CircleShape
                )
            }
        }

        // Apply filtering logic to global events
        val filteredEvents = remember(game.globalEvents, selectedPlayerFilter) {
            if (selectedPlayerFilter == null) {
                game.globalEvents
            } else {
                game.globalEvents.filter { 
                    it.playerName == selectedPlayerFilter || it.playerName == "SYSTEM" 
                }
            }
        }

        if (filteredEvents.isEmpty()) {
            EmptyTimelineState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Show latest events first
                items(filteredEvents.reversed(), key = { "${it.timestamp}_${it.playerName}_${it.points}" }) { event ->
                    DetailEventItem(event)
                }
            }
        }
    }
}

/**
 * Individual event card in the match timeline.
 */
@Composable
private fun DetailEventItem(event: GameEvent) {
    val playerColor = getPlayerColor(event.playerName ?: "")
    
    // Event type classification
    val isUndo = event.type == GameEventType.UNDO
    val isSystem = event.playerName == "SYSTEM"
    val isScore = event.type == GameEventType.SCORE
    val isNegative = isScore && event.points < 0
    val isZero = isScore && event.points == 0

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Event Icon Indicator
            Surface(
                shape = CircleShape,
                color = when {
                    isUndo -> MaterialTheme.colorScheme.errorContainer
                    isSystem -> MaterialTheme.colorScheme.surfaceContainerHighest
                    isNegative -> MaterialTheme.colorScheme.errorContainer
                    isZero -> MaterialTheme.colorScheme.surfaceContainerHighest
                    else -> playerColor.copy(alpha = 0.14f)
                },
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            isUndo -> Icons.AutoMirrored.Filled.Undo
                            isSystem -> Icons.Default.Info
                            isNegative -> Icons.Default.Warning
                            isZero -> Icons.Default.Close
                            else -> Icons.Default.SportsScore
                        },
                        contentDescription = null,
                        tint = when {
                            isUndo -> MaterialTheme.colorScheme.onErrorContainer
                            isSystem -> MaterialTheme.colorScheme.onSurfaceVariant
                            isNegative -> MaterialTheme.colorScheme.onErrorContainer
                            isZero -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> playerColor
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            // Event Content
            Column(modifier = Modifier.weight(1f)) {
                val headerText = when (event.type) {
                    GameEventType.SCORE -> event.playerName ?: "Unknown"
                    GameEventType.UNDO -> "Correction (Undo)"
                    GameEventType.STATUS_CHANGE -> "System Status"
                    else -> event.playerName ?: "Event"
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                val detailText = when (event.type) {
                    GameEventType.SCORE -> {
                        if (event.previousScore != null && event.newScore != null) {
                            "Score updated (${event.previousScore} → ${event.newScore})"
                        } else if (isZero) {
                            "Missed turn (0 pts)"
                        } else if (isNegative) {
                            "Penalty: ${event.points} pts"
                        } else {
                            "Scored ${event.points} pts"
                        }
                    }
                    GameEventType.UNDO -> "Reverted previous action"
                    GameEventType.STATUS_CHANGE -> event.message ?: "Match status updated"
                    else -> event.message ?: ""
                }

                Text(
                    text = detailText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right-side Points Badge (for score events)
            if (isScore) {
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = CircleShape,
                    color = when {
                        isNegative -> MaterialTheme.colorScheme.errorContainer
                        isZero -> MaterialTheme.colorScheme.surfaceContainerHighest
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                ) {
                    Text(
                        text = "${if (event.points > 0) "+" else ""}${event.points}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = when {
                            isNegative -> MaterialTheme.colorScheme.onErrorContainer
                            isZero -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Placeholder displayed when no events match the current filter.
 */
@Composable
private fun EmptyTimelineState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No events to show",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Match actions and score changes will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
