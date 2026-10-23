package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.ui.feature.history.utils.DateTimeUtils
import com.mwarrc.pocketscore.ui.feature.history.utils.GameStatsUtils

/**
 * Material 3 match card matching the larger Friends / Players tab design.
 *
 * Features:
 * - Large squircle-to-circle avatar that enters selection mode on tap
 * - Winner headline with time, player count and duration
 * - Status chips for the winning score and match state
 * - Expandable full scoreboard with resume / stats / share actions
 * - Overflow menu for archive, share, delete and select
 *
 * @param game The game state to display
 * @param onDelete Callback to delete this game
 * @param onArchive Callback to toggle archive status
 * @param onResume Callback to resume this game
 * @param onShare Callback to share this game's record
 * @param onViewDetails Callback to view detailed match analysis
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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

    // Collapse card while selecting matches
    LaunchedEffect(selectionMode) {
        if (selectionMode) isExpanded = false
    }

    // Calculate game statistics
    val winners = GameStatsUtils.getWinners(game)
    val isTie = GameStatsUtils.isTie(game)
    val isResumable = GameStatsUtils.isResumable(game)
    val nextTurnPlayer = game.players.find { it.id == game.currentPlayerId }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(220),
        label = "match_card_chevron"
    )

    val avatarContainer = when {
        isSelected -> MaterialTheme.colorScheme.primary
        game.isArchived -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        isResumable -> MaterialTheme.colorScheme.secondaryContainer
        isTie -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val avatarOnContainer = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        game.isArchived -> MaterialTheme.colorScheme.onSurfaceVariant
        isResumable -> MaterialTheme.colorScheme.onSecondaryContainer
        isTie -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        tonalElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .combinedClickable(
                    onClick = {
                        if (selectionMode) {
                            onToggleSelection()
                        } else {
                            isExpanded = !isExpanded
                        }
                    },
                    onLongClick = {
                        if (!selectionMode) {
                            onEnterSelectionMode()
                        } else {
                            onToggleSelection()
                        }
                    }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // ── Top Row: Avatar + Winner/Meta + Menu/Chevron ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar (tap directly toggles selection)
                Surface(
                    onClick = {
                        if (selectionMode) {
                            onToggleSelection()
                        } else {
                            onEnterSelectionMode()
                        }
                    },
                    shape = CircleShape,
                    color = avatarContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when {
                            isSelected -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = avatarOnContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            isTie -> Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = avatarOnContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            isResumable -> Icon(
                                imageVector = if (game.isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = avatarOnContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            winners.firstOrNull()?.let { it.score > 0 } == true -> Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Winner",
                                tint = avatarOnContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            else -> Text(
                                text = winners.firstOrNull()?.name?.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = avatarOnContainer
                            )
                        }
                    }
                }

                // Winner name and supporting meta
                Column(modifier = Modifier.weight(1f)) {
                    GameHistoryCardHeadline(
                        game = game,
                        winners = winners,
                        isTie = isTie
                    )
                    Spacer(Modifier.height(2.dp))
                    GameHistoryCardSupporting(game = game)
                }

                // Menu + expand indicator
                if (!selectionMode) {
                    Box {
                        GameHistoryCardMenuButton(onMenuToggle = { showMenu = true })

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

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(chevronRotation)
                    )
                }
            }

            // ── Status chips row ──
            GameHistoryStatusChips(
                game = game,
                winners = winners,
                isTie = isTie,
                isResumable = isResumable,
                modifier = Modifier.padding(start = 62.dp, top = 10.dp)
            )

            // ── Expanded details area ──
            AnimatedVisibility(visible = isExpanded && !selectionMode) {
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
 * Headline showing the winner(s) or a friendly fallback.
 */
@Composable
private fun GameHistoryCardHeadline(
    game: GameState,
    winners: List<Player>,
    isTie: Boolean
) {
    val textColor = if (game.isArchived) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (game.isArchived) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isTie) {
            Surface(
                color = MaterialTheme.colorScheme.tertiary,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "TIE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }

        Text(
            text = when {
                isTie -> winners.joinToString(" & ") { it.name }
                winners.isEmpty() && game.players.isNotEmpty() -> "Pacifist Run 🕊️"
                else -> winners.firstOrNull()?.name ?: "No Result"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Supporting line under the winner name: time, player count and duration.
 */
@Composable
private fun GameHistoryCardSupporting(game: GameState) {
    val timeStr = DateTimeUtils.formatTime(game.endTime ?: game.startTime)
    val durationStr = if (game.endTime != null && game.endTime!! > game.startTime) {
        DateTimeUtils.formatDuration(game.endTime!! - game.startTime)
    } else null

    val playerCountStr = when (game.players.size) {
        1 -> "Solo"
        2 -> "Heads up"
        else -> "${game.players.size} players"
    }

    Text(
        text = listOfNotNull(timeStr, playerCountStr, durationStr).joinToString(" • "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Compact chips summarising the score and the match state.
 */
@Composable
private fun GameHistoryStatusChips(
    game: GameState,
    winners: List<Player>,
    isTie: Boolean,
    isResumable: Boolean,
    modifier: Modifier = Modifier
) {
    val topScore = winners.firstOrNull()?.score

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Score chip (or player count for unfinished games)
        if (topScore != null && !isResumable) {
            val negative = topScore < 0
            MatchChip(
                icon = Icons.Default.EmojiEvents,
                text = "$topScore pts",
                containerColor = if (negative) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                contentColor = if (negative) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
        } else {
            MatchChip(
                icon = Icons.Default.Group,
                text = when (game.players.size) {
                    1 -> "Solo"
                    2 -> "Heads up"
                    else -> "${game.players.size} players"
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Match state chip
        when {
            isResumable -> MatchChip(
                icon = if (game.isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                text = if (game.isPaused) "Paused" else "Active",
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
            game.isArchived -> MatchChip(
                icon = Icons.Default.Archive,
                text = "Archived",
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                bordered = true
            )
            isTie -> MatchChip(
                icon = Icons.Default.Group,
                text = "Tie",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
            else -> MatchChip(
                icon = Icons.Default.CheckCircle,
                text = "Completed",
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Small pill used in the status chip row of a match card.
 */
@Composable
private fun MatchChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    bordered: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor,
        border = if (bordered) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
