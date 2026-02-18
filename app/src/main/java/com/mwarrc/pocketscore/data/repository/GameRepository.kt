package com.mwarrc.pocketscore.data.repository

import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing game data, history, and settings.
 * 
 * Provides reactive flows for observing data changes and suspend functions
 * for data manipulation.
 */
interface GameRepository {
    
    /**
     * Flow of current active game state.
     * Emits updates whenever the game state changes.
     */
    val gameState: Flow<GameState>
    
    /**
     * Flow of game history containing past games.
     * Emits updates whenever history is modified.
     */
    val gameHistory: Flow<GameHistory>
    
    /**
     * Flow of application settings.
     * Emits updates whenever settings change.
     */
    val appSettings: Flow<AppSettings>
    
    /**
     * Saves the current game state.
     * 
     * @param gameState The game state to save
     */
    suspend fun saveGameState(gameState: GameState)
    
    /**
     * Clears the current game state.
     * Used when ending or resetting a game.
     */
    suspend fun clearGameState()
    
    /**
     * Archives the current game to history.
     * 
     * @param gameState Game to archive
     * @param saveOverride Force save even in guest mode
     */
    suspend fun archiveCurrentGame(gameState: GameState, saveOverride: Boolean = false)
    
    /**
     * Deletes a specific game from history.
     * 
     * @param gameId ID of the game to delete
     */
    suspend fun deleteGameFromHistory(gameId: String)
    
    /**
     * Updates an existing game in history.
     * 
     * @param game Updated game state
     */
    suspend fun updateGameInHistory(game: GameState)
    
    /**
     * Updates application settings.
     * Settings are validated before being saved.
     * 
     * @param settings New settings to save
     */
    suspend fun updateSettings(settings: AppSettings)
    
    /**
     * Creates a shareable data package.
     * 
     * @param gameIds Optional list of specific game IDs to include.
     *                If null, includes all games and players.
     * @return Shareable data package
     */
    suspend fun getShareableData(gameIds: List<String>? = null): PocketScoreShare
    
    /**
     * Merges imported shared data with existing data.
     * 
     * Performs smart deduplication and applies player name mappings.
     * 
     * @param share Imported data to merge
     * @param playerNameMappings Map of imported names to local names
     */
    suspend fun mergeShareData(
        share: PocketScoreShare,
        playerNameMappings: Map<String, String> = emptyMap()
    )
    
    /**
     * Renames a player across all games and settings.
     * 
     * Updates the name in:
     * - Saved player names
     * - Game history
     * - Active game state
     * - Event logs
     * 
     * @param oldName Current player name
     * @param newName New player name
     */
    suspend fun renamePlayer(oldName: String, newName: String)
    
    // Snapshot Management
    
    /**
     * Creates a new local snapshot backup.
     * 
     * Saves to multiple locations for redundancy:
     * - Internal app storage
     * - Public documents folder
     * - Sync folder (if enabled)
     * 
     * @param name Snapshot name
     */
    suspend fun createLocalSnapshot(name: String)
    
    /**
     * Gets list of available snapshots.
     * 
     * Merges snapshots from all backup locations.
     * 
     * @return List of (snapshot name, timestamp) pairs, sorted by date descending
     */
    suspend fun getLocalSnapshots(): List<Pair<String, Long>>
    
    /**
     * Restores data from a snapshot.
     * 
     * Merges snapshot data with current data using smart deduplication.
     * 
     * @param name Snapshot name to restore
     */
    suspend fun restoreFromSnapshot(name: String)
    
    /**
     * Retrieves the content of a snapshot without restoring it.
     * 
     * @param name Snapshot name
     * @return Snapshot data, or null if not found
     */
    suspend fun getSnapshotContent(name: String): PocketScoreShare?
    
    /**
     * Deletes a snapshot from all backup locations.
     * 
     * @param name Snapshot name to delete
     */
    suspend fun deleteSnapshot(name: String)
    
    /**
     * Exports a snapshot to public storage.
     * 
     * Copies internal snapshot to public documents folder.
     * 
     * @param name Snapshot name to export
     * @return true if export succeeded, false otherwise
     */
    suspend fun exportSnapshotToPublic(name: String): Boolean
    
    /**
     * Creates an automatic daily snapshot if needed.
     * 
     * Only creates one snapshot per day.
     * Respects the localSnapshotsEnabled setting.
     */
    suspend fun triggerDailyAutoSnapshot()
    
    /**
     * Triggers a cloud backup.
     * 
     * Currently a placeholder for future cloud backup functionality.
     */
    suspend fun triggerCloudBackup()

    /**
     * Updates the persistent URI for the external backups folder.
     * 
     * @param uri The folder URI, or null to clear it.
     */
    suspend fun updateBackupsFolderUri(uri: String?)
}