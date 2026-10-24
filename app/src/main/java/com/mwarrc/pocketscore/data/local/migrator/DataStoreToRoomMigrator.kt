package com.mwarrc.pocketscore.data.local.migrator

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mwarrc.pocketscore.data.local.di.DataStoreModule
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

/**
 * Helper accessor for the shared preferences store.
 *
 * Kept local to this file because the migrator is one of the last places that
 * still reads the legacy blob keys directly.
 */
private val Context.dataStore: DataStore<Preferences> get() = DataStoreModule.getDataStore(this)

/**
 * One-time migrator that moves data from the legacy DataStore JSON blobs to
 * the new Room database.
 *
 * Legacy layout (DataStore keys):
 * - `game_state`    - currently active match serialized as [GameState].
 * - `game_history`  - past matches serialized as [GameHistory].
 * - `app_settings`  - application settings serialized as [AppSettings].
 *
 * After migration completes, a marker key (`room_migration_completed`) is
 * written so future app launches skip the work. The migrator is idempotent and
 * swallows per-game failures to avoid blocking startup entirely.
 *
 * Saved players are imported from `AppSettings.savedPlayerNames` with synthetic
 * IDs derived from the name (UUID v3) so duplicates don't accumulate.
 *
 * @param context Application context used to read DataStore and to pass to Room.
 * @param database Room database instance to write into.
 */
class DataStoreToRoomMigrator(
    private val context: Context,
    private val database: AppDatabase
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /** Legacy DataStore key holding the active [GameState]. */
    private val gameStateKey = stringPreferencesKey("game_state")

    /** Legacy DataStore key holding [GameHistory]. */
    private val gameHistoryKey = stringPreferencesKey("game_history")

    /** Legacy DataStore key holding [AppSettings]. */
    private val appSettingsKey = stringPreferencesKey("app_settings")

    /** Marker key written once migration finishes successfully. */
    private val migrationCompletedKey = stringPreferencesKey("room_migration_completed")

    private companion object {
        /** Logcat tag for migration diagnostics. */
        private const val TAG = "DataStoreToRoomMigrator"
    }

    /**
     * Runs migration if it has not been completed before.
     *
     * Reads all legacy blobs, converts domain models to Room entities via
     * [migrateGameStateToDb], inserts saved players, then marks migration as
     * complete.
     *
     * @return A [MigrationResult] describing what happened.
     */
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

    /**
     * Converts a domain [GameState] to Room entities and persists it.
     *
     * Preserves lifecycle flags exactly as they were stored so that a game
     * that was active remains active and a finished game remains finished.
     *
     * @param gameState Domain model to persist.
     */
    private suspend fun migrateGameStateToDb(gameState: GameState) {
        val gameEntity = GameEntity(
            id = gameState.id,
            isGameActive = gameState.isGameActive,
            isPaused = gameState.isPaused,
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

    /**
     * Checks whether migration has already completed.
     *
     * @return `true` if the marker is present in DataStore.
     */
    suspend fun isMigrationCompleted(): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[migrationCompletedKey] == "true"
    }

    /**
     * Clears the migration marker.
     *
     * Useful for testing; running the app again will re-run migration from the
     * legacy blobs (which may still exist if not cleaned up).
     */
    suspend fun resetMigration() {
        context.dataStore.edit { preferences ->
            preferences.remove(migrationCompletedKey)
        }
    }
}

/**
 * Result of a migration attempt.
 */
sealed class MigrationResult {
    /** Migration was already marked complete; no work performed. */
    object AlreadyCompleted : MigrationResult()

    /**
     * Migration completed.
     *
     * @property gamesCount Number of games migrated (history + active).
     * @property playersCount Number of saved players imported.
     */
    data class Success(val gamesCount: Int, val playersCount: Int) : MigrationResult()

    /**
     * Migration failed fatally.
     *
     * @property exception Root cause of failure.
     */
    data class Error(val exception: Exception) : MigrationResult()
}
