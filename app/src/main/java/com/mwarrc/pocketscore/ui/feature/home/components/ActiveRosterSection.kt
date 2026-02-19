package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.RosterSortOption
import com.mwarrc.pocketscore.ui.feature.home.utils.RosterSorter

/**
 * A sophisticated roster management section that allows quick player selection with smart sorting.
 * 
 * Features:
 * - **Smart Sorting**: Group players by frequency, recent performance (winners/losers), or alphabetical order.
 * - **Selection Flow**: Tracks the order of selection to assist with match setup.
 * - **Dynamic Pool**: Automatically adjusts based on available match history.
 * - **Compact Layout**: Uses FlowRow to efficiently display many players in a digestible way.
 * 
 * @param savedPlayerNames The master list of all players in the user's roster.
 * @param currentNames The names of players currently selected for the next match.
 * @param onSelectName Callback to toggle selection status for a player.
 * @param autoSortOption The current sorting strategy for the roster pool.
 * @param onAutoSortOptionChange Callback to update the sorting strategy.
 * @param history Game records used for statistical sorting.
 * @param modifier Modifier for the section container.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveRosterSection(
    savedPlayerNames: List<String>,
    currentNames: List<String>,
    onSelectName: (String) -> Unit,
    autoSortOption: RosterSortOption,
    onAutoSortOptionChange: (RosterSortOption) -> Unit,
    settings: AppSettings,
    history: GameHistory? = null,
    modifier: Modifier = Modifier
) {
    if (savedPlayerNames.isEmpty()) return

    var showSortMenu by remember { mutableStateOf(false) }
    var shuffleSeed by remember { mutableIntStateOf(0) }

    // Roster Pool Sorting
    val sortedNames = remember(savedPlayerNames, autoSortOption, history, shuffleSeed, settings.deactivatedPlayers) {
        val filtered = savedPlayerNames.filter { name ->
            name !in settings.deactivatedPlayers
        }
        RosterSorter.sort(filtered, autoSortOption, history, shuffleSeed)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = "Active Roster",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentCount = currentNames.count { it.trim().isNotEmpty() }
                Text(
                    text = if (currentCount > 0) "$currentCount players selected • Tap to toggle" else "Select players to start",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                // Sort Menu Trigger
                Box {
                    SortChip(
                        autoSortOption = autoSortOption,
                        onClick = { showSortMenu = true }
                    )

                    RosterSortMenu(
                        expanded = showSortMenu,
                        selectedOption = autoSortOption,
                        onDismiss = { showSortMenu = false },
                        onOptionSelect = { option ->
                            if (option == RosterSortOption.RANDOM && autoSortOption == RosterSortOption.RANDOM) {
                                shuffleSeed = (1..Int.MAX_VALUE).random()
                            } else {
                                onAutoSortOptionChange(option)
                            }
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        // Roster Pool Visualization
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedNames.forEach { name ->
                val selectionIndex = currentNames.indexOfFirst { it.trim().equals(name.trim(), ignoreCase = true) }
                val isSelected = selectionIndex != -1

                RosterChip(
                    name = name,
                    isSelected = isSelected,
                    selectionOrder = if (isSelected) selectionIndex + 1 else null,
                    onClick = { onSelectName(name) }
                )
            }
        }
    }
}

@Composable
private fun SortChip(
    autoSortOption: RosterSortOption,
    onClick: () -> Unit
) {
    FilterChip(
        selected = autoSortOption != RosterSortOption.MANUAL,
        onClick = onClick,
        label = { 
            Text(
                text = when (autoSortOption) {
                    RosterSortOption.MANUAL -> "Sort"
                    RosterSortOption.ALPHABETICAL -> "Alphabetical"
                    RosterSortOption.LOSERS_FIRST -> "Losers First"
                    RosterSortOption.WINNERS_FIRST -> "Winners First"
                    RosterSortOption.RANDOM -> "Random"
                    RosterSortOption.MOST_PLAYED -> "Frequent"
                }, 
                style = MaterialTheme.typography.labelSmall
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = when (autoSortOption) {
                    RosterSortOption.MANUAL -> Icons.AutoMirrored.Filled.Sort
                    RosterSortOption.ALPHABETICAL -> Icons.Default.SortByAlpha
                    RosterSortOption.LOSERS_FIRST -> Icons.Default.History
                    RosterSortOption.WINNERS_FIRST -> Icons.Default.EmojiEvents
                    RosterSortOption.RANDOM -> Icons.Default.Shuffle
                    RosterSortOption.MOST_PLAYED -> Icons.Default.BarChart
                },
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        trailingIcon = {
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(14.dp))
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.height(32.dp)
    )
}

@Composable
private fun RosterSortMenu(
    expanded: Boolean,
    selectedOption: RosterSortOption,
    onDismiss: () -> Unit,
    onOptionSelect: (RosterSortOption) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        SortMenuItem(
            text = "Manual (Recent)",
            icon = Icons.AutoMirrored.Filled.Sort,
            selected = selectedOption == RosterSortOption.MANUAL,
            onClick = { onOptionSelect(RosterSortOption.MANUAL) }
        )
        SortMenuItem(
            text = "Alphabetical",
            icon = Icons.Default.SortByAlpha,
            selected = selectedOption == RosterSortOption.ALPHABETICAL,
            onClick = { onOptionSelect(RosterSortOption.ALPHABETICAL) }
        )
        SortMenuItem(
            text = "Losers First",
            icon = Icons.Default.History,
            selected = selectedOption == RosterSortOption.LOSERS_FIRST,
            onClick = { onOptionSelect(RosterSortOption.LOSERS_FIRST) }
        )
        SortMenuItem(
            text = "Winners First",
            icon = Icons.Default.EmojiEvents,
            selected = selectedOption == RosterSortOption.WINNERS_FIRST,
            onClick = { onOptionSelect(RosterSortOption.WINNERS_FIRST) }
        )
        SortMenuItem(
            text = "Random Shuffle",
            icon = Icons.Default.Shuffle,
            selected = selectedOption == RosterSortOption.RANDOM,
            onClick = { onOptionSelect(RosterSortOption.RANDOM) }
        )
        SortMenuItem(
            text = "Most Played",
            icon = Icons.Default.BarChart,
            selected = selectedOption == RosterSortOption.MOST_PLAYED,
            onClick = { onOptionSelect(RosterSortOption.MOST_PLAYED) }
        )
    }
}

@Composable
private fun SortMenuItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { 
            Text(
                text = text, 
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            ) 
        },
        leadingIcon = { 
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            ) 
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    )
}
