package com.mwarrc.pocketscore.domain.model

import kotlinx.serialization.Serializable

/**
 * Container for past game records.
 * 
 * @property pastGames List of completed games
 */
@Serializable
data class GameHistory(
    val pastGames: List<GameState> = emptyList()
) {
    /**
     * Gets non-archived games.
     */
    fun getActiveGames(): List<GameState> = pastGames.filter { !it.isArchived }
    
    /**
     * Gets archived games.
     */
    fun getArchivedGames(): List<GameState> = pastGames.filter { it.isArchived }
    
    /**
     * Gets games sorted by most recent first.
     */
    fun getSortedByDate(): List<GameState> = pastGames.sortedByDescending { it.lastUpdate }
    
    /**
     * Finds a game by ID.
     */
    fun findById(id: String): GameState? = pastGames.find { it.id == id }
}