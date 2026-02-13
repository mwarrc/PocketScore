package com.mwarrc.pocketscore.ui.feature.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.feature.game.components.ActivePlayerCard
import com.mwarrc.pocketscore.ui.feature.game.components.BottomNavItem
import com.mwarrc.pocketscore.ui.feature.game.components.GameHistoryDialog
import com.mwarrc.pocketscore.ui.feature.game.components.GameHelpSheet
import com.mwarrc.pocketscore.ui.feature.game.components.PassivePlayerCard
import com.mwarrc.pocketscore.ui.feature.game.components.QuickCalculatorSheet
import com.mwarrc.pocketscore.ui.feature.game.components.QuickSettingsSheet
import com.mwarrc.pocketscore.ui.feature.game.components.ScoreNumpad
import com.mwarrc.pocketscore.ui.feature.game.components.ResetGameDialog
import com.mwarrc.pocketscore.ui.feature.game.components.ExitConfirmationDialog
import com.mwarrc.pocketscore.ui.feature.game.components.UndoConfirmationDialog
import com.mwarrc.pocketscore.ui.feature.game.components.PoolProbabilitySheet
import com.mwarrc.pocketscore.ui.feature.game.components.StrictModeBanner
import com.mwarrc.pocketscore.ui.feature.game.components.GameScoreboard
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    players: List<Player>,
    currentPlayerId: String?,
    settings: AppSettings,
    globalEvents: List<GameEvent>,
    canUndo: Boolean,
    onUpdateScore: (String, Int) -> Unit,
    onGlobalUndo: () -> Unit,
    onReset: (Boolean) -> Unit,
    onToggleLayout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onTogglePlayerActive: (String, Boolean) -> Unit,
    onSetCurrentPlayer: (String) -> Unit,
    onNextTurn: () -> Unit,
    onRestart: (List<String>) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showManagePlayers by remember { mutableStateOf(false) }
    var showUndoConfirmation by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var showPoolProbability by remember { mutableStateOf(false) }
    var showQuickBallDialog by remember { mutableStateOf(false) }
    var calculatorExpression by remember { mutableStateOf<TextFieldValue>(TextFieldValue("")) }
    var ballsOnTable by remember { mutableStateOf((1..15).toSet()) }
    var showHelp by remember { mutableStateOf(false) }
    var showHistoryBadge by remember { mutableStateOf(false) }
    var scoreInput by remember { mutableStateOf("") }
    var showNumpad by remember { mutableStateOf(false) }
    var isNumpadPinned by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showQuickRestart by remember { mutableStateOf(false) }
    var exitConfirmed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Check if there's an active game (any player has a score)
    val hasActiveGame = remember(players) {
        players.any { it.score > 0 }
    }

    // Unified BackHandler - only intercept when we have something to handle
    BackHandler(enabled = showNumpad || (hasActiveGame && !exitConfirmed)) {
        when {
            showNumpad && isNumpadPinned -> isNumpadPinned = false // Unpin first
            showNumpad -> showNumpad = false // Then dismiss
            showPoolProbability -> showPoolProbability = false
            hasActiveGame && !exitConfirmed -> showExitConfirmation = true
        }
    }


    // Clear input when active player switches
    LaunchedEffect(currentPlayerId) {
        scoreInput = ""
    }

    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible, settings.useCustomKeyboard) {
        if (!settings.useCustomKeyboard && !isImeVisible) {
            showNumpad = false
        }
    }

    LaunchedEffect(globalEvents) {
        val lastEvent = globalEvents.lastOrNull()
        if (lastEvent?.type == GameEventType.STATUS_CHANGE &&
            (lastEvent.message?.contains("Strict Mode", ignoreCase = true) == true)
        ) {
            showHistoryBadge = true
            delay(120000) // 2 minutes timeout
            showHistoryBadge = false
        } else {
            showHistoryBadge = false
        }
    }

    val activePlayers = remember(players) { players.filter { it.isActive } }
    val leaderId = remember(activePlayers, settings.leaderSpotlightEnabled) {
        if (settings.leaderSpotlightEnabled && activePlayers.size > 1) {
            activePlayers.maxByOrNull { it.score }?.id
        } else {
            null
        }
    }

    // Hoisted State Calculations for Game Logic
    val tableSum = remember(ballsOnTable) {
        val ballValues = mapOf(1 to 16, 2 to 17, 3 to 3, 4 to 4, 5 to 5, 6 to 6, 7 to 7, 8 to 8, 9 to 9, 10 to 10, 11 to 11, 12 to 12, 13 to 13, 14 to 14, 15 to 15)
        ballsOnTable.sumOf { ballValues[it] ?: 0 }
    }
    
    val leaderScore = remember(activePlayers, leaderId) {
        activePlayers.find { it.id == leaderId }?.score ?: 0
    }

    // Auto-Skip Eliminated Players Logic
    // Ensures that if the turn advances to an eliminated player, we skip them immediately.
    LaunchedEffect(currentPlayerId, tableSum, leaderScore, settings.strictTurnMode, settings.allowEliminatedInput) {
        val currentPlayer = activePlayers.find { it.id == currentPlayerId }
        
        if (currentPlayer != null && tableSum > 0) {
            val potentialMax = currentPlayer.score + tableSum
            val isEliminated = currentPlayer.id != leaderId && potentialMax < leaderScore
            
            // Only skip if strict mode is on OR if allowed eliminated input is off
            val shouldSkip = isEliminated && (settings.strictTurnMode || !settings.allowEliminatedInput)
            
            if (shouldSkip) {
                // Find next valid player
                val currentIndex = activePlayers.indexOfFirst { it.id == currentPlayerId }
                if (currentIndex != -1) {
                    var nextIndex = (currentIndex + 1) % activePlayers.size
                    var attempts = 0
                    
                    while (attempts < activePlayers.size) {
                        val p = activePlayers[nextIndex]
                        val pPotentialMax = p.score + tableSum
                        val pIsEliminated = p.id != leaderId && pPotentialMax < leaderScore
                        
                        // Check if the potential next player should also be skipped
                        val nextShouldSkip = pIsEliminated && (settings.strictTurnMode || !settings.allowEliminatedInput)
                        
                        if (!nextShouldSkip) {
                            onSetCurrentPlayer(p.id)
                            break
                        }
                        nextIndex = (nextIndex + 1) % activePlayers.size
                        attempts++
                    }
                }
            }
        }
    }
    
    var localHeaderSelection by remember { mutableStateOf<String?>(null) }

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

    LaunchedEffect(currentPlayerId, settings.autoScrollToActivePlayer) {
        if (settings.autoScrollToActivePlayer) {
            val index = activePlayers.indexOfFirst { it.id == currentPlayerId }
            if (index != -1) {
                if (settings.defaultLayout == ScoreboardLayout.GRID) {
                    lazyGridState.animateScrollToItem(index)
                } else {
                    // Offset -150px gives "breathing room" so it doesn't snap harshly to the very top
                    lazyListState.animateScrollToItem(index, scrollOffset = -150)
                }
            }
        }
    }

    LaunchedEffect(currentPlayerId) {
        localHeaderSelection = null
        scoreInput = ""
        // Numpad now stays open across player switches for faster scoring.
    }

    val selectionForHeader = remember(localHeaderSelection, currentPlayerId, activePlayers) {
        val selected = activePlayers.find { it.id == localHeaderSelection }
        val current = activePlayers.find { it.id == currentPlayerId }
        selected ?: current ?: activePlayers.firstOrNull()
    }

    // Dialogs - Using modular components
    if (showResetDialog) {
        ResetGameDialog(
            onDismiss = { showResetDialog = false },
            onFinishAndArchive = {
                onReset(true)
                showResetDialog = false
            },
            onResumeLater = {
                onReset(false)
                showResetDialog = false
            },
            onRestartMatch = {
                showResetDialog = false
                showQuickRestart = true
            },
            onModifyRoster = {
                showResetDialog = false
                showManagePlayers = true
            }
        )
    }

    if (showQuickRestart) {
        com.mwarrc.pocketscore.ui.feature.game.components.QuickRestartDialog(
            currentPlayers = players,
            settings = settings,
            onDismiss = { showQuickRestart = false },
            onStartGame = { updatedPlayers ->
                showQuickRestart = false
                onRestart(updatedPlayers)
            }
        )
    }

    if (showExitConfirmation) {
        val context = androidx.compose.ui.platform.LocalContext.current
        ExitConfirmationDialog(
            onDismiss = { showExitConfirmation = false },
            onConfirm = {
                showExitConfirmation = false
                exitConfirmed = true
                (context as? android.app.Activity)?.finish()
            }
        )
    }

    // State calculations - must be before dialogs that use them
    val lastScoreEvent = remember(globalEvents, canUndo) {
        if (canUndo) globalEvents.lastOrNull { it.type == GameEventType.SCORE || it.type == GameEventType.CORRECTION } else null
    }

    val mostRecentChange = remember(globalEvents) {
        globalEvents.lastOrNull { 
            it.type == GameEventType.SCORE || 
            it.type == GameEventType.CORRECTION || 
            it.type == GameEventType.UNDO 
        }
    }

    // Map of last score change points for EACH player
    val playerLastChanges = remember(globalEvents) {
        globalEvents.filter { 
            it.type == GameEventType.SCORE || 
            it.type == GameEventType.CORRECTION || 
            it.type == GameEventType.UNDO 
        }.groupBy { it.playerId }
         .mapValues { it.value.lastOrNull()?.points }
    }

    if (showUndoConfirmation) {
        UndoConfirmationDialog(
            lastScoreEvent = lastScoreEvent,
            players = players,
            onDismiss = { showUndoConfirmation = false },
            onConfirm = {
                onGlobalUndo()
                showUndoConfirmation = false
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

    if (showHistoryDialog) {
        GameHistoryDialog(
            events = globalEvents,
            playerNames = players.map { it.name },
            onDismiss = { showHistoryDialog = false }
        )
    }

    if (showCalculator) {
        QuickCalculatorSheet(
            settings = settings,
            expression = calculatorExpression,
            onExpressionChange = { calculatorExpression = it },
            players = players,
            onAddScore = onUpdateScore,
            onUpdateSettings = onUpdateSettings,
            onDismiss = { showCalculator = false }
        )
    }



    if (showPoolProbability) {
        PoolProbabilitySheet(
            players = players,
            ballsOnTable = ballsOnTable,
            onBallsOnTableChange = { newBalls -> ballsOnTable = newBalls },
            onDismiss = { showPoolProbability = false }
        )
    }

    if (showQuickBallDialog) {
        com.mwarrc.pocketscore.ui.feature.game.components.QuickBallSelectDialog(
            ballsOnTable = ballsOnTable,
            onBallsOnTableChange = { newBalls -> ballsOnTable = newBalls },
            onDismiss = { showQuickBallDialog = false }
        )
    }

    if (showHelp) {
        GameHelpSheet(onDismiss = { showHelp = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Scoreboard",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                        
                        Spacer(Modifier.width(8.dp))
                        
                        // New Ball Shortcut moved here to be close to text
                        Surface(
                            onClick = { showQuickBallDialog = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(38.dp),
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Analytics,
                                    "Quick Balls",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    TextButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Close,
                                "End Session",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("End Game", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showUndoConfirmation = true },
                        enabled = canUndo,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                "Undo Last",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Undo", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
                windowInsets = WindowInsets(top = 32.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (!showNumpad) {
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
                            hasBadge = showHistoryBadge,
                            onClick = { showHistoryDialog = true }
                        )
                        BottomNavItem(
                            icon = Icons.Default.Calculate,
                            label = "Math",
                            onClick = { showCalculator = true }
                        )
                        BottomNavItem(
                            icon = Icons.Default.Analytics,
                            label = "Balls",
                            onClick = { showPoolProbability = true }
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
        }
    ) { padding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)
            .imePadding()

        Box(modifier = contentModifier) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Mode Status Banners
                StrictModeBanner(settings)


                val listBottomPadding = when {
                    showNumpad && settings.useCustomKeyboard -> 340.dp
                    isImeVisible -> 16.dp 
                    else -> 88.dp
                }

                val scope = rememberCoroutineScope()

                GameScoreboard(
                    players = players,
                    currentPlayerId = currentPlayerId,
                    settings = settings,
                    leaderId = leaderId,
                    playerLastChanges = playerLastChanges,
                    scoreInput = scoreInput,
                    onScoreInputChange = { scoreInput = it },
                    onShowNumpad = { showNumpad = true },
                    onUpdateScore = { pid, pts ->
                        onUpdateScore(pid, pts)
                        
                        // Strict Mode Auto-Advance Fix:
                        // Trigger next turn if strict mode is ON and system auto-next-turn is OFF.
                        // We use a coroutine to ensure the score update settles first.
                        // We check pts >= 0 to allow passing (0 pts) or scoring.
                        if (pts >= 0 && settings.strictTurnMode && !settings.autoNextTurn) {
                            scope.launch {
                                delay(50) 
                                onNextTurn()
                            }
                        }
                    },
                    onSetCurrentPlayer = onSetCurrentPlayer,
                    listBottomPadding = listBottomPadding,
                    tableSum = tableSum,
                    leaderScore = leaderScore,
                    selectionForHeader = selectionForHeader,
                    onHeaderSelection = { localHeaderSelection = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Custom Numpad Overlay (Non-blocking system keyboard style)
        if (settings.useCustomKeyboard) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showNumpad,
                    enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
                    exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
                ) {
                    ScoreNumpad(
                        onNumberClick = { num ->
                            if (scoreInput.length < 5) scoreInput += num
                        },
                        onBackspaceClick = {
                            if (scoreInput.isNotEmpty()) scoreInput = scoreInput.dropLast(1)
                        },
                        onDismiss = { 
                            if (!isNumpadPinned) {
                                showNumpad = false
                            }
                        },
                        isPinned = isNumpadPinned,
                        onTogglePin = { isNumpadPinned = !isNumpadPinned },
                        settings = settings,
                        onUpdateSettings = onUpdateSettings
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


