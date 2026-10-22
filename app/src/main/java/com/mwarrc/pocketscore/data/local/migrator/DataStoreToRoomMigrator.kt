package com.mwarrc.pocketscore.data.local.migrator

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mwarrc.pocketscore.data.local.room.AppDatabase
import com.mwarrc.pocketscore.data.local.room.entity.GameEntity
import com.mwarrc.pocketscore.data.local.room.entity.GameEventEntity
import com.mwarrc.pocketscore.data.local.room.entity.GameWithPlayersAndEvents
import com.mwarrc.pocketscore.data.local.room.entity.PlayerEntity
import com.mwarrc.pocketscore.data.local.room.entity.SavedPlayerEntity
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.util.UUID

import com.mwarrc.pocketscore.data.local.di.DataStoreModule
private val Context.dataStore: DataStore<Preferences> get() = DataStoreModule.getDataStore(this)

class DataStoreToRoomMigrator(
    private val context: Context,
    private val database: AppDatabase
) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    private val gameStateKey = stringPreferencesKey("game_state")
    private val gameHistoryKey = stringPreferencesKey("game_history")
    private val appSettingsKey = stringPreferencesKey("app_settings")
    private val migrationCompletedKey = stringPreferencesKey("room_migration_completed")
    
    companion object {
        private const val TAG = "DataStoreToRoomMigrator"
    }
    
    suspend fun migrateIfNeeded(): MigrationResult {
        return try {
            val prefs = context.dataStore.data.first()
            val migrationCompleted = prefs[migrationCompletedKey] == "true"
            
            if (migrationCompleted) {
                Log.d(TAG, "Migration already completed")
                return MigrationResult.AlreadyCompleted
            }
            
            Log.d(TAG, "Starting migration from DataStore to Room")
            
            var gamesMigrated = 0
            var playersSaved = 0
            
            // Migrate game history
            prefs[gameHistoryKey]?.let { historyJson ->
                try {
                    val history = json.decodeFromString<GameHistory>(historyJson)
                    Log.d(TAG, "Migrating ${history.pastGames.size} games from history")
                    history.pastGames.forEach { gameState ->
                        migrateGameStateToDb(gameState)
                        gamesMigrated++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to migrate game history", e)
                }
            }
            
            // Migrate active game state
            prefs[gameStateKey]?.let { stateJson ->
                try {
                    val gameState = json.decodeFromString<GameState>(stateJson)
                    if (gameState.isGameActive || gameState.players.isNotEmpty()) {
                        Log.d(TAG, "Migrating active game state")
                        migrateGameStateToDb(gameState)
                        gamesMigrated++
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to migrate active game state", e)
                }
            }
            
            // Migrate saved players
            prefs[appSettingsKey]?.let { settingsJson ->
                try {
                    val settings = json.decodeFromString<AppSettings>(settingsJson)
                    if (settings.savedPlayerNames.isNotEmpty()) {
                        Log.d(TAG, "Migrating ${settings.savedPlayerNames.size} saved players")
                        val savedEntities = settings.savedPlayerNames.mapIndexed { index, name ->
                            SavedPlayerEntity(
                                id = UUID.nameUUIDFromBytes("saved_$name".toByteArray()).toString(),
                                name = name,
                                lastUsed = System.currentTimeMillis() - (index * 1000),
                                useCount = 1
                            )
                        }
                        database.savedPlayerDao().insertSavedPlayers(savedEntities)
                        playersSaved = savedEntities.size
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to migrate settings", e)
                }
            }
            
            context.dataStore.edit { preferences ->
                preferences[migrationCompletedKey] = "true"
            }
            
            Log.d(TAG, "Migration completed: games=$gamesMigrated, players=$playersSaved")
            MigrationResult.Success(gamesMigrated, playersSaved)
            
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed", e)
            MigrationResult.Error(e)
        }
    }
    
    private suspend fun migrateGameStateToDb(gameState: GameState) {
        val gameEntity = GameEntity(
            id = gameState.id,
            isGameActive = gameState.isGameActive,
            startTime = gameState.startTime,
            endTime = gameState.endTime,
            lastUpdate = gameState.lastUpdate,
            currentPlayerId = gameState.currentPlayerId,
            canUndo = gameState.canUndo,
            isFinalized = gameState.isFinalized,
            ballsOnTable = gameState.ballsOnTable,
            deviceInfo = gameState.deviceInfo,
            isArchived = gameState.isArchived
        )
        val playerEntities = gameState.players.map { player ->
            PlayerEntity(
                id = player.id,
                gameId = gameState.id,
                name = player.name,
                score = player.score,
                isActive = player.isActive
            )
        }
        val eventEntities = gameState.globalEvents.map { event ->
            GameEventEntity(
                id = event.id,
                gameId = gameState.id,
                playerId = event.playerId,
                playerName = event.playerName,
                type = event.type,
                timestamp = event.timestamp,
                details = event.message ?: "",
                points = event.points,
                previousPlayerId = event.previousPlayerId,
                isZeroInput = event.isZeroInput,
                previousScore = event.previousScore,
                newScore = event.newScore,
                autoRemovedBall = event.autoRemovedBall
            )
        }
        val gameWithDetails = GameWithPlayersAndEvents(
            game = gameEntity,
            players = playerEntities,
            events = eventEntities
        )
        database.gameDao().saveFullGame(gameWithDetails)
    }
    
    suspend fun isMigrationCompleted(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[migrationCompletedKey] == "true"
    }
    
    suspend fun resetMigration() {
        context.dataStore.edit { preferences ->
            preferences.remove(migrationCompletedKey)
        }
    }
}

sealed class MigrationResult {
    object AlreadyCompleted : MigrationResult()
    data class Success(val gamesCount: Int, val playersCount: Int) : MigrationResult()
    data class Error(val exception: Exception) : MigrationResult()
}
