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

@Dao
interface GameDao {
    
    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    fun getGameWithDetails(gameId: String): Flow<GameWithPlayersAndEvents?>
    
    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameWithDetailsOnce(gameId: String): GameWithPlayersAndEvents?
    
    @Transaction
    @Query("SELECT * FROM games ORDER BY lastUpdate DESC")
    fun getAllGamesWithDetails(): Flow<List<GameWithPlayersAndEvents>>
    
    /**
     * Lightweight query that loads games + players WITHOUT events.
     * Used for history listing where event details aren't needed.
     * Avoids loading thousands of event rows into memory.
     */
    @Transaction
    @Query("SELECT * FROM games WHERE isGameActive = 0 ORDER BY lastUpdate DESC")
    fun getAllGamesWithPlayers(): Flow<List<GameWithPlayers>>
    
    @Transaction
    @Query("SELECT * FROM games WHERE isGameActive = 1 ORDER BY lastUpdate DESC LIMIT 1")
    fun getActiveGame(): Flow<GameWithPlayersAndEvents?>
    
    @Transaction
    @Query("SELECT * FROM games ORDER BY lastUpdate DESC LIMIT :limit OFFSET :offset")
    suspend fun getGamesPage(limit: Int, offset: Int): List<GameWithPlayersAndEvents>
    
    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>): List<Long>
    
    @Update
    suspend fun updateGame(game: GameEntity): Int
    
    @Delete
    suspend fun deleteGame(game: GameEntity): Int
    
    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGameById(gameId: String): Int
    
    /**
     * Batch delete multiple games in a single query.
     * CASCADE handles child players and events.
     */
    @Query("DELETE FROM games WHERE id IN (:gameIds)")
    suspend fun deleteGamesByIds(gameIds: List<String>): Int
    
    /**
     * Find game IDs that contain a specific player name.
     * Used for targeted rename — avoids loading ALL games to find affected ones.
     */
    @Query("SELECT DISTINCT gameId FROM players WHERE name = :playerName")
    suspend fun getGameIdsByPlayerName(playerName: String): List<String>

    @Query("SELECT DISTINCT gameId FROM game_events WHERE playerName = :name")
    suspend fun getGameIdsByEventPlayerName(name: String): List<String>
    
    @Query("UPDATE players SET name = :newName WHERE name = :oldName")
    suspend fun renamePlayerInPlayers(oldName: String, newName: String): Int

    @Query("UPDATE game_events SET playerName = :newName WHERE playerName = :oldName")
    suspend fun renamePlayerInEvents(oldName: String, newName: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>): List<Long>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<GameEventEntity>): List<Long>
    
    @Update
    suspend fun updatePlayers(players: List<PlayerEntity>): Int
    
    @Query("DELETE FROM players WHERE gameId = :gameId")
    suspend fun deletePlayersByGameId(gameId: String): Int
    
    @Query("DELETE FROM game_events WHERE gameId = :gameId")
    suspend fun deleteEventsByGameId(gameId: String): Int

    @Query("DELETE FROM players WHERE gameId = :gameId AND id NOT IN (:keptIds)")
    suspend fun deletePlayersNotIn(gameId: String, keptIds: List<String>): Int

    @Query("DELETE FROM game_events WHERE gameId = :gameId AND id NOT IN (:keptIds)")
    suspend fun deleteEventsNotIn(gameId: String, keptIds: List<String>): Int
    
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
    
    @Transaction
    suspend fun deleteFullGame(gameId: String) {
        deletePlayersByGameId(gameId)
        deleteEventsByGameId(gameId)
        deleteGameById(gameId)
    }
    
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

    @Query("DELETE FROM games WHERE id NOT IN (SELECT DISTINCT gameId FROM players)")
    suspend fun deleteGhostGamesWithNoPlayers(): Int
    
    @Query("DELETE FROM games")
    suspend fun clearAllGames()
    
    /**
     * Deactivates ALL games. Used as a safety net after bulk import
     * to ensure no imported game is left in active state.
     */
    @Query("UPDATE games SET isGameActive = 0 WHERE isGameActive = 1")
    suspend fun deactivateAllGames(): Int
    
    /**
     * Deactivates all games EXCEPT the specified one.
     * Preserves the user's currently active game during import.
     */
    @Query("UPDATE games SET isGameActive = 0 WHERE isGameActive = 1 AND id != :excludeGameId")
    suspend fun deactivateAllGamesExcept(excludeGameId: String): Int
}
