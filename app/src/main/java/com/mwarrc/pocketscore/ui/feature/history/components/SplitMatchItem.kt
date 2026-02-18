package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*

/**
 * A selectable item representing a single match in the history list.
 * 
 * @param game The game state to display
 * @param isSelected Whether the match is currently selected for settlement
 * @param onToggle Callback when selection state is toggled
 */
@Composable
fun SplitMatchItem(
    game: GameState,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val maxScore = remember(game) { game.players.maxOfOrNull { it.score } ?: 0 }
    val winners = remember(game) { game.players.filter { it.score == maxScore } }
    val sortedPlayers = remember(game) { game.players.sortedByDescending { it.score } }
    
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = timeFormat.format(Date(game.endTime ?: game.startTime))

    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) 
        else 
            MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { 
                Text(
                    text = "Winner: ${winners.joinToString { it.name }}", 
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                ) 
            },
            supportingContent = { 
                Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("$timeStr • ${game.players.size} players")
                        if (game.isArchived) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ARCHIVED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = sortedPlayers.joinToString { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingContent = {
                Checkbox(
                    checked = isSelected, 
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}
