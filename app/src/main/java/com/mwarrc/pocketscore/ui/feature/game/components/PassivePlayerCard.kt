package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isCurrent -> MaterialTheme.colorScheme.primary
        isActualTurn -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        isLeader -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val borderWidth = when {
        isCurrent || isActualTurn -> 2.dp
        isLeader -> 1.dp
        else -> 0.dp
    }

    val containerColor = when {
        isActualTurn -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        isCurrent -> MaterialTheme.colorScheme.surfaceContainerHigh
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            if (isLeader) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = "Leader",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }

            if (isActualTurn) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Playing",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isActualTurn) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    color = if (isActualTurn) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${player.score}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

