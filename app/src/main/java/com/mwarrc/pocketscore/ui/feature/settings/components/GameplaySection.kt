package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.settings.InfoContent

/**
 * Section for configuring gameplay-related settings.
 * Includes options for haptic feedback, spotlights, strict mode, and saved players.
 *
 * @param settings The current [AppSettings].
 * @param onUpdateSettings Callback to update the settings.
 */
@Composable
fun GameplaySection(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var showStrictModeInfo by remember { mutableStateOf(false) }
    var showEnforceDialog by remember { mutableStateOf(false) }

    var infoDialogContent by remember { mutableStateOf<InfoContent?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Gameplay",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        SettingsItem(
            title = "Haptic Feedback",
            subtitle = "Subtle vibration on score changes",
            icon = Icons.Default.TouchApp,
            onIconClick = {
                infoDialogContent = InfoContent(
                    title = "Haptic Feedback",
                    description = "Provides tactile confirmation through subtle phone vibrations whenever you increment or decrement a score. This helps ensure your input was registered without looking at the screen.",
                    icon = Icons.Default.TouchApp
                )
            },
            trailing = {
                Switch(
                    checked = settings.hapticFeedbackEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(hapticFeedbackEnabled = enabled) }
                    }
                )
            }
        )

        SettingsItem(
            title = "Leader Spotlight",
            subtitle = "Highlight player with highest points",
            icon = Icons.Default.Palette,
            onIconClick = {
                infoDialogContent = InfoContent(
                    title = "Leader Spotlight",
                    description = "Visually distinguishes the current leader in the scoreboard with a persistent 'LEADER' badge and a high-contrast background. Perfect for keeping track of the front-runner at a glance.",
                    icon = Icons.Default.Palette
                )
            },
            trailing = {
                Switch(
                    checked = settings.leaderSpotlightEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(leaderSpotlightEnabled = enabled) }
                    }
                )
            }
        )

        SettingsItem(
            title = "Loser Spotlight",
            subtitle = "Highlight player with lowest points",
            icon = Icons.Default.Palette,
            onIconClick = {
                infoDialogContent = InfoContent(
                    title = "Loser Spotlight",
                    description = "Adds a subtle highlight to the player with the lowest score. Useful for identifying who needs to catch up or who is following the 'loser starts' rule.",
                    icon = Icons.Default.Palette
                )
            },
            trailing = {
                Switch(
                    checked = settings.loserSpotlightEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(loserSpotlightEnabled = enabled) }
                    }
                )
            }
        )

        SettingsItem(
            title = "In-Match Mode Alerts",
            subtitle = "Banner when mode changes (Strict/Free)",
            icon = Icons.Default.Info,
            onIconClick = {
                infoDialogContent = InfoContent(
                    title = "In-Match Mode Alerts",
                    description = "Displays a clear notification banner at the top of the game screen whenever 'Strict Turn Mode' is toggled. This ensures all players are aware of the current scoring rules.",
                    icon = Icons.Default.Info
                )
            },
            trailing = {
                Switch(
                    checked = settings.showStrictModeBanner,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(showStrictModeBanner = enabled) }
                    }
                )
            }
        )

        SettingsItem(
            title = "Saved Players",
            subtitle = "Show saved players on Home Screen",
            icon = Icons.Default.Group,
            onIconClick = {
                infoDialogContent = InfoContent(
                    title = "Saved Players",
                    description = "Enables a quick-select carousel on the home screen featuring your most frequent players. This makes setting up a new match much faster by avoiding manual name entry.",
                    icon = Icons.Default.Group
                )
            },
            trailing = {
                Switch(
                    checked = settings.showQuickSelectOnHome,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(showQuickSelectOnHome = enabled) }
                    }
                )
            }
        )

        SettingsItem(
            title = "Lock Scoring Rules",
            subtitle = if (settings.enforceStrictMode) "Strict Mode cannot be disabled in-game" else "Enable to lock turn-based security",
            icon = if (settings.enforceStrictMode) Icons.Default.Lock else Icons.Default.LockOpen,
            onIconClick = { showStrictModeInfo = true },
            trailing = {
                Switch(
                    checked = settings.enforceStrictMode,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            showEnforceDialog = true
                        } else {
                            onUpdateSettings { it.copy(enforceStrictMode = false) }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.error,
                        checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                        checkedBorderColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        )
    }

    infoDialogContent?.let { info ->
        SettingInfoDialog(
            title = info.title,
            description = info.description,
            icon = info.icon,
            onDismiss = { infoDialogContent = null }
        )
    }

    if (showEnforceDialog) {
        AlertDialog(
            onDismissRequest = { showEnforceDialog = false },
            icon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Enforce Strict Mode?") },
            text = {
                Text("When this is enabled, 'Strict Turn Mode' will be forced ON and cannot be turned off during a game session. This feature only allows single score inputs per player's round. Not recommended for casual games.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings { it.copy(enforceStrictMode = true, strictTurnMode = true, autoNextTurn = true) }
                        showEnforceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Confirm Lock") }
            },
            dismissButton = {
                TextButton(onClick = { showEnforceDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showStrictModeInfo) {
        StrictModeInfoDialog(onDismissRequest = { showStrictModeInfo = false })
    }
}
