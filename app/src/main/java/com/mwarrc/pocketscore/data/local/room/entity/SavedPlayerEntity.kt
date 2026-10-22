package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_players",
    indices = [
        Index(value = ["name"], name = "index_saved_players_name", unique = true)
    ]
)
data class SavedPlayerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "lastUsed")
    val lastUsed: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "useCount")
    val useCount: Int = 0
)
