package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * - Settlement Rules: Support for Losers Pay, All Split, and Bottom X Pay.
 * - Interactive Statistics: See exactly how many matches contribute to each player's debt.
 * - Date Grouping: Matches are grouped by date for easier selection.
 *
 * Bug fix: Player exclusion toggle now correctly resets the match item's excluded-players
 * indicator row. Previously, toggling a player back IN left a stale non-empty excluded set
 * in the map (empty Set<String> instead of a removed key), causing the "EXCLUDED:" row to
 * persist at zero-height and the card to not resize. The fix is two-part:
 *   1. The excluded players passed to SplitMatchItem are derived with an explicit emptySet()
 *      fallback so a missing key and an empty-set key both behave identically.
 *   2. The excluded-players indicator in SplitMatchItem is wrapped in AnimatedVisibility
 *      keyed off `excludedPlayers.isNotEmpty()` so it properly animates out when the set
 *      empties, collapsing the card height back to default.
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
        contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Summary Header & Settings
        item {
            SplitSettingsCard(
                totalAmount = calculation.totalAmount,
                settings = settings,
                onUpdateSettings = onUpdateSettings
            )
        }

        // 2. Debts Section (visible only when matches are selected)
        if (calculation.playerDebts.isNotEmpty()) {
            item {
                Text(
                    text = "Owed per Player",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            items(calculation.playerDebts, key = { it.first }) { (name, amount) ->
                val selectedGames = displayGames.filter { it.id in selectedMatchIds }
                val participationCount = selectedGames.count { g ->
                    // Use the same normalised exclusion lookup as below
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
        item {
            SelectionControls(
                displayGames = displayGames,
                onSelectChange = onSelectMatches,
                currentSelection = selectedMatchIds
            )
        }

        // 4. Grouped Match List
        groupedGames.forEach { (dateHeader, games) ->
            item(key = "header_$dateHeader") {
                Text(
                    text = dateHeader,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }

            items(games, key = { it.id }) { game ->
                val isSelected = game.id in selectedMatchIds

                // FIX: Normalise the excluded set so that a missing key and an empty-set key
                // are indistinguishable. This ensures that after the last player exclusion is
                // removed the set is genuinely empty regardless of how the map was cleaned up.
                val excludedPlayers = settings.matchExcludedPlayers[game.id].orEmpty()

                SplitMatchItem(
                    game = game,
                    isSelected = isSelected,
                    // Pass a canonical empty set when nothing is excluded so SplitMatchItem
                    // can drive AnimatedVisibility correctly (see SplitMatchItem fix below).
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
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Show older records", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "Select Matches",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val today = remember {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }

            TextButton(
                onClick = {
                    val todayIds = displayGames.filter {
                        val date = Date(it.endTime ?: it.startTime)
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) == today
                    }.map { it.id }
                    onSelectChange(currentSelection + todayIds)
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Select Today", style = MaterialTheme.typography.labelMedium)
            }

            TextButton(
                onClick = { onSelectChange(emptySet()) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Clear All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
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
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No finished matches found.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}