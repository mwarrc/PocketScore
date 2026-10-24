package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A player's score line within a single match.
 *
 * Rows are scoped to [gameId] and deleted with their game. The name is
 * denormalised on purpose: a historical match must still display the name used
 * at the time, and renaming a player updates these rows explicitly.
 *
 * @property id Stable identifier, also referenced by `GameEntity.currentPlayerId`
 *   and by events' `playerId`.
 * @property gameId Owning match; cascades on delete.
 * @property name Display name for this match.
 * @property score Current score.
 * @property team Optional team label for team modes; `null` for singles.
 * @property isActive Whether the player is still participating (not eliminated).
 * @property isFoul Whether the player is currently in a foul state.
 * @property foulsCount Running count of fouls, used by foul-based rules.
 * @property breakScore Points scored on the break, tracked separately by some
 *   rule sets.
 * @property turnsPlayed Number of turns taken, used by turn/statistics rules.
 */
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
