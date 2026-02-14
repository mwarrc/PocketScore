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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.mwarrc.pocketscore.ui.feature.game.components.ManagePlayersSheet
import com.mwarrc.pocketscore.ui.feature.game.components.GameTopBar
import com.mwarrc.pocketscore.ui.feature.game.components.GameBottomBar
import com.mwarrc.pocketscore.ui.feature.game.components.NumpadOverlay
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
    onReset: (Boolean, Boolean) -> Unit,
    onToggleLayout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onTogglePlayerActive: (String, Boolean) -> Unit,
    onSetCurrentPlayer: (String) -> Unit,
    onNextTurn: () -> Unit,
    onRestart: (List<String>, Boolean) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    ballsOnTable: Set<Int>,
    onUpdateBallsOnTable: (Set<Int>) -> Unit
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
    var showHelp by remember { mutableStateOf(false) }
    var showHistoryBadge by remember { mutableStateOf(false) }
    var scoreInput by remember { mutableStateOf("") }
    var showNumpad by remember { mutableStateOf(false) }
    var isNumpadPinned by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showQuickRestart by remember { mutableStateOf(false) }
    var exitConfirmed by remember { mutableStateOf(false) }
    
    // Store override decision from ResetDialog to play it forward to QuickRestart
    var tempForceSave by remember { mutableStateOf(false) }
    
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
    val leaderIds = remember(activePlayers, settings.leaderSpotlightEnabled) {
        if (settings.leaderSpotlightEnabled && activePlayers.size > 1) {
            val maxScore = activePlayers.maxOfOrNull { it.score } ?: 0
            if (maxScore > 0) {
                activePlayers.filter { it.score == maxScore }.map { it.id }.toSet()
            } else emptySet()
        } else {
            emptySet()
        }
    }
    val isTie = leaderIds.size > 1

    val leaderScore = remember(activePlayers) {
        activePlayers.maxOfOrNull { it.score } ?: 0
    }

    val loserIds = remember(activePlayers, settings.loserSpotlightEnabled, leaderScore) {
        if (settings.loserSpotlightEnabled && activePlayers.size > 1) {
            val minScore = activePlayers.minOfOrNull { it.score } ?: 0
            // Only show loser if there's a difference and they aren't also the leader (0-0 start)
            if (minScore < leaderScore) {
                activePlayers.filter { it.score == minScore }.map { it.id }.toSet()
            } else emptySet()
        } else {
            emptySet()
        }
    }
    val isLoserTie = loserIds.size > 1

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
                    lazyListState.animateScrollToItem(index, scrollOffset = -150)
                }
            }
        }
    }

    LaunchedEffect(currentPlayerId) {
        localHeaderSelection = null
        scoreInput = ""
    }

    val selectionForHeader = remember(localHeaderSelection, currentPlayerId, activePlayers) {
        val selected = activePlayers.find { it.id == localHeaderSelection }
        val current = activePlayers.find { it.id == currentPlayerId }
        selected ?: current ?: activePlayers.firstOrNull()
    }

    // Dialogs - Using modular components
    if (showResetDialog) {
        ResetGameDialog(
            settings = settings,
            onDismiss = { showResetDialog = false },
            onFinishAndArchive = { override ->
                onReset(true, override)
                showResetDialog = false
            },
            onResumeLater = {
                onReset(false, false)
                showResetDialog = false
            },
            onRestartMatch = { override ->
                tempForceSave = override
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
                onRestart(updatedPlayers, tempForceSave)
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
    // State calculations for Game Logic
    val tableSum = remember(ballsOnTable) {
        val ballValues = mapOf(1 to 16, 2 to 17, 3 to 3, 4 to 4, 5 to 5, 6 to 6, 7 to 7, 8 to 8, 9 to 9, 10 to 10, 11 to 11, 12 to 12, 13 to 13, 14 to 14, 15 to 15)
        ballsOnTable.sumOf { ballValues[it] ?: 0 }
    }

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
            onBallsOnTableChange = onUpdateBallsOnTable,
            onDismiss = { showPoolProbability = false }
        )
    }

    if (showQuickBallDialog) {
        com.mwarrc.pocketscore.ui.feature.game.components.QuickBallSelectDialog(
            ballsOnTable = ballsOnTable,
            onBallsOnTableChange = onUpdateBallsOnTable,
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
            GameTopBar(
                settings = settings,
                tableSum = tableSum,
                canUndo = canUndo,
                onEndSession = { showResetDialog = true },
                onUndo = { showUndoConfirmation = true },
                onQuickBalls = { showQuickBallDialog = true }
            )
        },
        bottomBar = {
            if (!showNumpad) {
                GameBottomBar(
                    settings = settings,
                    showHistoryBadge = showHistoryBadge,
                    onShowHelp = { showHelp = true },
                    onShowHistory = { showHistoryDialog = true },
                    onShowCalculator = { showCalculator = true },
                    onShowPoolProbability = { showPoolProbability = true },
                    onShowQuickSettings = { showQuickSettings = true },
                    onShowManagePlayers = { showManagePlayers = true }
                )
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
                    leaderIds = leaderIds,
                    isTie = isTie,
                    loserIds = loserIds,
                    isLoserTie = isLoserTie,
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

        // Custom Numpad Overlay
        NumpadOverlay(
            visible = showNumpad,
            scoreInput = scoreInput,
            onScoreInputChange = { scoreInput = it },
            isPinned = isNumpadPinned,
            onTogglePin = { isNumpadPinned = !isNumpadPinned },
            onDismiss = { if (!isNumpadPinned) showNumpad = false },
            settings = settings,
            onUpdateSettings = onUpdateSettings
        )
    }
    }



    if (showManagePlayers) {
        ManagePlayersSheet(
            players = players,
            currentPlayerId = currentPlayerId,
            settings = settings,
            onTogglePlayerActive = onTogglePlayerActive,
            onDismiss = { showManagePlayers = false }
        )
    }
}
