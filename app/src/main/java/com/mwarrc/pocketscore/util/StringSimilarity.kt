package com.mwarrc.pocketscore.util

import kotlin.math.max
import kotlin.math.min

object StringSimilarity {
    /**
     * Calculates the Jaro-Winkler similarity between two strings.
     * Returns a value between 0.0 (no similarity) and 1.0 (exact match).
     */
    fun calculateSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        
        val normalizedS1 = s1.trim().lowercase()
        val normalizedS2 = s2.trim().lowercase()
        
        if (normalizedS1 == normalizedS2) return 1.0f
        
        // Simple Levenshtein distance based similarity
        val distance = levenshteinDistance(normalizedS1, normalizedS2)
        val maxLength = max(normalizedS1.length, normalizedS2.length)
        
        if (maxLength == 0) return 0.0f
        
        return 1.0f - (distance.toFloat() / maxLength)
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1)

        for (i in 1..rhsLength) {
            newCost[0] = i

            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength]
    }

    /**
     * Finds the best match for a target string from a list of candidates.
     * Returns the best match and its score if it exceeds the threshold.
     */
    fun findBestMatch(
        target: String,
        candidates: List<String>,
        threshold: Float = 0.6f
    ): Pair<String, Float>? {
        return candidates
            .map { it to calculateSimilarity(target, it) }
            .filter { it.second >= threshold }
            .maxByOrNull { it.second }
    }
}
