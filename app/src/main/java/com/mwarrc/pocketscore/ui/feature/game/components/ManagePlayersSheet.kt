package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.ui.util.ImmersiveMode

/**
 * Bottom sheet allowing mid-match roster adjustments, subtle turn reordering,
 * and graceful mid-game player leave/withdraw.
 * 
 * @param players Full list of players in the session
 * @param currentPlayerId ID of the player whose turn it currently is
 * @param settings Current application configuration
 * @param onTogglePlayerActive Callback to activate/deactivate a specific player
 * @param onReorderPlayers Callback when players turn order is rearranged
 * @param onWithdrawPlayer Callback when a player leaves the match mid-game
 * @param onDismiss Callback to close the sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePlayersSheet(
    players: List<Player>,
    currentPlayerId: String?,
    settings: AppSettings,
    onTogglePlayerActive: (String, Boolean) -> Unit,
    onReorderPlayers: (List<Player>) -> Unit = {},
    onWithdrawPlayer: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val currentTurnPlayer = players.find { it.id == currentPlayerId }

    var isReordering by remember { mutableStateOf(false) }
    var playerToWithdraw by remember { mutableStateOf<Player?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    // Shared cap: at most 75% of the physical screen, matching every other
    // sheet on this screen so they open to a consistent height and always leave
    // a quarter of the screen tappable. Was 0.85f.
    val maxContentHeight = rememberGameSheetMaxHeight()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            // No statusBarsPadding()/displayCutoutPadding() on the handle: the
            // content column below already insets, and stacking both produced a
            // dead band of empty space at the top of the sheet.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f))
                )
            }
        }
    ) {
        ImmersiveMode()

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset = available

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity = available
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxContentHeight)
                .navigationBarsPadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Row: Title + Subtle Reorder Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Manage Players",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (settings.strictTurnMode) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Strict",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Quiet, unobtrusive Reorder toggle chip (borderless)
                    AssistChip(
                        onClick = { isReordering = !isReordering },
                        label = {
                            Text(
                                if (isReordering) "Done" else "Reorder",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (isReordering) Icons.Default.Check else Icons.Default.SwapVert,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isReordering) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            labelColor = if (isReordering) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            leadingIconContentColor = if (isReordering) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        border = null,
                        shape = CircleShape
                    )
                }
            }

            if (isReordering) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SwapVert,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Use arrows to adjust the player turn sequence.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (settings.strictTurnMode) {
                Spacer(Modifier.height(12.dp))
                // Solid container: `onTertiaryContainer` is guaranteed to be legible on
                // a full-opacity `tertiaryContainer`. The previous 0.3f alpha wash
                // broke that pairing and washed the text out in dark mode (especially
                // with dynamic/Material You colours, where the token values vary).
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Tip: In Strict Mode, player roster is locked. Players can leave mid-game using the leave button.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            } else if (currentTurnPlayer != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Current Turn: ${currentTurnPlayer.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(20.dp))

            // Player Items List
            players.forEachIndexed { index, player ->
                val isCurrentTurnPlayer = player.id == currentPlayerId
                val currentActiveCount = players.count { it.isActive }
                val canToggle = !settings.strictTurnMode || !player.isActive
                val canLeave = (currentActiveCount > 2 || !player.isActive) && players.size > 2

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isCurrentTurnPlayer) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .alpha(if (player.isActive) 1f else 0.55f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Order Index Badge
                        Surface(
                            shape = CircleShape,
                            color = if (isCurrentTurnPlayer) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentTurnPlayer) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        // Player Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    player.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isCurrentTurnPlayer) FontWeight.Bold else FontWeight.Medium
                                )
                                if (player.score != 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                    ) {
                                        Text(
                                            "${player.score} pts",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            if (isCurrentTurnPlayer) {
                                Text(
                                    "Playing Now",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else if (!player.isActive) {
                                Text(
                                    "Withdrawn / Inactive",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Right-side actions: Reorder buttons OR (Switch + Leave)
                        if (isReordering) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val reordered = players.toMutableList()
                                            val temp = reordered[index]
                                            reordered[index] = reordered[index - 1]
                                            reordered[index - 1] = temp
                                            onReorderPlayers(reordered)
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Move turn up",
                                        tint = if (index > 0) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (index < players.size - 1) {
                                            val reordered = players.toMutableList()
                                            val temp = reordered[index]
                                            reordered[index] = reordered[index + 1]
                                            reordered[index + 1] = temp
                                            onReorderPlayers(reordered)
                                        }
                                    },
                                    enabled = index < players.size - 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Move turn down",
                                        tint = if (index < players.size - 1) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        }
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Mid-game leave / withdraw button
                                IconButton(
                                    onClick = { playerToWithdraw = player },
                                    enabled = canLeave,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PersonRemove,
                                        contentDescription = "Leave Match",
                                        tint = if (canLeave) {
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Active toggle switch
                                Switch(
                                    checked = player.isActive,
                                    onCheckedChange = { onTogglePlayerActive(player.id, it) },
                                    enabled = canToggle && (player.isActive && currentActiveCount > 2 || !player.isActive)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Close Menu", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    // Confirmation Dialog for Player Leaving / Withdrawing Mid-Game
    playerToWithdraw?.let { player ->
        val hasPoints = player.score != 0
        AlertDialog(
            onDismissRequest = { playerToWithdraw = null },
            icon = {
                Icon(
                    Icons.Default.PersonRemove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = if (hasPoints) "Withdraw ${player.name}?" else "Remove ${player.name}?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (hasPoints) {
                        Text(
                            "${player.name} has recorded ${player.score} points. They will withdraw from remaining turns, but their score and stats remain safely recorded in match history.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            "${player.name} hasn't scored any points in this match. They will be removed from the active game roster.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Remaining active players will continue playing without disruption.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onWithdrawPlayer(player.id)
                        playerToWithdraw = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(if (hasPoints) "Withdraw Player" else "Remove Player")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { playerToWithdraw = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

