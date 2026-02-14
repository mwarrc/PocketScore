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

// Data Models
data class GameAnalysis(
    val biggestGap: Int,
    val leadChanges: Int,
    val totalTurns: Int,
    val dominantPlayer: String?,
    val playerStats: Map<String, PlayerAnalysis>,
    val scoreHistory: Map<String, List<Int>>
)

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

enum class PlayerArchetype(val title: String, val description: String, val icon: ImageVector, val isPositive: Boolean = true) {
    // MVP Level
    DOMINATOR("The Dominator", "Unstoppable force. Led almost the entire match.", Icons.Default.EmojiEvents),
    
    // Playstyles - Positive
    SNIPER("The Sniper", "Metronomic consistency. Rarely misses.", Icons.Default.MyLocation),
    ASSASSIN("The Assassin", "Terrifying scoring explosions.", Icons.Default.Bolt),
    CLOSER("The Closer", "Ice in their veins when it mattered most.", Icons.Default.Timer),
    EARLY_BIRD("The Early Bird", "Started strong and set the pace.", Icons.Default.Speed),
    COMEBACK_KING("Comeback King", "Never say die. Rallied hard efficiently.", Icons.AutoMirrored.Filled.TrendingUp),
    GRINDER("The Grinder", "Fought for every point through grit.", Icons.Default.Build),
    TACTICIAN("The Tactician", "Calculated, balanced performance.", Icons.Default.Psychology),
    STALWART("The Stalwart", "Consistent presence, never dipped.", Icons.Default.Shield),
    NINJA("The Ninja", "Sneaky victory. Stole the lead at the very end.", Icons.Default.VisibilityOff),
    
    // Playstyles - Mixed/Neutral
    WILD_CARD("Wild Card", "High risk, high reward. Unpredictable.", Icons.Default.Casino),
    LATE_BLOOMER("Late Bloomer", "Woke up late, but made it count.", Icons.Default.AccessTime),
    ROLLER_COASTER("Roller Coaster", "Dizzing highs and crushing lows.", Icons.Default.Waves),
    
    // Negative / Unfortunate
    CHOKER("The Bottler", "Dominant early, but crumbled at the finish line.", Icons.AutoMirrored.Filled.TrendingDown, false),
    SLEEPER("The Sleeper", "Barely active for most of the game.", Icons.Default.Hotel, false),
    BRICK_LAYER("The Brick Layer", "Structured a fortress of missed opportunities.", Icons.Default.Foundation, false),
    SPECTATOR("The Spectator", "Had the best seat in the house, watching others score.", Icons.Default.Visibility, false),
    TRAGIC_HERO("Tragic Hero", "Played their heart out, but fell short.", Icons.Default.BrokenImage, false),
    COLD_HANDS("Cold Hands", "Couldn't find a rhythm today.", Icons.Default.AcUnit, false)
}

