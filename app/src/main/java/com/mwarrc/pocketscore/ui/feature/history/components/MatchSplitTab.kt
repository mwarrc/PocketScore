package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.GameState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSplitTab(
    history: GameHistory,
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var selectedMatchIds by remember { mutableStateOf(setOf<String>()) }
    var showQuickSettings by remember { mutableStateOf(false) }

    val displayGames = remember(history) {
        history.pastGames.filter { it.isFinalized }
            .sortedByDescending { it.endTime ?: it.startTime }
    }

    val gamesToCalculate = remember(displayGames, selectedMatchIds) {
        displayGames.filter { it.id in selectedMatchIds }
    }

    // Use derived state for calculation
    val calculation = remember(gamesToCalculate, settings) {
        val playerDebts = mutableMapOf<String, Double>()
        var totalAmount = 0.0

        gamesToCalculate.forEach { game ->
            val playedCount = game.players.size
            if (playedCount == 0) return@forEach
            
            totalAmount += settings.matchCost
            
            val maxScore = game.players.maxOfOrNull { it.score } ?: 0
            val winners = game.players.filter { it.score == maxScore }
            
            val payers = if (settings.winnersPay) {
                game.players
            } else {
                game.players.filter { p -> p !in winners }
            }
            
            if ( payers.isNotEmpty() ) {
                val splitCost = settings.matchCost / payers.size
                payers.forEach { player ->
                    playerDebts[player.name] = (playerDebts[player.name] ?: 0.0) + splitCost
                }
            }
        }
        
        playerDebts.toList().sortedByDescending { it.second } to totalAmount
    }

    val (debts, totalSessionAmount) = calculation

    val groupedGames = remember(displayGames) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000))
        val headerFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

        displayGames.groupBy { game ->
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (totalSessionAmount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(28.dp),
                onClick = { showQuickSettings = !showQuickSettings }
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total to Settle", 
                                style = MaterialTheme.typography.labelMedium, 
                                color = if (totalSessionAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${settings.currencySymbol} ${String.format("%.0f", totalSessionAmount)}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black,
                                color = if (totalSessionAmount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            if (showQuickSettings) Icons.Default.ExpandLess else Icons.Default.Tune,
                            null,
                            tint = if (totalSessionAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.animation.AnimatedVisibility(visible = showQuickSettings) {
                        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if (settings.matchCost == 0.0) "" else settings.matchCost.toString(),
                                    onValueChange = { onUpdateSettings { s -> s.copy(matchCost = it.toDoubleOrNull() ?: 0.0) } },
                                    label = { Text("Cost per Match") },
                                    modifier = Modifier.weight(1f),
                                    prefix = { Text(settings.currencySymbol) },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = settings.currencySymbol,
                                    onValueChange = { onUpdateSettings { s -> s.copy(currencySymbol = it) } },
                                    label = { Text("Symbol") },
                                    modifier = Modifier.width(80.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (debts.isNotEmpty()) {
            item {
                Text(
                    "Owed per Player", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            items(debts) { (name, amount) ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    name.firstOrNull()?.toString()?.uppercase() ?: "?", 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                name, 
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold 
                            )
                            Text(
                                "Matches: ${gamesToCalculate.count { g -> g.players.any { it.name == name } }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${settings.currencySymbol} ${String.format("%.1f", amount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    "Split Method", 
                    style = MaterialTheme.typography.labelLarge, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !settings.winnersPay,
                        onClick = { onUpdateSettings { it.copy(winnersPay = false) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        label = { Text("Losers Pay", style = MaterialTheme.typography.labelSmall) },
                        icon = { Icon(Icons.Default.PersonRemove, null, modifier = Modifier.size(16.dp)) },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            activeContentColor = MaterialTheme.colorScheme.error,
                            activeBorderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    )
                    SegmentedButton(
                        selected = settings.winnersPay,
                        onClick = { onUpdateSettings { it.copy(winnersPay = true) } },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        label = { Text("All Split", style = MaterialTheme.typography.labelSmall) },
                        icon = { Icon(Icons.Default.Groups, null, modifier = Modifier.size(16.dp)) },
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color(0xFFE8F5E9).copy(alpha = 0.8f),
                            activeContentColor = Color(0xFF2E7D32),
                            activeBorderColor = Color(0xFF2E7D32).copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "Select Matches", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.padding(start = 4.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    TextButton(
                        onClick = {
                            val todayMatchIds = displayGames.filter { 
                                val date = Date(it.endTime ?: it.startTime)
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) == today
                            }.map { it.id }
                            selectedMatchIds = selectedMatchIds + todayMatchIds
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Select Today", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(
                        onClick = { selectedMatchIds = emptySet() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear All", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        
        groupedGames.forEach { (dateHeader, games) ->
            item {
                Text(
                    text = dateHeader,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }

            items(games) { game ->
                val isSelected = game.id in selectedMatchIds
                val maxScore = game.players.maxOfOrNull { it.score } ?: 0
                val winners = game.players.filter { it.score == maxScore }
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                val timeStr = timeFormat.format(Date(game.endTime ?: game.startTime))

                Surface(
                    onClick = {
                        selectedMatchIds = if (isSelected) selectedMatchIds - game.id else selectedMatchIds + game.id
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { 
                            Text(
                                winners.joinToString { it.name } + " won", 
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            ) 
                        },
                        supportingContent = { 
                            Column {
                                Text("$timeStr • ${game.players.size} players")
                                Text(
                                    game.players.joinToString { it.name },
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        },
                        trailingContent = {
                            Checkbox(checked = isSelected, onCheckedChange = {
                                selectedMatchIds = if (isSelected) selectedMatchIds - game.id else selectedMatchIds + game.id
                            })
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        if (displayGames.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("No finished matches found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        
        item { Spacer(Modifier.height(100.dp)) }
    }
}

// Extension to scale Switch
@Composable
fun Switch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, scale: Float) {
    Box(modifier = Modifier.padding(0.dp)) {
        androidx.compose.material3.Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
        )
    }
}

