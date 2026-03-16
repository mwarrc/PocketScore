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
            val excludedNames = settings.matchExcludedPlayers[game.id] ?: emptySet()
            val availablePlayers = game.players.filter { it.name !in excludedNames }
            
            if (availablePlayers.isEmpty()) return@forEach
            
            totalAmount += settings.matchCost
            
            val payers = when (settings.settlementMethod) {
                SettlementMethod.ALL_SPLIT -> availablePlayers
                
                SettlementMethod.LOSERS_PAY -> {
                    val allScores = availablePlayers.map { it.score }.distinct()
                    if (allScores.size == 1) {
                        availablePlayers
                    } else {
                        val maxScore = allScores.maxOrNull() ?: 0
                        availablePlayers.filter { it.score != maxScore }
                    }
                }
                
                SettlementMethod.LAST_N_PAY -> {
                    val allScores = availablePlayers.map { it.score }.distinct()
                    if (allScores.size == 1) {
                        availablePlayers
                    } else {
                        val maxScore = allScores.maxOrNull() ?: 0
                        val potentialPayers = availablePlayers.filter { it.score != maxScore }
                        val scoreGroups = potentialPayers.groupBy { it.score }
                        val sortedLoserScores = potentialPayers.map { it.score }.distinct().sorted()
                        val targetScores = sortedLoserScores.take(settings.lastLosersCount)
                        
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
        
        // Apply rounding
        val roundedDebts = playerDebts.map { (name, amount) ->
            val factor = Math.pow(10.0, settings.settlementRoundingDecimals.toDouble())
            val roundedAmount = Math.round(amount * factor) / factor
            name to roundedAmount
        }
        
        return SplitCalculationResult(
            playerDebts = roundedDebts.sortedByDescending { it.second },
            totalAmount = totalAmount
        )
    }
}
