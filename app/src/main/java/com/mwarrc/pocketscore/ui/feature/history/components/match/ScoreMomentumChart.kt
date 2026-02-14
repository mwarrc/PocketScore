package com.mwarrc.pocketscore.ui.feature.history.components.match

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

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

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val width = size.width
            val height = size.height
            val gridColor = androidx.compose.ui.graphics.Color.Gray
            val gridAlpha = 0.1f // Very subtle
            
            // Draw Zero Line if visible
            if (minScore < 0 && maxScore > 0) {
                val zeroY = height - ((0 - minScore) / scoreRange) * height
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(0f, zeroY),
                    end = Offset(width, zeroY),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Min/Max/Mid lines
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
                        // Smooth curve if possible? For now linear
                        path.lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Dot at current end
                if (points.isNotEmpty()) {
                    val lastX = ((points.size - 1) / (maxTurns - 1)) * width
                    val lastY = height - ((points.last() - minScore) / scoreRange) * height
                    drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(lastX, lastY))
                }
            }
        }
        
        // Minimal X-Axis Labels (Start - End)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Start", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Text("Finish", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}
