package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "players",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["gameId"], name = "index_players_gameId"),
        Index(value = ["name"], name = "index_players_name")
    ]
)
data class PlayerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "gameId")
    val gameId: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "score")
    val score: Int = 0,
    
    @ColumnInfo(name = "team")
    val team: String? = null,
    
    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "isFoul")
    val isFoul: Boolean = false,
    
    @ColumnInfo(name = "foulsCount")
    val foulsCount: Int = 0,
    
    @ColumnInfo(name = "breakScore")
    val breakScore: Int = 0,
    
    @ColumnInfo(name = "turnsPlayed")
    val turnsPlayed: Int = 0
)
