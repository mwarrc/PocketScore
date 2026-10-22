package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*

/**
 * A selectable item representing a single match in the settlement list.
 *
 * @param game The game state to display
 * @param isSelected Whether the match is currently selected for settlement
 * @param excludedPlayers Set of player names excluded from this match's cost split
 * @param onToggle Callback when selection state is toggled
 * @param onTogglePlayer Callback when a player's inclusion is toggled
 */
@Composable
fun SplitMatchItem(
    game: GameState,
    isSelected: Boolean,
    excludedPlayers: Set<String>,
    onToggle: () -> Unit,
    onTogglePlayer: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxScore = remember(game) { game.players.maxOfOrNull { it.score } ?: 0 }
    val minScore = remember(game) { game.players.minOfOrNull { it.score } ?: 0 }
    val winners = remember(game) { game.players.filter { it.score == maxScore } }
    val losers = remember(game) { game.players.filter { it.score == minScore } }
    val sortedPlayers = remember(game) { game.players.sortedByDescending { it.score } }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = timeFormat.format(Date(game.endTime ?: game.startTime))

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(220),
        label = "split_match_chevron"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else
            MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
                onClick = onToggle,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    // Match Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Winner: ${winners.joinToString { it.name }}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Subtitle line with time, player count, and loser tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$timeStr • ${game.players.size} players",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (losers.isNotEmpty() && game.players.size > 1 && maxScore != minScore) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "Loser: ${losers.joinToString { it.name }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Excluded indicator
                        AnimatedVisibility(
                            visible = excludedPlayers.isNotEmpty(),
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.PersonOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (excludedPlayers.size == game.players.size)
                                        "All players excluded"
                                    else
                                        "Excluded: ${excludedPlayers.joinToString()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Expand / Collapse player scores chevron
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.graphicsLayer { rotationZ = rotation },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Expanded Player Exclusion Chips
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Tap to toggle player inclusion in split:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        sortedPlayers.forEach { player ->
                            val isExcluded = player.name in excludedPlayers
                            Surface(
                                onClick = { onTogglePlayer(player.name) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (!isExcluded)
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (!isExcluded) Icons.Default.Check else Icons.Default.Block,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (!isExcluded) MaterialTheme.colorScheme.onPrimaryContainer
                                               else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        textDecoration = if (isExcluded) TextDecoration.LineThrough else null,
                                        color = if (!isExcluded) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${player.score} pts",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!isExcluded) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}