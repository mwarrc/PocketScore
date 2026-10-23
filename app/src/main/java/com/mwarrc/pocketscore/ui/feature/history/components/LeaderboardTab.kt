package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

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

/**
 * Tab component showing player rankings based on match history,
 * restyled to match the larger Friends / Matches tab design.
 *
 * Features:
 * - Period filters using tonal chips
 * - Clean top-3 podium without emojis or gradients
 * - Ranked cards (4+) with avatar badges and tap-to-expand stats
 * - Polished empty state
 *
 * @param history Complete game history used to calculate standings.
 * @param settings App settings, including players hidden from the board.
 */
@Composable
fun LeaderboardTab(
    history: GameHistory,
    settings: AppSettings
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Today", "Week", "Month")

    val filteredGames = remember(history, selectedFilter) {
        filterGames(history, selectedFilter)
    }
    val leaderboard = remember(filteredGames, settings.hiddenPlayers) {
        calculateLeaderboard(filteredGames, settings.hiddenPlayers)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── Summary header ──
        item(key = "lb_summary") {
            LeaderboardSummaryHeader(
                playerCount = leaderboard.size,
                matchCount = filteredGames.size
            )
        }

        // ── Period filters ──
        item(key = "lb_filters") {
            LeaderboardFilterChips(
                filters = filters,
                selected = selectedFilter,
                onSelect = { selectedFilter = it }
            )
        }

        if (leaderboard.isEmpty()) {
            item(key = "lb_empty") { EmptyLeaderboardState() }
        } else {
            // ── Top 3 podium ──
            if (leaderboard.size >= 2) {
                item(key = "lb_podium") {
                    PodiumSection(leaderboard.take(3))
                }
            }

            // ── Remaining ranks ──
            val restStartRank = if (leaderboard.size >= 2) 3 else 0
            val restList = if (leaderboard.size > restStartRank) {
                leaderboard.drop(restStartRank)
            } else {
                emptyList()
            }

            if (restList.isNotEmpty()) {
                item(key = "lb_rest_header") {
                    RankingsHeader(count = restList.size)
                }
                itemsIndexed(
                    items = restList,
                    key = { _, entry -> "${selectedFilter}_rest_${entry.name}" }
                ) { index, entry ->
                    RankedRow(rank = restStartRank + index + 1, entry = entry)
                }
            } else if (leaderboard.size == 1) {
                item(key = "lb_single") {
                    RankedRow(rank = 1, entry = leaderboard.first())
                }
            }
        }
    }
}

// ────────
// Summary header
// ────────

@Composable
private fun LeaderboardSummaryHeader(
    playerCount: Int,
    matchCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildString {
                append(playerCount)
                append(if (playerCount == 1) " player" else " players")
                append(" • ")
                append(matchCount)
                append(if (matchCount == 1) " match" else " matches")
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = "Ranked by wins",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

// ────────
// Period filter chips
// ────────

@Composable
private fun LeaderboardFilterChips(
    filters: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { label ->
            val isSelected = selected == label
            Surface(
                onClick = { onSelect(label) },
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                },
                border = if (!isSelected) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                } else null
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                )
            }
        }
    }
}

// ────────
// Podium section - top 3 in classic 2nd | 1st | 3rd layout
// ────────

@Composable
private fun PodiumSection(top: List<LeaderboardEntry>) {
    // Classic podium order: 2nd | 1st | 3rd
    val ordered = buildList {
        if (top.size >= 2) add(IndexedValue(1, top[1]))
        add(IndexedValue(0, top[0]))
        if (top.size >= 3) add(IndexedValue(2, top[2]))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Top performers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ranked by wins, then win rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                ordered.forEach { (index, entry) ->
                    PodiumColumn(
                        rank = index + 1,
                        entry = entry,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    rank: Int,
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val avatarSize = when (rank) {
        1 -> 72.dp
        2 -> 60.dp
        else -> 56.dp
    }
    val (containerColor, contentColor) = when (rank) {
        1 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        2 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatars align along their baseline so names line up
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box {
                Surface(
                    shape = CircleShape,
                    color = containerColor,
                    modifier = Modifier.size(avatarSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = entry.name.firstOrNull()?.uppercase() ?: "?",
                            style = if (rank == 1) {
                                MaterialTheme.typography.headlineSmall
                            } else {
                                MaterialTheme.typography.titleLarge
                            },
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }

                RankBadge(
                    rank = rank,
                    isTopRank = rank == 1,
                    borderColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-5).dp, y = (-5).dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = "${entry.wins}W • ${entry.winRate.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ────────
// Ranked row (rank 4+) - tap to expand
// ────────

@Composable
private fun RankedRow(rank: Int, entry: LeaderboardEntry) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(200),
        label = "lb_chevron_$rank"
    )

    val colorCycleIndex = entry.name.hashCode().absoluteValue % 3
    val (avatarContainer, avatarOnContainer) = when (colorCycleIndex) {
        0 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        1 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar with rank badge
                Box {
                    Surface(
                        shape = CircleShape,
                        color = avatarContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = entry.name.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = avatarOnContainer
                            )
                        }
                    }

                    RankBadge(
                        rank = rank,
                        isTopRank = false,
                        borderColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-5).dp, y = (-5).dp)
                    )
                }

                // Name and meta
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${entry.gamesPlayed} games • ${entry.winRate.toInt()}% win rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Wins chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${entry.wins}W",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = chevronRotation },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded stats
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit = fadeOut(tween(130)) + shrinkVertically(tween(180))
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(12.dp))
                    InlineStatGrid(entry)
                }
            }
        }
    }
}

