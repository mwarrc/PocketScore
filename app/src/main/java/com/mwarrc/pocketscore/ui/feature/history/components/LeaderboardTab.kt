package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardTab(
    history: GameHistory,
    settings: AppSettings
) {
    var selectedFilter by remember { mutableStateOf("All-Time") }
    val filters = listOf("All-Time", "Today", "Week", "Month")

    val leaderboard: List<LeaderboardEntry> = remember(history, selectedFilter, settings.hiddenPlayers) {
        val statsMap = mutableMapOf<String, MutableList<Int>>()
        val totalWins = mutableMapOf<String, Int>()
        val totalLosses = mutableMapOf<String, Int>()
        
        val now = System.currentTimeMillis()
        
        val filteredGames = history.pastGames.filter { game ->
            if (game.isArchived) return@filter false
            val gameTime = game.endTime ?: game.startTime
            when (selectedFilter) {
                "Today" -> {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.format(Date(gameTime)) == sdf.format(Date(now))
                }
                "Week" -> {
                    val weekStart = now - 7L * 24 * 60 * 60 * 1000L
                    gameTime >= weekStart
                }
                "Month" -> {
                    val monthStart = now - 30L * 24 * 60 * 60 * 1000L
                    gameTime >= monthStart
                }
                else -> true
            }
        }

        filteredGames.forEach { game ->
            val maxScore = if (game.players.isNotEmpty()) game.players.maxOf { it.score } else 0
            val winners = if (game.isFinalized && (maxScore > 0 || game.players.any { it.score != 0 })) {
                game.players.filter { it.score == maxScore }.map { it.name.trim() }.toSet()
            } else emptySet()
            
            game.players.forEach { player ->
                val name = player.name.trim()
                if (name.isNotEmpty() && name !in settings.hiddenPlayers) {
                    val entryName = statsMap.keys.find { it.equals(name, ignoreCase = true) } ?: name
                    statsMap.getOrPut(entryName) { mutableListOf() }.add(player.score)
                    
                    if (name in winners) {
                        totalWins[entryName] = (totalWins[entryName] ?: 0) + 1
                    } else {
                        totalLosses[entryName] = (totalLosses[entryName] ?: 0) + 1
                    }
                }
            }
        }

        statsMap.map { (name, scores) ->
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            modifier = Modifier.wrapContentWidth()
                        )
                    }
                }
            }

            if (leaderboard.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No Matches Yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Play some games to see the leaderboard",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    leaderboard, 
                    key = { _, entry -> "${selectedFilter}_${entry.name}" }
                ) { index, entry ->
                    LeaderboardCard(
                        rank = index + 1, 
                        entry = entry
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    rank: Int,
    entry: LeaderboardEntry
) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                        "$rank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) rankColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${entry.wins}W • ${entry.losses}L • ${entry.winRate.toInt()}% WR",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${entry.totalPoints}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "points",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
