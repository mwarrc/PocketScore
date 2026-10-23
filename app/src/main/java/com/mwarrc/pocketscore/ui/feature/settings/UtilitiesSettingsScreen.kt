package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.home.components.PrivacySettingsCard
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingInfoDialog
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilitiesSettingsScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onExportRecords: () -> Unit,
    onImportRecords: () -> Unit,
    onBack: () -> Unit
) {
    var showDeviceNameDialog by remember { mutableStateOf(false) }
    var tempDeviceName by remember { mutableStateOf(settings.customDeviceName ?: "") }
    var infoDialogContent by remember { mutableStateOf<InfoContent?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .displayCutoutPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Utilities & Sharing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Match Cost Calculator
            item {
                Text(
                    "Match Cost & Debt Tracking",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Session Cost Calculator",
                    subtitle = "Calculate table fees and debts in match history",
                    icon = Icons.Default.Payments,
                    onIconClick = {
                        infoDialogContent = InfoContent(
                            title = "Session Cost Calculator",
                            description = "Adds fee breakdown analysis to your match history. You can configure a price per match, and the app calculates individual player debts based on who lost or played.",
                            icon = Icons.Default.Payments
                        )
                    },
                    trailing = {
                        Switch(
                            checked = settings.matchSplitEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(matchSplitEnabled = enabled) }
                            }
                        )
                    }
                )
            }

            if (settings.matchSplitEnabled) {
                item {
                    SettingsItem(
                        title = "Settlement Method",
                        subtitle = "Set in the settle screen for each match",
                        icon = Icons.Default.Payments,
                        onIconClick = {
                            infoDialogContent = InfoContent(
                                title = "Settlement Method",
                                description = "The settlement method is chosen on the settle screen, per match, so it can match how each table was actually paid out. Open a match from history and use the Split tab to change it.",
                                icon = Icons.Default.Payments
                            )
                        }
                    )
                }
            }

            // Device Identity
            item {
                Text(
                    "Device Identity",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Generating Device Name",
                    subtitle = settings.customDeviceName?.ifEmpty { "Default (Phone Model)" } ?: "Default (Phone Model)",
                    icon = Icons.Default.Smartphone,
                    onIconClick = {
                        infoDialogContent = InfoContent(
                            title = "Device Identity",
                            description = "Defines the source name attached to your exported match records. If you share game data with friends, they will see this label as the Generating Device.",
                            icon = Icons.Default.Smartphone
                        )
                    },
                    onClick = {
                        tempDeviceName = settings.customDeviceName ?: ""
                        showDeviceNameDialog = true
                    }
                )
            }

            // Privacy Section
            item {
                Text(
                    "Privacy & Analytics",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Share usage data",
                    subtitle = if (settings.analyticsEnabled) "Helps improve PocketScore" else "Disabled",
                    icon = Icons.Default.Analytics,
                    trailing = {
                        Switch(
                            checked = settings.analyticsEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(analyticsEnabled = enabled) }
                            }
                        )
                    },
                    onClick = {
                        onUpdateSettings { it.copy(analyticsEnabled = !it.analyticsEnabled) }
                    }
                )
            }

            // Sharing & Backup
            item {
                Text(
                    "Sharing & Backup",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Share your records with other PocketScore players, or use these to back up and restore this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Primary action: sharing is the main reason to move data around.
                        Button(
                            onClick = onExportRecords,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Share Records", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onExportRecords,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Backup")
                            }

                            FilledTonalButton(
                                onClick = onImportRecords,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Restore")
                            }
                        }

                        Text(
                            "Backup saves a full copy of your matches and players. Restore merges a shared or backed-up file into this device.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }

    if (showDeviceNameDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceNameDialog = false },
            title = { Text("Device Identity") },
            text = {
                Column {
                    Text(
                        "Set a recognizable name attached to exported matches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempDeviceName,
                        onValueChange = { tempDeviceName = it },
                        label = { Text("Device Name") },
                        placeholder = { Text("e.g. Club Table 1") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = tempDeviceName.trim()
                        onUpdateSettings { it.copy(customDeviceName = trimmed.ifEmpty { null }) }
                        showDeviceNameDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDeviceNameDialog = false }) { Text("Cancel") }
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
}
