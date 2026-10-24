package com.mwarrc.pocketscore.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mwarrc.pocketscore.data.local.room.entity.GameEntity
import com.mwarrc.pocketscore.data.local.room.entity.GameEventEntity
import com.mwarrc.pocketscore.data.local.room.entity.GameWithPlayers
import com.mwarrc.pocketscore.data.local.room.entity.GameWithPlayersAndEvents
import com.mwarrc.pocketscore.data.local.room.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for games, players and the event log.
 *
 * Methods split between "read aggregates" (with `@Transaction` and `@Relation`)
 * and low-level row operations used by the repository's mappers. Where possible,
 * queries are written to avoid loading event rows for list views.
 */
@Dao
interface GameDao {

    /**
     * Observes a single game with all players and events.
     *
     * @param gameId Identifier of the game to load.
     * @return Reactive stream that emits `null` if the game no longer exists.
     */
    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    fun getGameWithDetails(gameId: String): Flow<GameWithPlayersAndEvents?>

    /**
     * Loads a single game with all players and events once.
     *
     * @param gameId Identifier of the game to load.
     * @return The aggregate, or `null` if not found.
     */
    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameWithDetailsOnce(gameId: String): GameWithPlayersAndEvents?

    /**
     * Observes every game with all players and events.
     *
     * Primarily used by legacy paths; prefer [getAllGamesWithPlayers] for history
     * lists because it avoids pulling the event log.
     *
     * @return All games ordered by `lastUpdate` descending.
     */
    @Transaction
    @Query("SELECT * FROM games ORDER BY lastUpdate DESC")
    fun getAllGamesWithDetails(): Flow<List<GameWithPlayersAndEvents>>

    /**
     * Observes finished/non-active games with their players but without events.
     *
     * This is the hot path for the Records screen. It filters to non-active games
     * so the in-progress match does not appear in history.
     *
     * @return Games ordered by `lastUpdate` descending.
     */
    @Transaction
    @Query("SELECT * FROM games WHERE isGameActive = 0 ORDER BY lastUpdate DESC")
    fun getAllGamesWithPlayers(): Flow<List<GameWithPlayers>>

    /**
     * Observes the currently active (or resumable) game with full details.
     *
     * @return The most recently updated active game, or `null` if none exists.
     */
    @Transaction
    @Query("SELECT * FROM games WHERE isGameActive = 1 ORDER BY lastUpdate DESC LIMIT 1")
    fun getActiveGame(): Flow<GameWithPlayersAndEvents?>

    /**
     * Paginates games with full player and event data.
     *
     * @param limit Maximum rows to return.
     * @param offset Rows to skip.
     * @return Page of game aggregates.
     */
    @Transaction
    @Query("SELECT * FROM games ORDER BY lastUpdate DESC LIMIT :limit OFFSET :offset")
    suspend fun getGamesPage(limit: Int, offset: Int): List<GameWithPlayersAndEvents>

    /** Returns the total number of game rows in the database. */
    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int

    /**
     * Inserts or replaces a game header.
     *
     * @param game Header row to upsert.
     * @return Row ID (implementation detail from Room).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    /**
     * Inserts or replaces multiple game headers.
     *
     * @param games Headers to upsert.
     * @return Row IDs for each inserted/replaced row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>): List<Long>

    /**
     * Updates a game header.
     *
     * @param game Header with updated values.
     * @return Number of rows affected.
     */
    @Update
    suspend fun updateGame(game: GameEntity): Int

    /**
     * Deletes a game header row directly.
     *
     * Child rows remain only if foreign keys are disabled; prefer
     * [deleteFullGame] or rely on `ON DELETE CASCADE` with foreign keys enabled.
     *
     * @param game Header to delete.
     * @return Number of rows affected.
     */
    @Delete
    suspend fun deleteGame(game: GameEntity): Int

    /**
     * Deletes a game by its identifier.
     *
     * @param gameId Identifier of the game to delete.
     * @return Number of rows affected.
     */
    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGameById(gameId: String): Int

    /**
     * Batch deletes multiple games in a single query.
     *
     * SQLite's `ON DELETE CASCADE` removes associated players and events when
     * foreign keys are enforced.
     *
     * @param gameIds Identifiers to delete.
     * @return Number of game rows affected.
     */
    @Query("DELETE FROM games WHERE id IN (:gameIds)")
    suspend fun deleteGamesByIds(gameIds: List<String>): Int

    /**
     * Finds game IDs that contain a specific player name.
     *
     * Used by targeted rename to avoid scanning every row in memory.
     *
     * @param playerName Player name to search for.
     * @return Distinct game IDs containing that name.
     */
    @Query("SELECT DISTINCT gameId FROM players WHERE name = :playerName")
    suspend fun getGameIdsByPlayerName(playerName: String): List<String>

    /**
     * Finds game IDs that contain events for a specific player name.
     *
     * @param name Player name to search for.
     * @return Distinct game IDs containing events for that name.
     */
    @Query("SELECT DISTINCT gameId FROM game_events WHERE playerName = :name")
    suspend fun getGameIdsByEventPlayerName(name: String): List<String>

    /**
     * Renames a player across all player rows.
     *
     * @param oldName Current name.
     * @param newName Replacement name.
     * @return Number of rows updated.
     */
    @Query("UPDATE players SET name = :newName WHERE name = :oldName")
    suspend fun renamePlayerInPlayers(oldName: String, newName: String): Int

