package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Minimal list item displaying a player's key info and management options.
 * 
 * Features:
 * - Avatar with player initial (or Checkbox in selection mode)
 * - Player name with status indicators
 * - Games played count
 * - Context menu for actions
 * - Long-press to enter selection mode
 * 
 * @param stat Player statistics to display
 * @param selectionMode Whether the list is in selection mode
 * @param isSelected Whether this specific item is selected
 * @param onToggleSelection Callback to toggle selection
 * @param onEnterSelectionMode Callback to start selection mode
 * @param onToggleLeaderboard Callback to toggle leaderboard visibility
 * @param onToggleDeactivated Callback to toggle home screen visibility
 * @param onRemove Callback to initiate player removal
 * @param onRename Callback to initiate player rename
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PlayerStatCard(
    stat: PlayerStats,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onEnterSelectionMode: () -> Unit = {},
    onToggleLeaderboard: () -> Unit,
    onToggleDeactivated: () -> Unit,
    onRemove: () -> Unit,
    onRename: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) 
        else 
            MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = { 
                        if (selectionMode) onToggleSelection() 
                        else showMenu = true 
                    },
                    onLongClick = {
                        if (!selectionMode) onEnterSelectionMode()
                        else onToggleSelection()
                    }
                )
        ) {
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stat.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(Modifier.width(8.dp))
                        
                        // Status Indicators
                        if (stat.isHiddenInLeaderboard) {
                            Icon(
                                Icons.Default.VisibilityOff,
                                contentDescription = "Hidden from Leaderboard",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        if (stat.isDeactivated) {
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                Icons.Default.HomeWork,
                                contentDescription = "Hidden from Home",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                supportingContent = {
                    Text(
                        "${stat.gamesPlayed} Matches • ${stat.winRate.toInt()}% Win Rate",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingContent = {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        stat.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                },
                trailingContent = {
                    if (!selectionMode) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                            }
                            
                            PlayerCardMenu(
                                isHidden = stat.isHiddenInLeaderboard,
                                isDeactivated = stat.isDeactivated,
                                showMenu = showMenu,
                                onDismiss = { showMenu = false },
                                onToggleLeaderboard = onToggleLeaderboard,
                                onToggleDeactivated = onToggleDeactivated,
                                onRename = onRename,
                                onRemove = onRemove,
                                onSelect = onEnterSelectionMode
                            )
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

/**
 * Context menu for player card actions.
 */
@Composable
private fun PlayerCardMenu(
    isHidden: Boolean,
    isDeactivated: Boolean,
    showMenu: Boolean,
    onDismiss: () -> Unit,
    onToggleLeaderboard: () -> Unit,
    onToggleDeactivated: () -> Unit,
    onRename: () -> Unit,
    onRemove: () -> Unit,
    onSelect: () -> Unit
) {
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
        
        // Toggle home screen visibility (Deactivate)
        DropdownMenuItem(
            text = { 
                Text(
                    if (isDeactivated) "Activate for Home" 
                    else "Hide from Home"
                ) 
            },
            leadingIcon = { 
                Icon(
                    if (isDeactivated) Icons.Default.Home 
                    else Icons.Default.HomeWork, 
                    contentDescription = null
                ) 
            },
            onClick = {
                onToggleDeactivated()
                onDismiss()
            }
        )
        
        // Multi-select
        DropdownMenuItem(
            text = { Text("Select Multiple") },
            leadingIcon = { Icon(Icons.Default.Checklist, contentDescription = null) },
            onClick = {
                onSelect()
                onDismiss()
            }
        )

        HorizontalDivider()

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
