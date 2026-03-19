package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.*
import com.mwarrc.pocketscore.ui.feature.home.utils.RosterSorter

@Composable
fun RosterGridSection(
    savedPlayerNames: List<String>,
    currentNames: List<String>,
    onSelectName: (String) -> Unit,
    autoSortOption: RosterSortOption,
    onAutoSortOptionChange: (RosterSortOption) -> Unit,
    layout: RosterLayout,
    onLayoutChange: (RosterLayout) -> Unit,
    settings: AppSettings,
    history: GameHistory? = null,
    newlyAddedNames: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var shuffleSeed by remember { mutableIntStateOf(0) }

    val sortedNames = remember(savedPlayerNames, autoSortOption, history, shuffleSeed, settings.deactivatedPlayers) {
        val filtered = savedPlayerNames.filter { it !in settings.deactivatedPlayers }
        RosterSorter.sort(filtered, autoSortOption, history, shuffleSeed)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RosterSectionHeader(
            currentCount = currentNames.size,
            autoSortOption = autoSortOption,
            showSortMenu = showSortMenu,
            onSortChipClick = { showSortMenu = true },
            onSortMenuDismiss = { showSortMenu = false },
            onSortOptionSelect = { option ->
                if (option == RosterSortOption.RANDOM && autoSortOption == RosterSortOption.RANDOM) {
                    shuffleSeed = (1..Int.MAX_VALUE).random()
                } else {
                    onAutoSortOptionChange(option)
                }
                showSortMenu = false
            },
            currentLayout = layout,
            onLayoutToggle = {
                onLayoutChange(if (layout == RosterLayout.GRID) RosterLayout.LIST else RosterLayout.GRID)
            }
        )

        if (layout == RosterLayout.GRID) {
            RosterGridLayout(
                names = sortedNames,
                currentNames = currentNames,
                onSelectName = onSelectName,
                columns = 3, // Smaller size by increasing column count
                newlyAddedNames = newlyAddedNames,
            )
        } else {
            RosterListLayout(
                names = sortedNames,
                currentNames = currentNames,
                onSelectName = onSelectName,
                newlyAddedNames = newlyAddedNames,
            )
        }
    }
}

// --───────────────────────────────
// Section header — label + selection count + sort chip + layout toggle
// --───────────────────────────────

@Composable
private fun RosterSectionHeader(
    currentCount: Int,
    autoSortOption: RosterSortOption,
    showSortMenu: Boolean,
    onSortChipClick: () -> Unit,
    onSortMenuDismiss: () -> Unit,
    onSortOptionSelect: (RosterSortOption) -> Unit,
    currentLayout: RosterLayout,
    onLayoutToggle: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = "Roster Pool",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                ),
                color = colorScheme.primary,
            )
            Text(
                text = if (currentCount > 0) "$currentCount selected" else "Tap player to select",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                ),
                color = colorScheme.onSurface.copy(alpha = 0.45f),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Layout Toggle
            IconButton(
                onClick = onLayoutToggle,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = if (currentLayout == RosterLayout.GRID) Icons.AutoMirrored.Filled.List else Icons.Default.GridView,
                    contentDescription = "Toggle Layout",
                    modifier = Modifier.size(18.dp),
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Box {
                PremiumSortChip(
                    autoSortOption = autoSortOption,
                    onClick = onSortChipClick,
                )
                RosterSortMenu(
                    expanded = showSortMenu,
                    selectedOption = autoSortOption,
                    onDismiss = onSortMenuDismiss,
                    onOptionSelect = onSortOptionSelect,
                )
            }
        }
    }
}

// --───────────────────────────────
// Layouts
// --───────────────────────────────

