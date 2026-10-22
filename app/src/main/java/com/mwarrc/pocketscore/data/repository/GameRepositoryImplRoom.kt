package com.mwarrc.pocketscore.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mwarrc.pocketscore.data.local.room.AppDatabase
import androidx.room.withTransaction
import com.mwarrc.pocketscore.data.local.room.entity.GameEntity
import com.mwarrc.pocketscore.data.local.room.entity.GameEventEntity
import com.mwarrc.pocketscore.data.local.room.entity.GameWithPlayers
import com.mwarrc.pocketscore.data.local.room.entity.GameWithPlayersAndEvents
import com.mwarrc.pocketscore.data.local.room.entity.PlayerEntity
import com.mwarrc.pocketscore.data.local.room.entity.SavedPlayerEntity
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

import com.mwarrc.pocketscore.data.local.di.DataStoreModule
private val Context.dataStore: DataStore<Preferences> get() = DataStoreModule.getDataStore(this)

/**
 * High-performance Room-based implementation of GameRepository.
 * This is the professional, scalable solution that avoids the blob duplication issues
 * by storing data in normalized SQLite tables.
 */
class GameRepositoryImplRoom(
    private val context: Context,
    private val database: AppDatabase
) : GameRepository {
    
    private companion object {
        const val TAG = "GameRepositoryImplRoom"
        const val GAMES_TO_HIDE_TIP = 5
    }
    
    private val appSettingsKey = stringPreferencesKey("app_settings")
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }
    
    // Game state - current active game
    override val gameState: Flow<GameState> = try {
        database.gameDao().getActiveGame()
            .map { gameWithDetails ->
                try {
                    val domain = gameWithDetails?.toDomainGameState()
                    if (domain != null && domain.players.isNotEmpty()) {
                        domain
                    } else {
                        GameState()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping game state", e)
                    GameState()
                }
            }
    } catch (e: Exception) {
        Log.e(TAG, "Error creating gameState flow", e)
        kotlinx.coroutines.flow.flowOf(GameState())
    }
    
    // Game history - all games including paused sessions (lightweight, loads without thousands of events)
    // Paused games are now included in history so users can resume them from the Records screen.
    override val gameHistory: Flow<GameHistory> = try {
        database.gameDao().getAllGamesWithPlayers()
            .map { gamesWithPlayers ->
                try {
                    val pastGames = gamesWithPlayers
                        .map { it.toDomainGameState() }
                        .filter { it.players.isNotEmpty() }
                        .sortedByDescending { it.lastUpdate }
                    GameHistory(pastGames = pastGames)
                } catch (e: Exception) {
                    Log.e(TAG, "Error mapping game history", e)
                    GameHistory()
                }
            }
    } catch (e: Exception) {
        Log.e(TAG, "Error creating gameHistory flow", e)
        kotlinx.coroutines.flow.flowOf(GameHistory())
    }
    
    // Settings (still stored in DataStore for simplicity and atomic updates)
    override val appSettings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        preferences[appSettingsKey]?.let { jsonString ->
            try {
                json.decodeFromString<AppSettings>(jsonString).validate()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode app settings", e)
                AppSettings()
            }
        } ?: AppSettings()
    }
    
    override suspend fun saveGameState(gameState: GameState) {
        if (gameState.players.isEmpty()) {
            Log.w(TAG, "Skipping save of game with no players")
            return
        }
        try {
            val gameWithDetails = gameState.toGameWithDetails()
            database.gameDao().saveFullGame(gameWithDetails)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save game state", e)
            throw e
        }
    }
    
    override suspend fun clearGameState() {
        try {
            val activeGame = database.gameDao().getActiveGame().first()
            activeGame?.game?.let {
                database.gameDao().deleteFullGame(it.id)
            }
            database.gameDao().deleteGhostGamesWithNoPlayers()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear game state", e)
            throw e
        }
    }
    
    override suspend fun archiveCurrentGame(gameState: GameState, saveOverride: Boolean, isPause: Boolean) {
        if (gameState.players.isEmpty()) {
            Log.w(TAG, "Skipping archive of game with no players")
            return
        }
        try {
            val currentSettings = appSettings.first()
            
            // Save the record when:
            //  - the caller explicitly forces a save (saveOverride), OR
            //  - this is a pause (always preserve paused sessions), OR
            //  - we're NOT in guest mode, or guest mode has guestSaveRecords on
            val shouldSaveRecord = saveOverride ||
                isPause ||
                (!currentSettings.isGuestSession || currentSettings.guestSaveRecords)
            
            if (shouldSaveRecord) {
                val archivedGame = gameState.copy(
                    endTime = if (gameState.endTime == null) System.currentTimeMillis() else gameState.endTime,
                    isGameActive = false,
                    isPaused = isPause,
                    isFinalized = !isPause, // Don't finalize if pausing
                    // Do NOT force isArchived = true here. A finished game is a
                    // completed record and belongs in the main Records list, not
                    // the Archive tab. isArchived should only be set when the user
                    // explicitly archives from the history screen. The DAO active-
                    // game query already filters on isGameActive / isPaused, so
                    // isArchived is not needed to hide finished games from it.
                    isArchived = gameState.isArchived
                )
                val gameWithDetails = archivedGame.toGameWithDetails()
                database.gameDao().saveFullGame(gameWithDetails)
            } else {
                // If not saving, just delete active game
                database.gameDao().deleteFullGame(gameState.id)
            }
            
            // Only increment games played count for finalized games, not pauses
            if (!isPause) {
                val newGamesPlayedCount = currentSettings.gamesPlayedCount + 1
                val updatedSettings = currentSettings.copy(
                    gamesPlayedCount = newGamesPlayedCount,
                    showIdentityTip = newGamesPlayedCount < GAMES_TO_HIDE_TIP
                )
                updateSettings(updatedSettings)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to archive game", e)
            throw e
        }
    }
    
    override suspend fun deleteGameFromHistory(gameId: String) {
        try {
            database.gameDao().deleteFullGame(gameId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete game from history", e)
            throw e
        }
    }
    
    override suspend fun deleteGamesFromHistory(gameIds: List<String>) {
        if (gameIds.isEmpty()) return
        try {
            database.gameDao().deleteGamesByIds(gameIds)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete games from history", e)
            throw e
        }
    }

    override fun getGameWithDetails(gameId: String): Flow<GameState?> {
        return database.gameDao().getGameWithDetails(gameId).map { it?.toDomainGameState() }
    }

    override suspend fun getGameWithDetailsOnce(gameId: String): GameState? {
        return database.gameDao().getGameWithDetailsOnce(gameId)?.toDomainGameState()
    }
    
    override suspend fun updateGameInHistory(game: GameState) {
        try {
            val gameWithDetails = game.toGameWithDetails()
            database.gameDao().saveFullGame(gameWithDetails)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update game in history", e)
            throw e
        }
    }
    
    override suspend fun updateSettings(settings: AppSettings) {
        try {
            val validatedSettings = settings.validate()
            context.dataStore.edit { preferences ->
                preferences[appSettingsKey] = json.encodeToString(validatedSettings)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update settings", e)
            throw e
        }
    }
    
    override suspend fun getShareableData(gameIds: List<String>?): PocketScoreShare {
        try {
            val settings = appSettings.first()
            
            return if (gameIds == null) {
                val allGames = database.gameDao().getAllGamesWithDetails().first()
                val pastGames = allGames.map { it.toDomainGameState() }
                val friends = database.savedPlayerDao().getSavedPlayerNames()
                PocketScoreShare(
                    sourceDevice = settings.customDeviceName ?: Build.MODEL,
                    friends = friends,
                    games = pastGames
                )
            } else {
                val games = mutableListOf<GameState>()
                gameIds.forEach { id ->
                    database.gameDao().getGameWithDetailsOnce(id)?.let {
                        games.add(it.toDomainGameState())
                    }
                }
                val friendsInGames = games
                    .flatMap { it.players.map { p -> p.name } }
                    .distinct()
                PocketScoreShare(
                    sourceDevice = settings.customDeviceName ?: Build.MODEL,
                    friends = friendsInGames,
                    games = games
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get shareable data", e)
            throw e
        }
    }
    
    override suspend fun mergeShareData(
        share: PocketScoreShare,
        playerNameMappings: Map<String, String>
    ) {
        try {
            val normalizedMappings = playerNameMappings.mapKeys { it.key.trim().lowercase() }
            
            fun getMappedName(rawName: String): String {
                val trimmed = rawName.trim()
                return normalizedMappings[trimmed.lowercase()] ?: trimmed
            }
            
            // FORCE all imported games inactive and finalized — regardless of source data
            val mappedGames = share.games.map { game ->
                game.copy(
                    deviceInfo = game.deviceInfo ?: share.sourceDevice,
                    isGameActive = false,
                    isFinalized = true,
                    isArchived = false,
                    players = game.players.map { player ->
                        player.copy(name = getMappedName(player.name))
                    },
                    globalEvents = game.globalEvents.map { event ->
                        event.copy(playerName = getMappedName(event.playerName))
                    }
                )
            }
            
            val mappedFriends = share.friends.map { getMappedName(it) }
            
            // Capture the user's current active game BEFORE import so we can preserve it
            val currentActiveGame = database.gameDao().getActiveGame().first()
            val currentActiveGameId = currentActiveGame?.game?.id
            
            // Wrap entire import in a single transaction so the Room Flow
            // doesn't emit intermediate states (prevents the "open 500 games" bug)
            database.withTransaction {
                // Insert games — all forced inactive
                mappedGames.forEach { gameState ->
                    val existing = database.gameDao().getGameWithDetailsOnce(gameState.id)
                    if (existing == null) {
                        val safeGame = gameState.copy(isGameActive = false, isFinalized = true, isArchived = false)
                        database.gameDao().saveFullGame(safeGame.toGameWithDetails())
                    } else {
                        // If exists, ensure it's not active — preserve all other fields
                        val updated = existing.game.copy(isGameActive = false, isFinalized = true)
                        database.gameDao().saveFullGame(existing.copy(game = updated))
                    }
                }
                
                // Merge saved players (inside same transaction)
                val existingNames = database.savedPlayerDao().getSavedPlayerNames()
                val existingNamesLower = existingNames.map { it.lowercase() }.toSet()
                val newFriends = mappedFriends.filter { it.lowercase() !in existingNamesLower }
                val savedEntities = newFriends.distinct().map { name ->
                    SavedPlayerEntity(
                        id = UUID.nameUUIDFromBytes("saved_$name".toByteArray()).toString(),
                        name = name,
                        lastUsed = System.currentTimeMillis(),
                        useCount = 1
                    )
                }
                if (savedEntities.isNotEmpty()) {
                    database.savedPlayerDao().insertSavedPlayers(savedEntities)
                }
                
                // Safety net: deactivate ALL games except the user's prior active game
                if (currentActiveGameId != null) {
                    database.gameDao().deactivateAllGamesExcept(currentActiveGameId)
                } else {
                    database.gameDao().deactivateAllGames()
                }
            }

            // The home roster is driven by AppSettings.savedPlayerNames, NOT by the
            // saved_players table. Without this sync imported players never show up
            // on the home screen even though their games were imported successfully.
            if (mappedFriends.isNotEmpty()) {
                val currentSettings = appSettings.first()
                val existingSavedLower = currentSettings.savedPlayerNames
                    .map { it.lowercase() }
                    .toSet()
                val friendsToAdd = mappedFriends
                    .distinct()
                    .filter { it.lowercase() !in existingSavedLower }
                if (friendsToAdd.isNotEmpty()) {
                    updateSettings(
                        currentSettings.copy(
                            savedPlayerNames = currentSettings.savedPlayerNames + friendsToAdd
                        )
                    )
                }
            }
            
            Log.d(TAG, "Merged ${mappedGames.size} games and ${mappedFriends.size} players (transaction committed)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to merge share data", e)
            throw e
        }
    }
    
    override suspend fun renamePlayer(oldName: String, newName: String) {
        if (oldName == newName || newName.isBlank()) return
        try {
            val oldTrimmed = oldName.trim()
            val newTrimmed = newName.trim()
            
            // Execute atomic update queries across players and events in SQLite
            database.gameDao().renamePlayerInPlayers(oldTrimmed, newTrimmed)
            database.gameDao().renamePlayerInEvents(oldTrimmed, newTrimmed)
            
            val existing = database.savedPlayerDao().getSavedPlayerNames()
            if (existing.contains(oldTrimmed)) {
                database.savedPlayerDao().deleteSavedPlayer(oldTrimmed)
                database.savedPlayerDao().insertSavedPlayer(
                    SavedPlayerEntity(
                        id = UUID.nameUUIDFromBytes("saved_$newTrimmed".toByteArray()).toString(),
                        name = newTrimmed,
                        lastUsed = System.currentTimeMillis(),
                        useCount = 1
                    )
                )
            }
            
            // Keep saved players in appSettings DataStore synchronized
            val currentSettings = appSettings.first()
            if (currentSettings.savedPlayerNames.contains(oldTrimmed)) {
                val updatedNames = currentSettings.savedPlayerNames.map {
                    if (it == oldTrimmed) newTrimmed else it
                }
                updateSettings(currentSettings.copy(savedPlayerNames = updatedNames))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rename player", e)
            throw e
        }
    }
    
    // Snapshot methods - keep same implementation as before for compatibility
    private val internalBackupDir: File by lazy {
        val dir = context.getExternalFilesDir("backups") 
            ?: File(context.filesDir, "backups")
        if (!dir.exists()) dir.mkdirs()
        dir
    }
    
    private val publicBackupDir: File by lazy {
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) 
            ?: File(context.filesDir, "Documents")
    }
    
    override suspend fun createLocalSnapshot(name: String) {
        try {
            val share = getShareableData(null)
            val jsonString = json.encodeToString(share)
            File(internalBackupDir, "$name.pscore").writeText(jsonString)
            saveToExternalVaultIfLinked(name, jsonString)
            updateSnapshotMetadata(jsonString.length)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create snapshot", e)
            throw e
        }
    }
    
    private suspend fun saveToExternalVaultIfLinked(name: String, jsonString: String) {
        val settings = appSettings.first()
        settings.backupsFolderUri?.let { uriString ->
            try {
                val folderUri = Uri.parse(uriString)
                val pickedDir = DocumentFile.fromTreeUri(context, folderUri)
                if (pickedDir != null && pickedDir.exists() && pickedDir.canWrite()) {
                    val pscoreFile = pickedDir.findFile("$name.pscore") 
                        ?: pickedDir.createFile("application/octet-stream", "$name.pscore")
                    pscoreFile?.uri?.let { fileUri ->
                        context.contentResolver.openOutputStream(fileUri)?.use { output ->
                            output.write(jsonString.toByteArray())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save snapshot to linked vault", e)
            }
        }
    }
    
    private suspend fun updateSnapshotMetadata(jsonLength: Int) {
        try {
            val currentSettings = appSettings.first()
            val sizeInKb = jsonLength.toDouble() / 1024.0
            val sizeStr = if (sizeInKb < 1024) {
                String.format(Locale.US, "%.1f KB", sizeInKb)
            } else {
                String.format(Locale.US, "%.1f MB", sizeInKb / 1024.0)
            }
            updateSettings(currentSettings.copy(
                lastLocalSnapshotTime = System.currentTimeMillis(),
                lastSnapshotSize = sizeStr
            ))
        } catch (e: Exception) { }
    }
    
    /**
     * Copies an internal snapshot into the public documents folder.
     *
     * This used to be `= true` — a stub that reported success without writing
     * anything, so any UI surfacing the result claimed an export that never
     * happened. Now it mirrors the DataStore implementation: verify the source
     * exists, copy it, and return false on any failure so callers can tell the
     * truth.
     */
    override suspend fun exportSnapshotToPublic(name: String): Boolean {
        return try {
            val sourceFile = File(internalBackupDir, "$name.pscore")
            if (!sourceFile.exists()) {
                Log.w(TAG, "Snapshot not found for export: $name")
                return false
            }

            if (!publicBackupDir.exists()) publicBackupDir.mkdirs()

            val destFile = File(publicBackupDir, "$name.pscore")
            sourceFile.copyTo(destFile, overwrite = true)

            Log.d(TAG, "Exported snapshot to public storage: $name")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to export snapshot", e)
            false
        }
    }
    override suspend fun getLocalSnapshots(): List<Pair<String, Long>> {
        // Delegate to a simplified implementation or use DataStore settings
        val snapshots = mutableMapOf<String, Long>()
        val settings = appSettings.first()
        listSnapshotsInDir(internalBackupDir).forEach { snapshots[it.first] = it.second }
        settings.backupsFolderUri?.let { uriString ->
            try {
                val folderUri = Uri.parse(uriString)
                val pickedDir = DocumentFile.fromTreeUri(context, folderUri)
                pickedDir?.listFiles()?.forEach { file ->
                    val fileName = file.name
                    if (fileName != null && fileName.endsWith(".pscore")) {
                        val nameSnap = fileName.removeSuffix(".pscore")
                        val date = file.lastModified()
                        if (date > (snapshots[nameSnap] ?: 0L)) snapshots[nameSnap] = date
                    }
                }
            } catch (e: Exception) { }
        }
        return snapshots.toList().sortedByDescending { it.second }
    }
    
    private fun listSnapshotsInDir(dir: File): List<Pair<String, Long>> {
        return try {
            if (!dir.exists()) return emptyList()
            dir.listFiles { _, name -> name.endsWith(".pscore") }
                ?.map { it.nameWithoutExtension to it.lastModified() }
                ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }
    
    override suspend fun restoreFromSnapshot(name: String) {
        try {
            getSnapshotContent(name)?.let { share -> mergeShareData(share) }
        } catch (e: Exception) { throw e }
    }
    
    override suspend fun getSnapshotContent(name: String): PocketScoreShare? {
        val settings = appSettings.first()
        val internalFile = File(internalBackupDir, "$name.pscore")
        if (internalFile.exists()) {
            return try { json.decodeFromString<PocketScoreShare>(internalFile.readText()) } catch (e: Exception) { null }
        }
        settings.backupsFolderUri?.let { uriString ->
            try {
                val folderUri = Uri.parse(uriString)
                val pickedDir = DocumentFile.fromTreeUri(context, folderUri)
                val pscoreFile = pickedDir?.findFile("$name.pscore")
                pscoreFile?.uri?.let { fileUri ->
                    context.contentResolver.openInputStream(fileUri)?.use { input ->
                        val jsonString = input.bufferedReader().use { it.readText() }
                        return json.decodeFromString<PocketScoreShare>(jsonString)
                    }
                }
            } catch (e: Exception) { }
        }
        return null
    }
    
    override suspend fun deleteSnapshot(name: String) {
        val settings = appSettings.first()
        try {
            val file = File(internalBackupDir, "$name.pscore")
            if (file.exists()) file.delete()
        } catch (e: Exception) { }
        settings.backupsFolderUri?.let { uriString ->
            try {
                val folderUri = Uri.parse(uriString)
                val pickedDir = DocumentFile.fromTreeUri(context, folderUri)
                pickedDir?.findFile("$name.pscore")?.delete()
            } catch (e: Exception) { }
        }
    }
    
    override suspend fun triggerDailyAutoSnapshot() {
        try {
            val settings = appSettings.first()
            if (!settings.localSnapshotsEnabled) return
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            if (settings.lastAutoSnapshotDate == today) return
            createLocalSnapshot("Daily-Auto-$today")
            pruneOldDailySnapshots()
            updateSettings(settings.copy(lastAutoSnapshotDate = today))
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering daily auto-snapshot", e)
        }
    }

    private fun pruneOldDailySnapshots() {
        try {
            val dailyFiles = internalBackupDir.listFiles { _, name -> 
                name.startsWith("Daily-Auto-") && name.endsWith(".pscore") 
            }
            if (dailyFiles != null && dailyFiles.size > 7) {
                dailyFiles.sortedBy { it.lastModified() }
                    .take(dailyFiles.size - 7)
                    .forEach { it.delete() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to prune old daily snapshots", e)
        }
    }
    
    override suspend fun triggerCloudBackup() {
        Log.d(TAG, "Cloud backup not yet implemented")
    }
    
    override suspend fun updateBackupsFolderUri(uri: String?) {
        updateSettings(appSettings.first().copy(backupsFolderUri = uri))
    }
}

// Mappers
private fun GameWithPlayersAndEvents.toDomainGameState(): GameState {
    return GameState(
        id = game.id,
        players = players.map { playerEntity ->
            Player(
                id = playerEntity.id,
                name = playerEntity.name,
                score = playerEntity.score,
                isActive = playerEntity.isActive
            )
        },
        isGameActive = game.isGameActive,
        isPaused = game.isPaused,
        startTime = game.startTime,
        endTime = game.endTime,
        lastUpdate = game.lastUpdate,
        currentPlayerId = game.currentPlayerId,
        globalEvents = events.map { eventEntity ->
            GameEvent(
                id = eventEntity.id,
                playerId = eventEntity.playerId ?: "",
                playerName = eventEntity.playerName,
                type = eventEntity.type,
                timestamp = eventEntity.timestamp,
                points = eventEntity.points,
                previousPlayerId = eventEntity.previousPlayerId,
                isZeroInput = eventEntity.isZeroInput,
                message = eventEntity.details.ifEmpty { null },
                previousScore = eventEntity.previousScore,
                newScore = eventEntity.newScore,
                autoRemovedBall = eventEntity.autoRemovedBall
            )
        },
        canUndo = game.canUndo,
        isFinalized = game.isFinalized,
        ballsOnTable = game.ballsOnTable,
        deviceInfo = game.deviceInfo,
        isArchived = game.isArchived
    )
}

private fun GameWithPlayers.toDomainGameState(): GameState {
    return GameState(
        id = game.id,
        players = players.map { playerEntity ->
            Player(
                id = playerEntity.id,
                name = playerEntity.name,
                score = playerEntity.score,
                isActive = playerEntity.isActive
            )
        },
        isGameActive = game.isGameActive,
        isPaused = game.isPaused,
        startTime = game.startTime,
        endTime = game.endTime,
        lastUpdate = game.lastUpdate,
        currentPlayerId = game.currentPlayerId,
        globalEvents = emptyList(),
        canUndo = game.canUndo,
        isFinalized = game.isFinalized,
        ballsOnTable = game.ballsOnTable,
        deviceInfo = game.deviceInfo,
        isArchived = game.isArchived
    )
}

private fun GameState.toGameWithDetails(): GameWithPlayersAndEvents {
    val gameEntity = GameEntity(
        id = id,
        isGameActive = isGameActive,
        isPaused = isPaused,
        startTime = startTime,
        endTime = endTime,
        lastUpdate = lastUpdate,
        currentPlayerId = currentPlayerId,
        canUndo = canUndo,
        isFinalized = isFinalized,
        ballsOnTable = ballsOnTable,
        deviceInfo = deviceInfo,
        isArchived = isArchived
    )
    val playerEntities = players.map { player ->
        PlayerEntity(
            id = player.id,
            gameId = id,
            name = player.name,
            score = player.score,
            isActive = player.isActive
        )
    }
    val eventEntities = globalEvents.map { event ->
        GameEventEntity(
            id = event.id,
            gameId = id,
            playerId = event.playerId,
            playerName = event.playerName,
            type = event.type,
            timestamp = event.timestamp,
            points = event.points,
            previousPlayerId = event.previousPlayerId,
            isZeroInput = event.isZeroInput,
            details = event.message ?: "",
            previousScore = event.previousScore,
            newScore = event.newScore,
            autoRemovedBall = event.autoRemovedBall
        )
    }
    return GameWithPlayersAndEvents(
        game = gameEntity,
        players = playerEntities,
        events = eventEntities
    )
}
