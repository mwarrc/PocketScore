package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Card displaying a player's statistics and management options.
 * 
 * Features:
 * - Avatar with player initial
 * - Player name with hidden indicator
 * - Games played count
 * - Statistics grid (Win Rate, Wins, Average, Best Score)
 * - Context menu for actions (toggle leaderboard visibility, rename, remove)
 * 
 * @param stat Player statistics to display
 * @param onToggleLeaderboard Callback to toggle leaderboard visibility
 * @param onRemove Callback to initiate player removal
 * @param onRename Callback to initiate player rename
 */
@Composable
fun PlayerStatCard(
    stat: PlayerStats,
    onToggleLeaderboard: () -> Unit,
    onRemove: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Avatar, Name, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerAvatar(
                    initial = stat.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                    size = 44.dp
                )
                
                Spacer(Modifier.width(16.dp))
                
                PlayerInfo(
                    name = stat.name,
                    gamesPlayed = stat.gamesPlayed,
                    isHidden = stat.isHiddenInLeaderboard,
                    modifier = Modifier.weight(1f)
                )

                PlayerCardMenu(
                    isHidden = stat.isHiddenInLeaderboard,
                    showMenu = showMenu,
                    onMenuToggle = { showMenu = !showMenu },
                    onDismiss = { showMenu = false },
                    onToggleLeaderboard = onToggleLeaderboard,
                    onRename = onRename,
                    onRemove = onRemove
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Statistics Grid
            PlayerStatsGrid(stat = stat)
        }
    }
}

/**
 * Circular avatar displaying player's initial.
 */
@Composable
private fun PlayerAvatar(
    initial: String,
    size: androidx.compose.ui.unit.Dp
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Player name and games played count with optional hidden indicator.
 */
@Composable
private fun PlayerInfo(
    name: String,
    gamesPlayed: Int,
    isHidden: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (isHidden) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.VisibilityOff,
                    contentDescription = "Hidden from leaderboard",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        
        Text(
            "$gamesPlayed Matches Total",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Context menu for player card actions.
 */
@Composable
private fun PlayerCardMenu(
    isHidden: Boolean,
    showMenu: Boolean,
    onMenuToggle: () -> Unit,
    onDismiss: () -> Unit,
    onToggleLeaderboard: () -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit
) {
    Box {
        IconButton(onClick = onMenuToggle) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }
        
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onDismiss
        ) {
            // Toggle leaderboard visibility
            DropdownMenuItem(
                text = { 
                    Text(
                        if (isHidden) "Show in Leaderboard" 
                        else "Hide from Leaderboard"
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        if (isHidden) Icons.Default.Visibility 
                        else Icons.Default.VisibilityOff, 
                        contentDescription = null
                    ) 
                },
                onClick = {
                    onToggleLeaderboard()
                    onDismiss()
                }
            )
            
            // Rename player
            DropdownMenuItem(
                text = { Text("Rename Player") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    onRename()
                    onDismiss()
                }
            )
            
            HorizontalDivider()
            
            // Remove from roster
            DropdownMenuItem(
                text = { 
                    Text(
                        "Remove from Roster", 
                        color = MaterialTheme.colorScheme.error
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    ) 
                },
                onClick = {
                    onRemove()
                    onDismiss()
                }
            )
        }
    }
}

/**
 * Grid displaying player statistics.
 */
@Composable
private fun PlayerStatsGrid(stat: PlayerStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem(label = "Win Rate", value = "${stat.winRate.toInt()}%")
        StatItem(label = "Wins", value = "${stat.wins}")
        StatItem(label = "Avg", value = "${stat.avgScore.toInt()}")
        StatItem(label = "Best", value = "${stat.bestScore}")
    }
}

/**
 * Individual statistic item with label and value.
 */
@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
