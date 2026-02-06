package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchInsightsTab(game: GameState) {
    val analysis = remember(game) { analyzeGame(game) }
    var visiblePlayers by remember { mutableStateOf(analysis.scoreHistory.keys.toSet()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Match Momentum", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Score progression over time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    if (visiblePlayers.size < analysis.scoreHistory.size) {
                        TextButton(onClick = { visiblePlayers = analysis.scoreHistory.keys.toSet() }) {
                            Text("Reset Zoom", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                ScoreMomentumChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    history = analysis.scoreHistory,
                    visiblePlayers = visiblePlayers
                )

                // Chart Legend (Interactive)
                LazyRow(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(analysis.scoreHistory.keys.toList()) { name ->
                        val isSelected = name in visiblePlayers
                        val color = getPlayerColor(name)
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                visiblePlayers = if (isSelected) {
                                    if (visiblePlayers.size > 1) visiblePlayers - name else visiblePlayers
                                } else {
                                    visiblePlayers + name
                                }
                            },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = {
                                Box(
                                    Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) color else color.copy(alpha = 0.3f))
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.1f),
                                selectedLabelColor = color
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = color,
                                borderColor = color.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        item {
            Text("Match Highlights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InsightCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.VerticalAlignBottom,
                    label = "Biggest Gap",
                    value = "${analysis.biggestGap} pts",
                    subValue = "Max point difference",
                    color = MaterialTheme.colorScheme.error
                )
                InsightCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.SwapCalls,
                    label = "Lead Changes",
                    value = "${analysis.leadChanges}",
                    subValue = "Leader swaps",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (analysis.dominantPlayer != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Stars, null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            val stats = analysis.playerStats[analysis.dominantPlayer]
                            Text(
                                "Match Dominance: ${analysis.dominantPlayer}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Controlled the lead for ${String.format("%.0f", (stats?.timeInLeadPercent ?: 0.0) * 100)}% of the match.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("Player Personas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }

        items(analysis.playerStats.values.sortedByDescending { it.finalScore }) { stats ->
            PlayerDetailedStatsCard(stats)
        }
    }
}

@Composable
fun ScoreMomentumChart(
    modifier: Modifier = Modifier,
    history: Map<String, List<Int>>,
    visiblePlayers: Set<String> = history.keys
) {
    val filteredHistory = history.filterKeys { it in visiblePlayers }
    val allVisibleScores = filteredHistory.values.flatten()
    if (allVisibleScores.isEmpty()) return

    val minScore = (allVisibleScores.minOrNull() ?: 0).toFloat()
    val maxScore = (allVisibleScores.maxOrNull() ?: 100).toFloat().coerceAtLeast(minScore + 1f)
    val scoreRange = maxScore - minScore
    val maxTurns = (history.values.maxOfOrNull { it.size } ?: 1).toFloat()

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Y-Axis Labels
            Column(
                modifier = Modifier.fillMaxHeight().width(32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(maxScore.toInt().toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(((maxScore + minScore) / 2).toInt().toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Text(minScore.toInt().toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
            
            Spacer(Modifier.width(8.dp))
            
            val gridColor = MaterialTheme.colorScheme.onSurface
            
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val width = size.width
                val height = size.height

                // Draw horizontal guide lines (Grid)
                val gridAlpha = 0.1f
                
                // Top, Middle, Bottom lines
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1.dp.toPx())
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(0f, height/2), Offset(width, height/2), strokeWidth = 1.dp.toPx())
                drawLine(gridColor.copy(alpha = gridAlpha), Offset(0f, height), Offset(width, height), strokeWidth = 1.dp.toPx())

            filteredHistory.forEach { (name, points) ->
                val color = getPlayerColor(name)
                val path = Path()
                
                points.forEachIndexed { index, score ->
                    val x = (index / (maxTurns - 1)) * width
                    val scoreOffset = score - minScore
                    val y = height - (scoreOffset / scoreRange) * height
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Optional: Point at the end
                if (points.isNotEmpty()) {
                    val lastX = ((points.size - 1) / (maxTurns - 1)) * width
                    val lastY = height - ((points.last() - minScore) / scoreRange) * height
                    drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(lastX, lastY))
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTimelineTab(game: GameState) {
    var selectedPlayerFilter by remember { mutableStateOf<String?>(null) }
    val players = remember(game) { game.players.map { it.name } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedPlayerFilter == null,
                    onClick = { selectedPlayerFilter = null },
                    label = { Text("All") },
                    leadingIcon = if (selectedPlayerFilter == null) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
            items(players) { playerName ->
                FilterChip(
                    selected = selectedPlayerFilter == playerName,
                    onClick = { 
                        selectedPlayerFilter = if (selectedPlayerFilter == playerName) null else playerName 
                    },
                    label = { Text(playerName) },
                    leadingIcon = if (selectedPlayerFilter == playerName) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        val filteredEvents = remember(game.globalEvents, selectedPlayerFilter) {
            if (selectedPlayerFilter == null) {
                game.globalEvents
            } else {
                game.globalEvents.filter { it.playerName == selectedPlayerFilter || it.playerName == "SYSTEM" }
            }
        }

        if (filteredEvents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No matching events found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEvents.reversed()) { event ->
                    DetailEventItem(event)
                }
            }
        }
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subValue, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PlayerDetailedStatsCard(stats: PlayerAnalysis) {
    val playerColor = getPlayerColor(stats.playerName)
    val archetype = stats.archetype
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = playerColor.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, playerColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = playerColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(archetype.icon, null, tint = playerColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stats.playerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(archetype.title, style = MaterialTheme.typography.labelMedium, color = playerColor, fontWeight = FontWeight.Bold)
                }
                Text("${stats.finalScore}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            }
            
            Text(
                archetype.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            HorizontalDivider(modifier = Modifier.alpha(0.2f))
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Avg/Turn", String.format("%.1f", stats.averagePerTurn))
                StatItem("Volatility", String.format("%.1f", stats.volatility))
                StatItem("Max Turn", "${stats.maxSingleTurn}")
                StatItem("Hot Streak", "${stats.hotStreak}")
            }
            
            if (stats.timeInLeadPercent > 0) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { stats.timeInLeadPercent.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                    color = playerColor,
                    trackColor = playerColor.copy(alpha = 0.1f),
                )
                Text(
                    "Match Lead Coverage: ${String.format("%.0f", stats.timeInLeadPercent * 100)}%",
                    style = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                    color = playerColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetailEventItem(event: GameEvent) {
    val playerColor = getPlayerColor(event.playerName ?: "")
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val cardColor = when {
        event.type == GameEventType.UNDO -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        event.type == GameEventType.STATUS_CHANGE -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        event.isZeroInput -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
        else -> playerColor.copy(alpha = if (isDark) 0.15f else 0.1f)
    }

    val icon = when {
        event.type == GameEventType.UNDO -> Icons.AutoMirrored.Filled.Undo
        event.type == GameEventType.STATUS_CHANGE -> Icons.Default.Info
        else -> Icons.Default.Adjust
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (event.playerName == "SYSTEM") MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            else playerColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (event.playerName == "SYSTEM") MaterialTheme.colorScheme.surfaceVariant else playerColor.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, 
                        null, 
                        modifier = Modifier.size(20.dp),
                        tint = if (event.playerName == "SYSTEM") MaterialTheme.colorScheme.onSurfaceVariant else playerColor
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val headerText = when (event.type) {
                    GameEventType.SCORE -> event.playerName ?: "Unknown"
                    GameEventType.UNDO -> "Undo Action"
                    GameEventType.STATUS_CHANGE -> "System Note"
                    else -> event.playerName ?: "Game Event"
                }
                Text(
                    headerText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (event.playerName == "SYSTEM") MaterialTheme.colorScheme.onSurfaceVariant else playerColor
                )
                
                val detailText = when (event.type) {
                    GameEventType.SCORE -> {
                        if (event.previousScore != null && event.newScore != null) {
                            "Changed from ${event.previousScore} to ${event.newScore}"
                        } else {
                            "Added ${event.points} points"
                        }
                    }
                    GameEventType.UNDO -> "Reverted last scoring action"
                    GameEventType.STATUS_CHANGE -> event.message ?: "Match status updated"
                    else -> event.message ?: ""
                }
                Text(
                    detailText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            if (event.type == GameEventType.SCORE) {
                Text(
                    "${if (event.points > 0) "+" else ""}${event.points}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (event.points < 0) MaterialTheme.colorScheme.error else playerColor
                )
            }
        }
    }
}

// Analysis Logic
data class GameAnalysis(
    val biggestGap: Int,
    val leadChanges: Int,
    val totalTurns: Int,
    val dominantPlayer: String?,
    val playerStats: Map<String, PlayerAnalysis>,
    val scoreHistory: Map<String, List<Int>> // Snapshots of scores over time
)

data class PlayerAnalysis(
    val playerName: String,
    val finalScore: Int,
    val averagePerTurn: Double,
    val maxSingleTurn: Int,
    val volatility: Double,
    val timeInLeadPercent: Double,
    val clutchPoints: Int,
    val hotStreak: Int, // Max points in 3 consecutive turns
    val archetype: PlayerArchetype
)

enum class PlayerArchetype(val title: String, val description: String, val icon: ImageVector) {
    JUGGERNAUT("The Juggernaut", "Led the match for most of the duration.", Icons.Default.EmojiEvents),
    SNEAK("The Snake", "Stealthy finish, took the lead at the very end.", Icons.Default.Psychology),
    CLOSER("The Closer", "Strongest performance in the final turns.", Icons.Default.Timer),
    EARLY_BIRD("The Early Bird", "Dominated the early stages of the match.", Icons.Default.Speed),
    ASSASSIN("The Assassin", "Devastating single-turn performance.", Icons.Default.Bolt),
    SNIPER("The Sniper", "Metronomic consistency in point delivery.", Icons.Default.MyLocation),
    STALWART("The Stalwart", "Consistent presence and steady points.", Icons.Default.Shield),
    FIREBALL("The Fireball", "Experienced an incredible scoring hot streak.", Icons.Default.Whatshot),
    COMEBACK("Comeback King", "Strongest performance in the late game.", Icons.AutoMirrored.Filled.TrendingUp),
    HIGH_ROLLER("High Roller", "High risk, high reward scoring patterns.", Icons.Default.Star),
    GRINDER("The Grinder", "Fought hard despite being behind.", Icons.Default.Build),
    TACTICIAN("The Tactician", "Balanced and strategic approach.", Icons.Default.Adjust)
}

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
    val clutchThreshold = (totalScoreEvents * 0.8).toInt()
    val earlyThreshold = (totalScoreEvents * 0.25).toInt()
    val pClutchScore = game.players.associate { it.id to 0 }.toMutableMap()
    val pEarlyScore = game.players.associate { it.id to 0 }.toMutableMap()

    scoreEvents.forEachIndexed { index, event ->
        val player = game.players.find { it.name == event.playerName } ?: return@forEachIndexed
        val pid = player.id
        val points = event.points
        
        pScores[pid] = (pScores[pid] ?: 0) + points
        pHistory[pid]?.add(points)
        
        // Record snapshots for everyone
        game.players.forEach { p ->
            pScoreSnapshots[p.id]?.add(pScores[p.id] ?: 0)
        }

        if (index >= clutchThreshold) {
            pClutchScore[pid] = (pClutchScore[pid] ?: 0) + points
        }
        if (index < earlyThreshold) {
            pEarlyScore[pid] = (pEarlyScore[pid] ?: 0) + points
        }

        // Calculate Gap
        val sortedScores = pScores.values.sortedDescending()
        if (sortedScores.size >= 2) {
            val gap = sortedScores[0] - sortedScores[1]
            if (gap > maxGap) maxGap = gap
        }

        // Track Lead Changes & Time in Lead
        val topPlayerEntry = pScores.maxByOrNull { it.value }
        val topPlayerId = topPlayerEntry?.key
        
        if (topPlayerId != null) {
            pTurnsInLead[topPlayerId] = (pTurnsInLead[topPlayerId] ?: 0) + 1
            if (currentLeaderId != null && currentLeaderId != topPlayerId) {
                leadChanges++
            }
            currentLeaderId = topPlayerId
        }
    }

    val overallAvgScore = if (pScores.isNotEmpty()) pScores.values.average() else 0.0
    val winnerId = pScores.maxByOrNull { it.value }?.key

    val stats = game.players.associate { player ->
        val history = pHistory[player.id] ?: emptyList()
        val totalPoints = pScores[player.id] ?: 0
        val avg = if (history.isNotEmpty()) history.average() else 0.0
        val variance = if (history.isNotEmpty()) history.map { (it - avg) * (it - avg) }.average() else 0.0
        val stdDev = kotlin.math.sqrt(variance)
        val timeInLead = if (totalScoreEvents > 0) (pTurnsInLead[player.id]?.toDouble() ?: 0.0) / totalScoreEvents else 0.0
        
        val clutchPoints = pClutchScore[player.id] ?: 0
        val earlyPoints = pEarlyScore[player.id] ?: 0
        val clutchRatio = if (totalPoints > 0) clutchPoints.toDouble() / totalPoints else 0.0
        val earlyRatio = if (totalPoints > 0) earlyPoints.toDouble() / totalPoints else 0.0
        val maxTurn = history.maxOrNull() ?: 0
        val hotStreakScore = calculateHotStreak(history)
        
        val isWinner = player.id == winnerId
        val leadLate = indexAtLead(pScoreSnapshots[player.id]) > 0.8

        // Determine Archetype (Ordered from most specific/impressive to general)
        val archetype = when {
            timeInLead > 0.7 -> PlayerArchetype.JUGGERNAUT
            isWinner && timeInLead < 0.15 && leadLate -> PlayerArchetype.SNEAK
            clutchRatio > 0.5 && history.size > 3 -> PlayerArchetype.CLOSER
            earlyRatio > 0.5 && history.size > 3 -> PlayerArchetype.EARLY_BIRD
            maxTurn > (totalPoints * 0.6) && totalPoints > 0 -> PlayerArchetype.ASSASSIN
            hotStreakScore > (totalPoints * 0.7) && totalPoints > 0 -> PlayerArchetype.FIREBALL
            isWinner && leadLate && timeInLead < 0.4 -> PlayerArchetype.COMEBACK
            stdDev < (avg * 0.15) && history.size > 4 -> PlayerArchetype.SNIPER
            stdDev > (avg * 1.5) && totalPoints > 0 -> PlayerArchetype.HIGH_ROLLER
            history.all { it > 0 } && history.size > 5 -> PlayerArchetype.STALWART
            totalPoints < overallAvgScore && totalPoints > 0 -> PlayerArchetype.GRINDER
            else -> PlayerArchetype.TACTICIAN
        }
        
        player.name to PlayerAnalysis(
            playerName = player.name,
            finalScore = player.score,
            averagePerTurn = avg,
            maxSingleTurn = history.maxOrNull() ?: 0,
            volatility = stdDev,
            timeInLeadPercent = timeInLead,
            clutchPoints = pClutchScore[player.id] ?: 0,
            hotStreak = calculateHotStreak(history),
            archetype = archetype
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
    var maxStreak = 0
    for (i in 0..history.size - 3) {
        val current = history[i] + history[i+1] + history[i+2]
        if (current > maxStreak) maxStreak = current
    }
    return maxStreak
}

private fun indexAtLead(history: List<Int>?): Double {
    if (history == null || history.size < 2) return 0.0
    val finalScore = history.last()
    if (finalScore <= 0) return 0.0
    val target = finalScore * 0.8
    val index = history.indexOfFirst { it >= target }
    return if (index >= 0) index.toDouble() / history.size else 0.5
}

private fun getPlayerColor(name: String): Color {
    val colors = listOf(
        Color(0xFF0061A4), Color(0xFF006E1C), Color(0xFF984061),
        Color(0xFF6750A4), Color(0xFF8B5000), Color(0xFF006A6A),
        Color(0xFFBA1A1A), Color(0xFF626200)
    )
    if (name.isBlank() || name == "SYSTEM") return Color.Gray
    val index = abs(name.hashCode()) % colors.size
    return colors[index]
}
