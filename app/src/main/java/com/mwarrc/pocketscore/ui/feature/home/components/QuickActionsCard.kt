package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.Player

/**
 * A prominent call-to-action card displayed when a match is in progress or paused.
 *
 * @param isPaused When true the session is suspended: copy, badge and colours shift
 *                 to the paused treatment so it reads differently from a live match.
 */
@Composable
fun QuickActionsCard(
    activePlayers: List<Player>,
    isPaused: Boolean = false,
    onResumeGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    showSettingsBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section
        QuickActionsHeader(
            activePlayersCount = activePlayers.size,
            isPaused = isPaused
        )

        // Action Hierarchy
        QuickActionsButtonGroup(
            activePlayers = activePlayers,
            isPaused = isPaused,
            onResumeGame = onResumeGame,
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings,
            showSettingsBadge = showSettingsBadge
        )
    }
}

@Composable
private fun QuickActionsHeader(activePlayersCount: Int, isPaused: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (isPaused) "Paused Match" else "Ongoing Match",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isPaused) "$activePlayersCount players · progress saved"
                       else "$activePlayersCount players in session",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        // Live / Paused badge
        Surface(
            color = if (isPaused) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
            shape = CircleShape
        ) {
            Text(
                text = if (isPaused) " PAUSED " else " LIVE ",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = if (isPaused) MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ActiveRosterOverlapList(players: List<Player>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((-8).dp)
    ) {
        players.take(5).forEach { player ->
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = player.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        if (players.size > 5) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+${players.size - 5}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsButtonGroup(
    activePlayers: List<Player>,
    isPaused: Boolean = false,
    onResumeGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    showSettingsBadge: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Primary Action
        Button(
            onClick = onResumeGame,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (isPaused) "Resume Match" else "Resume Session", 
                        fontWeight = FontWeight.Black, 
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                
                ActiveRosterOverlapList(players = activePlayers)
            }
        }
 
        RecordsSettingsRow(
            onNavigateToHistory = onNavigateToHistory,
            onNavigateToSettings = onNavigateToSettings,
            showSettingsBadge = showSettingsBadge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
