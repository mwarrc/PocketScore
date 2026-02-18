package com.mwarrc.pocketscore.ui.feature.history.utils

import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.Player

/**
 * Utility functions for calculating game statistics.
 */
object GameStatsUtils {
    
    /**
     * Gets the winner(s) of a game.
     * Returns empty list if no scores or all scores are zero.
     */
    fun getWinners(game: GameState): List<Player> {
        val maxScore = game.players.maxOfOrNull { it.score } ?: 0
        return if (maxScore > 0 || game.players.any { it.score != 0 }) {
            game.players.filter { it.score == maxScore }
        } else {
            emptyList()
        }
    }
    
    /**
     * Checks if a game ended in a tie.
     */
    fun isTie(game: GameState): Boolean {
        return getWinners(game).size > 1
    }
    
    /**
     * Gets the player with the highest score.
     * Returns null if no players or all scores are zero.
     */
    fun getLeader(game: GameState): Player? {
        val winners = getWinners(game)
        return if (winners.size == 1) winners.first() else null
    }
    
    /**
     * Calculates the score spread (difference between highest and lowest scores).
     */
    fun getScoreSpread(game: GameState): Int {
        if (game.players.isEmpty()) return 0
        val maxScore = game.players.maxOf { it.score }
        val minScore = game.players.minOf { it.score }
        return maxScore - minScore
    }
    
    /**
     * Gets the average score across all players.
     */
    fun getAverageScore(game: GameState): Double {
        if (game.players.isEmpty()) return 0.0
        return game.players.sumOf { it.score }.toDouble() / game.players.size
    }
    
    /**
     * Checks if a game is resumable (not finalized and has active state).
     */
    fun isResumable(game: GameState): Boolean {
        return !game.isFinalized && game.isGameActive
    }
    
    /**
     * Gets a formatted winner string for display.
     * 
     * Examples:
     * - Single winner: "Alice"
     * - Tie: "Alice & Bob"
     * - No winner: "No winner"
     */
    fun getWinnerDisplayText(game: GameState): String {
        val winners = getWinners(game)
        return when {
            winners.isEmpty() -> "No winner"
            winners.size == 1 -> winners.first().name
            else -> winners.joinToString(" & ") { it.name }
        }
    }
    
    /**
     * Gets the total number of scoring events in the game.
     */
    fun getScoringEventCount(game: GameState): Int {
        return game.getScoringEventCount()
    }
}
