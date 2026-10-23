package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
 * Modernized to follow Material 3 design specifications.
 * 
 * Features:
 * - **Match Momentum Chart Card**: Elevated card container with interactive line chart and player toggle chips
 * - **Match Highlights Bento**: Bento tiles for biggest gap and lead changes
 * - **Dominance Indicator Banner**: Highlighted card for the player who dominated the lead
 * - **Player Personas**: Detailed cards for each player with assigned archetypes and metric grids
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
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Section 1: Momentum Chart in Elevated M3 Card ──
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Match Momentum", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Score progression over time", 
                                    style = MaterialTheme.typography.labelSmall, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Reset chart filter button
                        if (visiblePlayers.size < analysis.scoreHistory.size) {
                            FilledTonalButton(
                                onClick = { 
                                    visiblePlayers = analysis.scoreHistory.keys.toSet() 
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Reset", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    ScoreMomentumChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        history = analysis.scoreHistory,
                        visiblePlayers = visiblePlayers
                    )

                    Spacer(Modifier.height(12.dp))

                    // Interactive Chart Legend Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(analysis.scoreHistory.keys.toList(), key = { it }) { name ->
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
                                label = { Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) color else color.copy(alpha = 0.35f))
                                    )
                                },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.12f),
                                    selectedLabelColor = color
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = color,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // ── Section 2: High-level Highlights Bento ──
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Match Highlights", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
                        subValue = "Leader shifts",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── Section 3: Dominance Spotlight (Conditional) ──
        if (analysis.dominantPlayer != null) {
            item {
                val stats = analysis.playerStats[analysis.dominantPlayer]
                val leadPercent = (stats?.timeInLeadPercent ?: 0.0) * 100

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Match Dominance: ${analysis.dominantPlayer}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Controlled the lead for ${String.format("%.0f", leadPercent)}% of the match.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ── Section 4: Player Personas Header & Cards ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Player Personas", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${analysis.playerStats.size} analyzed",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(analysis.playerStats.values.sortedByDescending { it.finalScore }, key = { it.playerName }) { stats ->
            PlayerDetailedStatsCard(stats)
        }
    }
}
