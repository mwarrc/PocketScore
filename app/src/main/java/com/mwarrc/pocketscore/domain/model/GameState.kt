package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents the complete state of a game session.
 * 
 * @property id Unique game identifier
 * @property players List of players in the game
 * @property isGameActive Whether the game is currently active
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
     * Gets the winner (highest score).
     */
    fun getWinner(): Player? = players.maxByOrNull { it.score }
    
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
     * Validates game state consistency.
     */
    fun isValid(): Boolean {
        return players.isNotEmpty() &&
               id.isNotBlank() &&
               startTime > 0 &&
               (endTime == null || endTime >= startTime)
    }
}