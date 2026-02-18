package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.domain.model.RosterSortOption
import androidx.compose.material3.surfaceColorAtElevation

/**
 * Dialog shown when the user wants to end or reset the current game session.
 * 
 * Offers options to:
 * 1. Finish & Archive: Save to history and return home.
 * 2. Save & Play Again: Save current match and start a new one with the same roster.
 * 3. Pause & Resume Later: Keep state and return home.
 * 
 * Handles Guest Mode restrictions by warning the user if records won't be saved.
 * 
 * @param settings Current application settings
 * @param onDismiss Callback to close the dialog
 * @param onFinishAndArchive Callback to end session and save (with override flag)
 * @param onRestartMatch Callback to archive and start fresh with same players
 * @param onResumeLater Callback to return home without finalizing
 * @param onModifyRoster Callback to change players before restart
 */
@Composable
fun ResetGameDialog(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onFinishAndArchive: (Boolean) -> Unit,
    onRestartMatch: (Boolean) -> Unit,
    onResumeLater: () -> Unit,
    onModifyRoster: () -> Unit
) {
    // Only relevant if Guest Mode is ON and automatic saving is OFF
    val isGuestRestricted = settings.isGuestSession && !settings.guestSaveRecords
    var overrideGuest by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isGuestRestricted) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isGuestRestricted) Icons.Default.Groups else Icons.Default.CheckCircle,
                        null,
                        modifier = Modifier.size(28.dp),
                        tint = if (isGuestRestricted) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        title = {
            Text(
                if (isGuestRestricted) "End Guest Session?" else "End This Match?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (isGuestRestricted) 
                        "This session will not be saved to your History unless you log it now." 
                    else 
                        "This match is over. What's the next move?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isGuestRestricted) {
                    Surface(
                        onClick = { overrideGuest = !overrideGuest },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (overrideGuest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp).fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = overrideGuest,
                                onCheckedChange = { overrideGuest = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Log to History",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Override Guest Mode for this match",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Finish & Archive (PRIMARY ACTION)
                Surface(
                    onClick = { onFinishAndArchive(overrideGuest) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isGuestRestricted && !overrideGuest) Icons.Default.Close else Icons.Default.Lock,
                                    null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isGuestRestricted && !overrideGuest) "Discard Session" else "Finish & Archive",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                if (isGuestRestricted && !overrideGuest) "Close without logging" else "Lock scores and return home",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Save & Play Again (Secondary Action)
                Surface(
                    onClick = { onRestartMatch(overrideGuest) },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, if (overrideGuest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Refresh,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isGuestRestricted && !overrideGuest) "Restart Guest Session" else "Save & Play Again",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                "Start next round immediately",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                // Resume Later (Prominent secondary action)
                Surface(
                    onClick = onResumeLater,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.History,
                                    null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pause & Resume Later",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Keep Progress for another time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

/**
 * General exit confirmation dialog for hardware back press or home button.
 * 
 * @param onDismiss Callback to stay in the app
 * @param onConfirm Callback to exit the app
 */
@Composable
fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Close,
                null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Close App?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "Your game progress will be saved and you can resume later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Stay")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Confirmation dialog for the Undo action.
 * 
 * Shows details about what action is being reverted and who the turn will return to.
 * 
 * @param lastScoreEvent The event that will be undone
 * @param players Current list of players
 * @param onDismiss Callback to cancel undo
 * @param onConfirm Callback to proceed with undo
 */
@Composable
fun UndoConfirmationDialog(
    lastScoreEvent: GameEvent?,
    players: List<Player>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (lastScoreEvent == null) {
        onDismiss()
        return
    }

    val previousPlayerName = remember(lastScoreEvent.previousPlayerId, players) {
        players.find { it.id == lastScoreEvent.previousPlayerId }?.name ?: "Unknown"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text("Confirm Undo")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Are you sure you want to revert the last action?")
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "ACTION TO REVERT:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${lastScoreEvent.playerName} scored ${lastScoreEvent.points} points",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            "NEW STATE:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "• Turn will go back to: $previousPlayerName",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "• ${lastScoreEvent.playerName}'s score will decrease by ${lastScoreEvent.points}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Text(
                    "This is final. You can only undo the very last move.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Revert Now") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Expressive dialog for starting a new match immediately.
 * 
 * Allows re-selecting players from the current match (sorted appropriately)
 * or adding existing players from the library / guest pool.
 * 
 * @param currentPlayers Players from the just-finished match
 * @param settings Current application settings
 * @param onDismiss Callback to cancel
 * @param onStartGame Callback with the IDs of the players for the next match
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickRestartDialog(
    currentPlayers: List<Player>,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onStartGame: (List<String>) -> Unit
) {
    var selectedNames by remember { 
        mutableStateOf(currentPlayers.filter { it.isActive }.map { it.name }.toSet()) 
    }
    var searchInput by remember { mutableStateOf("") }
    
    val otherSavedNames = remember(settings.savedPlayerNames, currentPlayers) {
        settings.savedPlayerNames.filter { name -> 
            currentPlayers.none { it.name.trim().equals(name.trim(), ignoreCase = true) } 
        }
    }

    val filteredSavedNames = remember(searchInput, otherSavedNames) {
        if (searchInput.isBlank()) otherSavedNames
        else otherSavedNames.filter { it.contains(searchInput, ignoreCase = true) }
    }
    
    var sortOption by remember { mutableStateOf(RosterSortOption.LOSERS_FIRST) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    val sortedCurrentMatchPlayers = remember(currentPlayers, sortOption) {
        when (sortOption) {
            RosterSortOption.LOSERS_FIRST -> currentPlayers.sortedBy { it.score }
            RosterSortOption.WINNERS_FIRST -> currentPlayers.sortedByDescending { it.score }
            RosterSortOption.RANDOM -> currentPlayers.shuffled()
            else -> currentPlayers
        }
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 24.dp),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Next Session",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "Select players for the next round",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Refresh, 
                                null, 
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Search & Quick Add
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = { Text("Search or add guest...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    leadingIcon = { 
                        Icon(
                            Icons.Default.PersonAdd, 
                            null, 
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    trailingIcon = {
                        if (searchInput.isNotBlank() && !selectedNames.contains(searchInput.trim())) {
                            IconButton(
                                onClick = {
                                    selectedNames = selectedNames + searchInput.trim()
                                    searchInput = ""
                                },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add, 
                                    null, 
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                )

                // Selection Summary / Ribbon Cards Group 1: Recent Match
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle, 
                            null, 
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "RECENT MATCH",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        
                        // Dropdown Sort Menu
                        Spacer(Modifier.width(8.dp))
                        Box {
                            FilterChip(
                                selected = true,
                                onClick = { showSortMenu = true },
                                label = { 
                                    Text(
                                        when (sortOption) {
                                            RosterSortOption.LOSERS_FIRST -> "Losers Start"
                                            RosterSortOption.WINNERS_FIRST -> "Winners Start"
                                            RosterSortOption.RANDOM -> "Random"
                                            else -> "Default"
                                        }, 
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black
                                    ) 
                                },
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
                                },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    labelColor = MaterialTheme.colorScheme.primary,
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = true,
                                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.height(30.dp)
                            )

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                SortMenuItem(
                                    text = "Losers Start",
                                    icon = Icons.Default.History,
                                    selected = sortOption == RosterSortOption.LOSERS_FIRST,
                                    onClick = {
                                        sortOption = RosterSortOption.LOSERS_FIRST
                                        showSortMenu = false
                                    }
                                )
                                SortMenuItem(
                                    text = "Winners Start",
                                    icon = Icons.Default.EmojiEvents,
                                    selected = sortOption == RosterSortOption.WINNERS_FIRST,
                                    onClick = {
                                        sortOption = RosterSortOption.WINNERS_FIRST
                                        showSortMenu = false
                                    }
                                )
                                SortMenuItem(
                                    text = "Random Shuffle",
                                    icon = Icons.Default.Shuffle,
                                    selected = sortOption == RosterSortOption.RANDOM,
                                    onClick = {
                                        sortOption = RosterSortOption.RANDOM
                                        showSortMenu = false
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))
                        val activeMatchCount = sortedCurrentMatchPlayers.count { it.name in selectedNames }
                        if (activeMatchCount > 0) {
                            Text(
                                "$activeMatchCount selected",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        sortedCurrentMatchPlayers.forEach { player ->
                            RosterRibbonCard(
                                name = player.name,
                                score = player.score,
                                isSelected = player.name in selectedNames,
                                onClick = {
                                    selectedNames = if (player.name in selectedNames) {
                                        selectedNames - player.name
                                    } else {
                                        selectedNames + player.name
                                    }
                                }
                            )
                        }
                    }
                }

                // Ribbon Cards Group 2: Saved Library
                if (filteredSavedNames.isNotEmpty() || selectedNames.any { name -> currentPlayers.none { it.name == name } }) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.History, 
                                null, 
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "SAVED LIBRARY",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // First show names that were added as guests or selected from saved
                            val extraSelections = selectedNames.filter { name -> currentPlayers.none { it.name == name } }
                            extraSelections.forEach { name ->
                                RosterRibbonCard(
                                    name = name,
                                    isSelected = true,
                                    onClick = { selectedNames = selectedNames - name }
                                )
                            }
                            
                            // Then show other saved names that are not selected
                            filteredSavedNames.filter { it !in selectedNames }.forEach { name ->
                                RosterRibbonCard(
                                    name = name,
                                    isSelected = false,
                                    onClick = { selectedNames = selectedNames + name }
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedNames.size >= 2) {
                        // Preserve order: first match players in their relative order, then extras
                        val finalRoster = (currentPlayers.map { it.name } + selectedNames.toList())
                            .distinct()
                            .filter { it in selectedNames }
                            
                        onStartGame(finalRoster)
                    }
                },
                enabled = selectedNames.size >= 2,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.6.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    "Archive & Start New Round", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            Box(modifier = Modifier.padding(top = 8.dp)) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Cancel", 
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        },
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * Shared menu item for sort selections
 */
@Composable
private fun SortMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { 
            Text(
                text, 
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            ) 
        },
        leadingIcon = { 
            Icon(
                icon, 
                null, 
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            ) 
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Premium Ribbon-style card for roster selection
 */
@Composable
private fun RosterRibbonCard(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    score: Int? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        label = "color"
    )
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Player Initial / Selection Icon
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            name.take(1).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Column {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (score != null) {
                    Text(
                        "$score pts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerRosterItem(
    player: Player,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isSelected) {
                   Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(22.dp))
                } else {
                   Text(
                       player.name.take(1).uppercase(),
                       style = MaterialTheme.typography.titleMedium,
                       fontWeight = FontWeight.ExtraBold
                   )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                player.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                color = if (player.score >= 0) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    " ${player.score} pts ",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (player.score >= 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Black
                )
            }
        }
        
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
