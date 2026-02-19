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
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

/**
 * Implementation of GameRepository using DataStore for persistence.
 * 
 * Features:
 * - Reactive data flows using DataStore
 * - JSON serialization for complex objects
 * - Multi-location snapshot backups
 * - Smart data merging with deduplication
 * - Guest mode support
 * 
 * @property context Application context
 */
class GameRepositoryImpl(private val context: Context) : GameRepository {

    private companion object {
        const val TAG = "GameRepositoryImpl"
        const val MAX_HISTORY_SIZE = 50
        const val MAX_MERGED_HISTORY_SIZE = 100
        const val MAX_SAVED_PLAYERS = 100
        const val GAMES_TO_HIDE_TIP = 2
    }

    private val gameStateKey = stringPreferencesKey("game_state")
    private val gameHistoryKey = stringPreferencesKey("game_history")
    private val appSettingsKey = stringPreferencesKey("app_settings")

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    override val gameState: Flow<GameState> = context.dataStore.data.map { preferences ->
        preferences[gameStateKey]?.let { jsonString ->
            try {
                json.decodeFromString<GameState>(jsonString)
            } catch (e: SerializationException) {
                Log.e(TAG, "Failed to decode game state", e)
                GameState()
            }
        } ?: GameState()
    }

    override val gameHistory: Flow<GameHistory> = context.dataStore.data.map { preferences ->
        preferences[gameHistoryKey]?.let { jsonString ->
            try {
                json.decodeFromString<GameHistory>(jsonString)
            } catch (e: SerializationException) {
                Log.e(TAG, "Failed to decode game history", e)
                GameHistory()
            }
        } ?: GameHistory()
    }

