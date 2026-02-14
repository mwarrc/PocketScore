package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onArchiveGame: (String) -> Unit,
    onShareGame: (String) -> Unit,
    onViewDetails: (String) -> Unit
) {
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
        val groupedGames = remember(history.pastGames) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000))
            val headerFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
            val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

            history.pastGames
                .sortedByDescending { it.endTime ?: it.startTime }
                .groupBy { game ->
                    val date = Date(game.endTime ?: game.startTime)
                    val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                    
                    when (dateKey) {
                        today -> "Today"
                        yesterday -> "Yesterday"
                        else -> {
                            val gameYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
                            if (gameYear == currentYear) {
                                headerFormat.format(date)
                            } else {
                                SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(date)
                            }
                        }
                    }
                }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {
            groupedGames.forEach { (dateHeader, games) ->
                item(key = dateHeader) {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(top = 8.dp)
                    )
                }

                items(games, key = { it.id }) { game ->
                    GameHistoryCard(
                        game = game,
                        onDelete = { gameToDelete = game },
                        onArchive = { onArchiveGame(game.id) },
                        onResume = { gameToResume = game },
                        onShare = { onShareGame(game.id) },
                        onViewDetails = { onViewDetails(game.id) }
                    )
                }
            }
        }
    }
}
