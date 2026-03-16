package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitMatchItem(
    game: GameState,
    isSelected: Boolean,
    excludedPlayers: Set<String>,
    onToggle: () -> Unit,
    onTogglePlayer: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxScore = remember(game) { game.players.maxOfOrNull { it.score } ?: 0 }
    val winners = remember(game) { game.players.filter { it.score == maxScore } }
    val sortedPlayers = remember(game) { game.players.sortedByDescending { it.score } }
    
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = timeFormat.format(Date(game.endTime ?: game.startTime))

    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotation")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f) 
        else 
            MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ONLY the top section is clickable for match selection
            // This prevents gesture conflicts with the player scrolling row below.
            Surface(
                onClick = onToggle,
                color = Color.Transparent,
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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("$timeStr • ${game.players.size} players", style = MaterialTheme.typography.bodySmall)
                                
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

                            if (excludedPlayers.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.padding(top = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PersonOff, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = if (excludedPlayers.size == game.players.size) 
                                            "ALL PLAYERS EXCLUDED" 
                                        else 
                                            "EXCLUDED: ${excludedPlayers.joinToString()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected, 
                                onCheckedChange = { onToggle() },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            IconButton(onClick = { isExpanded = !isExpanded }) {
                                Icon(
                                    Icons.Default.ExpandMore, 
                                    contentDescription = "Expand",
                                    modifier = Modifier.rotate(rotation)
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                // Full-Width Player Toggle Row
                // Removed horizontal padding from modifier to allow it to "span" the card width.
                // Added Spacers inside the Row to maintain the 16.dp margin for content.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    Spacer(Modifier.width(8.dp)) // Start Margin
                    
                    sortedPlayers.forEach { player ->
                        val isExcluded = player.name in excludedPlayers
                        FilterChip(
                            selected = !isExcluded,
                            onClick = { onTogglePlayer(player.name) },
                            label = { 
                                Text(
                                    player.name, 
                                    style = MaterialTheme.typography.labelSmall,
                                    textDecoration = if (isExcluded) TextDecoration.LineThrough else null
                                ) 
                            },
                            leadingIcon = if (!isExcluded) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(12.dp)) }
                            } else {
                                { Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp)) }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                containerColor = if (isExcluded) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isExcluded) FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                                borderWidth = 1.dp
                            ) else FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                    
                    Spacer(Modifier.width(8.dp)) // End Margin
                }
            }
        }
    }
}
