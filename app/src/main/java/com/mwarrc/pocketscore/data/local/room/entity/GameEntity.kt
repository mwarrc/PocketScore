package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "games",
    indices = [
        Index(value = ["lastUpdate"], name = "index_games_lastUpdate"),
        Index(value = ["endTime"], name = "index_games_endTime"),
        Index(value = ["isArchived"], name = "index_games_isArchived"),
        Index(value = ["startTime"], name = "index_games_startTime"),
        Index(value = ["isArchived", "isGameActive", "lastUpdate"], name = "index_games_active_lookup")
    ]
)
data class GameEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "isGameActive")
    val isGameActive: Boolean = false,
    
    @ColumnInfo(name = "isPaused", defaultValue = "0")
    val isPaused: Boolean = false,
    
    @ColumnInfo(name = "startTime")
    val startTime: Long,
    
    @ColumnInfo(name = "endTime")
    val endTime: Long? = null,
    
    @ColumnInfo(name = "lastUpdate")
    val lastUpdate: Long,
    
    @ColumnInfo(name = "currentPlayerId")
    val currentPlayerId: String? = null,
    
    @ColumnInfo(name = "canUndo")
    val canUndo: Boolean = false,
    
    @ColumnInfo(name = "isFinalized")
    val isFinalized: Boolean = false,
    
    @ColumnInfo(name = "ballsOnTable")
    val ballsOnTable: Set<Int> = (1..15).toSet(),
    
    @ColumnInfo(name = "deviceInfo")
    val deviceInfo: String? = null,
    
    @ColumnInfo(name = "isArchived")
    val isArchived: Boolean = false,
    
    @ColumnInfo(name = "syncStatus")
    val syncStatus: Int = 0
)
