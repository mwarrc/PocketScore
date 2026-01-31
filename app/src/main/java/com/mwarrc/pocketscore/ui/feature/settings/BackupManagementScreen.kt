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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupManagementScreen(
    snapshots: List<Pair<String, Long>>,
    onBack: () -> Unit,
    onCreateSnapshot: (String) -> Unit,
    onRestoreSnapshot: (String) -> Unit,

    onDeleteSnapshot: (String) -> Unit,
    onShareSnapshot: (String) -> Unit,
    onTriggerCloudBackup: () -> Unit,
    onToggleLocalSnapshots: (Boolean) -> Unit,
    settings: AppSettings
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    var showCreateDialog by remember { mutableStateOf(false) }
    var snapshotName by remember { mutableStateOf("") }
    var snapshotToRestore by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Snapshot") },
            text = {
                Column {
                    Text("Save a point-in-time version of all your current records.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = snapshotName,
                        onValueChange = { snapshotName = it },
                        label = { Text("Snapshot Name") },
                        placeholder = { Text("e.g. Before merging with Bob") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (snapshotName.isNotBlank()) {
                            onCreateSnapshot(snapshotName)
                            snapshotName = ""
                            showCreateDialog = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (snapshotToRestore != null) {
        AlertDialog(
            onDismissRequest = { snapshotToRestore = null },
            title = { Text("Restore Snapshot?") },
            text = { Text("This will merge data from the snapshot '${snapshotToRestore}' into your current records. Duplicate games will be ignored.") },
            confirmButton = {
                Button(
                    onClick = {
                        onRestoreSnapshot(snapshotToRestore!!)
                        snapshotToRestore = null
                        scope.launch {
                            snackbarHostState.showSnackbar("Snapshot restored successfully")
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { snapshotToRestore = null }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        Spacer(Modifier.statusBarsPadding())
        Scaffold(
            modifier = Modifier.displayCutoutPadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Backup Center", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, "Create Local Snapshot")
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            
            // Cloud Info (Now local snapshots info)
            // Header Section: Status & Toggle
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daily Auto-Snapshots",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Protect your data with automated daily saves.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = settings.localSnapshotsEnabled,
                            onCheckedChange = onToggleLocalSnapshots,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Last Snapshot",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (settings.lastLocalSnapshotTime == 0L) "No snapshots recorded" else dateFormat.format(Date(settings.lastLocalSnapshotTime)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Storage Used",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                settings.lastSnapshotSize.ifEmpty { "0 KB" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = { onCreateSnapshot("Auto-Snapshot ${dateFormat.format(Date())}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        enabled = settings.localSnapshotsEnabled,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.Default.AutoMode, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Instant Auto-Snapshot")
                    }
                }
            }

            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }

            item {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        "History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Previously saved local snapshots.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            if (snapshots.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text("No snapshots yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(snapshots) { (name, timestamp) ->
                SnapshotCard(
                    name = name,
                    date = dateFormat.format(Date(timestamp)),
                    onRestore = { snapshotToRestore = name },
                    onDelete = { onDeleteSnapshot(name) },
                    onShare = { onShareSnapshot(name) }
                )
            }
        }
    }
}
}



@Composable
fun SnapshotCard(
    name: String,
    date: String,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
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
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onRestore) {
                    Icon(
                        Icons.Default.Restore,
                        "Restore",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
