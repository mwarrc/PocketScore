package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.Player

/**
 * A prominent call-to-action card displayed when a match is currently in progress.
 * 
 * Features:
 * - **Live Status**: Visual branding indicating a "LIVE" session.
 * - **Roster Preview**: Overlapping avatar list showing who is currently playing.
 * - **Fast Action**: Large button to immediately jump back into the active game.
 * - **Secondary Navigation**: Quick links to match history and settings.
 * 
 * @param activePlayers The list of players currently participating in the live game.
 * @param onResumeGame Callback to navigate back to the live match screen.
 * @param onNavigateToHistory Callback to navigate to the match history tab.
 * @param onNavigateToSettings Callback to open the application settings.
 * @param modifier Modifier for the card container.
 */
@Composable
fun QuickActionsCard(
    activePlayers: List<Player>,
    onResumeGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    showSettingsBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            QuickActionsHeader(activePlayersCount = activePlayers.size)

            // Roster Visualization
            if (activePlayers.isNotEmpty()) {
                ActiveRosterOverlapList(players = activePlayers)
            }

            // Action Hierarchy
            QuickActionsButtonGroup(
                onResumeGame = onResumeGame,
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = onNavigateToSettings,
                showSettingsBadge = showSettingsBadge
            )
        }
    }
}

@Composable
private fun QuickActionsHeader(activePlayersCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Ongoing Match",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$activePlayersCount players in session",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        // Live Badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {
            Text(
                text = " LIVE ",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ActiveRosterOverlapList(players: List<Player>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy((-8).dp) // Overlap style
    ) {
        players.take(5).forEach { player ->
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = player.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        if (players.size > 5) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+${players.size - 5}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsButtonGroup(
    onResumeGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    showSettingsBadge: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Primary Action
        Button(
            onClick = onResumeGame,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Resume Session", fontWeight = FontWeight.ExtraBold)
        }
 
        // Secondary Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToHistory,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Records", 
                    style = MaterialTheme.typography.labelMedium, 
                    fontWeight = FontWeight.Bold
                )
            }
 
            Box {
                OutlinedButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                }

                if (showSettingsBadge) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(7.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        shadowElevation = 4.dp
                    ) { }
                }
            }
        }
    }
}
