package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.Player

@Composable
fun PassivePlayerCard(
    player: Player,
    isLeader: Boolean,
    isCurrent: Boolean,
    isActualTurn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    lastPoints: Int? = null
) {
    val containerColor = when {
        isActualTurn -> MaterialTheme.colorScheme.primaryContainer
        isLeader -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
        isCurrent -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val contentColor = when {
        isActualTurn -> MaterialTheme.colorScheme.onPrimaryContainer
        isLeader -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderStroke = when {
        isCurrent -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = borderStroke,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            // Last Points Indicator
            if (lastPoints != null) {
                val pointsColor = when {
                    lastPoints > 0 -> Color(0xFF4CAF50) // Green
                    lastPoints < 0 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
                val pointsText = when {
                    lastPoints > 0 -> "+$lastPoints"
                    lastPoints < 0 -> "$lastPoints"
                    else -> "0"
                }

                Surface(
                    color = pointsColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = pointsText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pointsColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Leader: visible star field
            // Leader: premium sparkle field
            if (isLeader) {
                val starTint = MaterialTheme.colorScheme.tertiary
                Box(modifier = Modifier.matchParentSize()) {
                    // Top Left
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopStart)
                            .offset(8.dp, 8.dp)
                            .alpha(0.15f)
                            .rotate(-15f),
                        tint = starTint
                    )
                    // Top Right
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset((-24).dp, 6.dp)
                            .alpha(0.12f)
                            .rotate(20f),
                        tint = starTint
                    )
                    // Middle area
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.CenterEnd)
                            .offset((-8).dp, (-12).dp)
                            .alpha(0.1f)
                            .rotate(10f),
                        tint = starTint
                    )
                    // Bottom Left
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.BottomStart)
                            .offset(12.dp, (-8).dp)
                            .alpha(0.15f)
                            .rotate(-10f),
                        tint = starTint
                    )
                    // Bottom Right
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.BottomEnd)
                            .offset((-10).dp, (-6).dp)
                            .alpha(0.18f)
                            .rotate(-5f),
                        tint = starTint
                    )
                }
            }

            if (isActualTurn) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Playing",
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                        .size(16.dp),
                    tint = contentColor
                )
            }

            if (isLeader) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Lead",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Leader",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Text(
                    player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isActualTurn || isLeader) FontWeight.Black else FontWeight.Medium,
                    maxLines = 1,
                    color = contentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${player.score}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}
