package com.mwarrc.pocketscore.ui.feature.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewScreen(
    shareData: PocketScoreShare,
    existingPlayers: List<String> = emptyList(), // Added parameter for auto-detection
    onConfirm: (Map<String, String>) -> Unit, // Updated callback to pass mappings
    onCancel: () -> Unit
) {
    // State for player name mappings: ImportName -> LocalName
    var playerMappings by remember { mutableStateOf(mapOf<String, String>()) }
    var autoMatchedNames by remember { mutableStateOf(setOf<String>()) }
    var expandMappingSection by remember { mutableStateOf(false) }

    // Auto-detect mappings on first load
    LaunchedEffect(shareData, existingPlayers) {
        if (existingPlayers.isNotEmpty()) {
            val newMappings = mutableMapOf<String, String>()
            val matched = mutableSetOf<String>()
            
            shareData.friends.forEach { importedName ->
                // Exact match (case-insensitive) - usually handled by backend but good to be explicit
                val exactMatch = existingPlayers.find { it.equals(importedName, ignoreCase = true) }
                if (exactMatch != null) {
                    newMappings[importedName] = exactMatch
                    matched.add(importedName)
                } else {
                    // Fuzzy match
                    val bestMatch = com.mwarrc.pocketscore.util.StringSimilarity.findBestMatch(importedName, existingPlayers)
                    if (bestMatch != null) {
                        newMappings[importedName] = bestMatch.first
                        matched.add(importedName)
                    }
                }
            }
            
            if (newMappings.isNotEmpty()) {
                playerMappings = newMappings
                autoMatchedNames = matched
                expandMappingSection = true // Auto-expand if we found potential matches
            }
        }
    }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        Spacer(Modifier.statusBarsPadding())
        Scaffold(
            modifier = Modifier.displayCutoutPadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Import Data", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Default.Close, "Cancel")
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(12.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("Cancel") }
                        
                        Button(
                            onClick = { onConfirm(playerMappings) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(12.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) { 
                            Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Import & Merge") 
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Info & Stats
                item {
                    com.mwarrc.pocketscore.ui.feature.history.components.ImportSummaryCard(shareData)
                }

                // Player Mapping Section
                if (shareData.friends.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandMappingSection = !expandMappingSection }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Configure Player Mapping",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            if (playerMappings.isNotEmpty()) "${playerMappings.size} players mapped" else "Map imported names to local players",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        if (expandMappingSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        null
                                    )
                                }
                                
                                androidx.compose.animation.AnimatedVisibility(visible = expandMappingSection) {
                                    Column(
                                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val unavailablePlayers = playerMappings.values.toSet()
                                        
                                        shareData.friends.forEach { importedName ->
                                            com.mwarrc.pocketscore.ui.feature.history.components.ImportPlayerMappingCard(
                                                importedName = importedName,
                                                existingPlayers = existingPlayers,
                                                currentMapping = playerMappings[importedName],
                                                unavailablePlayers = unavailablePlayers,
                                                isAutoMatched = importedName in autoMatchedNames,
                                                onMappingChanged = { newMapping ->
                                                    playerMappings = if (newMapping != null) {
                                                        playerMappings + (importedName to newMapping)
                                                    } else {
                                                        playerMappings - importedName
                                                    }
                                                    // Remove from auto-matched if manually changed
                                                    if (importedName in autoMatchedNames) {
                                                        autoMatchedNames = autoMatchedNames - importedName
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Match Records Preview
                if (shareData.games.isNotEmpty()) {
                    item {
                        Text(
                            "Match Preview (with mappings)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(shareData.games) { game ->
                         com.mwarrc.pocketscore.ui.feature.history.components.ImportMatchPreviewCard(
                             game = game,
                             mappings = playerMappings
                         )
                    }
                }

                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoFixHigh, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Smart Merge active: We'll skip any games you already have.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}