    /**
     * Renames a player across all event rows.
     *
     * @param oldName Current name.
     * @param newName Replacement name.
     * @return Number of rows updated.
     */
    @Query("UPDATE game_events SET playerName = :newName WHERE playerName = :oldName")
    suspend fun renamePlayerInEvents(oldName: String, newName: String): Int

    /**
     * Inserts or replaces multiple players.
     *
     * @param players Player rows to upsert.
     * @return Row IDs for each inserted/replaced row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>): List<Long>

    /**
     * Inserts or replaces multiple events.
     *
     * @param events Event rows to upsert.
     * @return Row IDs for each inserted/replaced row.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<GameEventEntity>): List<Long>

    /**
     * Updates multiple player rows.
     *
     * @param players Player rows with updated values.
     * @return Number of rows affected.
     */
    @Update
    suspend fun updatePlayers(players: List<PlayerEntity>): Int

    /**
     * Deletes all players belonging to a game.
     *
     * @param gameId Identifier of the game.
     * @return Number of rows deleted.
     */
    @Query("DELETE FROM players WHERE gameId = :gameId")
    suspend fun deletePlayersByGameId(gameId: String): Int

    /**
     * Deletes all events belonging to a game.
     *
     * @param gameId Identifier of the game.
     * @return Number of rows deleted.
     */
    @Query("DELETE FROM game_events WHERE gameId = :gameId")
    suspend fun deleteEventsByGameId(gameId: String): Int

    /**
     * Deletes players not present in [keptIds] for a given game.
     *
     * Used by [saveFullGame] to reconcile state when a game is updated.
     *
     * @param gameId Identifier of the game.
     * @param keptIds Player IDs that should remain.
     * @return Number of rows deleted.
     */
    @Query("DELETE FROM players WHERE gameId = :gameId AND id NOT IN (:keptIds)")
    suspend fun deletePlayersNotIn(gameId: String, keptIds: List<String>): Int

    /**
     * Deletes events not present in [keptIds] for a given game.
     *
     * Used by [saveFullGame] to reconcile state when a game is updated.
     *
     * @param gameId Identifier of the game.
     * @param keptIds Event IDs that should remain.
     * @return Number of rows deleted.
     */
    @Query("DELETE FROM game_events WHERE gameId = :gameId AND id NOT IN (:keptIds)")
    suspend fun deleteEventsNotIn(gameId: String, keptIds: List<String>): Int

    /**
     * Persists a full game aggregate atomically.
     *
     * Upserts the game header, then reconciles players (delete those not in the
     * aggregate and insert the rest) and events similarly. Using this keeps the
     * game consistent even if only part of it changed.
     *
     * @param gameWithDetails Complete game state to persist.
     */
    @Transaction
    suspend fun saveFullGame(gameWithDetails: GameWithPlayersAndEvents) {
        insertGame(gameWithDetails.game)
        val playerIds = gameWithDetails.players.map { it.id }
        if (playerIds.isEmpty()) {
            deletePlayersByGameId(gameWithDetails.game.id)
        } else {
            deletePlayersNotIn(gameWithDetails.game.id, playerIds)
            insertPlayers(gameWithDetails.players)
        }
        val eventIds = gameWithDetails.events.map { it.id }
        if (eventIds.isEmpty()) {
            deleteEventsByGameId(gameWithDetails.game.id)
        } else {
            deleteEventsNotIn(gameWithDetails.game.id, eventIds)
            insertEvents(gameWithDetails.events)
        }
    }

    /**
     * Deletes an entire game and its children.
     *
     * Explicitly removes players and events before the game header for clarity;
     * `ON DELETE CASCADE` would also handle this when foreign keys are enabled.
     *
     * @param gameId Identifier of the game to delete.
     */
    @Transaction
    suspend fun deleteFullGame(gameId: String) {
        deletePlayersByGameId(gameId)
        deleteEventsByGameId(gameId)
        deleteGameById(gameId)
    }

    /**
     * Deletes multiple games and their children.
     *
     * @param gameIds Identifiers to delete.
     */
    @Transaction
    suspend fun deleteMultipleGames(gameIds: List<String>) {
        if (gameIds.isNotEmpty()) {
            for (id in gameIds) {
                deletePlayersByGameId(id)
                deleteEventsByGameId(id)
            }
            deleteGamesByIds(gameIds)
        }
    }

    /**
     * Removes orphaned game rows that have no players.
     *
     * Used as a cleanup pass to discard ghost entries created by failed writes.
     *
     * @return Number of orphaned games deleted.
     */
    @Query("DELETE FROM games WHERE id NOT IN (SELECT DISTINCT gameId FROM players)")
    suspend fun deleteGhostGamesWithNoPlayers(): Int

    /** Deletes every game, player and event in the database. */
    @Query("DELETE FROM games")
    suspend fun clearAllGames()

    /**
     * Deactivates all games.
     *
     * Safety net used after bulk import to guarantee no imported game ends up
     * marked as the active match.
     *
     * @return Number of rows affected.
     */
    @Query("UPDATE games SET isGameActive = 0 WHERE isGameActive = 1")
    suspend fun deactivateAllGames(): Int

    /**
     * Deactivates all games except the specified one.
     *
     * Preserves the user's currently active game during import.
     *
     * @param excludeGameId Game to leave active.
     * @return Number of rows affected.
     */
    @Query("UPDATE games SET isGameActive = 0 WHERE isGameActive = 1 AND id != :excludeGameId")
    suspend fun deactivateAllGamesExcept(excludeGameId: String): Int
}
