package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSettingsSheet(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Quick Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                val isDark = settings.isDarkMode == true
                IconButton(
                    onClick = {
                        onUpdateSettings { it.copy(isDarkMode = !isDark) }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Scoreboard Layout", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // List View Toggle
                    if (settings.defaultLayout == ScoreboardLayout.LIST) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { /* Already selected */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.ViewAgenda, null)
                            Spacer(Modifier.width(8.dp))
                            Text("List View")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onUpdateSettings { it.copy(defaultLayout = ScoreboardLayout.LIST) }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.ViewAgenda, null)
                            Spacer(Modifier.width(8.dp))
                            Text("List View")
                        }
                    }

                    // Grid View Toggle
                    if (settings.defaultLayout == ScoreboardLayout.GRID) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { /* Already selected */ },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.GridView, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Grid View")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onUpdateSettings { it.copy(defaultLayout = ScoreboardLayout.GRID) }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.GridView, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Grid View")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Leader Spotlight", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Highlight the current winner",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.leaderSpotlightEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(leaderSpotlightEnabled = enabled) }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Custom Numpad", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Use minimal keyboard for scoring",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.useCustomKeyboard,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(useCustomKeyboard = enabled) }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Strict Turn Mode", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (settings.enforceStrictMode) "Rules Locked (In Settings)" 
                        else if (settings.strictTurnMode) "Security Active" 
                        else "Quick play enabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    enabled = !settings.enforceStrictMode,
                    checked = settings.strictTurnMode,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { 
                            var newSettings = it.copy(strictTurnMode = enabled)
                            // Strict Mode requires Auto-Advance to be ON
                            if (enabled) {
                                newSettings = newSettings.copy(autoNextTurn = true)
                            }
                            newSettings
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.error,
                        checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                    )
                )
            }

            if (!settings.strictTurnMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Allow Eliminated Input", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Eliminated players keep their turns",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.allowEliminatedInput,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(allowEliminatedInput = enabled) }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Scroll to Active", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Keep current player in view",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.autoScrollToActivePlayer,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(autoScrollToActivePlayer = enabled) }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-Advance Turn", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Switch player after scoring",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    // Lock this setting to ON if Strict Turn Mode is active
                    enabled = !settings.strictTurnMode, 
                    checked = settings.autoNextTurn || settings.strictTurnMode,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(autoNextTurn = enabled) }
                    }
                )
            }
 
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Show Help Link", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Toggle Help icon in the bottom bar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.showHelpInNavBar,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(showHelpInNavBar = enabled) }
                    }
                )
            }
        }
    }
}}

