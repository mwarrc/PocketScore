package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Types of game events that can be recorded.
 */
@Serializable
enum class GameEventType {
    /** Score change event */
    SCORE,
    /** Score correction/adjustment */
    CORRECTION,
    /** Undo action */
    UNDO,
    /** Player status change (active/inactive) */
    STATUS_CHANGE
}

/**
 * Represents a single game event in the event log.
 * 
 * @property id Unique event identifier
 * @property playerId ID of player involved
 * @property playerName Name of player involved (denormalized for display)
 * @property type Type of event
 * @property points Points involved in the event
 * @property previousPlayerId ID of previous current player (for turn tracking)
 * @property isZeroInput Whether this was a zero-point input
 * @property message Optional event message
 * @property timestamp Event timestamp
 * @property previousScore Player's score before the event
 * @property newScore Player's score after the event
 */
@Serializable
data class GameEvent(
    val id: String = UUID.randomUUID().toString(),
    val playerId: String,
    val playerName: String,
    val type: GameEventType,
    val points: Int,
    val previousPlayerId: String? = null,
    val isZeroInput: Boolean = false,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val previousScore: Int? = null,
    val newScore: Int? = null
) {
    /**
     * Validates that the event has consistent data.
     */
    fun isValid(): Boolean {
        return playerId.isNotBlank() && 
               playerName.isNotBlank() &&
               timestamp > 0
    }
}