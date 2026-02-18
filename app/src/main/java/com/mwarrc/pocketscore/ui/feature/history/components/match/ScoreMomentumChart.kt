package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A custom line chart displaying the score progression (momentum) of players over time.
 * 
 * Features:
 * - **Dynamic Scaling**: Automatically adjusts Y-axis based on min/max scores achieved.
 * - **Lead Tracking**: Highlights which player was leading at any given turnout.
 * - **Filter Support**: Renders only the players currently selected in the legend.
 * - **Zero-Line**: Visual indicator if any players dipped into negative scores.
 * - **Smooth Rendering**: Uses path drawing with rounded caps for a premium feel.
 * 
 * @param modifier Modifier for the chart container.
 * @param history Map of player names to their score snapshots over the match.
 * @param visiblePlayers Set of players to include in the render (for filtering).
 */
@Composable
fun ScoreMomentumChart(
    modifier: Modifier = Modifier,
    history: Map<String, List<Int>>,
    visiblePlayers: Set<String> = history.keys
) {
    // Filter and aggregate data for scaling calculations
    val filteredHistory = history.filterKeys { it in visiblePlayers }
    val allVisibleScores = filteredHistory.values.flatten()
    
    // Guard against empty data
    if (allVisibleScores.isEmpty()) {
        Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("No data to visualize", style = MaterialTheme.typography.labelSmall)
        }
        return
    }

    // Coordinate scaling calculations
    val minScore = (allVisibleScores.minOrNull() ?: 0).toFloat()
    val maxScore = (allVisibleScores.maxOrNull() ?: 100).toFloat().coerceAtLeast(minScore + 1f)
    val scoreRange = maxScore - minScore
    val maxTurns = (history.values.maxOfOrNull { it.size } ?: 1).toFloat()

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val width = size.width
            val height = size.height
            val gridColor = Color.Gray
            val gridAlpha = 0.1f // Light grid for precision
            
            // 1. Draw Zero Line (if cross-over occurs)
            if (minScore < 0 && maxScore > 0) {
                val zeroY = height - ((0 - minScore) / scoreRange) * height
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(0f, zeroY),
                    end = Offset(width, zeroY),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // 2. Draw Horizontal Grid Lines (Min, Mid, Max)
            drawLine(gridColor.copy(alpha = gridAlpha), Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1.dp.toPx())
            drawLine(gridColor.copy(alpha = gridAlpha), Offset(0f, height/2), Offset(width, height/2), strokeWidth = 1.dp.toPx())
            drawLine(gridColor.copy(alpha = gridAlpha), Offset(0f, height), Offset(width, height), strokeWidth = 1.dp.toPx())

            // 3. Draw Progression Paths for each player
            filteredHistory.forEach { (name, points) ->
                val color = getPlayerColor(name)
                val path = Path()
                
                points.forEachIndexed { index, score ->
                    // Calculate X/Y coordinates based on turn index and score value
                    val x = if (maxTurns > 1) (index / (maxTurns - 1)) * width else 0f
                    val scoreOffset = score - minScore
                    val y = height - (scoreOffset / scoreRange) * height
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                // Draw the line
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw a small indicator dot at the final point
                if (points.isNotEmpty()) {
                    val lastX = if (maxTurns > 1) ((points.size - 1) / (maxTurns - 1)) * width else 0f
                    val lastY = height - ((points.last() - minScore) / scoreRange) * height
                    drawCircle(
                        color = color, 
                        radius = 3.dp.toPx(), 
                        center = Offset(lastX, lastY)
                    )
                }
            }
        }
        
        // Match Timeline Labels (X-Axis)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Match Start", 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                "Final Score", 
                style = MaterialTheme.typography.labelSmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
