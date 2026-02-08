package com.mwarrc.pocketscore.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mwarrc.pocketscore.data.repository.GameRepository
import com.mwarrc.pocketscore.domain.model.*
import com.mwarrc.pocketscore.util.AnalyticsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppState(
    val gameState: GameState = GameState(),
    val gameHistory: GameHistory = GameHistory(),
    val settings: AppSettings = AppSettings()
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val _snapshots = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
    val snapshots: StateFlow<List<Pair<String, Long>>> = _snapshots.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.gameState,
                repository.gameHistory,
                repository.appSettings
            ) { gameState, gameHistory, settings ->
                AppState(gameState, gameHistory, settings)
            }.collect { newState ->
                _state.value = newState
            }
        }
        refreshSnapshots()
        viewModelScope.launch {
            repository.triggerDailyAutoSnapshot()
            refreshSnapshots() // Refresh again if it created a new one
        }
    }

    fun refreshSnapshots() {
        viewModelScope.launch {
            _snapshots.value = repository.getLocalSnapshots()
        }
    }

    /**
     * Starts a new game with the specified player names.
     * Filters out blank names and initializes the first player as current.
     */
    fun startNewGame(playerNames: List<String>) {
        val newPlayers = playerNames.filter { it.isNotBlank() }.map { Player(name = it.trim()) }
        val firstPlayerId = newPlayers.firstOrNull()?.id
        val newState = GameState(
            players = newPlayers, 
            isGameActive = true, 
            currentPlayerId = firstPlayerId,
            deviceInfo = _state.value.settings.customDeviceName ?: android.os.Build.MODEL
        )
        updateGameState(newState)
        AnalyticsManager.logGameStarted(newPlayers.size)
    }

    /**
     * Sets the current active player by ID.
     */
    fun setCurrentPlayer(playerId: String) {
        val currentState = _state.value.gameState
        updateGameState(currentState.copy(currentPlayerId = playerId))
    }

    /**
     * Resumes a previously saved game.
     * @param game The game state to resume
     * @param shouldOverrideHistory If true, removes the game from history to avoid duplicates
     */
    fun resumeGame(game: GameState, shouldOverrideHistory: Boolean) {
        viewModelScope.launch {
            if (shouldOverrideHistory) {
                repository.deleteGameFromHistory(game.id)
            }
            updateGameState(game.copy(isGameActive = true, lastUpdate = System.currentTimeMillis()))
            AnalyticsManager.logGameStarted(game.players.size, isResume = true)
        }
    }

    /**
     * Deletes a specific game from the storage history.
     */
    fun deleteGameFromHistory(gameId: String) {
        viewModelScope.launch {
            repository.deleteGameFromHistory(gameId)
        }
    }

    /**
     * Advances to the next active player's turn.
     */
    fun nextTurn() {
        val currentState = _state.value.gameState
        val activePlayers = currentState.players.filter { it.isActive }
        if (activePlayers.isEmpty()) return

        val currentIndex = activePlayers.indexOfFirst { it.id == currentState.currentPlayerId }
        val nextIndex = (currentIndex + 1) % activePlayers.size
        updateGameState(currentState.copy(currentPlayerId = activePlayers[nextIndex].id))
    }

    /**
     * Updates a player's score and records the event.
     * In strict turn mode, only the current player can modify scores.
     * Automatically advances to the next player's turn after scoring.
     */
    fun updateScore(playerId: String, points: Int) {
        val currentState = _state.value.gameState
        val settings = _state.value.settings
        val player = currentState.players.find { it.id == playerId } ?: return

        // Enforce strict turn mode: only current player can score
        if (settings.strictTurnMode && playerId != currentState.currentPlayerId) {
            return
        }

        // Update player score and maintain score history
        val newScore = player.score + points
        val newEvent = ScoreEvent(points = points)
        val newLocalEventHistory = (listOf(newEvent) + player.eventHistory).take(50)

        val updatedPlayers = currentState.players.map {
            if (it.id == playerId) it.copy(
                score = newScore,
                history = (listOf(it.score) + it.history).take(10),
                eventHistory = newLocalEventHistory
            )
            else it
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
            val nextIndex = (currentIndex + 1) % activePlayers.size
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
     * Supports one level of undo, reverting both the score and turn state.
     */
    fun undoLastGlobalAction() {
        val currentState = _state.value.gameState
        if (!currentState.canUndo || currentState.globalEvents.isEmpty()) return

        val lastEvent = currentState.globalEvents.last { it.type == GameEventType.SCORE }

        val updatedPlayers = currentState.players.map { player ->
            if (player.id == lastEvent.playerId) {
                player.copy(score = player.score - lastEvent.points)
            } else {
                player
            }
        }

        // Record undo event in global log
        val undoEvent = GameEvent(
            playerId = lastEvent.playerId,
            playerName = lastEvent.playerName,
            type = GameEventType.UNDO,
            points = -lastEvent.points,
            previousPlayerId = currentState.currentPlayerId,
            previousScore = currentState.players.find { it.id == lastEvent.playerId }?.score,
            newScore = currentState.players.find { it.id == lastEvent.playerId }?.score?.minus(lastEvent.points)
        )

        updateGameState(currentState.copy(
            players = updatedPlayers,
            currentPlayerId = lastEvent.previousPlayerId ?: currentState.currentPlayerId,
            globalEvents = currentState.globalEvents + undoEvent,
            lastUpdate = System.currentTimeMillis(),
            canUndo = false
        ))
    }

    /**
     * Toggles a player's active status.
     * In strict turn mode, players cannot be removed (only added).
     * Prevents deactivation if it would result in fewer than 2 active players.
     */
    fun setPlayerActive(playerId: String, isActive: Boolean) {
        val currentState = _state.value.gameState
        val settings = _state.value.settings
        val player = currentState.players.find { it.id == playerId } ?: return

        // Strict mode: prevent player removal
        if (settings.strictTurnMode && !isActive) {
            return
        }

        // Ensure minimum of 2 active players
        val currentActiveCount = currentState.players.count { it.isActive }
        val isTargetActive = player.isActive

        if (!isActive && isTargetActive && currentActiveCount <= 2) return

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
                nextPlayerId = nextActive[oldIndex % nextActive.size].id
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
     * Archives the game to history if any player has a non-zero score.
     */
    fun resetGame(finalized: Boolean = true) {
        viewModelScope.launch {
            val currentGame = _state.value.gameState
            if (currentGame.players.any { it.score != 0 }) {
                repository.archiveCurrentGame(currentGame.copy(isFinalized = finalized))
            }
            // Log game ended analytics
            AnalyticsManager.logGameEnded(
                playerCount = currentGame.players.size,
                totalTurns = currentGame.globalEvents.count { it.type == GameEventType.SCORE },
                isFinalized = finalized
            )
            repository.clearGameState()
        }
    }

    /**
     * Updates application settings using the provided transformation function.
     */
    fun updateSettings(update: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            val currentSettings = _state.value.settings
            val newSettings = update(currentSettings)

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
        }
    }

    /**
     * Prepares data for sharing based on a specific game ID or the entire history.
     */
    suspend fun getShareData(gameId: String? = null): PocketScoreShare {
        return repository.getShareableData(gameId)
    }

    /**
     * Imports shared data using smart merging.
     */
    fun importData(share: PocketScoreShare, playerNameMappings: Map<String, String> = emptyMap()) {
        viewModelScope.launch {
            repository.mergeShareData(share, playerNameMappings)
        }
    }

    /**
     * Renames a player globally across all records and settings.
     */
    fun renamePlayer(oldName: String, newName: String) {
        viewModelScope.launch {
            repository.renamePlayer(oldName, newName)
        }
    }

    suspend fun getLocalSnapshots(): List<Pair<String, Long>> {
        return repository.getLocalSnapshots()
    }

    fun createSnapshot(name: String) {
        viewModelScope.launch {
            repository.createLocalSnapshot(name)
            refreshSnapshots()
        }
    }

    suspend fun getSnapshotData(name: String): PocketScoreShare? {
        return repository.getSnapshotContent(name)
    }

    fun restoreSnapshot(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.restoreFromSnapshot(name)
            onSuccess()
        }
    }

    fun deleteSnapshot(name: String) {
        viewModelScope.launch {
            repository.deleteSnapshot(name)
            refreshSnapshots()
        }
    }

    fun triggerCloudBackup() {
        viewModelScope.launch {
            repository.triggerCloudBackup()
            refreshSnapshots()
        }
    }

    suspend fun exportSnapshot(name: String): Boolean {
        return repository.exportSnapshotToPublic(name)
    }

    private fun updateGameState(newState: GameState) {
        viewModelScope.launch {
            repository.saveGameState(newState)
        }
    }
}