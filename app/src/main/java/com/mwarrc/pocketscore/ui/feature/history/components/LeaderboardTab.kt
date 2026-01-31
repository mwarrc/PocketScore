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
import com.mwarrc.pocketscore.domain.model.GameHistory

data class LeaderboardEntry(
    val name: String,
    val wins: Int,
    val gamesPlayed: Int,
    val winRate: Float,
    val totalPoints: Int,
    val avgScore: Float
)

@Composable
fun LeaderboardTab(
    history: GameHistory
) {
    val leaderboard: List<LeaderboardEntry> = remember(history) {
        val statsMap = mutableMapOf<String, MutableList<Int>>()
        val totalWins = mutableMapOf<String, Int>()
        
        history.pastGames.forEach { game ->
            val winner = if (game.isFinalized) game.players.maxByOrNull { it.score } else null
            
            game.players.forEach { player ->
                val name = player.name.trim()
                if (name.isNotEmpty()) {
                    statsMap.getOrPut(name) { mutableListOf() }.add(player.score)
                    if (winner != null && winner.name.trim() == name) {
                        totalWins[name] = (totalWins[name] ?: 0) + 1
                    }
                }
            }
        }
        
        statsMap.map { (name, scores) ->
            val wins = totalWins[name] ?: 0
            val played = scores.size
            LeaderboardEntry(
                name = name,
                wins = wins,
                gamesPlayed = played,
                winRate = if (played > 0) (wins.toFloat() / played.toFloat()) * 100f else 0f,
                totalPoints = scores.sum(),
                avgScore = if (played > 0) scores.average().toFloat() else 0f
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
                            "No data for leaderboard",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    "All-Time Ranking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            itemsIndexed(leaderboard) { index, entry ->
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
        1 -> Color(0xFFFFC107) // Premium Gold (Vibrant)
        2 -> Color(0xFF90A4AE) // Metallic Silver (Distinct Blue-Gray)
        3 -> Color(0xFFB08D57) // Classic Bronze
        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rank <= 3) rankColor.copy(alpha = 0.25f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (rank <= 3) androidx.compose.foundation.BorderStroke(2.dp, rankColor.copy(alpha = 0.6f)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Surface(
                shape = CircleShape,
                color = if (rank <= 3) rankColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (rank <= 3) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = if (rank == 1) Color(0xFF3E2723) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "$rank",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${entry.wins} Wins • ${String.format("%.1f", entry.winRate)}% Win Rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${entry.totalPoints}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Total Points",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Detailed Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatMini(Icons.AutoMirrored.Filled.TrendingUp, "Avg", String.format("%.1f", entry.avgScore))
            StatMini(Icons.Default.Leaderboard, "Played", "${entry.gamesPlayed}")
            if (rank == 1) {
               Text("🏆 RECORD HOLDER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = rankColor)
            }
        }
    }
}

@Composable
fun StatMini(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        Text(
            "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
