package com.mwarrc.pocketscore.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.animateScrollBy
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.Player
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch
import com.mwarrc.pocketscore.ui.feature.home.components.*
import androidx.compose.animation.*

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
    var isInputFocused by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    // Outer Box — the BottomStartGameBar is overlaid here so it is completely
    // detached from the Scaffold / IME machinery and never moves with the keyboard.
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffsetX > 100f) {
                            onNavigateToHistory()
                        } else if (swipeOffsetX < -100f) {
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

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface,
            // No bottomBar slot — keeps Scaffold from reserving space above the keyboard
        ) { padding ->
            // Surface-colored box fills the ENTIRE scaffold area so no colour
            // bleed appears in the ime-inset region when the keyboard opens.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding(),   // shrinks the scroll viewport above the keyboard
                    // bottom padding massively increases when focused so the list isn't conceptually
                    // "stuck" at the bottom boundary, allowing the heavy manual scroll to push the input high up
                    contentPadding = PaddingValues(top = 56.dp, bottom = if (isInputFocused) 160.dp else 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
            // Hero Section
            item {
                HomeHeader(
                    onNavigateToAbout = onNavigateToAbout,
                    onNavigateToHelp = onNavigateToHelp,
                    modifier = Modifier.padding(horizontal = 24.dp)
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
            }

            // Feature Highlights & Pro Tips
            if (settings.showIdentityTip) {
                item {
                    HomePromoSection(
                        showTip = settings.showIdentityTip,
                        onDismissTip = { onUpdateSettings { it.copy(showIdentityTip = false) } },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // High-Level Session Feedback
            if (settings.isGuestSession) {
                item {
                    HomeSessionBanner(
                        settings = settings,
                        onSettingsClick = onNavigateToSettings,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            // Visible gap before roster section
            item {
                Spacer(Modifier.height(8.dp))
            }

            // Roster pool OR compact selected list
            if (settings.showQuickSelectOnHome) {
                item {
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
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 4.dp)
                    )
                }
            } else {
                // Compact selected players list when roster pool is hidden
                if (selectedPlayerNames.isNotEmpty()) {
                    item {
                        SelectedPlayersSection(
                            selectedNames = selectedPlayerNames,
                            onRemoveName = { name ->
                                selectedPlayerNames = selectedPlayerNames.filterNot {
                                    it.equals(name, ignoreCase = true)
                                }
                            },
                            onMoveToEnd = { name ->
                                val targetName = selectedPlayerNames.find { it.equals(name, ignoreCase = true) }
                                if (targetName != null) {
                                    selectedPlayerNames = selectedPlayerNames.filterNot { it.equals(targetName, ignoreCase = true) } + targetName
                                }
                            },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Always-visible Quick Add bar
            item {
                PlayerInputSection(
                    name = tempPlayerName,
                    onNameChange = { newName -> 
                        tempPlayerName = newName.filter { it.isLetterOrDigit() }.take(26) 
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
                        .padding(top = 4.dp, bottom = 8.dp)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusEvent { focusState ->
                            isInputFocused = focusState.isFocused
                            if (focusState.isFocused) {
                                scope.launch {
                                    // Small delay allows keyboard animation to start
                                    kotlinx.coroutines.delay(200)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        }
                )
            }
                }  // end LazyColumn
            }  // end Box (surface background)
        }  // end Scaffold

        // ── Floating bar overlay --
        // We hide this when keyboard is open to prevent a "gap" above the keyboard.
        val isKeyboardVisible = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp
        
        AnimatedVisibility(
            visible = !isKeyboardVisible,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            BottomStartGameBar(
                selectedCount = selectedPlayerNames.size,
                onStartGame = {
                    handleStartGame(
                        trimmedNames = selectedPlayerNames,
                        settings = settings,
                        onUpdateSettings = onUpdateSettings,
                        onStartGame = onStartGame
                    )
                }
            )
        }
    }  // end outer Box
}

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
