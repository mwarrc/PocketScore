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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
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
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.Player

/**
 * Compact read-only card for a player in Grid layout.
 * 
 * Used for all players in the grid who are not currently the primary focus.
 * Shows status (Leader, Loser, Eliminated) and core score information.
 * 
 * @param player The player data
 * @param isLeader Whether this player is currently in the lead
 * @param isTie Whether the lead is shared
 * @param isLoser Whether this player is currently in last place
 * @param isLoserTie Whether the last place is shared
 * @param isCurrent Whether this player is currently focused in the header
 * @param isActualTurn Whether this player is currently the active turn holder
 * @param onClick Callback when the card is tapped to focus it
 * @param modifier Root modifier
 * @param lastPoints Last points change for this player
 * @param leaderScore Leader's score for contender math
 * @param tableSum Table sum for contender math
 * @param poolBallManagementEnabled Whether to perform contender math
 */
@Composable
fun PassivePlayerCard(
    player: Player,
    isLeader: Boolean,
    isTie: Boolean = false,
    isLoser: Boolean = false,
    isLoserTie: Boolean = false,
    isCurrent: Boolean,
    isActualTurn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    lastPoints: Int? = null,
    leaderScore: Int = 0,
    tableSum: Int = 0,
    poolBallManagementEnabled: Boolean = true
) {
    // Calculate if player is eliminated - Only if pool management is enabled
    val potentialMax = player.score + tableSum
    val isEliminated = poolBallManagementEnabled && !isLeader && potentialMax < leaderScore && tableSum > 0
    
    val containerColor = when {
        isEliminated -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        isActualTurn -> MaterialTheme.colorScheme.primaryContainer
        isLeader -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f) // Increased visibility
        isLoser -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        isCurrent -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val contentColor = when {
        isActualTurn -> MaterialTheme.colorScheme.onPrimaryContainer
        isLeader -> MaterialTheme.colorScheme.onTertiaryContainer
        isLoser -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    val borderStroke = when {
        isEliminated -> BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
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
            // Eliminated overlay effect
            if (isEliminated) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.6f)
                )
                
                // Eliminated icon
                Icon(
                    androidx.compose.material.icons.Icons.Default.Block,
                    contentDescription = "Eliminated",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                        .alpha(0.15f),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            
            // Last Points Indicator
            if (lastPoints != null && !isEliminated) {
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

            if (isLeader || isLoser) {
                val badgeColor = if (isLeader) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                val onBadgeColor = if (isLeader) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                val label = when {
                    isLeader && isTie -> "TIED"
                    isLeader -> "LEADER"
                    isLoser && isLoserTie -> "TIED"
                    else -> "LOSER"
                }
                val icon = when {
                    isLeader && isTie -> Icons.Default.Group
                    isLeader -> Icons.Default.EmojiEvents
                    else -> null // No icon for loser to make it look "less premium"
                }

                Surface(
                    color = badgeColor,
                    contentColor = onBadgeColor,
                    shape = RoundedCornerShape(bottomStart = 12.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        if (icon != null) {
                            Icon(
                                icon,
                                contentDescription = label,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .alpha(if (isEliminated) 0.5f else 1f)
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
                
                // Eliminated label
                if (isEliminated) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "ELIMINATED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.error,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
