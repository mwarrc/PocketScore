package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single scoring event in a player's history.
 * 
 * @property points Points scored/lost in this event
 * @property timestamp Event timestamp
 */
@Serializable
data class ScoreEvent(
    val points: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Validates the score event.
     */
    fun isValid(): Boolean = timestamp > 0
}

/**
 * Represents a player in a game.
 * 
 * @property id Unique player identifier
 * @property name Player's display name
 * @property score Current total score
 * @property history Score history (for simple undo)
 * @property eventHistory Detailed event history
 * @property isActive Whether player is active in game
 */
@Serializable
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val score: Int = 0,
    val history: List<Int> = emptyList(),
    val eventHistory: List<ScoreEvent> = emptyList(),
    val isActive: Boolean = true
) {
    /**
     * Gets the number of scoring events for this player.
     */
    fun getEventCount(): Int = eventHistory.size
    
    /**
     * Gets the player's average points per scoring event.
     */
    fun getAveragePointsPerEvent(): Double {
        return if (eventHistory.isEmpty()) 0.0
        else eventHistory.sumOf { it.points }.toDouble() / eventHistory.size
    }
    
    /**
     * Gets the player's highest single scoring event.
     */
    fun getHighestSingleScore(): Int = eventHistory.maxOfOrNull { it.points } ?: 0
    
    /**
     * Gets the player's lowest single scoring event.
     */
    fun getLowestSingleScore(): Int = eventHistory.minOfOrNull { it.points } ?: 0
    
    /**
     * Validates player data.
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && name.isNotBlank()
    }
    
    companion object {
        /**
         * Maximum allowed name length.
         */
        const val MAX_NAME_LENGTH = 50
        
        /**
         * Minimum allowed name length.
         */
        const val MIN_NAME_LENGTH = 1
    }
}