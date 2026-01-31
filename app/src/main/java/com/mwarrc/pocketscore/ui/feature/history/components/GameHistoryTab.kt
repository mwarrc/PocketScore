package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GameHistoryTab(
    history: GameHistory,
    onNavigateToGame: () -> Unit,
    onResumeGame: (GameState, Boolean) -> Unit,
    onDeleteGame: (String) -> Unit,
    onShareGame: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    var gameToResume by remember { mutableStateOf<GameState?>(null) }
    var gameToDelete by remember { mutableStateOf<GameState?>(null) }

    if (gameToResume != null) {
        AlertDialog(
            onDismissRequest = { gameToResume = null },
            icon = { Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("How to Resume?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Choose how you'd like to rejoin this match session.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Button(
                        onClick = {
                            onResumeGame(gameToResume!!, false)
                            gameToResume = null
                            onNavigateToGame()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Resume (New Entry)", fontWeight = FontWeight.Bold)
                            Text("Keep old data as a separate record", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Button(
                        onClick = {
                            onResumeGame(gameToResume!!, true)
                            gameToResume = null
                            onNavigateToGame()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Replace Game", fontWeight = FontWeight.Bold)
                            Text("Overwrite and continue old session", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { gameToResume = null }) { Text("Go Back") }
            }
        )
    }

    if (gameToDelete != null) {
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete This Record?") },
            text = { Text("Are you sure you want to permanently delete this game record? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGame(gameToDelete!!.id)
                        gameToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (history.pastGames.isEmpty()) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.History,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No history yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            items(history.pastGames.sortedByDescending { it.endTime ?: it.startTime }) { game ->
                var isExpanded by remember { mutableStateOf(false) }
                val winner = game.players.maxByOrNull { it.score }
                val dateStr = dateFormat.format(Date(game.endTime ?: game.startTime))
                val nextTurnPlayer = game.players.find { it.id == game.currentPlayerId }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
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
                                        Text(
                                            winner?.name ?: "No Winner",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        if (winner != null) {
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "${winner.score} pts",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (winner.score < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                },
                                supportingContent = {
                                    Text(
                                        dateStr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingContent = {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                (winner?.name?.firstOrNull() ?: '?').toString(),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                },
                                trailingContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { gameToDelete = game }) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                "Delete",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
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
                                if (nextTurnPlayer != null) {
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
                                            fontWeight = if (player.id == winner?.id) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            }
                                        )
                                        Text(
                                            "${player.score}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (player.score < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (player.id == winner?.id) {
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
                                        onClick = { gameToResume = game },
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

                                Spacer(Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { onShareGame(game.id) },
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
        }
    }
}
