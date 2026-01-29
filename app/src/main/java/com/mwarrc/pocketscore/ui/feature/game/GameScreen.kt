package com.mwarrc.pocketscore.ui.feature.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.components.HelpDialog
import com.mwarrc.pocketscore.ui.feature.game.components.ActivePlayerCard
import com.mwarrc.pocketscore.ui.feature.game.components.BottomNavItem
import com.mwarrc.pocketscore.ui.feature.game.components.GameHistoryDialog
import com.mwarrc.pocketscore.ui.feature.game.components.PassivePlayerCard
import com.mwarrc.pocketscore.ui.feature.game.components.QuickCalculatorSheet
import com.mwarrc.pocketscore.ui.feature.game.components.QuickSettingsSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    players: List<Player>,
    currentPlayerId: String?,
    settings: AppSettings,
    globalEvents: List<GameEvent>,
    canUndo: Boolean,
    onUpdateScore: (String, Int) -> Unit,
    onGlobalUndo: () -> Unit,
    onReset: () -> Unit,
    onToggleLayout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onTogglePlayerActive: (String, Boolean) -> Unit,
    onSetCurrentPlayer: (String) -> Unit,
    onNextTurn: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showManagePlayers by remember { mutableStateOf(false) }
    var showUndoConfirmation by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val activePlayers = remember(players) { players.filter { it.isActive } }
    val leaderId = remember(activePlayers, settings.leaderSpotlightEnabled) {
        if (settings.leaderSpotlightEnabled && activePlayers.size > 1) {
            activePlayers.filter { it.score > 0 }.maxByOrNull { it.score }?.id
        } else {
            null
        }
    }

    var localHeaderSelection by remember { mutableStateOf<String?>(null) }

    val selectionForHeader = remember(localHeaderSelection, currentPlayerId, activePlayers) {
        val selected = activePlayers.find { it.id == localHeaderSelection }
        val current = activePlayers.find { it.id == currentPlayerId }
        selected ?: current ?: activePlayers.firstOrNull()
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("End Session?") },
            text = {
                Text(
                    "Are you done playing? You can end and archive this session now, or keep it open and continue playing later."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReset()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("End & Archive") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Keep Playing") }
            }
        )
    }

    if (showQuickSettings) {
        QuickSettingsSheet(
            settings = settings,
            onUpdateSettings = onUpdateSettings,
            onDismiss = { showQuickSettings = false }
        )
    }

    val lastScoreEvent = remember(globalEvents, canUndo) {
        if (canUndo) globalEvents.lastOrNull { it.type == GameEventType.SCORE } else null
    }

    if (showUndoConfirmation && lastScoreEvent != null) {
        val previousPlayerName = remember(lastScoreEvent.previousPlayerId, players) {
            players.find { it.id == lastScoreEvent.previousPlayerId }?.name ?: "Unknown"
        }

        AlertDialog(
            onDismissRequest = { showUndoConfirmation = false },
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
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                "${lastScoreEvent.playerName} scored ${lastScoreEvent.points} points",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                "NEW STATE:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
                    onClick = {
                        onGlobalUndo()
                        showUndoConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Revert Now") }
            },
            dismissButton = {
                TextButton(onClick = { showUndoConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    if (showHistoryDialog) {
        GameHistoryDialog(
            events = globalEvents,
            onDismiss = { showHistoryDialog = false }
        )
    }

    if (showCalculator) {
        QuickCalculatorSheet(onDismiss = { showCalculator = false })
    }

    if (showHelp) {
        HelpDialog(onDismiss = { showHelp = false })
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Scoreboard",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            Icons.Default.Close,
                            "End Session",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showUndoConfirmation = true }, enabled = canUndo) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            "Undo Last",
                            tint = if (canUndo) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                },
                windowInsets = WindowInsets(top = 32.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (settings.showHelpInNavBar) {
                        BottomNavItem(
                            icon = Icons.AutoMirrored.Filled.Help,
                            label = "Help",
                            onClick = { showHelp = true }
                        )
                    }
                    BottomNavItem(
                        icon = Icons.Default.History,
                        label = "History",
                        onClick = { showHistoryDialog = true }
                    )
                    BottomNavItem(
                        icon = Icons.Default.Calculate,
                        label = "Math",
                        onClick = { showCalculator = true }
                    )
                    BottomNavItem(
                        icon = Icons.Outlined.Tune,
                        label = "Settings",
                        onClick = { showQuickSettings = true }
                    )
                    BottomNavItem(
                        icon = Icons.Outlined.Group,
                        label = "Players",
                        onClick = { showManagePlayers = true }
                    )
                }
            }
        }
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)

        Column(modifier = contentModifier) {
            if (!settings.strictTurnMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Strict Mode is OFF. Anyone can edit any score.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (settings.defaultLayout == ScoreboardLayout.GRID) {
                if (selectionForHeader != null) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        ActivePlayerCard(
                            player = selectionForHeader,
                            isLeader = selectionForHeader.id == leaderId,
                            isCurrentTurn = selectionForHeader.id == currentPlayerId,
                            isStrictTurnMode = settings.strictTurnMode,
                            onAdd = { pts ->
                                if (settings.hapticFeedbackEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onUpdateScore(selectionForHeader.id, pts)
                            },
                            onSubtract = { pts ->
                                if (settings.hapticFeedbackEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onUpdateScore(selectionForHeader.id, -pts)
                            }
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activePlayers, key = { it.id }) { player ->
                        val isLeader = player.id == leaderId
                        val isSelected = player.id == selectionForHeader?.id
                        val isActualTurn = player.id == currentPlayerId

                        PassivePlayerCard(
                            player = player,
                            isLeader = isLeader,
                            isCurrent = isSelected,
                            isActualTurn = isActualTurn,
                            onClick = {
                                localHeaderSelection = player.id
                                if (!settings.strictTurnMode) {
                                    onSetCurrentPlayer(player.id)
                                }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(activePlayers, key = { it.id }) { player ->
                        val isLeader = player.id == leaderId
                        val isTurn = player.id == currentPlayerId

                        ActivePlayerCard(
                            player = player,
                            isLeader = isLeader,
                            isCurrentTurn = isTurn,
                            isStrictTurnMode = settings.strictTurnMode,
                            onAdd = { pts ->
                                if (settings.hapticFeedbackEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onUpdateScore(player.id, pts)
                            },
                            onSubtract = { pts ->
                                if (settings.hapticFeedbackEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onUpdateScore(player.id, -pts)
                            },
                            onSetTurn = { onSetCurrentPlayer(player.id) }
                        )
                    }
                }
            }
        }
    }

    if (showManagePlayers) {
        val currentTurnPlayer = players.find { it.id == currentPlayerId }
        ModalBottomSheet(onDismissRequest = { showManagePlayers = false }) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Manage Players",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    if (settings.strictTurnMode) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Strict Mode: Fixed Roster",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (settings.strictTurnMode) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Tip: In Strict Mode, you can add players back, but you cannot disable them. Record a zero to pass a turn instead.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                } else if (currentTurnPlayer != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Current Turn: ${currentTurnPlayer.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(24.dp))

                players.forEach { player ->
                    val isCurrentTurnPlayer = player.id == currentPlayerId
                    val canToggle = !settings.strictTurnMode || !player.isActive

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .alpha(if (canToggle) 1f else 0.6f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                player.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isCurrentTurnPlayer) {
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                } else {
                                    androidx.compose.ui.text.font.FontWeight.Normal
                                }
                            )
                            if (isCurrentTurnPlayer) {
                                Text(
                                    "Is Playing Now",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        val currentActiveCount = players.count { it.isActive }
                        Switch(
                            checked = player.isActive,
                            onCheckedChange = { onTogglePlayerActive(player.id, it) },
                            enabled = canToggle && (player.isActive && currentActiveCount > 2 || !player.isActive)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showManagePlayers = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Close, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Close Menu")
                }
            }
        }
    }
}

