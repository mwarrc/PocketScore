package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.mwarrc.pocketscore.domain.model.GameEventType
import com.mwarrc.pocketscore.domain.model.GameState
import kotlin.math.abs

/**
 * Result of a comprehensive game analysis.
 * 
 * @property biggestGap The largest lead difference between 1st and 2nd place at any point.
 * @property leadChanges Number of times the 1st place position shifted.
 * @property totalTurns Total number of scoring events recorded.
 * @property dominantPlayer Player who spent the most time in lead (or null if tied/none).
 * @property playerStats Detailed performance data for each player, keyed by name.
 * @property scoreHistory Timeline of scores for each player throughout the match.
 */
data class GameAnalysis(
    val biggestGap: Int,
    val leadChanges: Int,
    val totalTurns: Int,
    val dominantPlayer: String?,
    val playerStats: Map<String, PlayerAnalysis>,
    val scoreHistory: Map<String, List<Int>>
)

/**
 * Analytical data for a single player in a specific match.
 * 
 * @property playerName The player's display name.
 * @property finalScore Final points achieved.
 * @property averagePerTurn Mean points per scoring event.
 * @property maxSingleTurn Highest points gained in one turn.
 * @property volatility Standard deviation of points per turn (stability indicator).
 * @property timeInLeadPercent Percentage of turns spent in 1st place.
 * @property clutchPoints Points scored in the final 25% of the match.
 * @property hotStreak Highest combined score over any 3 consecutive turns.
 * @property zeroTurns Number of rounds where 0 points were added.
 * @property negativeTurns Number of penalty/correction turns.
 * @property archetype The behavioral profile assigned to this player based on their performance pattern.
 */
data class PlayerAnalysis(
    val playerName: String,
    val finalScore: Int,
    val averagePerTurn: Double,
    val maxSingleTurn: Int,
    val volatility: Double,
    val timeInLeadPercent: Double,
    val clutchPoints: Int,
    val hotStreak: Int,
    val zeroTurns: Int,
    val negativeTurns: Int,
    val archetype: PlayerArchetype
)

/**
 * Archetypes representing distinct playstyles and match outcomes.
 * Each player is assigned exactly one unique archetype per match.
 */
enum class PlayerArchetype(
    val title: String, 
    val description: String, 
    val icon: ImageVector, 
    val isPositive: Boolean = true
) {
    // Top-tier Profiles
    DOMINATOR("The Dominator", "Unstoppable force. Led almost the entire match.", Icons.Default.EmojiEvents),
    
    // Positive Behavioral Profiles
    SNIPER("The Sniper", "Metronomic consistency. Rarely misses.", Icons.Default.MyLocation),
    ASSASSIN("The Assassin", "Terrifying scoring explosions.", Icons.Default.Bolt),
    CLOSER("The Closer", "Ice in their veins when it mattered most.", Icons.Default.Timer),
    EARLY_BIRD("The Early Bird", "Started strong and set the pace.", Icons.Default.Speed),
    COMEBACK_KING("Comeback King", "Never say die. Rallied hard efficiently.", Icons.AutoMirrored.Filled.TrendingUp),
    GRINDER("The Grinder", "Fought for every point through grit.", Icons.Default.Build),
    TACTICIAN("The Tactician", "Calculated, balanced performance.", Icons.Default.Psychology),
    STALWART("The Stalwart", "Consistent presence, never dipped.", Icons.Default.Shield),
    NINJA("The Ninja", "Sneaky victory. Stole the lead at the very end.", Icons.Default.VisibilityOff),
    
    // Volatile/Mixed Profiles
    WILD_CARD("Wild Card", "High risk, high reward. Unpredictable.", Icons.Default.Casino),
    LATE_BLOOMER("Late Bloomer", "Woke up late, but made it count.", Icons.Default.AccessTime),
    ROLLER_COASTER("Roller Coaster", "Dizzing highs and crushing lows.", Icons.Default.Waves),
    
    // Negative/Unfortunate Profiles
    CHOKER("The Bottler", "Dominant early, but crumbled at the finish line.", Icons.AutoMirrored.Filled.TrendingDown, false),
    SLEEPER("The Sleeper", "Barely active for most of the game.", Icons.Default.Hotel, false),
    BRICK_LAYER("The Brick Layer", "Structured a fortress of missed opportunities.", Icons.Default.Foundation, false),
    SPECTATOR("The Spectator", "Had the best seat in the house, watching others score.", Icons.Default.Visibility, false),
    TRAGIC_HERO("Tragic Hero", "Played their heart out, but fell short.", Icons.Default.BrokenImage, false),
    COLD_HANDS("Cold Hands", "Couldn't find a rhythm today.", Icons.Default.AcUnit, false)
}

