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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Collapsible section that lists all imported player names and lets the user
 * decide whether each one maps to an existing local player (MERGE) or should
 * be created fresh (NEW).
 *
 * @param friends           All player names found in the imported file.
 * @param existingPlayers   Local player names available for mapping.
 * @param playerMappings    Current mapping state: importedName → localName.
 * @param autoMatchedNames  Set of imported names that were auto-detected.
 * @param expanded          Whether the section body is visible.
 * @param onExpandToggle    Toggle the expanded state.
 * @param onMappingChanged  Called when the user changes a mapping.
 *                          Passes (importedName, localName?) — null means "New Player".
 */
@Composable
fun ImportPlayerMappingSection(
    friends: List<String>,
    existingPlayers: List<String>,
    playerMappings: Map<String, String>,
    autoMatchedNames: Set<String>,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onMappingChanged: (String, String?) -> Unit
) {
    val allMapped = playerMappings.size == friends.size

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            // ── Section header (always visible) ──
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
                        color = if (allMapped)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (allMapped) Icons.Default.CheckCircle else Icons.Default.People,
                            contentDescription = null,
                            tint = if (allMapped)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Player Identity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = buildMappingSummary(playerMappings, friends, autoMatchedNames),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Expandable rows ──
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val unavailablePlayers = playerMappings.values.toSet()
                    friends.forEach { importedName ->
                        ImportPlayerMappingRow(
                            importedName = importedName,
                            existingPlayers = existingPlayers,
                            currentMapping = playerMappings[importedName],
                            unavailablePlayers = unavailablePlayers,
                            isAutoMatched = importedName in autoMatchedNames,
                            onMappingChanged = { newMapping ->
                                onMappingChanged(importedName, newMapping)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun buildMappingSummary(
    mappings: Map<String, String>,
    allFriends: List<String>,
    autoMatched: Set<String>
): String {
    if (mappings.isEmpty()) return "Tap to configure player identity"
    val autoCount = mappings.keys.count { it in autoMatched }
    val total = allFriends.size
    return buildString {
        append("${mappings.size}/$total mapped")
        if (autoCount > 0) append(" · $autoCount auto-detected")
    }
}
