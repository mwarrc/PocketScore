package com.mwarrc.pocketscore.ui.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.ui.feature.home.components.*
import kotlinx.coroutines.launch

/**
 * The primary entry point of the application.
 * 
 * Features:
 * - **Dynamic Match Setup**: Add and remove players with real-time validation.
 * - **Quick Roster Access**: Select from recently played or popular players.
 * - **Session Management**: Visual indicators for active games and guest session status.
 * - **Smart Navigation**: Fast access to history, settings, and analytical features.
 * - **Identity Awareness**: Encourages consistent 'Pro Name' usage for accurate lifetime stats.
 * 
 * @param settings Application configuration (themes, guest mode, roster limits).
 * @param history Recent game history used for roster smart-sorting.
 * @param activePlayers Players in the currently running match (if any).
 * @param hasActiveGame Flag indicating if a match is currently in progress.
 * @param onStartGame Callback to initialize a new match with the selected roster.
 * @param onResumeGame Callback to return to the active match.
 * @param onNavigateToHistory Navigate to the match history tab.
 * @param onNavigateToSettings Navigate to global app settings.
 * @param onNavigateToAbout Open the "About PocketScore" screen.
 * @param onNavigateToRoadmap View the feature development roadmap.
 * @param onNavigateToUpcoming Explore coming-soon synchronization features.
 * @param onUpdateSettings Functional update for app configuration.
 * @param modifier Modifier for the screen container.
 */
@Composable
fun HomeScreen(
    settings: AppSettings,
    history: GameHistory,
    activePlayers: List<Player> = emptyList(),
    hasActiveGame: Boolean = false,
    storagePermissionGranted: Boolean = true,
    onStartGame: (List<String>) -> Unit,
    onResumeGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    // Roster State
    var playerNames by remember { mutableStateOf(listOf("", "")) }
    var hasStartedEditing by remember { mutableStateOf(false) }
    var autoFocusIndex by remember { mutableStateOf(-1) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // track interaction to enable validation feedback
    LaunchedEffect(playerNames) {
        if (playerNames.any { it.isNotEmpty() }) {
            hasStartedEditing = true
        }
    }

    // Validation Logic
    val trimmedNames = playerNames.map { it.trim() }
    val hasEmptyNames = trimmedNames.any { it.isEmpty() }
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
            contentPadding = PaddingValues(top = 56.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Section (Branding & Quick Navigation)
            item {
                HomeHeader(
                    onNavigateToAbout = onNavigateToAbout,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Quick Actions / Resume Section
            item {
                if (hasActiveGame) {
                    QuickActionsCard(
                        activePlayers = activePlayers,
                        onResumeGame = onResumeGame,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToSettings = onNavigateToSettings,
                        showSettingsBadge = !storagePermissionGranted,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                } else {
                    NavigationCard(
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToSettings = onNavigateToSettings,
                        showSettingsBadge = !storagePermissionGranted,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // High-Level Session Feedback
            item {
                HomeSessionBanner(
                    settings = settings,
                    onSettingsClick = onNavigateToSettings,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Feature Highlights & Pro Tips
            item {
                HomePromoSection(
                    showTip = settings.showIdentityTip,
                    onDismissTip = { onUpdateSettings { it.copy(showIdentityTip = false) } },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Roster Setup Header
            item {
                HomeRosterHeader(modifier = Modifier.padding(horizontal = 24.dp))
            }

            // Smart Quick-Select Roster
            if (settings.savedPlayerNames.isNotEmpty() && settings.showQuickSelectOnHome) {
                item {
                    ActiveRosterSection(
                        savedPlayerNames = settings.savedPlayerNames,
                        currentNames = playerNames,
                        onSelectName = { name ->
                            playerNames = updateRosterSelection(playerNames, name, settings.maxPlayers)
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

            // Active Validation Feedback
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

            // Primary Roster Inputs
            itemsIndexed(
                items = playerNames,
                key = { index, _ -> "player_input_$index" } 
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

            // Interaction: Extend Roster
            if (playerNames.size < settings.maxPlayers) {
                item {
                    AddPlayerButton(
                        onClick = { 
                            autoFocusIndex = playerNames.size
                            playerNames = playerNames + ""
                            coroutineScope.launch {
                                // Scroll to ensure new input is visible
                                listState.animateScrollToItem(playerNames.lastIndex + 5)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Action: Command Start
            item {
                StartGameFloatingBar(
                    isVisible = isValid,
                    onStartGame = {
                        handleStartGame(
                            trimmedNames = trimmedNames,
                            settings = settings,
                            onUpdateSettings = onUpdateSettings,
                            onStartGame = onStartGame
                        )
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

/**
 * Logic for updating the player list when a name is selected via quick-select.
 */
private fun updateRosterSelection(
    currentNames: List<String>,
    selectedName: String,
    maxPlayers: Int
): List<String> {
    val normalizedName = selectedName.trim()
    val existingIndex = currentNames.indexOfFirst { it.trim().equals(normalizedName, ignoreCase = true) }
    
    return if (existingIndex != -1) {
        // Toggle off
        currentNames.filterNot { it.trim().equals(normalizedName, ignoreCase = true) }
    } else {
        // Toggle on or append
        currentNames.toMutableList().apply {
            val firstEmpty = indexOfFirst { it.trim().isEmpty() }
            if (firstEmpty != -1) {
                this[firstEmpty] = selectedName
            } else if (size < maxPlayers) {
                add(selectedName)
            }
        }
    }
}

/**
 * Handles the game start procedure including roster persistence logic.
 */
private fun handleStartGame(
    trimmedNames: List<String>,
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onStartGame: (List<String>) -> Unit
) {
    val shouldSavePlayers = if (settings.isGuestSession) settings.guestSavePlayers else true

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
}

/**
 * Visual button for appending a blank entry to the roster.
 */
@Composable
private fun AddPlayerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add, 
                contentDescription = null, 
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Add Another Player", 
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
