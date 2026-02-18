package com.mwarrc.pocketscore.ui.feature.home.utils

import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.RosterSortOption
import java.util.*

/**
 * Utility for managing and sorting the player roster pool.
 */
object RosterSorter {
    
    /**
     * Sorts a list of player names based on the selected criteria and match history.
     * 
     * @param names The list of player names to sort.
     * @param option The sorting strategy to apply.
     * @param history The game history used for statistics-based sorting (Frequent, Winners, Losers).
     * @param shuffleSeed Seed for consistent random shuffling.
     * @return A sorted list of player names.
     */
    fun sort(
        names: List<String>,
        option: RosterSortOption,
        history: GameHistory?,
        shuffleSeed: Int = 0
    ): List<String> {
        if (names.isEmpty()) return emptyList()
        
        val lastGame = history?.pastGames?.firstOrNull()
        
        return when (option) {
            RosterSortOption.MANUAL -> names // Retain entry/recency order
            
            RosterSortOption.ALPHABETICAL -> names.sortedWith(String.CASE_INSENSITIVE_ORDER)
            
            RosterSortOption.LOSERS_FIRST -> {
                if (lastGame == null) names.sortedWith(String.CASE_INSENSITIVE_ORDER)
                else names.sortedWith { name1, name2 ->
                    compareByLastMatchScore(name1, name2, lastGame, ascending = true)
                }
            }
            
            RosterSortOption.WINNERS_FIRST -> {
                if (lastGame == null) names.sortedWith(String.CASE_INSENSITIVE_ORDER)
                else names.sortedWith { name1, name2 ->
                    compareByLastMatchScore(name1, name2, lastGame, ascending = false)
                }
            }
            
            RosterSortOption.RANDOM -> {
                if (shuffleSeed == 0) names.shuffled()
                else names.shuffled(Random(shuffleSeed.toLong()))
            }
            
            RosterSortOption.MOST_PLAYED -> {
                val frequencies = calculatePlayerFrequencies(history)
                names.sortedWith { name1, name2 ->
                    val f1 = frequencies[name1.trim().lowercase()] ?: 0
                    val f2 = frequencies[name2.trim().lowercase()] ?: 0
                    if (f1 != f2) f2.compareTo(f1) // Descending
                    else name1.compareTo(name2, ignoreCase = true)
                }
            }
        }
    }

    private fun compareByLastMatchScore(
        name1: String,
        name2: String,
        lastGame: com.mwarrc.pocketscore.domain.model.GameState,
        ascending: Boolean
    ): Int {
        val p1 = lastGame.players.find { it.name.trim().equals(name1.trim(), ignoreCase = true) }
        val p2 = lastGame.players.find { it.name.trim().equals(name2.trim(), ignoreCase = true) }
        
        return when {
            p1 != null && p2 != null -> {
                if (ascending) p1.score.compareTo(p2.score) 
                else p2.score.compareTo(p1.score)
            }
            p1 != null -> -1
            p2 != null -> 1
            else -> name1.compareTo(name2, ignoreCase = true)
        }
    }

    private fun calculatePlayerFrequencies(history: GameHistory?): Map<String, Int> {
        return history?.pastGames?.flatMap { it.players }
            ?.groupBy { it.name.trim().lowercase() }
            ?.mapValues { it.value.size } ?: emptyMap()
    }
}
