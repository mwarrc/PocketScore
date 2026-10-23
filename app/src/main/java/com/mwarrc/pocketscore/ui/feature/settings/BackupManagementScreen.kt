package com.mwarrc.pocketscore.ui.feature.settings

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import com.mwarrc.pocketscore.ui.feature.settings.components.SnapshotItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Clean, professional, and streamlined screen for managing game backups.
 * 
 * Provides automated rolling daily backups, external persistent folder linking,
 * and manual snapshot creation and restoration.
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

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var snapshotName by remember { mutableStateOf("") }
    var snapshotToDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showCreateDialog) {
        CreateSnapshotDialog(
            snapshotName = snapshotName,
            onNameChange = { snapshotName = it },
            onConfirm = {
                if (snapshotName.isNotBlank()) {
                    onCreateManualSnapshot(snapshotName)
                    snapshotName = ""
                    showCreateDialog = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Backup created successfully")
                    }
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
                    snackbarHostState.showSnackbar("Backup deleted")
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
                    Text(
                        "Backup & Restore",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    val defaultName = "Backup-${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}"
                    snapshotName = defaultName
                    showCreateDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Backup")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 0. Why linking matters (only while no folder is linked) ──
            if (backupsFolderUri == null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Link a folder to keep your records safe",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "Backups currently live inside the app's private storage, so uninstalling PocketScore - or clearing its data - deletes every match and player record with it.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    lineHeight = 18.sp
                                )
                                Text(
                                    "Link an external folder below and your backups are written somewhere the app can't take with it. It's the single most important step for keeping years of records.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    lineHeight = 18.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Button(
                                    onClick = onLinkBackupsFolder,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.FolderShared, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Link Folder Now", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // ── 1. Status Overview Card ──
            item {
                val isAutoOn = settings.localSnapshotsEnabled
                Surface(
                    color = if (isAutoOn) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else 
                        MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = if (isAutoOn) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isAutoOn) Icons.Default.CheckCircle else Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (isAutoOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isAutoOn) "Automatic Protection Active" else "Auto-Backups Paused",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            val lastBackupText = if (settings.lastLocalSnapshotTime > 0) {
                                "Last saved: ${dateFormat.format(Date(settings.lastLocalSnapshotTime))} • ${settings.lastSnapshotSize}"
                            } else {
                                "No backups recorded yet"
                            }
                            Text(
                                lastBackupText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // ── 2. Settings Card (Daily Auto-Backup & Linked Storage) ──
            item {
                Text(
                    "Preferences",
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Daily Auto-Backup Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Daily Rolling Backup",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Saves once per day on app launch and keeps the latest 7 versions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            Switch(
                                checked = settings.localSnapshotsEnabled,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings { it.copy(localSnapshotsEnabled = enabled) }
                                }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // External Persistent Vault / Folder
                        val isLinked = backupsFolderUri != null
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isLinked) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLinkBackupsFolder() }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Icon(
                                    if (isLinked) Icons.Default.FolderOpen else Icons.Default.FolderShared,
                                    contentDescription = null,
                                    tint = if (isLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (isLinked) "External Folder Linked" else "Link External Folder",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLinked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    val folderName = if (isLinked) {
                                        Uri.parse(backupsFolderUri).path?.split("/")?.lastOrNull() ?: "Selected Folder"
                                    } else {
                                        "Backups survive if you uninstall the app."
                                    }
                                    Text(
                                        folderName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                                Button(
                                    onClick = onLinkBackupsFolder,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = if (isLinked) {
                                        ButtonDefaults.filledTonalButtonColors()
                                    } else {
                                        ButtonDefaults.buttonColors()
                                    }
                                ) {
                                    Text(
                                        if (isLinked) "Change" else "Select",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. Manual Actions Row ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val defaultName = "Backup-${SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.US).format(Date())}"
                            snapshotName = defaultName
                            showCreateDialog = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Create Backup", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // ── 4. History / Available Backups Header ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Available Backups (${snapshots.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── 5. Backups List ──
            if (snapshots.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Text(
                                "No Backups Saved",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Create a manual backup or enable daily auto-backups to protect your games.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(snapshots, key = { it.first }) { (name, timestamp) ->
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
}
