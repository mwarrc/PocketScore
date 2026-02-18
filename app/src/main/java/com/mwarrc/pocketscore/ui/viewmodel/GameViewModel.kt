package com.mwarrc.pocketscore.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mwarrc.pocketscore.data.repository.GameRepository
import com.mwarrc.pocketscore.domain.model.*
import com.mwarrc.pocketscore.util.AnalyticsManager
import com.mwarrc.pocketscore.util.DeviceInfoProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for managing game state and business logic.
 * 
 * This ViewModel handles:
 * - Game state management and player interactions
 * - Score tracking and turn management
 * - Game history and archival
 * - Settings and preferences
 * - Data import/export and snapshots
 * 
 * @param repository The game data repository
 * @param deviceInfoProvider Provider for device-specific information
 * @param analyticsManager Manager for analytics events (injected for testability)
 */
class GameViewModel(
    private val repository: GameRepository,
    private val deviceInfoProvider: DeviceInfoProvider = DeviceInfoProvider.Default,
    private val analyticsManager: AnalyticsManager = AnalyticsManager
) : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _snapshots = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
    val snapshots: StateFlow<List<Pair<String, Long>>> = _snapshots.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    init {
        initializeState()
        initializeAnalytics()
        initializeSnapshots()
    }

    /**
     * Initializes the combined state flow from repository sources.
     */
    private fun initializeState() {
        viewModelScope.launch {
            supervisorScope {
                try {
                    combine(
                        repository.gameState,
                        repository.gameHistory,
                        repository.appSettings
                    ) { gameState, gameHistory, settings ->
                        AppState(gameState, gameHistory, settings)
                    }.collect { newState ->
                        _state.value = newState
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing state", e)
                    emitError("Failed to initialize game state")
                }
            }
        }
    }

    /**
     * Initializes analytics with a stable user ID.
     * Ensures ID is created once and persisted.
     */
    private fun initializeAnalytics() {
        viewModelScope.launch {
            supervisorScope {
                try {
                    // Wait for settings to be available
                    val settings = repository.appSettings.first()
                    
                    val finalId = if (settings.analyticsId == null) {
                        val newId = java.util.UUID.randomUUID().toString()
                        repository.updateSettings(settings.copy(analyticsId = newId))
                        com.mwarrc.pocketscore.util.CloudAnalyticsManager.logInstallation(newId)
                        newId
                    } else {
                        settings.analyticsId!!
                    }
                    
                    // Log app open with stable ID
                    analyticsManager.logAppOpen(finalId)
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing analytics", e)
                }
            }
        }
    }

    /**
     * Initializes snapshot data and triggers daily auto-backup.
     */
    private fun initializeSnapshots() {
        viewModelScope.launch {
            supervisorScope {
                try {
                    refreshSnapshots()
                    repository.triggerDailyAutoSnapshot()
                    refreshSnapshots() // Refresh if new snapshot was created
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing snapshots", e)
                }
            }
        }
    }

    /**
     * Refreshes the list of available snapshots.
     */
    fun refreshSnapshots() {
        viewModelScope.launch {
            try {
                _snapshots.value = repository.getLocalSnapshots()
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing snapshots", e)
                emitError("Failed to load snapshots")
            }
        }
    }

    /**
     * Starts a new game with the specified player names.
     * 
     * Validates input and initializes the first player as current.
     * 
     * @param playerNames List of player names (duplicates and blanks will be filtered)
     * @throws IllegalArgumentException if fewer than MIN_PLAYERS valid names provided
     */
    fun startNewGame(playerNames: List<String>) {
        val trimmedNames = playerNames.map { it.trim() }.filter { it.isNotBlank() }
        
        // Validation
        if (trimmedNames.size < MIN_PLAYERS) {
            emitError("At least $MIN_PLAYERS players are required to start a game")
            return
        }
        
        if (trimmedNames.size > MAX_PLAYERS) {
            emitError("Maximum $MAX_PLAYERS players allowed")
            return
        }
        
        // Check for duplicates
        val uniqueNames = trimmedNames.distinct()
        if (uniqueNames.size != trimmedNames.size) {
            emitError("Player names must be unique")
            return
        }
        
        val newPlayers = uniqueNames.map { Player(name = it) }
        val firstPlayerId = newPlayers.first().id
        
        val newState = GameState(
            players = newPlayers,
            isGameActive = true,
            currentPlayerId = firstPlayerId,
            deviceInfo = _state.value.settings.customDeviceName ?: deviceInfoProvider.getDeviceModel()
        )
        
        updateGameState(newState)
        analyticsManager.logGameStarted(
            newPlayers.size,
            analyticsId = _state.value.settings.analyticsId
        )
    }

    /**
     * Sets the current active player by ID.
     * 
     * If pool management is enabled, automatically skips eliminated players.
     * 
     * @param playerId The ID of the player to set as current
     */
    fun setCurrentPlayer(playerId: String) {
        val currentState = _state.value.gameState
        val settings = _state.value.settings
        
        // Validate player exists and is active
        val player = currentState.players.find { it.id == playerId }
        if (player == null) {
            Log.w(TAG, "Attempted to set non-existent player as current: $playerId")
            return
        }
        
        if (!player.isActive) {
            Log.w(TAG, "Attempted to set inactive player as current: $playerId")
            emitError("Cannot set inactive player as current")
            return
        }
        
        // If pool management is enabled, ensure we don't switch to an eliminated player
        val nextId = if (settings.poolBallManagementEnabled) {
            findNextValidPlayer(currentState.copy(currentPlayerId = playerId), settings) ?: playerId
        } else {
            playerId
        }
        
        updateGameState(currentState.copy(currentPlayerId = nextId))
    }

    /**
     * Updates the balls remaining on the table and recalculates current player.
     * 
     * This may trigger automatic player skipping if pool management is enabled.
     * 
     * @param balls Set of ball numbers currently on the table
     */
    fun updateBallsOnTable(balls: Set<Int>) {
        val currentState = _state.value.gameState
        val settings = _state.value.settings
        
        // Validate ball numbers
        if (balls.any { it < 1 || it > 15 }) {
            Log.w(TAG, "Invalid ball numbers provided: $balls")
            emitError("Ball numbers must be between 1 and 15")
            return
        }
        
        val nextId = if (settings.poolBallManagementEnabled) {
            findNextValidPlayer(
                currentState.copy(ballsOnTable = balls),
                settings
            ) ?: currentState.currentPlayerId
        } else {
            currentState.currentPlayerId
        }
        
        updateGameState(currentState.copy(
            ballsOnTable = balls,
            currentPlayerId = nextId
        ))
    }

    /**
     * Finds the next valid (non-eliminated) player for the current turn.
     * 
     * In pool management mode, skips players who are mathematically eliminated.
     * 
     * @param state Current game state
     * @param settings App settings including pool management configuration
     * @return ID of next valid player, or null if none available
     */
    private fun findNextValidPlayer(state: GameState, settings: AppSettings): String? {
        val activePlayers = state.players.filter { it.isActive }
        if (activePlayers.isEmpty()) return state.currentPlayerId
        
        val currentPlayerId = state.currentPlayerId ?: return activePlayers.firstOrNull()?.id
        if (!settings.poolBallManagementEnabled) return currentPlayerId
        
        val ballValues = settings.ballValues
        val tableSum = state.ballsOnTable.sumOf { ballValues[it] ?: 0 }
        val leaderScore = activePlayers.maxOfOrNull { it.score } ?: 0
        
        // Determine actual leaders (players tied for first place)
        val actualLeaders = if (activePlayers.any { it.score != 0 }) {
            activePlayers.filter { it.score == leaderScore }.map { it.id }.toSet()
        } else {
            emptySet()
        }

        /**
         * Checks if a player is mathematically eliminated.
         */
        fun isEliminated(player: Player): Boolean {
            if (tableSum <= 0) return false
            val potentialMax = player.score + tableSum
            return !actualLeaders.contains(player.id) && potentialMax < leaderScore
        }

        /**
         * Checks if a player should be skipped in turn rotation.
         */
        fun shouldSkipPlayer(player: Player): Boolean {
            return isEliminated(player) && 
                   (settings.strictTurnMode || !settings.allowEliminatedInput)
        }

        val currentPlayer = activePlayers.find { it.id == currentPlayerId }
        val shouldSkipCurrent = currentPlayer != null && shouldSkipPlayer(currentPlayer)
            
        if (!shouldSkipCurrent) return currentPlayerId
        
        // Find next valid player in rotation
        val currentIndex = activePlayers.indexOfFirst { it.id == currentPlayerId }
        if (currentIndex == -1) return activePlayers.firstOrNull()?.id
        
        var nextIndex = (currentIndex + 1) % activePlayers.size
        var attempts = 0
        
        while (attempts < activePlayers.size) {
            val candidate = activePlayers[nextIndex]
            if (!shouldSkipPlayer(candidate)) {
                return candidate.id
            }
            nextIndex = (nextIndex + 1) % activePlayers.size
            attempts++
        }
        
        // All players eliminated - return current
        return currentPlayerId
    }

    /**
     * Resumes a previously saved game.
     * 
     * @param game The game state to resume
     * @param shouldOverrideHistory If true, removes the game from history to avoid duplicates
     */
    fun resumeGame(game: GameState, shouldOverrideHistory: Boolean) {
        viewModelScope.launch {
            try {
                if (shouldOverrideHistory) {
                    repository.deleteGameFromHistory(game.id)
                }
                updateGameState(game.copy(
                    isGameActive = true,
                    lastUpdate = System.currentTimeMillis()
                ))
                analyticsManager.logGameStarted(
                    game.players.size,
                    isResume = true,
                    analyticsId = _state.value.settings.analyticsId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming game", e)
                emitError("Failed to resume game")
            }
        }
    }

    /**
     * Deletes a specific game from the storage history.
     * 
     * @param gameId ID of the game to delete
     */
    fun deleteGameFromHistory(gameId: String) {
        viewModelScope.launch {
            try {
                repository.deleteGameFromHistory(gameId)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting game from history", e)
                emitError("Failed to delete game")
            }
        }
    }

    /**
     * Toggles the archived status of a specific game.
     * 
     * @param gameId ID of the game to archive/unarchive
     */
    fun toggleArchiveGame(gameId: String) {
        viewModelScope.launch {
            try {
                val history = _state.value.gameHistory
                val game = history.pastGames.find { it.id == gameId }
                
                if (game == null) {
                    Log.w(TAG, "Game not found for archiving: $gameId")
                    emitError("Game not found")
                    return@launch
                }
                
                repository.updateGameInHistory(game.copy(isArchived = !game.isArchived))
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling archive status", e)
                emitError("Failed to update game archive status")
            }
        }
    }

    /**
     * Advances to the next active player's turn.
     */
    fun nextTurn() {
        val currentState = _state.value.gameState
        val activePlayers = currentState.players.filter { it.isActive }
        
        if (activePlayers.isEmpty()) {
            Log.w(TAG, "No active players for next turn")
            return
        }

        val currentIndex = activePlayers.indexOfFirst { it.id == currentState.currentPlayerId }
        val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % activePlayers.size
        
        updateGameState(currentState.copy(currentPlayerId = activePlayers[nextIndex].id))
    }

    /**
     * Updates a player's score and records the event.
     * 
     * In strict turn mode, only the current player can modify scores.
     * Automatically advances to the next player's turn after scoring if enabled.
     * 
     * @param playerId The player's unique identifier
     * @param points Points to add (can be negative)
     */
    fun updateScore(playerId: String, points: Int) {
        val currentState = _state.value.gameState
        val settings = _state.value.settings
        val player = currentState.players.find { it.id == playerId }
        
        if (player == null) {
            Log.w(TAG, "Attempted to update score for non-existent player: $playerId")
            return
        }

        // Enforce strict turn mode: only current player can score
        if (settings.strictTurnMode && playerId != currentState.currentPlayerId) {
            Log.w(TAG, "Score update blocked by strict turn mode")
            return
        }

        // Update player score and maintain score history
        val newScore = player.score + points
        val newEvent = ScoreEvent(points = points)
        val newLocalEventHistory = (listOf(newEvent) + player.eventHistory).take(MAX_EVENT_HISTORY)

        val updatedPlayers = currentState.players.map {
            if (it.id == playerId) {
                it.copy(
                    score = newScore,
                    history = (listOf(it.score) + it.history).take(MAX_SCORE_HISTORY),
                    eventHistory = newLocalEventHistory
                )
            } else {
                it
            }
        }

        // Record event in global game log
        val isZero = points == 0
        val globalEvent = GameEvent(
            playerId = playerId,
            playerName = player.name,
            type = GameEventType.SCORE,
            points = points,
            previousPlayerId = currentState.currentPlayerId,
            isZeroInput = isZero,
            previousScore = player.score,
            newScore = newScore
        )
        val updatedGlobalEvents = currentState.globalEvents + globalEvent

        // Advance to next active player if enabled
        val activePlayers = updatedPlayers.filter { it.isActive }
        val nextPlayerId = if (settings.autoNextTurn && activePlayers.isNotEmpty()) {
            val currentIndex = activePlayers.indexOfFirst { it.id == currentState.currentPlayerId }
            val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % activePlayers.size
            activePlayers[nextIndex].id
        } else {
            currentState.currentPlayerId
        }

        updateGameState(currentState.copy(
            players = updatedPlayers,
            currentPlayerId = nextPlayerId,
            lastUpdate = System.currentTimeMillis(),
            globalEvents = updatedGlobalEvents,
            canUndo = true
        ))
    }

    /**
     * Undoes the last score change.
     * 
     * Supports one level of undo, reverting both the score and turn state.
     */
    fun undoLastGlobalAction() {
        val currentState = _state.value.gameState
        
        if (!currentState.canUndo || currentState.globalEvents.isEmpty()) {
            Log.w(TAG, "Undo not available")
            return
        }

        val lastScoreEvent = currentState.globalEvents.lastOrNull { it.type == GameEventType.SCORE }
        
        if (lastScoreEvent == null) {
            Log.w(TAG, "No score event to undo")
            return
        }

        val updatedPlayers = currentState.players.map { player ->
            if (player.id == lastScoreEvent.playerId) {
                player.copy(score = player.score - lastScoreEvent.points)
            } else {
                player
            }
        }

        // Record undo event in global log
        val undoEvent = GameEvent(
            playerId = lastScoreEvent.playerId,
            playerName = lastScoreEvent.playerName,
            type = GameEventType.UNDO,
            points = -lastScoreEvent.points,
            previousPlayerId = currentState.currentPlayerId,
            previousScore = currentState.players.find { it.id == lastScoreEvent.playerId }?.score,
            newScore = currentState.players.find { it.id == lastScoreEvent.playerId }?.score?.minus(lastScoreEvent.points)
        )

        updateGameState(currentState.copy(
            players = updatedPlayers,
            currentPlayerId = lastScoreEvent.previousPlayerId ?: currentState.currentPlayerId,
            globalEvents = currentState.globalEvents + undoEvent,
            lastUpdate = System.currentTimeMillis(),
            canUndo = false
        ))
    }

    /**
     * Toggles a player's active status.
     * 
     * In strict turn mode, players cannot be removed (only added).
     * Prevents deactivation if it would result in fewer than MIN_ACTIVE_PLAYERS.
     * 
     * @param playerId The player's unique identifier
     * @param isActive New active status
     */
    fun setPlayerActive(playerId: String, isActive: Boolean) {
        val currentState = _state.value.gameState
        val settings = _state.value.settings
        val player = currentState.players.find { it.id == playerId }
        
        if (player == null) {
            Log.w(TAG, "Attempted to toggle non-existent player: $playerId")
            return
        }

        // Strict mode: prevent player removal
        if (settings.strictTurnMode && !isActive) {
            Log.w(TAG, "Player removal blocked by strict turn mode")
            return
        }

        // Ensure minimum of MIN_ACTIVE_PLAYERS active players
        val currentActiveCount = currentState.players.count { it.isActive }
        val isTargetActive = player.isActive

        if (!isActive && isTargetActive && currentActiveCount <= MIN_ACTIVE_PLAYERS) {
            emitError("Cannot deactivate - minimum $MIN_ACTIVE_PLAYERS players required")
            return
        }

        val updatedPlayers = currentState.players.map { p ->
            if (p.id == playerId) p.copy(isActive = isActive) else p
        }

        // Log status change event
        val statusEvent = GameEvent(
            playerId = playerId,
            playerName = player.name,
            type = GameEventType.STATUS_CHANGE,
            points = if (isActive) 1 else 0,
            message = if (isActive) "RE-ENABLED: ${player.name}" else "DISABLED: ${player.name}"
        )
        val updatedGlobalEvents = currentState.globalEvents + statusEvent

        // If disabling the current player, advance to next active player
        var nextPlayerId = currentState.currentPlayerId
        if (!isActive && playerId == currentState.currentPlayerId) {
            val nextActive = updatedPlayers.filter { it.isActive }
            if (nextActive.isNotEmpty()) {
                val oldActive = currentState.players.filter { it.isActive }
                val oldIndex = oldActive.indexOfFirst { it.id == playerId }
                
                // Fixed: Check for valid index before using modulo
                nextPlayerId = if (oldIndex >= 0) {
                    nextActive[oldIndex % nextActive.size].id
                } else {
                    nextActive.first().id
                }
            }
        }

        updateGameState(currentState.copy(
            players = updatedPlayers,
            currentPlayerId = nextPlayerId,
            globalEvents = updatedGlobalEvents,
            lastUpdate = System.currentTimeMillis()
        ))
    }

    /**
     * Resets the current game.
     * 
     * Archives the game to history if any player has a non-zero score.
     * 
     * @param finalized Whether the game was completed normally
     * @param forceSave Force save even if scores are zero
     */
    fun resetGame(finalized: Boolean = true, forceSave: Boolean = false) {
        viewModelScope.launch {
            try {
                val currentGame = _state.value.gameState
                val settings = _state.value.settings
                
                if (currentGame.players.any { it.score != 0 } || currentGame.getScoringEventCount() > 0 || forceSave) {
                    repository.archiveCurrentGame(
                        currentGame.copy(isFinalized = finalized),
                        saveOverride = forceSave
                    )
                }
                
                logGameEndedAnalytics(currentGame, finalized)
                
                // Trigger Post-Game Snapshot if enabled AND not in Guest Mode
                if (settings.snapshotAfterGameEnabled && !settings.isGuestSession && (currentGame.players.any { it.score != 0 } || currentGame.getScoringEventCount() > 0 || forceSave)) {
                    val timestamp = SimpleDateFormat("HH-mm", Locale.getDefault()).format(Date())
                    repository.createLocalSnapshot("Post-Game-Trigger-$timestamp")
                    refreshSnapshots()
                }

                // Chance to trigger a random snapshot just for fun/extra safety
                if (settings.randomSnapshotsEnabled && (1..100).random() <= 15) { // 15% chance
                    repository.createLocalSnapshot("SafeGuard-Random-Pulse")
                    refreshSnapshots()
                }
                
                repository.clearGameState()
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting game", e)
                emitError("Failed to reset game")
            }
        }
    }

    /**
     * Finishes current game and starts a new one with a custom roster.
     * 
     * @param playerNames Names for the new game
     * @param forceSave Force save current game even if scores are zero
     */
    fun startRestartMatch(playerNames: List<String>, forceSave: Boolean = false) {
        viewModelScope.launch {
            try {
                val currentGame = _state.value.gameState
                val settings = _state.value.settings
                
                // Archive current game
                if (currentGame.players.any { it.score != 0 } || forceSave) {
                    repository.archiveCurrentGame(
                        currentGame.copy(isFinalized = true),
                        saveOverride = forceSave
                    )
                    logGameEndedAnalytics(currentGame, finalized = true)

                    // Trigger Post-Game Snapshot on restart if enabled AND not in Guest Mode
                    if (settings.snapshotAfterGameEnabled && !settings.isGuestSession) {
                        val timestamp = SimpleDateFormat("HH-mm", Locale.getDefault()).format(Date())
                        repository.createLocalSnapshot("Restart-Trigger-$timestamp")
                        refreshSnapshots()
                    }
                }
                
                // Start new game
                startNewGame(playerNames)
            } catch (e: Exception) {
                Log.e(TAG, "Error restarting match", e)
                emitError("Failed to restart match")
            }
        }
    }

    /**
     * Logs game ended analytics.
     * Extracted to avoid code duplication.
     */
    private fun logGameEndedAnalytics(game: GameState, finalized: Boolean) {
        val winnerScore = game.players.maxOfOrNull { it.score } ?: 0
        val analyticsId = _state.value.settings.analyticsId
        
        analyticsManager.logGameEnded(
            playerCount = game.players.size,
            totalTurns = game.globalEvents.count { it.type == GameEventType.SCORE },
            isFinalized = finalized,
            winnerScore = winnerScore,
            durationMillis = System.currentTimeMillis() - game.startTime,
            analyticsId = analyticsId
        )
    }

    /**
     * Updates application settings using the provided transformation function.
     * 
     * @param update Function to transform current settings to new settings
     */
    fun updateSettings(update: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            try {
                val currentSettings = repository.appSettings.first()
                val newSettings = update(currentSettings)

                // Log strict mode changes to game events
                if (currentSettings.strictTurnMode != newSettings.strictTurnMode) {
                    val currentState = _state.value.gameState
                    val message = if (newSettings.strictTurnMode) {
                        "Strict Mode ENABLED"
                    } else {
                        "Strict Mode DISABLED"
                    }
                    val statusEvent = GameEvent(
                        playerId = "", // System event
                        playerName = "SYSTEM",
                        type = GameEventType.STATUS_CHANGE,
                        points = 0,
                        message = message
                    )
                    val updatedGlobalEvents = currentState.globalEvents + statusEvent

                    updateGameState(currentState.copy(
                        globalEvents = updatedGlobalEvents,
                        lastUpdate = System.currentTimeMillis()
                    ))
                }

                repository.updateSettings(newSettings)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating settings", e)
                emitError("Failed to update settings")
            }
        }
    }

    /**
     * Prepares data for sharing based on a specific game ID or the entire history.
     * 
     * @param gameId Optional game ID to share specific game, null for entire history
     * @return Shareable data structure
     */
    suspend fun getShareData(gameIds: List<String>? = null): PocketScoreShare {
        return repository.getShareableData(gameIds)
    }

    /**
     * Imports shared data using smart merging.
     * 
     * @param share The shared data to import
     * @param playerNameMappings Optional mappings for resolving player name conflicts
     */
    fun importData(share: PocketScoreShare, playerNameMappings: Map<String, String> = emptyMap()) {
        viewModelScope.launch {
            try {
                repository.mergeShareData(share, playerNameMappings)
            } catch (e: Exception) {
                Log.e(TAG, "Error importing data", e)
                emitError("Failed to import data")
            }
        }
    }

    /**
     * Renames a player globally across all records and settings.
     * 
     * @param oldName Current player name
     * @param newName New player name
     */
    fun renamePlayer(oldName: String, newName: String) {
        val trimmedOldName = oldName.trim()
        val trimmedNewName = newName.trim()
        
        if (trimmedOldName.isBlank() || trimmedNewName.isBlank()) {
            emitError("Player names cannot be blank")
            return
        }
        
        if (trimmedOldName == trimmedNewName) {
            return // No change needed
        }
        
        viewModelScope.launch {
            try {
                repository.renamePlayer(trimmedOldName, trimmedNewName)
            } catch (e: Exception) {
                Log.e(TAG, "Error renaming player", e)
                emitError("Failed to rename player")
            }
        }
    }

    /**
     * Gets the list of local snapshots.
     * 
     * @return List of snapshot name and timestamp pairs
     */
    suspend fun getLocalSnapshots(): List<Pair<String, Long>> {
        return repository.getLocalSnapshots()
    }

    /**
     * Creates a new snapshot with the given name.
     * 
     * @param name Name for the snapshot
     */
    fun createLocalSnapshot(name: String) {
        if (name.trim().isBlank()) {
            emitError("Snapshot name cannot be blank")
            return
        }
        
        viewModelScope.launch {
            try {
                repository.createLocalSnapshot(name.trim())
                refreshSnapshots()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating snapshot", e)
                emitError("Failed to create snapshot")
            }
        }
    }

    /**
     * Gets the content of a specific snapshot.
     * 
     * @param name Snapshot name
     * @return Snapshot data or null if not found
     */
    suspend fun getSnapshotContent(name: String): PocketScoreShare? {
        return try {
            repository.getSnapshotContent(name)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting snapshot data", e)
            null
        }
    }

    /**
     * Restores data from a snapshot.
     * 
     * @param name Snapshot name
     * @param onSuccess Callback invoked on successful restoration
     */
    fun restoreFromSnapshot(name: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                repository.restoreFromSnapshot(name)
                onSuccess?.invoke()
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring snapshot", e)
                emitError("Failed to restore snapshot")
            }
        }
    }

    /**
     * Deletes a snapshot.
     * 
     * @param name Snapshot name
     */
    fun deleteSnapshot(name: String) {
        viewModelScope.launch {
            try {
                repository.deleteSnapshot(name)
                refreshSnapshots()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting snapshot", e)
                emitError("Failed to delete snapshot")
            }
        }
    }

    /**
     * Triggers a cloud backup of current data.
     */
    fun triggerCloudBackup() {
        viewModelScope.launch {
            try {
                repository.triggerCloudBackup()
                refreshSnapshots()
            } catch (e: Exception) {
                Log.e(TAG, "Error triggering cloud backup", e)
                emitError("Failed to trigger cloud backup")
            }
        }
    }

    /**
     * Exports a snapshot to public storage.
     * 
     * @param name Snapshot name
     * @return true if export successful, false otherwise
     */
    suspend fun exportSnapshot(name: String): Boolean {
        return try {
            repository.exportSnapshotToPublic(name)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting snapshot", e)
            emitError("Failed to export snapshot")
            false
        }
    }

    /**
     * Updates the persistent URI for the backups folder.
     * 
     * @param uri The folder URI string, or null to clear.
     */
    fun updateBackupsFolderUri(uri: String?) {
        viewModelScope.launch {
            try {
                repository.updateBackupsFolderUri(uri)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating backups folder URI", e)
            }
        }
    }

    /**
     * Updates the game state in the repository.
     * 
     * @param newState The new game state to save
     */
    private fun updateGameState(newState: GameState) {
        viewModelScope.launch {
            try {
                repository.saveGameState(newState)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating game state", e)
                emitError("Failed to save game state")
            }
        }
    }

    /**
     * Emits an error message to the error flow.
     */
    private fun emitError(message: String) {
        viewModelScope.launch {
            _error.emit(message)
        }
    }

    companion object {
        private const val TAG = "GameViewModel"
        private const val MIN_PLAYERS = 2
        private const val MAX_PLAYERS = 20
        private const val MIN_ACTIVE_PLAYERS = 2
        private const val MAX_EVENT_HISTORY = 50
        private const val MAX_SCORE_HISTORY = 10
    }
}