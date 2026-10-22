package com.mwarrc.pocketscore.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mwarrc.pocketscore.data.local.room.entity.SavedPlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPlayerDao {
    
    @Query("SELECT * FROM saved_players ORDER BY lastUsed DESC, useCount DESC")
    fun getAllSavedPlayers(): Flow<List<SavedPlayerEntity>>
    
    @Query("SELECT * FROM saved_players ORDER BY lastUsed DESC LIMIT :limit")
    suspend fun getSavedPlayers(limit: Int): List<SavedPlayerEntity>
    
    @Query("SELECT name FROM saved_players")
    suspend fun getSavedPlayerNames(): List<String>
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedPlayer(player: SavedPlayerEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSavedPlayers(players: List<SavedPlayerEntity>): List<Long>
    
    @Update
    suspend fun updateSavedPlayer(player: SavedPlayerEntity): Int
    
    @Query("UPDATE saved_players SET lastUsed = :timestamp, useCount = useCount + 1 WHERE name = :name")
    suspend fun touchPlayer(name: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM saved_players WHERE name = :name")
    suspend fun deleteSavedPlayer(name: String): Int
    
    @Query("DELETE FROM saved_players")
    suspend fun clearAllSavedPlayers()
}
