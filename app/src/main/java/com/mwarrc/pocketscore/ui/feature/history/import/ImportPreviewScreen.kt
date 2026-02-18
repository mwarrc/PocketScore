package com.mwarrc.pocketscore.ui.feature.history.import_

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.PocketScoreShare
import com.mwarrc.pocketscore.ui.feature.history.import_.components.*
import com.mwarrc.pocketscore.util.StringSimilarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Import Preview Screen — the entry point for the import flow.
 *
 * Orchestrates all import sub-components:
 * - [ImportSourceHeader]          — source device + date + stat chips
 * - [ImportBreakdownCard]         — new/duplicate/player count grid
 * - [ImportPlayerMappingSection]  — collapsible player identity mapper
 * - [ImportMatchPreviewSection]   — collapsible match record previews
 * - [ImportSmartMergeCard]        — smart merge footer info
 * - [ImportBottomBar]             — cancel / confirm action bar
 *
 * All heavy computation (fuzzy name matching) runs on [Dispatchers.Default]
 * to keep the UI thread responsive.
 *
 * @param shareData      The structured data package being imported.
 * @param existingPlayers Local player names for mapping suggestions.
 * @param existingGames  Local game IDs used for duplicate detection.
 * @param onConfirm      Called with the final player name mappings on confirm.
 * @param onCancel       Called when the user cancels the import.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPreviewScreen(
    shareData: PocketScoreShare,
    existingPlayers: List<String> = emptyList(),
    existingGames: List<String> = emptyList(),
    onConfirm: (Map<String, String>) -> Unit,
    onCancel: () -> Unit
) {
    // ── State ──
    var playerMappings by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var autoMatchedNames by remember { mutableStateOf<Set<String>>(emptySet()) }
    var expandMappingSection by remember { mutableStateOf(true) }
    var expandMatchSection by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // ── Derived counts ──
    val duplicateGameIds = remember(shareData, existingGames) {
        shareData.games.map { it.id }.filter { it in existingGames }.toSet()
    }
    val newGamesCount = shareData.games.size - duplicateGameIds.size
    val newPlayersCount = shareData.friends.count { name ->
        existingPlayers.none { it.equals(name, ignoreCase = true) }
    }

    // ── System back = cancel ──
    BackHandler(onBack = onCancel)

    // ── Auto-detect player mappings on background thread ──
    LaunchedEffect(shareData, existingPlayers) {
        if (existingPlayers.isEmpty()) return@LaunchedEffect
        val (newMappings, matched) = withContext(Dispatchers.Default) {
            val mappings = mutableMapOf<String, String>()
            val matchedSet = mutableSetOf<String>()
            shareData.friends.forEach { importedName ->
                findBestMapping(importedName, existingPlayers)?.let { best ->
                    mappings[importedName] = best
                    matchedSet.add(importedName)
                }
            }
            mappings to matchedSet
        }
        if (newMappings.isNotEmpty()) {
            playerMappings = newMappings
            autoMatchedNames = matched
        }
    }

    // ── UI ──
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        Spacer(Modifier.statusBarsPadding())

        Scaffold(
            modifier = Modifier.displayCutoutPadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Import Data",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Review before merging",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancel import"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                ImportBottomBar(
                    onCancel = onCancel,
                    onConfirm = {
                        isProcessing = true
                        onConfirm(playerMappings)
                    },
                    isProcessing = isProcessing,
                    newGamesCount = newGamesCount,
                    duplicateCount = duplicateGameIds.size
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Source device header
                item { ImportSourceHeader(shareData = shareData) }

                // New / duplicate / player breakdown
                item {
                    ImportBreakdownCard(
                        newGamesCount = newGamesCount,
                        duplicateCount = duplicateGameIds.size,
                        newPlayersCount = newPlayersCount,
                        mergingPlayersCount = shareData.friends.size - newPlayersCount
                    )
                }

                // Player identity mapping
                if (shareData.friends.isNotEmpty()) {
                    item {
                        ImportPlayerMappingSection(
                            friends = shareData.friends,
                            existingPlayers = existingPlayers,
                            playerMappings = playerMappings,
                            autoMatchedNames = autoMatchedNames,
                            expanded = expandMappingSection,
                            onExpandToggle = { expandMappingSection = !expandMappingSection },
                            onMappingChanged = { importedName, newMapping ->
                                playerMappings = if (newMapping != null) {
                                    playerMappings + (importedName to newMapping)
                                } else {
                                    playerMappings - importedName
                                }
                                autoMatchedNames = autoMatchedNames - importedName
                            }
                        )
                    }
                }

                // Match records preview
                if (shareData.games.isNotEmpty()) {
                    item {
                        ImportMatchPreviewSection(
                            games = shareData.games,
                            mappings = playerMappings,
                            duplicateIds = duplicateGameIds,
                            expanded = expandMatchSection,
                            onExpandToggle = { expandMatchSection = !expandMatchSection }
                        )
                    }
                }

                // Smart merge footer
                item { ImportSmartMergeCard(duplicateCount = duplicateGameIds.size) }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Finds the best local player name for an imported name.
 * Exact case-insensitive match takes priority over fuzzy matching.
 */
private fun findBestMapping(importedName: String, existingPlayers: List<String>): String? {
    existingPlayers.find { it.equals(importedName, ignoreCase = true) }?.let { return it }
    return StringSimilarity.findBestMatch(importedName, existingPlayers)?.first
}
