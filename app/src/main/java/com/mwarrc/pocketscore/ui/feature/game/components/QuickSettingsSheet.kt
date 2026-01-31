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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
            Text(
                "Quick Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

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
                        onUpdateSettings { it.copy(strictTurnMode = enabled) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.error,
                        checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                    )
                )
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
                    checked = settings.autoNextTurn,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(autoNextTurn = enabled) }
                    }
                )
            }

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
}

