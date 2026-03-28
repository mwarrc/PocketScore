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
import androidx.compose.animation.AnimatedVisibility

/**
 * Data model for an entry in the leaderboard.
 */
data class LeaderboardEntry(
    val name: String,
    val wins: Int,
    val losses: Int,
    val gamesPlayed: Int,
    val winRate: Float,
    val totalPoints: Int,       // Net sum (what is used for primary ranks)
    val positivePoints: Int,    // Total good scoring
    val negativePoints: Int,    // Total bleeding/negative scoring
    val avgScore: Float,        // Using median to mitigate extreme outliers like 1.1B
    val bestScore: Int,
    val worstScore: Int
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
            contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
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
        
        val sortedScores = scores.sorted()
        val medianAvg = if (sortedScores.isEmpty()) 0f else {
            if (sortedScores.size % 2 == 0) {
                ((sortedScores[sortedScores.size / 2 - 1] + sortedScores[sortedScores.size / 2]) / 2.0).toFloat()
            } else {
                sortedScores[sortedScores.size / 2].toFloat()
            }
        }
        
        LeaderboardEntry(
            name = name,
            wins = wins,
            losses = losses,
            gamesPlayed = played,
            winRate = if (played > 0) (wins.toFloat() / played.toFloat()) * 100f else 0f,
            totalPoints = scores.sum(),
            positivePoints = scores.filter { it > 0 }.sum(),
            negativePoints = scores.filter { it < 0 }.sum(),
            avgScore = medianAvg, // Used median to cap outliers per requirements
            bestScore = scores.maxOrNull() ?: 0,
            worstScore = scores.minOrNull() ?: 0
        )
    }.sortedWith(
        compareByDescending<LeaderboardEntry> { it.wins }
            .thenByDescending { it.winRate }
            .thenByDescending { it.totalPoints }
    )
}

/**
 * Individual card displaying a player's rank and detailed stats.
 */
@Composable
fun LeaderboardRankCard(rank: Int, entry: LeaderboardEntry) {
    var expanded by remember { mutableStateOf(false) }

    val rankColor = when (rank) {
        1 -> Color(0xFFFFC107) // Amber/Gold
        2 -> Color(0xFF90A4AE) // Blue-grey/Silver
        3 -> Color(0xFFFF8A65) // Deep Orange/Bronze
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val containerColor = when (rank) {
        1 -> Color(0xFFFFC107).copy(alpha = 0.08f)
        2 -> Color(0xFF90A4AE).copy(alpha = 0.08f)
        3 -> Color(0xFFFF8A65).copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        tonalElevation = if (rank <= 3) 1.dp else 0.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Badge
                Surface(
                    shape = CircleShape,
                    color = if (rank <= 3) rankColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (rank <= 3) rankColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Player Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${entry.gamesPlayed} games played",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // Total Points Display (Net)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${entry.totalPoints}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) rankColor else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Net Points",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Expanded Statistics Grid
            androidx.compose.animation.AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))

                    // Row 1: Wins, Losses, Win Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Wins", value = "${entry.wins}", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        StatItem(label = "Losses", value = "${entry.losses}", color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        StatItem(
                            label = "Win Rate", 
                            value = "${entry.winRate.toInt()}%", 
                            color = if (entry.winRate >= 50f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Row 2: Best, Worst, Median
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Best Score", value = "${entry.bestScore}", color = MaterialTheme.colorScheme.onSurface)
                        StatItem(label = "Worst Score", value = "${entry.worstScore}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        StatItem(label = "Median Avg", value = String.format(Locale.US, "%.1f", entry.avgScore), color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(Modifier.height(14.dp))

                    // Row 3: Positives & Negatives
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(label = "Total Pos.", value = "+${entry.positivePoints}", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        StatItem(label = "Total Bleed", value = "${entry.negativePoints}", color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.StatItem(label: String, value: String, color: Color) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
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
