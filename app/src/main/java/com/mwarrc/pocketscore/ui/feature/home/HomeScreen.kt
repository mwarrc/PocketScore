package com.mwarrc.pocketscore.ui.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.GameHistory
import com.mwarrc.pocketscore.domain.model.Player
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.focus.FocusRequester
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
    isPaused: Boolean = false,
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

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val playerInputFocusRequester = remember { FocusRequester() }
    // Set while the "+" shortcut is driving the scroll, so the field's own
    // onFocusEvent handler doesn't launch a competing bringIntoView().
    val suppressFocusScroll = remember { mutableStateOf(false) }

    // Jump straight to the name field - used by the "+" shortcut in the roster
    // header so adding a player doesn't require scrolling past the whole pool.
    //
    // Order matters: request focus FIRST so the keyboard starts rising, then
    // scroll. Scrolling first is what broke the cold case - `maxValue` is
    // measured against the un-shrunk viewport, so the list stopped exactly one
    // keyboard-height short. Focusing first grows `maxValue` by the IME inset,
    // so the scroll then travels the full distance.
    val focusPlayerInput: () -> Unit = {
        scope.launch {
            suppressFocusScroll.value = true
            playerInputFocusRequester.requestFocus()

            // Scroll toward the bottom via a sentinel rather than `maxValue`.
            // `animateScrollTo` clamps to maxValue internally, so this always
            // resolves against the LIVE viewport height - no stale measurement.
            // (The first tap's focus + IME sequence resizes the viewport, which
            // is what made a pre-measured maxValue land short.)
            scrollState.animateScrollTo(Int.MAX_VALUE)

            // Re-assert after the IME settles, in case the viewport grew.
            kotlinx.coroutines.delay(400)
            scrollState.animateScrollTo(Int.MAX_VALUE)

            suppressFocusScroll.value = false
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.BottomCenter) {
        val minHeight = maxHeight
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Container that is at least the height of the screen to allow weight() to work
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    // Bottom padding reserves space so the sticky bar never overlaps the
                    // last tile. Top inset is owned by HomeHeader's statusBarsPadding(),
                    // so only a small gap is needed here.
                    .padding(top = 8.dp, bottom = 140.dp)
            ) {
                // ── Hero ───────
                // Swipe-to-navigate is scoped to this band only, so it can't fire
                // while the user is dragging across the roster or the name field.
                SwipeNavigationArea(
                    onSwipeLeft = onNavigateToSettings,
                    onSwipeRight = onNavigateToHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomeHeader(
                        onNavigateToAbout = onNavigateToAbout,
                        onNavigateToHelp = onNavigateToHelp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Resume an in-progress match, or navigate ────────
                // Kept near the top: resuming beats everything else when a match
                // is already running.
                if (hasActiveGame) {
                    QuickActionsCard(
                        activePlayers = activePlayers,
                        isPaused = isPaused,
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

                Spacer(Modifier.height(28.dp))

                // ── The main job: choose players ────────────
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
                        onAddPlayerClick = focusPlayerInput,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(Modifier.height(20.dp))

                    // Type-to-add lives with the roster: it's a roster action.
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
                                val canonicalName = settings.savedPlayerNames.firstOrNull { it.equals(newName, ignoreCase = true) } ?: newName
                                val updatedSaved = settings.savedPlayerNames.toMutableList()
                                if (!updatedSaved.any { it.equals(canonicalName, ignoreCase = true) }) {
                                    updatedSaved.add(canonicalName)
                                }
                                val updatedDeactivated = settings.deactivatedPlayers.filterNot { it.equals(canonicalName, ignoreCase = true) }
                                if (updatedSaved != settings.savedPlayerNames || updatedDeactivated != settings.deactivatedPlayers) {
                                    onUpdateSettings { it.copy(savedPlayerNames = updatedSaved, deactivatedPlayers = updatedDeactivated) }
                                }
                                newlyAddedPlayerNames = newlyAddedPlayerNames + canonicalName

                                if (selectedPlayerNames.size < settings.maxPlayers && !selectedPlayerNames.any { it.equals(canonicalName, ignoreCase = true) }) {
                                    selectedPlayerNames = selectedPlayerNames + canonicalName
                                }
                                tempPlayerName = ""
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused && !suppressFocusScroll.value) {
                                    scope.launch {
                                        kotlinx.coroutines.delay(250)
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        focusRequester = playerInputFocusRequester,
                    )
                } else {
                    // Roster pool hidden: lead with the current line-up so the
                    // screen never opens on an empty void, then allow adding.
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

                        Spacer(Modifier.height(20.dp))
                    }

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
                                val canonicalName = settings.savedPlayerNames.firstOrNull { it.equals(newName, ignoreCase = true) } ?: newName
                                val updatedSaved = settings.savedPlayerNames.toMutableList()
                                if (!updatedSaved.any { it.equals(canonicalName, ignoreCase = true) }) {
                                    updatedSaved.add(canonicalName)
                                }
                                val updatedDeactivated = settings.deactivatedPlayers.filterNot { it.equals(canonicalName, ignoreCase = true) }
                                if (updatedSaved != settings.savedPlayerNames || updatedDeactivated != settings.deactivatedPlayers) {
                                    onUpdateSettings { it.copy(savedPlayerNames = updatedSaved, deactivatedPlayers = updatedDeactivated) }
                                }
                                newlyAddedPlayerNames = newlyAddedPlayerNames + canonicalName

                                if (selectedPlayerNames.size < settings.maxPlayers && !selectedPlayerNames.any { it.equals(canonicalName, ignoreCase = true) }) {
                                    selectedPlayerNames = selectedPlayerNames + canonicalName
                                }
                                tempPlayerName = ""
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent { focusState ->
                                if (focusState.isFocused && !suppressFocusScroll.value) {
                                    scope.launch {
                                        kotlinx.coroutines.delay(250)
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            },
                        focusRequester = playerInputFocusRequester,
                    )
                }

                // ── Secondary: status note, then onboarding ────────
                // These are informational, so they sit below the work surface.
                if (settings.isGuestSession) {
                    Spacer(Modifier.height(24.dp))
                    HomeSessionBanner(
                        settings = settings,
                        onSettingsClick = onNavigateToSettings,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // One-time onboarding tip. Also auto-retires once the user has
                // played a match, so it never lingers past the first session.
                if (settings.showIdentityTip && settings.gamesPlayedCount == 0) {
                    Spacer(Modifier.height(20.dp))
                    HomePromoSection(
                        showTip = true,
                        onDismissTip = { onUpdateSettings { it.copy(showIdentityTip = false) } },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }

        // ── Sticky Start Match bar ────
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
