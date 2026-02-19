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

    Scaffold(
        floatingActionButton = {
            if (!selectionMode) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("New Player") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        bottomBar = {
            if (selectionMode) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actions = {
                        // Hide from Leaderboard
                        IconButton(onClick = {
                            onUpdateSettings { s ->
                                val allHidden = (s.hiddenPlayers + selectedNames).distinct()
                                s.copy(hiddenPlayers = allHidden)
                            }
                            selectionMode = false
                        }) {
                            Icon(Icons.Default.VisibilityOff, "Hide from Ranks")
                        }
                        
                        // Hide from Home Screen (Deactivate)
                        IconButton(onClick = {
                            onUpdateSettings { s ->
                                val allDeactivated = (s.deactivatedPlayers + selectedNames).distinct()
                                s.copy(hiddenPlayers = (s.hiddenPlayers + selectedNames).distinct(), deactivatedPlayers = allDeactivated)
                            }
                            selectionMode = false
                        }) {
                            Icon(Icons.Default.HomeWork, "Hide from Home")
                        }
                    },
                    floatingActionButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Delete Mass Action
                            FloatingActionButton(
                                onClick = {
                                    onUpdateSettings { s ->
                                        s.copy(savedPlayerNames = s.savedPlayerNames.filter { it !in selectedNames })
                                    }
                                    selectionMode = false
                                },
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Icon(Icons.Default.Delete, "Delete Selected")
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (playerStats.isEmpty()) {
            EmptyRosterState(modifier = Modifier.padding(padding))
        } else {
            PlayerList(
                playerStats = playerStats,
                padding = padding,
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
    }

    // Dialogs
    if (showAddDialog) {
        AddPlayerDialog(
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

    friendToRemove?.let { stat ->
        RemovePlayerDialog(
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
        RenamePlayerDialog(
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
 * Empty state displayed when no players are in the roster.
 */
@Composable
private fun EmptyRosterState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 80.dp)
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Your Roster is Empty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                "Add friends to track their individual stats.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
        contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