/**
 * Performs a comprehensive analysis of a match history.
 * 
 * Processing steps:
 * 1. Synchronous score reconstruction from global events.
 * 2. Metric calculation (Gap, Lead Changes, Thresholds).
 * 3. Dynamic Archetype scoring based on heuristics.
 * 4. Competitive Archetype assignment (ensuring no two players share the same profile).
 * 
 * @param game The game state to analyze.
 * @return Compiled GameAnalysis object.
 */
fun analyzeGame(game: GameState): GameAnalysis {
    var maxGap = 0
    var leadChanges = 0
    var currentLeaderId: String? = null
    
    val pScores = game.players.associate { it.id to 0 }.toMutableMap()
    val pHistory = game.players.associate { it.id to mutableListOf<Int>() }
    val pScoreSnapshots = game.players.associate { it.id to mutableListOf<Int>(0) }
    val pTurnsInLead = game.players.associate { it.id to 0 }.toMutableMap()
    
    val scoreEvents = game.globalEvents.filter { it.type == GameEventType.SCORE }
    val totalScoreEvents = scoreEvents.size
    
    // Analytical Thresholds
    val clutchThreshold = (totalScoreEvents * 0.75).toInt()
    val pClutchScore = game.players.associate { it.id to 0 }.toMutableMap()
    val pZeroTurns = game.players.associate { it.id to 0 }.toMutableMap()
    val pNegativeTurns = game.players.associate { it.id to 0 }.toMutableMap()

    // 1. Reconstruct Timeline
    scoreEvents.forEachIndexed { index, event ->
        val player = game.players.find { it.name == event.playerName } ?: return@forEachIndexed
        val pid = player.id
        val points = event.points
        
        pScores[pid] = (pScores[pid] ?: 0) + points
        pHistory[pid]?.add(points)
        
        if (points == 0) pZeroTurns[pid] = (pZeroTurns[pid] ?: 0) + 1
        if (points < 0) pNegativeTurns[pid] = (pNegativeTurns[pid] ?: 0) + 1

        // Take snapshot for charts & momentum analysis
        game.players.forEach { p ->
            pScoreSnapshots[p.id]?.add(pScores[p.id] ?: 0)
        }

        // Clutch Performance Tracking
        if (index >= clutchThreshold) {
            pClutchScore[pid] = (pClutchScore[pid] ?: 0) + points
        }

        // Calculate Lead Gap (Gap between #1 and #2)
        val sortedScores = pScores.values.sortedDescending()
        if (sortedScores.size >= 2) {
            val gap = sortedScores[0] - sortedScores[1]
            if (gap > maxGap) maxGap = gap
        }

        // Track Lead Ownership & Changes
        val topPlayerEntry = pScores.maxByOrNull { it.value }
        val topPlayerId = topPlayerEntry?.key
        if (topPlayerId != null) {
            val maxScore = topPlayerEntry.value
            val isTie = pScores.count { it.value == maxScore } > 1
            if (!isTie) { // Ties don't count as leading
                pTurnsInLead[topPlayerId] = (pTurnsInLead[topPlayerId] ?: 0) + 1
                if (currentLeaderId != null && currentLeaderId != topPlayerId) {
                    leadChanges++
                }
                currentLeaderId = topPlayerId
            }
        }
    }

    // 2. Determine Archetype Scores
    // We calculate a weight for every possible archetype per player
    val playerArchetypeScores = mutableMapOf<String, MutableMap<PlayerArchetype, Double>>()
    
    val finalScores = game.players.associate { it.id to (pScores[it.id] ?: 0) }
    val maxFinalScore = finalScores.values.maxOrNull() ?: 0
    val avgFinalScore = if (finalScores.isNotEmpty()) finalScores.values.average() else 0.0
    val winnerIds = finalScores.filter { it.value == maxFinalScore }.keys

    game.players.forEach { player ->
        val pid = player.id
        val history = pHistory[pid] ?: emptyList()
        val finalScore = finalScores[pid] ?: 0
        val timeInLead = if (totalScoreEvents > 0) (pTurnsInLead[pid]?.toDouble() ?: 0.0) / totalScoreEvents else 0.0
        val avg = if (history.isNotEmpty()) history.average() else 0.0
        
        // Variance calculation for Sniper/WildCard detection
        val variance = if (history.isNotEmpty()) history.map { (it - avg) * (it - avg) }.average() else 0.0
        val stdDev = kotlin.math.sqrt(variance)
        val maxTurn = history.maxOrNull() ?: 0
        val zeroCount = pZeroTurns[pid] ?: 0
        val negCount = pNegativeTurns[pid] ?: 0
        val clutchPoints = pClutchScore[pid] ?: 0
        
        // Contextual signals
        val isWinner = pid in winnerIds
        val isLast = finalScore == (finalScores.values.minOrNull() ?: 0)
        
        // Momentum signals (when did they take the final lead?)
        val leadIndex = indexAtLead(pScoreSnapshots[pid])
        val leadLate = leadIndex > 0.8
        val lostLeadLate = leadIndex < 0.8 && timeInLead > 0.4 && !isWinner
        
        val scores = mutableMapOf<PlayerArchetype, Double>()
        
        // Heuristic Scoring logic
        if (timeInLead > 0.8 && isWinner) scores[PlayerArchetype.DOMINATOR] = 10.0
        if (stdDev < avg * 0.2 && history.size > 5) scores[PlayerArchetype.SNIPER] = 7.0 + (1.0/stdDev)
        if (maxTurn > avg * 3.0) scores[PlayerArchetype.ASSASSIN] = 6.0 + (maxTurn / avg)
        if (clutchPoints > (finalScore * 0.4)) scores[PlayerArchetype.CLOSER] = 6.5 + (clutchPoints.toDouble()/finalScore)
        if (lostLeadLate) scores[PlayerArchetype.CHOKER] = 9.0
        if (zeroCount > history.size * 0.5) scores[PlayerArchetype.SLEEPER] = 8.0
        if (negCount > 0) scores[PlayerArchetype.COLD_HANDS] = 5.0 + negCount
        if (stdDev > avg * 1.5) scores[PlayerArchetype.WILD_CARD] = 7.0
        if (isWinner && timeInLead < 0.2 && leadLate) scores[PlayerArchetype.NINJA] = 8.5
        if (isLast && finalScore < avgFinalScore * 0.5) scores[PlayerArchetype.SPECTATOR] = 7.0
        if (!isWinner && finalScore > avgFinalScore * 1.2) scores[PlayerArchetype.TRAGIC_HERO] = 6.0
        if (history.all { it > 0 }) scores[PlayerArchetype.STALWART] = 5.0
        if (zeroCount > 2 && finalScore < avgFinalScore) scores[PlayerArchetype.BRICK_LAYER] = 6.0

        scores[PlayerArchetype.TACTICIAN] = 3.0 // Default/Fallback
        
        playerArchetypeScores[player.id] = scores
    }

    // 3. Unique Archetype Assignment
    // Matches players to archetypes using a competitive selection process
    val assignedArchetypes = mutableMapOf<String, PlayerArchetype>()
    val takenArchetypes = mutableSetOf<PlayerArchetype>()
    val playersAssigned = mutableSetOf<String>()
    
    // Flatten all possible player-archetype pairs and sort by match strength
    val candidates = mutableListOf<Triple<String, PlayerArchetype, Double>>()
    playerArchetypeScores.forEach { (pid, map) ->
        map.forEach { (arch, score) ->
            candidates.add(Triple(pid, arch, score))
        }
    }
    candidates.sortByDescending { it.third }
    
    // Greedy assignment: strongest matches claim their archetype first
    candidates.forEach { (pid, arch, _) ->
        if (pid !in playersAssigned && arch !in takenArchetypes) {
            assignedArchetypes[pid] = arch
            takenArchetypes.add(arch)
            playersAssigned.add(pid)
        }
    }
    
    // Guaranteed fallbacks for unassigned players
    game.players.forEach { player ->
        if (player.id !in playersAssigned) {
            val opts = playerArchetypeScores[player.id]?.entries?.sortedByDescending { it.value } ?: emptyList()
            val best = opts.firstOrNull { it.key !in takenArchetypes }?.key ?: PlayerArchetype.TACTICIAN
            assignedArchetypes[player.id] = best
            takenArchetypes.add(best)
            playersAssigned.add(player.id)
        }
    }

    // 4. Final Data Assembly
    val stats = game.players.associate { player ->
        val pid = player.id
        val history = pHistory[pid] ?: emptyList()
        val avg = if (history.isNotEmpty()) history.average() else 0.0
        val variance = if (history.isNotEmpty()) history.map { (it - avg) * (it - avg) }.average() else 0.0
        val stdDev = kotlin.math.sqrt(variance)
        val timeInLead = if (totalScoreEvents > 0) (pTurnsInLead[player.id]?.toDouble() ?: 0.0) / totalScoreEvents else 0.0
        
        player.name to PlayerAnalysis(
            playerName = player.name,
            finalScore = player.score,
            averagePerTurn = avg,
            maxSingleTurn = history.maxOrNull() ?: 0,
            volatility = stdDev,
            timeInLeadPercent = timeInLead,
            clutchPoints = pClutchScore[player.id] ?: 0,
            hotStreak = calculateHotStreak(history),
            zeroTurns = pZeroTurns[pid] ?: 0,
            negativeTurns = pNegativeTurns[pid] ?: 0,
            archetype = assignedArchetypes[pid] ?: PlayerArchetype.TACTICIAN
        )
    }

    val dominantPlayer = stats.values.maxByOrNull { it.timeInLeadPercent }?.playerName

    return GameAnalysis(
        biggestGap = maxGap,
        leadChanges = leadChanges,
        totalTurns = totalScoreEvents,
        dominantPlayer = dominantPlayer,
        playerStats = stats,
        scoreHistory = pScoreSnapshots.mapKeys { entry -> 
            game.players.find { it.id == entry.key }?.name ?: "Unknown"
        }
    )
}

