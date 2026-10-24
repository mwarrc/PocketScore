package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A reusable player in the home-screen roster.
 *
 * Unlike [PlayerEntity], these rows are not tied to a match. They remember how
 * recently and how often a name was used so the roster can order suggestions by
 * recency and frequency.
 *
 * [name] is uniquely indexed, so inserting the same name twice with
 * `OnConflictStrategy.IGNORE` is a no-op instead of a duplicate.
 *
 * @property id Stable identifier derived from the name (UUID v3), which keeps
 *   inserts idempotent across imports.
 * @property name Unique display name.
 * @property lastUsed Epoch millis of the last game this player participated in.
 * @property useCount Number of games played, used as a secondary sort key.
 */
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
