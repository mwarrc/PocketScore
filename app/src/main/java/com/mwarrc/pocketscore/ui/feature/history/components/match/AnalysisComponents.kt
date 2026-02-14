package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InsightCard(
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
        color = MaterialTheme.colorScheme.surfaceContainerLow, // Cleaner background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.1f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(subValue, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun PlayerDetailedStatsCard(stats: PlayerAnalysis) {
    val playerColor = getPlayerColor(stats.playerName)
    val archetype = stats.archetype
    // Use Error color for negative archetypes unless it's just neutral
    val archetypeColor = if (archetype.isPositive) playerColor else MaterialTheme.colorScheme.error
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer, // Minimal container
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = archetypeColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(archetype.icon, null, tint = archetypeColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stats.playerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(archetype.title, style = MaterialTheme.typography.labelMedium, color = archetypeColor, fontWeight = FontWeight.Bold)
                }
                Text("${stats.finalScore}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            }
            
            Text(
                archetype.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            HorizontalDivider(modifier = Modifier.alpha(0.1f))
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AnalysisStatItem("Avg/Turn", String.format("%.1f", stats.averagePerTurn))
                AnalysisStatItem("Volatility", String.format("%.1f", stats.volatility))
                AnalysisStatItem("Zero Turns", "${stats.zeroTurns}")
                AnalysisStatItem("Best Streak", "${stats.hotStreak}")
            }
            
            if (stats.timeInLeadPercent > 0) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { stats.timeInLeadPercent.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = playerColor,
                    trackColor = playerColor.copy(alpha = 0.1f),
                )
                Text(
                    "Lead for ${String.format("%.0f", stats.timeInLeadPercent * 100)}% of match",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalysisStatItem(label: String, value: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
