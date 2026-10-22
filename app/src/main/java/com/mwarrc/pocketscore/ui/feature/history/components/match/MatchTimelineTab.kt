package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tab component that displaying a reverse-chronological timeline of all match events.
 * 
 * Features:
 * - **Player Filtering**: Quick-select chips to filter events by a specific player.
 * - **System Events**: Integration of match-level status changes (pause/resume).
 * - **Detailed Event Breakdown**: Shows points gained, previous/new score, and timestamps.
 * - **Correction Highlighting**: Visual indicators for match corrections (undofill).
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
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All Events" selector
            item {
                FilterChip(
                    selected = selectedPlayerFilter == null,
                    onClick = { selectedPlayerFilter = null },
                    label = { Text("All Events") },
                    border = null,
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
            items(players) { playerName ->
                val color = getPlayerColor(playerName)
                FilterChip(
                    selected = selectedPlayerFilter == playerName,
                    onClick = { 
                        selectedPlayerFilter = if (selectedPlayerFilter == playerName) null else playerName 
                    },
                    label = { Text(playerName) },
                    border = null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        selectedContainerColor = color.copy(alpha = 0.2f),
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
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Show latest events first
                items(filteredEvents.reversed()) { event ->
                    DetailEventItem(event)
                }
            }
        }
    }
}

/**
 * Individual item in the match timeline.
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline visual indicator (Dot)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, 
            modifier = Modifier.width(24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isUndo -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    isSystem -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    isNegative -> MaterialTheme.colorScheme.error
                    isZero -> MaterialTheme.colorScheme.outlineVariant
                    else -> playerColor
                },
                modifier = Modifier.size(10.dp).padding(top = 6.dp)
            ) {}
        }
        
        Spacer(Modifier.width(16.dp))
        
        // Event Content
        Column(modifier = Modifier.weight(1f)) {
            val headerText = when (event.type) {
                GameEventType.SCORE -> event.playerName ?: "Unknown"
                GameEventType.UNDO -> "Correction"
                GameEventType.STATUS_CHANGE -> "System"
                else -> event.playerName ?: "Event"
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystem) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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
                GameEventType.UNDO -> "Undo previous action"
                GameEventType.STATUS_CHANGE -> event.message ?: "Status changed"
                else -> event.message ?: ""
            }
            
            Text(
                text = detailText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isNegative || isUndo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Right-side Points Badge (for score events)
        if (isScore) {
            Text(
                text = "${if (event.points > 0) "+" else ""}${event.points}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    isNegative -> MaterialTheme.colorScheme.error
                    isZero -> MaterialTheme.colorScheme.outline
                    else -> playerColor
                }
            )
        }
    }
}

/**
 * Placeholder displayed when no events match the current filter.
 */
@Composable
private fun EmptyTimelineState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No events to show.",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
