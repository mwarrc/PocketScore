package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mwarrc.pocketscore.domain.model.GameEventType

@Entity(
    tableName = "game_events",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["gameId"], name = "index_game_events_gameId"),
        Index(value = ["timestamp"], name = "index_game_events_timestamp"),
        Index(value = ["playerName"], name = "index_game_events_playerName")
    ]
)
data class GameEventEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "gameId")
    val gameId: String,
    
    @ColumnInfo(name = "playerId")
    val playerId: String? = null,
    
    @ColumnInfo(name = "playerName")
    val playerName: String,
    
    @ColumnInfo(name = "type")
    val type: GameEventType,
    
    @ColumnInfo(name = "points")
    val points: Int = 0,
    
    @ColumnInfo(name = "previousPlayerId")
    val previousPlayerId: String? = null,
    
    @ColumnInfo(name = "isZeroInput")
    val isZeroInput: Boolean = false,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
    
    @ColumnInfo(name = "details")
    val details: String = "",
    
    @ColumnInfo(name = "previousScore")
    val previousScore: Int? = null,
    
    @ColumnInfo(name = "newScore")
    val newScore: Int? = null,
    
    @ColumnInfo(name = "autoRemovedBall")
    val autoRemovedBall: Int? = null
)
