package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState 
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsDivider
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem
import com.mwarrc.pocketscore.ui.feature.settings.components.StrictModeInfoDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onNavigateToBackups: () -> Unit,
    onNavigateToFeedback: () -> Unit
) {
    var showStrictModeInfo by remember { mutableStateOf(false) }
    var showEnforceDialog by remember { mutableStateOf(false) }
    var upcomingFeaturesClickCount by remember { mutableIntStateOf(0) }
    var showDeviceNameDialog by remember { mutableStateOf(false) }
    var tempDeviceName by remember { mutableStateOf(settings.customDeviceName ?: "") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Reset click count when disabled
    LaunchedEffect(settings.showComingSoonFeatures) {
        if (!settings.showComingSoonFeatures) {
            upcomingFeaturesClickCount = 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToGame) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // GAMEPLAY SETTINGS
            Text(
                "Gameplay",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            SettingsItem(
                title = "Haptic Feedback",
                subtitle = "Subtle vibration on score changes",
                icon = Icons.Default.TouchApp,
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
                trailing = {
                    Switch(
                        checked = settings.leaderSpotlightEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(leaderSpotlightEnabled = enabled) }
                        }
                    )
                }
            )

/*
            SettingsItem(
                title = "Custom Numpad",
                subtitle = "Use minimal in-app keyboard for scoring",
                icon = Icons.Default.Keyboard,
                trailing = {
                    Switch(
                        checked = settings.useCustomKeyboard,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(useCustomKeyboard = enabled) }
                        }
                    )
                }
            )
*/

            SettingsItem(
                title = "Saved Players",
                subtitle = "Show saved players on Home Screen",
                icon = Icons.Default.Group,
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

            if (showEnforceDialog) {
                AlertDialog(
                    onDismissRequest = { showEnforceDialog = false },
                    icon = { Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.error) },
                    title = { Text("Enforce Strict Mode?") },
                    text = {
                        Text("When this is enabled, 'Strict Turn Mode' will be forced ON and cannot be turned off during a game session. Only disable this if you trust all players.")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onUpdateSettings { it.copy(enforceStrictMode = true, strictTurnMode = true) }
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

            SettingsItem(
                title = "In-Match Mode Alerts",
                subtitle = "Banner when mode changes (Strict/Free)",
                icon = Icons.Default.Info,
                trailing = {
                    Switch(
                        checked = settings.showStrictModeBanner,
                        onCheckedChange = { enabled ->
                            onUpdateSettings { it.copy(showStrictModeBanner = enabled) }
                        }
                    )
                }
            )

            if (showStrictModeInfo) {
                StrictModeInfoDialog(onDismissRequest = { showStrictModeInfo = false })
            }



            SettingsDivider(alpha = 0.5f)

            // UTILITIES
            Text(
                "Utilities",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            SettingsItem(
                title = "Session Cost Calculator",
                subtitle = "Calculate match fees and debts in History",
                icon = Icons.Default.Payments,
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
                onClick = { 
                    tempDeviceName = settings.customDeviceName ?: ""
                    showDeviceNameDialog = true 
                }
            )

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

            SettingsDivider(alpha = 0.5f)

            // APPEARANCE SETTINGS
            Text(
                "Appearance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Switch between Light and Dark",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf("System", "Light", "Dark")
                    val selectedIndex = when (settings.isDarkMode) {
                        null -> 0
                        false -> 1
                        true -> 2
                    }
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = {
                                val newVal = when (index) {
                                    0 -> null
                                    1 -> false
                                    2 -> true
                                    else -> null
                                }
                                onUpdateSettings { it.copy(isDarkMode = newVal) }
                            },
                            selected = index == selectedIndex,
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.ViewModule, null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Default Layout", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Preferred scoreboard view",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val options = listOf("Grid", "List")
                    options.forEachIndexed { index, label ->
                        val isGridChoice = label == "Grid"
                        val isSelected = (settings.defaultLayout == ScoreboardLayout.GRID && isGridChoice) ||
                                       (settings.defaultLayout == ScoreboardLayout.LIST && !isGridChoice)
                        
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = {
                                onUpdateSettings { 
                                    it.copy(defaultLayout = if (isGridChoice) ScoreboardLayout.GRID else ScoreboardLayout.LIST) 
                                }
                            },
                            selected = isSelected,
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (isGridChoice) Icons.Outlined.GridView else Icons.Outlined.ViewAgenda,
                                        null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        )
                    }
                }
            }

            SettingsDivider(alpha = 0.5f)

            // LIMITS
            Text(
                "Limits",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Max Players", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Allowed in setup: ${settings.maxPlayers}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Slider(
                    value = settings.maxPlayers.toFloat(),
                    onValueChange = { value ->
                        onUpdateSettings { it.copy(maxPlayers = value.toInt()) }
                    },
                    valueRange = 8f..100f, /* to be replace with a better input method */
                    steps = 24,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))


            // BACKUP & SHARING
            Text(
                "Backup & Sharing",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            SettingsItem(
                title = "Share Game Records",
                subtitle = "Share history and friends as a file",
                icon = Icons.Default.IosShare,
                onClick = onExportBackup
            )

            SettingsItem(
                title = "Import Game Records",
                subtitle = "Load data from a .pscore file",
                icon = Icons.Default.FileDownload,
                onClick = onImportBackup
            )

            SettingsItem(
                title = "Backup Center",
                subtitle = "Manage local snapshots and history",
                icon = Icons.Default.Security,
                onClick = onNavigateToBackups
            )

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Security,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Safe Snapshot System",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            if (settings.localSnapshotsEnabled) 
                                "Active and protecting your data with daily snapshots." 
                            else 
                                "Turn on to enable automatic daily snapshots and manual recovery points to keep your data safe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (settings.localSnapshotsEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SettingsDivider(alpha = 0.5f)
            
            // FEEDBACK & SUPPORT
            Text(
                "Support",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Surface(
                onClick = onNavigateToFeedback,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.RateReview,
                                null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Feedback & Support",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Report bugs or suggest features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // ABOUT LINK - SEPARATED
            Surface(
                onClick = onNavigateToAbout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Info,
                                null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "About PocketScore",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Mission, Version and Info",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                "v0.1.2 Expressive",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

