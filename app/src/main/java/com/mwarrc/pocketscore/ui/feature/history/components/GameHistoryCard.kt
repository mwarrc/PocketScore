package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GameHistoryCard(
    game: GameState,
    onDelete: () -> Unit,
    onArchive: () -> Unit,
    onResume: () -> Unit,
    onShare: () -> Unit,
    onViewDetails: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val maxScore = game.players.maxOfOrNull { it.score } ?: 0
    val winners = if (maxScore > 0 || game.players.any { it.score != 0 }) {
        game.players.filter { it.score == maxScore }
    } else emptyList()
    val isTie = winners.size > 1
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = timeFormat.format(Date(game.endTime ?: game.startTime))
    val nextTurnPlayer = game.players.find { it.id == game.currentPlayerId }
    val isResumable = !game.isFinalized

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = when {
            game.isArchived -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            isResumable -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isResumable) 2.dp else 1.dp,
        border = if (isResumable) {
            androidx.compose.foundation.BorderStroke(
                1.dp, 
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            )
        } else if (game.isArchived) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        } else null
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { isExpanded = !isExpanded }
            ) {
                ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (game.isArchived) {
                                Icon(
                                    Icons.Default.Archive, 
                                    null, 
                                    modifier = Modifier.size(14.dp).padding(end = 4.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                                    color = if (game.isArchived) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                val winner = winners.firstOrNull()
                                Text(
                                    winner?.name ?: "No Result",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (game.isArchived) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
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
                                            null,
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
                    },
                    supportingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                timeStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (game.isArchived) {
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
                    },
                    leadingContent = {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = when {
                                game.isArchived -> MaterialTheme.colorScheme.surfaceVariant
                                isResumable -> MaterialTheme.colorScheme.secondaryContainer
                                isTie -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isTie) {
                                    Icon(
                                        Icons.Default.Group,
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (isResumable) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                } else {
                                    val winner = winners.firstOrNull()
                                    Text(
                                        (winner?.name?.firstOrNull() ?: '?').toString(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isResumable) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        "Options",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    offset = androidx.compose.ui.unit.DpOffset((-8).dp, 0.dp),
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(if (game.isArchived) "Unarchive Match" else "Archive Match") },
                                        leadingIcon = { Icon(if (game.isArchived) Icons.Default.Unarchive else Icons.Default.Archive, null) },
                                        onClick = {
                                            onArchive()
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share Record") },
                                        leadingIcon = { Icon(Icons.Default.Share, null) },
                                        onClick = {
                                            onShare()
                                            showMenu = false
                                        }
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Permanently", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            onDelete()
                                            showMenu = false
                                        }
                                    )
                                }
                            }
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 72.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    )
                ) {
                    if (nextTurnPlayer != null && isResumable) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Next Turn: ${nextTurnPlayer.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    game.players.sortedByDescending { it.score }.forEach { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                player.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (winners.any { it.id == player.id }) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                            Text(
                                "${player.score}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (player.score < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (winners.any { it.id == player.id }) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (!game.isFinalized) {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Restore,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Resume Game", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Match Completed & Locked",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("View Detailed Records", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onShare,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.IosShare,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Share Match Record", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
