package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tab component that handles match cost settlement between players.
 *
 * Features:
 * - Match selection: Choose which games to include in the session settlement.
 * - Dynamic Calculation: Automatically recalculates debts based on selected games and rules.
 * - Settlement Rules: Support for Loser Pays (sole loser), Non-Winners, All Split, and Bottom X.
 * - Interactive Statistics: See exactly how many matches contribute to each player's debt.
 * - Date Grouping: Matches are grouped by date for easier selection.
 *
 * @param history Complete game history
 * @param settings App settings containing cost and settlement preferences
 * @param onUpdateSettings Callback to persist settlement rule changes
 */
@Composable
fun MatchSplitTab(
    history: GameHistory,
    settings: AppSettings,
    selectedMatchIds: Set<String>,
    onSelectMatches: (Set<String>) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    // View state for history length
    var showAllHistory by remember { mutableStateOf(false) }

    // Prepare display data with 7-day filter
    val (displayGames, hasOlderGames) = remember(history, showAllHistory) {
        val allFinalized = history.pastGames
            .filter { it.isFinalized && !it.isArchived }
            .sortedByDescending { it.endTime ?: it.startTime }

        if (showAllHistory) {
            allFinalized to false
        } else {
            val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
            val recentGames = allFinalized.filter {
                val time = it.endTime ?: it.startTime
                time > sevenDaysAgo
            }
            recentGames to (allFinalized.size > recentGames.size)
        }
    }

    // Perform calculations for selected matches
    val calculation = remember(displayGames, selectedMatchIds, settings) {
        val selectedGames = displayGames.filter { it.id in selectedMatchIds }
        SplitCalculator.calculate(selectedGames, settings)
    }

    // Grouping for the match list
    val groupedGames = remember(displayGames) {
        groupGamesByDate(displayGames)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Summary Header & Settings Card
        item {
            SplitSettingsCard(
                totalAmount = calculation.totalAmount,
                settings = settings,
                onUpdateSettings = onUpdateSettings
            )
        }

        // 2. Debts Section (visible when matches are selected)
        if (calculation.playerDebts.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settlement Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${calculation.playerDebts.size} payers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(calculation.playerDebts, key = { it.first }) { (name, amount) ->
                val selectedGames = displayGames.filter { it.id in selectedMatchIds }
                val participationCount = selectedGames.count { g ->
                    val excluded = settings.matchExcludedPlayers[g.id].orEmpty()
                    g.players.any { it.name == name } && name !in excluded
                }

                SplitResultCard(
                    name = name,
                    amount = amount,
                    matchCount = participationCount,
                    currencySymbol = settings.currencySymbol,
                    decimals = settings.settlementRoundingDecimals
                )
            }
        }

        // 3. Selection Controls
        if (displayGames.isNotEmpty()) {
            item {
                SelectionControls(
                    displayGames = displayGames,
                    onSelectChange = onSelectMatches,
                    currentSelection = selectedMatchIds
                )
            }
        }

        // 4. Grouped Match List
        groupedGames.forEach { (dateHeader, games) ->
            item(key = "header_$dateHeader") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${games.size} ${if (games.size == 1) "game" else "games"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(games, key = { it.id }) { game ->
                val isSelected = game.id in selectedMatchIds
                val excludedPlayers = settings.matchExcludedPlayers[game.id].orEmpty()

                SplitMatchItem(
                    game = game,
                    isSelected = isSelected,
                    excludedPlayers = excludedPlayers,
                    onToggle = {
                        val nextSelection = if (isSelected) {
                            selectedMatchIds - game.id
                        } else {
                            selectedMatchIds + game.id
                        }
                        onSelectMatches(nextSelection)
                    },
                    onTogglePlayer = { playerName ->
                        onUpdateSettings { s ->
                            s.toggleMatchPlayerExclusion(game.id, playerName)
                        }
                    }
                )
            }
        }

        // 5. Empty State
        if (displayGames.isEmpty()) {
            item {
                EmptyMatchesState()
            }
        } else if (hasOlderGames && !showAllHistory) {
            item {
                TextButton(
                    onClick = { showAllHistory = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text("Show older records", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Helper to group games into human-readable date categories.
 */
private fun groupGamesByDate(games: List<GameState>): Map<String, List<GameState>> {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = fmt.format(Date())
    val yesterday = fmt.format(Date(System.currentTimeMillis() - 86_400_000L))
    val headerFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

    return games
        .distinctBy { it.id }
        .groupBy { game ->
            val date = Date(game.endTime ?: game.startTime)
            val dateKey = fmt.format(date)
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

/**
 * UI controls for bulk selection and clearing of matches.
 */
@Composable
private fun SelectionControls(
    displayGames: List<GameState>,
    onSelectChange: (Set<String>) -> Unit,
    currentSelection: Set<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Matches",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (currentSelection.isNotEmpty()) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${currentSelection.size} of ${displayGames.size} selected",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Quick selection chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val today = remember {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }

            Surface(
                onClick = {
                    onSelectChange(displayGames.map { it.id }.toSet())
                },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "Select All",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Surface(
                onClick = {
                    val todayIds = displayGames.filter {
                        val date = Date(it.endTime ?: it.startTime)
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) == today
                    }.map { it.id }.toSet()
                    onSelectChange(todayIds)
                },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = "Today Only",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            if (currentSelection.isNotEmpty()) {
                Surface(
                    onClick = { onSelectChange(emptySet()) },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * Placeholder displayed when no finalized matches are available for splitting.
 */
@Composable
private fun EmptyMatchesState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "No completed matches found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Finish games to split table costs between players.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}