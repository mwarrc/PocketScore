package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory

/**
 * Friends / Players management tab in History screen.
 * Redesigned to match the Material 3 specification.
 *
 * Features:
 * - Meta stats summary header (# players, # on leaderboard, # in Home picker)
 * - Collapsible "What each control does" Guide card with "Got it" action
 * - Interactive player cards with inline switches and quick-action chips
 * - Multi-selection mode with bulk visibility and removal controls
 * - Floating "Add Player" FAB positioned safely above the navigation dock
 * - Material 3 styled Add, Rename, and Remove dialogs
 *
 * @param settings Current application settings.
 * @param history Complete game history for calculating player stats.
 * @param onUpdateSettings Callback to update application settings.
 * @param onRename Callback to rename a player globally across all games.
 * @param onSelectionModeChange Callback when player selection mode enters/exits.
 */
@Composable
fun FriendsTab(
    settings: AppSettings,
    history: GameHistory,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onRename: (String, String) -> Unit,
    onSelectionModeChange: (Boolean) -> Unit = {}
) {
    // Selection state management
    var selectionMode by remember { mutableStateOf(false) }
    var selectedNames by remember { mutableStateOf(setOf<String>()) }

    // Dialog state management
    var showAddDialog by remember { mutableStateOf(false) }
    var showMultiRemoveDialog by remember { mutableStateOf(false) }
    var friendToRemove by remember { mutableStateOf<PlayerStats?>(null) }
    var friendToRename by remember { mutableStateOf<PlayerStats?>(null) }

    // Sync selection state with parent HistoryScreen
    LaunchedEffect(selectionMode) {
        if (!selectionMode) selectedNames = emptySet()
        onSelectionModeChange(selectionMode)
    }

    // Calculate player statistics from history
    val playerStats = remember(history, settings.savedPlayerNames, settings.hiddenPlayers, settings.deactivatedPlayers) {
        PlayerStats.calculateAll(
            playerNames = settings.savedPlayerNames,
            history = history,
            hiddenPlayers = settings.hiddenPlayers,
            deactivatedPlayers = settings.deactivatedPlayers
        ).sortedByDescending { it.gamesPlayed }
    }

    val onLeaderboardCount = playerStats.count { !it.isHiddenInLeaderboard }
    val inHomeCount = playerStats.count { !it.isDeactivated }

    Box(modifier = Modifier.fillMaxSize()) {
        if (playerStats.isEmpty()) {
            EmptyRosterState(onAddPlayer = { showAddDialog = true })
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Top Header Bar ──
                if (selectionMode) {
                    // Selection Mode Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { selectionMode = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                                }
                                Text(
                                    text = "${selectedNames.size} selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = {
                                    selectedNames = if (selectedNames.size == playerStats.size) {
                                        emptySet()
                                    } else {
                                        playerStats.map { it.name }.toSet()
                                    }
                                    if (selectedNames.isEmpty()) selectionMode = false
                                }
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (selectedNames.size == playerStats.size) "Deselect all" else "Select all",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Normal Stats Summary Header
                    Text(
                        text = "${playerStats.size} players • $onLeaderboardCount on leaderboard • $inHomeCount in Home picker",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // ── Player List ──
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Player cards
                    items(playerStats, key = { it.name }) { stat ->
                        PlayerStatCard(
                            stat = stat,
                            selectionMode = selectionMode,
                            isSelected = stat.name in selectedNames,
                            onToggleSelection = {
                                selectedNames = if (stat.name in selectedNames) {
                                    selectedNames - stat.name
                                } else {
                                    selectedNames + stat.name
                                }
                                if (selectedNames.isEmpty()) selectionMode = false
                            },
                            onEnterSelectionMode = {
                                selectionMode = true
                                selectedNames = setOf(stat.name)
                            },
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
                            onToggleDeactivated = {
                                onUpdateSettings { s ->
                                    val newDeactivated = if (stat.isDeactivated) {
                                        s.deactivatedPlayers - stat.name
                                    } else {
                                        s.deactivatedPlayers + stat.name
                                    }
                                    s.copy(deactivatedPlayers = newDeactivated)
                                }
                            },
                            onRemove = { friendToRemove = stat },
                            onRename = { friendToRename = stat }
                        )
                    }
                }
            }
        }

        // ── Extended Floating Action Button (Add Player) ──
        // Positioned safely above the floating navigation bar dock
        AnimatedVisibility(
            visible = !selectionMode && playerStats.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) + scaleIn(
                initialScale = 0.88f,
                animationSpec = tween(220, easing = LinearOutSlowInEasing)
            ),
            exit = fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing)) + scaleOut(
                targetScale = 0.88f,
                animationSpec = tween(160, easing = FastOutLinearInEasing)
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 89.dp, end = 20.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp
                ),
                modifier = Modifier.graphicsLayer {
                    this.shadowElevation = 6.dp.toPx()
                    this.shape = RoundedCornerShape(18.dp)
                    this.clip = false
                    this.ambientShadowColor = Color.Black.copy(alpha = 0.20f)
                    this.spotShadowColor = Color.Black.copy(alpha = 0.30f)
                },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text("Add player", fontWeight = FontWeight.Bold) }
            )
        }

        // ── Floating Player Selection Action Bar ──
        // Designed to match the glassmorphic pill capsule dock aesthetic
        AnimatedVisibility(
            visible = selectionMode,
            enter = fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) + scaleIn(
                initialScale = 0.94f,
                animationSpec = tween(220, easing = LinearOutSlowInEasing)
            ),
            exit = fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing)) + scaleOut(
                targetScale = 0.94f,
                animationSpec = tween(160, easing = FastOutLinearInEasing)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 10.dp)
        ) {
            val anyShownInRanks = selectedNames.any { it !in settings.hiddenPlayers }
            val anyShownOnHome = selectedNames.any { it !in settings.deactivatedPlayers }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .graphicsLayer {
                        this.shadowElevation = 8.dp.toPx()
                        this.shape = CircleShape
                        this.clip = false
                        this.ambientShadowColor = Color.Black.copy(alpha = 0.20f)
                        this.spotShadowColor = Color.Black.copy(alpha = 0.30f)
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                tonalElevation = 3.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Selected count pill badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${selectedNames.size}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (selectedNames.size == 1) "1 player selected" else "${selectedNames.size} selected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Choose action",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Right: Action icon buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Toggle Leaderboard Ranks
                        FilledTonalIconButton(
                            onClick = {
                                onUpdateSettings { s ->
                                    val newHidden = if (anyShownInRanks) {
                                        (s.hiddenPlayers + selectedNames).distinct()
                                    } else {
                                        s.hiddenPlayers - selectedNames
                                    }
                                    s.copy(hiddenPlayers = newHidden)
                                }
                                selectionMode = false
                            },
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = if (anyShownInRanks) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (anyShownInRanks) "Hide in Ranks" else "Show in Ranks",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Toggle Home Picker
                        FilledTonalIconButton(
                            onClick = {
                                onUpdateSettings { s ->
                                    val newDeactivated = if (anyShownOnHome) {
                                        (s.deactivatedPlayers + selectedNames).distinct()
                                    } else {
                                        s.deactivatedPlayers - selectedNames
                                    }
                                    s.copy(deactivatedPlayers = newDeactivated)
                                }
                                selectionMode = false
                            },
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = if (anyShownOnHome) Icons.Default.HomeWork else Icons.Default.Home,
                                contentDescription = if (anyShownOnHome) "Hide on Home" else "Show on Home",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Remove selected
                        FilledIconButton(
                            onClick = { showMultiRemoveDialog = true },
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove selected",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Dialogs ──
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
 * Empty roster state matching Material 3 prototype styling.
 */
@Composable
private fun EmptyRosterState(
    onAddPlayer: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 60.dp, start = 32.dp, end = 32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Your roster is empty",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Add friends to track their individual stats across games.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onAddPlayer,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add your first player", fontWeight = FontWeight.Bold)
            }
        }
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
        title = {
            Text(
                "New player",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Letters and numbers only. New players appear on the leaderboard and in the Home picker.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = newFriendName,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isLetterOrDigit() }.take(14)
                        newFriendName = filtered
                    },
                    label = { Text("Display name") },
                    singleLine = true,
                    isError = alreadyExists,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (alreadyExists) "This name is already in your roster" else "")
                            Text("${newFriendName.length}/14")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isValid) onConfirm(trimmed) },
                enabled = isValid,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Add to roster", fontWeight = FontWeight.Bold)
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
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                "Rename player",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Names are unique IDs. Renaming updates all past match records to the new name.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isLetterOrDigit() }.take(14)
                        renameValue = filtered
                    },
                    label = { Text("Update name") },
                    singleLine = true,
                    isError = isTaken,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isTaken) "Name already taken" else "")
                            Text("${renameValue.length}/14")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (isValid) onConfirm(trimmed) },
                enabled = isValid,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Update records", fontWeight = FontWeight.Bold)
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
 * Confirmation dialog for removing a single player.
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
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                "Remove from roster?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Removing $playerName hides them from the Home player picker, but their match history stays safe.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Remove", fontWeight = FontWeight.Bold)
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
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                "Remove $count players?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "These $count players will come off your roster. Their match history and stats are kept.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Remove all", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep them")
            }
        }
    )
}
