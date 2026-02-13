package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.automirrored.filled.Sort
import com.mwarrc.pocketscore.domain.model.RosterSortOption
import androidx.compose.material3.*
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveRosterSection(
    savedPlayerNames: List<String>,
    currentNames: List<String>,
    onSelectName: (String) -> Unit,
    autoSortOption: RosterSortOption,
    onAutoSortOptionChange: (RosterSortOption) -> Unit,
    history: com.mwarrc.pocketscore.domain.model.GameHistory? = null,
    modifier: Modifier = Modifier
) {
    if (savedPlayerNames.isEmpty()) return

    var showSortMenu by remember { mutableStateOf(false) }
    var shuffleSeed by remember { mutableIntStateOf(0) }

    // Roster Pool Sorting
    val sortedNames = remember(savedPlayerNames, autoSortOption, history, shuffleSeed) {
        val lastGame = history?.pastGames?.firstOrNull()
        
        when (autoSortOption) {
            RosterSortOption.MANUAL -> savedPlayerNames
            RosterSortOption.ALPHABETICAL -> savedPlayerNames.sortedWith(String.CASE_INSENSITIVE_ORDER)
            RosterSortOption.LOSERS_FIRST -> {
                if (lastGame == null) savedPlayerNames.sortedWith(String.CASE_INSENSITIVE_ORDER)
                else {
                    savedPlayerNames.sortedWith { name1, name2 ->
                        val p1 = lastGame.players.find { it.name.trim().equals(name1.trim(), ignoreCase = true) }
                        val p2 = lastGame.players.find { it.name.trim().equals(name2.trim(), ignoreCase = true) }
                        when {
                            p1 != null && p2 != null -> p1.score.compareTo(p2.score)
                            p1 != null -> -1
                            p2 != null -> 1
                            else -> name1.compareTo(name2, ignoreCase = true)
                        }
                    }
                }
            }
            RosterSortOption.WINNERS_FIRST -> {
                if (lastGame == null) savedPlayerNames.sortedWith(String.CASE_INSENSITIVE_ORDER)
                else {
                    savedPlayerNames.sortedWith { name1, name2 ->
                        val p1 = lastGame.players.find { it.name.trim().equals(name1.trim(), ignoreCase = true) }
                        val p2 = lastGame.players.find { it.name.trim().equals(name2.trim(), ignoreCase = true) }
                        when {
                            p1 != null && p2 != null -> p2.score.compareTo(p1.score)
                            p1 != null -> -1
                            p2 != null -> 1
                            else -> name1.compareTo(name2, ignoreCase = true)
                        }
                    }
                }
            }
            RosterSortOption.RANDOM -> {
                if (shuffleSeed == 0) savedPlayerNames.shuffled()
                else savedPlayerNames.shuffled(java.util.Random(shuffleSeed.toLong()))
            }
            RosterSortOption.MOST_PLAYED -> {
                val frequencies = history?.pastGames?.flatMap { it.players }
                    ?.groupBy { it.name.trim().lowercase() }
                    ?.mapValues { it.value.size } ?: emptyMap()
                
                savedPlayerNames.sortedWith { name1, name2 ->
                    val f1 = frequencies[name1.trim().lowercase()] ?: 0
                    val f2 = frequencies[name2.trim().lowercase()] ?: 0
                    if (f1 != f2) f2.compareTo(f1) // Descending
                    else name1.compareTo(name2, ignoreCase = true)
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                "Active Roster",
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
                    if (currentCount > 0) "$currentCount players selected • Tap to toggle" else "Select players to start",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Box {
                    FilterChip(
                        selected = autoSortOption != RosterSortOption.MANUAL,
                        onClick = { showSortMenu = true },
                        label = { 
                            Text(
                                when (autoSortOption) {
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
                                when (autoSortOption) {
                                    RosterSortOption.MANUAL -> Icons.AutoMirrored.Filled.Sort
                                    RosterSortOption.ALPHABETICAL -> Icons.Default.SortByAlpha
                                    RosterSortOption.LOSERS_FIRST -> Icons.Default.History
                                    RosterSortOption.WINNERS_FIRST -> Icons.Default.EmojiEvents
                                    RosterSortOption.RANDOM -> Icons.Default.Shuffle
                                    RosterSortOption.MOST_PLAYED -> Icons.Default.BarChart
                                },
                                null,
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

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        SortMenuItem(
                            text = "Manual (Recent)",
                            icon = Icons.AutoMirrored.Filled.Sort,
                            selected = autoSortOption == RosterSortOption.MANUAL,
                            onClick = {
                                onAutoSortOptionChange(RosterSortOption.MANUAL)
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Alphabetical",
                            icon = Icons.Default.SortByAlpha,
                            selected = autoSortOption == RosterSortOption.ALPHABETICAL,
                            onClick = {
                                onAutoSortOptionChange(RosterSortOption.ALPHABETICAL)
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Losers First",
                            icon = Icons.Default.History,
                            selected = autoSortOption == RosterSortOption.LOSERS_FIRST,
                            onClick = {
                                onAutoSortOptionChange(RosterSortOption.LOSERS_FIRST)
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Winners First",
                            icon = Icons.Default.EmojiEvents,
                            selected = autoSortOption == RosterSortOption.WINNERS_FIRST,
                            onClick = {
                                onAutoSortOptionChange(RosterSortOption.WINNERS_FIRST)
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Random Shuffle",
                            icon = Icons.Default.Shuffle,
                            selected = autoSortOption == RosterSortOption.RANDOM,
                            onClick = {
                                if (autoSortOption == RosterSortOption.RANDOM) {
                                    shuffleSeed = (1..Int.MAX_VALUE).random()
                                } else {
                                    onAutoSortOptionChange(RosterSortOption.RANDOM)
                                }
                                showSortMenu = false
                            }
                        )
                        SortMenuItem(
                            text = "Most Played",
                            icon = Icons.Default.BarChart,
                            selected = autoSortOption == RosterSortOption.MOST_PLAYED,
                            onClick = {
                                onAutoSortOptionChange(RosterSortOption.MOST_PLAYED)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Roster Pool
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
fun SortMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { 
            Text(
                text, 
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            ) 
        },
        leadingIcon = { 
            Icon(
                icon, 
                null, 
                modifier = Modifier.size(20.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            ) 
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    null,
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

@Composable
fun RosterChip(
    name: String,
    isSelected: Boolean,
    selectionOrder: Int? = null,
    onClick: () -> Unit
) {
    val playerColor = getMaterialPlayerColor(name)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()


    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isSelected) {
                if (selectionOrder != null) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.onPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectionOrder.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                // Minimal circle instead of player color
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ) { Box {} }
            }
            
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
