package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import androidx.compose.animation.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Friends/Players management tab in the history screen.
 * 
 * Features:
 * - List of all saved players with their statistics
 * - Add new players to the roster
 * - Rename players globally across all records
 * - Remove players from active roster (preserves history)
 * - Toggle leaderboard visibility per player
 * - Empty state with helpful messaging
 * 
 * Players are sorted by games played (most active first).
 * 
 * @param settings App settings containing saved player names and hidden players
 * @param history Complete game history for statistics calculation
 * @param onUpdateSettings Callback to update app settings
 * @param onRename Callback to rename a player globally (oldName, newName)
 */
@Composable
fun FriendsTab(
    settings: AppSettings,
    history: GameHistory,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onRename: (String, String) -> Unit
) {
    // Selection state management
    var selectionMode by remember { mutableStateOf(false) }
    var selectedNames by remember { mutableStateOf(setOf<String>()) }

    // Dialog state management
    var showAddDialog by remember { mutableStateOf(false) }
    var showMultiRemoveDialog by remember { mutableStateOf(false) }
    var friendToRemove by remember { mutableStateOf<PlayerStats?>(null) }
    var friendToRename by remember { mutableStateOf<PlayerStats?>(null) }

    // Reset selection when exiting selection mode
    LaunchedEffect(selectionMode) {
        if (!selectionMode) selectedNames = emptySet()
    }

    // Calculate player statistics from history
    val playerStats = remember(history, settings.savedPlayerNames, settings.hiddenPlayers, settings.deactivatedPlayers) {
        PlayerStats.calculateAll(
            playerNames = settings.savedPlayerNames,
            history = history,
            hiddenPlayers = settings.hiddenPlayers,
            deactivatedPlayers = settings.deactivatedPlayers
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (playerStats.isEmpty()) {
            EmptyRosterState()
        } else {
            PlayerList(
                playerStats = playerStats,
                padding = PaddingValues(0.dp), // Padding handled internally via contentPadding
                selectionMode = selectionMode,
                selectedNames = selectedNames,
                onToggleSelection = { name ->
                    selectedNames = if (name in selectedNames) {
                        selectedNames - name
                    } else {
                        selectedNames + name
                    }
                    if (selectedNames.isEmpty()) selectionMode = false
                },
                onEnterSelectionMode = { name ->
                    selectionMode = true
                    selectedNames = setOf(name)
                },
                onToggleLeaderboard = { stat ->
                    onUpdateSettings { s ->
                        val newHidden = if (stat.isHiddenInLeaderboard) {
                            s.hiddenPlayers - stat.name
                        } else {
                            s.hiddenPlayers + stat.name
                        }
                        s.copy(hiddenPlayers = newHidden)
                    }
                },
                onToggleDeactivated = { stat ->
                    onUpdateSettings { s ->
                        val newDeactivated = if (stat.isDeactivated) {
                            s.deactivatedPlayers - stat.name
                        } else {
                            s.deactivatedPlayers + stat.name
                        }
                        s.copy(deactivatedPlayers = newDeactivated)
                    }
                },
                onRemove = { friendToRemove = it },
                onRename = { friendToRename = it }
            )
        }

        // ── Refresh Floating Actions ---

        // Add Player FAB
        androidx.compose.animation.AnimatedVisibility(
            visible = !selectionMode,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
        ) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Player", modifier = Modifier.size(24.dp))
            }
        }

        // Selection Actions Bar
        androidx.compose.animation.AnimatedVisibility(
            visible = selectionMode,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val anyShownInRanks = selectedNames.any { it !in settings.hiddenPlayers }
                    val anyShownOnHome = selectedNames.any { it !in settings.deactivatedPlayers }

                    Row {
                        IconButton(onClick = {
                            onUpdateSettings { s ->
                                val newHidden = if (anyShownInRanks) {
                                    (s.hiddenPlayers + selectedNames).distinct()
                                } else {
                                    s.hiddenPlayers - selectedNames
                                }
                                s.copy(hiddenPlayers = newHidden)
                            }
                            selectionMode = false
                        }) {
                            Icon(
                                if (anyShownInRanks) Icons.Default.VisibilityOff else Icons.Default.Visibility, 
                                if (anyShownInRanks) "Hide from Ranks" else "Show in Ranks"
                            )
                        }
                        IconButton(onClick = {
                            onUpdateSettings { s ->
                                val newDeactivated = if (anyShownOnHome) {
                                    (s.deactivatedPlayers + selectedNames).distinct()
                                } else {
                                    s.deactivatedPlayers - selectedNames
                                }
                                s.copy(deactivatedPlayers = newDeactivated)
                            }
                            selectionMode = false
                        }) {
                            Icon(
                                if (anyShownOnHome) Icons.Default.HomeWork else Icons.Default.Home, 
                                if (anyShownOnHome) "Hide from Home" else "Show on Home"
                            )
                        }
                        IconButton(onClick = { selectionMode = false }) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    }

                    Text(
                        "${selectedNames.size} Selected",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { showMultiRemoveDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Remove")
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        FriendsTabAddPlayerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newName ->
                onUpdateSettings { 
                    it.copy(savedPlayerNames = it.savedPlayerNames + newName) 
                }
                showAddDialog = false
            },
            existingNames = settings.savedPlayerNames
        )
    }

    if (showMultiRemoveDialog) {
        FriendsTabRemoveMultiplePlayersDialog(
            count = selectedNames.size,
            onDismiss = { showMultiRemoveDialog = false },
            onConfirm = {
                onUpdateSettings { s ->
                    s.copy(savedPlayerNames = s.savedPlayerNames.filter { it !in selectedNames })
                }
                showMultiRemoveDialog = false
                selectionMode = false
            }
        )
    }

    friendToRemove?.let { stat ->
        FriendsTabRemovePlayerDialog(
            playerName = stat.name,
            onDismiss = { friendToRemove = null },
            onConfirm = {
                onUpdateSettings { s ->
                    s.copy(savedPlayerNames = s.savedPlayerNames.filter { it != stat.name })
                }
                friendToRemove = null
            }
        )
    }

    friendToRename?.let { stat ->
        FriendsTabRenamePlayerDialog(
            currentName = stat.name,
            onDismiss = { friendToRename = null },
            onConfirm = { newName ->
                onRename(stat.name, newName)
                friendToRename = null
            },
            existingNames = settings.savedPlayerNames
        )
    }
}