// Logic
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
    val clutchThreshold = (totalScoreEvents * 0.75).toInt()
    val earlyThreshold = (totalScoreEvents * 0.25).toInt()
    val pClutchScore = game.players.associate { it.id to 0 }.toMutableMap()
    val pZeroTurns = game.players.associate { it.id to 0 }.toMutableMap()
    val pNegativeTurns = game.players.associate { it.id to 0 }.toMutableMap()

    scoreEvents.forEachIndexed { index, event ->
        val player = game.players.find { it.name == event.playerName } ?: return@forEachIndexed
        val pid = player.id
        val points = event.points
        
        pScores[pid] = (pScores[pid] ?: 0) + points
        pHistory[pid]?.add(points)
        
        if (points == 0) pZeroTurns[pid] = (pZeroTurns[pid] ?: 0) + 1
        if (points < 0) pNegativeTurns[pid] = (pNegativeTurns[pid] ?: 0) + 1

        // Record snapshots
        game.players.forEach { p ->
            pScoreSnapshots[p.id]?.add(pScores[p.id] ?: 0)
        }

        if (index >= clutchThreshold) {
            pClutchScore[pid] = (pClutchScore[pid] ?: 0) + points
        }

        // Calculate Gap
        val sortedScores = pScores.values.sortedDescending()
        if (sortedScores.size >= 2) {
            val gap = sortedScores[0] - sortedScores[1]
            if (gap > maxGap) maxGap = gap
        }

        // Track Lead
        val topPlayerEntry = pScores.maxByOrNull { it.value }
        val topPlayerId = topPlayerEntry?.key
        if (topPlayerId != null) {
            val maxScore = topPlayerEntry.value
            // Only count as lead if strictly greater or alone?
            // Simple approach: max score holder gets lead credit.
            // Check if tie?
            val isTie = pScores.count { it.value == maxScore } > 1
            if (!isTie) {
                pTurnsInLead[topPlayerId] = (pTurnsInLead[topPlayerId] ?: 0) + 1
                if (currentLeaderId != null && currentLeaderId != topPlayerId) {
                    leadChanges++
                }
                currentLeaderId = topPlayerId
            }
        }
    }

    // Determine Archetypes carefully to ensure uniqueness
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
        val variance = if (history.isNotEmpty()) history.map { (it - avg) * (it - avg) }.average() else 0.0
        val stdDev = kotlin.math.sqrt(variance)
        val maxTurn = history.maxOrNull() ?: 0
        val zeroCount = pZeroTurns[pid] ?: 0
        val negCount = pNegativeTurns[pid] ?: 0
        val clutchPoints = pClutchScore[pid] ?: 0
        
        // Normalize metrics
        val isWinner = pid in winnerIds
        val isLast = finalScore == (finalScores.values.minOrNull() ?: 0)
        val leadLate = indexAtLead(pScoreSnapshots[pid]) > 0.8
        val lostLeadLate = indexAtLead(pScoreSnapshots[pid]) < 0.8 && timeInLead > 0.4 && !isWinner
        
        val scores = mutableMapOf<PlayerArchetype, Double>()
        
        // Scoring Logic
        if (timeInLead > 0.8 && isWinner) scores[PlayerArchetype.DOMINATOR] = 10.0
        if (stdDev < avg * 0.2 && history.size > 5) scores[PlayerArchetype.SNIPER] = 7.0 + (1.0/stdDev)
        if (maxTurn > avg * 3.0) scores[PlayerArchetype.ASSASSIN] = 6.0 + (maxTurn / avg)
        if (clutchPoints > (finalScore * 0.4)) scores[PlayerArchetype.CLOSER] = 6.5 + (clutchPoints.toDouble()/finalScore)
        if (lostLeadLate) scores[PlayerArchetype.CHOKER] = 9.0
        if (zeroCount > history.size * 0.5) scores[PlayerArchetype.SLEEPER] = 8.0
        if (negCount > 0) scores[PlayerArchetype.COLD_HANDS] = 5.0 + negCount
        if (stdDev > avg * 1.5) scores[PlayerArchetype.WILD_CARD] = 7.0
        if (isWinner && timeInLead < 0.2 && leadLate) scores[PlayerArchetype.NINJA] = 8.5 // Sneak
        if (isLast && finalScore < avgFinalScore * 0.5) scores[PlayerArchetype.SPECTATOR] = 7.0
        if (!isWinner && finalScore > avgFinalScore * 1.2) scores[PlayerArchetype.TRAGIC_HERO] = 6.0
        if (history.all { it > 0 }) scores[PlayerArchetype.STALWART] = 5.0
        if (zeroCount > 2 && finalScore < avgFinalScore) scores[PlayerArchetype.BRICK_LAYER] = 6.0

        // Fallback
        scores[PlayerArchetype.TACTICIAN] = 3.0
        
        playerArchetypeScores[player.id] = scores
    }

    // Assign unique archetypes
    val assignedArchetypes = mutableMapOf<String, PlayerArchetype>()
    val takenArchetypes = mutableSetOf<PlayerArchetype>()
    
    // Greedy assignment: specific strong matches first
    // Flatten to (Player, Archetype, Score)
    val candidates = mutableListOf<Triple<String, PlayerArchetype, Double>>()
    playerArchetypeScores.forEach { (pid, map) ->
        map.forEach { (arch, score) ->
            candidates.add(Triple(pid, arch, score))
        }
    }
    
    // Sort by score descending
    candidates.sortByDescending { it.third }
    
    val playersAssigned = mutableSetOf<String>()
    
    candidates.forEach { (pid, arch, score) ->
        if (pid !in playersAssigned) {
            // If archetype not taken OR specific override allowed? 
            // User wants "avoid ever giving people same".
            if (arch !in takenArchetypes) {
                assignedArchetypes[pid] = arch
                takenArchetypes.add(arch)
                playersAssigned.add(pid)
            } else {
                // If specific high score (e.g. > 8), maybe allow duplicate if no other option? 
                // Better to skip and find next best for this player.
            }
        }
    }
    
    // Fill remaining
    game.players.forEach { player ->
        if (player.id !in playersAssigned) {
            // Find best available
            val opts = playerArchetypeScores[player.id]?.entries?.sortedByDescending { it.value } ?: emptyList()
            val best = opts.firstOrNull { it.key !in takenArchetypes }?.key ?: PlayerArchetype.TACTICIAN
            assignedArchetypes[player.id] = best
            takenArchetypes.add(best) // Mark as taken
            playersAssigned.add(player.id)
        }
    }

    val stats = game.players.associate { player ->
        val pid = player.id
        val history = pHistory[pid] ?: emptyList()
        val finalScore = finalScores[pid] ?: 0
        
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

private fun calculateHotStreak(history: List<Int>): Int {
    if (history.size < 3) return history.sum()
    var maxStreak = Int.MIN_VALUE
    for (i in 0..history.size - 3) {
        val current = history[i] + history[i+1] + history[i+2]
        if (current > maxStreak) maxStreak = current
    }
    return if (maxStreak == Int.MIN_VALUE) 0 else maxStreak
}

private fun indexAtLead(history: List<Int>?): Double {
    if (history == null || history.size < 2) return 0.0
    val finalScore = history.last()
    if (finalScore <= 0) return 0.0
    val target = finalScore * 0.9 // 90%
    val index = history.indexOfFirst { it >= target }
    return if (index >= 0) index.toDouble() / history.size else 1.0
}

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
