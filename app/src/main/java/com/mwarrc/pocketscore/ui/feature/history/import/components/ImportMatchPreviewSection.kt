package com.mwarrc.pocketscore.ui.feature.history.import_.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState

/**
 * Collapsible section that previews all match records in the imported file.
 *
 * Shows up to 8 matches with score bars, player names, and duplicate indicators.
 * If there are more than 8, a "…and N more" overflow label is shown.
 *
 * @param games         All game records from the imported file.
 * @param mappings      Current player name mappings to apply to display names.
 * @param duplicateIds  Set of game IDs that already exist in the local database.
 * @param expanded      Whether the section body is visible.
 * @param onExpandToggle Toggle the expanded state.
 */
@Composable
fun ImportMatchPreviewSection(
    games: List<GameState>,
    mappings: Map<String, String>,
    duplicateIds: Set<String>,
    expanded: Boolean,
    onExpandToggle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            // ── Section header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onExpandToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.History, null,
                            modifier = Modifier.padding(8.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Match Records",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${games.size} total · ${duplicateIds.size} duplicates will be skipped",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Expandable match cards ──
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    games.take(8).forEach { game ->
                        ImportMatchPreviewCard(
                            game = game,
                            mappings = mappings,
                            isDuplicate = game.id in duplicateIds
                        )
                    }
                    if (games.size > 8) {
                        Text(
                            text = "… and ${games.size - 8} more matches",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
