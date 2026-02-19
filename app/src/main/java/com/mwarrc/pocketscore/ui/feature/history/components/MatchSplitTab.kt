package com.mwarrc.pocketscore.ui.feature.history.components

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

    // Prepare display data
    val displayGames = remember(history) {
        history.pastGames.filter { it.isFinalized }
            .sortedByDescending { it.endTime ?: it.startTime }
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
        // 1. Summary Header & Settings
        item {
            SplitSettingsCard(
                totalAmount = calculation.totalAmount,
                settings = settings,
                onUpdateSettings = onUpdateSettings
            )
        }

        // 2. Debts Section (Visible only if matches are selected)
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
                val participationCount = selectedGames.count { g -> g.players.any { it.name == name } }
                
                SplitResultCard(
                    name = name,
                    amount = amount,
                    matchCount = participationCount,
                    currencySymbol = settings.currencySymbol
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
            item {
                Text(
                    text = dateHeader,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }

            items(games, key = { it.id }) { game ->
                val isSelected = game.id in selectedMatchIds
                SplitMatchItem(
                    game = game,
                    isSelected = isSelected,
                    onToggle = {
                        val nextSelection = if (isSelected) {
                            selectedMatchIds - game.id 
                        } else {
                            selectedMatchIds + game.id
                        }
                        onSelectMatches(nextSelection)
                    }
                )
            }
        }

        // Empty State
        if (displayGames.isEmpty()) {
            item {
                EmptyMatchesState()
            }
        }
        
        item { Spacer(Modifier.height(100.dp)) }
    }
}

/**
 * Helper to group games into human-readable date categories.
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
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
            val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
            
            TextButton(
                onClick = {
                    val todayMatchIds = displayGames.filter { 
                        val date = Date(it.endTime ?: it.startTime)
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) == today
                    }.map { it.id }
                    onSelectChange(currentSelection + todayMatchIds)
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
        modifier = Modifier.fillMaxWidth().padding(40.dp), 
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No finished matches found.", 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
