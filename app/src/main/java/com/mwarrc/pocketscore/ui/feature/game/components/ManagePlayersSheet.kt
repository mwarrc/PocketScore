package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.Player

/**
 * Bottom sheet allowing mid-match roster adjustments.
 * 
 * Users can enable or disable players. If "Strict Turn Mode" is active,
 * existing players cannot be disabled to preserve game integrity.
 * 
 * @param players Full list of players in the session
 * @param currentPlayerId ID of the player whose turn it currently is
 * @param settings Current application configuration
 * @param onTogglePlayerActive Callback to activate/deactivate a specific player
 * @param onDismiss Callback to close the sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePlayersSheet(
    players: List<Player>,
    currentPlayerId: String?,
    settings: AppSettings,
    onTogglePlayerActive: (String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val currentTurnPlayer = players.find { it.id == currentPlayerId }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .systemBarsPadding()
                .displayCutoutPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
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
                                "Strict Mode: Fixed Roster",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (settings.strictTurnMode) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
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
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Tip: In Strict Mode, you can add players back, but you cannot disable them. Record a zero to pass a turn instead.",
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

            Spacer(Modifier.height(24.dp))

            players.forEach { player ->
                val isCurrentTurnPlayer = player.id == currentPlayerId
                val canToggle = !settings.strictTurnMode || !player.isActive

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .alpha(if (canToggle) 1f else 0.6f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            player.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isCurrentTurnPlayer) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isCurrentTurnPlayer) {
                            Text(
                                "Is Playing Now",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    val currentActiveCount = players.count { it.isActive }
                    Switch(
                        checked = player.isActive,
                        onCheckedChange = { onTogglePlayerActive(player.id, it) },
                        enabled = canToggle && (player.isActive && currentActiveCount > 2 || !player.isActive)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Close, null)
                Spacer(Modifier.width(8.dp))
                Text("Close Menu")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