@Composable
private fun RosterGridLayout(
    names: List<String>,
    currentNames: List<String>,
    onSelectName: (String) -> Unit,
    columns: Int,
    newlyAddedNames: Set<String> = emptySet(),
) {
    val rows = names.chunked(columns)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        val selectionIndex = currentNames.indexOfFirst {
                            it.trim().equals(item.trim(), ignoreCase = true)
                        }
                        val isSelected = selectionIndex != -1
                        PlayerTile(
                            name = item,
                            isSelected = isSelected,
                            selectionOrder = if (isSelected) selectionIndex + 1 else null,
                            isNew = item.trim().lowercase() in newlyAddedNames.map { it.trim().lowercase() },
                            onClick = { onSelectName(item) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                repeat(columns - rowItems.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RosterListLayout(
    names: List<String>,
    currentNames: List<String>,
    onSelectName: (String) -> Unit,
    newlyAddedNames: Set<String> = emptySet(),
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        names.forEach { item ->
            val selectionIndex = currentNames.indexOfFirst {
                it.trim().equals(item.trim(), ignoreCase = true)
            }
            val isSelected = selectionIndex != -1
            PlayerTile(
                name = item,
                isSelected = isSelected,
                selectionOrder = if (isSelected) selectionIndex + 1 else null,
                isNew = item.trim().lowercase() in newlyAddedNames.map { it.trim().lowercase() },
                onClick = { onSelectName(item) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --───────────────────────────────
// Sort chip — premium pill with drawn border
// --───────────────────────────────

@Composable
private fun PremiumSortChip(
    autoSortOption: RosterSortOption,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isActive = autoSortOption != RosterSortOption.MANUAL

    val borderAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.85f else 0.35f,
        animationSpec = tween(180),
        label = "sort_border",
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.18f else 0.08f,
        animationSpec = tween(180),
        label = "sort_bg",
    )

    val label = when (autoSortOption) {
        RosterSortOption.MANUAL -> "Sort"
        RosterSortOption.ALPHABETICAL -> "A–Z"
        RosterSortOption.LOSERS_FIRST -> "Losers first"
        RosterSortOption.WINNERS_FIRST -> "Winners first"
        RosterSortOption.RANDOM -> "Random"
        RosterSortOption.MOST_PLAYED -> "Frequent"
    }
    val icon = when (autoSortOption) {
        RosterSortOption.MANUAL -> Icons.AutoMirrored.Filled.Sort
        RosterSortOption.ALPHABETICAL -> Icons.Default.SortByAlpha
        RosterSortOption.LOSERS_FIRST -> Icons.Default.History
        RosterSortOption.WINNERS_FIRST -> Icons.Default.EmojiEvents
        RosterSortOption.RANDOM -> Icons.Default.Shuffle
        RosterSortOption.MOST_PLAYED -> Icons.Default.BarChart
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .height(30.dp)
            .drawBehind {
                drawRoundRect(
                    color = colorScheme.primary.copy(alpha = bgAlpha),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                )
                drawRoundRect(
                    color = colorScheme.primary.copy(alpha = borderAlpha),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = colorScheme.primary.copy(alpha = if (isActive) 1f else 0.65f),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.02.sp,
            ),
            color = colorScheme.primary.copy(alpha = if (isActive) 1f else 0.65f),
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = colorScheme.primary.copy(alpha = if (isActive) 1f else 0.65f),
        )
    }
}

// --───────────────────────────────
// Sort dropdown — refined surface
// --───────────────────────────────

@Composable
private fun RosterSortMenu(
    expanded: Boolean,
    selectedOption: RosterSortOption,
    onDismiss: () -> Unit,
    onOptionSelect: (RosterSortOption) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
    ) {
        SortMenuItem("Manual (Recent)", Icons.AutoMirrored.Filled.Sort, selectedOption == RosterSortOption.MANUAL) { onOptionSelect(RosterSortOption.MANUAL) }
        SortMenuItem("Alphabetical", Icons.Default.SortByAlpha, selectedOption == RosterSortOption.ALPHABETICAL) { onOptionSelect(RosterSortOption.ALPHABETICAL) }
        SortMenuItem("Losers First", Icons.Default.History, selectedOption == RosterSortOption.LOSERS_FIRST) { onOptionSelect(RosterSortOption.LOSERS_FIRST) }
        SortMenuItem("Winners First", Icons.Default.EmojiEvents, selectedOption == RosterSortOption.WINNERS_FIRST) { onOptionSelect(RosterSortOption.WINNERS_FIRST) }
        SortMenuItem("Random Shuffle", Icons.Default.Shuffle, selectedOption == RosterSortOption.RANDOM) { onOptionSelect(RosterSortOption.RANDOM) }
        SortMenuItem("Most Played", Icons.Default.BarChart, selectedOption == RosterSortOption.MOST_PLAYED) { onOptionSelect(RosterSortOption.MOST_PLAYED) }
    }
}

@Composable
private fun SortMenuItem(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.sp,
                ),
                color = if (selected) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.75f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.35f),
            )
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = colorScheme.primary.copy(alpha = 0.7f),
                )
            }
        },
        onClick = onClick,
    )
}