/**
 * Dialog for adding a new player to the roster.
 */
@Composable
private fun FriendsTabAddPlayerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    existingNames: List<String>
) {
    var newFriendName by remember { mutableStateOf("") }
    
    val trimmed = newFriendName.trim()
    val alreadyExists = existingNames.any { it.equals(trimmed, ignoreCase = true) }
    val isValid = trimmed.isNotEmpty() && !alreadyExists

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Player") },
        text = {
            Column {
                OutlinedTextField(
                    value = newFriendName,
                    onValueChange = { newValue -> 
                        val filtered = newValue.filter { it.isLetterOrDigit() }
                        if (filtered.length <= 14) {
                            newFriendName = filtered
                        }
                    },
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
                        onConfirm(trimmed)
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text("Add to Roster") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}

/**
 * Dialog for renaming a player globally across all records.
 */
@Composable
private fun FriendsTabRenamePlayerDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    existingNames: List<String>
) {
    var renameValue by remember { mutableStateOf(currentName) }
    
    val trimmed = renameValue.trim()
    val isTaken = existingNames.any { 
        it.trim().equals(trimmed, ignoreCase = true) && 
        !it.trim().equals(currentName.trim(), ignoreCase = true) 
    }
    val isValid = !isTaken && trimmed.isNotEmpty() && trimmed != currentName
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                Icons.Default.Warning, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            ) 
        },
        title = { Text("Rename Player") },
        text = {
            Column {
                // Warning message
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        "Names are unique IDs. Renaming will update all past match " +
                        "records to match this new name.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                // Input field
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isLetterOrDigit() }
                        if (filtered.length <= 14) {
                            renameValue = filtered
                        }
                    },
                    label = { Text("Update Name") },
                    singleLine = true,
                    isError = isTaken,
                    supportingText = if (isTaken) { 
                        { Text("Name already taken") } 
                    } else null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(trimmed)
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text("Update Records") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}

/**
 * Confirmation dialog for removing a player from the roster.
 */
@Composable
private fun FriendsTabRemovePlayerDialog(
    playerName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                Icons.Default.Delete, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            ) 
        },
        title = { Text("Remove from Roster?") },
        text = { 
            Text(
                "Removing $playerName will hide them from the 'Active Roster' picker, " +
                "but their match history will still be safe."
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { 
                Text("Remove") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}

/**
 * Confirmation dialog for bulk removing multiple players.
 */
@Composable
private fun FriendsTabRemoveMultiplePlayersDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                Icons.Default.DeleteForever, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            ) 
        },
        title = { Text("Remove $count Players?") },
        text = { 
            Text(
                "Are you sure you want to remove these $count players from the active roster? " +
                "Their match history and statistics will be preserved."
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { 
                Text("Remove All") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Keep Them") 
            }
        }
    )
}

@Composable
private fun EmptyRosterState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 80.dp) // Offset from center to account for FAB space
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Your Roster is Empty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Add friends to track their individual stats across games.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Scrollable list of player stat cards.
 */
@Composable
private fun PlayerList(
    playerStats: List<PlayerStats>,
    padding: PaddingValues,
    selectionMode: Boolean = false,
    selectedNames: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
    onEnterSelectionMode: (String) -> Unit = {},
    onToggleLeaderboard: (PlayerStats) -> Unit,
    onToggleDeactivated: (PlayerStats) -> Unit,
    onRemove: (PlayerStats) -> Unit,
    onRename: (PlayerStats) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(top = 12.dp, start = 4.dp, end = 4.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(playerStats, key = { it.name }) { stat ->
            PlayerStatCard(
                stat = stat,
                selectionMode = selectionMode,
                isSelected = stat.name in selectedNames,
                onToggleSelection = { onToggleSelection(stat.name) },
                onEnterSelectionMode = { onEnterSelectionMode(stat.name) },
                onToggleLeaderboard = { onToggleLeaderboard(stat) },
                onToggleDeactivated = { onToggleDeactivated(stat) },
                onRemove = { onRemove(stat) },
                onRename = { onRename(stat) }
            )
        }
    }
}
