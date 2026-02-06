package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val filters = listOf("All-Time", "Today")

    val leaderboard: List<LeaderboardEntry> = remember(history, selectedFilter, settings.hiddenPlayers) {
        val statsMap = mutableMapOf<String, MutableList<Int>>()
        val totalWins = mutableMapOf<String, Int>()
        val totalLosses = mutableMapOf<String, Int>()
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val filteredGames = if (selectedFilter == "Today") {
            history.pastGames.filter { 
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.endTime ?: it.startTime)) == today 
            }
        } else {
            history.pastGames
        }

        filteredGames.forEach { game ->
            val sortedPlayers = game.players
                .filter { it.name.trim().isNotEmpty() && it.name.trim() !in settings.hiddenPlayers }
                .sortedByDescending { it.score }
            val winner = if (game.isFinalized && sortedPlayers.isNotEmpty()) sortedPlayers.first() else null
            
            game.players.forEach { player ->
                val name = player.name.trim()
                if (name.isNotEmpty() && name !in settings.hiddenPlayers) {
                    statsMap.getOrPut(name) { mutableListOf() }.add(player.score)
                    if (winner != null) {
                        if (winner.name.trim() == name) {
                            totalWins[name] = (totalWins[name] ?: 0) + 1
                        } else {
                            totalLosses[name] = (totalLosses[name] ?: 0) + 1
                        }
                    }
                }
            }
        }
        
        statsMap.map { (name, scores) ->
            val wins = totalWins[name] ?: 0
            val losses = totalLosses[name] ?: 0
            val played = scores.size
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                filters.forEachIndexed { index, filter ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = filters.size),
                        onClick = { selectedFilter = filter },
                        selected = selectedFilter == filter,
                        label = { Text(filter) }
                    )
                }
            }
        }

        if (leaderboard.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Leaderboard,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No Data for $selectedFilter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            itemsIndexed(leaderboard, key = { _, entry -> "${selectedFilter}_${entry.name}" }) { index, entry ->
                LeaderboardCard(index + 1, entry)
            }
            
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun LeaderboardCard(
    rank: Int,
    entry: LeaderboardEntry
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFC107) // Gold
        2 -> Color(0xFF90A4AE) // Silver
        3 -> Color(0xFFB08D57) // Bronze
        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rank <= 3) rankColor.copy(alpha = 0.15f) 
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (rank <= 3) {
            androidx.compose.foundation.BorderStroke(2.dp, rankColor.copy(alpha = 0.4f))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank & Name
                Surface(
                    shape = CircleShape,
                    color = if (rank <= 3) rankColor else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$rank",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (rank <= 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${entry.gamesPlayed} Matches Played",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${entry.totalPoints}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Total Pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Wins", "${entry.wins}", MaterialTheme.colorScheme.primary)
                StatItem("Losses", "${entry.losses}", MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                StatItem("Win Rate", "${entry.winRate.toInt()}%", MaterialTheme.colorScheme.secondary)
                StatItem("Best", "${entry.bestScore}", MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
