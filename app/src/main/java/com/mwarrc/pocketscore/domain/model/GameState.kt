package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents the complete state of a game session.
 * 
 * @property id Unique game identifier
 * @property players List of players in the game
 * @property isGameActive Whether the game is currently active
 * @property isPaused Whether the game was suspended and can be resumed
 * @property startTime Game start timestamp
 * @property endTime Game end timestamp (null if ongoing)
 * @property lastUpdate Last modification timestamp
 * @property currentPlayerId ID of current player's turn
 * @property globalEvents Chronological event log
 * @property canUndo Whether undo is available
 * @property isFinalized Whether game was completed normally
 * @property ballsOnTable Pool balls remaining on table
 * @property deviceInfo Device where game was created
 * @property isArchived Whether game is archived
 */
@Serializable
data class GameState(
    val id: String = UUID.randomUUID().toString(),
    val players: List<Player> = emptyList(),
    val isGameActive: Boolean = false,
    /**
     * A suspended-but-unfinished session. [isGameActive] is false while paused, so
     * the app does not auto-navigate into the match, but the game is retained and
     * offered as resumable rather than being archived into history.
     */
    val isPaused: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val lastUpdate: Long = System.currentTimeMillis(),
    val currentPlayerId: String? = null,
    val globalEvents: List<GameEvent> = emptyList(),
    val canUndo: Boolean = false,
    val isFinalized: Boolean = false,
    val ballsOnTable: Set<Int> = (1..15).toSet(),
    val deviceInfo: String? = null,
    val isArchived: Boolean = false
) {
    /**
     * Gets the current player.
     */
    fun getCurrentPlayer(): Player? = players.find { it.id == currentPlayerId }
    
    /**
     * Gets active players.
     */
    fun getActivePlayers(): List<Player> = players.filter { it.isActive }
    
    /**
     * Gets game duration in milliseconds.
     */
    fun getDuration(): Long {
        val end = endTime ?: System.currentTimeMillis()
        return end - startTime
    }
    
    /**
     * Gets total points scored across all players.
     */
    fun getTotalPoints(): Int = players.sumOf { it.score }
    
    /**
     * Gets number of scoring events.
     */
    fun getScoringEventCount(): Int = globalEvents.count { it.type == GameEventType.SCORE }

    /**
     * Whether this match is worth keeping in history.
     *
     * Deliberately NOT based on the final scores. A player can finish a real,
     * fully-played match on 0 net points — they potted, then fouled, and it
     * washed out — and pinning "worth saving" to `score != 0` threw those games
     * away. Equally, a long game where everyone happened to net 0 is still a
     * game that was played.
     *
     * What actually distinguishes a played match from someone idly tapping the
     * 0 key is whether any *meaningful* entry was ever made. The event log
     * records that: a 0 tap is stored as a SCORE event with `isZeroInput = true`,
     * so filtering those out leaves exactly the real score changes.
     *
     * Note [getScoringEventCount] is not a usable substitute — it counts 0 taps
     * too, which is why the old guard let pointless sessions into history.
     */
    fun hasRecordedScore(): Boolean = globalEvents.any {
        it.type == GameEventType.SCORE && !it.isZeroInput && it.points != 0
    }
    
    /**
     * Validates game state consistency.
     */
    fun isValid(): Boolean {
        return players.isNotEmpty() &&
               id.isNotBlank() &&
               startTime > 0 &&
               (endTime == null || endTime >= startTime)
    }
}