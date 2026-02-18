package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * - **Smart Grouping**: Matches are grouped by date (Today, Yesterday, Date headers).
 * - **Management Actions**: Swipe/Menu actions for resume, delete, archive, share, and details.
 * - **Safe Resume**: Multi-option dialog for rejoining matches (new entry vs overwrite).
 * - **Confirmation Guards**: Warning dialogs before permanent deletions.
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
            onConfirm = { shouldReplace ->
                onResumeGame(gameToResume!!, shouldReplace)
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
    if (history.pastGames.isEmpty()) {
        EmptyHistoryPlaceholder()
    } else {
        // Group games by date categories for better readability
        val groupedGames = remember(history.pastGames) {
            groupGamesByDate(history.pastGames)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
        ) {
            groupedGames.forEach { (dateHeader, games) ->
                // Section Header
                item(key = dateHeader) {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .padding(top = 8.dp)
                    )
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
 * Placeholder displayed when no matching game records are found.
 */
@Composable
private fun EmptyHistoryPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No history yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
