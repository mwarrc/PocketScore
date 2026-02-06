package com.mwarrc.pocketscore.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import android.os.Build
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Environment

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

class GameRepositoryImpl(private val context: Context) : GameRepository {

    private val gameStateKey = stringPreferencesKey("game_state")
    private val gameHistoryKey = stringPreferencesKey("game_history")
    private val appSettingsKey = stringPreferencesKey("app_settings")

    override val gameState: Flow<GameState> = context.dataStore.data.map { preferences ->
        preferences[gameStateKey]?.let {
            try { Json.decodeFromString<GameState>(it) } catch (e: Exception) { GameState() }
        } ?: GameState()
    }

    override val gameHistory: Flow<GameHistory> = context.dataStore.data.map { preferences ->
        preferences[gameHistoryKey]?.let {
            try { Json.decodeFromString<GameHistory>(it) } catch (e: Exception) { GameHistory() }
        } ?: GameHistory()
    }

    override val appSettings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        preferences[appSettingsKey]?.let {
            try { Json.decodeFromString<AppSettings>(it) } catch (e: Exception) { AppSettings() }
        } ?: AppSettings()
    }

    override suspend fun saveGameState(gameState: GameState) {
        context.dataStore.edit { preferences ->
            preferences[gameStateKey] = Json.encodeToString(gameState)
        }
    }

    override suspend fun clearGameState() {
        context.dataStore.edit { preferences ->
            preferences.remove(gameStateKey)
        }
    }

    override suspend fun archiveCurrentGame(gameState: GameState) {
        val currentHistory = gameHistory.first()
        val currentSettings = appSettings.first()
        
        val updatedHistory = currentHistory.copy(
            pastGames = (listOf(gameState.copy(endTime = System.currentTimeMillis())) + currentHistory.pastGames).take(50)
        )
        
        val newGamesPlayedCount = currentSettings.gamesPlayedCount + 1
        val updatedSettings = currentSettings.copy(
            gamesPlayedCount = newGamesPlayedCount,
            showIdentityTip = if (newGamesPlayedCount >= 2) false else currentSettings.showIdentityTip
        )
        
        context.dataStore.edit { preferences ->
            preferences[gameHistoryKey] = Json.encodeToString(updatedHistory)
            preferences[appSettingsKey] = Json.encodeToString(updatedSettings)
        }
    }

    override suspend fun deleteGameFromHistory(gameId: String) {
        val currentHistory = gameHistory.first()
        val updatedHistory = currentHistory.copy(
            pastGames = currentHistory.pastGames.filter { it.id != gameId }
        )
        context.dataStore.edit { preferences ->
            preferences[gameHistoryKey] = Json.encodeToString(updatedHistory)
        }
    }

    override suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { preferences ->
            preferences[appSettingsKey] = Json.encodeToString(settings)
        }
    }

    override suspend fun getShareableData(gameId: String?): PocketScoreShare {
        val history = gameHistory.first()
        val settings = appSettings.first()
        
        return if (gameId == null) {
            PocketScoreShare(
                sourceDevice = settings.customDeviceName ?: Build.MODEL,
                friends = settings.savedPlayerNames,
                games = history.pastGames
            )
        } else {
            val game = history.pastGames.find { it.id == gameId }
            val friendsInGame = game?.players?.map { it.name } ?: emptyList()
            PocketScoreShare(
                sourceDevice = settings.customDeviceName ?: Build.MODEL,
                friends = friendsInGame,
                games = listOfNotNull(game)
            )
        }
    }

    override suspend fun mergeShareData(share: PocketScoreShare, playerNameMappings: Map<String, String>) {
        val currentHistory = gameHistory.first()
        val currentSettings = appSettings.first()

        // 1. Apply name mappings and propagate device info to incoming data
        val mappedGames = share.games.map { game ->
            game.copy(
                deviceInfo = game.deviceInfo ?: share.sourceDevice, // Propagate source device name
                players = game.players.map { player ->
                    val mappedName = playerNameMappings[player.name] ?: player.name
                    player.copy(name = mappedName)
                }
            )
        }
        
        val mappedFriends = share.friends.map { name ->
            playerNameMappings[name] ?: name
        }

        // 2. Smart merge games: avoid duplicates by ID
        val existingIds = currentHistory.pastGames.map { it.id }.toSet()
        val newGames = mappedGames.filter { it.id !in existingIds }
        
        val updatedHistory = currentHistory.copy(
            pastGames = (newGames + currentHistory.pastGames).take(100)
        )

        // 3. Smart merge friends: avoid duplicates by case-insensitive name
        val existingNames = currentSettings.savedPlayerNames.map { it.lowercase() }.toSet()
        val newFriends = mappedFriends.filter { it.lowercase() !in existingNames }
        
        val updatedSettings = currentSettings.copy(
            savedPlayerNames = (currentSettings.savedPlayerNames + newFriends).distinct().take(100)
        )

        context.dataStore.edit { preferences ->
            preferences[gameHistoryKey] = Json.encodeToString(updatedHistory)
            preferences[appSettingsKey] = Json.encodeToString(updatedSettings)
        }
    }

    override suspend fun renamePlayer(oldName: String, newName: String) {
        if (oldName == newName || newName.isBlank()) return
        
        val currentSettings = appSettings.first()
        val currentHistory = gameHistory.first()
        val currentGameState = gameState.first()

        // 1. Update Saved Player Names
        val updatedSavedNames = currentSettings.savedPlayerNames.map {
            if (it == oldName) newName else it
        }

        // 2. Update Game History records
        val updatedPastGames = currentHistory.pastGames.map { game ->
            game.copy(
                players = game.players.map { player ->
                    if (player.name == oldName) player.copy(name = newName) else player
                },
                globalEvents = game.globalEvents.map { event ->
                    if (event.playerName == oldName) event.copy(playerName = newName) else event
                }
            )
        }

        // 3. Update Active Game State (if any)
        val updatedActiveGameState = if (currentGameState.isGameActive) {
            currentGameState.copy(
                players = currentGameState.players.map { player ->
                    if (player.name == oldName) player.copy(name = newName) else player
                },
                globalEvents = currentGameState.globalEvents.map { event ->
                    if (event.playerName == oldName) event.copy(playerName = newName) else event
                }
            )
        } else currentGameState

        context.dataStore.edit { preferences ->
            preferences[appSettingsKey] = Json.encodeToString(currentSettings.copy(savedPlayerNames = updatedSavedNames))
            preferences[gameHistoryKey] = Json.encodeToString(currentHistory.copy(pastGames = updatedPastGames))
            if (currentGameState.isGameActive) {
                preferences[gameStateKey] = Json.encodeToString(updatedActiveGameState)
            }
        }
    }
    private val internalBackupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
    private val publicBackupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PocketScore").apply { 
        try { if (!exists()) mkdirs() } catch (e: Exception) { /* Scoped storage might block this on 11+ */ }
    }

    override suspend fun createLocalSnapshot(name: String) {
        val share = getShareableData(null)
        val json = Json.encodeToString(share)
        
        // 1. Save to Internal (Private)
        File(internalBackupDir, "$name.pscore").writeText(json)
        
        // 2. Try to save to Public (Survives Uninstall)
        try {
            if (publicBackupDir.exists() || publicBackupDir.mkdirs()) {
                File(publicBackupDir, "$name.pscore").writeText(json)
            }
        } catch (e: Exception) {
            // Silently fail public backup if scoped storage blocks it (Android 11+)
        }

        // Update settings with metadata for UI
        val sizeInKb = (json.length.toDouble() / 1024.0)
        val sizeStr = if (sizeInKb < 1024) String.format("%.1f KB", sizeInKb) else String.format("%.1f MB", sizeInKb / 1024.0)
        
        val currentSettings = appSettings.first()
        updateSettings(currentSettings.copy(
            lastLocalSnapshotTime = System.currentTimeMillis(),
            lastSnapshotSize = sizeStr
        ))
    }

    override suspend fun getLocalSnapshots(): List<Pair<String, Long>> {
        val internalList = internalBackupDir.listFiles { _, name -> name.endsWith(".pscore") }
            ?.map { it.nameWithoutExtension to it.lastModified() } ?: emptyList()
            
        val publicList = try {
            publicBackupDir.listFiles { _, name -> name.endsWith(".pscore") }
                ?.map { it.nameWithoutExtension to it.lastModified() } ?: emptyList()
        } catch (e: Exception) { emptyList() }
        
        // Merge and deduplicate by name, preferring the one with the latest timestamp
        return (internalList + publicList)
            .groupBy { it.first }
            .map { (name, stats) -> name to stats.maxOf { it.second } }
            .sortedByDescending { it.second }
    }

    override suspend fun restoreFromSnapshot(name: String) {
        getSnapshotContent(name)?.let { share ->
            mergeShareData(share)
        }
    }

    override suspend fun getSnapshotContent(name: String): PocketScoreShare? {
        // Try internal first, then public
        var file = File(internalBackupDir, "$name.pscore")
        if (!file.exists()) {
            file = File(publicBackupDir, "$name.pscore")
        }
        
        return if (file.exists()) {
            try {
                val jsonString = file.readText()
                Json.decodeFromString<PocketScoreShare>(jsonString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    override suspend fun deleteSnapshot(name: String) {
        File(internalBackupDir, "$name.pscore").let { if (it.exists()) it.delete() }
        try {
            File(publicBackupDir, "$name.pscore").let { if (it.exists()) it.delete() }
        } catch (e: Exception) { }
    }

    override suspend fun triggerDailyAutoSnapshot() {
        val settings = appSettings.first()
        if (!settings.localSnapshotsEnabled) return

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (settings.lastAutoSnapshotDate == today) return

        // Create the snapshot first
        createLocalSnapshot("Daily-Auto-$today")
        
        // Then update the auto-snapshot date specifically
        val updatedSettings = appSettings.first()
        updateSettings(updatedSettings.copy(lastAutoSnapshotDate = today))
    }

    override suspend fun triggerCloudBackup() {
        // TODO: Re-enable cloud backup later. Current focus is on local snapshots only.
        /*
        try {
            android.app.backup.BackupManager(context).dataChanged()
        } catch (e: Exception) { }
        */
    }
}
