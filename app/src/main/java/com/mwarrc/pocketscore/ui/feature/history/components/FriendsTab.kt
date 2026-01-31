package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
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
    val highestScore: Int,
    val lowestScore: Int,
    val totalPoints: Int
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

    // Calculate stats on the fly
    val playerStats = remember(history, settings.savedPlayerNames) {
        settings.savedPlayerNames.map { name ->
            val games = history.pastGames.filter { game -> 
                game.players.any { it.name.trim().equals(name, ignoreCase = true) } 
            }
            
            val playerScores = games.mapNotNull { game ->
                game.players.find { it.name.trim().equals(name, ignoreCase = true) }?.score
            }

            val wins = games.count { game -> 
                val winner = game.players.maxByOrNull { it.score }
                winner?.name?.trim()?.equals(name, ignoreCase = true) == true
            }
            
            val totalPoints = playerScores.sum()
            val highest = playerScores.maxOrNull() ?: 0
            val lowest = playerScores.minOrNull() ?: 0
            
            PlayerStats(
                name = name, 
                gamesPlayed = games.size, 
                wins = wins, 
                highestScore = highest, 
                lowestScore = lowest, 
                totalPoints = totalPoints
            )
        }.sortedByDescending { it.gamesPlayed } // Sort by 'veteran' status
    }

    if (friendToRemove != null) {
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title = { Text("Remove Friend") },
            text = { 
                Text("Are you sure you want to remove ${friendToRemove?.name}? This will hide them from the active list, but their game history will remain.") 
            },
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

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Friend") },
            text = {
                OutlinedTextField(
                    value = newFriendName,
                    onValueChange = { newFriendName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newFriendName.trim()
                        val alreadyExists = settings.savedPlayerNames.any { it.equals(trimmed, ignoreCase = true) }
                        
                        if (trimmed.isNotEmpty() && !alreadyExists) {
                            onUpdateSettings { it.copy(savedPlayerNames = it.savedPlayerNames + trimmed) }
                            newFriendName = ""
                            showAddDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (friendToRename != null) {
        AlertDialog(
            onDismissRequest = { friendToRename = null },
            title = { Text("Rename Player") },
            text = {
                Column {
                    Text("This will update their name in all past records too.", 
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp))
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = { renameValue = it },
                        label = { Text("New Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = renameValue.trim()
                        if (trimmed.isNotEmpty() && trimmed != friendToRename?.name) {
                            onRename(friendToRename!!.name, trimmed)
                            friendToRename = null
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { friendToRename = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Friend") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Handle insets manually if needed
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (playerStats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No friends saved yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Add players to track their stats",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(playerStats) { stat ->
                    FriendStatCard(
                        stat = stat,
                        onRemove = { friendToRemove = stat },
                        onRename = { 
                            friendToRename = stat
                            renameValue = stat.name
                        }
                    )
                }
                // Spacer for FAB
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun FriendStatCard(
    stat: PlayerStats,
    onRemove: () -> Unit,
    onRename: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                stat.name.firstOrNull()?.toString() ?: "?",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stat.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                
                IconButton(onClick = onRename) {
                    Icon(Icons.Default.Edit, "Rename", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            
            HorizontalDivider(    
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.EmojiEvents,
                    label = "Wins",
                    value = "${stat.wins}",
                    tint = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Played",
                    value = "${stat.gamesPlayed}",
                    tint = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    icon = Icons.Default.Person,
                    label = "Total",
                    value = "${stat.totalPoints}",
                    tint = if (stat.totalPoints < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${stat.highestScore}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.highestScore < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text("Highest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${stat.lowestScore}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (stat.lowestScore < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Text("Lowest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = tint)
            Spacer(Modifier.width(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
