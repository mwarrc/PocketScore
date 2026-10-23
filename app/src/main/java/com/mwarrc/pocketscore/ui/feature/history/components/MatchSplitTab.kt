package com.mwarrc.pocketscore.ui.feature.history.components

import android.content.Intent
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tab component that handles match cost settlement between players,
 * redesigned to match the larger M3 Friends/Matches/Leaderboard tab design.
 *
 * Features:
 * - Meta stats summary header (# matches, # selected, # payers, total amount)
 * - Interactive settings card with expandable rules, cost editor, and currency controls
 * - Settlement breakdown receipt cards with quick Share / Copy actions
 * - Bulk match selection chips ("Select All", "Today Only", "Clear")
 * - Date-grouped match cards with individual player exemption controls
 *
 * @param history Complete game history
 * @param settings App settings containing cost and settlement preferences
 * @param selectedMatchIds Set of game IDs currently selected for settlement
 * @param onSelectMatches Callback when selection changes
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
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
        val calc = SplitCalculator.calculate(selectedGames, settings)
        if (selectedGames.isNotEmpty()) {
            try {
                val b = android.os.Bundle()
                b.putInt("player_count", selectedGames.sumOf { it.players.size })
                b.putInt("excluded_player_count", selectedGames.sumOf { g -> settings.matchExcludedPlayers[g.id]?.size ?: 0 })
                b.putString("settlement_method", settings.settlementMethod.name)
                b.putDouble("match_cost", settings.matchCost)
                b.putString("currency", settings.currencySymbol)
                com.mwarrc.pocketscore.util.analytics.PocketScoreAnalytics.get().logSettlementViewed(b)
            } catch (_: Exception) {
            }
        }
        calc
    }

    // Grouping for the match list
    val groupedGames = remember(displayGames) {
        groupGamesByDate(displayGames)
    }

    val formatString = if (settings.settlementRoundingDecimals == 0) "%.0f" else "%.${settings.settlementRoundingDecimals}f"
    val formattedTotal = String.format(formatString, calculation.totalAmount)

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Top Summary Header Bar (Matching Friends/Matches tab header style) ──
        Text(
            text = "${displayGames.size} matches • ${selectedMatchIds.size} selected • ${calculation.playerDebts.size} payers • ${settings.currencySymbol} $formattedTotal total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Settings & Session Cost Card
            item(key = "settle_settings") {
                SplitSettingsCard(
                    totalAmount = calculation.totalAmount,
                    settings = settings,
                    onUpdateSettings = onUpdateSettings
                )
            }

            // 2. Settlement Breakdown Section (Debts)
            if (calculation.playerDebts.isNotEmpty()) {
                item(key = "settle_breakdown_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Settlement Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${calculation.playerDebts.size} players owe table fees",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Share / Copy Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val shareText = remember(calculation, settings, selectedMatchIds.size) {
                                buildShareSummaryText(calculation, settings, selectedMatchIds.size)
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(shareText))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy breakdown to clipboard",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Settlement")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share settlement",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                items(calculation.playerDebts, key = { "debt_${it.first}" }) { (name, amount) ->
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

            // 3. Selection Controls & Filter Chips
            if (displayGames.isNotEmpty()) {
                item(key = "settle_selection_controls") {
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
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest
                        ) {
                            Text(
                                text = "${games.size} ${if (games.size == 1) "game" else "games"}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                items(games, key = { "game_${it.id}" }) { game ->
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

            // 5. Empty State / Load older matches
            if (displayGames.isEmpty()) {
                item(key = "settle_empty") {
                    EmptyMatchesState()
                }
            } else if (hasOlderGames && !showAllHistory) {
                item(key = "settle_show_older") {
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
}

/**
 * Builds formatted settlement summary text for sharing or copying.
 */
private fun buildShareSummaryText(
    calculation: SplitCalculationResult,
    settings: AppSettings,
    selectedMatchCount: Int
): String {
    val formatString = if (settings.settlementRoundingDecimals == 0) "%.0f" else "%.${settings.settlementRoundingDecimals}f"
    val totalStr = String.format(formatString, calculation.totalAmount)
    val sb = StringBuilder()
    sb.append("🎱 PocketScore Table Settlement\n")
    sb.append("Total: ${settings.currencySymbol} $totalStr ($selectedMatchCount ${if (selectedMatchCount == 1) "match" else "matches"})\n\n")
    calculation.playerDebts.forEach { (name, amount) ->
        val amountStr = String.format(formatString, amount)
        sb.append("• $name: ${settings.currencySymbol} $amountStr\n")
    }
    return sb.toString()
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
            .padding(top = 12.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Matches to Settle",
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Quick selection chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val today = remember {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            }

            FilterChip(
                selected = currentSelection.size == displayGames.size && displayGames.isNotEmpty(),
                onClick = {
                    onSelectChange(displayGames.map { it.id }.toSet())
                },
                label = { Text("Select All") },
                leadingIcon = {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            )

            FilterChip(
                selected = false,
                onClick = {
                    val todayIds = displayGames.filter {
                        val date = Date(it.endTime ?: it.startTime)
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) == today
                    }.map { it.id }.toSet()
                    onSelectChange(todayIds)
                },
                label = { Text("Today Only") }
            )

            if (currentSelection.isNotEmpty()) {
                FilterChip(
                    selected = false,
                    onClick = { onSelectChange(emptySet()) },
                    label = { Text("Clear") },
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = MaterialTheme.colorScheme.error
                    )
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
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary
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
