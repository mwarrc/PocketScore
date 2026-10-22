package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
 * - **Smooth Curves**: Uses bezier curves instead of jagged lines.
 * - **Leader Focus**: Adds a prominent gradient fill under the winning player's curve.
 * - **Filter Support**: Renders only the players currently selected in the legend.
 * - **Zero-Line**: Visual indicator if any players dipped into negative scores.
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
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data to visualize", style = MaterialTheme.typography.labelMedium)
        }
        return
    }

    // Coordinate scaling calculations
    val minScore = (allVisibleScores.minOrNull() ?: 0).toFloat()
    val maxScore = (allVisibleScores.maxOrNull() ?: 100).toFloat().coerceAtLeast(minScore + 1f)
    val scoreRange = maxScore - minScore
    val maxTurns = (history.values.maxOfOrNull { it.size } ?: 1).toFloat()

    // Find the leader's final score to highlight their path
    val leaderFinalScore = filteredHistory.values.maxOfOrNull { it.lastOrNull() ?: Int.MIN_VALUE } ?: Int.MIN_VALUE

    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val gridColor = Color.Gray
                val gridAlpha = 0.2f // Stronger grid for precision
                
                // 1. Draw Zero Line (if cross-over occurs)
                if (minScore < 0 && maxScore > 0) {
                    val zeroY = height - ((0 - minScore) / scoreRange) * height
                    drawLine(
                        color = gridColor.copy(alpha = 0.5f),
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
                    
                    var prevX = 0f
                    var prevY = 0f
                    
                    points.forEachIndexed { index, score ->
                        val x = if (maxTurns > 1) (index / (maxTurns - 1)) * width else 0f
                        val scoreOffset = score - minScore
                        val y = height - (scoreOffset / scoreRange) * height
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            val cx = (prevX + x) / 2f
                            path.cubicTo(cx, prevY, cx, y, x, y)
                        }
                        prevX = x
                        prevY = y
                    }
                    
                    val isLeader = points.lastOrNull() == leaderFinalScore

                    // Draw focus gradient for the leader
                    if (isLeader && points.isNotEmpty()) {
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(prevX, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )
                    }

                    // Stroke strength: leader gets slightly thicker line
                    val strokeWidth = if (isLeader) 4.dp.toPx() else 2.dp.toPx()
                    val lineAlpha = if (isLeader) 1f else 0.6f

                    // Draw the line
                    drawPath(
                        path = path,
                        color = color.copy(alpha = lineAlpha),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Draw a small indicator dot at the final point
                    if (points.isNotEmpty()) {
                        val lastX = if (maxTurns > 1) ((points.size - 1) / (maxTurns - 1)) * width else 0f
                        val lastY = height - ((points.last() - minScore) / scoreRange) * height
                        drawCircle(
                            color = color, 
                            radius = if (isLeader) 5.dp.toPx() else 3.dp.toPx(), 
                            center = Offset(lastX, lastY)
                        )
                    }
                }
            }
            
            // Y-Axis Labels overlay
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 2.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(maxScore.toInt().toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(((maxScore + minScore) / 2).toInt().toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(minScore.toInt().toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
        
        // Match Timeline Labels (X-Axis)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Match Start", 
                style = MaterialTheme.typography.labelMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
            Text(
                "Final Score", 
                style = MaterialTheme.typography.labelMedium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            )
        }
    }
}