/**
 * Finds the highest combined score over 3 consecutive turns.
 */
private fun calculateHotStreak(history: List<Int>): Int {
    if (history.size < 3) return history.sum()
    var maxStreak = 0
    for (i in 0..history.size - 3) {
        val current = history[i] + history[i+1] + history[i+2]
        if (current > maxStreak) maxStreak = current
    }
    return maxStreak
}

/**
 * Returns a normalized index (0.0 - 1.0) representing when the player 
 * reached 90% of their final score. Used to detect early vs late bloomers.
 */
private fun indexAtLead(history: List<Int>?): Double {
    if (history == null || history.size < 2) return 0.0
    val finalScore = history.last()
    if (finalScore <= 0) return 0.0
    val target = finalScore * 0.9 
    val index = history.indexOfFirst { it >= target }
    return if (index >= 0) index.toDouble() / history.size else 1.0
}

/**
 * Generates a stable, unique color for a player name.
 * Used for timeline charts and UI indicators.
 */
fun getPlayerColor(name: String): Color {
    val colors = listOf(
        Color(0xFF0061A4), Color(0xFF006E1C), Color(0xFF984061),
        Color(0xFF6750A4), Color(0xFF8B5000), Color(0xFF006A6A),
        Color(0xFFBA1A1A), Color(0xFF626200), Color(0xFF4C662B),
        Color(0xFF994060)
    )
    if (name.isBlank() || name == "SYSTEM") return Color.Gray
    val index = abs(name.hashCode()) % colors.size
    return colors[index]
}
