package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

/**
 * Material 3 Player Card redesigned according to the Friends tab specification.
 *
 * Features:
 * - Color-cycling avatar (tap to select/deselect or enter selection mode)
 * - Win rate and game statistics summary
 * - Quick-action chips for Leaderboard and Home picker visibility
 * - Expandable settings panel with M3 switches for instant toggling
 * - Rename and Remove actions
 *
 * @param stat Player statistics to display.
 * @param selectionMode Whether selection mode is currently active.
 * @param isSelected Whether this player is currently selected.
 * @param onToggleSelection Callback to toggle this player's selection state.
 * @param onEnterSelectionMode Callback to enter selection mode with this player.
 * @param onToggleLeaderboard Callback to toggle leaderboard visibility.
 * @param onToggleDeactivated Callback to toggle Home picker visibility.
 * @param onRemove Callback to initiate player removal.
 * @param onRename Callback to initiate player rename.
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
    var isExpanded by remember { mutableStateOf(false) }

    // Collapse card when entering selection mode
    LaunchedEffect(selectionMode) {
        if (selectionMode) isExpanded = false
    }

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(200),
        label = "chevron_rotation"
    )

    // Avatar color cycling matching HTML design tokens: c0, c1, c2
    val colorCycleIndex = (stat.name.hashCode().absoluteValue) % 3
    val (avatarContainer, avatarOnContainer) = when (colorCycleIndex) {
        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
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
            // ── Top Row: Avatar + Name/Stats + Chevron ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar button (tap directly toggles selection)
                Surface(
                    onClick = {
                        if (selectionMode) {
                            onToggleSelection()
                        } else {
                            onEnterSelectionMode()
                        }
                    },
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else avatarContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = stat.name.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = avatarOnContainer
                            )
                        }
                    }
                }

                // Name and stats
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stat.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${stat.gamesPlayed} games • ${stat.wins} wins • ${stat.winRate.toInt()}% win rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Trailing expand/collapse indicator
                if (!selectionMode) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle)
                    )
                }
            }

            // ── Quick-Action Status Chips Row ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 62.dp, top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip 1: Leaderboard
                val onLeaderboard = !stat.isHiddenInLeaderboard
                Surface(
                    onClick = { if (!selectionMode) onToggleLeaderboard() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (onLeaderboard) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    border = if (!onLeaderboard) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (onLeaderboard) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (onLeaderboard) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (onLeaderboard) "On leaderboard" else "Hidden from leaderboard",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (onLeaderboard) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chip 2: Home Picker
                val inHome = !stat.isDeactivated
                Surface(
                    onClick = { if (!selectionMode) onToggleDeactivated() },
                    shape = RoundedCornerShape(8.dp),
                    color = if (inHome) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    border = if (!inHome) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (inHome) Icons.Default.Home else Icons.Default.HomeWork,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (inHome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (inHome) "In Home picker" else "Not in Home picker",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (inHome) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Expanded Details Area ──
            AnimatedVisibility(
                visible = isExpanded && !selectionMode,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit = fadeOut(tween(140)) + shrinkVertically(tween(180))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )

                    Spacer(Modifier.height(8.dp))

                    // Setting 1: Leaderboard toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (!stat.isHiddenInLeaderboard) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show on leaderboard",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (!stat.isHiddenInLeaderboard) {
                                    "${stat.name} appears in the rankings. Games stay in History."
                                } else {
                                    "${stat.name} is left out of the rankings. Games stay in History."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = !stat.isHiddenInLeaderboard,
                            onCheckedChange = { onToggleLeaderboard() }
                        )
                    }

                    // Setting 2: Home Picker toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show in Home picker",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (!stat.isDeactivated) {
                                    "${stat.name} can be picked when you start a game."
                                } else {
                                    "${stat.name} is hidden when you start a game."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = !stat.isDeactivated,
                            onCheckedChange = { onToggleDeactivated() }
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onRename,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Rename", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.width(4.dp))

                        TextButton(
                            onClick = onRemove,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Remove", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Explanatory footer caption
                    Text(
                        text = "Rename updates every past match. Remove keeps match history safe.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, end = 4.dp)
                    )
                }
            }
        }
    }
}
