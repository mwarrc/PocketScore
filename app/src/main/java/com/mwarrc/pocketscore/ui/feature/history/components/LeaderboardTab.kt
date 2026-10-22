package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val positivePoints: Int,
    val negativePoints: Int,
    val avgScore: Float,
    val bestScore: Int,
    val worstScore: Int
)

private val GoldColor   = Color(0xFFFFBF00)
private val SilverColor = Color(0xFF9EABB5)
private val BronzeColor = Color(0xFFCD7F52)

private fun podiumColor(rank: Int, fallback: Color) = when (rank) {
    1    -> GoldColor
    2    -> SilverColor
    3    -> BronzeColor
    else -> fallback
}

/**
 * Tab component showing player rankings based on match history.
 *
 * Features:
 * - Time-based filtering (Today, Week, Month, All-Time)
 * - Top-3 podium hero section
 * - Ranked rows (4+) with tap-to-expand stats
 */
@Composable
fun LeaderboardTab(
    history: GameHistory,
    settings: AppSettings
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Today", "Week", "Month")

    val leaderboard = remember(history, selectedFilter, settings.hiddenPlayers) {
        calculateLeaderboard(history, selectedFilter, settings.hiddenPlayers)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Filter Chips ────────────────────────────────────────────────────
        item {
            LeaderboardFilterChips(
                filters = filters,
                selected = selectedFilter,
                onSelect = { selectedFilter = it }
            )
        }

        if (leaderboard.isEmpty()) {
            item { EmptyLeaderboardState() }
        } else {
            // ── Podium (top 3) ───────────────────────────────────────────────
            if (leaderboard.size >= 2) {
                item {
                    Spacer(Modifier.height(8.dp))
                    PodiumSection(leaderboard.take(minOf(3, leaderboard.size)))
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Rest of the list (rank 4+) ───────────────────────────────────
            val restStartRank = if (leaderboard.size >= 2) 3 else 0
            val restList = if (leaderboard.size > restStartRank) leaderboard.drop(restStartRank) else emptyList()

            if (restList.isNotEmpty()) {
                item {
                    Text(
                        text = "Rankings",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
                itemsIndexed(
                    items = restList,
                    key = { i, e -> "${selectedFilter}_rest_${e.name}" }
                ) { i, entry ->
                    RankedRow(rank = restStartRank + i + 1, entry = entry)
                }
            } else if (leaderboard.size == 1) {
                item { RankedRow(rank = 1, entry = leaderboard.first()) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filter chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LeaderboardFilterChips(
    filters: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        filters.forEach { label ->
            val isSelected = selected == label
            Surface(
                onClick = { onSelect(label) },
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Podium section — top 3 in classic 2nd | 1st | 3rd layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PodiumSection(top: List<LeaderboardEntry>) {
    // Classic podium order: 2nd | 1st | 3rd
    val ordered = buildList {
        if (top.size >= 2) add(IndexedValue(1, top[1]))
        add(IndexedValue(0, top[0]))
        if (top.size >= 3) add(IndexedValue(2, top[2]))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        ordered.forEach { (idx, entry) ->
            val rank         = idx + 1
            val podiumHeight = when (rank) { 1 -> 80.dp; 2 -> 60.dp; else -> 50.dp }
            val avatarSize   = when (rank) { 1 -> 56.dp; 2 -> 48.dp; else -> 44.dp }
            val color        = podiumColor(rank, MaterialTheme.colorScheme.primary)

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Medal emoji
                Text(
                    text = when (rank) { 1 -> "👑"; 2 -> "🥈"; else -> "🥉" },
                    fontSize = if (rank == 1) 22.sp else 18.sp
                )
                Spacer(Modifier.height(4.dp))

                // Initial avatar
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                }
                Spacer(Modifier.height(6.dp))

                // Name
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                // Win count
                Text(
                    text = "${entry.wins}W · ${entry.winRate.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))

                // Podium block with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(podiumHeight)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.12f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = color
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ranked row (rank 4+) — tap to expand
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RankedRow(rank: Int, entry: LeaderboardEntry) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "lb_chevron_$rank"
    )

    Surface(
        onClick = { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank number
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(28.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.width(10.dp))

                // Initial avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Name + games
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${entry.gamesPlayed} games",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Wins badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${entry.wins}W",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = chevronRotation },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded stats
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit  = fadeOut(tween(130)) + shrinkVertically(tween(180))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    InlineStatGrid(entry)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Inline stat grid shown when a row is expanded
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InlineStatGrid(entry: LeaderboardEntry) {
    // Animated win-rate bar
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Win Rate",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(58.dp)
        )
        val animatedRate by animateFloatAsState(
            targetValue = (entry.winRate / 100f).coerceIn(0f, 1f),
            animationSpec = tween(600),
            label = "wr_bar"
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedRate)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = "${entry.winRate.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(34.dp),
            textAlign = TextAlign.End
        )
    }

    Spacer(Modifier.height(12.dp))

    // 4-column mini stats
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MiniStat(label = "Net Pts",  value = "${entry.totalPoints}")
        MiniStat(label = "Best",     value = "${entry.bestScore}")
        MiniStat(label = "Worst",    value = "${entry.worstScore}")
        MiniStat(label = "Avg",      value = String.format(Locale.US, "%.1f", entry.avgScore))
    }
}

@Composable
private fun RowScope.MiniStat(label: String, value: String) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Empty state
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyLeaderboardState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = "No games yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Play some matches to see the rankings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Data calculation (unchanged logic)
// ─────────────────────────────────────────────────────────────────────────────

private fun calculateLeaderboard(
    history: GameHistory,
    filter: String,
    hiddenPlayers: List<String>
): List<LeaderboardEntry> {
    val statsMap    = mutableMapOf<String, MutableList<Int>>()
    val totalWins   = mutableMapOf<String, Int>()
    val totalLosses = mutableMapOf<String, Int>()
    val now         = System.currentTimeMillis()

    val filteredGames = history.pastGames.filter { game ->
        if (game.isArchived) return@filter false
        val gameTime = game.endTime ?: game.startTime
        when (filter) {
            "Today" -> {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(gameTime)) == sdf.format(Date(now))
            }
            "Week"  -> gameTime >= now - 7L  * 24 * 60 * 60 * 1000L
            "Month" -> gameTime >= now - 30L * 24 * 60 * 60 * 1000L
            else    -> true
        }
    }

    filteredGames.forEach { game ->
        val maxScore = if (game.players.isNotEmpty()) game.players.maxOf { it.score } else 0
        val winners  = if (game.isFinalized && (maxScore > 0 || game.players.any { it.score != 0 })) {
            game.players.filter { it.score == maxScore }.map { it.name.trim().lowercase() }.toSet()
        } else emptySet()

        game.players.forEach { player ->
            val name = player.name.trim()
            if (name.isNotEmpty() && name !in hiddenPlayers) {
                val key = statsMap.keys.find { it.equals(name, ignoreCase = true) } ?: name
                statsMap.getOrPut(key) { mutableListOf() }.add(player.score)
                if (name.lowercase() in winners) totalWins[key]   = (totalWins[key]   ?: 0) + 1
                else                             totalLosses[key] = (totalLosses[key] ?: 0) + 1
            }
        }
    }

    return statsMap.map { (name, scores) ->
        val wins   = totalWins[name]   ?: 0
        val losses = totalLosses[name] ?: 0
        val played = wins + losses

        val sorted = scores.sorted()
        val median = if (sorted.isEmpty()) 0f else {
            if (sorted.size % 2 == 0)
                ((sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0).toFloat()
            else
                sorted[sorted.size / 2].toFloat()
        }

        LeaderboardEntry(
            name           = name,
            wins           = wins,
            losses         = losses,
            gamesPlayed    = played,
            winRate        = if (played > 0) (wins.toFloat() / played) * 100f else 0f,
            totalPoints    = scores.sum(),
            positivePoints = scores.filter { it > 0 }.sum(),
            negativePoints = scores.filter { it < 0 }.sum(),
            avgScore       = median,
            bestScore      = scores.maxOrNull() ?: 0,
            worstScore     = scores.minOrNull() ?: 0
        )
    }.sortedWith(
        compareByDescending<LeaderboardEntry> { it.wins }
            .thenByDescending { it.winRate }
            .thenByDescending { it.totalPoints }
    )
}

