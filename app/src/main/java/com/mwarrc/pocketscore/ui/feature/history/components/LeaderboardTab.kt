package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data model for an entry in the leaderboard.
 */
data class LeaderboardEntry(
    val name: String,
    val wins: Int,
    val losses: Int,
    val gamesPlayed: Int,
    val winRate: Float,
    val totalPoints: Int,
    val avgScore: Float,
    val bestScore: Int
)

/**
 * Tab component showing player rankings based on match history.
 * 
 * Features:
 * - Time-based filtering (Today, Week, Month, All-Time)
 * - Automatic ranking based on Wins -> Win Rate -> Total Points
 * - Clean visual representation with rank badges (Gold, Silver, Bronze)
 * - Excludes players marked as "hidden" in settings
 * 
 * @param history Complete game history
 * @param settings App settings (used for hidden player filtering)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardTab(
    history: GameHistory,
    settings: AppSettings
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Today", "Week", "Month")

    // Memoized leaderboard calculation
    val leaderboard = remember(history, selectedFilter, settings.hiddenPlayers) {
        calculateLeaderboard(history, selectedFilter, settings.hiddenPlayers)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Filters Row
            item {
                LeaderboardFilters(
                    filters = filters,
                    selectedFilter = selectedFilter,
                    onSelect = { selectedFilter = it }
                )
            }

            // Empty State
            if (leaderboard.isEmpty()) {
                item {
                    EmptyLeaderboardState()
                }
            } else {
                // Ranking List
                itemsIndexed(
                    items = leaderboard, 
                    key = { _, entry -> "${selectedFilter}_${entry.name}" }
                ) { index, entry ->
                    LeaderboardRankCard(
                        rank = index + 1, 
                        entry = entry
                    )
                }
            }
        }
    }
}

/**
 * Logic to aggregate and rank player statistics.
 */
private fun calculateLeaderboard(
    history: GameHistory,
    filter: String,
    hiddenPlayers: List<String>
): List<LeaderboardEntry> {
    val statsMap = mutableMapOf<String, MutableList<Int>>()
    val totalWins = mutableMapOf<String, Int>()
    val totalLosses = mutableMapOf<String, Int>()
    
    val now = System.currentTimeMillis()
    
    // Filter games based on time and status
    val filteredGames = history.pastGames.filter { game ->
        if (game.isArchived) return@filter false
        val gameTime = game.endTime ?: game.startTime
        when (filter) {
            "Today" -> {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(gameTime)) == sdf.format(Date(now))
            }
            "Week" -> gameTime >= now - 7L * 24 * 60 * 60 * 1000L
            "Month" -> gameTime >= now - 30L * 24 * 60 * 60 * 1000L
            else -> true
        }
    }

    // Aggregate statistics
    filteredGames.forEach { game ->
        val maxScore = if (game.players.isNotEmpty()) game.players.maxOf { it.score } else 0
        val winners = if (game.isFinalized && (maxScore > 0 || game.players.any { it.score != 0 })) {
            game.players.filter { it.score == maxScore }.map { it.name.trim().lowercase() }.toSet()
        } else emptySet()
        
        game.players.forEach { player ->
            val name = player.name.trim()
            if (name.isNotEmpty() && name !in hiddenPlayers) {
                // Use consistent casing for indexing but preserve original for display
                val originalName = statsMap.keys.find { it.equals(name, ignoreCase = true) } ?: name
                statsMap.getOrPut(originalName) { mutableListOf() }.add(player.score)
                
                if (name.lowercase() in winners) {
                    totalWins[originalName] = (totalWins[originalName] ?: 0) + 1
                } else {
                    totalLosses[originalName] = (totalLosses[originalName] ?: 0) + 1
                }
            }
        }
    }

    // Build and sort entries
    return statsMap.map { (name, scores) ->
        val wins = totalWins[name] ?: 0
        val losses = totalLosses[name] ?: 0
        val played = wins + losses
        
        LeaderboardEntry(
            name = name,
            wins = wins,
            losses = losses,
            gamesPlayed = played,
            winRate = if (played > 0) (wins.toFloat() / played.toFloat()) * 100f else 0f,
            totalPoints = scores.sum(),
            avgScore = if (played > 0) scores.average().toFloat() else 0f,
            bestScore = scores.maxOrNull() ?: 0
        )
    }.sortedWith(
        compareByDescending<LeaderboardEntry> { it.wins }
            .thenByDescending { it.winRate }
            .thenByDescending { it.totalPoints }
    )
}

/**
 * Individual card displaying a player's rank and key stats.
 */
@Composable
fun LeaderboardRankCard(rank: Int, entry: LeaderboardEntry) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val containerColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.1f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.1f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Surface(
                shape = CircleShape,
                color = if (rank <= 3) rankColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$rank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) rankColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Player Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entry.wins}W • ${entry.losses}L • ${entry.winRate.toInt()}% WR",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Total Points Display
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.totalPoints}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "points",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaderboardFilters(
    filters: List<String>,
    selectedFilter: String,
    onSelect: (String) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        filters.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = filters.size),
                onClick = { onSelect(label) },
                selected = selectedFilter == label,
                label = { 
                    Text(
                        text = label, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    ) 
                }
            )
        }
    }
}

@Composable
private fun EmptyLeaderboardState() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Matches Yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Play some games to see the leaderboard",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
