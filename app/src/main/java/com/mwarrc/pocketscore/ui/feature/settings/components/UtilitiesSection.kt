package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.mwarrc.pocketscore.ui.feature.home.components.PrivacySettingsCard
import com.mwarrc.pocketscore.ui.feature.settings.InfoContent

/**
 * Section for utility settings.
 * Includes privacy, cost calculator, and device identity settings.
 *
 * @param settings The current [AppSettings].
 * @param onUpdateSettings Callback to update the settings.
 */
@Composable
fun UtilitiesSection(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var showDeviceNameDialog by remember { mutableStateOf(false) }
    var tempDeviceName by remember { mutableStateOf(settings.customDeviceName ?: "") }

    var infoDialogContent by remember { mutableStateOf<InfoContent?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Utilities",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        PrivacySettingsCard(
            settings = settings,
            onUpdateSettings = onUpdateSettings
        )

        SettingsItem(
            title = "Session Cost Calculator",
            subtitle = "Calculate match fees and debts in History",
            icon = Icons.Default.Payments,
            onIconClick = {
                infoDialogContent = InfoContent(
                    title = "Session Cost Calculator",
                    description = "Adds a 'Fee breakdown' to your match history. You can set a price per match, and the app will calculate individual player debts based on who played or lost. Useful for tracking table fees or side bets.",
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

        SettingsItem(
            title = "Device Identity",
            subtitle = settings.customDeviceName ?: "Set a custom record source name",
            icon = Icons.Default.Smartphone,
            onIconClick = {
                infoDialogContent = InfoContent(
                    title = "Device Identity",
                    description = "Defines the source name attached to your exported match records. If you share your game data with friends, they will see this name as the 'Generating Device'.",
                    icon = Icons.Default.Smartphone
                )
            },
            onClick = {
                tempDeviceName = settings.customDeviceName ?: ""
                showDeviceNameDialog = true
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

    if (showDeviceNameDialog) {
        AlertDialog(
            onDismissRequest = { showDeviceNameDialog = false },
            icon = { Icon(Icons.Default.Smartphone, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Device Identity") },
            text = {
                Column {
                    Text(
                        "This name will be attached to games you share. It identifies this device as the source.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempDeviceName,
                        onValueChange = { tempDeviceName = it },
                        label = { Text("Device Name") },
                        placeholder = { Text(android.os.Build.MODEL) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings { it.copy(customDeviceName = tempDeviceName.trim().ifBlank { null }) }
                        showDeviceNameDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Save Name") }
            },
            dismissButton = {
                TextButton(onClick = { showDeviceNameDialog = false }) { Text("Cancel") }
            }
        )
    }
}
