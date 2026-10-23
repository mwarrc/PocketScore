package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tab component that displays a reverse-chronological list of past games,
 * redesigned to match the larger Friends / Players tab styling.
 *
 * Features:
 * - Summary header with match counts and a collapsible tips card
 * - Big rounded match cards with winner avatars and status chips
 * - Paused games surfaced above finished records
 * - Collapsible archived section with smooth chevron animation
 * - Polished empty placeholder
 * - Safe resume and permanent deletion guard dialogs
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

        val totalMatches = activeGames.size + pausedGames.size + archivedGames.size

        Column(modifier = Modifier.fillMaxSize()) {
            // ── Summary header ──
            if (!selectionMode) {
                MatchesSummaryHeader(
                    totalMatches = totalMatches,
                    pausedCount = pausedGames.size,
                    archivedCount = archivedGames.size
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(top = 2.dp, bottom = 120.dp)
            ) {


                // ── Paused matches (in progress) ──
                if (pausedGames.isNotEmpty() && !selectionMode) {
                    item(key = "paused_header") {
                        MatchSectionHeader(
                            icon = Icons.Default.Pause,
                            iconContainer = MaterialTheme.colorScheme.tertiaryContainer,
                            iconContent = MaterialTheme.colorScheme.onTertiaryContainer,
                            title = "Paused matches",
                            subtitle = "${
                                if (pausedGames.size == 1) "1 match" else "${pausedGames.size} matches"
                            } in progress",
                            isExpanded = showPaused,
                            onToggle = { showPaused = !showPaused },
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                if (pausedGames.isNotEmpty() && (showPaused || selectionMode)) {
                    items(
                        pausedGames.sortedByDescending { it.lastUpdate ?: it.startTime },
                        key = { "paused_${it.id}" }
                    ) { game ->
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

                // ── Finished matches grouped by date ──
                groupedGames.forEach { (dateHeader, games) ->
                    item(key = dateHeader) {
                        DateSectionHeader(dateLabel = dateHeader, matchCount = games.size)
                    }

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

                // Nothing active left to show
                if (activeGames.isEmpty() && pausedGames.isEmpty() && !selectionMode) {
                    item(key = "no_active_matches") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No active matches",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Your archived records are kept safe below.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // ── Archived matches (collapsed by default) ──
                if (archivedGames.isNotEmpty() && !selectionMode) {
                    item(key = "archived_header") {
                        MatchSectionHeader(
                            icon = Icons.Default.Archive,
                            iconContainer = MaterialTheme.colorScheme.surfaceContainerHighest,
                            iconContent = MaterialTheme.colorScheme.primary,
                            title = "Archived matches",
                            subtitle = "${
                                if (archivedGames.size == 1) "1 record" else "${archivedGames.size} records"
                            } saved",
                            isExpanded = showArchived,
                            onToggle = { showArchived = !showArchived },
                            modifier = Modifier
                                .padding(top = 14.dp)
                                .animateItem()
                        )
                    }
                }

                if (archivedGames.isNotEmpty() && (showArchived || selectionMode)) {
                    items(
                        archivedGames.sortedByDescending { it.endTime ?: it.startTime },
                        key = { "archived_${it.id}" }
                    ) { game ->
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
}

/**
 * Compact summary line with match counts, mirroring the Friends tab header.
 */
@Composable
private fun MatchesSummaryHeader(
    totalMatches: Int,
    pausedCount: Int,
    archivedCount: Int
) {
    val summary = buildString {
        append(totalMatches)
        append(if (totalMatches == 1) " match" else " matches")
        if (pausedCount > 0) {
            append(" • ")
            append(pausedCount)
            append(" paused")
        }
        if (archivedCount > 0) {
            append(" • ")
            append(archivedCount)
            append(" archived")
        }
    }

    Text(
        text = summary,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

/**
 * Date bucket header with a match-count chip.
 */
@Composable
private fun DateSectionHeader(
    dateLabel: String,
    matchCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 14.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
        ) {
            Text(
                text = if (matchCount == 1) "1 match" else "$matchCount matches",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

/**
 * Expandable section header used for paused and archived matches.
 */
@Composable
private fun MatchSectionHeader(
    icon: ImageVector,
    iconContainer: Color,
    iconContent: Color,
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconContent
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val chevronRotation by animateFloatAsState(
                targetValue = if (isExpanded) 180f else 0f,
                animationSpec = tween(220),
                label = "section_chevron"
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer { rotationZ = chevronRotation },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Placeholder displayed when no game records are found.
 */
@Composable
private fun EmptyHistoryPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 60.dp, start = 32.dp, end = 32.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "No match history yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

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
