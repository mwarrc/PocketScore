package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.mwarrc.pocketscore.domain.model.AppSettings

/**
 * Section for backup and sharing options.
 * Includes backup center access, export/import records, and snapshot permissions/status.
 *
 * @param settings The current [AppSettings].
 * @param onNavigateToBackups Callback to navigate to backups screen.
 * @param onLinkBackupsFolder Callback to link an external backups folder.
 * @param backupsFolderUri The currently linked folder URI.
 * @param onExportRecords Callback to export records.
 * @param onImportRecords Callback to import records.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSection(
    settings: AppSettings,
    onNavigateToBackups: () -> Unit,
    onLinkBackupsFolder: () -> Unit,
    backupsFolderUri: String?,
    onExportRecords: () -> Unit,
    onImportRecords: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Backup & Sharing",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        // ── Backup Center — navigable card ──
        Surface(
            onClick = onNavigateToBackups,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Backup Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Manage snapshots, sharing & recovery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Open Backup Center",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                onClick = onExportRecords,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.IosShare,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Export All",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                onClick = onImportRecords,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.FileDownload,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Import File",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Surface(
            onClick = onLinkBackupsFolder,
            color = if (backupsFolderUri == null) 
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            border = BorderStroke(
                1.dp, 
                if (backupsFolderUri == null) 
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                else 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (backupsFolderUri == null) Icons.Default.Warning else Icons.Default.FolderOpen,
                    null,
                    tint = if (backupsFolderUri == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        if (backupsFolderUri == null) "Backup Vault: Unlinked" else "Backup Vault: Active",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (backupsFolderUri == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (backupsFolderUri == null)
                            "Unlinked records won't survive an app uninstall."
                        else
                            "Triple-Shield mirror protection is active.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
