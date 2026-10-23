package com.mwarrc.pocketscore.ui.feature.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.logic.EliminationRules
import com.mwarrc.pocketscore.domain.model.*
import com.mwarrc.pocketscore.ui.feature.game.components.*
import kotlinx.coroutines.delay

/**
 * Constants for Game Screen UI behavior.
 */
private object GameUIConstants {
    const val STATUS_BADGE_TIMEOUT = 120000L
    val NUMPAD_BOTTOM_PADDING = 340.dp
    val IME_BOTTOM_PADDING = 16.dp
    val DEFAULT_BOTTOM_PADDING = 88.dp
}

/**
 * A score entry held back pending the "value above your point system" confirmation.
 *
 * @property playerId Player the score belongs to
 * @property points The signed score the player attempted to enter
 */
private data class PendingScore(val playerId: String, val points: Int)

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
 * @param onReset Callback to end the current session (finalize or archive)
 * @param onPause Callback to suspend the session without ending it, so it can be resumed later
 * @param onToggleLayout Callback to switch between Grid and List views
 * @param onNavigateToSettings Callback to open the main settings screen
 * @param onNavigateToHistory Callback to open the local match history
 * @param onTogglePlayerActive Callback to enable/disable players mid-game
 * @param onSetCurrentPlayer Callback to manually change the active turn
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
    onUpdateScore: (String, Int, Int?) -> Unit,
    onGlobalUndo: () -> Unit,
    onReset: (Boolean, Boolean) -> Unit,
    onPause: () -> Unit,
    onToggleLayout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onTogglePlayerActive: (String, Boolean) -> Unit,
    onSetCurrentPlayer: (String) -> Unit,
    onRestart: (List<String>, Boolean) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onReorderPlayers: (List<Player>) -> Unit = {},
    onWithdrawPlayer: (String) -> Unit = {},
    onLiveScaleChange: (Float) -> Unit = {},
    ballsOnTable: Set<Int>,
    onUpdateBallsOnTable: (Set<Int>) -> Unit,
    isLoading: Boolean = false,
    loadingMessage: String? = null
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

    // Measured height of the custom numpad, in pixels. NumpadOverlay reports
    // it on first layout and on changes (keyboard-height setting, nav-bar
    // inset, globalScale). The scoreboard's bottom padding derives from this,
    // so the list can always scroll every card clear of the keyboard - a
    // hardcoded constant couldn't: at high globalScale or with a taller
    // keyboard setting, ~100dp of rows were stuck behind the numpad and could
    // never be scrolled into view.
    var numpadHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    var exitConfirmed by remember { mutableStateOf(false) }
    var tempForceSave by remember { mutableStateOf(false) }
    var localHeaderSelection by remember { mutableStateOf<String?>(null) }
    var lastAutoRemovedBall by remember { mutableStateOf<Int?>(null) }
    var showNoBallWarning by remember { mutableStateOf(false) }
    var pendingOversizedScore by remember { mutableStateOf<PendingScore?>(null) }
    var suppressOversizeWarningThisSession by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableStateOf(0L) }
    
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- Derived Game State ---
    
    val activePlayers = remember(players) { players.filter { it.isActive } }
    
    val hasActiveGame = remember(players) {
        players.any { it.score != 0 }
    }

    val tableSum = remember(ballsOnTable, settings.ballValues) {
        val ballValues = settings.ballValues
        ballsOnTable.sumOf { ballValues[it] ?: 0 }
    }

    // Highest points any single ball is worth in the active setup. Drives the
    // "value above your point system" guard so it adapts to custom presets.
    val maxBallValue = remember(settings.ballValues) {
        settings.ballValues.values.maxOrNull() ?: 0
    }

    // Commits a score, running the shared ball-management and auto-advance logic.
    // Used both directly and after the oversize-score confirmation.
    val commitScore: (String, Int) -> Unit = { pid, pts ->
        // Auto-remove pool balls logic.
        // Validate BOTH positive and negative entries: a ball that
        // has already been pocketed no longer exists, so its value
        // can't be scored again in either direction.
        var ballToRemove: Int? = null
        if (settings.poolBallManagementEnabled && settings.autoRemovePoolBalls && pts != 0) {
            val matchingBall = ballsOnTable.firstOrNull { ball ->
                settings.ballValues[ball] == kotlin.math.abs(pts)
            }
            if (matchingBall == null) {
                // No ball on the table matches this score. Surface a
                // prominent warning instead of silently accepting what
                // may be a double-scored (or non-existent) ball.
                lastAutoRemovedBall = null
                showNoBallWarning = true
            } else if (pts > 0) {
                // Pocketing a ball removes it from the table.
                ballToRemove = matchingBall
                lastAutoRemovedBall = matchingBall
            } else {
                // Negative score deducts the value but the ball stays
                // on the table - nothing to remove.
                lastAutoRemovedBall = null
            }
        }

        // Single call handles updating score and updating balls atomically.
        //
        // The turn rotation lives in the ViewModel, in the same state write:
        // strict turn mode always advances there, so there is no second
        // "advance now" call to race with this one. The old UI-side fallback
        // (advance 50ms later when strict mode was on but auto-advance was
        // off) read the game state before this score had been persisted and
        // wrote the whole thing back - which could undo the score it was
        // supposed to follow, and could skip a player when it did land.
        onUpdateScore(pid, pts, ballToRemove)
    }

    val leaderScore = remember(activePlayers) {
        activePlayers.maxOfOrNull { it.score } ?: 0
    }

    // Leader Spotlight IDs
    val leaderIds = remember(activePlayers, settings.leaderSpotlightEnabled) {
        if (settings.leaderSpotlightEnabled && activePlayers.size > 1 && activePlayers.any { it.score != 0 }) {
            val maxScore = activePlayers.maxOfOrNull { it.score } ?: 0
            activePlayers.filter { it.score == maxScore }.map { it.id }.toSet()
        } else emptySet()
    }

    val isTie = remember(leaderIds) { leaderIds.size > 1 }

    // Loser Spotlight IDs
    val loserIds = remember(activePlayers, settings.loserSpotlightEnabled, leaderScore) {
        if (settings.loserSpotlightEnabled && activePlayers.size > 1) {
            val minScore = activePlayers.minOfOrNull { it.score } ?: 0
            if (minScore < leaderScore) {
                activePlayers.filter { it.score == minScore }.map { it.id }.toSet()
            } else emptySet()
        } else emptySet()
    }

    val isLoserTie = remember(loserIds) { loserIds.size > 1 }

    // --- Elimination Detection ---
    // Players who can no longer mathematically catch the leader. Used to pulse
    // the top bar so someone checks the table before elimination is trusted.
    //
    // Delegates to EliminationRules rather than recomputing `score + tableSum <
    // leaderScore` inline. That inline copy lacked the guards the turn rotation
    // has, so the pulse could fire - and cards could read ELIMINATED - for a
    // player the rotation was still passing the turn to.
    val eliminatedIds = remember(activePlayers, tableSum, leaderScore, settings.poolBallManagementEnabled) {
        if (!settings.poolBallManagementEnabled) {
            emptySet()
        } else {
            EliminationRules.eliminatedPlayerIds(
                activePlayers = activePlayers,
                tableSum = tableSum,
                leaderScore = leaderScore,
            )
        }
    }

    var showEliminationPulse by remember { mutableStateOf(false) }
    var previouslyEliminated by remember { mutableStateOf<Set<String>>(emptySet()) }
    var eliminationDetected by remember { mutableStateOf(false) }

    LaunchedEffect(eliminatedIds) {
        val newlyEliminated = eliminatedIds - previouslyEliminated
        // Ignore the first computation (game start / resume) so entering a match
        // never triggers a pulse. Never pulse once the table is cleared - that is
        // the endgame, handled by the celebration overlay.
        if (eliminationDetected && newlyEliminated.isNotEmpty() && tableSum > 0) {
            showEliminationPulse = true
        }
        previouslyEliminated = eliminatedIds
        eliminationDetected = true
    }

    LaunchedEffect(showEliminationPulse) {
        if (showEliminationPulse) {
            // Let the pulse breathe a couple of times, then settle.
            delay(2400L)
            showEliminationPulse = false
        }
    }

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
         .mapValues { entry -> 
             val last = entry.value.lastOrNull()
             last?.let { Pair(it.points, it.type == GameEventType.UNDO) }
         }
    }

    // --- Interaction Handlers ---
    
    val isImeVisible = WindowInsets.isImeVisible
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    BackHandler(enabled = showCelebration || showNumpad || (hasActiveGame && !exitConfirmed && !isImeVisible)) {
        when {
            showCelebration -> showCelebration = false
            showNumpad && isNumpadPinned -> isNumpadPinned = false
            showNumpad -> showNumpad = false
            showPoolProbability -> showPoolProbability = false
            hasActiveGame && !exitConfirmed -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000L) {
                    showExitConfirmation = true
                } else {
                    lastBackPressTime = currentTime
                    android.widget.Toast.makeText(context, "Press back again to exit game", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Dynamic Effects ---

    // Sync Numpad with Keyboard visibility
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



    // Which player the header card shows. The header is the card you actually
    // score on, so who it points at matters:
    //
    //  - Strict mode: the turn decides. The header is always the turn holder.
    //    It used to honour a tapped tile instead, which pinned the header to
    //    whoever was tapped - so after the auto-advance the turn had moved on
    //    while the card you were still typing into belonged to the previous
    //    player. The advance looked like it had done nothing.
    //  - Otherwise: a tapped tile pins the header, so you can look at - and
    //    score - a player other than the current turn holder.
    val selectionForHeader = remember(
        localHeaderSelection,
        currentPlayerId,
        activePlayers,
        settings.strictTurnMode,
    ) {
        val selected = activePlayers.find { it.id == localHeaderSelection }
        val current = activePlayers.find { it.id == currentPlayerId }
        if (settings.strictTurnMode) {
            current ?: selected ?: activePlayers.firstOrNull()
        } else {
            selected ?: current ?: activePlayers.firstOrNull()
        }
    }

    // Reset the pending entry when the header's player changes, but deliberately
    // KEEP any manually-picked header selection. Previously this also nulled
    // localHeaderSelection, so tapping a player and then having the turn advance
    // relocated the header a second time - the double-jump on tap.
    //
    // Keyed on the HEADER's player, not on currentPlayerId. scoreInput is a
    // single buffer shared by every card, so what it must be cleared for is
    // whoever the input will be submitted against - which is the header player.
    // Keying on the turn alone missed the case where the header moves without the
    // turn moving (tapping a tile in strict mode sets localHeaderSelection but
    // not currentPlayerId), leaving a half-typed value attached to the
    // previously-focused player and scorable against the wrong person.
    LaunchedEffect(selectionForHeader?.id) {
        scoreInput = ""
    }

    // An eliminated player whose input is not explicitly allowed cannot have
    // points typed in at all. The numpad writes into one shared buffer, so the
    // block has to live here as well as in the card: disabling the +/- buttons
    // alone still let digits land in an eliminated player's field, which read
    // as "typing worked" even though nothing could be applied.
    //
    // The rule comes from EliminationRules so this, the focused card and the
    // turn rotation all answer the same question the same way.
    val headerInputLocked = EliminationRules.isInputLocked(
        isEliminated = selectionForHeader?.let { it.id in eliminatedIds } == true,
        allowEliminatedInput = settings.allowEliminatedInput,
        strictTurnMode = settings.strictTurnMode,
    )

    // Strict mode has a second, independent reason to close the entry: the
    // header might not be the turn holder.
    //
    // In the grid that used to happen constantly. Tapping a tile pinned the
    // header to that player (the tap deliberately doesn't move the turn in
    // strict mode), so after the auto-advance the turn had moved on while the
    // card you were still typing into belonged to the previous player. The
    // advance looked like it had done nothing, and the shared buffer happily
    // accepted digits for a card the turn had already left.
    val headerIsTurnHolder = selectionForHeader?.id == currentPlayerId
    val headerTurnLocked = settings.strictTurnMode && !headerIsTurnHolder

    // One gate for everything that writes into the shared entry buffer: the
    // numpad, the buffer reset, and the change handler handed to the
    // scoreboard. Keeping the two reasons separate above means the wording
    // ("out of the game" vs "not your turn") can stay honest, while everything
    // downstream only has to ask one question.
    val headerEntryLocked = headerInputLocked || headerTurnLocked

    // Keyed on the header player as well as the lock itself: switching
    // straight from one locked player to another leaves the lock unchanged, and
    // the numpad would otherwise stay open over a player who can't be scored.
    LaunchedEffect(selectionForHeader?.id, headerEntryLocked) {
        if (headerEntryLocked) {
            scoreInput = ""
            showNumpad = false
        }
    }

    LaunchedEffect(lastAutoRemovedBall) {
        if (lastAutoRemovedBall != null) {
            delay(3000L)
            lastAutoRemovedBall = null
        }
    }

    // Keep the "no matching ball" warning on screen long enough to be read.
    // The banner also has explicit dismiss/undo actions.
    LaunchedEffect(showNoBallWarning) {
        if (showNoBallWarning) {
            delay(10_000L)
            showNoBallWarning = false
        }
    }

    // End-Game Celebration
    LaunchedEffect(tableSum, leaderScore, activePlayers) {
        if (!settings.poolBallManagementEnabled || !settings.endGameCelebrationEnabled || !hasActiveGame || showCelebration) return@LaunchedEffect
        
        val maxScore = activePlayers.maxOfOrNull { it.score } ?: 0
        
        // Table cleared: all balls potted
        val tableCleared = tableSum == 0 && activePlayers.isNotEmpty()
        
        // Last man standing: the lead is held by exactly one player and everyone
        // else is mathematically out. Expressed through EliminationRules so this
        // agrees with the cards and the turn rotation - a leader is never
        // eliminated by that rule, so "all non-leaders eliminated" is precisely
        // "everyone except the unique leader is eliminated".
        val leaders = activePlayers.filter { it.score == maxScore }
        val others = activePlayers.filter { it.score < maxScore }
        val allOthersEliminated = others.isNotEmpty() && others.all { other ->
            EliminationRules.isEliminated(
                player = other,
                activePlayers = activePlayers,
                tableSum = tableSum,
                leaderScore = maxScore,
            )
        }
        val lastManStanding = leaders.size == 1 && allOthersEliminated && activePlayers.size >= 2

        if (tableCleared || lastManStanding) {
            focusManager.clearFocus(force = true)
            showNumpad = false
            keyboardController?.hide()
            showCelebration = true
        }
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
                onPause()
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
            initialSettings = settings,
            onUpdateSettingsBase = onUpdateSettings,
            onLiveScaleChange = onLiveScaleChange,
            onDismiss = { showQuickSettings = false }
        )
    }

    if (showHistoryDialog) {
        GameHistoryDialog(
            events = globalEvents,
            playerNames = players.map { it.name },
            ballValues = if (settings.poolBallManagementEnabled) settings.ballValues else emptyMap(),
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
            settings = settings,
            onUpdateSettings = onUpdateSettings,
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
            onReorderPlayers = onReorderPlayers,
            onWithdrawPlayer = onWithdrawPlayer,
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
                    ballsRemaining = ballsOnTable.size,
                    canUndo = canUndo,
                    lastAutoRemovedBall = lastAutoRemovedBall,
                    eliminationPulse = showEliminationPulse,
                    onEliminationPulseConsumed = { showEliminationPulse = false },
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
                    // Room for the real, measured numpad + breathing gap.
                    showNumpad && settings.useCustomKeyboard && numpadHeightPx > 0 ->
                        with(density) { numpadHeightPx.toDp() } + 24.dp

                    // Before the numpad has been measured at least once, fall
                    // back to the old static estimate so padding isn't zero.
                    showNumpad && settings.useCustomKeyboard -> GameUIConstants.NUMPAD_BOTTOM_PADDING

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
                    lastUpdatedPlayerId = lastScoreEvent?.playerId,
                    scoreInput = scoreInput,
                    // The buffer is shared by every card, so the gate lives here
                    // rather than in each card: a card can only refuse its own
                    // field, it cannot refuse a numpad that writes into the
                    // shared state from the outside.
                    onScoreInputChange = { if (!headerEntryLocked) scoreInput = it },
                    onShowNumpad = { showNumpad = true },
                    onUpdateScore = { pid, pts ->
                        val shouldWarn = settings.poolBallManagementEnabled &&
                            settings.warnOnExceedingMaxBallValue &&
                            !suppressOversizeWarningThisSession &&
                            maxBallValue > 0 &&
                            kotlin.math.abs(pts) > maxBallValue

                        if (shouldWarn) {
                            // Hold the entry until the player confirms it.
                            pendingOversizedScore = PendingScore(pid, pts)
                        } else {
                            commitScore(pid, pts)
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
                // Ignore keys while the selected player is locked. The overlay
                // is dismissed when that happens, so this is a belt-and-braces
                // guard against a key landing in the same frame.
                onScoreInputChange = { if (!headerEntryLocked) scoreInput = it },
                isPinned = isNumpadPinned,
                onTogglePin = { isNumpadPinned = !isNumpadPinned },
                onDismiss = { if (!isNumpadPinned) showNumpad = false },
                settings = settings,
                onUpdateSettings = onUpdateSettings,
                onHeightChanged = { numpadHeightPx = it }
            )
        }

        // "No matching ball" warning - drops down over the top bar.
        NoMatchingBallBanner(
            visible = showNoBallWarning,
            hapticEnabled = settings.hapticFeedbackEnabled,
            onDismiss = { showNoBallWarning = false },
            onUndo = {
                showNoBallWarning = false
                if (canUndo) showUndoConfirmation = true
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Oversize-score confirmation. Threshold follows the active point system.
        pendingOversizedScore?.let { pending ->
            ExceedsMaxValueDialog(
                enteredValue = kotlin.math.abs(pending.points),
                maxBallValue = maxBallValue,
                hapticEnabled = settings.hapticFeedbackEnabled,
                onConfirm = {
                    commitScore(pending.playerId, pending.points)
                    pendingOversizedScore = null
                },
                onCancel = { pendingOversizedScore = null },
                onConfirmWithSuppression = {
                    // Session-level suppression plus a persisted preference, so a
                    // player who never wants the guard again can also turn it off
                    // in Settings.
                    suppressOversizeWarningThisSession = true
                    onUpdateSettings { it.copy(warnOnExceedingMaxBallValue = false) }
                    commitScore(pending.playerId, pending.points)
                    pendingOversizedScore = null
                }
            )
        }

        // Winner Celebration overlay - sits on top of everything
        LaunchedEffect(showCelebration) {
            if (showCelebration) {
                try {
                    com.mwarrc.pocketscore.util.analytics.PocketScoreAnalytics.get().logWinnerScreenShown("", settings.winnerScreenStyle.name)
                } catch (_: Exception) {
                }
            }
        }
        if (showCelebration) {
            // maxScore must be taken over ACTIVE players only. This used to scan
            // the full roster, so a withdrawn player holding the top score set
            // maxScore to a value no active player could match - `winners` came
            // back empty and the celebration dismissed itself with no winner.
            val maxScore = activePlayers.maxOfOrNull { it.score } ?: 0
            val winners = activePlayers.filter { it.score == maxScore }

            LaunchedEffect(winners.size) {
                if (winners.isEmpty()) {
                    showCelebration = false
                }
            }

            if (winners.isNotEmpty()) {
                when (settings.winnerScreenStyle) {
                    WinnerScreenStyle.WAVY_HERO -> WinnerWavyHeroScreen(
                        standings = activePlayers,
                        winners = winners,
                        onDismiss = { showCelebration = false },
                        onRestart = {
                            showCelebration = false
                            tempForceSave = false // Normal save & play again
                            showQuickRestart = true
                        },
                        onArchive = {
                            showCelebration = false
                            onReset(true, false)
                        }
                    )
                    WinnerScreenStyle.CLASSIC -> WinnerCelebrationOverlay(
                        winners = winners,
                        onDismiss = { showCelebration = false },
                        onRestart = {
                            showCelebration = false
                            tempForceSave = false // Normal save & play again
                            showQuickRestart = true
                        },
                        onArchive = {
                            showCelebration = false
                            onReset(true, false)
                        }
                    )
                }
            }
        }
    }
}