/**
 * Small circular rank badge used on avatars.
 */
@Composable
private fun RankBadge(
    rank: Int,
    isTopRank: Boolean,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = if (isTopRank) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier.size(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isTopRank) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// ────────
// Inline stat grid shown when a row is expanded
// ────────

@Composable
private fun InlineStatGrid(entry: LeaderboardEntry) {
    // Animated win-rate bar
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Win rate",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        val animatedRate by animateFloatAsState(
            targetValue = (entry.winRate / 100f).coerceIn(0f, 1f),
            animationSpec = tween(600),
            label = "wr_bar"
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
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
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }

    Spacer(Modifier.height(12.dp))

    // Four compact stat tiles
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MiniStatTile(label = "Net", value = "${entry.totalPoints}", modifier = Modifier.weight(1f))
        MiniStatTile(label = "Best", value = "${entry.bestScore}", modifier = Modifier.weight(1f))
        MiniStatTile(label = "Worst", value = "${entry.worstScore}", modifier = Modifier.weight(1f))
        MiniStatTile(
            label = "Avg",
            value = String.format(Locale.US, "%.1f", entry.avgScore),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MiniStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ────────
// Rankings header for rank 4+
// ────────

@Composable
private fun RankingsHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .padding(top = 8.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rankings",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
        ) {
            Text(
                text = if (count == 1) "1 player" else "$count players",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

// ────────
// Empty state
// ────────

@Composable
private fun EmptyLeaderboardState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "No rankings yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Play a match to start building the leaderboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ────────
// Data calculation
// ────────

private fun filterGames(history: GameHistory, filter: String): List<GameState> {
    val now = System.currentTimeMillis()
    return history.pastGames.filter { game ->
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
}

private fun calculateLeaderboard(
    games: List<GameState>,
    hiddenPlayers: List<String>
): List<LeaderboardEntry> {
    val statsMap = mutableMapOf<String, MutableList<Int>>()
    val totalWins = mutableMapOf<String, Int>()
    val totalLosses = mutableMapOf<String, Int>()

    games.forEach { game ->
        val maxScore = if (game.players.isNotEmpty()) game.players.maxOf { it.score } else 0
        val winners = if (game.isFinalized && (maxScore > 0 || game.players.any { it.score != 0 })) {
            game.players.filter { it.score == maxScore }.map { it.name.trim().lowercase() }.toSet()
        } else {
            emptySet()
        }

        game.players.forEach { player ->
            val name = player.name.trim()
            if (name.isNotEmpty() && name !in hiddenPlayers) {
                val key = statsMap.keys.find { it.equals(name, ignoreCase = true) } ?: name
                statsMap.getOrPut(key) { mutableListOf() }.add(player.score)
                if (name.lowercase() in winners) {
                    totalWins[key] = (totalWins[key] ?: 0) + 1
                } else {
                    totalLosses[key] = (totalLosses[key] ?: 0) + 1
                }
            }
        }
    }

    return statsMap.map { (name, scores) ->
        val wins = totalWins[name] ?: 0
        val losses = totalLosses[name] ?: 0
        val played = wins + losses

        val sorted = scores.sorted()
        val median = if (sorted.isEmpty()) {
            0f
        } else if (sorted.size % 2 == 0) {
            ((sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0).toFloat()
        } else {
            sorted[sorted.size / 2].toFloat()
        }

        LeaderboardEntry(
            name = name,
            wins = wins,
            losses = losses,
            gamesPlayed = played,
            winRate = if (played > 0) (wins.toFloat() / played) * 100f else 0f,
            totalPoints = scores.sum(),
            positivePoints = scores.filter { it > 0 }.sum(),
            negativePoints = scores.filter { it < 0 }.sum(),
            avgScore = median,
            bestScore = scores.maxOrNull() ?: 0,
            worstScore = scores.minOrNull() ?: 0
        )
    }.sortedWith(
        compareByDescending<LeaderboardEntry> { it.wins }
            .thenByDescending { it.winRate }
            .thenByDescending { it.totalPoints }
    )
}
