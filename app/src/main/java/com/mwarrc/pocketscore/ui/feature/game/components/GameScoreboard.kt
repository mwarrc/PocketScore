package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout

/**
 * Main Scoreboard Component - Handles both Grid and List layouts.
 * 
 * This component acts as a high-level router that chooses between [GridScoreboard] 
 * and [ListScoreboard] based on the user's preference in [AppSettings].
 * 
 * @param players Full list of players in the game (active and inactive)
 * @param currentPlayerId ID of the player currently in turn
 * @param settings Current application settings
 * @param leaderIds Set of player IDs who are currently leading
 * @param isTie Whether multiple players are currently leading
 * @param loserIds Set of player IDs who are currently in last place
 * @param isLoserTie Whether multiple players are currently in last place
 * @param playerLastChanges Map of player IDs to their last score change
 * @param scoreInput Current text input value for points
 * @param onScoreInputChange Callback when score input text changes
 * @param onShowNumpad Callback to trigger showing the custom numpad
 * @param onUpdateScore Callback to add/subtract points for a player
 * @param onSetCurrentPlayer Callback to manually change the turn
 * @param listBottomPadding Bottom padding to accommodate floating UI/numpads
 * @param tableSum Current sum of ball values remaining on the table
 * @param leaderScore The score of the current leader(s)
 * @param selectionForHeader The player currently focused in the Grid mode header
 * @param onHeaderSelection Callback when a player is selected in Grid mode
 * @param modifier Base modifier for the scoreboard container
 */
@Composable
fun GameScoreboard(
    players: List<Player>,
    currentPlayerId: String?,
    settings: AppSettings,
    leaderIds: Set<String>,
    isTie: Boolean,
    loserIds: Set<String>,
    isLoserTie: Boolean,
    playerLastChanges: Map<String, Int?>,
    scoreInput: String,
    onScoreInputChange: (String) -> Unit,
    onShowNumpad: () -> Unit,
    onUpdateScore: (String, Int) -> Unit,
    onSetCurrentPlayer: (String) -> Unit,
    listBottomPadding: Dp,
    tableSum: Int,
    leaderScore: Int,
    selectionForHeader: Player?,
    onHeaderSelection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlayers: List<Player> by remember(players) { 
        derivedStateOf { players.filter { it.isActive } }
    }
    val haptic = LocalHapticFeedback.current

    if (settings.defaultLayout == ScoreboardLayout.GRID) {
        GridScoreboard(
            activePlayers = activePlayers,
            currentPlayerId = currentPlayerId,
            settings = settings,
            leaderIds = leaderIds,
            isTie = isTie,
            loserIds = loserIds,
            isLoserTie = isLoserTie,
            playerLastChanges = playerLastChanges,
            scoreInput = scoreInput,
            onScoreInputChange = onScoreInputChange,
            onShowNumpad = onShowNumpad,
            onUpdateScore = onUpdateScore,
            onSetCurrentPlayer = onSetCurrentPlayer,
            listBottomPadding = listBottomPadding,
            tableSum = tableSum,
            leaderScore = leaderScore,
            selectionForHeader = selectionForHeader,
            onHeaderSelection = onHeaderSelection,
            haptic = haptic,
            modifier = modifier
        )
    } else {
        ListScoreboard(
            activePlayers = activePlayers,
            currentPlayerId = currentPlayerId,
            settings = settings,
            leaderIds = leaderIds,
            isTie = isTie,
            loserIds = loserIds,
            isLoserTie = isLoserTie,
            playerLastChanges = playerLastChanges,
            scoreInput = scoreInput,
            onScoreInputChange = onScoreInputChange,
            onShowNumpad = onShowNumpad,
            onUpdateScore = onUpdateScore,
            onSetCurrentPlayer = onSetCurrentPlayer,
            listBottomPadding = listBottomPadding,
            tableSum = tableSum,
            leaderScore = leaderScore,
            haptic = haptic,
            modifier = modifier
        )
    }
}

/**
 * Internal Grid Layout Implementation.
 * 
 * Displays players in a 2-column grid with a prominent "Focused Player" card at the top.
 * Tapping a player in the grid promotes them to the header card for score entry.
 */
