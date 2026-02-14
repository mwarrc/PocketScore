package com.mwarrc.pocketscore.ui.feature.history.components.match

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTimelineTab(game: GameState) {
    var selectedPlayerFilter by remember { mutableStateOf<String?>(null) }
    val players = remember(game) { game.players.map { it.name } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Minimal Filter Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedPlayerFilter == null,
                    onClick = { selectedPlayerFilter = null },
                    label = { Text("All Events") },
                    border = null, // Minimal, no border for unselected
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = CircleShape
                )
            }
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

        val filteredEvents = remember(game.globalEvents, selectedPlayerFilter) {
            if (selectedPlayerFilter == null) {
                game.globalEvents
            } else {
                game.globalEvents.filter { it.playerName == selectedPlayerFilter || it.playerName == "SYSTEM" }
            }
        }

        if (filteredEvents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No events to show.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredEvents.reversed()) { event ->
                    DetailEventItem(event)
                }
            }
        }
    }
}

@Composable
private fun DetailEventItem(event: GameEvent) {
    val playerColor = getPlayerColor(event.playerName ?: "")
    
    // Determine style based on event impact
    val isUndo = event.type == GameEventType.UNDO
    val isSystem = event.playerName == "SYSTEM"
    val isScore = event.type == GameEventType.SCORE
    val isNegative = isScore && event.points < 0
    val isZero = isScore && event.points == 0
    val isBigScore = isScore && event.points >= 10 // Arbitrary "big" logic, or relative logic?

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Dot/Icon
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
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
            // Vertical line connection could go here if we wanted complex drawing
        }
        
        Spacer(Modifier.width(16.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            val headerText = when (event.type) {
                GameEventType.SCORE -> event.playerName ?: "Unknown"
                GameEventType.UNDO -> "Correction"
                GameEventType.STATUS_CHANGE -> "System"
                else -> event.playerName ?: "Event"
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    headerText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystem) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(event.timestamp)),
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
                detailText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isNegative || isUndo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Points Badge (Right side)
        if (isScore) {
            Text(
                "${if (event.points > 0) "+" else ""}${event.points}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isNegative) MaterialTheme.colorScheme.error else if (isZero) MaterialTheme.colorScheme.outline else playerColor
            )
        }
    }
}
