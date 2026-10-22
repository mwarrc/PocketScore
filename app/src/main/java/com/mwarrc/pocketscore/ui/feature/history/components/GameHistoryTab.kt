package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tab component that displays a reverse-chronological list of past games.
 * 
 * Features:
 * - Smart Grouping: Matches grouped with modern date chips & match counts
 * - Refined archived matches header with smooth chevron animations
 * - Polished empty placeholder
 * - Safe Resume & Permanent Deletion guard dialogs
 * 
 * @param history The complete game history to display.
 * @param onNavigateToGame Callback to navigate back to the active game screen.
 * @param onResumeGame Callback to restart a past game with a specific mode (overwrite vs new).
 * @param onDeleteGame Callback to permanently remove a game record.
 * @param onArchiveGame Callback to hide a game from the main history list.
 * @param onShareGame Callback to trigger the system share sheet for a match.
 * @param onViewDetails Callback to open the detailed match analysis view.
 */
@Composable
fun GameHistoryTab(
    history: GameHistory,
    onNavigateToGame: () -> Unit,
    onResumeGame: (GameState, Boolean) -> Unit,
    onDeleteGame: (String) -> Unit,
    onArchiveGame: (String) -> Unit,
    onShareGame: (String) -> Unit,
    onViewDetails: (String) -> Unit,
    selectionMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    onToggleSelection: (String) -> Unit = {},
    onEnterSelectionMode: (String) -> Unit = {}
) {
    // Dialog state management
    var gameToResume by remember { mutableStateOf<GameState?>(null) }
    var gameToDelete by remember { mutableStateOf<GameState?>(null) }

    // Resume Session Logic
    if (gameToResume != null) {
        ResumeGameDialog(
            onDismiss = { gameToResume = null },
            onConfirm = {
                onResumeGame(gameToResume!!, true) // Always replace
                gameToResume = null
                onNavigateToGame()
            }
        )
    }

    // Permanent Deletion Logic
    if (gameToDelete != null) {
        DeleteGameDialog(
            onDismiss = { gameToDelete = null },
            onConfirm = {
                onDeleteGame(gameToDelete!!.id)
                gameToDelete = null
            }
        )
    }

    // Main UI
    val activeGames = remember(history.pastGames) {
        history.pastGames.filter { !it.isArchived && !it.isPaused && it.players.isNotEmpty() }
    }
    
    val pausedGames = remember(history.pastGames) {
        history.pastGames.filter { it.isPaused && !it.isArchived && it.players.isNotEmpty() }
    }
    
    val archivedGames = remember(history.pastGames) {
        history.pastGames.filter { it.isArchived && it.players.isNotEmpty() }
    }

    if (activeGames.isEmpty() && archivedGames.isEmpty() && pausedGames.isEmpty()) {
        EmptyHistoryPlaceholder()
    } else {
        val groupedGames = remember(activeGames) {
            groupGamesByDate(activeGames)
        }
        
        var showArchived by remember { mutableStateOf(false) }
        var showPaused by remember { mutableStateOf(true) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp)
        ) {
            // Archived Section Header (hidden in selection mode)
            if (archivedGames.isNotEmpty() && !selectionMode) {
                item {
                    ArchivedMatchesHeader(
                        count = archivedGames.size,
                        isExpanded = showArchived,
                        onToggle = { showArchived = !showArchived }
                    )
                }
            }

            // Archived games list - always shown (in selection mode OR when expanded)
            if (archivedGames.isNotEmpty() && (showArchived || selectionMode)) {
                items(archivedGames.sortedByDescending { it.endTime ?: it.startTime }, key = { "archived_${it.id}" }) { game ->
                    GameHistoryCard(
                        game = game,
                        onDelete = { gameToDelete = game },
                        onArchive = { onArchiveGame(game.id) },
                        onResume = { gameToResume = game },
                        onShare = { onShareGame(game.id) },
                        onViewDetails = { onViewDetails(game.id) },
                        selectionMode = selectionMode,
                        isSelected = game.id in selectedIds,
                        onToggleSelection = { onToggleSelection(game.id) },
                        onEnterSelectionMode = { onEnterSelectionMode(game.id) }
                    )
                }
                
                if (!selectionMode) {
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 18.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Paused Games Section
            if (pausedGames.isNotEmpty() && !selectionMode) {
                item(key = "paused_header") {
                    PausedMatchesHeader(
                        count = pausedGames.size,
                        isExpanded = showPaused,
                        onToggle = { showPaused = !showPaused }
                    )
                }
            }

            if (pausedGames.isNotEmpty() && (showPaused || selectionMode)) {
                items(pausedGames.sortedByDescending { it.lastUpdate ?: it.startTime }, key = { "paused_${it.id}" }) { game ->
                    GameHistoryCard(
                        game = game,
                        onDelete = { gameToDelete = game },
                        onArchive = { onArchiveGame(game.id) },
                        onResume = { gameToResume = game },
                        onShare = { onShareGame(game.id) },
                        onViewDetails = { onViewDetails(game.id) },
                        selectionMode = selectionMode,
                        isSelected = game.id in selectedIds,
                        onToggleSelection = { onToggleSelection(game.id) },
                        onEnterSelectionMode = { onEnterSelectionMode(game.id) }
                    )
                }
                
                if (!selectionMode) {
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 18.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            if (activeGames.isEmpty() && !showArchived && !showPaused) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active matches",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            groupedGames.forEach { (dateHeader, games) ->
                // Modern Section Header with count chip
                item(key = dateHeader) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = if (games.size == 1) "1 match" else "${games.size} matches",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Individual Match Cards
                items(games, key = { it.id }) { game ->
                    GameHistoryCard(
                        game = game,
                        onDelete = { gameToDelete = game },
                        onArchive = { onArchiveGame(game.id) },
                        onResume = { gameToResume = game },
                        onShare = { onShareGame(game.id) },
                        onViewDetails = { onViewDetails(game.id) },
                        selectionMode = selectionMode,
                        isSelected = game.id in selectedIds,
                        onToggleSelection = { onToggleSelection(game.id) },
                        onEnterSelectionMode = { onEnterSelectionMode(game.id) }
                    )
                }
            }
        }
    }
}

/**
 * Header for the archived matches section.
 */
@Composable
private fun ArchivedMatchesHeader(
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Archived Matches",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$count records saved",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val chevronRotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(220),
                label = "archived_chevron"
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Header for the paused matches section.
 */
@Composable
private fun PausedMatchesHeader(
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Paused Matches",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$count ${if (count == 1) "match" else "matches"} in progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val chevronRotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(220),
                label = "paused_chevron"
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Placeholder displayed when no matching game records are found.
 */
@Composable
private fun EmptyHistoryPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "No match history yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Play a game and your finished scoreboards will be recorded here automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Helper function to group games into human-readable date buckets.
 */
private fun groupGamesByDate(games: List<GameState>): Map<String, List<GameState>> {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
        Date(System.currentTimeMillis() - 86400000)
    )
    val headerFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

    return games
        .distinctBy { it.id }
        .sortedByDescending { it.endTime ?: it.startTime }
        .groupBy { game ->
            val date = Date(game.endTime ?: game.startTime)
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            
            when (dateKey) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> {
                    val gameYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(date)
                    if (gameYear == currentYear) {
                        headerFormat.format(date)
                    } else {
                        SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(date)
                    }
                }
            }
        }
}
