package com.mwarrc.pocketscore.ui.feature.history.components

import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.SettlementMethod

/**
 * Result of match split calculations.
 * 
 * @property playerDebts List of pairs containing player name and the amount they owe, 
 *                       sorted by amount descending.
 * @property totalAmount The total cost for all selected matches combined.
 */
data class SplitCalculationResult(
    val playerDebts: List<Pair<String, Double>>,
    val totalAmount: Double
)

/**
 * Utility class for calculating match settlement splits based on different methods.
 */
object SplitCalculator {
    /**
     * Calculates how much each player owes based on a list of games and settlement rules.
     * 
     * @param games List of finalized games to include in calculation
     * @param settings App settings containing match cost and settlement method
     * @return SplitCalculationResult containing debts and total session amount
     */
    fun calculate(
        games: List<GameState>,
        settings: AppSettings
    ): SplitCalculationResult {
        val playerDebts = mutableMapOf<String, Double>()
        var totalAmount = 0.0

        games.forEach { game ->
            if (game.players.isEmpty()) return@forEach
            
            totalAmount += settings.matchCost
            
            val payers = when (settings.settlementMethod) {
                SettlementMethod.ALL_SPLIT -> game.players
                
                SettlementMethod.LOSERS_PAY -> {
                    val allScores = game.players.map { it.score }.distinct()
                    if (allScores.size == 1) {
                        // All tied (or 0-0) -> Everyone pays
                        game.players
                    } else {
                        val maxScore = allScores.maxOrNull() ?: 0
                        game.players.filter { it.score != maxScore }
                    }
                }
                
                SettlementMethod.LAST_N_PAY -> {
                    val allScores = game.players.map { it.score }.distinct()
                    if (allScores.size == 1) {
                        // All tied -> Everyone splits
                        game.players
                    } else {
                        val maxScore = allScores.maxOrNull() ?: 0
                        // Exclude winners from the pool of potential payers
                        val potentialPayers = game.players.filter { it.score != maxScore }
                        
                        // Group remaining losers by score
                        val scoreGroups = potentialPayers.groupBy { it.score }
                        // Sort distinct loser scores ascending (lowest first)
                        val sortedLoserScores = potentialPayers.map { it.score }.distinct().sorted()
                        
                        // Take the bottom N groups (entities) from the losers
                        val targetScores = sortedLoserScores.take(settings.lastLosersCount)
                        
                        // Collect all players belonging to those score groups
                        targetScores.flatMap { score -> 
                            scoreGroups[score] ?: emptyList() 
                        }
                    }
                }
            }
            
            if (payers.isNotEmpty()) {
                val splitCost = settings.matchCost / payers.size
                payers.forEach { player ->
                    playerDebts[player.name] = (playerDebts[player.name] ?: 0.0) + splitCost
                }
            }
        }
        
        return SplitCalculationResult(
            playerDebts = playerDebts.toList().sortedByDescending { it.second },
            totalAmount = totalAmount
        )
    }
}
