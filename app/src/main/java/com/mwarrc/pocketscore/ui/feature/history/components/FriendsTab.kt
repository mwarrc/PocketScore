package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory

data class PlayerStats(
    val name: String,
    val gamesPlayed: Int,
    val wins: Int,
    val winRate: Float,
    val avgScore: Float,
    val bestScore: Int,
    val totalPoints: Int,
    val isHiddenInLeaderboard: Boolean
)

@Composable
fun FriendsTab(
    settings: AppSettings,
    history: GameHistory,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onRename: (String, String) -> Unit
) {
    var newFriendName by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var friendToRemove by remember { mutableStateOf<PlayerStats?>(null) }
    var friendToRename by remember { mutableStateOf<PlayerStats?>(null) }
    var renameValue by remember { mutableStateOf("") }

    val playerStats = remember(history, settings.savedPlayerNames, settings.hiddenPlayers) {
        settings.savedPlayerNames.map { name ->
            val games = history.pastGames.filter { game -> 
                game.players.any { it.name.trim().equals(name, ignoreCase = true) } 
            }
            
            val playerScores = games.mapNotNull { game ->
                game.players.find { it.name.trim().equals(name, ignoreCase = true) }?.score
            }

            val wins = games.count { game -> 
                val maxScore = if (game.players.isNotEmpty()) game.players.maxOf { it.score } else 0
                val winners = game.players.filter { it.score == maxScore }.map { it.name.trim().lowercase() }
                name.trim().lowercase() in winners
            }

            val played = games.size
            
            PlayerStats(
                name = name, 
                gamesPlayed = played, 
                wins = wins, 
                winRate = if (played > 0) (wins.toFloat() / played.toFloat()) * 100f else 0f,
                avgScore = if (played > 0) playerScores.average().toFloat() else 0f,
                bestScore = playerScores.maxOrNull() ?: 0,
                totalPoints = playerScores.sum(),
                isHiddenInLeaderboard = name in settings.hiddenPlayers
            )
        }.sortedByDescending { it.gamesPlayed }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Player") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (playerStats.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Group, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text("Your Roster is Empty", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Add friends to track their individual stats.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(playerStats, key = { it.name }) { stat ->
                    PlayerStatCard(
                        stat = stat,
                        onToggleLeaderboard = {
                            onUpdateSettings { s ->
                                val newHidden = if (stat.isHiddenInLeaderboard) {
                                    s.hiddenPlayers - stat.name
                                } else {
                                    s.hiddenPlayers + stat.name
                                }
                                s.copy(hiddenPlayers = newHidden)
                            }
                        },
                        onRemove = { friendToRemove = stat },
                        onRename = { 
                            friendToRename = stat
                            renameValue = stat.name
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        val trimmed = newFriendName.trim()
        val alreadyExists = settings.savedPlayerNames.any { it.equals(trimmed, ignoreCase = true) }
        val isValid = trimmed.isNotEmpty() && !alreadyExists

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Player") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFriendName,
                        onValueChange = { newFriendName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        isError = alreadyExists,
                        supportingText = if (alreadyExists) {
                            { Text("This name is already in your roster") }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isValid) {
                            onUpdateSettings { it.copy(savedPlayerNames = it.savedPlayerNames + trimmed) }
                            newFriendName = ""
                            showAddDialog = false
                        }
                    },
                    enabled = isValid,
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add to Roster") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (friendToRemove != null) {
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Remove from Roster?") },
            text = { Text("Removing ${friendToRemove?.name} will hide them from the 'Active Roster' picker, but their match history will still be safe.") },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings { s ->
                            s.copy(savedPlayerNames = s.savedPlayerNames.filter { it != friendToRemove?.name })
                        }
                        friendToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { friendToRemove = null }) { Text("Cancel") }
            }
        )
    }

    if (friendToRename != null) {
        val trimmed = renameValue.trim()
        val isTaken = settings.savedPlayerNames.any { it.trim().equals(trimmed, ignoreCase = true) && !it.trim().equals(friendToRename?.name?.trim(), ignoreCase = true) }
        
        AlertDialog(
            onDismissRequest = { friendToRename = null },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.tertiary) },
            title = { Text("Rename Player") },
            text = {
                Column {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            "Names are unique IDs. Renaming will update all past match records to match this new name.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = { renameValue = it },
                        label = { Text("Update Name") },
                        singleLine = true,
                        isError = isTaken,
                        supportingText = if (isTaken) { { Text("Name already taken") } } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isTaken && trimmed.isNotEmpty() && trimmed != friendToRename?.name) {
                            onRename(friendToRename!!.name, trimmed)
                            friendToRename = null
                        }
                    },
                    enabled = !isTaken && trimmed.isNotEmpty() && trimmed != friendToRename?.name,
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Update Records") }
            },
            dismissButton = {
                TextButton(onClick = { friendToRename = null }) { Text("Cancel") }
            }
        )
    }
}

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            stat.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stat.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (stat.isHiddenInLeaderboard) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.Default.VisibilityOff, 
                                "Hidden from leaderboard", 
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Text(
                        "${stat.gamesPlayed} Matches Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (stat.isHiddenInLeaderboard) "Show in Leaderboard" else "Hide from Leaderboard") },
                            leadingIcon = { Icon(if (stat.isHiddenInLeaderboard) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) },
                            onClick = { 
                                onToggleLeaderboard()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename Player") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { 
                                onRename()
                                showMenu = false
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Remove from Roster", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { 
                                onRemove()
                                showMenu = false
                            }
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Win Rate", "${stat.winRate.toInt()}%")
                StatItem("Wins", "${stat.wins}")
                StatItem("Avg", "${stat.avgScore.toInt()}")
                StatItem("Best", "${stat.bestScore}")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
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
