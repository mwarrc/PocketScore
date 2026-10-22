package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState

/**
 * Tab component that displays detailed analytical insights about a match.
 * 
 * Features:
 * - **Match Momentum Chart**: Interactive line chart showing score progression
 * - **Interactive Legend**: Toggle player visibility on the chart
 * - **Match Highlights**: Key metrics like biggest gap and lead changes
 * - **Dominance Indicator**: Highlighted section for the player who led the most
 * - **Player Personas**: Detailed cards for each player with assigned archetypes
 * 
 * @param game The game state to analyze and display insights for.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchInsightsTab(game: GameState) {
    // Perform compute-heavy analysis once per game change
    val analysis = remember(game) { analyzeGame(game) }
    
    // UI state for chart filtering
    var visiblePlayers by remember { 
        mutableStateOf(analysis.scoreHistory.keys.toSet()) 
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section 1: Momentum Chart
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Match Momentum", 
                            style = MaterialTheme.typography.titleLarge, 
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Score progression over time", 
                            style = MaterialTheme.typography.labelMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Reset chart filter button
                    if (visiblePlayers.size < analysis.scoreHistory.size) {
                        TextButton(onClick = { 
                            visiblePlayers = analysis.scoreHistory.keys.toSet() 
                        }) {
                            Text("Reset", style = MaterialTheme.typography.labelSmall)
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

                // Interactive Chart Legend
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
                                    // Prevent hiding all players
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

        // Section 2: High-level Highlights
        item {
            Text(
                text = "Match Highlights", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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

        // Section 3: Dominance Spotlight (Conditional)
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
                        Icon(
                            imageVector = Icons.Default.Stars, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.tertiary
                        )
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

        // Section 4: Detailed Player Stats
        item {
            Text(
                text = "Player Personas", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold, 
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(analysis.playerStats.values.sortedByDescending { it.finalScore }) { stats ->
            PlayerDetailedStatsCard(stats)
        }
    }
}
