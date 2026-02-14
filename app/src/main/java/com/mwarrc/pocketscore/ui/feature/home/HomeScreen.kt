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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.home.components.*
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun HomeScreen(
    settings: AppSettings,
    history: com.mwarrc.pocketscore.domain.model.GameHistory,
    activePlayers: List<com.mwarrc.pocketscore.domain.model.Player> = emptyList(),
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
                        activePlayers = activePlayers,
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
                        TextButton(
                            onClick = onNavigateToUpcoming,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sync", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "•", 
                            modifier = Modifier.padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        TextButton(
                            onClick = onNavigateToRoadmap,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Groups, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Teams", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // Player Setup Section — styled title + record tip
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    /*Text(
                        "Match Setup",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )*/
                    Text(
                        "Assemble your roster for the game session.",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    if (settings.showIdentityTip) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(16.dp).padding(end = 24.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        Icons.Default.CloudSync, 
                                        null, 
                                        modifier = Modifier.size(20.dp).padding(top = 2.dp), 
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                    Column {
                                        Text(
                                            "Pro Tip",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Use your unique 'Pro Name' for consistent match history and accurate stats across all your devices!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onUpdateSettings { it.copy(showIdentityTip = false) } },
                                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss Tip",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Incognito Mode Banner
            item {
                AnimatedVisibility(
                    visible = settings.isIncognitoMode,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Surface(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Incognito Mode Active",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Active Roster Section
            if (settings.savedPlayerNames.isNotEmpty() && settings.showQuickSelectOnHome) {
                item {
                    ActiveRosterSection(
                        savedPlayerNames = settings.savedPlayerNames,
                        currentNames = playerNames,
                        onSelectName = { name ->
                            val normalizedName = name.trim()
                            val existingIndex = playerNames.indexOfFirst { it.trim().equals(normalizedName, ignoreCase = true) }
                            
                            val updatedNames = if (existingIndex != -1) {
                                playerNames.filterNot { it.trim().equals(normalizedName, ignoreCase = true) }
                            } else {
                                playerNames.toMutableList().apply {
                                    val firstEmpty = indexOfFirst { it.trim().isEmpty() }
                                    if (firstEmpty != -1) {
                                        this[firstEmpty] = name
                                    } else if (size < settings.maxPlayers) {
                                        add(name)
                                    }
                                }
                            }

                            // Simply update the list in selection order
                            playerNames = updatedNames
                        },
                        autoSortOption = settings.rosterSortOption,
                        onAutoSortOptionChange = { option ->
                            onUpdateSettings { it.copy(rosterSortOption = option) }
                        },
                        history = history,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
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
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Player Name Inputs
            itemsIndexed(
                items = playerNames,
                key = { index, _ -> index } 
            ) { index, name ->
                val trimmed = name.trim()
                val isDuplicate = trimmedNames.count { it.equals(trimmed, ignoreCase = true) && it.isNotEmpty() } > 1
                val isEmpty = trimmed.isEmpty()
                val isContinuing = settings.savedPlayerNames.any { it.equals(trimmed, ignoreCase = true) }
                val isNewPlayer = trimmed.isNotEmpty() && !isContinuing && !isDuplicate
                val hasError = (isDuplicate || isEmpty) && hasStartedEditing

                PlayerInputCard(
                    index = index,
                    name = name,
                    hasError = hasError,
                    isDuplicate = isDuplicate && !isEmpty,
                    isContinuing = isContinuing && !isEmpty,
                    isNewPlayer = isNewPlayer,
                    allowRemove = playerNames.size > 2,
                    shouldFocus = index == autoFocusIndex,
                    onNameChange = { newName ->
                        playerNames = playerNames.toMutableList().apply { this[index] = newName }
                    },
                    onRemove = {
                        playerNames = playerNames.toMutableList().apply { removeAt(index) }
                    },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Add Player Button — Premium Surface
            if (playerNames.size < settings.maxPlayers) {
                item {
                    Surface(
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
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Add, 
                                null, 
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Add Another Player", 
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Start Game Button
            item {
                StartGameFloatingBar(
                    isVisible = isValid,
                    onStartGame = {
                        // Check Incognito Settings for Roster
                        val shouldSavePlayers = if (settings.isIncognitoMode) settings.incognitoSavePlayers else true

                        if (shouldSavePlayers) {
                            val currentSaved = settings.savedPlayerNames.toMutableList()
                            trimmedNames.reversed().forEach { name ->
                                if (name.isNotEmpty()) {
                                    currentSaved.removeAll { it.equals(name, ignoreCase = true) }
                                    currentSaved.add(0, name)
                                }
                            }
                            val finalSaved = currentSaved.distinctBy { it.lowercase() }
                            onUpdateSettings { it.copy(savedPlayerNames = finalSaved) }
                        }
                        onStartGame(trimmedNames)
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }


        }
    }
}
