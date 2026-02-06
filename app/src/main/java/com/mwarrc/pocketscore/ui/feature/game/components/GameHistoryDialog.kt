package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mwarrc.pocketscore.core.time.formatTimeAgo
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType

@Composable
fun GameHistoryDialog(
    events: List<GameEvent>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Game Audit Log",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                if (events.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No events yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(events.reversed()) { event ->
                            EventItem(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getPlayerColor(name: String): Color {
    val colors = listOf(
        Color(0xFF0061A4), // Vibrant Blue
        Color(0xFF006E1C), // Forest Green
        Color(0xFF984061), // Berry/Pink
        Color(0xFF6750A4), // Deep Purple
        Color(0xFF8B5000), // Rich Orange
        Color(0xFF006A6A), // Teal
        Color(0xFFBA1A1A), // Bright Red
        Color(0xFF626200)  // Olive/Gold
    )
    if (name.isBlank()) return MaterialTheme.colorScheme.surfaceVariant
    val index = Math.abs(name.hashCode()) % colors.size
    return colors[index]
}

@Composable
private fun EventItem(event: GameEvent) {
    val playerColor = if (!event.playerName.isNullOrBlank()) {
        getPlayerColor(event.playerName)
    } else {
        null
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val cardColor = when {
        event.type == GameEventType.UNDO -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        event.type == GameEventType.STATUS_CHANGE -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        event.isZeroInput -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        playerColor != null -> playerColor.copy(alpha = if (isDark) 0.25f else 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val icon = when {
        event.type == GameEventType.UNDO -> Icons.AutoMirrored.Filled.Undo
        event.type == GameEventType.STATUS_CHANGE -> Icons.Default.Warning
        event.isZeroInput -> Icons.Default.Warning
        else -> Icons.Default.History
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp, 
            playerColor?.copy(alpha = 0.4f) ?: MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Accent Bar - Now more prominent
            if (playerColor != null) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(72.dp)
                        .background(playerColor.copy(alpha = 0.8f))
                )
            }

            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when {
                        event.type == GameEventType.STATUS_CHANGE -> MaterialTheme.colorScheme.primary
                        playerColor != null -> playerColor.copy(alpha = if (isDark) 0.9f else 1f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val text = when (event.type) {
                        GameEventType.SCORE -> {
                            if (event.previousScore != null && event.newScore != null) {
                                "${event.playerName}: ${event.previousScore} ➝ ${event.newScore}"
                            } else {
                                "${event.playerName} scored ${event.points}"
                            }
                        }
                        GameEventType.UNDO -> {
                            if (event.previousScore != null && event.newScore != null) {
                                "Undo: ${event.previousScore} ➝ ${event.newScore}"
                            } else {
                                "Undo action on ${event.playerName}"
                            }
                        }
                        GameEventType.CORRECTION -> "Correction on ${event.playerName}"
                        GameEventType.STATUS_CHANGE -> event.message ?: "Status changed for ${event.playerName}"
                    }

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = playerColor?.copy(alpha = if (isDark) 1f else 0.85f) ?: MaterialTheme.colorScheme.onSurface
                    )

                    when {
                        event.isZeroInput -> {
                            Text(
                                text = "ZERO INPUT DETECTED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        event.type == GameEventType.STATUS_CHANGE -> {
                            Text(
                                text = "IMPORTANT: GAME ELIGIBILITY UPDATED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        else -> {
                            Text(
                                text = formatTimeAgo(event.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (event.type == GameEventType.SCORE) {
                    Text(
                        text = "${if (event.points > 0) "+" else ""}${event.points}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = playerColor?.copy(alpha = if (isDark) 1f else 0.9f) ?: MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

