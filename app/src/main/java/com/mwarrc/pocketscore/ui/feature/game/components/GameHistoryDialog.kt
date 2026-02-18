package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor
import com.mwarrc.pocketscore.core.time.formatTimeAgo
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType

/**
 * An audit dialog showing the chronological log of game events.
 * 
 * Supports filtering by "Global" (all events) or by specific player names
 * using a scrollable tab row. Events include scores, undos, and rule changes.
 * 
 * @param events List of events for the current match
 * @param playerNames Names of active players to generate filter tabs
 * @param onDismiss Callback to close the dialog
 */
@Composable
fun GameHistoryDialog(
    events: List<GameEvent>,
    playerNames: List<String>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Global") + playerNames

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Match History",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Audit every point change",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                // Modern Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 24.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, name ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                // Content
                val filteredEvents = remember(selectedTab, events) {
                    if (selectedTab == 0) {
                        events.reversed()
                    } else {
                        val playerName = tabs[selectedTab]
                        events.filter { it.playerName == playerName }.reversed()
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (filteredEvents.isEmpty()) {
                        EmptyHistoryState(
                            message = if (selectedTab == 0) "No events recorded yet." else "${tabs[selectedTab]} hasn't scored yet."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredEvents, key = { it.id }) { event ->
                                ImprovedEventItem(event)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ImprovedEventItem(event: GameEvent) {
    val playerColor = getMaterialPlayerColor(event.playerName)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val containerColor = when {
        event.type == GameEventType.UNDO -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
        event.type == GameEventType.STATUS_CHANGE -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        else -> playerColor.copy(alpha = if (isDark) 0.12f else 0.08f)
    }

    val icon = when {
        event.type == GameEventType.UNDO -> Icons.AutoMirrored.Filled.Undo
        event.type == GameEventType.STATUS_CHANGE -> Icons.Default.Warning
        else -> Icons.Default.History
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            else playerColor.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon / Avatar
            Surface(
                shape = CircleShape,
                color = if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.error else playerColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.onError else Color.White
                    )
                }
            }

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                val title = when (event.type) {
                    GameEventType.SCORE -> {
                        if (event.previousScore != null && event.newScore != null) {
                            "${event.playerName}: ${event.previousScore} ➝ ${event.newScore}"
                        } else {
                            "${event.playerName}"
                        }
                    }
                    GameEventType.UNDO -> "Action Undone"
                    GameEventType.CORRECTION -> "Correction: ${event.playerName}"
                    GameEventType.STATUS_CHANGE -> "Rule Change"
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = when (event.type) {
                        GameEventType.SCORE -> "Added ${event.points} points"
                        GameEventType.UNDO -> "Reverted ${event.points} points for ${event.playerName}"
                        GameEventType.STATUS_CHANGE -> event.message ?: "Game status updated"
                        else -> "Manual adjustment"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))
                
                Text(
                    text = formatTimeAgo(event.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }

            // Points Badge
            if (event.type == GameEventType.SCORE || event.type == GameEventType.UNDO) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.error else playerColor).copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${if (event.points > 0 && event.type != GameEventType.UNDO) "+" else ""}${event.points}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.error else playerColor
                    )
                }
            }
        }
    }
}

