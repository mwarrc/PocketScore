package com.mwarrc.pocketscore.ui.feature.history.components

import com.mwarrc.pocketscore.domain.model.GameHistory

/**
 * Statistics for a single player across all their games.
 * 
 * This data class aggregates performance metrics from game history
 * to provide a comprehensive view of a player's performance.
 * 
 * @property name Player's display name (unique identifier)
 * @property gamesPlayed Total number of games participated in
 * @property wins Number of games won (including ties)
 * @property winRate Win percentage (0-100)
 * @property avgScore Average score across all games
 * @property bestScore Highest score achieved in any single game
 * @property totalPoints Sum of all points scored across all games
 * @property isHiddenInLeaderboard Whether player is excluded from leaderboard display
 */
data class PlayerStats(
    val name: String,
    val gamesPlayed: Int,
    val wins: Int,
    val winRate: Float,
    val avgScore: Float,
    val bestScore: Int,
    val totalPoints: Int,
    val isHiddenInLeaderboard: Boolean,
    val isDeactivated: Boolean
) {
    companion object {
        /**
         * Calculates player statistics from game history.
         * 
         * @param playerName The player's name to calculate stats for
         * @param history Complete game history
         * @param hiddenPlayers List of player names hidden from leaderboard
         * @param deactivatedPlayers List of player names hidden from home screen
         * @return PlayerStats object with calculated metrics
         */
        fun fromHistory(
            playerName: String,
            history: GameHistory,
            hiddenPlayers: List<String>,
            deactivatedPlayers: List<String> = emptyList()
        ): PlayerStats {
            // Find all games this player participated in (case-insensitive)
            val games = history.pastGames.filter { game ->
                game.players.any { it.name.trim().equals(playerName, ignoreCase = true) }
            }
            
            // Extract all scores for this player
            val playerScores = games.mapNotNull { game ->
                game.players.find { 
                    it.name.trim().equals(playerName, ignoreCase = true) 
                }?.score
            }

            // Count wins (including ties)
            val wins = games.count { game ->
                val maxScore = if (game.players.isNotEmpty()) {
                    game.players.maxOf { it.score }
                } else {
                    0
                }
                val winners = game.players
                    .filter { it.score == maxScore }
                    .map { it.name.trim().lowercase() }
                playerName.trim().lowercase() in winners
            }

            val played = games.size
            
            return PlayerStats(
                name = playerName,
                gamesPlayed = played,
                wins = wins,
                winRate = if (played > 0) (wins.toFloat() / played.toFloat()) * 100f else 0f,
                avgScore = if (played > 0) playerScores.average().toFloat() else 0f,
                bestScore = playerScores.maxOrNull() ?: 0,
                totalPoints = playerScores.sum(),
                isHiddenInLeaderboard = playerName in hiddenPlayers,
                isDeactivated = playerName in deactivatedPlayers
            )
        }
        
        /**
         * Calculates statistics for all saved players.
         * 
         * @param playerNames List of all player names to calculate stats for
         * @param history Complete game history
         * @param hiddenPlayers List of player names hidden from leaderboard
         * @param deactivatedPlayers List of player names hidden from home screen
         * @return List of PlayerStats sorted by games played (descending)
         */
        fun calculateAll(
            playerNames: List<String>,
            history: GameHistory,
            hiddenPlayers: List<String>,
            deactivatedPlayers: List<String> = emptyList()
        ): List<PlayerStats> {
            return playerNames
                .map { name -> fromHistory(name, history, hiddenPlayers, deactivatedPlayers) }
                .sortedByDescending { it.gamesPlayed }
        }
    }
}
