package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.settings.components.CreateSnapshotDialog
import com.mwarrc.pocketscore.ui.feature.settings.components.DeleteSnapshotDialog
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsDivider
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem
import com.mwarrc.pocketscore.ui.feature.settings.components.SnapshotItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Screen for managing backup snapshots and automated backup settings.
 *
 * Provides options to create manual snapshots, toggle automated backups, and view/restore history.
 *
 * @param snapshots List of pairs (name, timestamp) of existing snapshots.
 * @param onBack Callback to navigate back.
 * @param onCreateSnapshot Callback to create a new manual snapshot.
 * @param onRestoreSnapshot Callback to restore a snapshot by name.
 * @param onDeleteSnapshot Callback to delete a snapshot by name.
 * @param onShareSnapshot Callback to share a snapshot by name.
 * @param onTriggerCloudBackup Callback to trigger cloud backup (if enabled).
 * @param onUpdateSettings Callback to update application settings.
 * @param onRefresh Callback to refresh the snapshot list.
 * @param settings Current application settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupManagementScreen(
    snapshots: List<Pair<String, Long>>,
    onBack: () -> Unit,
    onCreateManualSnapshot: (String) -> Unit,
    onRestore: (String) -> Unit,
    onDelete: (String) -> Unit,
    onShare: (String) -> Unit,
    onTriggerCloudBackup: () -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onRefresh: () -> Unit,
    onLinkBackupsFolder: () -> Unit,
    backupsFolderUri: String?,
    settings: AppSettings
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var snapshotName by remember { mutableStateOf("") }
    var snapshotToDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Dialogs
    if (showCreateDialog) {
        CreateSnapshotDialog(
            snapshotName = snapshotName,
            onNameChange = { snapshotName = it },
            onConfirm = {
                if (snapshotName.isNotBlank()) {
                    onCreateManualSnapshot(snapshotName)
                    snapshotName = ""
                    showCreateDialog = false
                }
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (snapshotToDelete != null) {
        DeleteSnapshotDialog(
            snapshotName = snapshotToDelete ?: "",
            onConfirm = {
                onDelete(snapshotToDelete!!)
                snapshotToDelete = null
                scope.launch {
                    snackbarHostState.showSnackbar("Snapshot deleted")
                }
            },
            onDismiss = { snapshotToDelete = null }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Backup,
                            null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Backup Center",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Create Snapshot")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val isLinked = settings.backupsFolderUri != null
                val isAutoOn = settings.localSnapshotsEnabled
                val layers = listOf(
                    "Internal" to true,
                    "Sync" to true,
                    "Vault" to isLinked,
                    "Downloads" to isAutoOn
                )
                val activeCount = layers.count { it.second }
                val allGood = activeCount == 4
                
                Surface(
                    color = if (allGood) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
                            else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp, 
                        if (allGood) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (allGood) Icons.Default.VerifiedUser else Icons.Default.Shield,
                                null,
                                tint = if (allGood) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (allGood) "Maximum Survival Active" else "Redundancy: $activeCount/4 Layers",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (allGood) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (allGood) "Every snapshot is safely mirrored across all 4 defense layers. Your game records are safe."
                            else "Currently mirrored to: ${layers.filter { it.second }.joinToString { it.first }}. Link a Vault and enable Auto-Snapshots for max security.",
                            style = MaterialTheme.typography.bodySmall,
                            color = (if (allGood) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer).copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Text(
                    "Primary Vault (External Folder)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            item {
                Surface(
                    color = if (backupsFolderUri == null) 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    else 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp, 
                        if (backupsFolderUri == null) 
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLinkBackupsFolder() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (backupsFolderUri == null) Icons.Default.Link else Icons.Default.FolderOpen,
                            null,
                            tint = if (backupsFolderUri == null) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (backupsFolderUri == null) "Connect Permanent Folder" else "Backup Vault Linked",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (backupsFolderUri == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                if (backupsFolderUri == null) 
                                    "Critical for survival after app uninstalls." 
                                else 
                                    "Connected to: ${Uri.parse(backupsFolderUri).path?.split("/")?.lastOrNull() ?: "Storage"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        if (backupsFolderUri == null) {
                            Button(
                                onClick = onLinkBackupsFolder,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Select", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Auto-Snapshots",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Daily Auto-Snapshots",
                    subtitle = "Protect your data with automated daily saves",
                    icon = Icons.Default.AutoMode,
                    trailing = {
                        Switch(
                            checked = settings.localSnapshotsEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(localSnapshotsEnabled = enabled) }
                            }
                        )
                    }
                )
            }

            item {
                SettingsItem(
                    title = "Post-Game Snapshots",
                    subtitle = "Automatically save after every finished game",
                    icon = Icons.Default.HistoryEdu,
                    trailing = {
                        Switch(
                            checked = settings.snapshotAfterGameEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(snapshotAfterGameEnabled = enabled) }
                            }
                        )
                    }
                )
            }

            item {
                SettingsItem(
                    title = "Pulse Protection",
                    subtitle = "Random background snapshots for extra safety",
                    icon = Icons.Default.Favorite,
                    trailing = {
                        Switch(
                            checked = settings.randomSnapshotsEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(randomSnapshotsEnabled = enabled) }
                            }
                        )
                    }
                )
            }

            item {
                SettingsItem(
                    title = "Universal Mirror protection",
                    subtitle = "Silently mirror copies to Vault, Downloads & Android Folder",
                    icon = Icons.Default.Sync,
                    trailing = {
                        Switch(
                            checked = settings.redundantSyncEnabled,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(redundantSyncEnabled = enabled) }
                            }
                        )
                    }
                )
            }

            item {
                SettingsItem(
                    title = "Last Snapshot",
                    subtitle = if (settings.lastLocalSnapshotTime == 0L)
                        "No snapshots recorded"
                    else
                        dateFormat.format(Date(settings.lastLocalSnapshotTime)),
                    icon = Icons.Default.Schedule
                )
            }

            item {
                SettingsItem(
                    title = "Storage Used",
                    subtitle = settings.lastSnapshotSize.ifEmpty { "0 KB" },
                    icon = Icons.Default.Storage
                )
            }

            item {
                SettingsItem(
                    title = "Instant Auto-Snapshot",
                    subtitle = "Create a snapshot right now",
                    icon = Icons.Default.FlashOn,
                    onClick = if (settings.localSnapshotsEnabled) {
                        { onCreateManualSnapshot("Auto-Snapshot ${dateFormat.format(Date())}") }
                    } else null
                )
            }

            item {
                SettingsDivider(alpha = 0.5f)
            }

            // History Section
            item {
                Text(
                    "History",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            item {
                Text(
                    "${snapshots.size} saved snapshot${if (snapshots.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            if (snapshots.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            "No snapshots yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(snapshots) { (name, timestamp) ->
                SnapshotItem(
                    name = name,
                    date = dateFormat.format(Date(timestamp)),
                    onRestore = {
                        onRestore(name)
                        scope.launch {
                            snackbarHostState.showSnackbar("Preparing restore preview...")
                        }
                    },
                    onDelete = { snapshotToDelete = name },
                    onShare = { onShare(name) }
                )
            }
        }
    }
}
