package com.mwarrc.pocketscore.ui.feature.settings
import androidx.compose.foundation.layout.*
import com.mwarrc.pocketscore.domain.model.AppSettings
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.launch
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupManagementScreen(
    snapshots: List<Pair<String, Long>>,
    onBack: () -> Unit,
    onCreateSnapshot: (String) -> Unit,
    onRestoreSnapshot: (String) -> Unit,
    onDeleteSnapshot: (String) -> Unit,
    onShareSnapshot: (String) -> Unit,
    onExportSnapshot: suspend (String) -> Boolean,
    onTriggerCloudBackup: () -> Unit,
    onToggleLocalSnapshots: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    settings: AppSettings
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    var showCreateDialog by remember { mutableStateOf(false) }
    var snapshotName by remember { mutableStateOf("") }
    var snapshotToRestore by remember { mutableStateOf<String?>(null) }
    var snapshotToDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Create Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { 
                Text(
                    "Create Snapshot",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Save a point-in-time version of all your current records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = snapshotName,
                        onValueChange = { snapshotName = it },
                        label = { Text("Snapshot Name") },
                        placeholder = { Text("e.g. Before merging with Bob") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        if (snapshotName.isNotBlank()) {
                            onCreateSnapshot(snapshotName)
                            snapshotName = ""
                            showCreateDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { 
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Create") 
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }


    // Delete Confirmation Dialog
    if (snapshotToDelete != null) {
        AlertDialog(
            onDismissRequest = { snapshotToDelete = null },
            icon = {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.DeleteForever,
                            null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            },
            title = { 
                Text(
                    "Delete Snapshot?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = { 
                Text(
                    "Are you sure you want to permanently delete '$snapshotToDelete'? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSnapshot(snapshotToDelete!!)
                        snapshotToDelete = null
                        scope.launch {
                            snackbarHostState.showSnackbar("Snapshot deleted")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { snapshotToDelete = null }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
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
            // Auto-Snapshots Section
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
                            onCheckedChange = onToggleLocalSnapshots
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
                        { onCreateSnapshot("Auto-Snapshot ${dateFormat.format(Date())}") }
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
                        onRestoreSnapshot(name)
                        scope.launch {
                            snackbarHostState.showSnackbar("Opening Import Manager...")
                        }
                    },
                    onDelete = { snapshotToDelete = name },
                    onShare = { onShareSnapshot(name) },
                    onExport = { 
                        scope.launch {
                            val success = onExportSnapshot(name)
                            if (success) {
                                snackbarHostState.showSnackbar("Exported to Documents/PocketScore Backups")
                            } else {
                                snackbarHostState.showSnackbar("Export Failed")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SnapshotItem(
    name: String,
    date: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Backup,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(onClick = onRestore) {
                    Icon(
                        Icons.Default.Restore,
                        "Restore",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onExport) {
                    Icon(
                        Icons.Default.SaveAlt,
                        "Export to Documents",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Default.Share,
                        "Share",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        "Delete",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}
