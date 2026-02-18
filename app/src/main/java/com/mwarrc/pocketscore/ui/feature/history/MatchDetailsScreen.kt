package com.mwarrc.pocketscore.ui.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*
import com.mwarrc.pocketscore.ui.feature.history.components.match.MatchInsightsTab
import com.mwarrc.pocketscore.ui.feature.history.components.match.MatchTimelineTab

/**
 * Detailed match analysis screen providing in-depth statistics and timeline.
 * 
 * Features three tabs:
 * - **Overview**: Match summary, final scores, duration, and key statistics
 * - **Insights**: Advanced analytics including score momentum, player performance trends
 * - **Timeline**: Chronological event log showing every score change and game action
 * 
 * This screen is accessed from the history list when viewing a completed or active game.
 * 
 * @param game The game state to analyze and display
 * @param onBack Callback to return to the history screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
    game: GameState,
    onBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Insights", "Timeline")
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column(modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
            ) {
                Spacer(Modifier.height(30.dp))
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Match Record",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val date = Date(game.endTime ?: game.startTime)
                            val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(date)
                            Text(
                                dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    windowInsets = WindowInsets(top = 0.dp)
                )
                
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.surface)
        ) {
            when (selectedTabIndex) {
                0 -> MatchOverviewTab(game)
                1 -> MatchInsightsTab(game)
                2 -> MatchTimelineTab(game)
            }
        }
    }
}

@Composable
private fun MatchOverviewTab(game: GameState) {
    val winners = game.players.groupBy { it.score }.maxByOrNull { it.key }?.value ?: emptyList()
    val sortedPlayers = game.players.sortedByDescending { it.score }
    val totalPoints = game.players.sumOf { it.score }
    val avgScore = if (game.players.isNotEmpty()) totalPoints / game.players.size else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Winner Section - Minimal
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "🏆",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    when {
                        winners.size > 2 -> "Multi-Way Tie"
                        winners.size == 2 -> "Tie"
                        else -> "Match Winner"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    winners.joinToString(" & ") { it.name },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${winners.firstOrNull()?.score ?: 0} pts",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Stats Grid
        item {
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val duration = if (game.endTime != null) (game.endTime - game.startTime) / 60000 else 0
                StatBox(modifier = Modifier.weight(1f), label = "Duration", value = "${duration}m")
                StatBox(modifier = Modifier.weight(1f), label = "Total Pts", value = "$totalPoints")
                StatBox(modifier = Modifier.weight(1f), label = "Events", value = "${game.globalEvents.size}")
            }
        }

        // Scoreboard
        item {
             Text(
                "Final Standings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(sortedPlayers) { player ->
            val isWinner = winners.any { it.id == player.id }
            val rank = sortedPlayers.indexOf(player) + 1
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#$rank",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.width(32.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        player.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, // Clean, not bold
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    "${player.score}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = if (isWinner) FontWeight.Black else FontWeight.Bold,
                    color = if (isWinner) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        }

        // Device Info - Subtle footer
        if (game.deviceInfo != null) {
            item {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(0.5f)) {
                    Icon(Icons.Default.Smartphone, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Played on ${game.deviceInfo}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
