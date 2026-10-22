package com.mwarrc.pocketscore.util

import kotlin.math.max
import kotlin.math.min

/**
 * Utility object for calculating string similarity and finding best matches.
 * 
 * Uses Levenshtein distance algorithm for fuzzy string matching.
 */
object StringSimilarity {
    
    private const val DEFAULT_THRESHOLD = 0.6f
    
    /**
     * Calculates the similarity between two strings.
     * 
     * Uses normalized Levenshtein distance to determine similarity.
     * Strings are normalized (trimmed and lowercased) before comparison.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return Similarity score between 0.0 (no match) and 1.0 (exact match)
     */
    fun calculateSimilarity(s1: String, s2: String): Float {
        if (s1 == s2) return 1.0f
        
        val normalizedS1 = s1.trim().lowercase()
        val normalizedS2 = s2.trim().lowercase()
        
        if (normalizedS1.isEmpty() && normalizedS2.isEmpty()) return 1.0f
        if (normalizedS1.isEmpty() || normalizedS2.isEmpty()) return 0.0f
        if (normalizedS1 == normalizedS2) return 1.0f
        
        val distance = levenshteinDistance(normalizedS1, normalizedS2)
        val maxLength = max(normalizedS1.length, normalizedS2.length)
        
        return if (maxLength == 0) 0.0f else 1.0f - (distance.toFloat() / maxLength)
    }

    /**
     * Calculates the Levenshtein distance between two character sequences.
     * 
     * The Levenshtein distance is the minimum number of single-character edits
     * (insertions, deletions, or substitutions) required to change one string into another.
     * 
     * @param lhs Left-hand side string
     * @param rhs Right-hand side string
     * @return The edit distance between the two strings
     */
    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        if (lhsLength == 0) return rhsLength
        if (rhsLength == 0) return lhsLength

        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1)

        for (i in 1..rhsLength) {
            newCost[0] = i

            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = min(costInsert, min(costDelete, costReplace))
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength]
    }

    /**
     * Finds the best matching string from a list of candidates.
     * 
     * Compares the target string against all candidates and returns the best match
     * if its similarity score exceeds the threshold.
     * 
     * @param target The string to match
     * @param candidates List of potential matches
     * @param threshold Minimum similarity score required (default 0.6)
     * @return Pair of (best match, similarity score) or null if no match exceeds threshold
     */
    fun findBestMatch(
        target: String,
        candidates: List<String>,
        threshold: Float = DEFAULT_THRESHOLD
    ): Pair<String, Float>? {
        if (target.isBlank() || candidates.isEmpty()) return null
        
        return candidates
            .asSequence()
            .map { it to calculateSimilarity(target, it) }
            .filter { it.second >= threshold }
            .maxByOrNull { it.second }
    }
    
    /**
     * Finds all matches above a certain similarity threshold.
     * 
     * @param target The string to match
     * @param candidates List of potential matches
     * @param threshold Minimum similarity score required (default 0.6)
     * @return List of (match, score) pairs sorted by score descending
     */
    fun findAllMatches(
        target: String,
        candidates: List<String>,
        threshold: Float = DEFAULT_THRESHOLD
    ): List<Pair<String, Float>> {
        if (target.isBlank() || candidates.isEmpty()) return emptyList()
        
        return candidates
            .asSequence()
            .map { it to calculateSimilarity(target, it) }
            .filter { it.second >= threshold }
            .sortedByDescending { it.second }
            .toList()
    }
}