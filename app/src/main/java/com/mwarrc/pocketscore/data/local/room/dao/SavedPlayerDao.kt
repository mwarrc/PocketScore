package com.mwarrc.pocketscore.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mwarrc.pocketscore.data.local.room.entity.SavedPlayerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data access for the reusable saved-player roster.
 *
 * The roster is independent of games; it powers suggestions on the home screen
 * and tracks usage so frequently played names rise to the top.
 */
@Dao
interface SavedPlayerDao {

    /**
     * Observes all saved players ordered by recency then usage.
     *
     * @return Reactive list sorted by `lastUsed` descending, then `useCount` descending.
     */
    @Query("SELECT * FROM saved_players ORDER BY lastUsed DESC, useCount DESC")
    fun getAllSavedPlayers(): Flow<List<SavedPlayerEntity>>

    /**
     * Loads up to [limit] saved players.
     *
     * @param limit Maximum number of players to return.
     * @return List ordered by `lastUsed` descending.
     */
    @Query("SELECT * FROM saved_players ORDER BY lastUsed DESC LIMIT :limit")
    suspend fun getSavedPlayers(limit: Int): List<SavedPlayerEntity>

    /** Returns all saved player names in the order stored by the query (unsorted). */
    @Query("SELECT name FROM saved_players")
    suspend fun getSavedPlayerNames(): List<String>

    /**
     * Inserts a player if one with the same name does not already exist.
     *
     * @param player Player to insert.
     * @return Row ID, or `-1` if the name already existed.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedPlayer(player: SavedPlayerEntity): Long

    /**
     * Inserts multiple players, skipping duplicates by name.
     *
     * @param players Players to insert.
     * @return Row IDs for each insertion (or `-1` for ignored duplicates).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedPlayers(players: List<SavedPlayerEntity>): List<Long>

    /**
     * Updates a saved player row.
     *
     * @param player Player with updated values.
     * @return Number of rows affected.
     */
    @Update
    suspend fun updateSavedPlayer(player: SavedPlayerEntity): Int

    /**
     * Updates usage metadata for a player.
     *
     * Sets [lastUsed] to now and increments [useCount] by one. If the player
     * does not exist, this does nothing (no insert).
     *
     * @param name Name of the player to touch.
     * @param timestamp Timestamp to record (defaults to `System.currentTimeMillis()`).
     * @return Number of rows affected.
     */
    @Query("UPDATE saved_players SET lastUsed = :timestamp, useCount = useCount + 1 WHERE name = :name")
    suspend fun touchPlayer(name: String, timestamp: Long = System.currentTimeMillis()): Int

    /**
     * Deletes a saved player by name.
     *
     * @param name Name of the player to delete.
     * @return Number of rows affected.
     */
    @Query("DELETE FROM saved_players WHERE name = :name")
    suspend fun deleteSavedPlayer(name: String): Int

    /** Deletes all saved players. */
    @Query("DELETE FROM saved_players")
    suspend fun clearAllSavedPlayers()
}