    override val appSettings: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        preferences[appSettingsKey]?.let { jsonString ->
            try {
                json.decodeFromString<AppSettings>(jsonString)
            } catch (e: SerializationException) {
                Log.e(TAG, "Failed to decode app settings", e)
                AppSettings()
            }
        } ?: AppSettings()
    }

    override suspend fun saveGameState(gameState: GameState) {
        try {
            context.dataStore.edit { preferences ->
                preferences[gameStateKey] = json.encodeToString(gameState)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save game state", e)
            throw e
        }
    }

    override suspend fun clearGameState() {
        try {
            context.dataStore.edit { preferences ->
                preferences.remove(gameStateKey)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear game state", e)
            throw e
        }
    }

    override suspend fun archiveCurrentGame(gameState: GameState, saveOverride: Boolean) {
        try {
            val currentHistory = gameHistory.first()
            val currentSettings = appSettings.first()
            
            // Determine if we should save this game
            val shouldSaveRecord = saveOverride ||
                !gameState.isFinalized ||
                (!currentSettings.isGuestSession || currentSettings.guestSaveRecords)
            
            val updatedHistory = if (shouldSaveRecord) {
                val archivedGame = gameState.copy(endTime = System.currentTimeMillis())
                currentHistory.copy(
                    pastGames = (listOf(archivedGame) + currentHistory.pastGames.filter { it.id != archivedGame.id })
                        .take(MAX_HISTORY_SIZE)
                )
            } else {
                currentHistory
            }
            
            val newGamesPlayedCount = currentSettings.gamesPlayedCount + 1
            val updatedSettings = currentSettings.copy(
                gamesPlayedCount = newGamesPlayedCount,
                showIdentityTip = newGamesPlayedCount < GAMES_TO_HIDE_TIP
            )
            
            context.dataStore.edit { preferences ->
                if (shouldSaveRecord) {
                    preferences[gameHistoryKey] = json.encodeToString(updatedHistory)
                }
                preferences[appSettingsKey] = json.encodeToString(updatedSettings)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to archive game", e)
            throw e
        }
    }

    override suspend fun deleteGameFromHistory(gameId: String) {
        try {
            val currentHistory = gameHistory.first()
            val updatedHistory = currentHistory.copy(
                pastGames = currentHistory.pastGames.filter { it.id != gameId }
            )
            
            context.dataStore.edit { preferences ->
                preferences[gameHistoryKey] = json.encodeToString(updatedHistory)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete game from history", e)
            throw e
        }
    }

    override suspend fun updateGameInHistory(game: GameState) {
        try {
            val currentHistory = gameHistory.first()
            val updatedHistory = currentHistory.copy(
                pastGames = currentHistory.pastGames.map { 
                    if (it.id == game.id) game else it
                }
            )
            
            context.dataStore.edit { preferences ->
                preferences[gameHistoryKey] = json.encodeToString(updatedHistory)
            }
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
            val history = gameHistory.first()
            val settings = appSettings.first()
            
            return if (gameIds == null) {
                // Full backup
                PocketScoreShare(
                    sourceDevice = settings.customDeviceName ?: Build.MODEL,
                    friends = settings.savedPlayerNames,
                    games = history.pastGames
                )
            } else {
                // Specific games
                val games = history.pastGames.filter { it.id in gameIds }
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
            val currentHistory = gameHistory.first()
            val currentSettings = appSettings.first()

            // Apply name mappings and propagate device info
            val mappedGames = share.games.map { game ->
                game.copy(
                    deviceInfo = game.deviceInfo ?: share.sourceDevice,
                    players = game.players.map { player ->
                        val mappedName = playerNameMappings[player.name] ?: player.name
                        player.copy(name = mappedName)
                    },
                    globalEvents = game.globalEvents.map { event ->
                        val mappedName = playerNameMappings[event.playerName] ?: event.playerName
                        event.copy(playerName = mappedName)
                    }
                )
            }
            
            val mappedFriends = share.friends.map { name ->
                playerNameMappings[name] ?: name
            }

            // Smart merge games: avoid duplicates by ID
            val existingIds = currentHistory.pastGames.map { it.id }.toSet()
            val newGames = mappedGames.filter { it.id !in existingIds }
            
            val updatedHistory = currentHistory.copy(
                pastGames = (newGames + currentHistory.pastGames)
                    .take(MAX_MERGED_HISTORY_SIZE)
            )

            // Smart merge friends: avoid duplicates (case-insensitive)
            val existingNamesLower = currentSettings.savedPlayerNames
                .map { it.lowercase() }
                .toSet()
            val newFriends = mappedFriends.filter { 
                it.lowercase() !in existingNamesLower 
            }
            
            val updatedSettings = currentSettings.copy(
                savedPlayerNames = (currentSettings.savedPlayerNames + newFriends)
                    .distinct()
                    .take(MAX_SAVED_PLAYERS)
            )

            context.dataStore.edit { preferences ->
                preferences[gameHistoryKey] = json.encodeToString(updatedHistory)
                preferences[appSettingsKey] = json.encodeToString(updatedSettings)
            }
            
            Log.d(TAG, "Merged ${newGames.size} games and ${newFriends.size} players")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to merge share data", e)
            throw e
        }
    }

    override suspend fun renamePlayer(oldName: String, newName: String) {
        if (oldName == newName || newName.isBlank()) {
            Log.w(TAG, "Invalid rename: old='$oldName', new='$newName'")
            return
        }
        
        try {
            val currentSettings = appSettings.first()
            val currentHistory = gameHistory.first()
            val currentGameState = gameState.first()

            // Update saved player names
            val updatedSavedNames = currentSettings.savedPlayerNames.map {
                if (it == oldName) newName else it
            }

            // Update game history records
            val updatedPastGames = currentHistory.pastGames.map { game ->
                game.copy(
                    players = game.players.map { player ->
                        if (player.name == oldName) player.copy(name = newName) else player
                    },
                    globalEvents = game.globalEvents.map { event ->
                        if (event.playerName == oldName) {
                            event.copy(playerName = newName)
                        } else {
                            event
                        }
                    }
                )
            }

            // Update active game state
            val updatedActiveGameState = if (currentGameState.isGameActive) {
                currentGameState.copy(
                    players = currentGameState.players.map { player ->
                        if (player.name == oldName) player.copy(name = newName) else player
                    },
                    globalEvents = currentGameState.globalEvents.map { event ->
                        if (event.playerName == oldName) {
                            event.copy(playerName = newName)
                        } else {
                            event
                        }
                    }
                )
            } else {
                currentGameState
            }

            context.dataStore.edit { preferences ->
                preferences[appSettingsKey] = json.encodeToString(
                    currentSettings.copy(savedPlayerNames = updatedSavedNames)
                )
                preferences[gameHistoryKey] = json.encodeToString(
                    currentHistory.copy(pastGames = updatedPastGames)
                )
                if (currentGameState.isGameActive) {
                    preferences[gameStateKey] = json.encodeToString(updatedActiveGameState)
                }
            }
            
            Log.d(TAG, "Renamed player: '$oldName' -> '$newName'")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rename player", e)
            throw e
        }
    }
    
    // Backup directories
    
    private val internalBackupDir: File by lazy {
        val dir = context.getExternalFilesDir("backups") 
            ?: File(context.filesDir, "backups")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    
    private val publicBackupDir: File by lazy {
        // Use app-private external documents if available, else internal documents
        context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) 
            ?: File(context.filesDir, "Documents")
    }

    private val syncBackupDir: File by lazy {
        // Redundant sync now mirrors to another app folder
        File(context.getExternalFilesDir(null), "sync/games").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun createLocalSnapshot(name: String) {
        try {
            val share = getShareableData(null)
            val jsonString = json.encodeToString(share)
            
            // 1. Internal Private (Always)
            File(internalBackupDir, "$name.pscore").writeText(jsonString)
            
            // 2. Linked Folder / Documents (User Accessible)
            saveToPublicStorage(name, jsonString)
            
            // 3. Android Sync (Survives until uninstall)
            saveToSyncStorage(name, jsonString)
            
            // 4. Auto-Download (Background Download System)
            saveToDownloadsStorage(name, jsonString)
            
            updateSnapshotMetadata(jsonString.length)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create snapshot", e)
            throw e
        }
    }

    private suspend fun saveToPublicStorage(name: String, jsonString: String) {
        val settings = appSettings.first()
        
        // 1. Try saved persistent folder first (if linked)
        settings.backupsFolderUri?.let { uriString ->
            try {
                val folderUri = Uri.parse(uriString)
                val pickedDir = DocumentFile.fromTreeUri(context, folderUri)
                if (pickedDir != null && pickedDir.exists() && pickedDir.canWrite()) {
                    val pscoreFile = pickedDir.findFile("$name.pscore") ?: pickedDir.createFile("application/octet-stream", "$name.pscore")
                    pscoreFile?.uri?.let { fileUri ->
                        context.contentResolver.openOutputStream(fileUri)?.use { output ->
                            output.write(jsonString.toByteArray())
                        }
                        Log.d(TAG, "Saved to persistent folder: $name")
                        return
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save to persistent folder: $uriString", e)
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // Legacy approach
            try {
                val publicDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "Backups/PocketScore/Games"
                )
                if (!publicDir.exists()) publicDir.mkdirs()
                File(publicDir, "$name.pscore").writeText(jsonString)
                Log.d(TAG, "Saved legacy snapshot: $name")
            } catch (e: Exception) {
                Log.w(TAG, "Legacy public save failed", e)
            }
            return
        }

        // 2. Modern MediaStore approach (fallback)
        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.pscore")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/Backups/PocketScore/Games")
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                Log.d(TAG, "Saved MediaStore snapshot: $name")
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore save failed", e)
        }
    }

    private suspend fun saveToSyncStorage(name: String, jsonString: String) {
        // Android Sync: Lives in Android/data/com.mwarrc.pocketscore/files/sync
        // Survives as long as the app is installed. Very safe from user deletion.
        try {
            val syncDir = File(context.getExternalFilesDir(null), "sync/games")
            if (!syncDir.exists()) syncDir.mkdirs()
            File(syncDir, "$name.pscore").writeText(jsonString)
            Log.d(TAG, "Synced to Android folder: $name")
        } catch (e: Exception) {
            Log.e(TAG, "Sync to Android folder failed", e)
        }
    }

    private suspend fun saveToDownloadsStorage(name: String, jsonString: String) {
        // Auto-Download: Acts like a browser download, saving to Downloads/PocketScore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.pscore")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PocketScore")
                }
                val uri = resolver.insert(MediaStore.Downloads.getContentUri("external"), contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { out ->
                        out.write(jsonString.toByteArray())
                    }
                    Log.d(TAG, "Auto-downloaded to Downloads folder: $name")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Auto-download failed", e)
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update snapshot metadata", e)
        }
    }

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
        val snapshots = mutableMapOf<String, Long>()
        val settings = appSettings.first()
        
        // 1. Scan internal private backups
        listSnapshotsInDir(internalBackupDir).forEach { 
            snapshots[it.first] = it.second 
        }

        // 2. Scan Persistent Linked Folder (if available)
        settings.backupsFolderUri?.let { uriString ->
            try {
                val folderUri = Uri.parse(uriString)
                val pickedDir = DocumentFile.fromTreeUri(context, folderUri)
                pickedDir?.listFiles()?.forEach { file ->
                    val fileName = file.name
                    if (fileName != null && fileName.endsWith(".pscore")) {
                        val name = fileName.removeSuffix(".pscore")
                        val date = file.lastModified()
                        if (date > (snapshots[name] ?: 0L)) {
                            snapshots[name] = date
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Persistent folder scan failed", e)
            }
        }
        
        // 3. Scan MediaStore (Public Documents) as fallback/supplement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val projection = arrayOf(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.DATE_MODIFIED
                )
                val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
                val selectionArgs = arrayOf("${Environment.DIRECTORY_DOCUMENTS}/Backups/PocketScore/Games%")
                
                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
                )?.use { cursor ->
                    val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                    
                    while (cursor.moveToNext()) {
                        val fileName = cursor.getString(nameColumn)
                        if (fileName.endsWith(".pscore")) {
                            val name = fileName.removeSuffix(".pscore")
                            val date = cursor.getLong(dateColumn) * 1000 // Convert to millis
                            // Keep the latest if we find duplicates
                            if (date > (snapshots[name] ?: 0L)) {
                                snapshots[name] = date
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "MediaStore scan failed", e)
            }
        }

        // 4. Scan Downloads Folder (Auto-Downloads)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED)
                val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
                val selectionArgs = arrayOf("${Environment.DIRECTORY_DOWNLOADS}/PocketScore%")
                
                context.contentResolver.query(
                    MediaStore.Downloads.getContentUri("external"),
                    projection, selection, selectionArgs, null
                )?.use { cursor ->
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                    while (cursor.moveToNext()) {
                        val fileName = cursor.getString(nameCol)
                        if (fileName.endsWith(".pscore")) {
                            val name = fileName.removeSuffix(".pscore")
                            val date = cursor.getLong(dateCol) * 1000
                            if (date > (snapshots[name] ?: 0L)) snapshots[name] = date
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Downloads scan failed", e)
            }
        }

        return snapshots.toList().sortedByDescending { it.second }
    }

    private fun listSnapshotsInDir(dir: File): List<Pair<String, Long>> {
        return try {
            if (!dir.exists()) return emptyList()
            dir.listFiles { _, name -> name.endsWith(".pscore") }
                ?.map { it.nameWithoutExtension to it.lastModified() }
                ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to list snapshots in ${dir.path}", e)
            emptyList()
        }
    }

    override suspend fun restoreFromSnapshot(name: String) {
        try {
            getSnapshotContent(name)?.let { share ->
                mergeShareData(share)
                Log.d(TAG, "Restored snapshot: $name")
            } ?: run {
                Log.w(TAG, "Snapshot not found: $name")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore snapshot", e)
            throw e
        }
    }

    override suspend fun getSnapshotContent(name: String): PocketScoreShare? {
        val settings = appSettings.first()

        // 1. Try internal private file first (fastest)
        val internalFile = File(internalBackupDir, "$name.pscore")
        if (internalFile.exists()) {
            return try {
                json.decodeFromString<PocketScoreShare>(internalFile.readText())
            } catch (e: Exception) { null }
        }

        // 2. Try Persistent Linked Folder
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
            } catch (e: Exception) {
                Log.w(TAG, "Persistent folder read failed for $name", e)
            }
        }
        
        // 3. Try MediaStore (Public Documents & Downloads)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val searchPaths = listOf(
                "${Environment.DIRECTORY_DOCUMENTS}/Backups/PocketScore/Games",
                "${Environment.DIRECTORY_DOWNLOADS}/PocketScore"
            )
            
            for (relativePath in searchPaths) {
                try {
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
                    val selectionArgs = arrayOf("$name.pscore", "$relativePath%")
                    
                    val uri = if (relativePath.contains(Environment.DIRECTORY_DOWNLOADS)) 
                        MediaStore.Downloads.getContentUri("external") 
                    else 
                        MediaStore.Files.getContentUri("external")

                    context.contentResolver.query(uri, null, selection, selectionArgs, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                            val id = cursor.getLong(idColumn)
                            val contentUri = Uri.withAppendedPath(uri, id.toString())
                            
                            context.contentResolver.openInputStream(contentUri)?.use { inputStream ->
                                val jsonString = inputStream.bufferedReader().use { it.readText() }
                                return json.decodeFromString<PocketScoreShare>(jsonString)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "MediaStore read failed for $name in $relativePath", e)
                }
            }
        }
        
        Log.w(TAG, "Snapshot not found in any location: $name")
        return null
    }

    override suspend fun deleteSnapshot(name: String) {
        val settings = appSettings.first()

        // 1. Delete from internal locations
        val locations = listOf(
            File(internalBackupDir, "$name.pscore"),
            File(context.getExternalFilesDir(null), "sync/games/$name.pscore")
        )
        
        for (file in locations) {
            try {
                if (file.exists() && file.delete()) {
                    Log.d(TAG, "Deleted local snapshot: $name")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete local snapshot", e)
            }
        }

        // 2. Delete from Linked Folder (SAF)
        settings.backupsFolderUri?.let { uriString ->
            try {
                val folderUri = Uri.parse(uriString)
                val pickedDir = DocumentFile.fromTreeUri(context, folderUri)
                pickedDir?.findFile("$name.pscore")?.delete()
                Log.d(TAG, "Deleted SAF snapshot: $name")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete SAF snapshot", e)
            }
        }

        // 3. Delete from MediaStore (Documents & Downloads)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val paths = listOf(
                "${Environment.DIRECTORY_DOCUMENTS}/Backups/PocketScore/Games",
                "${Environment.DIRECTORY_DOWNLOADS}/PocketScore"
            )
            for (path in paths) {
                try {
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
                    val selectionArgs = arrayOf("$name.pscore", "$path%")
                    val uri = if (path.contains(Environment.DIRECTORY_DOWNLOADS))
                        MediaStore.Downloads.getContentUri("external")
                    else
                        MediaStore.Files.getContentUri("external")
                    
                    context.contentResolver.delete(uri, selection, selectionArgs)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to delete MediaStore snapshot in $path", e)
                }
            }
        }
    }

    override suspend fun triggerDailyAutoSnapshot() {
        try {
            val settings = appSettings.first()
            if (!settings.localSnapshotsEnabled) {
                Log.d(TAG, "Auto-snapshots disabled")
                return
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            if (settings.lastAutoSnapshotDate == today) {
                Log.d(TAG, "Auto-snapshot already created today")
                return
            }

            // Create the snapshot
            createLocalSnapshot("Daily-Auto-$today")
            
            // Update the auto-snapshot date
            val updatedSettings = appSettings.first()
            updateSettings(updatedSettings.copy(lastAutoSnapshotDate = today))
            
            Log.d(TAG, "Created daily auto-snapshot: $today")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create daily auto-snapshot", e)
        }
    }

    override suspend fun triggerCloudBackup() {
        // Placeholder for future cloud backup implementation
        Log.d(TAG, "Cloud backup not yet implemented")
    }

    override suspend fun updateBackupsFolderUri(uri: String?) {
        val currentSettings = appSettings.first()
        updateSettings(currentSettings.copy(backupsFolderUri = uri))
    }
}