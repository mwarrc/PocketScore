package com.mwarrc.pocketscore.ui.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.home.components.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    settings: AppSettings,
    hasActiveGame: Boolean = false,
    onStartGame: (List<String>) -> Unit,
    onResumeGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    onNavigateToUpcoming: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var playerNames by remember { mutableStateOf(listOf("", "")) }
    var hasStartedEditing by remember { mutableStateOf(false) }
    var autoFocusIndex by remember { mutableStateOf(-1) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(playerNames) {
        if (playerNames.any { it.isNotEmpty() }) {
            hasStartedEditing = true
        }
    }

    val trimmedNames = playerNames.map { it.trim() }
    val hasEmptyNames = trimmedNames.any { it.isEmpty() }
    // Case-insensitive duplicate check
    val hasDuplicateNames = trimmedNames.size != trimmedNames.map { it.lowercase() }.toSet().size
    val isValid = !hasEmptyNames && !hasDuplicateNames && playerNames.size >= 2

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentPadding = PaddingValues(
                top = 56.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Header
            item {
                HomeHeader(
                    onNavigateToAbout = onNavigateToAbout,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Quick Actions Card
            if (hasActiveGame) {
                item {
                    QuickActionsCard(
                        onResumeGame = onResumeGame,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToSettings = onNavigateToSettings,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                item {
                    NavigationCard(
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToSettings = onNavigateToSettings,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }            // Minimal Header Promo
            if (settings.showComingSoonFeatures) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onNavigateToUpcoming) {
                            Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Sync", style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            "•", 
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        TextButton(onClick = onNavigateToRoadmap) {
                            Icon(Icons.Default.Groups, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Teams", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            // Player Setup Section — styled title + record tip
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Setup players",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Add at least 2 players to start.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CloudSync, 
                                    null, 
                                    modifier = Modifier.size(16.dp), 
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Identity Tip",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "To keep your stats accurate across all devices and when sharing with friends, always use your unique 'Pro Name'. Consistency is key to a perfect match history!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified // Let it flow naturally
                            )
                        }
                    }
                }
            }

            // Saved Names Quick Select
            if (settings.savedPlayerNames.isNotEmpty() && settings.showQuickSelectOnHome) {
                item {
                    QuickSelectRow(
                        savedPlayerNames = settings.savedPlayerNames,
                        currentNames = playerNames,
                        onSelectName = { name ->
                            playerNames = if (playerNames.contains(name)) {
                                playerNames.filter { it != name }
                            } else {
                                playerNames.toMutableList().apply {
                                    val firstEmpty = indexOfFirst { it.isEmpty() }
                                    if (firstEmpty != -1) {
                                        this[firstEmpty] = name
                                    } else if (size < settings.maxPlayers) {
                                        add(name)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // Validation Banner
            if (!isValid && hasStartedEditing) {
                item {
                    val message = when {
                        hasEmptyNames -> "All player names must be filled"
                        hasDuplicateNames -> "Player names must be unique"
                        else -> "Add at least 2 players"
                    }
                    PlayerErrorBanner(
                        errorMessage = message,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // Player Name Inputs
            itemsIndexed(
                items = playerNames,
                key = { index, _ -> index } // Using index as key might cause issues if we remove items from middle, but for now unrelated to scroll.
                // Ideally, we should have stable IDs, but index is standard for simple string lists.
            ) { index, name ->
                val trimmed = name.trim()
                val isDuplicate = trimmedNames.count { it == trimmed && it.isNotEmpty() } > 1
                val isEmpty = trimmed.isEmpty()
                val isContinuing = settings.savedPlayerNames.any { it.equals(trimmed, ignoreCase = true) }
                val hasError = (isDuplicate || isEmpty) && hasStartedEditing

                PlayerInputCard(
                    index = index,
                    name = name,
                    hasError = hasError,
                    isDuplicate = isDuplicate && !isEmpty,
                    isContinuing = isContinuing && !isEmpty,
                    allowRemove = playerNames.size > 2,
                    shouldFocus = index == autoFocusIndex,
                    onNameChange = { newName ->
                        playerNames = playerNames.toMutableList().apply { this[index] = newName }
                    },
                    onRemove = {
                        playerNames = playerNames.toMutableList().apply { removeAt(index) }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Add Player Button — M3 tonal button
            if (playerNames.size < settings.maxPlayers) {
                item {
                    FilledTonalButton(
                        onClick = { 
                            if (playerNames.size < settings.maxPlayers) {
                                autoFocusIndex = playerNames.size
                                playerNames = playerNames + ""
                                coroutineScope.launch {
                                    listState.animateScrollToItem(playerNames.size + 10)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Player", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }

            // Start Game Button
            item {
                StartGameFloatingBar(
                    isVisible = isValid,
                    onStartGame = {
                        val currentSaved = settings.savedPlayerNames.toMutableList()
                        trimmedNames.reversed().forEach { name ->
                            if (name.isNotEmpty()) {
                                currentSaved.removeAll { it.equals(name, ignoreCase = true) }
                                currentSaved.add(0, name)
                            }
                        }
                        val finalSaved = currentSaved.distinctBy { it.lowercase() }
                        onUpdateSettings { it.copy(savedPlayerNames = finalSaved) }
                        onStartGame(trimmedNames)
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }


        }
    }
}
