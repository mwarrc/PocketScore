package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Scoreboard Layout", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (settings.defaultLayout == ScoreboardLayout.LIST) {
                            "Current: List"
                        } else {
                            "Current: Grid"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        onUpdateSettings {
                            it.copy(
                                defaultLayout = if (it.defaultLayout == ScoreboardLayout.LIST) {
                                    ScoreboardLayout.GRID
                                } else {
                                    ScoreboardLayout.LIST
                                }
                            )
                        }
                    }
                ) {
                    Icon(
                        if (settings.defaultLayout == ScoreboardLayout.LIST) {
                            Icons.Outlined.GridView
                        } else {
                            Icons.Outlined.ViewAgenda
                        },
                        null
                    )
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
                        if (settings.strictTurnMode) {
                            "Security Active"
                        } else {
                            "Can be enabled for fair play"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (settings.strictTurnMode) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                if (!settings.strictTurnMode) {
                    Button(
                        onClick = {
                            onUpdateSettings { it.copy(strictTurnMode = true) }
                        }
                    ) {
                        Text("Turn On")
                    }
                } else {
                    Icon(
                        Icons.Default.Lock,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
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

