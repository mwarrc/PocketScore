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
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                Column {
                    // Points Summary Header
                    Text(
                        text = "Scores (Top to Bottom)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    // Compact Player Toggle Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 12.dp)
                    ) {
                        Spacer(Modifier.width(16.dp)) // Standard Left Margin
                        
                        sortedPlayers.forEach { player ->
                            val isExcluded = player.name in excludedPlayers
                            FilterChip(
                                selected = !isExcluded,
                                onClick = { onTogglePlayer(player.name) },
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            player.name, 
                                            style = MaterialTheme.typography.labelSmall,
                                            textDecoration = if (isExcluded) TextDecoration.LineThrough else null
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "${player.score}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = if (!isExcluded) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (!isExcluded) Icons.Default.Check else Icons.Default.Block,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = Color.Transparent,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = !isExcluded,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.dp
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp) // Fixed height to prevent "size bloating"
                            )
                        }
                        
                        Spacer(Modifier.width(16.dp)) // Standard Right Margin
                    }
                }
            }
        }
    }
}
