package com.mwarrc.pocketscore.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mwarrc.pocketscore.domain.model.GameEventType

/**
 * One entry in a match's append-only audit log.
 *
 * Events are the source of truth for the history/timeline UI and for undo
 * accounting. They are never mutated in place except when a player is renamed;
 * a correction is recorded as a new event instead.
 *
 * The row is owned by its [gameId] and deleted automatically when that game is
 * deleted.
 *
 * @property id Stable event identifier. Also used as the list key in the UI.
 * @property gameId Owning match; cascades on delete.
 * @property playerId Player the event belongs to, or `null` for system/legacy
 *   rows where only the name was stored.
 * @property playerName Denormalised player name. Stored alongside the ID so the
 *   log still reads correctly if a player row is missing, and so renames can be
 *   applied in a single UPDATE.
 * @property type What happened (score, undo, correction, status change, …).
 * @property points Signed point delta for score events; `0` for non-score events.
 * @property previousPlayerId Turn holder before the event, used by undo/redo.
 * @property isZeroInput `true` when the user deliberately entered `0`.
 * @property timestamp Epoch millis when the event was recorded.
 * @property details Free-form message (status changes, import notes, etc.).
 * @property previousScore Player's score before this event, if captured.
 * @property newScore Player's score after this event, if captured.
 * @property autoRemovedBall Ball number automatically taken off the table by the
 *   pool-ball engine, or `null` when the score was manual.
 */
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
