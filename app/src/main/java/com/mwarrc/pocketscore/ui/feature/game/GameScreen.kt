package com.mwarrc.pocketscore.ui.feature.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.*
import com.mwarrc.pocketscore.ui.feature.game.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Constants for Game Screen UI behavior.
 */
private object GameUIConstants {
    const val STATUS_BADGE_TIMEOUT = 120000L
    const val AUTO_ADVANCE_DELAY = 50L
    val NUMPAD_BOTTOM_PADDING = 340.dp
    val IME_BOTTOM_PADDING = 16.dp
    val DEFAULT_BOTTOM_PADDING = 88.dp
}

/**
 * The primary game session screen.
 * 
 * Orchestrates the game flow, including:
 * - Real-time scoreboard in Grid or List layout
 * - Score entry via system or custom numpads
 * - Game state management (Undo, Next Turn, Terminate)
 * - Navigation to Settings, History, and specialized tool sheets
 * - Mathematical pool probability calculation display
 * 
 * @param players List of players in the current game session
 * @param currentPlayerId ID of the player whose turn it currently is
 * @param settings Current application configuration and preferences
 * @param globalEvents Chronological log of all game events for history and undo
 * @param canUndo Whether the last action can be reverted
 * @param onUpdateScore Callback to add/subtract points for a player
 * @param onGlobalUndo Callback to revert the last score event
 * @param onReset Callback to end or pause the current session
 * @param onToggleLayout Callback to switch between Grid and List views
 * @param onNavigateToSettings Callback to open the main settings screen
 * @param onNavigateToHistory Callback to open the local match history
 * @param onTogglePlayerActive Callback to enable/disable players mid-game
 * @param onSetCurrentPlayer Callback to manually change the active turn
 * @param onNextTurn Callback to advance to the next player's turn
 * @param onRestart Callback to finish current match and start a new one with a roster
 * @param onUpdateSettings Callback to modify app preferences
 * @param ballsOnTable Set of pool ball numbers currently remaining on the table
 * @param onUpdateBallsOnTable Callback to update the set of balls on the table
 */
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
    // --- UI Visibility State ---
    var showResetDialog by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showManagePlayers by remember { mutableStateOf(false) }
    var showUndoConfirmation by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var showPoolProbability by remember { mutableStateOf(false) }
    var showQuickBallDialog by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showQuickRestart by remember { mutableStateOf(false) }
    
    // --- Input & Ephemeral State ---
    var calculatorExpression by remember { mutableStateOf(TextFieldValue("")) }
    var showHistoryBadge by remember { mutableStateOf(false) }
    var scoreInput by remember { mutableStateOf("") }
    var showNumpad by remember { mutableStateOf(false) }
    var isNumpadPinned by remember { mutableStateOf(false) }
    var exitConfirmed by remember { mutableStateOf(false) }
    var tempForceSave by remember { mutableStateOf(false) }
    var localHeaderSelection by remember { mutableStateOf<String?>(null) }
    
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // --- Derived Game State ---
    
    val activePlayers = remember(players) { players.filter { it.isActive } }
    
    val hasActiveGame = remember(players) {
        players.any { it.score != 0 }
    }

    val tableSum = remember(ballsOnTable, settings.ballValues) {
        val ballValues = settings.ballValues
        ballsOnTable.sumOf { ballValues[it] ?: 0 }
    }

    val leaderScore = remember(activePlayers) {
        activePlayers.maxOfOrNull { it.score } ?: 0
    }

    // Leader Spotlight IDs
    val leaderIds by remember(activePlayers, settings.leaderSpotlightEnabled) {
        derivedStateOf {
            if (settings.leaderSpotlightEnabled && activePlayers.size > 1 && activePlayers.any { it.score != 0 }) {
                val maxScore = activePlayers.maxOfOrNull { it.score } ?: 0
                activePlayers.filter { it.score == maxScore }.map { it.id }.toSet()
            } else emptySet()
        }
    }
    
    val isTie by remember { derivedStateOf { leaderIds.size > 1 } }

    // Loser Spotlight IDs
    val loserIds by remember(activePlayers, settings.loserSpotlightEnabled, leaderScore) {
        derivedStateOf {
            if (settings.loserSpotlightEnabled && activePlayers.size > 1) {
                val minScore = activePlayers.minOfOrNull { it.score } ?: 0
                if (minScore < leaderScore) {
                    activePlayers.filter { it.score == minScore }.map { it.id }.toSet()
                } else emptySet()
            } else emptySet()
        }
    }
    
    val isLoserTie by remember { derivedStateOf { loserIds.size > 1 } }

    // Last Change Tracking
    val lastScoreEvent = remember(globalEvents, canUndo) {
        if (canUndo) globalEvents.lastOrNull { it.type == GameEventType.SCORE || it.type == GameEventType.CORRECTION } else null
    }

    val playerLastChanges = remember(globalEvents) {
        globalEvents.filter { 
            it.type == GameEventType.SCORE || 
            it.type == GameEventType.CORRECTION || 
            it.type == GameEventType.UNDO 
        }.groupBy { it.playerId }
         .mapValues { it.value.lastOrNull()?.points }
    }

    // --- Interaction Handlers ---

    BackHandler(enabled = showNumpad || (hasActiveGame && !exitConfirmed)) {
        when {
            showNumpad && isNumpadPinned -> isNumpadPinned = false
            showNumpad -> showNumpad = false
            showPoolProbability -> showPoolProbability = false
            hasActiveGame && !exitConfirmed -> showExitConfirmation = true
        }
    }

    // --- Dynamic Effects ---

    // Sync Numpad with Keyboard visibility
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible, settings.useCustomKeyboard) {
        if (!settings.useCustomKeyboard && !isImeVisible) {
            showNumpad = false
        }
    }

    // Strict Mode Badge Logic
    LaunchedEffect(globalEvents) {
        val lastEvent = globalEvents.lastOrNull()
        if (lastEvent?.type == GameEventType.STATUS_CHANGE &&
            (lastEvent.message?.contains("Strict Mode", ignoreCase = true) == true)
        ) {
            showHistoryBadge = true
            delay(GameUIConstants.STATUS_BADGE_TIMEOUT)
            showHistoryBadge = false
        } else {
            showHistoryBadge = false
        }
    }

    // Auto-scroll logic
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

    // Reset ephemeral state when turn changes
    LaunchedEffect(currentPlayerId) {
        localHeaderSelection = null
        scoreInput = ""
    }

    val selectionForHeader = remember(localHeaderSelection, currentPlayerId, activePlayers) {
        val selected = activePlayers.find { it.id == localHeaderSelection }
        val current = activePlayers.find { it.id == currentPlayerId }
        selected ?: current ?: activePlayers.firstOrNull()
    }

    // --- Overlay & Dialog Composable Blocks ---

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
        QuickRestartDialog(
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
            ballValues = settings.ballValues,
            onBallsOnTableChange = onUpdateBallsOnTable,
            onDismiss = { showPoolProbability = false }
        )
    }

    if (showQuickBallDialog) {
        QuickBallSelectDialog(
            ballsOnTable = ballsOnTable,
            ballValues = settings.ballValues,
            onBallsOnTableChange = onUpdateBallsOnTable,
            onDismiss = { showQuickBallDialog = false }
        )
    }

    if (showHelp) {
        GameHelpSheet(onDismiss = { showHelp = false })
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

    // --- Main Layout ---

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().systemBarsPadding().displayCutoutPadding(),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding()
            ) {
                // Mode Status Banners
                StrictModeBanner(settings)

                val listBottomPadding = when {
                    showNumpad && settings.useCustomKeyboard -> GameUIConstants.NUMPAD_BOTTOM_PADDING
                    isImeVisible -> GameUIConstants.IME_BOTTOM_PADDING 
                    else -> GameUIConstants.DEFAULT_BOTTOM_PADDING
                }

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
                        
                        // Strict mode auto-advance logic
                        if (pts >= 0 && settings.strictTurnMode && !settings.autoNextTurn) {
                            scope.launch {
                                delay(GameUIConstants.AUTO_ADVANCE_DELAY) 
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
}