@Composable
private fun GridScoreboard(
    activePlayers: List<Player>,
    currentPlayerId: String?,
    settings: AppSettings,
    leaderIds: Set<String>,
    isTie: Boolean,
    loserIds: Set<String>,
    isLoserTie: Boolean,
    playerLastChanges: Map<String, Int?>,
    scoreInput: String,
    onScoreInputChange: (String) -> Unit,
    onShowNumpad: () -> Unit,
    onUpdateScore: (String, Int) -> Unit,
    onSetCurrentPlayer: (String) -> Unit,
    listBottomPadding: Dp,
    tableSum: Int,
    leaderScore: Int,
    selectionForHeader: Player?,
    onHeaderSelection: (String) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val lazyGridState = rememberLazyGridState()

    LaunchedEffect(currentPlayerId, settings.autoScrollToActivePlayer) {
        if (settings.autoScrollToActivePlayer) {
            val index = activePlayers.indexOfFirst { it.id == currentPlayerId }
            if (index != -1) {
                lazyGridState.animateScrollToItem(index)
            }
        }
    }

    Column(modifier = modifier) {
        // Header card for selected player
        if (selectionForHeader != null) {
            Box(modifier = Modifier.padding(16.dp)) {
                ActivePlayerCard(
                    player = selectionForHeader,
                    isLeader = leaderIds.contains(selectionForHeader.id),
                    isTie = isTie,
                    isLoser = loserIds.contains(selectionForHeader.id),
                    isLoserTie = isLoserTie,
                    isCurrentTurn = selectionForHeader.id == currentPlayerId,
                    isStrictTurnMode = settings.strictTurnMode,
                    allowEliminatedInput = settings.allowEliminatedInput,
                    lastPoints = playerLastChanges[selectionForHeader.id],
                    scoreInput = scoreInput,
                    onScoreInputChange = onScoreInputChange,
                    onFocus = onShowNumpad,
                    useCustomKeyboard = settings.useCustomKeyboard,
                    leaderScore = leaderScore,
                    tableSum = tableSum,
                    poolBallManagementEnabled = settings.poolBallManagementEnabled,
                    onAdd = { pts ->
                        if (settings.hapticFeedbackEnabled) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        }
                        onUpdateScore(selectionForHeader.id, pts)
                    },
                    onSubtract = { pts ->
                        if (settings.hapticFeedbackEnabled) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        }
                        onUpdateScore(selectionForHeader.id, -pts)
                    },
                    alwaysShowControls = true
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        // Grid of player cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = lazyGridState,
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = listBottomPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activePlayers, key = { it.id }) { player ->
                val isLeader = leaderIds.contains(player.id)
                val isLoser = loserIds.contains(player.id)
                val isSelected = player.id == selectionForHeader?.id
                val isActualTurn = player.id == currentPlayerId

                PassivePlayerCard(
                    player = player,
                    isLeader = isLeader,
                    isTie = isTie,
                    isLoser = isLoser,
                    isLoserTie = isLoserTie,
                    isCurrent = isSelected,
                    isActualTurn = isActualTurn,
                    lastPoints = playerLastChanges[player.id],
                    leaderScore = leaderScore,
                    tableSum = tableSum,
                    poolBallManagementEnabled = settings.poolBallManagementEnabled,
                    onClick = {
                        onHeaderSelection(player.id)
                        if (!settings.strictTurnMode) {
                            onSetCurrentPlayer(player.id)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Internal List Layout Implementation.
 * 
 * Displays players in a single vertical list. Each card is interactive 
 * and can be expanded for score entry.
 */
@Composable
private fun ListScoreboard(
    activePlayers: List<Player>,
    currentPlayerId: String?,
    settings: AppSettings,
    leaderIds: Set<String>,
    isTie: Boolean,
    loserIds: Set<String>,
    isLoserTie: Boolean,
    playerLastChanges: Map<String, Int?>,
    scoreInput: String,
    onScoreInputChange: (String) -> Unit,
    onShowNumpad: () -> Unit,
    onUpdateScore: (String, Int) -> Unit,
    onSetCurrentPlayer: (String) -> Unit,
    listBottomPadding: Dp,
    tableSum: Int,
    leaderScore: Int,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentPlayerId, settings.autoScrollToActivePlayer) {
        if (settings.autoScrollToActivePlayer) {
            val index = activePlayers.indexOfFirst { it.id == currentPlayerId }
            if (index != -1) {
                lazyListState.animateScrollToItem(index, scrollOffset = -150)
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = listBottomPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activePlayers, key = { it.id }) { player ->
            val isLeader = leaderIds.contains(player.id)
            val isLoser = loserIds.contains(player.id)
            val isTurn = player.id == currentPlayerId

            ActivePlayerCard(
                player = player,
                isLeader = isLeader,
                isTie = isTie,
                isLoser = isLoser,
                isLoserTie = isLoserTie,
                isCurrentTurn = isTurn,
                isStrictTurnMode = settings.strictTurnMode,
                allowEliminatedInput = settings.allowEliminatedInput,
                lastPoints = playerLastChanges[player.id],
                scoreInput = if (isTurn) scoreInput else "",
                onScoreInputChange = { if (isTurn) onScoreInputChange(it) },
                onFocus = onShowNumpad,
                useCustomKeyboard = settings.useCustomKeyboard,
                leaderScore = leaderScore,
                tableSum = tableSum,
                poolBallManagementEnabled = settings.poolBallManagementEnabled,
                onAdd = { pts ->
                    if (settings.hapticFeedbackEnabled) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }
                    onUpdateScore(player.id, pts)
                },
                onSubtract = { pts ->
                    if (settings.hapticFeedbackEnabled) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }
                    onUpdateScore(player.id, -pts)
                },
                onSetTurn = { onSetCurrentPlayer(player.id) }
            )
        }
    }
}
