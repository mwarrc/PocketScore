package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
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
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.ui.feature.history.utils.DateTimeUtils
import com.mwarrc.pocketscore.ui.feature.history.utils.GameStatsUtils

/**
 * Card component displaying a game from the match history.
 * 
 * Features:
 * - Compact view showing winner, score, and time
 * - Expandable view with full player list and actions
 * - Visual indicators for ties, archived games, and resumable matches
 * - Context menu for archive, share, and delete actions
 * - Quick resume button for unfinished games
 * 
 * @param game The game state to display
 * @param onDelete Callback to delete this game
 * @param onArchive Callback to toggle archive status
 * @param onResume Callback to resume this game
 * @param onShare Callback to share this game's record
 * @param onViewDetails Callback to view detailed match analysis
 */
@Composable
fun GameHistoryCard(
    game: GameState,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onResume: () -> Unit,
    onShare: () -> Unit,
    onViewDetails: () -> Unit,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelection: () -> Unit = {},
    onEnterSelectionMode: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    // Calculate game statistics
    val winners = GameStatsUtils.getWinners(game)
    val isTie = GameStatsUtils.isTie(game)
    val isResumable = GameStatsUtils.isResumable(game)
    val nextTurnPlayer = game.players.find { it.id == game.currentPlayerId }
    val timeStr = DateTimeUtils.formatTime(game.endTime ?: game.startTime)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            game.isArchived -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            isResumable -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isResumable) 2.dp else 1.dp,
        border = when {
            isResumable -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            )
            game.isArchived -> BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            else -> null
        }
    ) {
        Column {
            // Collapsed card content
            @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .combinedClickable(
                        onClick = { 
                            if (selectionMode) onToggleSelection() 
                            else isExpanded = !isExpanded 
                        },
                        onLongClick = {
                            if (!selectionMode) onEnterSelectionMode()
                            else onToggleSelection()
                        }
                    )
            ) {
                ListItem(
                    headlineContent = {
                        GameHistoryCardHeadline(
                            game = game,
                            winners = winners,
                            isTie = isTie,
                            isResumable = isResumable
                        )
                    },
                    supportingContent = {
                        GameHistoryCardSupporting(
                            timeStr = timeStr,
                            isArchived = game.isArchived,
                            isTie = isTie,
                            winners = winners
                        )
                    },
                    leadingContent = {
                        GameHistoryCardLeading(
                            isTie = isTie,
                            isResumable = isResumable,
                            isArchived = game.isArchived,
                            winners = winners,
                            isSelected = isSelected
                        )
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                GameHistoryCardMenuButton(
                                    onMenuToggle = { showMenu = true }
                                )
                                
                                GameHistoryCardMenu(
                                    expanded = showMenu,
                                    isArchived = game.isArchived,
                                    onDismiss = { showMenu = false },
                                    onArchive = onArchive,
                                    onShare = onShare,
                                    onDelete = onDelete,
                                    onSelect = onEnterSelectionMode
                                )
                            }
                            
                            IconButton(onClick = { isExpanded = !isExpanded }) {
                                Icon(
                                    if (isExpanded) Icons.Default.KeyboardArrowUp 
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // Expanded card content
            AnimatedVisibility(visible = isExpanded) {
                GameHistoryCardExpandedContent(
                    game = game,
                    winners = winners,
                    nextTurnPlayer = nextTurnPlayer,
                    isResumable = isResumable,
                    onResume = onResume,
                    onViewDetails = onViewDetails,
                    onShare = onShare
                )
            }
        }
    }
}

/**
 * Headline content showing winner name(s) and score.
 */
@Composable
private fun GameHistoryCardHeadline(
    game: GameState,
    winners: List<com.mwarrc.pocketscore.domain.model.Player>,
    isTie: Boolean,
    isResumable: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Archive indicator
        if (game.isArchived) {
            Icon(
                Icons.Default.Archive,
                contentDescription = null,
                modifier = Modifier.size(14.dp).padding(end = 4.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Tie badge and names
        if (isTie) {
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    " TIE ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }
            Text(
                winners.joinToString(" & ") { it.name },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = if (game.isArchived) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        } else {
            // Single winner or no result
            val winner = winners.firstOrNull()
            
            if (winner == null && game.players.isNotEmpty()) {
                // Easter Egg for 0-0 games (Pacifist Run)
                Text(
                    "Pacifist Run 🕊️", 
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    winner?.name ?: "No Result",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (game.isArchived) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (winner != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${winner.score} pts",
                        style = MaterialTheme.typography.titleMedium,
                        color = when {
                            game.isArchived -> MaterialTheme.colorScheme.onSurfaceVariant
                            winner.score < 0 -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Active game badge
        if (isResumable && !game.isArchived) {
            Spacer(Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Pending,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Supporting content showing time and additional metadata.
 */
@Composable
private fun GameHistoryCardSupporting(
    timeStr: String,
    isArchived: Boolean,
    isTie: Boolean,
    winners: List<com.mwarrc.pocketscore.domain.model.Player>
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            timeStr,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        if (isArchived) {
            Text(
                " • Archived",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isTie) {
            Spacer(Modifier.width(8.dp))
            Text(
                "• Shared ${winners.firstOrNull()?.score ?: 0} pts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Leading icon/avatar for the card.
 */
@Composable
private fun GameHistoryCardLeading(
    isTie: Boolean,
    isResumable: Boolean,
    isArchived: Boolean,
    winners: List<com.mwarrc.pocketscore.domain.model.Player>,
    isSelected: Boolean
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isArchived -> MaterialTheme.colorScheme.surfaceVariant
            isResumable -> MaterialTheme.colorScheme.secondaryContainer
            isTie -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            } else if (isTie) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isResumable) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    }
                )
            } else {
                val winner = winners.firstOrNull()
                Text(
                    (winner?.name?.firstOrNull() ?: '?').toString(),
                    fontWeight = FontWeight.Bold,
                    color = if (isResumable) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}
