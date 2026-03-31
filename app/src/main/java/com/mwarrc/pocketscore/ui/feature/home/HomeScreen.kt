package com.mwarrc.pocketscore.ui.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.Player
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch
import com.mwarrc.pocketscore.ui.feature.home.components.*

/**
 * The primary entry point of the application.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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
    onNavigateToHelp: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    // Roster State
    var selectedPlayerNames by remember { mutableStateOf(emptyList<String>()) }
    var newlyAddedPlayerNames by remember { mutableStateOf(setOf<String>()) }
    var tempPlayerName by remember { mutableStateOf("") }
    var swipeOffsetX by remember { mutableStateOf(0f) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    BoxWithConstraints(modifier = modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.BottomCenter) {
        val minHeight = maxHeight
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (swipeOffsetX > 350f) {
                                onNavigateToHistory()
                            } else if (swipeOffsetX < -350f) {
                                onNavigateToSettings()
                            }
                            swipeOffsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            swipeOffsetX += dragAmount
                        }
                    )
                }
        ) {
            // Container that is at least the height of the screen to allow weight() to work
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    // Extra bottom padding reserves space so the sticky bar never overlaps the last tile
                    // The generous value also gives the PlayerInputSection comfortable breathing room above the bar
                    .padding(top = 56.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Section
                HomeHeader(
                    onNavigateToAbout = onNavigateToAbout,
                    onNavigateToHelp = onNavigateToHelp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                // Quick Actions / Resume Section
                if (hasActiveGame) {
                    QuickActionsCard(
                        activePlayers = activePlayers,
                        onResumeGame = onResumeGame,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToSettings = onNavigateToSettings,
                        showSettingsBadge = settings.backupsFolderUri == null,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                } else {
                    NavigationCard(
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToSettings = onNavigateToSettings,
                        showSettingsBadge = settings.backupsFolderUri == null,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // Feature Highlights & Pro Tips
                if (settings.showIdentityTip) {
                    HomePromoSection(
                        showTip = settings.showIdentityTip,
                        onDismissTip = { onUpdateSettings { it.copy(showIdentityTip = false) } },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // High-Level Session Feedback
                if (settings.isGuestSession) {
                    HomeSessionBanner(
                        settings = settings,
                        onSettingsClick = onNavigateToSettings,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // Roster pool OR compact selected list
                if (settings.showQuickSelectOnHome) {
                    RosterGridSection(
                        savedPlayerNames = settings.savedPlayerNames,
                        currentNames = selectedPlayerNames,
                        onSelectName = { name ->
                            val idx = selectedPlayerNames.indexOfFirst {
                                it.trim().equals(name.trim(), ignoreCase = true)
                            }
                            selectedPlayerNames = if (idx != -1) {
                                selectedPlayerNames.filterNot {
                                    it.trim().equals(name.trim(), ignoreCase = true)
                                }
                            } else {
                                if (selectedPlayerNames.size < settings.maxPlayers)
                                    selectedPlayerNames + name
                                else
                                    selectedPlayerNames
                            }
                        },
                        autoSortOption = settings.rosterSortOption,
                        onAutoSortOptionChange = { option ->
                            onUpdateSettings { it.copy(rosterSortOption = option) }
                        },
                        layout = settings.rosterLayout,
                        onLayoutChange = { layout ->
                            onUpdateSettings { it.copy(rosterLayout = layout) }
                        },
                        settings = settings,
                        history = history,
                        newlyAddedNames = newlyAddedPlayerNames,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    // Compact selected players list when roster pool is hidden
                    if (selectedPlayerNames.isNotEmpty()) {
                        SelectedPlayersSection(
                            selectedNames = selectedPlayerNames,
                            onRemoveName = { name ->
                                selectedPlayerNames = selectedPlayerNames.filterNot {
                                    it.equals(name, ignoreCase = true)
                                }
                            },
                            onSwap = { i1, i2 ->
                                val list = selectedPlayerNames.toMutableList()
                                if (i1 in list.indices && i2 in list.indices) {
                                    val temp = list[i1]
                                    list[i1] = list[i2]
                                    list[i2] = temp
                                    selectedPlayerNames = list
                                }
                            },
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }

                // Player input field
                PlayerInputSection(
                    name = tempPlayerName,
                    onNameChange = { newName ->
                        tempPlayerName = newName.filter { it.isLetterOrDigit() }.take(14)
                    },
                    savedNames = settings.savedPlayerNames,
                    selectedNames = selectedPlayerNames,
                    onAddPlayer = {
                        val newName = tempPlayerName.trim()
                        if (newName.isNotEmpty()) {
                            val updatedSaved = settings.savedPlayerNames.toMutableList()
                            if (!updatedSaved.any { it.equals(newName, ignoreCase = true) }) {
                                updatedSaved.add(newName)
                                onUpdateSettings { it.copy(savedPlayerNames = updatedSaved) }
                                newlyAddedPlayerNames = newlyAddedPlayerNames + newName
                            }
                            if (selectedPlayerNames.size < settings.maxPlayers) {
                                selectedPlayerNames = selectedPlayerNames + newName
                            }
                            tempPlayerName = ""
                        }
                    },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                scope.launch {
                                    kotlinx.coroutines.delay(250)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        }
                )

            }
        }

        // ── Sticky Start Match bar ─────────────────────────────────────────
        // Lives outside the scroll column so it's always visible regardless
        // of how many player tiles are rendered above.
        BottomStartGameBar(
            selectedCount = selectedPlayerNames.size,
            onStartGame = {
                handleStartGame(
                    trimmedNames = selectedPlayerNames,
                    settings = settings,
                    onUpdateSettings = onUpdateSettings,
                    onStartGame = onStartGame
                )
            },
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .navigationBarsPadding()
        )
    }
}  // end HomeScreen

/**
 * Handles the game start procedure including roster persistence logic.
 */
fun handleStartGame(
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
